/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordwrite;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.codehaus.jackson.map.ObjectMapper;

import backtype.storm.tuple.Tuple;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.reindexing.common.ReindexingTuple;

/**
 *
 * @author ymamakis
 */
public class TuplePersistence implements Runnable {

	private final List<Tuple> tuples;
	private final CountDownLatch latch;
    
	private final SolrDocumentHandler solrHandlerIngst;
	private final CloudSolrServer solrServerIngst;
	private final EdmMongoServer mongoServerIngst;
    private final FullBeanHandler mongoHandlerIngst;
    
	private final SolrDocumentHandler solrHandlerProd;
	private final CloudSolrServer solrServerProd;
	private final EdmMongoServer mongoServerProd;
    private final FullBeanHandler mongoHandlerProd;

    private final ObjectMapper om = new ObjectMapper();
    
    public TuplePersistence(FullBeanHandler mongoHandlerIngst, EdmMongoServer mongoServerIngst, CloudSolrServer solrServerIngst, SolrDocumentHandler solrHandlerIngst,
    						FullBeanHandler mongoHandlerProd, EdmMongoServer mongoServerProd, CloudSolrServer solrServerProd, SolrDocumentHandler solrHandlerProd,
    						List<Tuple> tuples, CountDownLatch latch) {
        
        this.mongoHandlerIngst = mongoHandlerIngst;
        this.mongoServerIngst = mongoServerIngst;
        this.solrServerIngst = solrServerIngst;
        this.solrHandlerIngst = solrHandlerIngst;
        
        this.mongoHandlerProd = mongoHandlerProd;
        this.mongoServerProd = mongoServerProd;
        this.solrServerProd = solrServerProd;
        this.solrHandlerProd = solrHandlerProd;
       
        this.tuples = tuples;
        this.latch= latch;
    }

    @Override
    public void run() {
		//write data to INGESTION&PRODUCTION
    	Logger.getLogger("Saving...");
		save(solrHandlerIngst, solrServerIngst, mongoServerIngst,
				mongoHandlerIngst, solrHandlerProd, solrServerProd,
				mongoServerProd, mongoHandlerProd);
		//Notify the main thread that you finished and that it does not have to wait for you now
		latch.countDown();
		Logger.getLogger("Finished processing and persisting");
    }

	private void save(SolrDocumentHandler solrHandlerIngst,
			CloudSolrServer solrServerIngst,
			EdmMongoServer mongoServerIngst,
			FullBeanHandler mongoHandlerIngst,
			SolrDocumentHandler solrHandlerProd,
			CloudSolrServer solrServerProd,
			EdmMongoServer mongoServerProd,
			FullBeanHandler mongoHandlerProd) {
		for (Tuple tuple : tuples) {
            ReindexingTuple task = ReindexingTuple.fromTuple(tuple);
            try {           
                FullBeanImpl fBean = mongoServerProd.searchByAbout(FullBeanImpl.class, task.getIdentifier());
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.INFO, "*** Saving record " + fBean.getAbout() + " ... ***");
                cleanFullBean(fBean);
                appendEntities(fBean, task.getEntityWrapper());
                
                ModifiableSolrParams params = new ModifiableSolrParams();
                params.add("q", "europeana_id:" + ClientUtils.escapeQueryChars(fBean.getAbout()));
                params.add("fl", "is_fulltext,has_thumbnails,has_media,filter_tags,facet_tags,has_landingpage");
                
                mongoHandlerProd.saveEdmClasses(fBean, true);
                mongoServerProd.getDatastore().save(fBean);
                SolrInputDocument solrDocumentProd = solrHandlerProd.generate(fBean);
                addFields(solrDocumentProd, solrServerProd.query(params));
                solrServerProd.add(solrDocumentProd);
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.INFO, "*** Record " + fBean.getAbout() + "is saved in Production. ***");
                
                FullBeanImpl fBeanIngst = mongoServerIngst.searchByAbout(FullBeanImpl.class, fBean.getAbout());
                if (fBeanIngst != null) {
                	mongoHandlerIngst.saveEdmClasses(fBean, true);
                	mongoServerIngst.getDatastore().save(fBean);
                	SolrInputDocument solrDocumentIngst = solrHandlerIngst.generate(fBean);
                	addFields(solrDocumentIngst, solrServerIngst.query(params));
					solrServerIngst.add(solrDocumentIngst);                	
					Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.INFO, "*** Record " + fBean.getAbout() + "is saved in Ingestion. ***");
                }
            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SolrServerException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
	}
	
	/**
	 * Add CRF fields
	 * @param doc
	 * @param resp
	 */
	private void addFields(SolrInputDocument doc, QueryResponse resp) {
		if(resp.getResults().size() > 0) {
            SolrDocument retrievedDoc = resp.getResults().get(0);
            if (retrievedDoc.containsKey("is_fulltext")) {
            	doc.addField("is_fulltext", retrievedDoc.get("is_fulltext"));
            }
            if (retrievedDoc.containsKey("has_thumbnails")) {
            	doc.addField("has_thumbnails", retrievedDoc.get("has_thumbnails"));
            }
            if (retrievedDoc.containsKey("has_media")) {
            	doc.addField("has_media", retrievedDoc.get("has_media"));
            }
            if (retrievedDoc.containsKey("filter_tags")) {
            	doc.addField("filter_tags", retrievedDoc.get("filter_tags"));
            }
            if (retrievedDoc.containsKey("facet_tags")) {
            	doc.addField("facet_tags", retrievedDoc.get("facet_tags"));
            }
            if (retrievedDoc.containsKey("has_landingpage")) {
            	doc.addField("has_landingpage", retrievedDoc.get("has_landingpage"));
            }
        }
	}
    
	/**
	 * Clean bean from old obsolete data
	 * @param fBean
	 */
    private void cleanFullBean(FullBeanImpl fBean) {
        ProxyImpl europeanaProxy = null;
        fBean = cleanEntitiesFromBean(fBean);
        int index = 0;
        for (ProxyImpl proxy : fBean.getProxies()) {
            if (proxy.isEuropeanaProxy()) {
                europeanaProxy = proxy;
                break;
            }
            index++;
        }

        europeanaProxy.setDcDate(null);
        europeanaProxy.setDcCoverage(null);
        europeanaProxy.setDctermsTemporal(null);
        europeanaProxy.setYear(null);
        europeanaProxy.setDctermsSpatial(null);
        europeanaProxy.setDcType(null);
        europeanaProxy.setDcSubject(null);
        europeanaProxy.setDcCreator(null);
        europeanaProxy.setDcContributor(null);

        fBean.getProxies().set(index, europeanaProxy);
    }

    /**
     * Clean bean from Timespans, Agents and Places (before appending the enriched data)
     * @param fBean
     * @return
     */
    private FullBeanImpl cleanEntitiesFromBean(FullBeanImpl fBean) {
        ProxyImpl europeanaProxy = null;
        for (ProxyImpl proxy : fBean.getProxies()) {
            if (proxy.isEuropeanaProxy()) {
                europeanaProxy = proxy;
                break;
            }
        }
        
        //*** TIMESPAN ***//
        List<TimespanImpl> cleanedTs = new ArrayList<>();
        List<TimespanImpl> timespans = fBean.getTimespans();
        if(timespans != null) {
            for (TimespanImpl ts : timespans) {
               if(!StringUtils.contains(ts.getAbout(),"semium")) {
                   cleanedTs.add(ts);
               }
            }
        }
        fBean.setTimespans(cleanedTs);
        
        //*** CONCEPT ***//
        List<ConceptImpl> cleanedConcepts  = new ArrayList<>();
        List<ConceptImpl> concepts = fBean.getConcepts();
        if(concepts != null) {
            for(ConceptImpl concept : concepts) {
                if(!StringUtils.contains(concept.getAbout(),"eionet")) {
                    cleanedConcepts.add(concept);
                }
            }
        }
        fBean.setConcepts(cleanedConcepts);
        //FIXME !!! Need Yorgos' code review! !!!
        //**********				AGENT				***********//
        //***			CREATOR				***//
        List<AgentImpl> beanAgents = fBean.getAgents();
        if (beanAgents != null) {
        	List<AgentImpl> agents = new CopyOnWriteArrayList<>(beanAgents);        	
        	Map<String,List<String>> creators = europeanaProxy.getDcCreator();
        	if(creators != null) {
        		for(String creator : creators.get("def")) {
        			for(AgentImpl agent : agents){
        				if (StringUtils.equals(creator, agent.getAbout())){
        					agents.remove(agent);
        				}
        			}
        		}
        	}        
        	//***			CREATOR			***//
        	Map<String,List<String>> contributors = europeanaProxy.getDcContributor();
        	if(contributors != null) {
        		for(String contributor : contributors.get("def")) {
        			for(AgentImpl agent : agents) {
        				if (StringUtils.equals(contributor, agent.getAbout())) {
        					agents.remove(agent);
        				}
        			}
        		}
        	}
        	fBean.setAgents(agents);        	
        }
        //**********		END OF AGENT			***********//
        
        //**********			PLACE				***********//
        Map<String,String> placeMap = new HashMap<>();
        List<PlaceImpl> beanPlaces = fBean.getPlaces();
//        Logger.getGlobal().info("*** Bean " + fBean.getAbout() + " has " + beanPlaces.size() + " places. ***");
        if(beanPlaces != null) {
            for(PlaceImpl place : beanPlaces) {
				if (place != null 
						&& place.getAbout() != null
						&& place.getIsPartOf() != null
						&& place.getIsPartOf().get("def") != null
						&& !place.getIsPartOf().get("def").isEmpty()) {
					placeMap.put(place.getAbout(), place.getIsPartOf().get("def").get(0));
				} else {
//					Logger.getGlobal().info("!!! Place "
//									+ (place != null ? place.getAbout() : "")
//									+ " getIsPartOf is null or empty !!!");
				}
            }
        }
        if (beanPlaces != null) {        	
        	Set<String> toRemove = new HashSet<>();
        	
        	//*** SPATIAL ***//
        	Map<String,List<String>> spatial = europeanaProxy.getDctermsSpatial();
        	if(spatial != null) {
        		for(String sp : spatial.get("def")) {
        			if (placeMap.containsKey(sp)) {
        				toRemove.addAll(removeParent(sp, placeMap));
        			}
        		}
        	}
        	//*** COVERAGE ***//
        	Map<String,List<String>> coverage = europeanaProxy.getDcCoverage();
        	if(coverage != null) {
        		for(String sp : coverage.get("def")) {
        			if (placeMap.containsKey(sp)){
        				toRemove.addAll(removeParent(sp, placeMap));
        			}
        		}
        	}
        	
        	List<PlaceImpl> places = new CopyOnWriteArrayList<>(beanPlaces);
        	for (String uri : toRemove) {
        		for (PlaceImpl place : places){
        			if (StringUtils.equals(place.getAbout(), uri)){
        				places.remove(place);
        			}
        		}
        	}
        	fBean.setPlaces(places);
        }
        //**********			END OF PLACE			***********//
        return fBean;
    }

    private List<String> removeParent(String sp, Map<String, String> placeMap) {
        List<String> parents = new ArrayList<>();
        if(sp != null) {
            parents.add(sp);
            if (placeMap.get(sp) != null && !StringUtils.equals(sp, placeMap.get(sp))) {
//                Logger.getGlobal().severe(sp);
                parents.addAll(removeParent(placeMap.get(sp), placeMap));
            }
        }
        return parents;
    }

    private void appendEntities(FullBeanImpl fBean, String entityWrapper) {
        try {
        	byte[] decoded = new Base64().decode(entityWrapper);
    		ByteArrayInputStream byteInput = new ByteArrayInputStream(decoded);
    		GZIPInputStream gZipInput = new GZIPInputStream(byteInput);
    		EntityWrapperList readValue = om.readValue(gZipInput, EntityWrapperList.class);
    		List<EntityWrapper> entities = readValue.getWrapperList();
    		
//            List<EntityWrapper> entities = om.readValue(entityWrapper, EntityWrapperList.class).getWrapperList();
            List<RetrievedEntity> enriched = convertToObjects(entities);
            ProxyImpl europeanaProxy = null;
            int index = 0;
            for (ProxyImpl proxy : fBean.getProxies()) {
                if (proxy.isEuropeanaProxy()) {
                    europeanaProxy = proxy;
                    break;
                }
                index++;
            }
            new EntityAppender().addEntities(fBean, europeanaProxy, enriched);
            fBean.getProxies().set(index, europeanaProxy);
        } catch (IOException ex) {
            Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<RetrievedEntity> convertToObjects(
            List<EntityWrapper> enrichments) throws IOException {
        List<RetrievedEntity> entities = new ArrayList<>();
        for (EntityWrapper entity : enrichments) {
            RetrievedEntity ret = new RetrievedEntity();
            ret.setOriginalField(entity.getOriginalField());
            ret.setOriginalLabel(entity.getOriginalValue());
            ret.setUri(entity.getUrl());
            if (entity.getClassName().equals(TimespanImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.getContextualEntity(), TimespanImpl.class));
            } else if (entity.getClassName().equals(AgentImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.getContextualEntity(), AgentImpl.class));
            } else if (entity.getClassName().equals(ConceptImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.getContextualEntity(), ConceptImpl.class));
            } else {
                ret.setEntity(new ObjectMapper().readValue(entity.getContextualEntity(), PlaceImpl.class));
            }
            entities.add(ret);
        }
        return entities;
    }
}

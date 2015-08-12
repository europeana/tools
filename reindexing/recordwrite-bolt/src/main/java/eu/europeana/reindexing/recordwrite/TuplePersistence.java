/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordwrite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
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
                FullBeanImpl fBean = mongoServerIngst.searchByAbout(FullBeanImpl.class, task.getIdentifier());
                cleanFullBean(fBean);
                appendEntities(fBean, task.getEntityWrapper());
                
                mongoHandlerIngst.saveEdmClasses(fBean, true);
                mongoServerIngst.getDatastore().save(fBean);
                solrServerIngst.add(solrHandlerIngst.generate(fBean));
                
                mongoHandlerProd.saveEdmClasses(fBean, true);
                mongoServerProd.getDatastore().save(fBean);
                solrServerProd.add(solrHandlerProd.generate(fBean));
            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SolrServerException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
	}
    
    
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

    private FullBeanImpl cleanEntitiesFromBean(FullBeanImpl fBean) {
        ProxyImpl europeanaProxy = null;
        int index = 0;
        for (ProxyImpl proxy : fBean.getProxies()) {
            if (proxy.isEuropeanaProxy()) {
                europeanaProxy = proxy;
                break;
            }
            index++;
        }
        List<TimespanImpl> cleanedTs = new ArrayList<>();
        List<TimespanImpl> timespans = fBean.getTimespans();
        if(timespans!=null) {
            for (TimespanImpl ts : timespans) {
               if(!StringUtils.contains(ts.getAbout(),"semium")){
                   cleanedTs.add(ts);
               }

            }
        }
        fBean.setTimespans(cleanedTs);

        List<ConceptImpl> cleanedConcept  = new ArrayList<>();
        List<ConceptImpl> concepts = fBean.getConcepts();
        if(concepts!=null){
            for(ConceptImpl concept:concepts){
                if(!StringUtils.contains(concept.getAbout(),"eionet")){
                    cleanedConcept.add(concept);
                }
            }
        }

        fBean.setConcepts(cleanedConcept);

        Map<String,List<String>> creators = europeanaProxy.getDcCreator();
        List<AgentImpl> agents = fBean.getAgents();
        List<AgentImpl> agentCopy = new CopyOnWriteArrayList<>();
        Collections.copy(agents,agentCopy);
        if(creators!=null){
            for(String creator:creators.get("def")){

                for(AgentImpl agent:agentCopy){
                    if (StringUtils.equals(creator,agent.getAbout())){
                        agentCopy.remove(agent);
                    }
                }
            }
        }
        fBean.setAgents(agentCopy);

        Map<String,List<String>> contributors = europeanaProxy.getDcContributor();
        List<AgentImpl> agentsNew = fBean.getAgents();
        List<AgentImpl> agentCopyNew = new CopyOnWriteArrayList<>();
        Collections.copy(agentsNew,agentCopyNew);
        if(contributors!=null){
            for(String contributor:contributors.get("def")){

                for(AgentImpl agent:agentCopyNew){
                    if (StringUtils.equals(contributor,agent.getAbout())){
                        agentCopyNew.remove(agent);
                    }
                }
            }
        }
        fBean.setAgents(agentCopyNew);

        Map<String,String> placeMap = new HashMap<>();
        List<PlaceImpl> places = fBean.getPlaces();
        if(places != null){
            for(PlaceImpl place : places) {
            	if(place == null) {
            		Logger.getGlobal().severe("Place is null");
            	} else {
            		if (place.getAbout() == null)
            			Logger.getGlobal().severe("Place getAbout is null");
            		if (place.getIsPartOf() == null)
            			Logger.getGlobal().severe("Place getIsPartOf is null");
            		else if (place.getIsPartOf().get("def") == null) {
            			Logger.getGlobal().severe("Place getIsPartOf def is null");
            		} else if (place.getIsPartOf().get("def").isEmpty()) {
            			Logger.getGlobal().severe("Place getIsPartOf def is empty");
            		}
            	}
				placeMap.put(
						place.getAbout(),
						place.getIsPartOf() != null ? place.getIsPartOf()
								.get("def") != null ? !place.getIsPartOf().get("def").isEmpty() 
										? place.getIsPartOf().get("def").get(0) : null : null : null);
            }
        }

        Map<String,List<String>> spatial = europeanaProxy.getDctermsSpatial();
        Set<String> toRemove = new HashSet<>();
        if(spatial!=null){
            for(String sp : spatial.get("def")) {
                if (placeMap.containsKey(sp)){
                    toRemove.add(sp);
                    toRemove.addAll(removeParent(sp, placeMap));
                }

            }
        }

        List<PlaceImpl> placesCopy = new CopyOnWriteArrayList<>();
        Collections.copy(places,placesCopy);
        for(String uri:toRemove){
            for(PlaceImpl place:placesCopy){
                if(StringUtils.equals(place.getAbout(),uri)){
                    placesCopy.remove(place);
                }
            }
        }

        fBean.setPlaces(placesCopy);

        Map<String,List<String>> coverage = europeanaProxy.getDcCoverage();
        Set<String> toRemoveNew = new HashSet<>();
        if(coverage!=null){
            for(String sp:coverage.get("def")) {
                if (placeMap.containsKey(sp)){

                    toRemoveNew.addAll(removeParent(sp, placeMap));
                }

            }
        }

        List<PlaceImpl> placesCopyNew = new CopyOnWriteArrayList<>();
        Collections.copy(fBean.getPlaces(),placesCopy);
        for(String uri:toRemoveNew){
            for(PlaceImpl place:placesCopyNew){
                if(StringUtils.equals(place.getAbout(),uri)){
                    placesCopyNew.remove(place);
                }
            }
        }

        fBean.setPlaces(placesCopyNew);
        return fBean;
    }

    private List<String> removeParent(String sp, Map<String, String> placeMap) {
        List<String> parents = new ArrayList<>();
        if(sp!=null) {
            parents.add(sp);
            if (placeMap.get(sp) != null && !StringUtils.equals(sp,placeMap.get(sp))) {
                Logger.getGlobal().severe(sp);
                parents.addAll(removeParent(placeMap.get(sp), placeMap));
            }
        }
        return parents;
    }

    private void appendEntities(FullBeanImpl fBean, String entityWrapper) {
        try {
            List<EntityWrapper> entities = om.readValue(entityWrapper, EntityWrapperList.class).getWrapperList();
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
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), TimespanImpl.class));
            } else if (entity.getClassName().equals(AgentImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), AgentImpl.class));
            } else if (entity.getClassName().equals(ConceptImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), ConceptImpl.class));
            } else {
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), PlaceImpl.class));
            }
            entities.add(ret);
        }
        return entities;
    }
}

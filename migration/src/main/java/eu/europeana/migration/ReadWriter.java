/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.*;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Read from a Mongo and a Solr and Write to a Mongo and Solr
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class ReadWriter implements Runnable {

    private List<SolrDocument> segment;
    private EdmMongoServer sourceMongo;
    private CountDownLatch latch;
    
    private SolrDocumentHandler solrHandlerIngst;
    private CloudSolrServer cloudServerIngst;
    private EdmMongoServer targetMongoIngst;
    private FullBeanHandler fBeanHandlerIngst;
    
    private SolrDocumentHandler solrHandlerProd;
    private CloudSolrServer cloudServerProd;
    private EdmMongoServer targetMongoProd;
    private FullBeanHandler fBeanHandlerProd;
    private EnrichmentDriver driver;
    private MigrationUtils utils;
    
    
    public List<SolrDocument> getSegment() {
    	return segment;
    }
    
    public void setSegment(List<SolrDocument> segment) {
    	this.segment = segment;
    }

    public EdmMongoServer getSourceMongo() {
    	return sourceMongo;
    }
    
    public void setSourceMongo(EdmMongoServer sourceMongo) {
    	this.sourceMongo = sourceMongo;
    }

    public void setLatch(CountDownLatch latch) {
    	this.latch = latch;
    }
        
	public SolrDocumentHandler getSolrHandlerIngst() {
		return solrHandlerIngst;
	}

	public void setSolrHandlerIngst(SolrDocumentHandler solrHandlerIngst) {
		this.solrHandlerIngst = solrHandlerIngst;
	}

	public CloudSolrServer getCloudServerIngst() {
		return cloudServerIngst;
	}

	public void setCloudServerIngst(CloudSolrServer cloudServerIngst) {
		this.cloudServerIngst = cloudServerIngst;
	}

	public EdmMongoServer getTargetMongoIngst() {
		return targetMongoIngst;
	}

	public void setTargetMongoIngst(EdmMongoServer targetMongoIngst) {
		this.targetMongoIngst = targetMongoIngst;
	}

	public FullBeanHandler getfBeanHandlerIngst() {
		return fBeanHandlerIngst;
	}
	
	public void setfBeanHandlerIngst(FullBeanHandler fBeanHandlerIngst) {
		this.fBeanHandlerIngst = fBeanHandlerIngst;
	}
	
	public SolrDocumentHandler getSolrHandlerProd() {
		return solrHandlerProd;
	}

	public void setSolrHandlerProd(SolrDocumentHandler solrHandlerProd) {
		this.solrHandlerProd = solrHandlerProd;
	}

	public CloudSolrServer getCloudServerProd() {
		return cloudServerProd;
	}

	public void setCloudServerProd(CloudSolrServer cloudServerProd) {
		this.cloudServerProd = cloudServerProd;
	}

	public EdmMongoServer getTargetMongoProd() {
		return targetMongoProd;
	}

	public void setTargetMongoProd(EdmMongoServer targetMongoProd) {
		this.targetMongoProd = targetMongoProd;
	}
	
	public FullBeanHandler getfBeanHandlerProd() {
		return fBeanHandlerProd;
	}
	
	public void setfBeanHandlerProd(FullBeanHandler fBeanHandlerProd) {
		this.fBeanHandlerProd = fBeanHandlerProd;
	}

    public void setEnrichmentDriver(EnrichmentDriver driver){
        this.driver = driver;
    }
	/**
	 * To set all Ingestion settings at once.
	 * @param solrHandler
	 * @param cloudServer
	 * @param targetMongo
	 * @param fBeanHandler
	 */
	public void setTargetsIngestion(SolrDocumentHandler solrHandler,
									CloudSolrServer cloudServer, 
									EdmMongoServer targetMongo,
									FullBeanHandler fBeanHandler) {
		setSolrHandlerIngst(solrHandler);
		setfBeanHandlerIngst(fBeanHandler);
		setTargetMongoIngst(targetMongo);
		setCloudServerIngst(cloudServer);
	}

	/**
	 * To set all Production settings at once.
	 * @param solrHandler
	 * @param cloudServer
	 * @param targetMongo
	 * @param fBeanHandler
	 */
	public void setTargetsProduction(SolrDocumentHandler solrHandler,
									CloudSolrServer cloudServer, 
									EdmMongoServer targetMongo,
									FullBeanHandler fBeanHandler) {
		setSolrHandlerProd(solrHandler);
		setfBeanHandlerProd(fBeanHandler);
		setTargetMongoProd(targetMongo);
		setCloudServerProd(cloudServer);
	}

	@Override
    public void run() {
		//write data to INGESTION
        //save(solrHandlerIngst, cloudServerIngst, targetMongoIngst, fBeanHandlerIngst);
        //write data to PRODUCTION
        try {
            save(solrHandlerIngst, cloudServerIngst, targetMongoIngst, fBeanHandlerIngst, solrHandlerProd, cloudServerProd, targetMongoProd, fBeanHandlerProd, driver);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
        //Notify the main thread that you finished and that it does not have to wait for you now

    }

	private void save(SolrDocumentHandler solrHandler,
						CloudSolrServer cloudServer,
						EdmMongoServer targetMongo,
                      FullBeanHandler fBeanHandler,
                      SolrDocumentHandler solrHandlerProd,
                      CloudSolrServer cloudServerProd,
                      EdmMongoServer targetMongoProd,
						FullBeanHandler fBeanHandlerProd,
                      EnrichmentDriver driver) {
		List<SolrInputDocument> docList = new ArrayList<>();
         //For every document
         for (SolrDocument doc : segment) {
        	 
            //Fix bug with double slashes
            String id = doc.getFieldValue("europeana_id").toString();
            if (id.startsWith("//")) {
                id = id.replace("//", "/");
            }
            try {
                //Find the bean
                FullBeanImpl fBean = (FullBeanImpl) sourceMongo.getFullBean(id);
                
                //Replace empty timestampCreated with 0 (to make it searchable)
                if (fBean.getTimestampCreated() == null) {
                    fBean.setTimestampCreated(new Date(0));
                }
                if (fBean.getTimestampUpdated() == null) {
                    fBean.setTimestampUpdated(new Date(0));
                }
                if (fBean.getAggregations().get(0).getEdmUgc() == null){
                    AggregationImpl aggr = fBean.getAggregations().get(0);
                    aggr.setEdmUgc("false");
                    fBean.getAggregations().set(0, aggr);
                }
                //Adding missing rdf:resource in the EuropeanaAggregation and aggregation
                String pchoAbout = fBean.getProvidedCHOs().get(0).getAbout();
                EuropeanaAggregation aggr = fBean.getEuropeanaAggregation();
                aggr.setAggregatedCHO(pchoAbout);
                fBean.setEuropeanaAggregation(aggr);
                AggregationImpl aggregation = fBean.getAggregations().get(0);
                aggregation.setAggregatedCHO(pchoAbout);
                fBean.getAggregations().set(0,aggregation);
                appendWebResources(fBean);
                
                //Fix for the edm:rights
                if(fBean.getAggregations().get(0).getEdmRights()!=null) {

                    Map<String, List<String>> edmRights = fBean.getAggregations().get(0).getEdmRights();
                    for (String rightsKey : edmRights.keySet()) {
                        List<String> rights = edmRights.get(rightsKey);
                        if (rights.size() > 1) {
                            List<String> filteredRights = new ArrayList<>();
                            for (String right : rights) {
                                if (StringUtils.isNotBlank(right)) {
                                    filteredRights.add(right);
                                    break;
                                }
                            }
                            edmRights.put(rightsKey, filteredRights);
                        }
                    }

                fBean.getAggregations().get(0).setEdmRights(edmRights);
                }
                //Generate Solr document from bean
                clean(fBean);
                enrich(fBean);

                SolrInputDocument inputDoc = solrHandler.generate(fBean);
                
                //Add to list for saving later
                docList.add(inputDoc);
                
                //Save the individual classes in the Mongo cluster
                fBeanHandler.saveEdmClasses(fBean, true);
                //fBeanHandlerProd.saveEdmClasses(fBean,true);
                //and then save the records themselves (this does not happen in one go, because of UIM)
                targetMongo.getDatastore().save(fBean);
                //targetMongoProd.getDatastore().save(fBean);
            } catch (Exception ex) {
                Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, "Got exception for id: " +id, ex);

            }

        }

        try {
            //add the documents in Solr..they will become available..no need to commit.. PATIENZA
            cloudServer.add(docList);
            cloudServerProd.add(docList);
        } catch (SolrServerException | IOException ex) {
            Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

    private void clean(FullBeanImpl fBean) {

        ProxyImpl provProxy = findProviderProxy(fBean);
        ProxyImpl euProxy = findEuropeanaProxy(fBean);
        List<LangValue> euProxyValues = utils.extractValuesFromProxy(euProxy);
        Set<String> uris = utils.extractAllUris(provProxy);
        for (LangValue value:euProxyValues){
            value.getValue().stream().filter(uri -> !uris.contains(uri)).forEach(uri -> {
                switch (value.getVocabulary()){
                    case "AGENT":
                        if(fBean.getAgents()!=null){
                            fBean.getAgents().stream().filter(agent -> StringUtils.equals(agent.getAbout(), uri)).forEach(agent -> {
                                List<AgentImpl> agents = fBean.getAgents();
                                agents.remove(agent);
                                fBean.setAgents(agents);
                            });
                        }
                        break;
                    case "PLACE":
                        if(fBean.getPlaces()!=null){
                            fBean.getPlaces().stream().filter(place -> StringUtils.equals(place.getAbout(), uri)).forEach(place -> {
                                List<PlaceImpl> places = fBean.getPlaces();
                                places.remove(place);
                                fBean.setPlaces(places);
                            });
                        }
                        break;
                    case "TIMESPAN":
                        if(fBean.getTimespans()!=null){
                            fBean.getTimespans().stream().filter(timespan -> StringUtils.equals(timespan.getAbout(), uri)).forEach(timespan -> {
                                List<TimespanImpl> timespans = fBean.getTimespans();
                                timespans.remove(timespan);
                                fBean.setTimespans(timespans);
                            });
                        }
                        break;
                    case "CONCEPT":
                        if(fBean.getConcepts()!=null){
                            fBean.getConcepts().stream().filter(concept -> StringUtils.equals(concept.getAbout(), uri)).forEach(concept -> {
                                List<ConceptImpl> concepts = fBean.getConcepts();
                                concepts.remove(concept);
                                fBean.setConcepts(concepts);
                            });
                        }
                        break;
                }
            });
        }
    }

    private void enrich(FullBeanImpl fBean) {
        ProxyImpl provProxy = findProviderProxy(fBean);
        List<LangValue> provProxyValues = utils.extractValuesFromProxy(provProxy);

        List<InputValue> values = new ArrayList<>();
        for(LangValue val:provProxyValues){
            for(String langVal :val.getValue()){
                InputValue value = new InputValue();
                value.setLanguage(val.getLanguage());
                value.setValue(langVal);
                List<EntityClass> vocs = new ArrayList<>();
                vocs.add(EntityClass.valueOf(val.getVocabulary()));
                value.setVocabularies(vocs);
                value.setOriginalField(val.getOriginalField());
                values.add(value);
            }
        }

        try {
            List<EntityWrapper> entities= driver.enrich(values,false);
            mergeEntitiesToBean(entities,fBean);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeEntitiesToBean(List<EntityWrapper> entities, FullBeanImpl fBean) throws IOException {
        ProxyImpl euProxy = findEuropeanaProxy(fBean);
        if(entities!=null&&entities.size()>0) {
            for(EntityWrapper entity: entities){
                ProxyImpl euProxyRet = utils.addValueToProxy(entity,euProxy);
                replaceProxy(fBean, euProxyRet);
                utils.addContextualClassToBean(fBean,entity);
            }
        }

    }

    private void replaceProxy(FullBeanImpl fBean, ProxyImpl proxy){
        List<ProxyImpl> proxies = fBean.getProxies();
        int i=0;
        for(ProxyImpl pr:proxies){
            if(StringUtils.equals(pr.getAbout(),proxy.getAbout())){
                proxies.set(i,proxy);
            }
            i++;
        }
        fBean.setProxies(proxies);
    }

    private ProxyImpl findEuropeanaProxy(FullBeanImpl fBean){
        for (ProxyImpl proxy: fBean.getProxies()){
            if(proxy.isEuropeanaProxy()){
                return proxy;
            }
        }
        return null;
    }
    private ProxyImpl findProviderProxy(FullBeanImpl fBean){
        for (ProxyImpl proxy: fBean.getProxies()){
            if(!proxy.isEuropeanaProxy()){
                return proxy;
            }
        }
        return null;
    }
    /**
     * Create web resources for edm:object, edm:isShownBy, edm:isShownAt and edm:hasView
     * @param fBean 
     */
    private void appendWebResources(FullBeanImpl fBean) {
        AggregationImpl aggregation = fBean.getAggregations().get(0);
        Set<String> resources = new HashSet<>();
        if(aggregation.getEdmIsShownBy() != null){
            resources.add(aggregation.getEdmIsShownBy());
        }
        if(aggregation.getEdmIsShownAt() != null){
            resources.add(aggregation.getEdmIsShownAt());
        }
        if(aggregation.getEdmObject() != null){
            resources.add(aggregation.getEdmObject());
        }
        String[] hasView = aggregation.getHasView();
        if(hasView!=null){
            for(String str:hasView){
                resources.add(str);
            }
        }
        List<WebResourceImpl> wrs = (List<WebResourceImpl>) aggregation.getWebResources();
        List<WebResourceImpl> toAdd = new ArrayList<>();
        for(String res:resources){
            boolean exists = false;
            for(WebResourceImpl wr: wrs){
                if(StringUtils.equals(res, wr.getAbout())){
                    exists = true;
                    break;
                }
            }
            if(!exists){
                WebResourceImpl wr = new WebResourceImpl();
                wr.setAbout(res);
                toAdd.add(wr);
            }
        }
        wrs.addAll(toAdd);
        fBean.getAggregations().get(0).setWebResources(wrs);
    }

}

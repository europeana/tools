/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordwrite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

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

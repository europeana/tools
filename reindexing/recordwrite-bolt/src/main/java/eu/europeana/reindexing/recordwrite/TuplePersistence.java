/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordwrite;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
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

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.reindexing.common.mongo.PerTaskBatchesDao;
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
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

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
    private final PerTaskBatchesDao dao;

    private static IBindingFactory context;
    private static IUnmarshallingContext uctx;



        public TuplePersistence(FullBeanHandler mongoHandlerIngst, EdmMongoServer mongoServerIngst, CloudSolrServer solrServerIngst, SolrDocumentHandler solrHandlerIngst,
    						FullBeanHandler mongoHandlerProd, EdmMongoServer mongoServerProd, CloudSolrServer solrServerProd, SolrDocumentHandler solrHandlerProd,
    						List<Tuple> tuples, CountDownLatch latch, PerTaskBatchesDao dao) {
        
        this.mongoHandlerIngst = mongoHandlerIngst;
        this.mongoServerIngst = mongoServerIngst;
        this.solrServerIngst = solrServerIngst;
        this.solrHandlerIngst = solrHandlerIngst;
        
        this.mongoHandlerProd = mongoHandlerProd;
        this.mongoServerProd = mongoServerProd;
        this.solrServerProd = solrServerProd;
        this.solrHandlerProd = solrHandlerProd;
        this.dao=dao;
        this.tuples = tuples;
        this.latch= latch;

            try

            {

                context = BindingDirectory.getFactory(RDF.class);

                 uctx = context.createUnmarshallingContext();

            } catch (JiBXException e) {
                e.printStackTrace();
            }
        }

    @Override
    public void run() {
		//write data to INGESTION&PRODUCTION
    	Logger.getGlobal().info("Saving...");
        try {
            save(solrHandlerIngst, solrServerIngst, mongoServerIngst,
                    mongoHandlerIngst, solrHandlerProd, solrServerProd,
                    mongoServerProd, mongoHandlerProd, dao);
        } catch(Exception e){
            Logger.getGlobal().severe(e.getMessage());
            e.printStackTrace();
        }
		//Notify the main thread that you finished and that it does not have to wait for you now
		finally {
            Logger.getGlobal().info("Threads remaining:" + (latch.getCount() - 1));
            latch.countDown();
        }

		Logger.getGlobal().info("Finished processing and persisting");
    }

	private void save(SolrDocumentHandler solrHandlerIngst,
			CloudSolrServer solrServerIngst,
			EdmMongoServer mongoServerIngst,
			FullBeanHandler mongoHandlerIngst,
			SolrDocumentHandler solrHandlerProd,
			CloudSolrServer solrServerProd,
			EdmMongoServer mongoServerProd,
			FullBeanHandler mongoHandlerProd, PerTaskBatchesDao dao) {
		for (Tuple tuple : tuples) {
            ReindexingTuple task = ReindexingTuple.fromTuple(tuple);
            FullBeanImpl fBean = mongoServerProd.searchByAbout(FullBeanImpl.class, task.getIdentifier());
            try {           
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.INFO, "*** Saving record " + fBean.getAbout() + " ... ***");
                //cleanFullBean(fBean);
                //appendEntities(fBean, task.getEntityWrapper());
                if(task.getEdmXml()!=null) {
                    RDF edmOBJ = (RDF) uctx.unmarshalDocument(new StringReader(task.getEdmXml()));

                    FullBeanImpl fBeanToAppend = new MongoConstructor()
                            .constructFullBean(edmOBJ);
              /*  ModifiableSolrParams params = new ModifiableSolrParams();
                params.add("q", "europeana_id:" + ClientUtils.escapeQueryChars(fBean.getAbout()));
                params.add("fl", "is_fulltext,has_thumbnails,has_media,filter_tags,facet_tags,has_landingpage");
                */
                    mongoHandlerProd.saveEdmClasses(fBeanToAppend, true);
                    mongoServerProd.getDatastore().save(fBeanToAppend);
                    SolrInputDocument solrDocumentProd = solrHandlerProd.generate(fBeanToAppend);
                    //addFields(solrDocumentProd, solrServerProd.query(params));
                    solrServerProd.add(solrDocumentProd);
                    Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.INFO, "*** Record " + fBean.getAbout() + "is saved in Production. ***");

                    FullBeanImpl fBeanIngst = mongoServerIngst.searchByAbout(FullBeanImpl.class, fBean.getAbout());
                    if (fBeanIngst != null) {
                        //cleanFullBean(fBeanIngst);
                        //appendEntities(fBeanIngst, task.getEntityWrapper());
                        mongoHandlerIngst.saveEdmClasses(fBeanToAppend, true);
                        mongoServerIngst.getDatastore().save(fBeanToAppend);
                        //SolrInputDocument solrDocumentIngst = solrHandlerIngst.generate(fBeanIngst);
                        //addFields(solrDocumentIngst, solrServerIngst.query(params));
                        solrServerIngst.add(solrDocumentProd);
                        Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.INFO, "*** Record " + fBean.getAbout() + "is saved in Ingestion. ***");
                    }
                }
               // dao.removeRecordIdFromBatch(task.getTaskId(),task.getBatchId(),task.getIdentifier());
            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SolrServerException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e){

                try {
                    throw e;
                } catch (JiBXException e1) {
                    e1.printStackTrace();
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                }
            }
        }
	}
	
	/**
	 * Add CRF fields
	 * @param doc
	 * @param resp
	 */
/*	private void addFields(SolrInputDocument doc, QueryResponse resp) {
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
	}*/
    

}

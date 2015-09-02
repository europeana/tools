/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordwrite;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.storage.MongoServer;
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.Status;
import eu.europeana.reindexing.common.TaskReport;

/**
 *
 * @author ymamakis
 */
public class RecordWriteBolt extends BaseRichBolt {
    private OutputCollector collector;

    private FullBeanHandler mongoHandlerIngst;
    private EdmMongoServer mongoServerIngst;
    private CloudSolrServer solrServerIngst;
    private SolrDocumentHandler solrHandlerIngst;
    private String zkHostIngst;
   
    private FullBeanHandler mongoHandlerProd;
    private EdmMongoServer mongoServerProd;
    private CloudSolrServer solrServerProd;
    private SolrDocumentHandler solrHandlerProd;
    private String zkHostProd;
    
    private List<Tuple> tuples;
    private Datastore datastore;

    private long i;
 
    private String[] ingstMongoAddresses;
    private String[] ingstSolrAddresses;
    private String ingstSolrCollection;
    
    String ingstDbName;
    String ingstDbUser;
    String ingstDbPassword;
    
    private String[] prodMongoAddresses;
    private String[] prodSolrAddresses;
    private String prodSolrCollection;
    
    String prodDbName;
    String prodDbUser;
    String prodDbPassword;
    
    String[] taskreportMongoAddresses;

	public RecordWriteBolt(String ingstZkHost, String[] ingstMongoAddresses, String[] ingstSolrAddresses, String ingstSolrCollection,
						   String ingstDbName, String ingstDbUser, String ingstDbPassword,
						   String prodZkHost, String[] prodMongoAddresses, String[] prodSolrAddresses, String prodSolrCollection,
						   String prodDbName, String prodDbUser, String prodDbPassword,
						   String[] taskReportMongoAddresses) {		
		this.ingstMongoAddresses = ingstMongoAddresses;
        this.ingstSolrAddresses = ingstSolrAddresses;
        this.ingstSolrCollection = ingstSolrCollection;
        this.zkHostIngst = ingstZkHost;
        this.ingstDbName = ingstDbName;
        this.ingstDbUser = ingstDbUser;
        this.ingstDbPassword = ingstDbPassword;
        
		this.prodMongoAddresses = prodMongoAddresses;
        this.prodSolrAddresses = prodSolrAddresses;
        this.prodSolrCollection = prodSolrCollection;        
        this.zkHostProd = prodZkHost;
        this.prodDbName = prodDbName;
        this.prodDbUser = prodDbUser;
        this.prodDbPassword = prodDbPassword;
        
        this.taskreportMongoAddresses = taskreportMongoAddresses;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {

    }

    @Override
    public void prepare(Map map, TopologyContext tc, OutputCollector oc) {
        try {
            this.collector = oc;
            tuples = new ArrayList<>();
            LBHttpSolrServer lbTargetIngst = new LBHttpSolrServer(ingstSolrAddresses);
            solrServerIngst = new CloudSolrServer(zkHostIngst, lbTargetIngst);
            solrServerIngst.setDefaultCollection(ingstSolrCollection);
            solrServerIngst.connect();
            
            List<ServerAddress> addressesIngst = new ArrayList<>();
            for (String mongoStr : ingstMongoAddresses) {
                ServerAddress address = new ServerAddress(mongoStr, 27017);
                addressesIngst.add(address);
            }
            
            Mongo mongoIngst = new Mongo(addressesIngst);
            mongoServerIngst = new EdmMongoServerImpl(mongoIngst, ingstDbName, ingstDbUser, ingstDbPassword);
            mongoHandlerIngst = new FullBeanHandler(mongoServerIngst);
            solrHandlerIngst = new SolrDocumentHandler(solrServerIngst);
            
            LBHttpSolrServer lbTargetProd = new LBHttpSolrServer(prodSolrAddresses);
            solrServerProd = new CloudSolrServer(zkHostProd, lbTargetProd);           
            solrServerProd.setDefaultCollection(prodSolrCollection);            
            solrServerProd.connect();
            
            List<ServerAddress> addressesProd = new ArrayList<>();
            for (String mongoStr : prodMongoAddresses) {
                ServerAddress address = new ServerAddress(mongoStr, 27017);
                addressesProd.add(address);
            }
            
            Mongo mongoProd = new Mongo(addressesProd);
            mongoServerProd = new EdmMongoServerImpl(mongoProd, prodDbName, prodDbUser, prodDbPassword);
            mongoHandlerProd = new FullBeanHandler(mongoServerProd);
            solrHandlerProd = new SolrDocumentHandler(solrServerProd);
            
            i = 0;
            
            //datastore
            Morphia morphia = new Morphia().map(TaskReport.class);
            List<ServerAddress> addressesTaskReport = new ArrayList<>();
            for (String mStr : taskreportMongoAddresses) {
                ServerAddress addr = new ServerAddress(mStr, 27017);
                addressesTaskReport.add(addr);
            }
            Mongo mongoTaskReports = new Mongo(addressesTaskReport);
            MongoServer mongoServerTaskReports = new EdmMongoServerImpl(mongoTaskReports,"taskreports", null, null);
            
            datastore = morphia.createDatastore(mongoServerTaskReports.getDatastore().getMongo(), "taskreports");
            datastore.ensureIndexes();
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoDBException ex) {
            Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        tuples.add(tuple);
        i++;
        Logger.getGlobal().log(Level.INFO, "Got " + i + " records to save.");
        if (tuple.getLongByField(ReindexingFields.NUMFOUND) == i || tuples.size() == 5000) {
            Logger.getGlobal().log(Level.INFO, "Processing " + i + " records");
            processTuples(tuples);
            
            Query<TaskReport> query = datastore.find(TaskReport.class).filter("taskId", tuple.getLongByField(ReindexingFields.TASKID));
            UpdateOperations<TaskReport> ops = datastore.createUpdateOperations(TaskReport.class);
            TaskReport report = query.get();
           
            if (i < report.getProcessed()) {
                i = report.getProcessed() + tuples.size();
            }
            ops.set("processed", i);
            ops.set("dateUpdated", new Date().getTime());

            //to reset the index "i"
            if (report.getStatus() == Status.STOPPED) {
            	i = 0;
            }
            
            //to finish a current task report and reset the index "i"
            if (i == report.getTotal()) {
            	ops.set("status", Status.FINISHED);
                i = 0;
            } else {
            	ops.set("status", Status.PROCESSING);            	
            }
            datastore.update(query, ops);
            tuples.clear();
        }
    }

    private void processTuples(List<Tuple> tuples) {
        if (tuples.size() == 5000) {
            List<List<Tuple>> batches = splitTuplesIntoBatches(tuples);
            //10 batches per each method call
            CountDownLatch latch = new CountDownLatch(10);
            for (List<Tuple> batch : batches) {
            	//500 tuples per each batch
                Thread t = new Thread(new TuplePersistence(mongoHandlerIngst, mongoServerIngst, solrServerIngst, solrHandlerIngst, 
                										   mongoHandlerProd, mongoServerProd, solrServerProd, solrHandlerProd, batch, latch));
                t.start();
            }
            try {
                latch.await();
                Logger.getLogger("/// --- Finished saving of 5000 tuples.--- ///");
            } catch (InterruptedException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Thread t = new Thread(new TuplePersistence(mongoHandlerIngst, mongoServerIngst, solrServerIngst, solrHandlerIngst, 
					   								   mongoHandlerProd, mongoServerProd, solrServerProd, solrHandlerProd, tuples, latch));
            t.start();
            try {
                latch.await();
                Logger.getLogger("/// --- Finished saving of " + tuples.size() + " tuples.--- ///");
            } catch (InterruptedException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private List<List<Tuple>> splitTuplesIntoBatches(List<Tuple> tuples) {
        List<List<Tuple>> batches = new ArrayList<>();
        int i = 0;
        int k = 500;
        while (i < tuples.size()) {
            batches.add(tuples.subList(i, i+k));
            i = i + k;
        }
        return batches;
    }

}

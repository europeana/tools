/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordwrite;

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
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.Status;
import eu.europeana.reindexing.common.TaskReport;
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

/**
 *
 * @author ymamakis
 */
public class RecordWriteBolt extends BaseRichBolt {

    private OutputCollector collector;

    private EdmMongoServer mongoServer;
    private CloudSolrServer solrServer;
    private List<Tuple> tuples ;
    private Datastore datastore;

    private long i;

    private FullBeanHandler mongoHandler;
    private SolrDocumentHandler solrHandler;

    private String zkHost;
    private String[] mongoAddresses;
    private String[] solrAddresses;
    private String solrCollection;

    public RecordWriteBolt(String zkHost, String[] mongoAddresses, String[] solrAddresses, String solrCollection) {
        this.zkHost = zkHost;
        this.mongoAddresses = mongoAddresses;
        this.solrAddresses = solrAddresses;
        this.solrCollection = solrCollection;
        
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {

    }

    @Override
    public void prepare(Map map, TopologyContext tc, OutputCollector oc) {
        try {
            this.collector = oc;
            tuples = new ArrayList<>();
            LBHttpSolrServer lbTarget = new LBHttpSolrServer(solrAddresses);
            solrServer = new CloudSolrServer(zkHost, lbTarget);
            solrServer.setDefaultCollection(solrCollection);
            solrServer.connect();
            List<ServerAddress> addresses = new ArrayList<>();
            for (String mongoStr : mongoAddresses) {
                ServerAddress address;

                address = new ServerAddress(mongoStr, 27017);
                addresses.add(address);
            }
            i = 0;
            Mongo mongo = new Mongo(addresses);
            mongoServer = new EdmMongoServerImpl(mongo, "europeana", null, null);
            mongoHandler = new FullBeanHandler(mongoServer);
            solrHandler = new SolrDocumentHandler(solrServer);
            Morphia morphia = new Morphia().map(TaskReport.class);
            datastore = morphia.createDatastore(mongoServer.getDatastore().getMongo(), "taskreports");
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
        

        if (tuple.getLongByField(ReindexingFields.NUMFOUND) == i || tuples.size() == 10000) {
           Logger.getGlobal().log(Level.INFO, "processing " + i + "records");
            processTuples(tuples);
            
            Query<TaskReport> query = datastore.find(TaskReport.class).filter("taskId", tuple.getLongByField(ReindexingFields.TASKID));
            UpdateOperations<TaskReport> ops = datastore.createUpdateOperations(TaskReport.class);
            TaskReport report = query.get();
            if(i<report.getProcessed()){
                i = report.getProcessed() + tuples.size();
            }
            ops.set("processed", i);
            ops.set("dateUpdated", new Date().getTime());
            ops.set("status", Status.PROCESSING);
            datastore.update(query, ops);
            tuples.clear();
        }

        
    }

    private void processTuples(List<Tuple> tuples) {
        if (tuples.size() == 10000) {
            List<List<Tuple>> batches = splitTuplesIntoBatches(tuples);
            CountDownLatch latch = new CountDownLatch(10);
            for (List<Tuple> batch : batches) {
                Thread t = new Thread(new TuplePersistence(mongoHandler, mongoServer, solrServer, solrHandler, batch, latch));
                t.start();
            }
            try {
                latch.await();
                Logger.getLogger("Finished saving");
            } catch (InterruptedException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Thread t = new Thread(new TuplePersistence(mongoHandler, mongoServer, solrServer, solrHandler, tuples, latch));
            t.start();
            try {
                latch.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private List<List<Tuple>> splitTuplesIntoBatches(List<Tuple> tuples) {
        List<List<Tuple>> batches = new ArrayList<>();
        int i = 0;
        int k = 1000;
        while (i < tuples.size()) {
            batches.add(tuples.subList(i, i+k));
            i = i + k;
        }
        return batches;
    }

}

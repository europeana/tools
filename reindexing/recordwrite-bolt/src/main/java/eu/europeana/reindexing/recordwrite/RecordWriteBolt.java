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
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.ReindexingTuple;
import eu.europeana.reindexing.recordwrite.reporting.TaskReport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ymamakis
 */
public class RecordWriteBolt extends BaseRichBolt {

    private OutputCollector collector;

    private EdmMongoServer mongoServer;
    private CloudSolrServer solrServer;
    private Jedis jedis;
    private Mongo mongo;

    private Datastore datastore;

    private int i = 0;

    private FullBeanHandler mongoHandler;
    private SolrDocumentHandler solrHandler;

    public RecordWriteBolt(EdmMongoServer mongoServer, CloudSolrServer solrServer, Jedis jedis, Mongo mongo){
        this.mongo = mongo;
        this.mongoServer = mongoServer;
        this.solrServer = solrServer;
        this.jedis = jedis;
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {

    }

    @Override
    public void prepare(Map map, TopologyContext tc, OutputCollector oc) {
        this.collector = oc;
        mongoHandler = new FullBeanHandler(mongoServer);
        solrHandler = new SolrDocumentHandler(null);
        Morphia morphia = new Morphia().map(TaskReport.class);
        datastore = morphia.createDatastore(mongo, "taskreports");
        datastore.ensureIndexes();
    }

    @Override
    public void execute(Tuple tuple) {

        processTuples(tuple);

        i++;

        Query<TaskReport> query = datastore.find(TaskReport.class).filter("taskId", tuple.getLongByField(ReindexingFields.TASKID));
        TaskReport ts = query.get();
        if (ts != null) {
            UpdateOperations<TaskReport> ops = datastore.createUpdateOperations(TaskReport.class);
            ops.set("processed", i);
            datastore.update(query, ops);
        } else {
            datastore.save(ts);
        }
    }

    private void processTuples(Tuple tuple) {

        ReindexingTuple task = ReindexingTuple.fromTuple(tuple);

        byte[] serialized = jedis.get(task.getIdentifier().getBytes());
        try {
            ObjectInputStream oIn = new ObjectInputStream(new ByteArrayInputStream(serialized));
            FullBeanImpl fBean = (FullBeanImpl) oIn.readObject();
            mongoHandler.saveEdmClasses(fBean, true);
            mongoServer.getDatastore().save(fBean);
            solrServer.add(solrHandler.generate(fBean));

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SolrServerException ex) {
            Logger.getLogger(RecordWriteBolt.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordread;

import java.util.Map;

import org.apache.solr.client.solrj.impl.CloudSolrServer;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.ReindexingTuple;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import redis.clients.jedis.Jedis;

/**
 * Spout reading from Mongo and Solr for further processing
 *
 * @author ymamakis
 */
public class ReadSpout extends BaseRichSpout {

    private CloudSolrServer solrServer;

    private EdmMongoServer mongoServer;

    private Jedis jedis;

    private SpoutOutputCollector collector;

    private String query;
    
    private long taskId;
    
    public ReadSpout(CloudSolrServer solrServer, EdmMongoServer mongoServer, Jedis jedis){
        super();
        this.solrServer=solrServer;
        this.jedis = jedis;
        this.mongoServer = mongoServer;
    }
    
    @Override
    public void nextTuple() {
        SolrQuery params = new SolrQuery("*:*");
        query = params.getQuery();
        params.setRows(10000);
        //Enable Cursor (needs order)
        params.setSort("europeana_id", SolrQuery.ORDER.asc);
        //Retrieve only the europeana_id filed (the record is retrieved from Mongo)
        params.setFields("europeana_id");
        //Start from the begining
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        taskId = new Date().getTime();
        //Unless the querymark file exists which means start from where you previously stopped
        if (new File("querymark").exists()) {
            try {
                cursorMark = FileUtils.readFileToString(new File("querymark"));
            } catch (IOException ex) {
                Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        boolean done = false;
        //While we are not at the end of the index
        while (!done) {
            try {
                params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                QueryResponse resp = solrServer.query(params);
                String nextCursorMark = resp.getNextCursorMark();
                //Process
                doProcessing(resp);
                //Exit if reached the end
                if (cursorMark.equals(nextCursorMark)) {
                    done = true;
                }
                cursorMark = nextCursorMark;
                //Update the querymark
                FileUtils.write(new File("querymark"), cursorMark, false);

            } catch (SolrServerException | IOException ex) {
                Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(ReindexingFields.TASKID,ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY));

    }

    private void doProcessing(QueryResponse resp) {

        SolrDocumentList docs = resp.getResults();
        long numFound = resp.getResults().getNumFound();

        try {
            for (SolrDocument doc : docs) {
                String id = doc.getFieldValue("europeana_id").toString();
                FullBean bean = mongoServer.getFullBean(id);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oOut = new ObjectOutputStream(bout);
                oOut.writeObject(bean);
                jedis.set(id.getBytes(), bout.toByteArray());
                collector.emit(new ReindexingTuple(taskId,id, numFound, query).toTuple());
            }
            Thread.sleep(10000);
        } catch (MongoDBException | IOException | InterruptedException ex) {
            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}

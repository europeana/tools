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
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.ReindexingTuple;
import eu.europeana.reindexing.common.TaskReport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

/**
 * Spout reading from Mongo and Solr for further processing
 *
 * @author ymamakis
 */
public class ReadSpout extends BaseRichSpout {

    private CloudSolrServer solrServer;


    private Datastore datastore;

    private SpoutOutputCollector collector;

    private String query;

    private long taskId;
    private String zkHost;
    private String[] mongoAddresses;
    private String[] solrAddresses;
    private String solrCollection;

    public ReadSpout(String zkHost, String[] mongoAddresses, String[] solrAddresses, String solrCollection) {
        this.zkHost = zkHost;
        this.mongoAddresses = mongoAddresses;
        this.solrAddresses = solrAddresses;
        this.solrCollection = solrCollection;
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
            TaskReport tr = datastore.find(TaskReport.class).filter("taskId", taskId).get();
            if(tr==null || tr.getProcessed()%10000 ==0 ){
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

    }

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        try {
            this.collector = collector;
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
            Mongo mongo = new Mongo(addresses);
            Morphia morphia = new Morphia();
            morphia.map(TaskReport.class);
            datastore = morphia.createDatastore(mongo, "taskreport");
            datastore.ensureIndexes();
        } catch (MalformedURLException | UnknownHostException ex) {
            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(ReindexingFields.TASKID, ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY, ReindexingFields.ENTITYWRAPPER));

    }

    private void doProcessing(QueryResponse resp) {

        SolrDocumentList docs = resp.getResults();
        long numFound = resp.getResults().getNumFound();
        if (datastore.find(TaskReport.class).filter("taskId", taskId).get() == null) {
            TaskReport report = new TaskReport();
            report.setDateCreated(taskId);
            report.setQuery(query);
            report.setTopology("enrichment");
            report.setTotal(numFound);
            report.setDateUpdated(taskId);
            report.setProcessed(0);
            datastore.save(report);
        }
        try {
            for (SolrDocument doc : docs) {
                String id = doc.getFieldValue("europeana_id").toString();
                collector.emit(new ReindexingTuple(taskId, id, numFound, query, null).toTuple(), id);
                
            }
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}

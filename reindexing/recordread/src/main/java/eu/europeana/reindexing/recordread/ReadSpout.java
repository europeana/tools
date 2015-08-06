/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordread;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.ReindexingTuple;
import eu.europeana.reindexing.common.Status;
import eu.europeana.reindexing.common.TaskReport;

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
    private long processed = 0;

    public ReadSpout(String zkHost, String[] mongoAddresses, String[] solrAddresses, String solrCollection) {
        this.zkHost = zkHost;
        this.mongoAddresses = mongoAddresses;
        this.solrAddresses = solrAddresses;
        this.solrCollection = solrCollection;
    }

    @Override
    public void nextTuple() {
        // Check that there is a list of task reports with status INITIAL
        // If there is no task  - sleep 5 minutes!
    	List<TaskReport> initialTaskReports = datastore.find(TaskReport.class).field("status").in(Arrays.asList(Status.INITIAL, Status.PROCESSING)).asList();
		Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE,"Got " + (initialTaskReports!=null?initialTaskReports.size():0)+" tasks");
    	if (initialTaskReports == null || initialTaskReports.isEmpty()) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ex) {
				Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
			}
    	} else {		
    		for (TaskReport initialTaskReport : initialTaskReports) {    			
    			taskId = initialTaskReport.getTaskId();
				processed = initialTaskReport.getProcessed();
    			// Start from the beginning or from the last cursor mark of a task report (queryMark)
    			String cursorMark = initialTaskReport.getStatus() == Status.INITIAL? CursorMarkParams.CURSOR_MARK_START: initialTaskReport.getQueryMark();
    			
    			// Create another query "q" to update the task report "status", "dateUpdated"
				Query<TaskReport> q = datastore.find(TaskReport.class).filter("taskId", taskId);
    			UpdateOperations<TaskReport> ops = datastore.createUpdateOperations(TaskReport.class);
    			ops.set("dateUpdated", new Date().getTime());    			
    			ops.set("status", Status.PROCESSING);

    			initialTaskReport.setStatus(Status.PROCESSING);			
    			Logger.getGlobal().info("Processing task report: " + taskId);
    			
    			query = initialTaskReport.getQuery();
    			SolrQuery params = new SolrQuery(query);
    			params.setRows(5000);
    			// Enable Cursor (needs order)
    			params.setSort("europeana_id", SolrQuery.ORDER.asc);
    			// Retrieve only the europeana_id filed (the record is retrieved from Mongo)
    			params.setFields("europeana_id");
    			// Unless the query mark file exists which means start from where you previously stopped    			
    			boolean done = false;
    			// While we are not at the end of the index
    			while (!done) {
    				Logger.getGlobal().info("Processed for taskId " + taskId + " = " + processed);
    				try {
						TaskReport report = datastore.find(TaskReport.class).filter("taskId", taskId).get();
						while(processed>report.getProcessed()){
							report = datastore.find(TaskReport.class).filter("taskId", taskId).get();
							try {
								Thread.sleep(30000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
    					params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    					QueryResponse resp = solrServer.query(params);
    					if ( report.getStatus() == Status.STOPPED) {
    						break;
    					}
    					String nextCursorMark = resp.getNextCursorMark();


    					// For query "q" we update "total"
    					ops.set("total", resp.getResults().getNumFound());
    					
    					//if the number of results is 0 then we finish the task report
    					if (resp.getResults().getNumFound() == 0) {
    						initialTaskReport.setStatus(Status.FINISHED);
    						break;
    					}

						//datastore.update(q, ops);
    	    			// Process
    					doProcessing(resp);
    					processed += resp.getResults().size();
						Logger.getGlobal().info("Processed "+ processed +" for taskId " + taskId);
    					// Exit if reached the end
    					if (cursorMark.equals(nextCursorMark)) {
    						done = true;
    						Logger.getGlobal().info("Done is now true for taskId " + taskId);
    					}
    					cursorMark = nextCursorMark;
						// Update the query mark
						ops.set("queryMark", nextCursorMark);
						// Update current task report at the data store
						datastore.update(q, ops);

					} catch (SolrServerException ex) {
    					Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
    				}
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
            taskId = new Date().getTime();
            Mongo mongo = new Mongo(addresses);
            Morphia morphia = new Morphia();
            morphia.map(TaskReport.class);
            datastore = morphia.createDatastore(mongo, "taskreports");
            datastore.ensureIndexes();
        } catch (MalformedURLException | UnknownHostException ex) {
            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(ReindexingFields.TASKID, ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY, ReindexingFields.ENTITYWRAPPER));
    }

    /**
     * 
     * @param resp
     */
    private void doProcessing(QueryResponse resp) {
        SolrDocumentList docs = resp.getResults();
        long numFound = resp.getResults().getNumFound();
        try {
            for (SolrDocument doc : docs) {
                String id = doc.getFieldValue("europeana_id").toString();
                collector.emit(new ReindexingTuple(taskId, id, numFound, query, null).toTuple(), id);
                Thread.sleep(10);
            }
            Thread.sleep(30000);
        } catch (Exception ex) {
            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

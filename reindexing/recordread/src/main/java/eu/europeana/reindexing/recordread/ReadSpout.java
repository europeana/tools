/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordread;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.europeana.reindexing.common.*;
import eu.europeana.reindexing.common.mongo.PerTaskBatchesDao;
import org.apache.commons.lang.StringUtils;
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
    private PerTaskBatchesDao dao;

    public ReadSpout(String zkHost, String[] mongoAddresses, String[] solrAddresses, String solrCollection) {
        this.zkHost = zkHost;
        this.mongoAddresses = mongoAddresses;
        this.solrAddresses = solrAddresses;
        this.solrCollection = solrCollection;
    }

    /**
     * The logic for each unfinished task report is the following:
     * <p/>
     * - read an unfinished task report from DB, if there is no one then wait;
     * - set the status of initial task report to PROCESSING if was INITIAL;
     * - get the cursor mark from a task report;
     * - get the response for task report query and cursor mark;
     * - process (emit) the response for further re-indexing until it gets the number of "processed" equal to number of "total".
     * <p/>
     * NOTE: 	We DO update only the "total" (one first time when we start the initial task).
     * We DO update the "queryMark" after processing of the response.
     * We DO NOT change "processed" and "status" fields (except for an empty result set) for a task report during the processing.
     * The fields "processed" and "status" should be updated in RecordWriteBolt.
     */

    @Override
    public void nextTuple() {
        List<TaskReport> initialTaskReports = datastore.find(TaskReport.class).field("status").in(Arrays.asList(Status.INITIAL, Status.PROCESSING)).asList();
        Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, "Got " + (initialTaskReports != null ? initialTaskReports.size() : 0) + " tasks");
        if (initialTaskReports == null || initialTaskReports.isEmpty()) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            for (TaskReport initialTaskReport : initialTaskReports) {
                taskId = initialTaskReport.getTaskId();
                String cursorMark = CursorMarkParams.CURSOR_MARK_START;
                long numFound = initialTaskReport.getTotal();
                if (initialTaskReport.getStatus().equals(Status.INITIAL)) {
                    dao.deleteByTaskId(taskId);
                    numFound = 0;
                    Query<TaskReport> q = datastore.find(TaskReport.class).filter("taskId", taskId);
                    UpdateOperations<TaskReport> ops = datastore.createUpdateOperations(TaskReport.class);
                    ops.set("dateUpdated", new Date().getTime());
                    ops.set("status", Status.PROCESSING);
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
                    int i = 0;


                    while (!done) {
                        params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

                        try {
                            QueryResponse resp = solrServer.query(params);
                            if (numFound == 0) {
                                numFound = resp.getResults().getNumFound();
                            }
                            createBatches(resp, taskId, i);
                            if (StringUtils.equals(cursorMark, resp.getNextCursorMark())) {
                                done = true;
                            }
                            cursorMark = resp.getNextCursorMark();

                            i++;
                        } catch (SolrServerException ex) {
                            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    ops.set("total", numFound);
                    datastore.update(q, ops);
                }

                List<PerTaskBatch> batches = dao.findByTaskId(taskId);
                long size = 0;
                for (PerTaskBatch batch : batches) {
                    int i = 0;
                    for (String recordId : batch.getRecordIds()) {
                        collector.emit(new ReindexingTuple(batch.getTaskId(), batch.getBatchId(), recordId, numFound, query, null).toTuple(), recordId);
                        i++;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    boolean done = false;
                    size += i;
                    while (!done) {
                        try {
                            if (size == datastore.find(TaskReport.class).filter("taskId", taskId).get().getProcessed()) {
                                done = true;
                            } else {

                                Thread.sleep(30000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                }
            }
        }

    private void createBatches(QueryResponse resp, long taskId, long batchId) {
        List<String> recordIds = new ArrayList<>();
        for (SolrDocument doc : resp.getResults()) {
            recordIds.add(doc.getFirstValue("europeana_id").toString());

        }
        dao.createPerTaskBatch(taskId, batchId, recordIds);
    }

//    @Override
//    public void nextTuple() {
//    	List<TaskReport> initialTaskReports = datastore.find(TaskReport.class).field("status").in(Arrays.asList(Status.INITIAL, Status.PROCESSING)).asList();
//		Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE,"Got " + (initialTaskReports != null ? initialTaskReports.size() : 0) + " tasks");
//    	if (initialTaskReports == null || initialTaskReports.isEmpty()) {
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException ex) {
//				Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
//			}
//    	} else {
//    		for (TaskReport initialTaskReport : initialTaskReports) {
//    			taskId = initialTaskReport.getTaskId();
//				processed = initialTaskReport.getProcessed();
//    			// Start from the beginning or from the last cursor mark of a task report (queryMark)
//    			String cursorMark = initialTaskReport.getStatus() == Status.INITIAL ? CursorMarkParams.CURSOR_MARK_START : initialTaskReport.getQueryMark();
//
//    			// Create another query "q" to update the task report "status", "dateUpdated"
//				Query<TaskReport> q = datastore.find(TaskReport.class).filter("taskId", taskId);
//    			UpdateOperations<TaskReport> ops = datastore.createUpdateOperations(TaskReport.class);
//    			ops.set("dateUpdated", new Date().getTime());
//    			ops.set("status", Status.PROCESSING);
//
//    			initialTaskReport.setStatus(Status.PROCESSING);
//    			Logger.getGlobal().info("Processing task report: " + taskId);
//
//    			query = initialTaskReport.getQuery();
//    			SolrQuery params = new SolrQuery(query);
//    			params.setRows(5000);
//    			// Enable Cursor (needs order)
//    			params.setSort("europeana_id", SolrQuery.ORDER.asc);
//    			// Retrieve only the europeana_id filed (the record is retrieved from Mongo)
//    			params.setFields("europeana_id");
//    			// Unless the query mark file exists which means start from where you previously stopped
//    			boolean done = false;
//    			// While we are not at the end of the index
//    			while (!done) {
//    				try {
//						TaskReport report = datastore.find(TaskReport.class).filter("taskId", taskId).get();
//    					params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
//    					QueryResponse resp = solrServer.query(params);
//    					long numFound = resp.getResults().getNumFound();
//    					String nextCursorMarkFromSolr = resp.getNextCursorMark();
//
//    					// If the task report is stopped or finished
//    					if (report.getStatus() == Status.STOPPED || report.getStatus() == Status.FINISHED) {
//    						done = true;
//    						break;
//    					}
//
//    					// If the number of results is 0 then we finish the task report
//    					if (numFound == 0) {
//							done = true;
//							ops.set("status", Status.FINISHED);
//							datastore.update(q, ops);
//							break;
//    					}
//
//    					// For query "q" we update "total" if it has not been set already
//    					// NOTE: works only one time for each task report
//						if (report.getTotal() == 0) {
//							ops.set("total", numFound);
//						}
//
//    					// Process
//    					doProcessing(resp);
//
//    					// Exit if reached the end
//    					if (report.getProcessed() == numFound) {
//    						done = true;
//    						break;
//    					}
//
//    					// Update task report
//    					ops.set("queryMark", nextCursorMarkFromSolr);
//    					datastore.update(q, ops);
//
//    					processed += resp.getResults().size();
//						Logger.getGlobal().info("Processed "+ processed + " records for taskId=" + taskId);
//						while (processed > report.getProcessed()){
//							report = datastore.find(TaskReport.class).filter("taskId", taskId).get();
//							try {
//								Thread.sleep(30000);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//						}
//					} catch (SolrServerException ex) {
//    					Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
//    				}
//    			}
//    		}
//    	}
//    }

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

            datastore = morphia.createDatastore(mongo, "task_report_test");
            datastore.ensureIndexes();
            dao = new PerTaskBatchesDao(addresses, "pertaskbatch");

        } catch (MalformedURLException | UnknownHostException ex) {
            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(ReindexingFields.TASKID, ReindexingFields.BATCHID, ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY, ReindexingFields.ENTITYWRAPPER));
    }

    /**
     *
     * @param resp
     */
//    private void doProcessing(QueryResponse resp) {
//        SolrDocumentList docs = resp.getResults();
//        long numFound = resp.getResults().getNumFound();
//        try {
//            for (SolrDocument doc : docs) {
//                String id = doc.getFieldValue("europeana_id").toString();
//                collector.emit(new ReindexingTuple(taskId, id, numFound, query, null).toTuple(), id);
//                Thread.sleep(10);
//            }
//            Thread.sleep(30000);
//        } catch (Exception ex) {
//            Logger.getLogger(ReadSpout.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migration application prototype for a proposed Europeana infrastructure
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class Migration {

  private static final String propertiesPath = "/migration.properties";
  private static DBConnectionHandler dbConnectionHandler = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(Migration.class);

  static {
    try {
      dbConnectionHandler = DBConnectionHandler.getInstance(true, propertiesPath);
    } catch (IOException | MongoDBException e) {
      e.printStackTrace();
    }
  }

  public static void main(String... args) {
    try {
      //Query to the source
      String query = "*:*";
      //String query = "europeana_id:"+ ClientUtils.escapeQueryChars("/00101/65D6FA0D3552F0E42AAFDC884A2F32DBDBD78897");
      String fl = "europeana_id";
      // String[] crfFields = new String[]{fl,"has_thumbnails","has_media","filter_tags","facet_tags","has_landingpage"};
      SolrQuery params = new SolrQuery();
      params.setQuery(query);
      params.setRows(10000);
      //Enable Cursor (needs order)
      params.setSort(fl, SolrQuery.ORDER.asc);
      //Retrieve only the europeana_id filed (the record is retrieved from Mongo)
      params.setFields(fl);
      //Start from the begining
      String cursorMark = CursorMarkParams.CURSOR_MARK_START;
      //Unless the querymark file exists which means start from where you previously stopped
      if (new File("querymark").exists()) {
        cursorMark = FileUtils.readFileToString(new File("querymark"));
      }
      boolean done = false;
      int i = 0;
      //While we are not at the end of the index
      while (!done) {
        long time = System.currentTimeMillis();
        params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
        QueryResponse resp = dbConnectionHandler.getSourceSolr().query(params);
        String nextCursorMark = resp.getNextCursorMark();

        //Process
        LOGGER.info("*** Migrating the batch #" + (i / 10000 + 1) + " started. ***");
        doCustomProcessingOfResults(resp);

        //Exit if reached the end
        if (cursorMark.equals(nextCursorMark)) {
          done = true;
        }
        cursorMark = nextCursorMark;
        //Update the querymark
        FileUtils.write(new File("querymark"), cursorMark, false);
        i += 10000;
        time = System.currentTimeMillis() - time;
        //Logging
        LOGGER.info("*** Time spent for migrating the batch: " + time + " milliseconds which is around "
            + (int) ((time / 1000) / 60) + " minutes "
            + (int) ((time / 1000) % 60) + " seconds. ***");
        LOGGER.info("*** Added " + i + " documents. ***");

        if (i >= 200000) {
          done = true;
        }
      }

    } catch (UnknownHostException | SolrServerException
        | MalformedURLException ex) {
      LOGGER.error(null, ex);
    } catch (IOException ex) {
      LOGGER.error(null, ex);
    }

  }

  /**
   * Process a batch of data and store it to target databases
   *
   * @param resp //  * @param target
   */
  private static void doCustomProcessingOfResults(QueryResponse resp) {
    //If the list of results is full
    if (resp.getResults().size() == 10000) {
      List<List<SolrDocument>> batches = createBatches(resp.getResults());
      //Prepare the creation of 50 threads
      CountDownLatch latch = new CountDownLatch(50);

      for (List<SolrDocument> batch : batches) {
        final ReadWriter writer = new ReadWriter();
        writer.setBatches(batch);
        writer.setSourceMongo(dbConnectionHandler.getSourceMongo());
        writer.setLatch(latch);
                /*
        writer.setTargetsIngestion(
						ingestion.getSolrHandler(),
						ingestion.getTargetSolr(), 
						ingestion.getTargetMongo(),
						ingestion.getMongoHandler());*/
        writer.setConnectionTargets(
            dbConnectionHandler.getSolrHandler(),
            dbConnectionHandler.getTargetCloudSolr(),
            dbConnectionHandler.getTargetMongo(),
            dbConnectionHandler.getMongoHandler());

        writer.setEnrichmentDriver(dbConnectionHandler.getEnrichmentDriver());
        Thread t = new Thread(writer);
        t.start();
      }
      try {
        //Block until all threads are done
        latch.await();
      } catch (InterruptedException ex) {
        LOGGER.error(null, ex);
      }
      //On any other case do it single threadedly (end of the index)
    } else {
      CountDownLatch latch = new CountDownLatch(1);

      final ReadWriter writer = new ReadWriter();
      writer.setBatches(resp.getResults());
      writer.setSourceMongo(dbConnectionHandler.getSourceMongo());
      writer.setLatch(latch);
            
           /* writer.setTargetsIngestion(
          ingestion.getSolrHandler(),
					ingestion.getTargetSolr(), 
					ingestion.getTargetMongo(),
					ingestion.getMongoHandler());
					*/
      writer.setConnectionTargets(
          dbConnectionHandler.getSolrHandler(),
          dbConnectionHandler.getTargetCloudSolr(),
          dbConnectionHandler.getTargetMongo(),
          dbConnectionHandler.getMongoHandler());
      writer.setEnrichmentDriver(dbConnectionHandler.getEnrichmentDriver());
      Thread t = new Thread(writer);
      t.start();
      try {
        //Block in order to avoid closing the VM
        latch.await();
      } catch (InterruptedException ex) {
        LOGGER.error(null, ex);
      }
    }

  }

  /**
   * Segment the results
   */
  private static List<List<SolrDocument>> createBatches(SolrDocumentList results) {
    List<List<SolrDocument>> segments = new ArrayList<>();
    int i = 0;
    int k = 200;
    int iter = 50;
    while (i < iter * k) {
      List<SolrDocument> segment = results.subList(i, i + k);
      segments.add(segment);
      i += k;
    }
    return segments;
  }
}

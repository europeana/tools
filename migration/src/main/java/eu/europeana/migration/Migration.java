/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migration application prototype for a proposed Europeana infrastructure
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class Migration {

  private static final Logger LOGGER = LoggerFactory.getLogger(Migration.class);
  private static final String propertiesPath = "/migration.properties";
  private static final String queryMarkFileName = "querymark";
  private static final String idsFileName = "/test_ids.txt";
  private static final String returnField = "europeana_id";
  private static final int maxIdsPerBatch = 10000;
  private static InputStream fileInputStream;
  private static final File queryMarkFile = new File(queryMarkFileName);
  private static DBConnectionHandler dbConnectionHandler = null;

  static {
    try {
      dbConnectionHandler = DBConnectionHandler.getInstance(true, propertiesPath);
    } catch (IOException | MongoDBException e) {
      e.printStackTrace();
    }
  }

  public static void main(String... args) throws IOException, SolrServerException {

    if (dbConnectionHandler.isIdsFileReindex()) {
      fileInputStream = Migration.class.getResourceAsStream(idsFileName);
    }

    if (fileInputStream != null) {
      LOGGER.info(LogMarker.currentStateMarker, "*** ID'S FILE FOUND: " + idsFileName + " ***");
      idsReindex();
    }
    else if (dbConnectionHandler.isIdsFileReindex())
    {
      LOGGER
          .info(LogMarker.currentStateMarker, "*** ID'S FILE NOT FOUND: starting full reindex ***");
      fullReindex();
    }
    else {
      LOGGER
          .info(LogMarker.currentStateMarker, "*** Starting full reindex ***");
      fullReindex();
    }
    dbConnectionHandler.getTargetCloudSolr().commit();
    dbConnectionHandler.close();
  }

  private static void fullReindex() {
    try {
      //Query to the source
      String query = "*:*";
      //String query = "europeana_id:"+ ClientUtils.escapeQueryChars("/00101/65D6FA0D3552F0E42AAFDC884A2F32DBDBD78897");
      String fl = returnField;
      // String[] crfFields = new String[]{fl,"has_thumbnails","has_media","filter_tags","facet_tags","has_landingpage"};
      SolrQuery params = new SolrQuery();
      params.setQuery(query);
      params.setRows(maxIdsPerBatch);
      //Enable Cursor (needs order)
      params.setSort(fl, SolrQuery.ORDER.asc);
      //Retrieve only the europeana_id filed (the record is retrieved from Mongo)
      params.setFields(fl);
      //Start from the begining
      String cursorMark = CursorMarkParams.CURSOR_MARK_START;
      //Unless the querymark file exists which means start from where you previously stopped
      if (queryMarkFile.exists()) {
        cursorMark = FileUtils.readFileToString(queryMarkFile);
      }
      boolean done = false;
      int counterProcessed = 0;
      int counterBatch = 1;
      //While we are not at the end of the index
      while (!done) {
        long time = System.currentTimeMillis();
        params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
        QueryResponse resp = dbConnectionHandler.getSourceSolr().query(params);
        String nextCursorMark = resp.getNextCursorMark();

        //Process
        LOGGER
            .info(LogMarker.currentStateMarker,
                "*** MIGRATING THE BATCH #" + counterBatch + " STARTED. ***");
        List<List<String>> segments = createSegments(
            getFieldValuesListFromSolrDocument(resp.getResults(), returnField));
        doCustomProcessingOfResults(segments, counterBatch);

        //Exit if reached the end
        if (cursorMark.equals(nextCursorMark)) {
          done = true;
        }
        cursorMark = nextCursorMark;
        //Update the querymark
        FileUtils.write(queryMarkFile, cursorMark, false);
        counterProcessed += maxIdsPerBatch;
        counterBatch++;
        time = System.currentTimeMillis() - time;
        //Logging
        LOGGER.info(LogMarker.currentStateMarker,
            "*** TIME SPEND FOR MIGRATING THE BATCH: " + time + " MILLISECONDS WHICH IS AROUND "
                + (int) ((time / 1000) / 60) + " MINUTES "
                + (int) ((time / 1000) % 60) + " SECONDS. ***");
        LOGGER.info(LogMarker.currentStateMarker,
            "*** ADDED " + counterProcessed + " DOCUMENTS. ***");
      }
      queryMarkFile.delete();
    } catch (SolrServerException | IOException ex) {
      LOGGER.error(null, ex);
    }
  }

  private static void idsReindex() {
    List<String> ids = null;
    try {
      ids = readFileLinesToList(fileInputStream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    List<List<String>> segments = createSegments(ids);
    long time = System.currentTimeMillis();
    LOGGER.info(LogMarker.currentStateMarker,
        "*** MIGRATING THE MANUAL IDS #" + ids.size() + " STARTED. ***");
    doCustomProcessingOfResults(segments, 1);

    //Logging
    time = System.currentTimeMillis() - time;
    LOGGER.info(LogMarker.currentStateMarker,
        "*** TIME SPEND FOR MIGRATING THE BATCH: " + time + " MILLISECONDS WHICH IS AROUND "
            + (int) ((time / 1000) / 60) + " MINUTES "
            + (int) ((time / 1000) % 60) + " SECONDS. ***");
    LOGGER.info(LogMarker.currentStateMarker, "*** PROCESSED " + ids.size() + " DOCUMENTS. ***");

  }

  /**
   * Process a batch of data and store it to target databases
   */
  private static void doCustomProcessingOfResults(List<List<String>> segments, int counterBatch) {
    //Prepare the creation of 50 threads
    CountDownLatch latch = new CountDownLatch(segments.size());

    int counterSegment = 0;
    for (List<String> segment : segments) {
      counterSegment++;
      final ReadWriter writer = new ReadWriter(segment, counterBatch, counterSegment);
//        writer.setIdsBatch(batch);
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
  }

  /**
   * Segment the results
   */
  private static List<List<String>> createSegments(List<String> results) {
    List<List<String>> segments = new ArrayList<>();

    int offset = 0;
    int maxSizePerList = 200;
    int numberOfLists = results.size() / maxSizePerList;
    int remainingListSize = results.size() % maxSizePerList;
    while (offset < numberOfLists * maxSizePerList) {
      List<String> segment = results.subList(offset, offset + maxSizePerList);
      segments.add(segment);
      offset += maxSizePerList;
    }
    if (remainingListSize != 0) {
      List<String> segment = results.subList(offset, offset + remainingListSize);
      segments.add(segment);
    }
    return segments;
  }

  private static List<String> readFileLinesToList(InputStream fileInputStreamToRead)
      throws FileNotFoundException {
    Scanner s = new Scanner(fileInputStreamToRead);
    List<String> list = new ArrayList<>();
    while (s.hasNext()) {
      list.add(s.next());
    }
    s.close();
    return list;
  }

  private static List<String> getFieldValuesListFromSolrDocument(List<SolrDocument> solrDocuments,
      String fieldName) {
    List<String> europeanaIds = new ArrayList<>(solrDocuments.size());
    for (int i = 0; i < solrDocuments.size(); i++) {
      SolrDocument solrDocument = solrDocuments.get(i);
      europeanaIds.add(solrDocument.getFieldValue(fieldName).toString());
    }
    return europeanaIds;
  }

}

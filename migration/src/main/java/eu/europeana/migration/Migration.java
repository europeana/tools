/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;

/**
 * Migration application prototype for a proposed Europeana infrastructure
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class Migration {

    private static HttpSolrServer sourceSolr;
    private static EdmMongoServer sourceMongo;
    
    private static Target ingestion;
    private static Target production;
    
    private static Properties properties;

    public static void main(String... args) {

        properties = new Properties();

        Mongo mongo;
        try {
            properties.load(Migration.class.getResourceAsStream("/migration.properties"));
            String srcMongoUrl = properties.getProperty("source.mongo");
            String srcSolrUrl = properties.getProperty("source.solr");
            
            //Connect to Solr and Mongo (source)
            mongo = new Mongo(srcMongoUrl, 27017);
            sourceSolr = new HttpSolrServer(srcSolrUrl);
            sourceMongo = new EdmMongoServerImpl(mongo, "europeana", null, null);
            
            ingestion = Target.INGESTION;
            production = Target.PRODUCTION;
            
            //Query to the source
            String query = "europeana_collectionName:2022333_*";
            String fl = "europeana_id";
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
                QueryResponse resp = sourceSolr.query(params);
                String nextCursorMark = resp.getNextCursorMark();
                
                //Process
				Logger.getLogger(Migration.class.getName()).log(
						Level.INFO, "*** Migrating the batch #" + (i / 10000 + 1)
																+ " started. ***");
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
				Logger.getLogger(Migration.class.getName()).log(
						Level.INFO,
						"*** Time spent for migrating the batch: " + time
								+ " milliseconds which is around "
								+ (int) ((time / 1000) / 60) + " minutes "
								+ (int) ((time / 1000) % 60) + " seconds. ***");
                Logger.getLogger(Migration.class.getName()).log(Level.INFO, "*** Added " 
																+ i + " documents. ***");
            }

		} catch (UnknownHostException | MongoDBException | SolrServerException
				| MalformedURLException ex) {
			Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (IOException ex) {
			Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null,
					ex);
		}

    }

    /**
     * Process the batch of data to target
     * @param resp
     * @param target
     */
    private static void doCustomProcessingOfResults(QueryResponse resp) {
        //If the list of results is full 
        if (resp.getResults().size() == 10000) {
            List<List<SolrDocument>> segments = segment(resp.getResults());
            //Prepare the creation of 50 threads
            CountDownLatch latch = new CountDownLatch(50);

            for (List<SolrDocument> segment : segments) {
                final ReadWriter writer = new ReadWriter();
                writer.setSegment(segment);
                writer.setSourceMongo(sourceMongo);
                writer.setLatch(latch);
                
				writer.setTargetsIngestion(
						ingestion.getSolrHandler(),
						ingestion.getTargetSolr(), 
						ingestion.getTargetMongo(),
						ingestion.getMongoHandler());
				writer.setTargetsProduction(
						production.getSolrHandler(),
						production.getTargetSolr(),
						production.getTargetMongo(),
						production.getMongoHandler());

                Thread t = new Thread(writer);                
                t.start();
            }
            try {
                //Block until all threads are done
                latch.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
            }
            //On any other case do it single threadedly (end of the index)
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            
            final ReadWriter writer = new ReadWriter();
            writer.setSegment(resp.getResults());
            writer.setSourceMongo(sourceMongo);
            writer.setLatch(latch);
            
            writer.setTargetsIngestion(
					ingestion.getSolrHandler(),
					ingestion.getTargetSolr(), 
					ingestion.getTargetMongo(),
					ingestion.getMongoHandler());
			writer.setTargetsProduction(
					production.getSolrHandler(),
					production.getTargetSolr(),
					production.getTargetMongo(),
					production.getMongoHandler());

            Thread t = new Thread(writer);
            t.start();
            try {
                //Block in order to avoid closing the VM
                latch.await();
            } catch (InterruptedException ex) {
               Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /** Segment the results
     * 
     * @param results
     * @return
     */
    private static List<List<SolrDocument>> segment(SolrDocumentList results) {
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

    
    /**
     * 
     * @author Alena Fedasenka
     *
     */
    private static enum Target {
    	
    	INGESTION, PRODUCTION;
    	
    	private String[] targetMongoUrl;
		private String[] targetSolrUrl;    	
    	private String targetCollection;

    	private CloudSolrServer targetSolr;
        private EdmMongoServer targetMongo;
        private FullBeanHandler mongoHandler;
        private SolrDocumentHandler solrHandler;
    	
    	private Target() {
    		String target = this.name().toLowerCase();
			this.targetMongoUrl = properties.getProperty("target." + target + ".mongo").split(",");
			this.targetSolrUrl = properties.getProperty("target." + target + ".solr").split(",");
			this.targetCollection = properties.getProperty("target." + target + ".collection");
            
            String[] targetZookeeper = properties.getProperty("zookeeper."+target+".host").split(",");
            
			//Connect to target Solr and Mongo
            try {
                LBHttpSolrServer lbTarget = new LBHttpSolrServer(targetSolrUrl);
                this.targetSolr = new CloudSolrServer(targetZookeeper[0], lbTarget);
                this.targetSolr.setDefaultCollection(targetCollection);
                this.targetSolr.connect();
        		List<ServerAddress> addresses = new ArrayList<>();
        		for (String mongoStr : targetMongoUrl) {
        		    ServerAddress address = new ServerAddress(mongoStr, 27017);
        		    addresses.add(address);
        		}
        		Mongo tgtMongo = new Mongo(addresses);
        		this.targetMongo = new EdmMongoServerImpl(tgtMongo, "europeana", null, null);
            } catch (UnknownHostException | MongoDBException | MalformedURLException  ex) {
                Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Initialize Solr Document and Mongo Bean handlers
            this.mongoHandler = new FullBeanHandler(targetMongo);
            this.solrHandler = new SolrDocumentHandler(sourceSolr);

		}
    	
        public CloudSolrServer getTargetSolr() {
			return this.targetSolr;
		}

		public EdmMongoServer getTargetMongo() {
			return this.targetMongo;
		}

		public FullBeanHandler getMongoHandler() {
			return this.mongoHandler;
		}

		public SolrDocumentHandler getSolrHandler() {
			return this.solrHandler;
		}

    }
    
}

package eu.europeana.migration;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-03-23
 */
public class DBConnectionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectionHandler.class);
  private static DBConnectionHandler DBConnectionHandler;
  private boolean isIdsFileReindex;
  private HttpSolrServer sourceSolr;
  private EdmMongoServer sourceMongo;
  private CloudSolrServer targetCloudSolr;
  private EdmMongoServer targetMongo;
  private FullBeanHandler mongoHandler;
  private SolrDocumentHandler solrHandler;
  private EnrichmentDriver enrichmentDriver;

  private DBConnectionHandler(boolean isProduction, String propertiesPath)
      throws IOException, MongoDBException {
    String target = "ingestion";
    if(isProduction)
      target = "production";
    Properties properties = new Properties();
    properties.load(DBConnectionHandler.class.getResourceAsStream(propertiesPath));
    isIdsFileReindex = Boolean.parseBoolean(properties.getProperty("ids.file.reindex"));

    //Get all source properties
    String[] sourceMongoUrl = properties.getProperty("source.mongo").split(",");;
    String sourceMongoDB = properties.getProperty("source.mongo.db");
    int sourceMongoPort = Integer.parseInt(properties.getProperty("source.mongo.port"));
    String sourceSolrUrl = properties.getProperty("source.solr");
    String enrichmentUrl = properties.getProperty("enrichment.url");

    //Get all target properties
    String[] targetMongoUrl = properties.getProperty("target." + target + ".mongo").split(",");
    String targetMongoDb = properties.getProperty("target." + target + ".mongo.db");
    int targetMongoPort = Integer
        .parseInt(properties.getProperty("target." + target + ".mongo.port"));
    String[] targetSolrUrls = properties.getProperty("target." + target + ".solr").split(",");
    String targetCollection = properties.getProperty("target." + target + ".collection");
    String[] targetZookeeper = properties.getProperty("zookeeper." + target + ".host").split(",");

    //Connect to  Solr and Mongo
    List<ServerAddress> addresses = new ArrayList<>();
    for (String mongoStr : sourceMongoUrl) {
      ServerAddress address = new ServerAddress(mongoStr, sourceMongoPort);
      addresses.add(address);
    }
    Mongo mongo = new Mongo(addresses);
    sourceSolr = new HttpSolrServer(sourceSolrUrl);
    LOGGER.info("Connected to source Solr {}", sourceSolr.getBaseURL());
    sourceMongo = new EdmMongoServerImpl(mongo, sourceMongoDB, null, null);
    LOGGER.info("Connected to source Mongo {}", mongo.getAllAddress());
    enrichmentDriver = new EnrichmentDriver(enrichmentUrl);
    try {
      LBHttpSolrServer lbTarget = new LBHttpSolrServer(targetSolrUrls);
      this.targetCloudSolr = new CloudSolrServer(targetZookeeper[0], lbTarget);
      this.targetCloudSolr.setDefaultCollection(targetCollection);
      this.targetCloudSolr.connect();
      LOGGER.info("Connected to target Solr {}", targetCloudSolr.getZkStateReader().getClusterState().getLiveNodes());
      addresses = new ArrayList<>();
      for (String mongoStr : targetMongoUrl) {
        ServerAddress address = new ServerAddress(mongoStr, targetMongoPort);
        addresses.add(address);
      }
      Mongo targetMongo = new Mongo(addresses);
      this.targetMongo = new EdmMongoServerImpl(targetMongo, targetMongoDb, null, null);
      LOGGER.info("Connected to target Mongo {}", targetMongo.getAllAddress());
    } catch (UnknownHostException | MongoDBException | MalformedURLException ex) {
      LOGGER.error(null, ex);
    }

    //Initialize Solr Document and Mongo Bean handlers
    this.mongoHandler = new FullBeanHandler(targetMongo);
    this.solrHandler = new SolrDocumentHandler(sourceSolr);
    LOGGER.info("MongoHandler is set as {}", targetMongo);
    LOGGER.info("SolrHandler is set as {}", sourceSolr);
  }

  public static DBConnectionHandler getInstance(boolean isProduction, String propertiesPath)
      throws IOException, MongoDBException {
    if (DBConnectionHandler == null){
      DBConnectionHandler = new DBConnectionHandler(isProduction, propertiesPath);
    }
    return DBConnectionHandler;
  }

  public boolean isIdsFileReindex() {
    return isIdsFileReindex;
  }

  public HttpSolrServer getSourceSolr() {
    return sourceSolr;
  }

  public EdmMongoServer getSourceMongo() {
    return sourceMongo;
  }

  public CloudSolrServer getTargetCloudSolr() {
    return targetCloudSolr;
  }

  public EdmMongoServer getTargetMongo() {
    return targetMongo;
  }

  public FullBeanHandler getMongoHandler() {
    return mongoHandler;
  }

  public SolrDocumentHandler getSolrHandler() {
    return solrHandler;
  }

  public EnrichmentDriver getEnrichmentDriver() {
    return enrichmentDriver;
  }

  public void close(){
    sourceMongo.close();
    targetMongo.close();
    targetCloudSolr.shutdown();
    sourceSolr.shutdown();
  }
}

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-03-23
 */
public class DBConnectionHandler {
  private static DBConnectionHandler DBConnectionHandler;
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
    properties.load(Migration.class.getResourceAsStream(propertiesPath));
    //Get all source properties
    String sourceMongoUrl = properties.getProperty("source.mongo");
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
    Mongo mongo = new Mongo(sourceMongoUrl, sourceMongoPort);
    sourceSolr = new HttpSolrServer(sourceSolrUrl);
    sourceMongo = new EdmMongoServerImpl(mongo, sourceMongoDB, null, null);
    enrichmentDriver = new EnrichmentDriver(enrichmentUrl);
    try {
      LBHttpSolrServer lbTarget = new LBHttpSolrServer(targetSolrUrls);
      this.targetCloudSolr = new CloudSolrServer(targetZookeeper[0], lbTarget);
      this.targetCloudSolr.setDefaultCollection(targetCollection);
      this.targetCloudSolr.connect();
      List<ServerAddress> addresses = new ArrayList<>();
      for (String mongoStr : targetMongoUrl) {
        ServerAddress address = new ServerAddress(mongoStr, targetMongoPort);
        addresses.add(address);
      }
      Mongo tgtMongo = new Mongo(addresses);
      this.targetMongo = new EdmMongoServerImpl(tgtMongo, targetMongoDb, null, null);
    } catch (UnknownHostException | MongoDBException | MalformedURLException ex) {
      Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
    }

    //Initialize Solr Document and Mongo Bean handlers
    this.mongoHandler = new FullBeanHandler(targetMongo);
    this.solrHandler = new SolrDocumentHandler(targetCloudSolr);
  }

  public static DBConnectionHandler getInstance(boolean isProduction, String propertiesPath)
      throws IOException, MongoDBException {
    if (DBConnectionHandler == null){
      DBConnectionHandler = new DBConnectionHandler(isProduction, propertiesPath);
    }
    return DBConnectionHandler;
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
}

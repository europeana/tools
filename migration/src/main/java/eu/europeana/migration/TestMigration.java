/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author gmamakis
 */
public class TestMigration {
    private static EdmMongoServer sourceMongo;
    private static EdmMongoServer targetMongo;
    private static CloudSolrServer targetSolr;
    private static Properties properties;
     
    public static void main (String[] args){
        
         properties = new Properties();

        Mongo mongo;
        try {
            properties.load(Migration.class.getResourceAsStream("/migration.properties"));
            String srcMongoUrl = properties.getProperty("source.mongo");
            String srcSolrUrl = properties.getProperty("source.solr");
            String[] targetSolrUrl = properties.getProperty("target.solr").split(",");
            String zookeeperHost = properties.getProperty("zookeeper.host");
            String targetCollection = properties.getProperty("target.collection");
            String[] targetMongoUrl = properties.getProperty("target.mongo").split(",");
            //Connect to Solr and Mongo (source)
            mongo = new Mongo(srcMongoUrl, 27017);
            sourceMongo = new EdmMongoServerImpl(mongo, "europeana", null, null);
            //Connect to target Solr and Mongo
            LBHttpSolrServer lbTarget = new LBHttpSolrServer(targetSolrUrl);
            targetSolr = new CloudSolrServer(zookeeperHost, lbTarget);
            targetSolr.setDefaultCollection(targetCollection);
            targetSolr.connect();
            List<ServerAddress> addresses = new ArrayList<>();
            for (String mongoStr : targetMongoUrl) {
                ServerAddress address = new ServerAddress(mongoStr, 27017);
                addresses.add(address);
            }
            Mongo tgtMongo = new Mongo(addresses);
            targetMongo = new EdmMongoServerImpl(tgtMongo, "europeana", null, null);
            
            List<Count> collections = fetchMigratedSolr();
            for(Count collection:collections){
                testData(collection);
            }
        } catch (IOException ex) {
            Logger.getLogger(TestMigration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoDBException ex) {
            Logger.getLogger(TestMigration.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    private static List<Count> fetchMigratedSolr() {
        SolrQuery params = new SolrQuery();
        params.setQuery("*:*");
        params.addFacetField("europeana_collectionName");
        params.add("facet.limit", "1000");
        params.setRows(0);
        try {
            QueryResponse resp = targetSolr.query(params);
            return resp.getFacetField("europeana_collectionName").getValues();
        } catch (SolrServerException ex) {
            Logger.getLogger(TestMigration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static void testData(Count collection) {
        SolrQuery query = new SolrQuery("europeana_collectionName:"+collection.getName());
        query.setFields("europeana_id");
        query.setRows(1000);
        try {
            QueryResponse resp = targetSolr.query(query);
            SolrDocumentList lst = resp.getResults();
            for(SolrDocument doc:lst){
                checkBeanInMongo(doc.getFieldValue("europeana_id").toString());
            }
        } catch (SolrServerException ex) {
            Logger.getLogger(TestMigration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void checkBeanInMongo(String id) {
        try {
            FullBean sourceBean = sourceMongo.getFullBean(id);
            FullBean targetBean = targetMongo.getFullBean(id);
            boolean equals = true;
            
            
        } catch (MongoDBException ex) {
            Logger.getLogger(TestMigration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

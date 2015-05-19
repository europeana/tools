/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.enrichment.topology;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import eu.europeana.reindexing.enrichment.EnrichmentBolt;
import eu.europeana.reindexing.recordread.ReadSpout;
import eu.europeana.reindexing.recordwrite.RecordWriteBolt;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymamakis
 */
public class EnrichmentTopology {

    private static Properties properties;
    private static String path;
    private static String zkHost;
    private static String[] mongoAddresses;
    private static String[] solrAddresses;
    private static String solrCollection; 

    public StormTopology buildTopology() {

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("readSpout", new ReadSpout(zkHost,mongoAddresses,solrAddresses, solrCollection), 1);
        builder.setBolt("enrichment", new EnrichmentBolt(path, mongoAddresses), 20).setNumTasks(20).shuffleGrouping("readSpout");
        builder.setBolt("saverecords", new RecordWriteBolt(zkHost,mongoAddresses,solrAddresses, solrCollection), 1).shuffleGrouping("enrichment");
        return builder.createTopology();
    }

    public static void main(String[] args) {
        try {
            properties = new Properties();
            properties.load(EnrichmentTopology.class.getResourceAsStream("/topology.properties"));

            solrAddresses = properties.getProperty("solr.host").split(",");
            zkHost = properties.getProperty("zookeeper.host");
            solrCollection = properties.getProperty("solr.collection");
            mongoAddresses = properties.getProperty("mongo.host").split(",");
            path = properties.getProperty("enrichment.restendpoint");
            

            Config config = new Config();
            config.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, 2000);
            config.setNumWorkers(16);
            config.setNumAckers(4);
            StormTopology topology = new EnrichmentTopology().buildTopology();

            StormSubmitter.submitTopology("enrichment", config, topology);
        } catch (AlreadyAliveException | InvalidTopologyException ex) {
            Logger.getLogger(EnrichmentTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(EnrichmentTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(EnrichmentTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnrichmentTopology.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

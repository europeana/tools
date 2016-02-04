/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.enrichment.topology;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import eu.europeana.reindexing.enrichment.EnrichmentBolt;
import eu.europeana.reindexing.recordread.ReadSpout;
import eu.europeana.reindexing.recordwrite.RecordWriteBolt;

/**
 *
 * @author ymamakis
 */
public class EnrichmentTopology {
    private static Properties properties;
    
    private static Target ingestion;
    private static Target production;
    private static Target taskreport;
	
//	private static String zookeeperHost;
    
	private static StormTopology topology;

    public static void main(String[] args) {
        try {
        	properties = new Properties();
        	properties.load(EnrichmentTopology.class.getResourceAsStream("/topology.properties"));
        	
        	ingestion = Target.INGESTION;
        	production = Target.PRODUCTION;
        	taskreport = Target.TASKREPORT;
        	
        	Config config = new Config();
        	config.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, 2000);
        	config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 5000);
        	config.setNumWorkers(16);
        	
        	topology = buildTopology();
        	
            StormSubmitter.submitTopology(properties.getProperty("topology.name"), config, topology);
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
    
	
    public static StormTopology buildTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("readSpout", new ReadSpout(taskreport.getZookeeperHost(), taskreport.getMongoAddresses(),
				taskreport.getSolrAddresses(), taskreport.getSolrCollection(),
				properties.getProperty("taskreport.mongo.database"),properties.getProperty("taskreport.mongo.batches.database")), 1);
        
        builder.setBolt("enrichment", new EnrichmentBolt(production.getPath(), production.getMongoAddresses(), production.getDatabaseName(), 
        							production.getDatabaseUser(), production.getDatabasePassword()), 10).setNumTasks(10).shuffleGrouping("readSpout");
		
        builder.setBolt("saverecords", new RecordWriteBolt(ingestion.getZookeeperHost(), ingestion.getMongoAddresses(), ingestion.getSolrAddresses(), ingestion.getSolrCollection(), 
									ingestion.getDatabaseName(), ingestion.getDatabaseUser(), ingestion.getDatabasePassword(),
									production.getZookeeperHost(), production.getMongoAddresses(), production.getSolrAddresses(), production.getSolrCollection(),
									production.getDatabaseName(), production.getDatabaseUser(), production.getDatabasePassword(),
									taskreport.getMongoAddresses(),properties.getProperty("taskreport.mongo.database"),properties.getProperty("taskreport.mongo.batches.database")), 1).shuffleGrouping("enrichment");
		return builder.createTopology();
    }
    
    /**
     * 
     * @author Alena Fedasenka
     *
     */
    private static enum Target {
    	
    	INGESTION, PRODUCTION, TASKREPORT;
    	
		private String path;
		
    	private String[] mongoAddresses;
		private String[] solrAddresses;    	
    	private String solrCollection;
    	private String zookeeperHost;
    	private String databaseName;
    	private String databaseUser;
    	private String databasePassword;

		private Target() {    		
    		String target = this.name().toLowerCase();
			this.mongoAddresses = properties.getProperty(target + ".mongo.host").split(",");
			this.solrAddresses = properties.getProperty(target + ".solr.host").split(",");
			this.solrCollection = properties.getProperty(target + ".solr.collection");
			this.path = properties.getProperty(target + ".enrichment.restendpoint"); 
			this.zookeeperHost = properties.getProperty(target + ".zookeeper.host");
			this.databaseName = properties.getProperty(target + ".mongo.database");
			this.databaseUser = properties.getProperty(target + ".mongo.user");
			this.databasePassword = properties.getProperty(target + ".mongo.password");
		}

    	public String getPath() {
    		return this.path;
    	}
    	
    	public String[] getMongoAddresses() {
    		return this.mongoAddresses;
    	}
    	
    	public String[] getSolrAddresses() {
    		return this.solrAddresses;
    	}
    	
    	public String getSolrCollection() {
    		return this.solrCollection;
    	}
    	
    	public String getZookeeperHost() {
    		return this.zookeeperHost;
    	}
    	
    	public String getDatabaseName() {
			return databaseName;
		}

		public String getDatabaseUser() {
			return databaseUser;
		}

		public String getDatabasePassword() {
			return databasePassword;
		}
    }
}

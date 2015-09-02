package eu.europeana.reindexing.enrichment.topology;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import backtype.storm.Config;
import backtype.storm.ILocalCluster;
import backtype.storm.Testing;
import backtype.storm.generated.StormTopology;
import backtype.storm.testing.CompleteTopologyParam;
import backtype.storm.testing.MkClusterParam;
import backtype.storm.testing.MockedSources;
import backtype.storm.testing.TestJob;
import backtype.storm.topology.TopologyBuilder;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import eu.europeana.reindexing.common.ReindexingTuple;
import eu.europeana.reindexing.common.TaskReport;
import eu.europeana.reindexing.enrichment.EnrichmentBolt;
import eu.europeana.reindexing.enrichment.EnrichmentFieldsCreator;
import eu.europeana.reindexing.recordread.ReadSpout;

public class EnrichmentTopologyTest {

    private Datastore datastore;
    
    private Properties properties;

//	private String query;
//
//	private long taskId;

	private SolrDocumentList results;

	private String srcCollection;
	private String[] srcSolrAddresses;
	private String[] srcMongoAddresses;
	private String srcZookeeper;
	String srcPath;
	
	private String srcDbName;
	private String srcDbUser;
	private String srcDbPassword;

	private Mongo mongo;
    
    @Before
    public void setup() {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(new File("src/test/resources/topology.properties")));
            
            srcCollection = properties.getProperty("ingestion.solr.collection");
            srcSolrAddresses = properties.getProperty("ingestion.solr.host").split(",");
            srcMongoAddresses = properties.getProperty("ingestion.mongo.host").split(",");
            srcZookeeper = properties.getProperty("ingestion.zookeeper.host");
            srcPath = properties.getProperty("ingestion.enrichment.restendpoint");
            
            srcDbName = properties.getProperty("ingestion.mongo.database");
        	srcDbUser = properties.getProperty("ingestion.mongo.user");
        	srcDbPassword =properties.getProperty("ingestion.mongo.password");

        } catch (IOException ex) {
        	Logger.getLogger(EnrichmentTopologyTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	@Test
	public void topologyTest() {
		Config daemonConfig = new Config();
		daemonConfig.put(Config.TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS, true);

		MkClusterParam mkClusterParam = new MkClusterParam();
		mkClusterParam.setDaemonConf(daemonConfig);
		Testing.withSimulatedTimeLocalCluster(mkClusterParam, new TestJob() {
            @SuppressWarnings("rawtypes")
			@Override
            public void run(ILocalCluster cluster) throws IOException {            	
				//build topology
                TopologyBuilder builder = new TopologyBuilder();
                builder.setSpout("readSpout", new ReadSpout(srcZookeeper, srcMongoAddresses, srcSolrAddresses, srcCollection));
                builder.setBolt("enrichment", new EnrichmentBolt(srcPath, srcMongoAddresses, srcDbName, srcDbUser, srcDbPassword)).shuffleGrouping("readSpout");
                StormTopology topology = builder.createTopology();
                
                
                //topology config
                Config config = new Config();
                config.setNumWorkers(1);
                config.setMessageTimeoutSecs(10);

                //prepare the mock data                 
                MockedSources mockedSources = new MockedSources();
                List<ReindexingTuple> preparedTuples = prepareTuples();
				for(ReindexingTuple tuple: preparedTuples) {
                    mockedSources.addMockData("readSpout", tuple.toTuple());
                    Logger.getLogger(EnrichmentTopologyTest.class.getName()).log(Level.INFO, "Tuple mocked: "+ tuple.getTaskId());
                }
				
                CompleteTopologyParam completeTopology = new CompleteTopologyParam();
                completeTopology.setMockedSources(mockedSources);
                completeTopology.setStormConf(config);

                try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {}
                Map result = Testing.completeTopology(cluster, topology, completeTopology);
                //FIXME remove the line below
                System.out.println("Finished! " + result.size());
                      
//                assertEquals(result.get(key), );    
            }
        });
	}
	
	/**
	 * 
	 * @return
	 */
	private List<ReindexingTuple> prepareTuples() {
		List<ReindexingTuple> tuples = new ArrayList<ReindexingTuple>();
		try {
			//Connect to source Solr and Mongo
            List<ServerAddress> addresses = new ArrayList<>();
            for (String mongoStr : srcMongoAddresses) {
            	addresses.add(new ServerAddress(mongoStr, 27017));
            }
            mongo = new MongoClient(addresses);
            Morphia morphia = new Morphia();
            morphia.map(TaskReport.class);
            datastore = morphia.createDatastore(mongo, "taskreports");
            datastore.ensureIndexes();
            
            String query = "*:*";
            String fl = "europeana_id";
            SolrQuery params = new SolrQuery();
            params.setQuery(query);
            params.setRows(10);
            params.setSort(fl, SolrQuery.ORDER.asc);
            params.setFields(fl);
            
			LBHttpSolrServer lbTarget = new LBHttpSolrServer(srcSolrAddresses);
			CloudSolrServer solrServer = new CloudSolrServer(srcZookeeper, lbTarget);
	        solrServer.setDefaultCollection(srcCollection);
	        solrServer.connect();
            QueryResponse resp = solrServer.query(params);
            
			results = resp.getResults();
			long numFound = results.getNumFound();
			//task id is mocked
			for (SolrDocument solrDoc : results) {
				tuples.add(new ReindexingTuple(System.currentTimeMillis(), solrDoc.getFieldValue("europeana_id").toString(), numFound, query, null));								
			}
        } catch (SolrServerException | MalformedURLException | UnknownHostException ex) {
            Logger.getLogger(EnrichmentTopologyTest.class.getName()).log(Level.SEVERE, null, ex);
        }
		return tuples;
	}
	
	private List<FullBeanImpl> getBeans() {
		List<FullBeanImpl> beans = new ArrayList<FullBeanImpl>();
        try {
        	EdmMongoServer server = new EdmMongoServerImpl(mongo, "europeana", "", "");
        	for (SolrDocument doc : results) {
        		FullBeanImpl fullBean = (FullBeanImpl)server.getFullBean(doc.getFieldValue("europeana_id").toString());
        		beans.add(fullBean);        		
        	}
		} catch (MongoDBException e) {
			Logger.getLogger(EnrichmentTopologyTest.class.getName()).log(Level.SEVERE, null, e);
		}
        return beans;
	}
	
	private List<String> getEnrichments(List<FullBeanImpl> beans) {
		List<String> enrichments = new ArrayList<String>();
		for (FullBeanImpl bean : beans) {
			List<InputValue> values = getEnrichmentFields(bean);
			List<EntityWrapper> entities = new ArrayList<>();
			try {
	           if (values.size() > 0) {
					entities = new EnrichmentDriver(srcPath).enrich(values, false);
	           }
	           EntityWrapperList lst = new EntityWrapperList();
	           lst.setWrapperList(entities);
	           // appendEntities(fBean, entities);
	           enrichments.add(new ObjectMapper().writeValueAsString(lst));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return enrichments;
	}
	
    private List<InputValue> getEnrichmentFields(FullBeanImpl fBean) {
        ProxyImpl providerProxy = null;
        for (ProxyImpl proxy : fBean.getProxies()) {
            if (!proxy.isEuropeanaProxy()) {
                providerProxy = proxy;
                break;
            }
        }
        return EnrichmentFieldsCreator.extractEnrichmentFieldsFromProxy(providerProxy);
    } 
}

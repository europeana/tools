package eu.europeana.record.management.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.DB;

import eu.europeana.record.management.database.entity.MongoSystemObj;
import eu.europeana.record.management.database.entity.SolrSystemObj;
import eu.europeana.record.management.database.enums.ProfileType;
import eu.europeana.record.management.server.components.MongoService;
import eu.europeana.record.management.server.components.SolrService;

public class ManuallyDeleteCollectionsTest {

	private SolrService solrService = new SolrService();

	private MongoService mongoService = new MongoService();

	@Test
	@Ignore
	public void test() throws IOException {
		SolrSystemObj solrSystemLive = new SolrSystemObj();
		solrSystemLive.setUrls(
				"sol1.eanadev.org:9191/solr,sol2.eanadev.org:9191/solr,sol3.eanadev.org:9191/solr,sol4.eanadev.org:9191/solr,sol5.eanadev.org:9191/solr,sol6.eanadev.org:9191/solr");
		solrSystemLive.setProfileType(ProfileType.LIVE_PORTAL);
		solrSystemLive.setSolrCore("search_1");
		solrSystemLive.setZookeeperURL("sol1.eanadev.org:2181,sol2.eanadev.org:2181");

		SolrSystemObj solrSystemAcceptance = new SolrSystemObj();
		solrSystemAcceptance.setUrls(
				"sol1.ingest.eanadev.org:9191/solr,sol2.ingest.eanadev.org:9191/solr,sol3.ingest.eanadev.org:9191/solr");
		solrSystemAcceptance.setProfileType(ProfileType.ACCEPTANCE_PORTAL);
		solrSystemAcceptance.setSolrCore("search_1");
		solrSystemAcceptance.setZookeeperURL("sol1.ingest.eanadev.org:2181,sol2.ingest.eanadev.org:2181");

		MongoSystemObj mongoSystemLive = new MongoSystemObj();
		mongoSystemLive.setUrls("148.251.181.144,148.251.181.236 ,148.251.183.102");
		mongoSystemLive.setProfileType(ProfileType.LIVE_PORTAL);
		mongoSystemLive.setMongoDBName("europeana");
		mongoSystemLive.setUserName("nvvncmnmewnfjkdnv");
		mongoSystemLive.setPassword("dsjaklHFNDnfd3912sndc");

		MongoSystemObj mongoSystemAcceptance = new MongoSystemObj();
		mongoSystemAcceptance.setUrls("136.243.55.72,148.251.188.17,148.251.190.206");
		mongoSystemAcceptance.setProfileType(ProfileType.ACCEPTANCE_PORTAL);
		mongoSystemAcceptance.setMongoDBName("europeana");
		mongoSystemAcceptance.setUserName("mdfsmklmkldsfdsmkl");
		mongoSystemAcceptance.setPassword("nfjds32kn$%&Njk124sdfnks");

		CloudSolrServer cloudSolrServerLive = solrService.createSolrServerInstance(solrSystemLive);
		CloudSolrServer cloudSolrServerAcceptance = solrService.createSolrServerInstance(solrSystemAcceptance);

		DB mongoDbLive = mongoService.createMongoServerInstance(mongoSystemLive);
		DB mongoDbAcceptance = mongoService.createMongoServerInstance(mongoSystemAcceptance);

		URL textWithIds = this.getClass().getResource("collections.txt");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(textWithIds.openStream()));

		PrintWriter writer = new PrintWriter("result.txt", "UTF-8");

		try {
			String line = in.readLine();
			int counter = 0;
			while (line != null) {
				counter++;
				writer.println("");
				writer.println(String.format("Collection :%s    (%d)", line, counter));
				System.out.println(String.format("Removing collection :%s    (%d) ...", line, counter));
				try {
					solrService.deleteCollection(cloudSolrServerLive, line);
				} catch (Exception e) {
					writer.println(String.format("Collection %s wasn't remove by solr live", line));
				}

				try {
					solrService.deleteCollection(cloudSolrServerAcceptance, line);
				} catch (Exception e) {
					writer.println(String.format("Collection %s wasn't remove by solr acceptance", line));
				}

				try {
					mongoService.deleteCollection(mongoDbLive, line);
				} catch (Exception e) {
					writer.println(String.format("Collection %s  wasn't remove by mongo live", line));
				}

				try {
					mongoService.deleteCollection(mongoDbAcceptance, line);
				} catch (Exception e) {
					writer.println(String.format("Collection %s  wasn't remove by mongo acceptance", line));
				}
				line = in.readLine();
			}
			cloudSolrServerLive.shutdown();

		} finally {
			in.close();
			writer.close();
		}

	}

}

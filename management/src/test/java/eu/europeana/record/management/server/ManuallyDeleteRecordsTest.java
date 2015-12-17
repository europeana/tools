package eu.europeana.record.management.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.DB;

import eu.europeana.record.management.database.entity.MongoSystemObj;
import eu.europeana.record.management.database.entity.SolrSystemObj;
import eu.europeana.record.management.database.enums.ProfileType;
import eu.europeana.record.management.server.components.MongoService;
import eu.europeana.record.management.server.components.SolrService;
import eu.europeana.record.management.shared.dto.Record;

public class ManuallyDeleteRecordsTest {

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

		URL textWithIds = this.getClass().getResource("record_deletion_20151211_new.txt");
		// URL textWithIds = this.getClass().getResource("test.txt");

		BufferedReader in = new BufferedReader(new InputStreamReader(textWithIds.openStream()));

		PrintWriter writer = new PrintWriter("result.txt", "UTF-8");

		try {
			String line = in.readLine();
			int counter = 0;
			int commitCounter = 0;
			while (line != null) {

				counter++;
				if (counter >= 2694) {
					commitCounter++;
					writer.println("");
					writer.println(String.format("record :%s    (%d)", line, counter));
					System.out.println(String.format("Removing record: %s...", line));
					Record record = new Record();
					record.setValue(line);
					try {
						solrService.deleteRecord(cloudSolrServerLive, record);
					} catch (Exception e) {
						writer.println(String.format("Record %s wasn't remove by solr live", record.getValue()));
					}

					try {
						solrService.deleteRecord(cloudSolrServerAcceptance, record);
					} catch (Exception e) {
						writer.println(String.format("Record %s wasn't remove by solr acceptance", record.getValue()));
					}

					try {
						mongoService.deleteRecord(mongoDbLive, record);
					} catch (Exception e) {
						writer.println(String.format("Record %s wasn't remove by mongo live", record.getValue()));
					}

					try {
						mongoService.deleteRecord(mongoDbAcceptance, record);
					} catch (Exception e) {
						writer.println(String.format("Record %s wasn't remove by mongo acceptance", record.getValue()));
					}
					if (commitCounter == 100) {
						commitCounter = 0;
						try {
							cloudSolrServerLive.commit();
						} catch (SolrServerException e) {
							writer.write("Commit failed at record " + counter);
							e.printStackTrace();
						}
					}
				}
				line = in.readLine();
			}
			try {
				cloudSolrServerLive.commit();
			} catch (SolrServerException e) {
				writer.write("Commit failed");
				e.printStackTrace();
			}
			cloudSolrServerLive.shutdown();

		} finally {
			in.close();
			writer.close();
		}

	}

}

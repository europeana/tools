package eu.europeana.datamigration.ese2edm;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.tools.lookuptable.impl.EuropeanaIdMongoServerImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;

public class Europeana19141918RedirectFix {

	private final static String EUROPEANA_ID = "europeana_id";
	private final static String EUROPEANA_OBJECT = "provider_aggregation_edm_object";

	private static HttpSolrServer solrOld;
	private static HttpSolrServer solrNew;
	private static EuropeanaIdMongoServer idServer;
	private final static String QUERY = "europeana_collectionName:2020601_*";

	public static void main(String... args) {
		try {
			idServer = new EuropeanaIdMongoServerImpl(new Mongo("localhost",
					27017), "EuropeanaId", "", "");
			solrOld = new HttpSolrServer("http://localhost:9999/solr/search");
			solrNew = new HttpSolrServer("http://localhost:8989/solr/search");

			int i = args[0] != null ? Integer.parseInt(args[0]) : 0;
			while (i < 350000) {
				ModifiableSolrParams solrParams = new ModifiableSolrParams();
				solrParams.set("q", QUERY);
				solrParams.set("start", i);
				solrParams.set("rows", 1000);
				QueryResponse qr = solrOld.query(solrParams);
				for (SolrDocument solrDoc : qr.getResults()) {
					ModifiableSolrParams solrNewQuery = new ModifiableSolrParams();
					EuropeanaId id = new EuropeanaId();

					solrNewQuery.set(
							"q",
							EUROPEANA_OBJECT
									+ ":"
									+ ClientUtils.escapeQueryChars(solrDoc
											.getFieldValues(EUROPEANA_OBJECT)
											.iterator().next().toString()));

					QueryResponse qrNew = solrNew.query(solrNewQuery);
					if (qrNew.getResults().size() == 1) {

						id.setOldId(solrDoc.getFieldValue(EUROPEANA_ID)
								.toString());
						id.setNewId(qrNew.getResults().get(0)
								.getFieldValue(EUROPEANA_ID).toString());
						idServer.saveEuropeanaId(id);
					}
				}
				i += 1000;
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

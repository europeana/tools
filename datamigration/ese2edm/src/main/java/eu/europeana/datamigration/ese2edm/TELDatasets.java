package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.definitions.solr.entity.Proxy;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.tools.utils.PreSipCreatorUtils;
import eu.europeana.corelib.tools.utils.SipCreatorUtils;

public class TELDatasets {

	public static void main(String[] args) {
		try {
			List<String> datasets = FileUtils.readLines(new File(
					"/home/gmamakis/tel_datasets"));
			SipCreatorUtils sp = new SipCreatorUtils();
			sp.setRepository("/export/repository/");
			List<Field> fields = new ArrayList<Field>();
			HttpSolrServer server = new HttpSolrServer(
					"http://localhost:8989/solr/search");
			for (String dataset : datasets) {
				Field field = new Field();
				field.setCollectionName(dataset.split("_")[0]);
				String orField = sp.getHashField(dataset.split("_")[0],
						dataset.split("_")[0]);
				if (orField == null) {
					orField = new PreSipCreatorUtils().getHashField(
							dataset.split("_")[0], dataset.split("_")[0]);
				}
				field.setField(orField);
				if (orField != null) {
					ModifiableSolrParams params = new ModifiableSolrParams();
					params.add("q",
							"europeana_collectionName:" + dataset.split("_")[0]
									+ "_*");
					try {
						SolrDocumentList lst = server.query(params)
								.getResults();
						if (lst.size() > 0) {
							SolrDocument doc = lst.get(0);
							if (orField.startsWith("dc_identifier")) {
								FullBean fBean = new EdmMongoServerImpl(
										new Mongo("127.0.0.1", 27017),
										"europeana", "", "").getFullBean(doc
										.getFirstValue("europeana_id")
										.toString());
								for(Proxy proxy:fBean.getProxies()){
									if(!proxy.isEuropeanaProxy()){
										Map<String,List<String>> identifiers = proxy.getDcIdentifier();
										List<String> ids = identifiers.get("def");
										field.setExample(ids.get(0));
									}
								}
							} else if (orField
									.startsWith("europeana_isShownBy")) {
								field.setExample(doc.get(
										"provider_aggregation_edm_isShownBy")
										.toString());
							} else if (orField
									.startsWith("europeana_isShownAt")) {
								field.setExample(doc.get(
										"provider_aggregation_edm_isShownAt")
										.toString());
							}
						}
					} catch (SolrServerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MongoDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MongoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				fields.add(field);
			}
			for (Field field : fields) {
				FileUtils.write(new File("/home/gmamakis/teldatasets_new"),
						field.toString(), true);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

class Field {
	String field;
	String collectionName;
	String example;

	public Field() {

	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	@Override
	public String toString() {
		return this.getCollectionName() + ";" + this.getExample() + ";"
				+ this.getField() + "\n";
	}
}

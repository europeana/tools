package eu.europeana.record.management.server.components;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import eu.europeana.record.management.database.entity.SolrSystemObj;
import eu.europeana.record.management.shared.dto.Record;
import eu.europeana.record.management.shared.exceptions.UniqueRecordException;

public class SolrService implements ServerService<SolrSystemObj> {

	@Override
	public void deleteRecord(SolrSystemObj systemObj, Record record) throws SolrServerException, IOException {
		CloudSolrServer cloudSolrServer = createSolrServerInstance(systemObj);
		cloudSolrServer.deleteByQuery("europeana_id:" + ClientUtils.escapeQueryChars(record.getValue()), 10000);
		cloudSolrServer.commit();
	}

	@Override
	public void deleteCollection(SolrSystemObj systemObj, String collectionName)
			throws SolrServerException, IOException {
		CloudSolrServer cloudSolrServer = createSolrServerInstance(systemObj);
		cloudSolrServer.deleteByQuery("europeana_collectionName:" + collectionName + "_*", 10000);
		cloudSolrServer.commit();
	}

	@Override
	public Record identifyRecord(SolrSystemObj systemObj, Record record)
			throws MalformedURLException, SolrServerException {

		CloudSolrServer cloudSolrServer = createSolrServerInstance(systemObj);

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", record.getField() + ":" + ClientUtils.escapeQueryChars(record.getValue()));
		SolrDocumentList results;

		results = cloudSolrServer.query(params).getResults();
		if (results.size() > 1) {
			throw new UniqueRecordException(record.getField() + ":" + record.getValue());
		} else if (results.size() == 1) {
			Record recNew = new Record();
			recNew.setField("europeana_id");
			recNew.setValue(results.get(0).get("europeana_id").toString());
			return recNew;
		}

		return null;
	}
	
	public boolean optimize(SolrSystemObj systemObj) throws SolrServerException, IOException {
		CloudSolrServer cloudSolrServer = createSolrServerInstance(systemObj);
		cloudSolrServer.optimize();
		return true;
	}

	private CloudSolrServer createSolrServerInstance(SolrSystemObj systemObj) throws MalformedURLException {
		LBHttpSolrServer lbTarget = new LBHttpSolrServer(systemObj.getUrls().split(","));
		CloudSolrServer solrServer = new CloudSolrServer(systemObj.getZookeeperURL(), lbTarget);
		solrServer.setDefaultCollection(systemObj.getSolrCore());
		solrServer.connect();
		return solrServer;
	}
}

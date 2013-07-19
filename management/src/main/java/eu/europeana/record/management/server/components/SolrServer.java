/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.record.management.server.components;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import eu.europeana.record.management.shared.dto.Record;
import eu.europeana.record.management.shared.exceptions.UniqueRecordException;

/**
 * Solr Server implementation
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class SolrServer implements Server {

	String url;

	public void deleteRecord(Record record) {
		HttpSolrServer solrServer = new HttpSolrServer(url);
		try {
			solrServer.deleteByQuery(
					"europeana_id:"
							+ ClientUtils.escapeQueryChars(record.getValue()),
					10000);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void deleteCollection(String collectionName) {
		HttpSolrServer solrServer = new HttpSolrServer(url);
		try {
			solrServer.deleteByQuery("europeana_collectionName:"
					+ collectionName + "*", 10000);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Record identifyRecord(Record record) throws UniqueRecordException {
		HttpSolrServer solrServer = new HttpSolrServer(url);

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", record.getField()+":"+ClientUtils.escapeQueryChars(record.getValue()));
		SolrDocumentList results;
		try {
			results = solrServer.query(params).getResults();
			if (results.size() > 1) {
				throw new UniqueRecordException(record.getField() + ":"
						+ record.getValue());
			} else if (results.size() == 1) {
				Record recNew = new Record();
				recNew.setField("europeana_id");
				recNew.setValue(results.get(0).get("europeana_id").toString());
				return recNew;
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

}

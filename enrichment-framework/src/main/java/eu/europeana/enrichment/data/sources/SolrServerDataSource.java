/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.data.sources;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.xml.sax.helpers.DefaultHandler;

import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.converter.ConverterHandlerDataObjects;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.LiteralValue;

/**
 * Source dataset to get data from a running SOLR server.
 * 
 * @author Borys Omelayenko
 * 
 */
public class SolrServerDataSource extends AbstractQueryDataSource {

	SolrServer server;

	String idField;

	List<String> fields = new ArrayList<String>();

	public SolrServerDataSource(Environment environment, String solrSelectUrl,
			String idField, String... field) throws MalformedURLException {
		server = new HttpSolrServer(solrSelectUrl);
		this.idField = idField;
		for (String f : field) {
			this.fields.add(f);
		}
		addQuery("*:*");
		System.out.println(solrSelectUrl + "-" + idField);
	}

	@Override
	protected boolean parseQuery(DefaultHandler handler, String query,
			Path recordSeparatingPath, Path recordIdentifyingPath)
			throws Exception {

		ConverterHandlerDataObjects flatHandler = makeHandler(handler,
				recordSeparatingPath);

		boolean passedARecord = false;

		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQueryType("advanced");
		solrQuery.setQuery(query);
		solrQuery.setRows(500);
		solrQuery.setStart(0);
		solrQuery.setParam("spellcheck", false);

		System.out.println("query: " + solrQuery);
		QueryResponse response = server.query(solrQuery);
		System.out.println(response.getResponseHeader());
		System.out.println(response.getResults().size());
		for (SolrDocument doc : response.getResults()) {

			flatHandler.startDocument();
			passedARecord = true;
			String id = doc.getFirstValue(idField).toString();
			flatHandler.attemptDataObjectChange(id);

			for (String fieldName : doc.getFieldNames()) {

				for (Object value : doc.getFieldValues(fieldName)) {

					String preprocessedValue = preprocessValue(fieldName,
							value.toString());
					if (preprocessedValue != null) {
						flatHandler.addField(fieldName, new LiteralValue(
								preprocessedValue));
						System.out.println(id + "-" + fieldName + "-"
								+ preprocessedValue);
					}
				}
			}
			flatHandler.endDocument();
		}
		return passedARecord;
	}

}

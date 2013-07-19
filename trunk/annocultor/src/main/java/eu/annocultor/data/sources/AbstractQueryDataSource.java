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
package eu.annocultor.data.sources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.helpers.DefaultHandler;

import eu.annocultor.api.DataSource;
import eu.annocultor.converter.ConverterHandler;
import eu.annocultor.converter.ConverterHandlerDataObjects;
import eu.annocultor.converter.ConverterHandler.ConversionResult;
import eu.annocultor.path.Path;


/**
 * Source dataset for queries.
 * 
 * @author Borys Omelayenko
 * 
 */
abstract class AbstractQueryDataSource implements DataSource
{
	public final String ANNOCULTOR_DATASOURCE_ITERATOR_10 = "$ANNOCULTOR_DATASOURCE_ITERATOR_10";

	Logger log = LoggerFactory.getLogger(getClass().getName());

	// init: connection url
	private String connectionUrl;

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public String getConnectionUrl() {
		return connectionUrl;
	}

	// init: queries
	private List<String> queries = new ArrayList<String>();

	public void addQuery(String query) {
		queries.add(query);
	}

	public List<String> getQueries() {
		return queries;
	}

	@Override
	public void feedData(ConverterHandler handler, Path recordSeparatingPath, Path recordIdentifyingPath)
	throws Exception {

		boolean conversionResult = parseQueries(handler, recordSeparatingPath, recordIdentifyingPath);
		handler.setConversionResult(conversionResult ? ConversionResult.success : ConversionResult.failure);
	}

	protected boolean parseQueries(DefaultHandler handler, Path recordSeparatingPath, Path recordIdentifyingPath) 
	throws Exception {

		boolean passedARecord = false;
		for (String query : queries) {

			for (String expandedQuery : expandIterators(query)) {
				logBeforeQuery(expandedQuery, recordSeparatingPath.getPath());
				passedARecord |= parseQuery(handler, expandedQuery, recordSeparatingPath, recordIdentifyingPath);			
				logAfterQuery(expandedQuery);
			}
		}	
		if (!passedARecord) {
			throw new Exception("Error: Empty query result. Nothing is done.");
		}
		return passedARecord;
	}

	protected abstract boolean parseQuery(DefaultHandler handler, String query, Path recordSeparatingPath, Path recordIdentifyingPath) throws Exception;

	protected void logAfterQuery(String query) {
		log.info("Passed query");
	}

	protected void logBeforeQuery(String query, String recordSeparatingPath) {
		log.info("Prepareing query \n" + query + "\nWith separating path: " + recordSeparatingPath);
	}

	class QueryIterator implements Iterable<String>, Iterator<String> {

		long current = 0;
		long limit = 0;
		String token;
		String query;

		public QueryIterator(String query, String token, long limit) {
			this.limit = limit;
			this.token = token;
			this.query = query;
		}

		@Override
		public Iterator<String> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return current < limit;
		}

		@Override
		public String next() {
			return StringUtils.replace(query, token, "" + (current++));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	protected QueryIterator expandIterators(String query) {
		if (query.contains(ANNOCULTOR_DATASOURCE_ITERATOR_10)) {
			return new QueryIterator(query, ANNOCULTOR_DATASOURCE_ITERATOR_10, 10); 
		} 
		return new QueryIterator(query, null, 1);
	}
	
	protected String preprocessValue(String fieldName, String fieldValue) throws Exception {
		return fieldValue;
	}

	protected ConverterHandlerDataObjects makeHandler(DefaultHandler handler, Path recordSeparatingPath) {
		return new ConverterHandlerDataObjects(handler, recordSeparatingPath);
	}
}
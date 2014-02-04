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

import org.xml.sax.helpers.DefaultHandler;

import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.converter.ConverterHandlerDataObjects;
import eu.europeana.enrichment.path.Path;


/**
 * Source dataset consisting of an SQL query ResultSet, aggregates all records
 * that belong to a single object into one.
 * 
 * @author Borys Omelayenko
 * 
 */
public class AggregatingSqlDataSource extends SqlDataSource {
	
	public AggregatingSqlDataSource(Environment environment, String jdbcDriver, String jdbcUrl, String... sqlQuery) 
	throws ClassNotFoundException {
		super(environment, jdbcDriver, jdbcUrl, sqlQuery);
	}

	@Override
	protected ConverterHandlerDataObjects makeHandler(DefaultHandler handler, Path recordSeparatingPath) {
		ConverterHandlerDataObjects handlerDataObjects = new ConverterHandlerDataObjects(handler, recordSeparatingPath);
		handlerDataObjects.setAggregate(true);
		return handlerDataObjects;
	}

}

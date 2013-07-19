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

import java.io.IOException;

import org.xml.sax.helpers.DefaultHandler;

import eu.annocultor.context.Environment;
import eu.annocultor.converter.ConverterHandlerDataObjects;
import eu.annocultor.path.Path;


/**
 * 
 * @author Borys Omelayenko
 * 
 */
public class AggregatingSparqlDataSource extends SparqlDataSource {

	public AggregatingSparqlDataSource(Environment environment, String sparqlQuery, String... file) 
	throws IOException {
		super(environment, sparqlQuery, file);
	}

	@Override
	protected ConverterHandlerDataObjects makeHandler(DefaultHandler handler, Path recordSeparatingPath) {
		ConverterHandlerDataObjects handlerDataObjects = new ConverterHandlerDataObjects(handler, recordSeparatingPath);
		handlerDataObjects.setAggregate(true);
		return handlerDataObjects;
	}

}

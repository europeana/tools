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
package eu.europeana.enrichment.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFHandlerException;

import eu.europeana.enrichment.common.Helper;
import eu.europeana.enrichment.common.Utils;
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.utils.SesameWriter;

/**
 * Apply a SPARQL query to a set of RDF files, and save the results of this query to another file;
 * made as a convenience method to use in XML converters. 
 * 
 * @author Borys Omelayenko
 *
 */
public class SparqlQueryHelper {

	Repository rdf;
	RepositoryConnection connection;
	TupleQueryResult queryResult;
	final String namespacePrefix;
	
	public SparqlQueryHelper(String namespacePrefix) {
		this.namespacePrefix = namespacePrefix;
	}

	public static void filter(
			Namespaces namespaces, 
			String namespacePrefix, 
			String outputFilePrefix, 
			String query, 
			String... inputFilePattern) throws Exception {
	
		List<File> files = new ArrayList<File>();
		for (String pattern : inputFilePattern) {
			files.addAll(Utils.expandFileTemplateFrom(new File("."), pattern));
		}
		SparqlQueryHelper sqh = new SparqlQueryHelper(namespacePrefix);
		try {
			sqh.open();
			sqh.load(namespacePrefix, files.toArray(new File[]{}));
			sqh.query(query);
			sqh.save(namespaces, outputFilePrefix);
		} finally {
			sqh.close();
		}
	}

	private void open() throws Exception {
		rdf = Helper.createLocalRepository();
	}

	private void load(String namespace, File... files ) throws Exception {
		Helper.importRDFXMLFile(rdf, namespace, files);
	}

	private void query(String query) throws Exception {
		connection = rdf.getConnection();
		TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
		preparedQuery.setIncludeInferred(false);
		queryResult = preparedQuery.evaluate();
	}

	private void save(Namespaces namespaces, String outputFilePrefix) throws Exception {
		
		SesameWriter writer = SesameWriter.createRDFXMLWriter(new File(outputFilePrefix + ".1.rdf"), namespaces, "id", "description", 1000, 1000);
		ValueFactory valueFactory = connection.getValueFactory();
		writer.startRDF();
		while (queryResult.hasNext()) {
			final BindingSet binding = queryResult.next();
			final URI subject = valueFactory.createURI(binding.getBinding("subject").getValue().stringValue());
			final URI property = valueFactory.createURI(binding.getBinding("property").getValue().stringValue());
			final Value value = binding.getValue("value");
			Statement statement = valueFactory.createStatement(
					subject, 
					property, 
					value);
			writer.handleStatement(statement);
			
			if (!subject.stringValue().startsWith(namespacePrefix)) {
				throw new Exception("Expected " + subject.stringValue() + " to start with " + namespacePrefix);
			}

			writeStatementIntoSeparateFileByNamespace(statement, outputFilePrefix, namespaces);
		}		
		writer.endRDF();
		
		closeWritersByNamespace();
	}

	private void closeWritersByNamespace() throws RDFHandlerException {
		for (SesameWriter nsWriter : filesPerNamespace.values()) {
			nsWriter.endRDF();
		}
	}

	private void writeStatementIntoSeparateFileByNamespace(
			Statement statement,
			String outputFilePrefix, 
			Namespaces namespaces) throws Exception {
		String subject = statement.getSubject().stringValue(); 
		String nsFull = subject.substring(namespacePrefix.length());
		String nss[] = nsFull.split("/");
		String first = nss[0];
		String second = nss[1];
		if (StringUtils.containsAny(second, "0123456789")) {
			second = "";
		} else {
			second = "_" + second;
		}
		String ns = first + second;
		if (!filesPerNamespace.containsKey(ns)) {
			File nsFile = new File(outputFilePrefix + "_" + ns + ".1.rdf");
			SesameWriter writer = SesameWriter.createRDFXMLWriter(nsFile, namespaces, "id", "Exported items belonging to " + nsFull, 1000, 1000);
			writer.startRDF();
			filesPerNamespace.put(ns, writer);
		}

		SesameWriter writer = filesPerNamespace.get(ns);
		writer.handleStatement(statement);
	}

	Map<String, SesameWriter> filesPerNamespace = new HashMap<String, SesameWriter>();
	
	private void close() throws Exception {
		if (queryResult != null)
			queryResult.close();
		if (connection != null)
			connection.close();
		if (rdf != null)
			rdf.shutDown();
	}

}


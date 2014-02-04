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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.helpers.DefaultHandler;

import eu.europeana.enrichment.common.Helper;
import eu.europeana.enrichment.common.Utils;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.converter.ConverterHandlerDataObjects;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.LiteralValue;

/**
 * SPARQL datasource.
 * 
 * @author Borys Omelayenko
 * 
 */
public class SparqlDataSource extends AbstractQueryDataSource {

	private RepositoryConnection connection;

	private List<File> srcFiles = new ArrayList<File>();

	public SparqlDataSource(Environment environment, String sparqlQuery,
			String... file) throws IOException {
		addQuery(sparqlQuery);
		File inputDir = new File(
				environment
						.getParameter(Environment.PARAMETERS.ANNOCULTOR_INPUT_DIR));
		addSourceFile(inputDir, file);
	}

	public void addSourceFile(File dir, String... pattern) throws IOException {
		if (dir == null) {
			throw new IOException("Null dir in source RDF files ");
		}

		List<File> files = Utils.expandFileTemplateFrom(dir, pattern);
		if (files.size() == 0) {
			throw new IOException("No single file found with pattern "
					+ StringUtils.join(pattern, ",") + " in dir "
					+ dir.getCanonicalPath());
		}

		srcFiles.addAll(files);
	}

	@Override
	protected boolean parseQueries(DefaultHandler handler,
			Path recordSeparatingPath, Path recordIdentifyingPath)
			throws Exception {
		Repository rdf = createRepository();
		try {
			Helper.importRDFXMLFile(rdf, "http://localhost/namespace",
					srcFiles.toArray(new File[] {}));
			connection = rdf.getConnection();
			return super.parseQueries(handler, recordSeparatingPath,
					recordIdentifyingPath);
		} finally {
			connection.close();
			rdf.shutDown();
		}
	}

	protected Repository createRepository() throws RepositoryException {
		Repository rdf = Helper.createLocalRepository();
		return rdf;
	}

	@Override
	protected boolean parseQuery(DefaultHandler handler, String query,
			Path recordSeparatingPath, Path recordIdentifyingPath)
			throws Exception {

		ConverterHandlerDataObjects flatHandler = makeHandler(handler,
				recordSeparatingPath);

		boolean passedARecord = false;
		TupleQueryResult resultSet = connection.prepareTupleQuery(
				getQueryLanguage(), query).evaluate();
		List<String> bindingNames = resultSet.getBindingNames();
		try {

			flatHandler.startDocument();

			while (resultSet.hasNext()) {

				passedARecord = true;

				// iterate result set fields
				BindingSet bindingSet = resultSet.next();
				Value identifier = bindingSet.getValue(recordIdentifyingPath
						.getPath());
				if (identifier == null) {
					throw new NullPointerException("On path "
							+ recordIdentifyingPath + " binding set: "
							+ bindingSet);
				}
				flatHandler.attemptDataObjectChange(identifier.stringValue());
				for (String columnName : bindingNames) {

					Value openrdfValue = bindingSet.getValue(columnName);
					if (openrdfValue != null) {
						String columnValue = openrdfValue.stringValue().trim();
						// TODO: pull xml:lang through preprocess step
						String lang = null;
						if (openrdfValue instanceof LiteralImpl) {
							lang = ((LiteralImpl) openrdfValue).getLanguage();
							if (lang != null) {
								System.out.println("LANG " + lang + " on "
										+ columnValue);
							}
						}
						String preprocessedValue = preprocessValue(columnName,
								columnValue);
						if (preprocessedValue != null) {

							flatHandler.addField(columnName, new LiteralValue(
									preprocessedValue, lang));
						}
					}
				}
			}
			flatHandler.endDocument();
		} finally {
			resultSet.close();
		}
		return passedARecord;
	}

	protected QueryLanguage getQueryLanguage() {
		return QueryLanguage.SPARQL;
	}

}

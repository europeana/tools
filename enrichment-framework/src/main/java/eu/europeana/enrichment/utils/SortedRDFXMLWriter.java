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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in
 * XML-serialized RDF format.
 */
public class SortedRDFXMLWriter extends RDFXMLWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	List<Statement> statements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFXMLWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *            The OutputStream to write the RDF/XML document to.
	 */
	public SortedRDFXMLWriter(OutputStream out) {
		this(new OutputStreamWriter(out, Charset.forName("UTF-8")));
	}

	/**
	 * Creates a new RDFXMLWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *            The Writer to write the RDF/XML document to.
	 */
	public SortedRDFXMLWriter(Writer writer) {
		super(writer);
		statements = new LinkedList<Statement>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		if (!writingStarted) {
			throw new RuntimeException(
					"Document writing has not yet been started");
		}

		// we only add statement to the list, postponing
		// actual writing to flushPendingStatements()
		statements.add(st);

	}

	/**
	 * Returns predicate name in the form <code>ns:name</code>
	 */
	private String getPredicatePrintName(URI uri) {
		return uri.toString();
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		try {
			// write headers same as super.endRDF() does
			if (!headerWritten) {
				writeHeader();
			}

			// sort statements by subject-predicate-object
			Collections.sort(statements, new Comparator<Statement>() {

				public int compare(Statement left, Statement right) {
					// sort by subject
					int result = left.getSubject().stringValue()
							.compareTo(right.getSubject().stringValue());
					if (result == 0) {
						// sort by property within a subject, based on namespace
						// nicks
						result = getPredicatePrintName(left.getPredicate())
								.compareTo(
										getPredicatePrintName(right
												.getPredicate()));
						if (result == 0) {
							// sort by object within the same subject and
							// property
							result = left.getObject().stringValue()
									.compareTo(right.getObject().stringValue());
						}
					}
					return result;
				}

			});

			// write all statements
			for (Statement st : statements) {
				try {
					super.handleStatement(st);
				} catch (Exception e) {
					throw new Exception("Error handling statement " + st, e);
				}
			}

			statements.clear();
		} catch (Exception e) {
			throw new RDFHandlerException(e);
		}

		super.endRDF();
	}

}

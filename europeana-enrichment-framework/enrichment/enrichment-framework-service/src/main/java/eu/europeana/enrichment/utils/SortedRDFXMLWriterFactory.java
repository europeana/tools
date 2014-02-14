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
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

/**
 * An {@link RDFWriterFactory} for sorted RDF/XML writer.
 * 
 */
public class SortedRDFXMLWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#RDFXML}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.RDFXML;
	}

	/**
	 * Returns a new instance of {@link RDFXMLWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new SortedRDFXMLWriter(out);
	}

	/**
	 * Returns a new instance of {@link RDFXMLWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new SortedRDFXMLWriter(writer);
	}
}

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
package eu.europeana.enrichment.data.destinations;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.Graph;

public class XmlFile extends AbstractFileWritingGraph {
	private PrintWriter persistenceWriter;

	/**
	 * Target that corresponds to an output ESE file.
	 * 
	 * @param datasetId
	 *            signature of a dataset (conversion task). null indicates that
	 *            this graph is not a result of a conversion task.
	 * @param objectType
	 *            signature, typically the type (class) of objects stored in
	 *            this RDF.
	 * @param propertyType
	 *            signature, typically the type of properties stored in this
	 *            RDF.
	 * @param comment
	 *            Descriptive text put into the RDF file header
	 */
	public XmlFile(String datasetId, Environment environment,
			String datasetModifier, String objectType, String propertyType,
			String... comment) {
		super(datasetId, environment, datasetModifier, objectType,
				propertyType, "xml", comment);
	}

	@Override
	public void startRdf() throws Exception {
		super.startRdf();

		persistenceWriter = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(
						getFinalFile(getVolume())), "UTF8")));

		persistenceWriter.println("<?xml version='1.0' encoding='UTF-8'?>");
		persistenceWriter.println("<metadata ");
		for (String uri : getEnvironment().getNamespaces().listAllUris()) {
			persistenceWriter.println("xmlns:"
					+ getEnvironment().getNamespaces().getNick(uri) + "=\""
					+ uri + "\"");
		}
		persistenceWriter.println(">");
	}

	@Override
	public void finishObject() throws Exception {
		persistenceWriter.println("  </record>");
	}

	@Override
	public void startObject(String subject) throws Exception {
		persistenceWriter.println("  <record>");
		persistenceWriter.println("	   <europeana:uri>" + subject
				+ "</europeana:uri>");
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Graph))
			return false;
		Graph tObj = (Graph) obj;
		return getId().equals(tObj.getId());
	}

	@Override
	public void writeTriple(Triple triple) throws Exception {
		String tag = triple.getProperty().getUri();
		persistenceWriter.println("    <" + tag + ">"
				+ triple.getValue().getValue() + "</" + tag + ">");
	}

	@Override
	public void endRdf() throws Exception {
		if (writingHappened()) {
			finishObject();
			persistenceWriter.println("</metadata>");
			persistenceWriter.flush();
			persistenceWriter.close();
		}
	}
}

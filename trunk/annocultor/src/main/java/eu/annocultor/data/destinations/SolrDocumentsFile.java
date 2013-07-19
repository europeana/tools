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
package eu.annocultor.data.destinations;

import java.io.PrintWriter;

import org.apache.commons.io.output.FileWriterWithEncoding;

import eu.annocultor.context.Environment;
import eu.annocultor.triple.Triple;

public class SolrDocumentsFile extends AbstractFileWritingGraph
{

	private String lastWrittenUri = null;

	private PrintWriter writer;

	public SolrDocumentsFile(
			String datasetId,
			Environment environment,
			String datasetModifier,
			String objectType,
			String propertyType,
			String... comment)
	{
		super(datasetId, environment, datasetModifier, objectType, propertyType, "txt", comment);
	}

	@Override
	public void endRdf() throws Exception {
		if (writingHappened()) {
			writer.println("  </doc>");
			writer.println("</add>");
			writer.close();		
			lastWrittenUri = null;
		}
	}


	@Override
	public void startRdf() throws Exception {
		super.startRdf();
		writer = new PrintWriter(new FileWriterWithEncoding(getFinalFile(getVolume()), "UTF-8"));
		writer.println("<add>");
	}

	@Override
	public void writeTriple(Triple triple) throws Exception {

		if (objectChanged(triple)) {
			if (objectWasWritten()) {
				writer.println("  </doc>");				
			}
			writer.println("  <doc>");
			lastWrittenUri = triple.getSubject();
		}

		writer.println("  <field name=\"" + triple.getProperty() + "\">" + triple.getValue().getValue() + "</field>");

	}

	private boolean objectWasWritten() {
		return lastWrittenUri != null;
	}

	private boolean objectChanged(Triple triple) {
		return !triple.getSubject().equals(lastWrittenUri);
	}

}

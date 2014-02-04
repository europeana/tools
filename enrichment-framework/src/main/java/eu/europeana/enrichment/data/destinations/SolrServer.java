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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.Value;

/**
 * Commits data into a running SOLR server.
 * 
 * @author Borys Omelayenko
 * 
 */
public class SolrServer extends AbstractFileWritingGraph {

	private String solrUrl;

	HttpSolrServer server;

	Stack<SolrInputDocument> documents = new Stack<SolrInputDocument>();

	String lastWrittenUri;

	private static class FieldDefinition {
		boolean isMultiValued = false;
		String dataType = null;
		String name = null;

		public FieldDefinition(String line) {
			for (String token : StringUtils.trim(line).split(" ")) {
				if (token.startsWith("name=\"")) {
					name = StringUtils.substringBetween(token, "\"", "\"");
				}
				if (token.startsWith("type=\"")) {
					dataType = StringUtils.substringBetween(token, "\"", "\"");
				}
				if (token.startsWith("multiValued=\"")) {
					isMultiValued = Boolean.parseBoolean(StringUtils
							.substringBetween(token, "\"", "\""));
				}
			}

			if (name == null) {
				throw new RuntimeException(
						"Name is missing in schema.xml line " + line);
			}
			if (dataType == null) {
				throw new RuntimeException(
						"Data type is missing in schema.xml line " + line);
			}
			System.out.println("Loaded field " + name + " of " + dataType
					+ " multi: " + isMultiValued);
		}

		public boolean isMultiValued() {
			return isMultiValued;
		}

		public String getDataType() {
			return dataType;
		}

		public String getName() {
			return name;
		}

	}

	Map<String, FieldDefinition> fieldDefinitions = new HashMap<String, FieldDefinition>();

	public SolrServer(String datasetId, Environment environment,
			String datasetModifier, String objectType, String propertyType,
			String... comment) {
		super(datasetId, environment, datasetModifier, objectType,
				propertyType, "txt", comment);
		solrUrl = comment[0];
		try {
			String location = comment[1];
			InputStream is = location.startsWith("http://") ? new URL(location)
					.openStream() : new FileInputStream(location);
			for (String line : IOUtils.readLines(is, "UTF-8")) {
				String trimmedLine = StringUtils.trim(line);
				if (trimmedLine.startsWith("<field name=")
						|| trimmedLine.startsWith("<dynamicField name=")) {
					final FieldDefinition fieldDefinition = new FieldDefinition(
							line);
					fieldDefinitions.put(fieldDefinition.getName(),
							fieldDefinition);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startRdf() throws Exception {
		server = new HttpSolrServer(solrUrl);
		server.setRequestWriter(new BinaryRequestWriter());
	}

	@Override
	public void endRdf() throws Exception {
		flush();
		lastWrittenUri = null;
	}

	private void flush() throws Exception {

		int docs = documents.size();
		System.out.println("Start with " + docs);
		while (!documents.isEmpty()) {
			final SolrInputDocument document = documents.pop();
			if (document == null || document.getFieldNames().isEmpty()) {
				docs--;
			} else {
				log.info("Document to server: " + document.toString());
				try {
					server.add(document);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		server.commit();
		log.info("Committed " + docs + " documents");
	}

	boolean isPreamptiveFlushNeeded() {
		return documents.size() > 1000;
	}

	@Override
	public void writeTriple(Triple triple) throws Exception {

		if (subjectChanged(triple)) {
			lastWrittenUri = triple.getSubject();
			if (isPreamptiveFlushNeeded()) {
				flush();
			}
			log.info("Starting document " + lastWrittenUri);
			documents.add(new SolrInputDocument());
		}

		String property = extractSolrProperty(triple);
		// if (Concepts.ANNOCULTOR.PERIOD_BEGIN.getUri().equals(property)) {
		// property = "enrichment_time_begin";
		// }
		// if (Concepts.ANNOCULTOR.PERIOD_END.getUri().equals(property)) {
		// property = "enrichment_time_end";
		// }
		//
		FieldDefinition fieldDefinition = fieldDefinitions.get(property);
		if (fieldDefinition == null) {
			System.out
					.println("Field "
							+ property
							+ " does not have exact match. Trying wildcards assuming that they have form blabla.*");
			String wildcarded = StringUtils.substringBeforeLast(property, ".")
					+ ".*";
			fieldDefinition = fieldDefinitions.get(wildcarded);
		}
		if (fieldDefinition == null) {
			System.out.println("Skipped " + property
					+ " because it is not defined");
			// throw new Exception("Field " + triple.getProperty() +
			// " is not defined in schema.xml");
		} else {
			// if (fieldDefinition.dataType)
			System.out.println("Add " + property + "-"
					+ triple.getValue().getValue() + " of type "
					+ fieldDefinition.dataType);
			Object value = triple.getValue().getValue();
			if (fieldDefinition.dataType.equals("tdate")) {
				System.out.println("Recognized type tdate");
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				format.setTimeZone(TimeZone.getTimeZone("UTC"));
				value = format.parse(triple.getValue().getValue());
			}
			if (fieldDefinition.isMultiValued()) {
				documents.peek().addField(property, value);
			} else {
				documents.peek().setField(property, value);
			}
		}
	}

	private String extractSolrProperty(Triple triple) {
		String property = triple.getProperty().getUri();
		Value value = triple.getValue();
		if (value != null && value instanceof LiteralValue) {
			String lang = ((LiteralValue) value).getLang();
			if (StringUtils.length(lang) == 2) {
				property += "." + lang;
			}
		}
		return property;
	}

	private boolean subjectChanged(Triple triple) {
		return !triple.getSubject().equals(lastWrittenUri);
	}

	@Override
	void cleanAllFileVolumes() {
		// intentionally do nothing
	}

	@Override
	public File getFinalFile(int volume) throws IOException {
		return new File("Files are unapplicable to SolServer");
	}

}

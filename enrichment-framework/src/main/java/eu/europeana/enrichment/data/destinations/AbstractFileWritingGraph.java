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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.ValueHelper;
import eu.europeana.enrichment.utils.SesameWriter;
import eu.europeana.enrichment.xconverter.api.Graph;

public abstract class AbstractFileWritingGraph extends AbstractGraph {
	Logger log = LoggerFactory.getLogger(getClass().getName());

	private final long MAX_TRIPLES_PER_FILE = 300000;

	private static final int CACHE_SIZE = 100;

	private Map<Property, Integer> properties = new HashMap<Property, Integer>();

	private Map<String, Integer> propertySourcePath = new HashMap<String, Integer>();

	private Environment environment;

	private boolean startedWriting;

	private String extension;

	/**
	 * Target that corresponds to an output RDF file.
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
	public AbstractFileWritingGraph(String datasetId, Environment environment,
			String datasetModifier, String objectType, String propertyType,
			String extension, String... comment) {
		super(Common.makeNewNamedGraphId(datasetId, datasetModifier,
				objectType, propertyType), comment);
		this.extension = extension;
		this.environment = environment;

		setRealGraph(this);
		startedWriting = false;

		cleanAllFileVolumes();
	}

	void cleanAllFileVolumes() {
		File[] files = environment.getOutputDir().listFiles(
				new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.matches(getId() + "\\.\\d+\\." + extension);
					}
				});

		if (files != null) {
			for (File file : files) {
				if (!file.delete()) {
					log.warn("Failed to delete file " + file.getName());
				}
			}
		}
	}

	public void startRdf() throws Exception {
		if (startedWriting) {
			throw new RuntimeException("Writing has already started");
		}
		startedWriting = true;
	}

	public File getFinalFile(int volume) throws IOException {
		return new File(environment.getOutputDir(), getId() + "." + volume
				+ "." + extension);
	}

	private String makePropertySourcePath(Triple triple) {
		return triple.getProperty().getUri()
				+ "\n    source: "
				+ (triple.getRule() == null ? "NORULE" : ((triple.getRule()
						.getSourcePath() == null ? "NOPATH" : triple.getRule()
						.getSourcePath().getPath())));
	}

	private Set<String> subjects = new HashSet<String>();

	public Set<String> getSubject() {
		return subjects;
	}

	private void checkTriple(final Triple t) throws Exception {
		if (t == null)
			throw new Exception("TripleBuffer Rejects NULL triple " + t);
		if (t.getSubject() == null)
			throw new Exception(
					"TripleBuffer Rejects triple with NULL subject " + t);
		if (t.getSubject().length() == 0)
			throw new Exception(
					"TripleBuffer Rejects triple with empty subject " + t);
		if (!(t.getSubject().startsWith("http://") || t.getSubject()
				.startsWith("https://")))
			throw new Exception(
					"TripleBuffer Rejects triple with subject that does not start with 'http(s)://' "
							+ t);
		if (t.getSubject().contains("#http://")
				|| t.getSubject().contains("/http://"))
			throw new Exception(
					"Found suspicious pattern in subject #http:// or /http://, suggesting a mistake "
							+ t);

		// property
		if (t.getProperty() == null)
			throw new Exception(
					"TripleBuffer Rejects triple with NULL property, "
							+ t.getSubject());
		SesameWriter.checkPredicateUri(t.getProperty().getUri());

		// value
		if (t.getValue() == null)
			throw new Exception("TripleBuffer Rejects triple with NULL value, "
					+ t.getSubject());
		if (t.getValue().getValue() == null)
			throw new Exception(
					"TripleBuffer Rejects triple with NULL value of value, "
							+ t.getSubject());
		if (t.getValue().getValue().contains("#http://")
				|| t.getValue().getValue().contains("/http://"))
			log.debug("Found suspicious pattern in value #http:// or /http://, suggesting a mistake "
					+ t);

		if (ValueHelper.isResource(t.getValue())) {
			if (t.getValue().getValue().contains(";"))
				throw new Exception(
						"TripleBuffer Rejects resource triple with ; in resource value, "
								+ t);
			if (t.getValue().getValue().length() == 0)
				throw new Exception(
						"TripleBuffer Rejects resource triple with zero-length of value "
								+ t);
			if (!(t.getValue().getValue().startsWith("http://") || t.getValue()
					.getValue().startsWith("https://")))
				throw new Exception(
						"TripleBuffer Rejects resource triple with value that does not start with 'http(s)://' "
								+ t);
		}
	}

	private long size = 0;

	public long size() {
		return size;
	}

	LinkedList<Triple> tripleAddCache = new LinkedList<Triple>();

	public Triple getLastAddedTriple(int offset) {
		return tripleAddCache.get(offset);
	}

	private boolean isInCache(Triple triple) {
		return tripleAddCache.contains(triple);
	}

	private void cache(Triple triple) {
		tripleAddCache.addFirst(triple);
		while (tripleAddCache.size() > CACHE_SIZE) {
			tripleAddCache.removeLast();
		}
	}

	public void startObject(String subject) throws Exception {

	}

	public void finishObject() throws Exception {

	}

	private String lastSubject;

	private void checkStartVolume(String subject) throws Exception {

		if (size > getVolume() * MAX_TRIPLES_PER_FILE) {
			// check for resource boundary
			if (lastSubject == null || !lastSubject.equals(subject)) {
				// new resource, can start new volume
				endRdf();
				startedWriting = false;
				subjects = new HashSet<String>();
				incVolume();
			}
		}

		if (!startedWriting) {
			startRdf();
		}

		if (!StringUtils.equals(lastSubject, subject)) {
			if (lastSubject != null) {
				finishObject();
			}
			startObject(subject);
			lastSubject = subject;
		}
	}

	public final void add(Triple triple) throws Exception {
		if (getRealGraph() != this) {
			getRealGraph().add(triple);
		} else {
			checkTriple(triple);

			checkStartVolume(triple.getSubject());

			if (getListener() != null) {
				getListener().add(triple);
			}

			if (isInCache(triple)) {
				return;
			}
			cache(triple);

			// write into RDF file
			try {
				writeTriple(triple);
			} catch (Exception e) {
				// building last triples
				String msg = "Failed committing the following triple to Sesame "
						+ triple
						+ "\n"
						+ "This triple, and this record, may not be the cause, but the previous one \n"
						+ "The last triples that may actually cause the problem are as follows:";
				for (Triple t : tripleAddCache) {
					msg += t + "\n";
				}
				throw new Exception(msg, e);
			}
			size++;
			// RDF properties count
			if (properties.containsKey(triple.getProperty())) {
				int count = properties.get(triple.getProperty());
				properties.put(triple.getProperty(), count + 1);
			} else {
				properties.put(triple.getProperty(), 0);
			}
			// count on pairs (RDF property, XML path)
			String psp = makePropertySourcePath(triple);
			if (propertySourcePath.containsKey(psp)) {
				int count = propertySourcePath.get(psp);
				propertySourcePath.put(psp, count + 1);
			} else {
				propertySourcePath.put(psp, 0);
			}
			// RDF subjects
			if (!subjects.contains(triple.getSubject()))
				subjects.add(triple.getSubject());
		}
	}

	abstract public void writeTriple(Triple triple) throws Exception;

	public Set<Property> getProperties() {
		return properties.keySet();
	}

	public String getReport(Property property) throws Exception {
		try {
			String report = "";
			for (String psp : propertySourcePath.keySet()) {
				if (psp.startsWith(property.getUri())) {
					report += psp + "\n    count: "
							+ propertySourcePath.get(psp) + "\n";
				}
			}
			return report;
		} catch (Exception e) {
			throw new Exception("Property not found: " + property, e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Graph))
			return false;
		Graph tObj = (Graph) obj;
		return getId().equals(tObj.getId());
	}

	@Override
	public boolean writingHappened() throws Exception {
		return startedWriting;
	}

	abstract public void endRdf() throws Exception;

	public Environment getEnvironment() {
		return environment;
	}

	public static void createTempDocToShowWhileWorking(File file)
			throws IOException {
		FileUtils
				.writeStringToFile(
						file,
						"<html><head><title>AnnoCultor is working</title></head><body>"
								+ "<p><a href='http://annocultor.eu'>AnnoCultor</a> job was started on "
								+ new Date()
								+ " and is still running. Upon completion, a detailed report would appear at this location.</p>"
								+ "</body></head>", "UTF-8");
	}
}

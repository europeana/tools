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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;

public class PropertiesFile extends AbstractGraph {

	public PropertiesFile(
			String datasetId,
			Environment environment,
			String datasetModifier,
			String objectType,
			String propertyType,
			String... comment)
	{
		super(Common.makeNewNamedGraphId(
				datasetId,
				datasetModifier,
				objectType,
				propertyType), 
				comment);
		this.environment = environment;
		setRealGraph(this);
	}

	Properties properties = new Properties() {
		private static final long serialVersionUID = 8921648301937877174L;

		/**
		 * Sorted by key, to make text diff consistent
		 */
		@Override
		public Enumeration keys() {
			Enumeration keysEnum = super.keys();
			Vector<String> keyList = new Vector<String>();
			while(keysEnum.hasMoreElements()){
				keyList.add((String)keysEnum.nextElement());
			}
			Collections.sort(keyList);
			return keyList.elements();
		}
	};

	Property propertyRepresentedWithEqualsSign = null;

	Environment environment;

	@Override
	public void add(Triple triple) throws Exception {
		if (propertyRepresentedWithEqualsSign == null) {
			propertyRepresentedWithEqualsSign = triple.getProperty();
		}

		if (propertyRepresentedWithEqualsSign.equals(triple.getProperty())) {
			final String existingValue = properties.getProperty(convertSubjectToPropertyName(triple.getSubject()), "");
			if (!StringUtils.isEmpty(existingValue) && !existingValue.equals(triple.getValue().getValue())) {
				//				throw new Exception("Attempt to override " 
				//						+ existingValue
				//						+ " with "
				//						+ triple.getValue().getValue() 
				//						+ " at "
				//						+ triple.getSubject());
			}
			properties.setProperty(
					convertSubjectToPropertyName(triple.getSubject()),
					triple.getValue().getValue()
			);
		} else {
			throw new Exception("Property " + propertyRepresentedWithEqualsSign 
					+ " that is used in this file cannot be replaced (with " + triple.getProperty() 
					+ ") at subject " + triple.getSubject());
		}
	}

	protected String convertSubjectToPropertyName(String subject) {
		return subject;
	}

	@Override
	public boolean writingHappened() throws Exception {
		return true;
	}

	@Override
	public void endRdf() throws Exception {
		FileOutputStream os = new FileOutputStream(getFinalFile(getVolume()));
		properties.store(os, StringUtils.join(getComments(), "\n"));
		os.close();
	}

	@Override
	public File getFinalFile(int volume) throws IOException {
		return new File(environment.getOutputDir(), getId() + "." + volume + ".txt");
	}

	@Override
	public Triple getLastAddedTriple(int offset) {
		return null;
	}

	@Override
	public Set<Property> getProperties() {
		Set<Property> set = new HashSet<Property>();
		set.add(propertyRepresentedWithEqualsSign);
		return set;
	}

	@Override
	public long size() {
		return properties.size();
	}

	@Override
	public void startRdf() throws Exception {
		// nothing special to do
	}

}

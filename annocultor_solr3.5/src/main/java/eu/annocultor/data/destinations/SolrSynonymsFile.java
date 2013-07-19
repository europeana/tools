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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import eu.annocultor.context.Environment;
import eu.annocultor.triple.Triple;

public class SolrSynonymsFile extends AbstractFileWritingGraph
{

	private Properties synonyms = new Properties();

	public SolrSynonymsFile(
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
			OutputStream os = new FileOutputStream(getFinalFile(getVolume()));
			for (Entry<Object, Object> entry : synonyms.entrySet()) {
				IOUtils.write(entry.getKey() + "," + entry.getValue() + "\n", os, "UTF-8");
			}
			os.close();		
		}
	}

	@Override
	public void writeTriple(Triple triple) throws Exception {

		String collectedValues = synonyms.getProperty(triple.getSubject(), "");
		String newValue = triple.getValue().getValue().replaceAll("[\\s\\p{Punct}]", " ").trim();
		if (!newValue.isEmpty()) {
			synonyms.setProperty(triple.getSubject(), (collectedValues.isEmpty() ? "" : (collectedValues + ",")) + newValue);
		}
	}

}

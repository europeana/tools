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
package eu.europeana.enrichment.api;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import java.text.Normalizer;

import eu.europeana.enrichment.context.Namespace;

/**
 * Common constants.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Common
{

	/**
	 * Provides shared version id for the whole project.
	 */
	public static final long getCommonSerialVersionUID()
	{
		return 7;
	}

	/**
	 * Used to generate a name for a new named graph.
	 * 
	 * @param dataset
	 * @param datasetModifier
	 * @param objectType
	 * @param propertyType
	 * @return
	 */
	public static String makeNewNamedGraphId(
			String dataset,
			String datasetModifier,
			String objectType,
			String propertyType)
	{
		String graphId = "";
		graphId = appendPartOfGraphId(graphId, dataset);
		graphId = appendPartOfGraphId(graphId, datasetModifier);
		graphId = appendPartOfGraphId(graphId, objectType);
		graphId = appendPartOfGraphId(graphId, propertyType);
		return graphId;
	}

	private static String appendPartOfGraphId(String idString, String partOfId) {
		if (StringUtils.isEmpty(partOfId)) {
			return idString;
		} else {
			if (idString.isEmpty()) {
				return partOfId;
			} else {
				return idString + "." + partOfId;
			}
		}
	}
	
	private static Map<String, String> generatedNameBasedUris = new HashMap<String, String>();

	private static Pattern removeDiacriticPattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	public static String removeDiacritics(String text) {
		String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
		return removeDiacriticPattern.matcher(nfdNormalizedString).replaceAll("");
	}


	/**
	 * The single place where RDF ids are generated from term labels
	 * 
	 * @param namespace
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String generateNameBasedUri(Namespace namespace, String value) throws Exception
	{
		if (namespace == null)
			throw new NullPointerException("Namespace is null");
		if (value == null)
			throw new NullPointerException("Value is null");
		// remove non-alphanumeric
		value = value.replaceAll("\\W", "_");
		// get rid of accents
		String accentsGone = removeDiacritics(value);
		// UTF-8
		String uri = namespace + URLEncoder.encode(accentsGone, "UTF-8");
		if (generatedNameBasedUris.containsKey(uri))
		{
			String existingValue = generatedNameBasedUris.get(uri);
			if (accentsGone.equals(existingValue))
				return uri;
			else
				throw new Exception("Error: duplicated generated uri "
						+ uri
						+ ", this value "
						+ value
						+ ", already used for value "
						+ existingValue);
		}
		else
		{
			generatedNameBasedUris.put(uri, accentsGone);
			return uri;
		}
	}

}

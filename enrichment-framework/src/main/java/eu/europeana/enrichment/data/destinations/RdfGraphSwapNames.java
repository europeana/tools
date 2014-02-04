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

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.context.Environment;

public class RdfGraphSwapNames extends RdfGraph
{

	public RdfGraphSwapNames(String datasetId, Environment environment,
			String datasetModifier, String objectType, String propertyType,
			String... comment) {

		super(generateIdPrefix(datasetId, datasetModifier), 
				environment, 
				generateIdAffix(datasetId), 
				objectType, 
				propertyType,
				comment);
	}

	private static String generateIdPrefix(String datasetId, String datasetModifier) {
		return StringUtils.substringBefore(datasetId, "_") + "_" + datasetModifier;
	}

	private static String generateIdAffix(String datasetId) {
		return StringUtils.substringAfter(datasetId, "_");
	}


}

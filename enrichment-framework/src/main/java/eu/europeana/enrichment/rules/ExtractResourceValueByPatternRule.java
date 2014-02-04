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
package eu.europeana.enrichment.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;


/**
 * Rule to create an RDF literal property with the value specified by a regular
 * expression.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ExtractResourceValueByPatternRule extends RenameResourcePropertyRule
{
	private Pattern pattern;

	public ExtractResourceValueByPatternRule(
			Property trgProperty,
			String pattern,
			Namespace trgNamespace,
			Graph trgGraph)
	{
		super(trgProperty, trgNamespace, trgGraph);
		this.pattern = Pattern.compile(pattern);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		Matcher matcher = pattern.matcher(triple.getValue().getValue());

		if (matcher.find())
		{
			super.fire(triple.changeValue(new ResourceValue(matcher.group(1))), dataObject);
		}
	}
}

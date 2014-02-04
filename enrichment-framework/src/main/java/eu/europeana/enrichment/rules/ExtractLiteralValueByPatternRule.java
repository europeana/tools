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

import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Property;
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
public class ExtractLiteralValueByPatternRule extends RenameLiteralPropertyRule {
	private Pattern pattern;
	private int group;

	/**
	 * Uses a regular expression to pull a part of a value and to store it in a
	 * separate property.
	 * 
	 * @param group
	 *            group number in the pattern that is stored, e.g. 1, ...
	 */
	public ExtractLiteralValueByPatternRule(Property trgProperty,
			String pattern, String trgLang, int group, Graph trgGraph) {
		super(trgProperty, trgLang, trgGraph);
		this.pattern = Pattern.compile(pattern);
		this.group = group;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void fire(Triple triple, DataObject converter) throws Exception {
		Matcher matcher = pattern.matcher(triple.getValue().getValue());

		if (matcher.find()) {
			super.fire(
					triple.changeValue(new LiteralValue(matcher.group(group))),
					converter);
		}
	}
}

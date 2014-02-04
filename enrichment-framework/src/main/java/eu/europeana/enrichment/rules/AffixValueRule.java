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

import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.XmlValue;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.PropertyRule;

/**
 * Converts value by adding a prefix and a postfix.
 * 
 * @author Borys Omelayenko
 * @author Anna Tordai
 * 
 */
public class AffixValueRule extends SequenceRule
{
	private String prefix;

	private String suffix;

	@Override
	public String getAnalyticalRuleClass()
	{
		return "Value";
	}

	public AffixValueRule(String prefix, String suffix, PropertyRule... rule)
	{
		super(rule);
		this.prefix = prefix;
		this.suffix = suffix;
	}

	/**
	 * @inheritDoc
	 * 
	 */
	@Override
	public void fire(Triple triple, DataObject converter) throws Exception
	{
		super.fire(triple.changeValue(new XmlValue(prefix + triple.getValue() + suffix)), converter);
	}

}

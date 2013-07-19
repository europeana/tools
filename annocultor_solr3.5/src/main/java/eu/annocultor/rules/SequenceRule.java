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
package eu.annocultor.rules;

import eu.annocultor.annotations.AnnoCultor;
import eu.annocultor.api.Rule;
import eu.annocultor.path.Path;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.PropertyRule;

/**
 * Invokes several other rules in a sequence.
 * Typical use is to chain several <code>CreateResourcePropertyRule</code>s
 * to create several constant triples.
 * 
 * @author Borys Omelayenko
 * 
 */
public class SequenceRule extends PropertyRule
{

	public enum ExecultionMode
	{
		AllowSingleExecutionPerDataObject,
		AllowedMultipleExecutionsPerDataObject
	}

	private ExecultionMode executionMode;
	private String lastDataObject = null;

	@Override
	public String getAnalyticalRuleClass()
	{
		return "Sequence";
	}

	/**
	 * The sequence of rules would be applied once
	 * per data object. This is useful when applying the rules to 
	 * an attribute value, located in the beginning of XML path. 
	 * 
	 * @param rule rules to be invoked
	 */
	@AnnoCultor.XConverter( include = true, affix = "single" )
	public SequenceRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			PropertyRule... rule)
	{
		this(ExecultionMode.AllowSingleExecutionPerDataObject, rule);
		setSourcePath(srcPath);
	}

	/**
	 * @param rules to be invoked
	 */
	public SequenceRule(PropertyRule... rule)
	{
		this(ExecultionMode.AllowedMultipleExecutionsPerDataObject, rule);
	}

	public SequenceRule(ExecultionMode executionMode, PropertyRule... rules)
	{
		super(rules);
		this.executionMode = executionMode;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		XmlValue firstValue = dataObject.getFirstValue(dataObject.getIdPath());
		String newId = firstValue == null ? null : firstValue.getValue();
		if (executionMode == ExecultionMode.AllowSingleExecutionPerDataObject)
		{
			if (newId.equals(lastDataObject))
			{
				// attempt to apply this rule to the same data object multiple times
				return;
			}
			lastDataObject = newId;
		}
		for (Rule rule : getChildRules())
		{
			if (rule == this)
				throw new Exception("Cycle in sequence on rule " + rule);

			rule.fire(triple.changeRule(rule), dataObject);
			getTask().getReporter().reportRuleInvocation(this, triple, dataObject);
		}
	}

}

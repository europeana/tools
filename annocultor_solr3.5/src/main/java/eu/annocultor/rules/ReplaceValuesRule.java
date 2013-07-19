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
import eu.annocultor.path.Path;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.PropertyRule;
import eu.annocultor.xconverter.impl.XConverterFactory;
import eu.annocultor.xconverter.impl.XConverterFactory.MapObjectToObject;

/**
 * Replaces triple values, ignore case.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ReplaceValuesRule extends SequenceRule
{
	private String[] originalValues;
	private String[] replacements;

	@Override
	public String getAnalyticalRuleClass()
	{
		return "Value";
	}

	/**
	 * Replace triple values and apply another rule to the replaced triple.
	 * 
	 * @param srcPath
	 *          where the values to be replaced are coming from, <code>null</code>
	 *          means the source triple
	 * @param rule
	 *          rule that will be applied to the a triple with the replaced value
	 * @param map
	 * 			value map
	 *          
	 */
	@AnnoCultor.XConverter( include = true, affix = "default" )
	public ReplaceValuesRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			PropertyRule rule,
			XConverterFactory.MapObjectToObject... map)
	{
		super(rule);
		this.setSourcePath(srcPath);
		this.originalValues = new String[map.length];
		this.replacements = new String[map.length];
		
		int i = 0;
		for (MapObjectToObject m : map) {
			this.originalValues[i] = (String)m.getSrcValue();
			this.replacements[i] = (String)m.getDstValue();
			i ++;
		}
	}

	public ReplaceValuesRule(
			String[] originalValues, 
			String[] replacements, 
			Path path, 
			PropertyRule... rules)
	{
		super(rules);
		setSourcePath(path);
		this.originalValues = originalValues;
		this.replacements = replacements;
	}
	/**
	 * @inheritDoc
	 * 
	 */
	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		Triple newTriple = triple.copy();
		if (getSourcePath() != null)
			newTriple = newTriple.changeValue(new XmlValue(dataObject.getFirstValue(getSourcePath()).getValue()));

		for (int i = 0; i < originalValues.length; i++)
		{
			if (originalValues[i].equalsIgnoreCase(newTriple.getValue().getValue()))
			{
				newTriple = newTriple.changeValue(new XmlValue(replacements[i]));
				break;
			}
		}
		if (newTriple.getValue() != null)
			super.fire(newTriple, dataObject);
	}

}

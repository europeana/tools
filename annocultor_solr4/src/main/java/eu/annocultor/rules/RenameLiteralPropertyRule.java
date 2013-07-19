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
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Property;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.Graph;

/**
 * Stores the value of an XML element or attribute as a literal RDF triple.
 * 
 * @author Borys Omelayenko
 */
public class RenameLiteralPropertyRule extends AbstractRenamePropertyRule
{
	private String enforcedLang = null;
	private String defaultLang = null;
	private Path pathToLang = null;

	/**
	 * Enforces a fixed <code>xml:lang</code>. Use it if you know the language of this 
	 * property, when it is not specified in the source XML, or may be specified incorrectly.
	 * 
	 * @param dstProperty destination property
	 * @param dstLangCode 2-letter code of the xml:lang to set in the destination property, e.g. "nl"; 
	 *    empty value keeps the original language 
	 * @param dstGraph destination graph
	 */
	@AnnoCultor.XConverter( include = true, affix = "enforceLang" )
	public RenameLiteralPropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Property dstProperty, 
			String dstLangCode, 
			Graph dstGraph)
	{
		this(dstProperty, dstLangCode, dstGraph);
		this.setSourcePath(srcPath);
	}

	/**
	 * Preserves the <code>xml:lang</code> attribute.
	 * 
	 * @param dstProperty destination property
	 * @param dstGraph destination graph
	 */
	@AnnoCultor.XConverter( include = true, affix = "default" )
	public RenameLiteralPropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Property dstProperty, 
			Graph dstGraph)
	{
		this(dstProperty, null, dstGraph);
		this.setSourcePath(srcPath);
	}

	@AnnoCultor.XConverter( include = true, affix = "template" )
	public RenameLiteralPropertyRule(Graph dstGraph)
	{
		super(null, dstGraph);
	}

	public RenameLiteralPropertyRule(Property propertyName, String enforcedLang, Graph graph)
	{
		this(propertyName, graph);
		this.enforcedLang = enforcedLang;
	}

	/**
	 * @param propertyName
	 * @param defaultLang
	 * @param srcPathToLang
	 * @param target
	 */
	@AnnoCultor.XConverter( include = true, affix = "lang" )
	public RenameLiteralPropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty,
			Path srcPathToLang,
			String defaultLang,
			Graph dstGraph)
	{
		this(srcPath, dstProperty, dstGraph);
		this.defaultLang = defaultLang;
		this.pathToLang = srcPathToLang;
	}

	private RenameLiteralPropertyRule(Property propertyName, Graph target)
	{
		super(propertyName, target);
		if (propertyName == null)
			throw new RuntimeException("NULL propertyName");
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		Triple t = renameProperty(triple);
		if (t.getValue() instanceof LiteralValue) 
		{
			LiteralValue value = (LiteralValue)t.getValue();
			if (enforcedLang == null)
			{
				// dynamic lang
				String dynamicLang = pathToLang == null ? null : convertLanguageToIsoCode(getLangValue(dataObject));
				if (value.getLang() == null)
				{					
					if (dynamicLang != null)
					{
						// use dynamic
						t = t.changeValue(new LiteralValue(value.getValue(), dynamicLang));
					}
					else if (defaultLang == null || defaultLang.equals(NULL))
					{
						// keep the xml:lang intact
					}
					else
					{
						// use default from the rule
						t = t.changeValue(new LiteralValue(value.getValue(), defaultLang));
					}
				}
			}
			else
			{
				// enforce default for the rule
				t = t.changeValue(new LiteralValue(value.getValue(), enforcedLang));
			}
			super.fire(t, dataObject);
		} 
		else 
		{
			throw new Exception(this.getClass().getName() + " is applied to a non-literal value");
		}

	}
	
	private String getLangValue(DataObject dataObject) throws Exception {
		XmlValue langValue = dataObject.getFirstValue(pathToLang);
		return langValue == null ? null : langValue.getValue();
	}

	protected String convertLanguageToIsoCode(String language)
	throws Exception {
		return language;
	}

	protected Triple renameProperty(Triple triple) throws Exception {
		return triple.changeProperty(getTargetPropertyName());
	}

}

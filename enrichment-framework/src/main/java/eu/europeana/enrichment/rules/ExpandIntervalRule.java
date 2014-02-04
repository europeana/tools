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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.annotations.AnnoCultor;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.Value;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Stores the value of an XML element or attribute as a literal RDF triple.
 * 
 * @author Borys Omelayenko
 */
public class ExpandIntervalRule extends AbstractRenamePropertyRule
{
	Logger log = LoggerFactory.getLogger(getClass().getName());

	protected Path srcPathBegin;
	protected Path srcPathEnd;

	@AnnoCultor.XConverter( include = true, affix = "default" )
	public ExpandIntervalRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			@AnnoCultor.XConverter.sourceXMLPath Path srcPathBegin, 
			@AnnoCultor.XConverter.sourceXMLPath Path srcPathEnd, 
			Property dstProperty, 
			Graph dstGraph)
	{
		super(dstProperty, dstGraph);
		this.setSourcePath(srcPath);
		this.srcPathBegin = srcPathBegin;
		this.srcPathEnd = srcPathEnd;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		try {
			int yearBegin = getYear(srcPathBegin, dataObject);
			int yearEnd = getYear(srcPathEnd, dataObject);
			for (int year = yearBegin; year <= yearEnd; year++) {
				super.fire(triple.changePropertyAndValue(getTargetPropertyName(), new LiteralValue("" + year)), dataObject);
			}
		} catch (Exception e) {
			log.warn(e.getMessage() + " on " + dataObject);
		}
	}
	
	Pattern yearPattern = Pattern.compile("^(-?\\d+)(-(\\d\\d)-(\\d\\d))?$");
	
	public int getYear(Path path, DataObject dataObject) throws Exception {
		Value value = dataObject.getFirstValue(path);
		if (value == null) {
			throw new Exception("NULL value of path " + path + ", dataobject " + dataObject);
		}
		return getYear(value.getValue().trim());
	}
	
	public int getYear(String value) throws Exception {
		Matcher m = yearPattern.matcher(value);
		if (m.matches()) {
			return Integer.parseInt(m.group(1));
		}
		throw new Exception("Failed to interpret start/end date '" + value + "'");
	}
	
}

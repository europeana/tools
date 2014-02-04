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

import eu.europeana.enrichment.annotations.AnnoCultor;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Creates a constant property value. Used for types, repository names, and
 * other statements that are applied to all records of a dataset.
 * 
 * @author Borys Omelayenko
 * 
 */
public class CreateLiteralPropertyRule extends RenameLiteralPropertyRule {
	private String value;
	private String lang;

	@AnnoCultor.XConverter(include = true, affix = "default")
	public CreateLiteralPropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty, String dstValue, Graph dstGraph) {
		this(srcPath, dstProperty, dstValue, null, dstGraph);
	}

	@AnnoCultor.XConverter(include = true, affix = "lang")
	public CreateLiteralPropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty, String dstValue, String dstLang,
			Graph dstGraph) {
		super(dstProperty, dstLang, dstGraph);
		this.value = dstValue;
		this.lang = dstLang;
		setSourcePath(srcPath);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void fire(Triple triple, DataObject converter) throws Exception {
		super.fire(triple.changeValue(new LiteralValue(value, lang)), converter);
	}
}

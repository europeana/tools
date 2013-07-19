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
import eu.annocultor.triple.Property;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.Graph;

/**
 * Stores the value of an XML element or attribute as a literal RDF triple.
 * 
 * @author Borys Omelayenko
 */
public class CopyLiteralPropertyRule extends RenameLiteralPropertyRule
{

	/**
	 * Copies to a literal property, keeping property name intact.
	 * 
	 */
	@AnnoCultor.XConverter( include = true, affix = "default" )
	public CopyLiteralPropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Property dstProperty, 
			Graph dstGraph)
	{
		super(srcPath, dstProperty, dstGraph);
	}
	
	protected Triple renameProperty(Triple triple) throws Exception {
		return triple;
	}

}

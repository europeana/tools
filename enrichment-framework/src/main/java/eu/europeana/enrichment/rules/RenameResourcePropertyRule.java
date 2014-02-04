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
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Stores the value of an XML element or attribute as a resource RDF triple.
 *  
 * @author Borys Omelayenko
 * 
 */
public class RenameResourcePropertyRule extends AbstractRenamePropertyRule
{
	private Namespace namespaceForValues = null;

	/**
	 * Prefixes the value with a namespace. Should be used when
	 * the values identify RDF resources with local identifiers, 
	 * that need to be prefixed with a namespace to
	 * form legal URIs.
	 * 
	 * @param dstProperty destination property
	 * @param dstNamespace namespace to prefix value URIs
	 * @param dstGraph destination graph
	 */
	@AnnoCultor.XConverter( include = true, affix = "prefix" )
	public RenameResourcePropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Property dstProperty, 
			Namespace dstNamespace, 
			Graph dstGraph)
	{
		super(dstProperty, dstGraph);
		this.setSourcePath(srcPath);
		this.namespaceForValues = dstNamespace;
		if (dstProperty == null)
			throw new RuntimeException("NULL propertyName");
	}

	/**
	 * Keeps the original value in the destination RDF triple.
	 * Should be used when the values store full URI of the objects.
	 * 
	 * @param dstProperty destination property
	 * @param dstNamespace namespace to prefix value URIs
	 * @param dstGraph destination graph
	 */
	@AnnoCultor.XConverter( include = true, affix = "verbatim" )
	public RenameResourcePropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Property dstProperty, 
			Graph dstGraph)
	{
		super(dstProperty, dstGraph);
		this.setSourcePath(srcPath);
		this.namespaceForValues = null;
		if (dstProperty == null)
			throw new RuntimeException("NULL propertyName");
	}

	public RenameResourcePropertyRule(
			Property dstProperty, 
			Namespace dstNamespace, 
			Graph dstGraph)
	{
		super(dstProperty, dstGraph);
		this.namespaceForValues = dstNamespace;
		if (dstProperty == null)
			throw new RuntimeException("NULL propertyName");
	}

	@Override
	public void fire(Triple triple, DataObject converter) throws Exception
	{
		Triple t =
			triple.changePropertyAndValue(
					getTargetPropertyName(), 
					new ResourceValue(
							namespaceForValues == null ? "" : namespaceForValues.getUri(),
									triple.getValue().getValue()
					)
			);

		super.fire(t, converter);
	}

}

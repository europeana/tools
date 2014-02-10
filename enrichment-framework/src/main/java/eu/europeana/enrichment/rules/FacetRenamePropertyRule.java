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

import java.util.Map;

import eu.europeana.enrichment.annotations.AnnoCultor;
import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.api.Rule;
import eu.europeana.enrichment.common.Language.Lang;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;
import eu.europeana.enrichment.xconverter.api.PropertyRule;


/**
 * Links to a value with a property, which name depends on the value of another property.
 * 
 * @author Borys Omelayenko
 * 
 */
public class FacetRenamePropertyRule extends AbstractBranchRule
{
	private Map<String, Property> facetToProperty = null;

	private Path valuePath;

	private Path subjectProperty;
	private Namespace subjectPrefix;

	private String targetValuePrefix;

	private Lang lang;

	private Map<String, String> facetToLang;

	private Path langProperty;

	private Graph graph;

	private Namespace namespaceForGeneratedPropertyNames = null;

	/**
	 * Creates a link to a value with a property, which name depends on another
	 * value. Should be applied on a property that has the facet code, mentioned
	 * in the <code>facetToProperty</code> objectRule.
	 * 
	 * @param facetToProperty
	 *          objectRule <code>(facet code, target property name)</code>,
	 *          <code>null</code> forces the target properties to be generated
	 *          from the source properties.
	 * @param subjectPath
	 *          if not null then should point to the property where the subject
	 *          should be taken from. Put INTERNAL.ParentId there to link to the
	 *          parent.
	 * @param targetValuePrefix
	 *          typically a namespace prefix for resource target values
	 * @param targetPropertyType
	 *          <code>true</code> if the target value is a literal
	 */
	public FacetRenamePropertyRule(
			Map<String, Property> facetToProperty,
			Path subjectPath,
			Namespace subjectPrefix,
			Path valuePath,
			String targetValuePrefix,
			Lang lang,
			Graph graph,
			PropertyRule success,
			PropertyRule failure)
	{
		super(success, failure);
		this.subjectProperty = subjectPath;
		this.subjectPrefix = subjectPrefix;
		this.facetToProperty = facetToProperty;
		this.valuePath = valuePath;
		this.targetValuePrefix = targetValuePrefix;
		this.lang = lang;
		this.graph = graph;
		this.facetToLang = null;
	}

	/**
	 * Target properties are not mapped in the converter but generated from the
	 * value of the facet property; see
	 * {@link #FacetRenamePropertyRule(Map, Path, Namespace, Path, String, boolean, Lang, Graph, Rule, Rule, Environment)}
	 * 
	 * @param facetToProperty
	 * @param subjectProperty
	 * @param subjectPrefix
	 * @param valueProperty
	 * @param targetValuePrefix
	 * @param targetPropertyType
	 * @param lang
	 * @param graph
	 * @param success
	 * @param failure
	 * @param env
	 */
	public FacetRenamePropertyRule(
			Namespace namespaceForGeneratedPropertyNames,
			Path subjectProperty,
			Namespace subjectPrefix,
			Path valueProperty,
			String targetValuePrefix,
			Lang lang,
			Graph graph,
			PropertyRule success,
			PropertyRule failure)
	{
		super(success, failure);
		this.namespaceForGeneratedPropertyNames = namespaceForGeneratedPropertyNames;
		this.subjectProperty = subjectProperty;
		this.subjectPrefix = subjectPrefix;
		this.valuePath = valueProperty;
		this.targetValuePrefix = targetValuePrefix;
		this.lang = lang;
		this.graph = graph;
		this.facetToLang = null;
	}

	/**
	 * Uses variable language of the resulting property, pulled from another
	 * property.
	 * 
	 * @param langProperty
	 *          property of the same record where the local language code is
	 * @param facetToLang
	 *          objectRule (local language code, XML language code)
	 */
	public FacetRenamePropertyRule(
			Map<String, Property> facetToProperty,
			Path subjectProperty,
			Namespace subjectPrefix,
			Path valueProperty,
			String targetValuePrefix,
			Map<String, String> facetToLang,
			Path langProperty,
			Graph graph,
			PropertyRule success,
			PropertyRule failure)
	{
		super(success, failure);
		this.subjectProperty = subjectProperty;
		this.subjectPrefix = subjectPrefix;
		this.facetToProperty = facetToProperty;
		this.valuePath = valueProperty;
		this.targetValuePrefix = targetValuePrefix;
		this.lang = null;
		this.graph = graph;
		this.facetToLang = null;
		this.facetToLang = facetToLang;
		this.langProperty = langProperty;
	}

	/**
	 * Creates a resource link to a value with a property, which name depends on 
	 * the value of the <code>srcPath</code> (the value to which this rule is applied). 
	 * A valid RDF property URI, suitable for XML/RDF serialization, is generated from this value,
	 * and prefixed with the <code>dstNamespaceProperties</code>.
	 * 
	 * The values are taken from the <code>valuePath</code> and prefixed
	 * with the <code>dstNamespaceValue</code>.
	 * 
	 * @param srcPath
	 * @param dstNamespaceProperties 
	 * @param dstNamespaceValues
	 * @param valuePath
	 * @param dstGraph
	 */
	@AnnoCultor.XConverter( include = true, affix = "resourceFromProperty" )
	public FacetRenamePropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Namespace dstNamespaceProperties,
			Namespace dstNamespaceValues,
			Path valuePath,
			Graph dstGraph)
	{
		super(null, null);
		setSourcePath(srcPath);
		this.namespaceForGeneratedPropertyNames = dstNamespaceProperties;
		this.targetValuePrefix = dstNamespaceValues.getUri();
		this.valuePath = valuePath;
		this.lang = null;
		this.graph = dstGraph;
		this.facetToLang = null;
	}


	public Path getValuePath() {
		return valuePath;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{

		Property facetTargetProperty;
		if (facetToProperty == null)
		{
			// generate target properties from the sources
			facetTargetProperty =
					new Property(Common.generateNameBasedUri(namespaceForGeneratedPropertyNames, triple.getValue().getValue()));
		}
		else
		{
			facetTargetProperty = facetToProperty.get(triple.getValue());
		}

		if (facetTargetProperty != null)
		{
			// if this facet is present in this record
			ResourceValue value = null;
			if (valuePath == null)
			{
				value = new ResourceValue(triple.getSubject());
			}
			else
			{
				String firstValue = dataObject.getFirstValue(valuePath).getValue();
				value = firstValue == null ? null : new ResourceValue(targetValuePrefix, firstValue);
			}

			if (value != null)
			{

				Triple t =
						triple
								.changePropertyAndValue(facetTargetProperty, value)
								.changeRule(this);

				if (subjectProperty != null)
					graph.add(t.changeSubject(subjectPrefix + dataObject.getFirstValue(subjectProperty).getValue()));
				else
					graph.add(t);

				if (success != null)
					success.fire(triple.changeValue(value), dataObject);
				return;
			}
		}

		if (failure != null)
			failure.fire(triple, dataObject);
	}
}

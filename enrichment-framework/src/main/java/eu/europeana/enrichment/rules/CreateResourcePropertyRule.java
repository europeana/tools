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
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.context.Concepts.Concept;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;
import eu.europeana.enrichment.xconverter.api.Resource;

/**
 * Creates a triple with a constant property-value pair. Used to attach
 * statements about resources that do not depend on input data. Typically used
 * to assign
 * <ul>
 * <li>types for generated objects, e.g. <code>rdf:type=ns:Document</code></li>
 * <li>repository names, e.g. <code>skos:inScheme=ns:InstitutionX</code></li>
 * <li>etc.</li>
 * </ul>
 * 
 * @author Borys Omelayenko
 * 
 */
public class CreateResourcePropertyRule extends RenameResourcePropertyRule {
	private ResourceValue value;
	private String lastSubject;

	@AnnoCultor.XConverter(include = true, affix = "default")
	public CreateResourcePropertyRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty, Resource dstResource, Graph dstGraph) {
		this(dstProperty, dstResource.toString(), dstGraph);
		this.setSourcePath(srcPath);
	}

	public CreateResourcePropertyRule(Property targetPropertyName,
			String value, Graph target) {
		super(targetPropertyName, Namespaces.EMPTY_NS, target);
		this.value = new ResourceValue(value);
	}

	public CreateResourcePropertyRule(Property targetPropertyName,
			Concept value, Graph targetImpl) {
		super(targetPropertyName, Namespaces.EMPTY_NS, targetImpl);
		this.value = new ResourceValue(value.getUri());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void fire(Triple triple, DataObject converter) throws Exception {
		if (!triple.getSubject().equals(lastSubject)) {
			super.fire(triple.changeValue(value), converter);
			lastSubject = triple.getSubject();
		}
	}
}

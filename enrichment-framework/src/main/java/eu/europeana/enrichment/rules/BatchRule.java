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

import java.util.List;

import eu.europeana.enrichment.annotations.AnnoCultor;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.PropertyRule;
import eu.europeana.enrichment.xconverter.impl.XConverterFactory;
import eu.europeana.enrichment.xconverter.impl.XConverterFactory.MapObjectToObject;


/**
 * May convert multiple properties keeping them in the same terminology as in
 * XML. Incompatible with AnnoCultor 1.3.
 * 
 * @author Borys Omelayenko
 * 
 */
public class BatchRule extends SequenceRule
{
	public static final Property BATCH = new Property("http://annocultor.sourceforge.net/none");

	@Override
	public String getAnalyticalRuleClass()
	{
		return "Batch";
	}

	/**
	 * Generate a property from a path, using the last tag in the path
	 * 
	 * @param path
	 * @return
	 */
	private static Property generatePropertyFromPath(Path path) throws Exception
	{
		if (path.isAttributeQuery())
			throw new Exception("Failed to generate property from " + path + " (attr query)");
		return new Property(path.getLastTagExpanded());
	}
	
	/**
	 * Wrap-up rule, calls the <code>rule</code> with different source paths,
	 * destination properties are generated from the source paths.
	 * 
	 * @param rule rule being called, typically created with a <code>template</code> constructor
	 */
	@AnnoCultor.XConverter( include = true, affix = "autoPropNames" )
	public BatchRule(AbstractRenamePropertyRule rule, Namespace dstNamespace, Path... srcPath)
	throws Exception
	{
	    // to allow override in init()
	    this.dstNamespace = dstNamespace;
	    init();
		// create a batch of wrapped rules
		for (Path path : srcPath) 
		{
			Path batchPath = path;
			if (dstNamespace != null)
			{
				batchPath = Path.changeNamespace(this.dstNamespace, path);
			}
			WrappedRule wrappedRule = new WrappedRule(rule, batchPath, generatePropertyFromPath(batchPath));
			wrappedRule.setSourcePath(path);
			addChildRule(wrappedRule);
		}
	}

	Namespace dstNamespace;	
	
	public void setDstNamespace(Namespace dstNamespace) {
        this.dstNamespace = dstNamespace;
    }

    /**
	 * Wrap-up rule, calls the <code>rule</code> with different source paths,
	 * destination properties are set explicitly.
	 * 
	 * @param rule
	 */
	@AnnoCultor.XConverter( include = true, affix = "setPropNames" )
	public BatchRule(AbstractRenamePropertyRule rule, XConverterFactory.MapObjectToObject... map)
	throws Exception
	{
		// create a batch of wrapped rules
		for (MapObjectToObject m : map) 
		{
			WrappedRule wrappedRule = new WrappedRule(rule, (Path)m.getSrcValue(), (Property)m.getDstValue());
			wrappedRule.setSourcePath((Path)m.getSrcValue());
			addChildRule(wrappedRule);
		}
	}

	
	@Override
	public List<PropertyRule> getExpandedRules() 
	{
		return getChildRules();
	}


	/**
	 * Rule invocation wrapper.
	 */
	private class WrappedRule extends PropertyRule
	{

		AbstractRenamePropertyRule rule;
		Property dstProperty;
				
		public WrappedRule(
				AbstractRenamePropertyRule rule, 
				Path srcPath,
				Property dstProperty) 
		{
			this.rule = rule;
			this.dstProperty = dstProperty;
			setSourcePath(srcPath);
		}

		@Override
		public void fire(Triple triple, DataObject dataObject) throws Exception 
		{
			// reusing the same rule with different property names
			if (dstProperty == null)
				throw new NullPointerException("Error: destination property name should have been assigned in batch rule");
			rule.setTargetPropertyName(dstProperty);
			rule.setObjectRule(getObjectRule());
			rule.setSourcePath(getSourcePath());
			rule.fire(triple, dataObject);
		}

		@Override
		public String getAnalyticalRuleClass() 
		{
			return "Wrapped " + rule.getAnalyticalRuleClass();
		}
	}
}

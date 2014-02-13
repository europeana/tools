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

import java.util.ArrayList;
import java.util.List;

import eu.europeana.enrichment.annotations.AnnoCultor;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.Value;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.PropertyRule;

/**
 * Applies a sequence of rules to the value of some other path. It is typically
 * used together with other branching rules, when the condition of branching is
 * specified on one path, while the other path needs to be processed.
 * 
 * For example, consider a record with two paths:
 * <ul>
 * <li> <code>public</code> taking values <code>true</code> and
 * <code>false</code></li>
 * <li> <code>name</code> with person names.</li>
 * </ul>
 * 
 * We want to convert public names only. Then, a branching rule may be placed on
 * path <code>public</code>. This rule invokes another rule that should rename
 * <code>name</code> if <code>public=true</code>. However, the current value is
 * <code>true</code> or <code>false</code>, and not the name. This rule is need
 * to get hold on the <code>name</code>, being invoked from <code>public</code>.
 * 
 * 
 * @author Borys Omelayenko
 * 
 */
public class UseOtherPathValueRule extends SequenceRule {
	Path propertyName;

	@Override
	public String getAnalyticalRuleClass() {
		return "Value";
	}

	/**
	 * 
	 * @param srcValuePath
	 *            property which value would be taken and fed to the rules
	 * @param rule
	 *            rules that would be applied to this value
	 */
	@AnnoCultor.XConverter(include = true, affix = "default")
	public UseOtherPathValueRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Path srcValuePath, PropertyRule... rule) {
		super(rule);
		this.propertyName = srcValuePath;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception {
		List<Value> values = new ArrayList<Value>();

		// current property is treated separately to avoid issues with multiple
		// occurrences of property
		if (propertyName == null)
			values.add(triple.getValue());
		else
			values.addAll(dataObject.getValues(propertyName));

		for (Value value : values) {
			super.fire(triple.changeValue(value), dataObject);
		}
	}

}

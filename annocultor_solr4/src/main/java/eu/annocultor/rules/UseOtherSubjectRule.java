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
import eu.annocultor.context.Namespace;
import eu.annocultor.path.Path;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.Value;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.PropertyRule;

/**
 * Applies a sequence of rules to the subject from some other path.
 * It is typically used to create new RDF objects made of some paths.
 * 
 * For example, consider a record with two paths:
 * <ul>
 *  <li> <code>name</code> with person names.</li>
 * </ul> 
 * 
 * We want to convert public names only. Then, a branching rule may be placed on path <code>public</code>.
 * This rule invokes another rule that should rename <code>name</code> if <code>public=true</code>.
 * However, the current value is <code>true</code> or <code>false</code>, and not the name. 
 * This rule is need to get hold on the <code>name</code>, being invoked from <code>public</code>.
 * 
 * 
 * @author Borys Omelayenko
 * 
 */

public class UseOtherSubjectRule extends SequenceRule
{
	Path propertyName;
	Namespace namespace;

	@Override
	public String getAnalyticalRuleClass()
	{
		return "Value";
	}

	/**
	 * 
	 * @param srcSubjectPath path which value would be taken and fed to the rules
	 * @param rule rules that would be applied to this value
	 */
	@AnnoCultor.XConverter( include = true, affix = "default" )
	public UseOtherSubjectRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Path srcSubjectPath,
			PropertyRule... rule)
	{
		super(rule);
		this.propertyName = srcSubjectPath;
	}

	@AnnoCultor.XConverter( include = true, affix = "prefix" )
	public UseOtherSubjectRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath, 
			Path srcSubjectPath,
			Namespace dstNamespace, 
			PropertyRule... rule)
	{
		super(rule);
		this.propertyName = srcSubjectPath;
		this.namespace = dstNamespace;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		for (Value value : dataObject.getValues(propertyName))
		{
			String subject = value.getValue();
			if (namespace != null) {
				subject = namespace.getUri() + subject;
			}
			super.fire(triple.changeSubject(subject), dataObject);
		}
	}

}

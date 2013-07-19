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

import eu.annocultor.path.Path;
import eu.annocultor.tagger.vocabularies.Vocabulary;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.PropertyRule;

/**
 * Invokes different writers depending if a value was found in a vocabulary.
 * 
 * @author Borys Omelayenko
 * 
 */
public class BranchOnTermInVocabularyRule extends AbstractBranchRule
{

	Path propertyName;

	Vocabulary vocabulary;

	/**
	 * If the value of <code>propertyName</code> of this object occurs in the
	 * vocabulary. Note that there may be multiple occurrences of this
	 * <code>propertyName</code> and their values would be merged.
	 * 
	 * @param pattern
	 * @param pathToEvaluate
	 *          path which value should be evaluated. Use <code>null</code> if
	 *          you want to use the current triple instead of naming it to avoid
	 *          errors with multiple occurrences of this property in the same
	 *          record.
	 * @param success
	 * @param failure
	 */
	public BranchOnTermInVocabularyRule(
			Vocabulary vocabulary,
			Path pathToEvaluate,
			PropertyRule success,
			PropertyRule failure)
	{
		super(success, failure);
		this.propertyName = pathToEvaluate;
		this.vocabulary = vocabulary;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		getTask().getReporter().reportRuleInvocation(this, triple, dataObject);

		// String value = null;
		// current property is treated separately to avoid issues with multiple
		// occurrences of property
		// if (propertyName == null)
		// value = triple.getValue();
		// else
		// value = converter.getFirstValue(propertyName);

		throw new Exception("FIX");
		/*
		 * if (value != null && vocabulary.getTermCodeByLabel(value, null) != null)
		 * { if (success != null) { success.write(rdf, triple, converter); return; }
		 * } else { if (failure != null) { failure.write(rdf, triple, converter);
		 * return; } }
		 */
	}
}

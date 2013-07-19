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
package eu.annocultor.reports;

import java.io.IOException;

import eu.annocultor.api.ConverterTester;
import eu.annocultor.api.Reporter;
import eu.annocultor.api.Rule;
import eu.annocultor.api.Task;
import eu.annocultor.tagger.rules.VocabularyMatchResult;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.DataObject;

/**
 * A dummy reporter that does not do anything.
 * 
 * @author Borys Omelayenko
 * 
 */
public class DummyReporter extends AbstractReporter implements Reporter
{

	public DummyReporter() {
		super(null, null);
	}

	@Override
	public void addTask(Task task) {
		// do nothing, we are dummy here
		
	}

	@Override
	public void addTestReport(ConverterTester tester) throws Exception {
		// do nothing, we are dummy here
		
	}

	@Override
	public void inc(Rule rule, String categoryOrContextProperty,
			String subCategoryOrVocabulary, String counterName, String term,
			long incrementOffset) throws Exception {
		// do nothing, we are dummy here
		
	}

	@Override
	public void incTotals(Rule rule, String categoryName, String counterName)
			throws Exception {
		// do nothing, we are dummy here
		
	}

	@Override
	public void log(String msg) throws IOException {
		// do nothing, we are dummy here
		
	}

	@Override
	public String printTermOccurranceMatrix() {
		// do nothing, we are dummy here
		return null;
	}

	@Override
	public void report() throws Exception {
		// do nothing, we are dummy here
		
	}

	@Override
	public void reportLookupRuleInvocation(Rule rule, String label,
			String code, VocabularyMatchResult result)
			throws Exception {
		// do nothing, we are dummy here
		
	}

	@Override
	public void reportRuleInstantiation(Rule rule) {
		// do nothing, we are dummy here
		
	}

	@Override
	public void reportRuleInvocation(Rule rule, Triple triple, DataObject dataObject) {
		// do nothing, we are dummy here
		
	}

	@Override
	public void setSortCounters(boolean sortCounters) {
		// do nothing, we are dummy here
		
	}

}

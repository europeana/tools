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
package eu.annocultor.objects;

import junit.framework.TestCase;

import org.junit.Test;

import eu.annocultor.TestEnvironment;
import eu.annocultor.api.Factory;
import eu.annocultor.api.ObjectRule;
import eu.annocultor.api.Task;
import eu.annocultor.context.Namespaces;
import eu.annocultor.path.Path;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.xconverter.api.DataObject;

/**
 * Should stay in this package, otherwise AllTests would not pick its descendants up.
 * 
 *
 */
public class TestRulesSetup extends TestCase
{
	public Task task;

	public ObjectRule objectRule;

	public DataObject dataObject;

	public TestNamedGraph trg = new TestNamedGraph();

	public TestNamedGraph trgWorks = new TestNamedGraph();
	public TestNamedGraph trgTerms = new TestNamedGraph();
	public TestNamedGraph trgLinks = new TestNamedGraph();

	
	@Override
	protected void setUp() throws Exception {
		task = Factory.makeTask("bibliopolis", "terms", "Bibliopolis, KB", Namespaces.DC, new TestEnvironment());
		objectRule = ObjectRuleImpl.makeObjectRule(task, new Path(""), new Path(""), new Path(""), null, false);
		dataObject = new TestDataObject(task);
		super.setUp();
	}

	@Test
	public void testNothing() {
		
	}
}
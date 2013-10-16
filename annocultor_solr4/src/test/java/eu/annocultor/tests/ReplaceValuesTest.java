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
package eu.annocultor.tests;

import eu.annocultor.TestEnvironment;
import eu.annocultor.api.Factory;
import eu.annocultor.api.Task;
import eu.annocultor.context.Concepts;
import eu.annocultor.context.Namespaces;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.path.Path;
import eu.annocultor.rules.RenameLiteralPropertyRule;
import eu.annocultor.rules.ReplaceValuesRule;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.XmlValue;

public class ReplaceValuesTest extends TestRulesSetup
{
	Task task ;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		dataObject.addValue(new Path("id"), new XmlValue("1"));
		dataObject.addValue(new Path("dc:language"), new XmlValue("fr"));
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));
		dataObject.addValue(new Path("dc:type[@xml:lang='fr']"), new XmlValue("typeFr"));
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));

		task = Factory.makeTask("tst", "images", "Images from Test", Namespaces.DC, new TestEnvironment());

		super.setUp();
	}

	public void testUnicode1() throws Exception
	{
		ReplaceValuesRule rule = new ReplaceValuesRule(
				new String[] { "\u00EB" }, 
				new String[] { "e" }, 
				null, 
				new RenameLiteralPropertyRule(Concepts.DC.CONTRIBUTOR, null, trg));

		rule.setTask(task);
		rule.fire(new Triple("http://1", Concepts.DC.TYPE, new XmlValue("\u00EB", "en"), null), dataObject);

		assertEquals("Wrong result size", 1, trg.size());
		assertEquals("Failed to replace", "e", trg.getTriples().get(0).getValue().getValue());
	}

	public void testUnicode2() throws Exception
	{
		ReplaceValuesRule rule = new ReplaceValuesRule(
				new String[]  { "W" }, 
				new String[] { "e" }, 
				null, 
				new RenameLiteralPropertyRule(Concepts.DC.CONTRIBUTOR, null, trg));
		rule.setTask(task);

		rule.fire(new Triple("http://1", Concepts.DC.TYPE, new XmlValue("W", "en"), null), dataObject);
		assertEquals("Wrong result size", 1, trg.size());
		assertEquals("Failed to replace", "e", trg.getTriples().get(0).getValue().getValue());
		assertNotSame("Failed to replace", "W", trg.getTriples().get(0).getValue().getValue());
	}

	public void testUnicode3() throws Exception
	{
		ReplaceValuesRule rule = new ReplaceValuesRule(
				new String[] { "Wlki" }, 
				new String[] { "e" }, 
				null, 
				new RenameLiteralPropertyRule(Concepts.DC.CONTRIBUTOR, null, trg));
		rule.setTask(task);

		rule.fire(new Triple("http://1", Concepts.DC.TYPE, new XmlValue("Wlki", "en"), null), dataObject);

		assertEquals("Wrong result size", 1, trg.size());
		assertEquals("Failed to replace", "e", trg.getTriples().get(0).getValue().getValue());
	}

	public void testUnicode4() throws Exception
	{
		ReplaceValuesRule rule = new ReplaceValuesRule(
				new String[] { "Wlki" }, 
				new String[] { "e" }, 
				null, 
				new RenameLiteralPropertyRule(Concepts.DC.CONTRIBUTOR, null, trg));
		rule.setTask(task);

		rule.fire(new Triple("http://1", Concepts.DC.TYPE, new XmlValue("elki", "en"), null), dataObject);

		assertEquals("Wrong result size", 1, trg.size());
		assertEquals("Failed to replace", "elki", trg.getTriples().get(0).getValue().getValue());
		assertNotSame("Failed to replace", "e", trg.getTriples().get(0).getValue().getValue());
	}

	public void testUnicode5() throws Exception
	{
		ReplaceValuesRule rule = new ReplaceValuesRule(
				new String[] { "monographie imprimWe" }, 
				new String[] { "X" }, 
				null, 
				new RenameLiteralPropertyRule(Concepts.DC.CONTRIBUTOR, null, trg));
		rule.setTask(task);

		rule.fire(
				new Triple("http://1", Concepts.DC.TYPE, new XmlValue("monographie imprimWe", "en"), null),
				dataObject);

		assertEquals("Wrong result size", 1, trg.size());
		assertEquals("Failed to replace", "X", trg.getTriples().get(0).getValue().getValue());
	}
}
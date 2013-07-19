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

import eu.annocultor.context.Concepts;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.path.Path;
import eu.annocultor.rules.RenameLiteralPropertyRule;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.XmlValue;

public class RenameLiteralPropertyTest extends TestRulesSetup
{

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		dataObject.addValue(new Path("dc:language"), new XmlValue("fr"));
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));
		dataObject.addValue(new Path("dc:type[@xml:lang='fr']"), new XmlValue("typeFr"));
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));
		
	}

	public void testLang() throws Exception
	{
		RenameLiteralPropertyRule rule = new RenameLiteralPropertyRule(new Path("dc:date"), Concepts.DC.TYPE, null, "fr", trgWorks);
		rule.setTask(task);
		
		rule.fire(new Triple("http://1", Concepts.DC.TYPE, new XmlValue("tag-value", "en"), null), null);

		assertEquals("Wrong result size", 1, trgWorks.size());

		assertEquals("Wrong language", "en", ((LiteralValue)trgWorks.getTriples().get(0).getValue()).getLang());

	}

}
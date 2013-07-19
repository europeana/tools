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
import eu.annocultor.context.Namespaces;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.path.Path;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.rules.SequenceRule;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.Value;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;

public class ObjectRuleTest extends TestRulesSetup
{

	private static final XmlValue RULE_WAS_NOT_EXECUTED = new XmlValue("!!!Rule was not executed!!!");

	private static final XmlValue TYPE_EN = new XmlValue("typeEn");

	private static final XmlValue TYPE_FR = new XmlValue("typeFr");

	private static final XmlValue PARFUMER = new XmlValue("Parfumer");
	private static final XmlValue RES = new XmlValue("Resss");
	private static final XmlValue RES2 = new XmlValue("Resss2");
	private static final XmlValue R3 = new XmlValue("Resss25346");
	
	Value value = null;

	public Value fireSingleRule(Path path) throws Exception
	{
		value = RULE_WAS_NOT_EXECUTED;

		Task task = Factory.makeTask("tst", "images", "Images from Test", Namespaces.DC, new TestEnvironment());

		objectRule =
				ObjectRuleImpl.makeObjectRule(task,
						new Path("adlibXML/recordList/record"),
						new Path("adlibXML/recordList/record/priref"),
						new Path("adlibXML/recordList/record/priref"),
						null,
						false);

		dataObject.addValue(new Path("adlibXML/recordList/record/dc:title[@rdf:resource='" + RES + "']"), PARFUMER);
		dataObject.addValue(new Path("adlibXML/recordList/record/dc:parent[@rdf:resource='" + RES2 + "' and @dc:try='" + R3 + "']"), (XmlValue)null);
		//dataObject.addValue(new Path("adlibXML/recordList/record/dc:title"), RES);
		dataObject.addValue(new Path("adlibXML/recordList/record/dc:language"), new XmlValue("fr"));
		dataObject.addValue(new Path("adlibXML/recordList/record/dc:language[@lang='ua' and @type='langsource']/dc:source"), new XmlValue("src"));
		dataObject.addValue(new Path("adlibXML/recordList/record/dc:type[@xml:lang='en']"), TYPE_EN);
		dataObject.addValue(new Path("adlibXML/recordList/record/dc:type[@xml:lang='fr']"), TYPE_FR);
		dataObject.addValue(new Path("adlibXML/recordList/record/dc:type[@type='en']"), TYPE_EN);

		objectRule.addRelRule(path, new SequenceRule()
		{

			@Override
			public void fire(Triple triple, DataObject data) throws Exception
			{
				super.fire(triple, data);
				value = triple.getValue();
			}

		});
		
		objectRule.processDataObject("http://1", dataObject);
		return value;
	}

	public void testBasic() throws Exception
	{
		assertNotNull("Rule failed to fire", fireSingleRule(new Path("dc:title")));
	}

	public void testAttrLastInPath() throws Exception
	{
		assertEquals(PARFUMER, fireSingleRule(new Path("dc:title")));
		assertEquals(new XmlValue("en"), fireSingleRule(new Path("dc:type[@type]")));
		assertEquals(RULE_WAS_NOT_EXECUTED, fireSingleRule(new Path("dc:title[@dc:try]")));
		assertEquals(R3, fireSingleRule(new Path("dc:parent[@dc:try]")));
		assertEquals(RES, fireSingleRule(new Path("dc:title[@rdf:resource]")));
		assertEquals(RES2, fireSingleRule(new Path("dc:parent[@rdf:resource]")));

		// TODO: fall back to EN
		//assertEquals(TYPE_EN, fireSingleRule(new Path(DC.TYPE + "")));

	}

	public void testAttrNoLastInPath() throws Exception
	{
		assertEquals(new XmlValue("src"), fireSingleRule(new Path("dc:language/dc:source")));
		assertEquals(new XmlValue("langsource"), fireSingleRule(new Path("dc:language[@type]")));
		assertEquals(new XmlValue("ua"), fireSingleRule(new Path("dc:language[@lang]")));
	}

}
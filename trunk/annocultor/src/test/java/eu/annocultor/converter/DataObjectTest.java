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
package eu.annocultor.converter;

import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.junit.Ignore;

import eu.annocultor.TestEnvironment;
import eu.annocultor.api.ObjectRule;
import eu.annocultor.api.Task;
import eu.annocultor.context.Concepts;
import eu.annocultor.context.Namespaces;
import eu.annocultor.converter.ConverterHandler;
import eu.annocultor.converter.CoreFactory;
import eu.annocultor.path.Path;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;

public class DataObjectTest extends TestCase
{

	public static DataObject makeDataObject()
	{
		return new DataObjectImpl(null, null, null);
	}

	@Override
	protected void setUp() throws Exception
	{
	}

	public void testToString1() throws Exception
	{
		DataObject dataObject = makeDataObject();
		dataObject.addValue(new Path("dc:language"), new XmlValue("fr"));
		assertEquals(Concepts.DC.LANGUAGE + "=[fr]\n", dataObject.toString());
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));
	}

	public void testToString2() throws Exception
	{
		DataObject dataObject = makeDataObject();
		dataObject.addValue(new Path("dc:language"), new XmlValue("fr"));
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));
		String[] lns = dataObject.toString().split("\n");
		assertEquals(2, lns.length);
		Arrays.sort(lns);
		assertEquals(Concepts.DC.LANGUAGE + "=[fr]", lns[0]);
		assertEquals(Concepts.DC.TYPE + "[@" + Namespaces.XML + "lang='en']=[typeEn]", lns[1]);
	}

	public void testChildrent() throws Exception
	{
		DataObject parent = makeDataObject();
		DataObject son = new DataObjectImpl(null, null, parent);
		DataObject grandson = new DataObjectImpl(null, null, son);

		// get direct children
		assertEquals(1, parent.getChildren().size());
		assertEquals(1, son.getChildren().size());
		assertEquals(0, grandson.getChildren().size());

		//get all children 
		assertEquals(2, parent.findAllChildren().size());
		assertEquals(1, son.findAllChildren().size());
		assertEquals(0, grandson.findAllChildren().size());
	}

	public void testGeo() throws Exception
	{
		DataObject dataObject = makeDataObject();
		Namespaces namespaces = new Namespaces();
		namespaces.addNamespace("http://www.geonames.org/ontology#", "g");
		dataObject.addValue(
				new Path("fileset/file[@name='/Users/borys/europeana/vocabularies/geonames/input_source/EU/MC.rdf']" 
						+ "/rdf:RDF/g:Feature[@rdf:about='http://sws.geonames.org/2993457/']" 
						+ "/g:alternateName[@xml:lang='hsb']", namespaces), 
						new XmlValue("Monaco")
		);
		assertEquals(
				"fileset*file[@name='/Users/borys/europeana/vocabularies/geonames/input_source/EU/MC.rdf']"
				+"*http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF*http://www.geonames.org/ontology#Feature"
				+"[@http://www.w3.org/1999/02/22-rdf-syntax-ns#about='http://sws.geonames.org/2993457/']"
				+"*http://www.geonames.org/ontology#alternateName[@http://www.w3.org/XML/1998/namespacelang='hsb']=[Monaco]\n", dataObject.toString());

		// quote test
		Path p1 = new Path("rdf:RDF/g:Feature[@rdf:about=\"http://sws.geonames.org/2992741/\"]/g:name[@xml:lang=\"hsb\"]", namespaces);

		Path p2 = new Path("rdf:RDF/g:Feature[@rdf:about='http://sws.geonames.org/2992741/']/g:name[@xml:lang='hsb']", namespaces);
		assertEquals(p1.getPath(), p2.getPath());

		dataObject.addValue(p1,new XmlValue("Monaco"));
		assertEquals("Monaco", dataObject.getFirstValue(p1).getValue());

		dataObject.addValue(
				new Path("fileset/file[@name='/Users/borys/europeana/vocabularies/geonames/input_source/EU/MC.rdf']" 
						+ "/rdf:RDF/g:Feature[@rdf:about='http://sws.geonames.org/2993457/']" 
						+ "/g:parentFeature[@rdf:resource='http://another.url.com']", namespaces), 
						new XmlValue("ATTR"));

	}

	public void testGeo2() throws Exception
	{
		Task task = CoreFactory.makeTask("t", "", "descr", Namespaces.DC, 
				new TestEnvironment() {

		});
		ConverterHandler handler = new ConverterHandler(task);		

		Namespaces namespaces = new Namespaces();
		namespaces.addNamespace("http://www.geonames.org/ontology#", "g");

		ObjectRule objectRule =
			ObjectRuleImpl.makeObjectRule(task,
					new Path("/rdf:RDF/g:Feature", namespaces),
					new Path("/rdf:RDF/g:Feature[@rdf:about]", namespaces),
					new Path("adlibXML/recordList/record/priref"),
					null,
					true);

		runSaxParser(handler, "/MC.rdf");
	}

	@Ignore
	public static void runSaxParser(ConverterHandler handler, String fileFromClasspath) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);

		// Create the builder and parse the file
		SAXParser newSAXParser = factory.newSAXParser();
		if (newSAXParser == null)
		{
			throw new Exception("null SAX parser");
		}
		final InputStream fileFromClasspathAsStream = DataObjectTest.class.getResourceAsStream(fileFromClasspath);
		if (fileFromClasspathAsStream == null) {
			throw new Exception("Resource not found on classpath: " + fileFromClasspath);
		}
		newSAXParser.parse(fileFromClasspathAsStream, handler);

	}
}

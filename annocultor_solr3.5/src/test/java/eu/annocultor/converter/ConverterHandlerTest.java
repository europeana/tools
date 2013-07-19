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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import eu.annocultor.api.ObjectRule;
import eu.annocultor.context.Namespaces;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.path.Path;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.rules.RenameLiteralPropertyRule;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Property;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.DataObject;

public class ConverterHandlerTest extends TestRulesSetup
{

	public void testDocumentLang() throws Exception
	{
		makeTestDocumentLang(new ConverterHandler(task) {

			@Override
			public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
				super.startElement(namespaceURI, localName, qName, atts);
			}
		}
		);
	}

	void makeTestDocumentLang(ConverterHandler handler) throws Exception {

		Namespaces namespaces = new Namespaces();
		namespaces.addNamespace("http://www.w3.org/2004/02/skos/core#", "skos");

		ObjectRule objectRule =
			ObjectRuleImpl.makeObjectRule(task,
					new Path("/rdf:RDF/rdf:Description", namespaces),
					new Path("/rdf:RDF/rdf:Description[@rdf:about]", namespaces),
					new Path("/rdf:RDF/rdf:Description[@rdf:about]"),
					null,
					true);

		objectRule.addRule(
				new RenameLiteralPropertyRule(new Path("/rdf:RDF/rdf:Description/rdf:prefLabel"), new Property(new Path("dc:xxx")), trg) {

					@Override
					public void fire(Triple triple, DataObject dataObject) throws Exception {
						super.fire(triple, dataObject);
					}

				});

		DataObjectTest.runSaxParser(handler, "/gmt.rdf");

		//assertEquals(Lang.fr, handler.documentWideLang);

		assertEquals("Wrong result size", 2, trg.size());

		assertEquals("Wrong language", "fr", ((LiteralValue)trg.getTriples().get(0).getValue()).getLang());

		assertEquals("Wrong language", "en", ((LiteralValue)trg.getTriples().get(1).getValue()).getLang());

		trg.getTriples();
	}
}

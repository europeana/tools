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
package eu.annocultor.xconverter.impl;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

import eu.annocultor.TestEnvironment;
import eu.annocultor.common.Helper;
import eu.annocultor.context.Concepts;
import eu.annocultor.context.Namespaces;

public class XmlApiConverterGeneratorTest extends TestCase
{

	public String getXConverterPropertyValue(Repository r, String className, String instanceName, String... propertyName)
	throws Exception
	{
		ValueFactory vf = r.getValueFactory();		
		if (r.getConnection().hasStatement(
				vf.createURI(Namespaces.ANNOCULTOR_CONVERTER.getUri() + instanceName),
				vf.createURI(Concepts.RDF.TYPE.getUri()),
				vf.createURI(Namespaces.ANNOCULTOR_CONVERTER.getUri() + className),
				true))
		{		
			String query = 	
				"PREFIX anno:   <" + Namespaces.ANNOCULTOR_CONVERTER.getUri() + "> \n" +
				"PREFIX ac:   <" + Namespaces.ANNOCULTOR_CONVERTER.getUri() + "> \n" +
				"SELECT ?value  WHERE  { \n "; 
			for (int i = 0; i < propertyName.length; i++)
			{
				String p = propertyName[i];
				String left = "ERROR";
				String right = "ERROR";
				if (i == 0)
					left = "anno:" + instanceName;
				else
					left = "?var" + (i-1);

				if (i == propertyName.length - 1)						
					right = " ?value";
				else
					right = "?var" + i + ".";							

				query += left + " ac:" + p + " " + right + "\n";
			}
			query += "}";

			TupleQueryResult qr = r.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();

			if (!qr.hasNext())
				return null;

			return qr.next().getBinding("value").getValue().stringValue();
		}
		else
		{
			throw new Exception("Empty converter");
		}
	}

	public void testProfileAsRDF() throws Exception
	{
		Repository r = Helper.createLocalRepository();
		r.getConnection().add(
				new File(XmlApiConverterGeneratorTest.class.getResource("/xconverter/profile-test-europeanaWithAnnoCultor.xml").getFile()),
				Namespaces.ANNOCULTOR_CONVERTER.getUri(), RDFFormat.RDFXML);
		assertEquals("KB", getXConverterPropertyValue(r, "Profile", "europeanaWithAnnocultor", "institution"));
		assertEquals("aatned.rdf", getXConverterPropertyValue(r, "VocabularyOfTerms", "terms", "file"));
		assertNotNull(getXConverterPropertyValue(r, "Repository", "beeldbank", "datasources"));
		assertEquals("Works", getXConverterPropertyValue(r, "RdfGraph", "Works", "comment"));
	}


	public void testSample1() throws Exception
	{

		StringOutputStream out = new StringOutputStream();
		OutputStream javaOutputStream = new StringOutputStream();
		int result = 
			Converter.run(
					new File(XmlApiConverterGeneratorTest.class.getResource("/xconverter/profile-test-europeanaWithAnnoCultor.xml").getFile()),
					new TestEnvironment().getTmpDir(),
					new PrintWriter(out),
					javaOutputStream);
		// tests on source code
		String javaCode = javaOutputStream.toString();
		//	assertTrue("Error in Velocity DOM on getFirstChild (expected return null on absent child?)", 
		//			javaCode.contains("factory.makeNamespace(\"inm\"),"));

		String testOutput = out.toString();

		// tests on execution result
		if (result != 0)
		{
			System.out.println(testOutput);
			fail("Result code " + result);
		}

		checkIfRuleInvoked(testOutput);
	}

	private void checkIfRuleInvoked(String testOutput) throws Exception {
		if (!testOutput.contains("Starting generated converter")) {
			System.out.println(testOutput);
			throw new Exception("Expected output");
		}
		if (!testOutput.contains("title rule invoked")) {
			System.out.println(testOutput);
			throw new Exception("Title rule never invoked"); 
		}
	}

	public void testVocabularyFromSparql() throws Exception
	{

		StringOutputStream out = new StringOutputStream();
		OutputStream javaOutputStream = new StringOutputStream();
		int result = 
			Converter.run(
					new File(XmlApiConverterGeneratorTest.class.getResource("/xconverter/profile-test-sparqlVocabularySource.xml").getFile()),
					new TestEnvironment().getTmpDir(),
					new PrintWriter(out),
					javaOutputStream);
		// tests on source code
		String javaCode = javaOutputStream.toString();
		//	assertTrue("Error in Velocity DOM on getFirstChild (expected return null on absent child?)", 
		//			javaCode.contains("factory.makeNamespace(\"inm\"),"));

		String testOutput = out.toString();

		// tests on execution result
		if (result != 0)
		{
			System.out.println(testOutput);
			fail("Result code " + result);
		}

		checkIfRuleInvoked(testOutput);
	}

}

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

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;

import eu.annocultor.TestEnvironment;
import eu.annocultor.common.Helper;
import eu.annocultor.context.Concepts.SKOS;

/**
 * Tests meant to check if build and classpath is ok.
 * 
 * @author Borys Omelayenko
 * 
 */
public class BuildTest extends TestCase
{

	public void testSesame() throws Exception
	{
		Repository rdf = Helper.createLocalRepository();
		Helper.importRDFXMLFile(
				rdf, 
				"http://localhost/namespace", 
				new File(new TestEnvironment().getVocabularyDir()	+ "/demos/collections/tutorial/voc/vocabulary.rdf"));
		TupleQueryResult result =	rdf.getConnection().prepareTupleQuery(
				QueryLanguage.SPARQL,
				"SELECT ?C ?L WHERE {?C <" + SKOS.LABEL_PREFERRED + "> ?L}"
		).evaluate();
		List<String> bindingNames = result.getBindingNames();
		assertEquals(2, bindingNames.size());
	}
}

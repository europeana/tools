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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.annocultor.TestEnvironment;
import eu.annocultor.api.Task;
import eu.annocultor.common.Language.Lang;
import eu.annocultor.context.Concepts.SKOS;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.path.Path;
import eu.annocultor.tagger.rules.TermCreator;
import eu.annocultor.tagger.rules.TermCreatorInt.LabelCaseOption;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.triple.ResourceValue;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.Graph;

public class TermCreatorTest extends TestRulesSetup
{

	private VocabularyOfTerms v = new VocabularyOfTerms("testvoc", Lang.nl);
	private VocabularyOfPlaces places = new VocabularyOfPlaces("testplaces", Lang.nl);
	private List<VocabularyOfPlaces> placesList = new ArrayList<VocabularyOfPlaces>();
	{
		placesList.add(places);
	}


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		v.loadTermsSeRQL("SELECT C, P, L FROM {C} <"
				+ SKOS.LABEL_PREFERRED
				+ "> {L};"
				+ "[<"
				+ SKOS.BROADER
				+ "> {P}] "
				+ "WHERE lang(L) LIKE \"nl\"", 
				new TestEnvironment().getTmpDir(), 
				new TestEnvironment().getVocabularyDir(),
		"/demos/collections/tutorial/voc/vocabulary.rdf");
		places.loadTermsSeRQL("SELECT C, P, L FROM {C} <"
				+ SKOS.LABEL_PREFERRED
				+ "> {L};"
				+ "[<"
				+ SKOS.BROADER
				+ "> {P}] "
				+ "WHERE lang(L) LIKE \"nl\"", 
				new TestEnvironment().getTmpDir(), 
				new TestEnvironment().getVocabularyDir(),
		"/demos/collections/tutorial/voc/vocabulary.rdf");

		dataObject.addValue(new Path("dc:language"), new XmlValue("fr"));
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));
		dataObject.addValue(new Path("dc:type[@xml:lang='fr']"), new XmlValue("typeFr"));
		dataObject.addValue(new Path("dc:type[@xml:lang='en']"), new XmlValue("typeEn"));

		super.setUp();
	}

	final String GenerateTermNs = "http://test/";
	final String vocabularyName1 = "testVoc";
	final String vocabularyName2 = "testVoc2";
	final String Subject = "http://records/1";
	final String ObjectValue = "Amsterdam";
	final String externalTermAmsterdam = "http://external/Amsterdam";
	final String externalTermBrussels = "http://external/Brussels";

	private void makeTest(
			TermCreator tc,
			int graphSize,
			int resultSize,
			String externalTerm,
			String vocName,
			Task task) 
	throws Exception, URISyntaxException, MalformedURLException
	{
		tc.writeLinkTermToVocabulary(
				GenerateTermNs + ObjectValue, 
				new Term("Amsterdam", 
						Lang.en, 
						new CodeURI(externalTerm),	
						vocName
				), 
				null, Lang.en, "test", new TestEnvironment(), dataObject, null);

		// new target
		assertEquals("new graphs: count", graphSize, task.getGraphs().size());

		// getting the last graph
		Graph graph = null;
		Iterator<Graph> iterator = task.getGraphs().iterator();
		for (int i = 0; i < graphSize; i++)
		{
			graph = iterator.next();
		}

		assertEquals("new graphs: name", "bibliopolis_terms.map.test." + vocName, graph.getId());

		// single map
		assertEquals("Wrong result size", resultSize, graph.size());
		assertEquals(new Triple(GenerateTermNs + ObjectValue,
				SKOS.EXACT_MATCH,
				new ResourceValue(externalTerm),
				null).toString(), 
				graph.getLastAddedTriple(0).toString());
	}

	public void testBasic() throws Exception
	{
		TermCreator tc = new TermCreator(GenerateTermNs, LabelCaseOption.KEEP_ORIGINAL_CASE);
		tc.setTask(task);

		makeTest(tc, 1, 1, externalTermAmsterdam, vocabularyName1, task);

		// second run should not change the situation
		makeTest(tc, 1, 1, externalTermAmsterdam, vocabularyName1, task);
		{
			Graph graph = task.getGraphs().iterator().next();
			assertEquals("Wrong result size", 1, graph.size());
		}
		// another run with a different term should change
		tc.writeLinkTermToVocabulary(GenerateTermNs + ObjectValue, 
				new Term("Brussels",
						Lang.en,
						new CodeURI(externalTermBrussels),
						vocabularyName1
				), null, Lang.en, "test", new TestEnvironment(), dataObject, null);

		assertEquals("new graphs: count", 1, task.getGraphs().size());

		Graph graph = task.getGraphs().iterator().next();

		// second map
		assertEquals("Wrong result size", 2, graph.size());
		assertEquals(new Triple(GenerateTermNs + ObjectValue,
				SKOS.EXACT_MATCH,
				new ResourceValue(externalTermBrussels),
				null).toString(), graph.getLastAddedTriple(0).toString());

		// another run with different vocabulary - new target is created
		makeTest(tc, 2, 1, externalTermBrussels, vocabularyName2, task);
	}

	public void testSchemeTrailing() throws Exception
	{
		TermCreator tc = new TermCreator("http://my.com/ns/", LabelCaseOption.KEEP_ORIGINAL_CASE);
		assertEquals("http://my.com/ns", tc.getScheme());
	}
}

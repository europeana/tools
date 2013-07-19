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

import java.util.ArrayList;
import java.util.List;

import eu.annocultor.TestEnvironment;
import eu.annocultor.common.Language.Lang;
import eu.annocultor.context.Concepts;
import eu.annocultor.context.Namespaces;
import eu.annocultor.context.Concepts.RDF;
import eu.annocultor.context.Concepts.SKOS;
import eu.annocultor.context.Concepts.VRA;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.path.Path;
import eu.annocultor.tagger.rules.AbstractLookupRule;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.ResourceValue;
import eu.annocultor.triple.Triple;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.Graph;

public class AbstractLookupRuleTest extends TestRulesSetup
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
	final String Subject = "http://records/1";
	final String Amsterdam = "Amsterdam";
	final String Brussels = "Brussels";

	public void testBasicNoVocabulary() throws Exception
	{
		final String vocabularyName = "testVoc";
		// this rule prefixes all terms with a namespace and does no real lookup
		AbstractLookupRule rule =
			new AbstractLookupRule(
					VRA.CREATOR,
					VRA.CREATOR_ATTRIBUTED,
					RDF.COMMENT,
					Lang.en,
					"test2",
					",",
					trgLinks,
					trgLinks,
					null, 
					new AbstractLookupRule.ProxyTermDefinition(Namespaces.DCTERMS, trgTerms, Lang.en))
		{

			@Override
			public String getAnalyticalRuleClass()
			{
				return "test";
			}

			@Override
			protected TermList getDisambiguatedTerms(DataObject dataObject, String termLabel, Lang termLang)
			throws Exception
			{
				TermList r = new TermList();
				r.add(new Term(termLabel, Lang.en, new CodeURI(GenerateTermNs + termLabel), vocabularyName));
				return r;
			}

            @Override
            public TermList getTermsByUri(CodeURI uri) throws Exception {
                return null;
            }

		};

		rule.setObjectRule(objectRule);
		rule.setTask(task);
		rule.setSourcePath(new Path(""));
		rule.fire(
				new Triple(Subject, Concepts.DC.TYPE, new XmlValue(Amsterdam, "en"), null), 
				dataObject);

		// local terms
		assertEquals("Wrong result size", 3, trgTerms.size());
		assertEquals(
				new Triple(Namespaces.DCTERMS + Amsterdam, RDF.TYPE, new ResourceValue(SKOS.CONCEPT), rule).toString(),
		trgTerms.getTriples().get(0).toString());
		assertEquals(new Triple(Namespaces.DCTERMS + Amsterdam,
				SKOS.LABEL_PREFERRED,
				new LiteralValue(Amsterdam,	"en"),
				rule).toString(), trgTerms.getTriples().get(2).toString());

		// work-to-term link + literal property
		assertEquals("Wrong result size", 2, trgLinks.size());
		assertEquals(
				new Triple(
						Subject, 
						VRA.CREATOR, 
						new ResourceValue(Namespaces.DCTERMS, Amsterdam), 
						rule)
				.toString(), 
				trgLinks.getTriples().get(1).toString());

	}

	public void testPreventDoubleTermCreation() throws Exception
	{
		final String vocabularyName2 = "testVoc2";
		// this rule prefixes all terms with a namespace and does no real lookup
		AbstractLookupRule rule =
			new AbstractLookupRule(VRA.CREATOR,
					VRA.CREATOR_ATTRIBUTED,
					null,
					Lang.en,
					"test3",
					",",
					trgLinks,
					trgLinks,
					null, 
					new AbstractLookupRule.ProxyTermDefinition(Namespaces.DCTERMS, trgTerms, Lang.en))
		{

			@Override
			public String getAnalyticalRuleClass()
			{
				return "test";
			}

			@Override
			protected TermList getDisambiguatedTerms(DataObject dataObject, String termLabel, Lang termLang)
			throws Exception
			{
				TermList r = new TermList();
				r.add(new Term(termLabel, Lang.en, new CodeURI(GenerateTermNs + termLabel), vocabularyName2));
				return r;
			}

            @Override
            public TermList getTermsByUri(CodeURI uri) throws Exception {
                return null;
            }
		};

		// first time asm
		rule.setObjectRule(objectRule);
		rule.setTask(task);
		rule.setSourcePath(new Path(""));
		rule.fire(new Triple(Subject, Concepts.DC.TYPE, new XmlValue(Amsterdam, "en"), null), dataObject);

		// local terms
		assertEquals("Wrong result size", 3, trgTerms.size());
		assertEquals(
				new Triple(
						Namespaces.DCTERMS + Amsterdam, 
						RDF.TYPE, 
						new ResourceValue(SKOS.CONCEPT), 
						rule)
				.toString(),
				trgTerms.getTriples().get(0).toString());
		assertEquals(
				new Triple(
						Namespaces.DCTERMS + Amsterdam,
						SKOS.LABEL_PREFERRED,
						new LiteralValue(Amsterdam, "en"),
						rule)
				.toString(), 
				trgTerms.getTriples().get(2).toString());

		// work-to-term link + literal
		assertEquals("Wrong result size", 2, trgLinks.size());
		assertEquals(
				new Triple(
						Subject, 
						VRA.CREATOR, 
						new ResourceValue(Namespaces.DCTERMS, Amsterdam), 
						rule)
				.toString(), 
				trgLinks.getTriples().get(1).toString());

		// map
		{
			// getting the last graph
			Graph graph = task.getGraphs().iterator().next();

			assertEquals("new graphs: name", "bibliopolis_terms.map.test3." + vocabularyName2, graph.getId());

			// single map
			assertEquals("Wrong result size", 1, graph.size());
			assertEquals(new Triple(Namespaces.DCTERMS + Amsterdam,
					SKOS.EXACT_MATCH,
					new ResourceValue(GenerateTermNs, Amsterdam),
					null).toString(), graph.getLastAddedTriple(0).toString());
		}

		// ---------------------------------------------------------------

		// second time ams - mostly the same situation
		rule.fire(
				new Triple(
						Subject + "difference", 
						Concepts.DC.TYPE, 
						new XmlValue(Amsterdam, "en"), 
						null),
						dataObject);

		// local terms - same
		assertEquals("Wrong result size", 3, trgTerms.size());

		// work-to-term link - plus one literal - twice
		assertEquals("Wrong result size", 4, trgLinks.size());

		// ---------------------------------------------------------------

		// second - brussels
		rule.fire(
				new Triple(
						Subject + "differencion", 
						Concepts.DC.TYPE, 
						new XmlValue(Brussels, "en"), 
						null),
						dataObject);

		// local terms - plus one
		assertEquals("Wrong result size", 6, trgTerms.size());

		// work-to-term link - plus one literal 3 times
		assertEquals("Wrong result size", 3 * 2, trgLinks.size());

		{
			// getting the last graph
			Graph graph = task.getGraphs().iterator().next();

			assertEquals("new graphs: name", "bibliopolis_terms.map.test3." + vocabularyName2, graph.getId());

			// single map
			assertEquals("Wrong result size", 2, graph.size());
			assertEquals(new Triple(Namespaces.DCTERMS + Brussels,
					SKOS.EXACT_MATCH,
					new ResourceValue(GenerateTermNs, Brussels),
					null).toString(), graph.getLastAddedTriple(0).toString());
		}
	}

}

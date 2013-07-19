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
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import eu.annocultor.TestEnvironment;
import eu.annocultor.common.Language.Lang;
import eu.annocultor.context.Concepts.SKOS;
import eu.annocultor.tagger.postprocessors.LanguageTermFilter;
import eu.annocultor.tagger.postprocessors.PeopleTermFilter;
import eu.annocultor.tagger.rules.LookupPlaceRule;
import eu.annocultor.tagger.rules.LookupTimeRule;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.DisambiguationContext;
import eu.annocultor.tagger.vocabularies.VocabularyOfPeople;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.tagger.vocabularies.VocabularyOfTime;
import eu.annocultor.tagger.vocabularies.Vocabulary.NormaliseCaller;

public class VocabularyTest extends TestCase
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
		super.setUp();
	}

	public void testLanguageDisambiguation() throws Exception
	{
		final LanguageTermFilter disambiguator = new LanguageTermFilter();
		final String ACRYL = "acrylverf";
		final String VOCABULARYNAME = "acrylverf";
		final String URL = "http://code/15";

		TermList list = new TermList();
		list.add(new Term(ACRYL, Lang.nl, new CodeURI(URL), VOCABULARYNAME));
		assertFalse(
				"Disambiguation not needed",
				disambiguator.disambiguate(
						list,  
						new DisambiguationContext(null, null, null)
				).isEmpty());
		assertFalse(
				"Disambiguation not needed",
				disambiguator.disambiguate(
						list,  
						new DisambiguationContext(Lang.en, null, null)
				).isEmpty());
		list.add(new Term(ACRYL, Lang.fr, new CodeURI(URL), VOCABULARYNAME));
		assertFalse(
				disambiguator.disambiguate(
						list,  
						new DisambiguationContext(null, null, null)
				).isEmpty());
		assertTrue(
				disambiguator.disambiguate(
						list,  
						new DisambiguationContext(Lang.en, null, null)
				).isEmpty());
	}

	public void testIntegratedLanguageDisambiguation() throws Exception
	{
		final String ACRYL = "acrylverf";
		v.addDisambiguator(new LanguageTermFilter());

		assertFalse(v.lookupTerm(ACRYL, null, null).isEmpty());
		assertFalse(v.lookupTerm(ACRYL, Lang.nl, null).isEmpty());


		TermList lookupTerm = v.lookupTerm(ACRYL, Lang.fr, null);
		assertTrue(lookupTerm.toString(), lookupTerm.size() == 1);
	}

	/**
	 * If two codes have the same label, they both should be considered for
	 * disambiguation.
	 * 
	 * @throws Exception
	 */
	public void testMergeLabelsForDisambig() throws Exception
	{
		TermList tlist = places.findByLabel("Amsterdam", DisambiguationContext.NO_DISAMBIGUATION);
		assertNotNull(tlist);
		assertFalse(tlist.isEmpty());

		TermList t1 = LookupPlaceRule.getDisambiguatedTerms(placesList, null, "Amsterdam", Lang.nl, null, null);
		assertEquals("Returned ambiguous result", 2, t1.size());

		List<CodeURI> listEurope = new ArrayList<CodeURI>();
		listEurope.add(new CodeURI("http://e-culture.multimedian.nl/ns/tutorial#Europe"));
		TermList t2 =
			LookupPlaceRule.getDisambiguatedTerms(placesList, null, "Amsterdam", Lang.nl, listEurope, null);
		assertEquals("Expected single result", 1, t2.size());
		assertEquals("http://e-culture.multimedian.nl/ns/tutorial#Amsterdam", t2.iterator().next().getCode());
		//assertEquals("http://e-culture.multimedian.nl/ns/tutorial#Amsterdam", t.get(0).getCode());
	}


	@Test
	public void testNormalizeTimeLabel() throws Exception {
	
		VocabularyOfTime vot = new VocabularyOfTime("test-time", null);
		assertEquals("moyen age", vot.onNormaliseLabel("Moyen Ãge", null));
		assertEquals("moyen age", vot.onNormaliseLabel("Moyen Age", null));
	}
	
	/*
	public void testOnNormalizeLabel() throws Exception
	{
		VocabularyOfPeople voc = new VocabularyOfPeople("textVocPeople", Lang.en)
		{

			@Override
			public String onNormaliseLabel(String label, NormaliseCaller caller)
					throws Exception {
				return label + "XXX";
			}


		};
		// test
		try 
		{
			if (!"jan vermeer".equals(voc.onNormaliseLabel("vermeer, Jan", NormaliseCaller.query)))
			{
				throw new RuntimeException("change vocabulary label of load failed");
			}
		} 
		catch (Exception e) 
		{
			throw new RuntimeException("change vocabulary label of load failed: normaliser");			
		}

	}
	 */
}

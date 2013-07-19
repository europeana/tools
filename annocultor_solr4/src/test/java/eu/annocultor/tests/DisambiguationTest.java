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

import org.junit.Assert;

import eu.annocultor.common.Language.Lang;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.vocabularies.DisambiguationContext;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;

public class DisambiguationTest extends TestRulesSetup
{
	private static final String A_POPULATION = "population";
	private static final String PARIS = "paris";
	private static final String ZUID_HOLLAND = "zuid-holland";

	public void testBasic() throws Exception
	{
		VocabularyOfPlaces vocabulary = new VocabularyOfPlaces("places", null);
		vocabulary.putTerm(new Term(ZUID_HOLLAND, Lang.nl, new CodeURI("http://zh"), "test"));
		vocabulary.putTerm(new Term(ZUID_HOLLAND, Lang.de, new CodeURI("http://zh"), "test"));
		vocabulary.putTerm(new Term("south-holland", Lang.en, new CodeURI("http://zh"), "test"));
		vocabulary.putTerm(new Term(PARIS, null, new CodeURI("http://paris"), "test"));
	
		Assert.assertEquals(1, vocabulary.findByLabel(ZUID_HOLLAND, null).size());

		Assert.assertEquals(1, vocabulary.findByLabel(PARIS, null).size());
		Assert.assertEquals(1, vocabulary.findByLabel(PARIS, new DisambiguationContext(Lang.en, null, null)).size());

//		TermFilter disambiguator = new LanguageTermFilter();
//		TermList termList = new TermList();
//		termList.add(new Term(ZUID_HOLLAND, Lang.nl, new CodeURI("http://zh"), "test"));
//		termList.add(new Term(ZUID_HOLLAND, Lang.de, new CodeURI("http://zh"), "test"));
//		termList.add(new Term("south-holland", Lang.en, new CodeURI("http://zh"), "test"));
//		
//		TermList list = disambiguator.disambiguate(termList, null);
//		Assert.assertEquals(3, list.size());
//		Assert.assertEquals(ZUID_HOLLAND, list.getUnambigous().getLabel().toString());

	}

	public void testPopulation() throws Exception
	{
		VocabularyOfPlaces vocabulary = new VocabularyOfPlaces("places", null);
//		vocabulary.clearDisambiguators();
//		vocabulary.addDisambiguator(new PopulationTermFilter());

		Term region = new Term(PARIS, null, new CodeURI("http://region"), "test");
		vocabulary.putTerm(region);
		Term city = new Term(PARIS, null, new CodeURI("http://city"), "test");
		vocabulary.putTerm(city);
		Term paris = new Term(PARIS, null, new CodeURI("http://paris"), "test");
		vocabulary.putTerm(paris);
	
		region.setProperty(A_POPULATION, "5");
		city.setProperty(A_POPULATION, "4");
		
		Assert.assertEquals(1, vocabulary.findByLabel(PARIS, null).size());
		Assert.assertEquals("http://region", vocabulary.findByLabel(PARIS, null).getFirst().getCode());

		city.setProperty(A_POPULATION, "6");

		Assert.assertEquals(1, vocabulary.findByLabel(PARIS, null).size());
		Assert.assertEquals("http://city", vocabulary.findByLabel(PARIS, null).getFirst().getCode());
	}

}

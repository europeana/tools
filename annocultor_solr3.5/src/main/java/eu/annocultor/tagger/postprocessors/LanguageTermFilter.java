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
package eu.annocultor.tagger.postprocessors;

import eu.annocultor.common.Language.Lang;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.DisambiguationContext;


/**
 * Selects terms that match language profile.
 * 
 * @author Borys Omelayenko
 * 
 */
public class LanguageTermFilter extends TermFilter {

	@Override
	public TermList disambiguate(TermList allTerms, DisambiguationContext disambiguationContext) throws Exception {

		Lang langToFind = disambiguationContext == null ? null : disambiguationContext.getLang();
		Lang defaultLang = disambiguationContext == null ? null : disambiguationContext.getDefaultLang();

		// disambiguation not needed
		if (allTerms.size() < 2)
			return allTerms;

		TermList selectedTerms = new TermList();

		// disambiguation: select those with the label in the right language
		for (Term term : allTerms) {
			if (langToFind == null || term.getLang() == langToFind || term.getLang() == defaultLang) {
				selectedTerms.add(term);
			}
		}

		// check for empty result
		if (selectedTerms.isEmpty()) {
			return selectedTerms;
		}
		
		// check if they are all the same
		if (selectedTerms.isSameLabels() && selectedTerms.isSameCodes()) {
			TermList singleTerm = new TermList();
			singleTerm.add(selectedTerms.getFirst());
			return singleTerm;
		} 

		// no disambiguation was possible
		return allTerms;
	}

}

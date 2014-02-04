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
package eu.europeana.enrichment.converters.solr;

import java.util.HashSet;

import eu.europeana.enrichment.common.Language.Lang;
import eu.europeana.enrichment.tagger.terms.Term;
import eu.europeana.enrichment.tagger.vocabularies.Vocabulary;

/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 * 
 */
public class SolrPeopleTagger extends SolrTagger {

	public SolrPeopleTagger(Vocabulary vocabulary, String termFieldName,
			String labelFieldName, FieldRulePair... fieldRulePairs) {
		super("people", termFieldName, labelFieldName, null, null,
				fieldRulePairs);
	}

	HashSet<Lang> languagesForAltLabels = new HashSet<Lang>();
	{
		languagesForAltLabels.add(Lang.en);
		languagesForAltLabels.add(Lang.ru);
		languagesForAltLabels.add(Lang.uk);
		languagesForAltLabels.add(Lang.de);
		languagesForAltLabels.add(Lang.fr);
		languagesForAltLabels.add(Lang.nl);
		languagesForAltLabels.add(Lang.es);
		languagesForAltLabels.add(Lang.pl);
		languagesForAltLabels.add(Lang.it);
		languagesForAltLabels.add(Lang.pt);
		languagesForAltLabels.add(Lang.el);
		languagesForAltLabels.add(Lang.bg);
		languagesForAltLabels.add(Lang.sv);
		languagesForAltLabels.add(Lang.fi);
		languagesForAltLabels.add(Lang.no);
		languagesForAltLabels.add(Lang.hu);
		languagesForAltLabels.add(Lang.da);
		languagesForAltLabels.add(Lang.sk);
		languagesForAltLabels.add(Lang.sl);
		languagesForAltLabels.add(Lang.la);
		languagesForAltLabels.add(Lang.lt);
		languagesForAltLabels.add(Lang.et);
		languagesForAltLabels.add(Lang.ro);
		languagesForAltLabels.add(Lang.cs);
		languagesForAltLabels.add(Lang.zh);
		languagesForAltLabels.add(Lang.id);
	}

	@Override
	boolean shouldInclude(Term term) {
		return term.getLang() == null
				|| languagesForAltLabels.contains(term.getLang());
	}

}

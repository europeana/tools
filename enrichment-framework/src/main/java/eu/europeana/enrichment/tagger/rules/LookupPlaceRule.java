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
package eu.europeana.enrichment.tagger.rules;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.enrichment.annotations.AnnoCultor;
import eu.europeana.enrichment.common.Language.Lang;
import eu.europeana.enrichment.context.Concepts;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.tagger.terms.CodeURI;
import eu.europeana.enrichment.tagger.terms.Term;
import eu.europeana.enrichment.tagger.terms.TermList;
import eu.europeana.enrichment.tagger.vocabularies.DisambiguationContext;
import eu.europeana.enrichment.tagger.vocabularies.Vocabulary;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPlaces;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Looks places up in external vocabularies. Should be applied to a
 * <code>srcPath</code> that stores place labels.
 * 
 * @author Borys Omelayenko
 * 
 */
public class LookupPlaceRule extends AbstractLookupRule {
	public enum ContextContinents {
		Europe, America, Asia, Africa
	};

	protected static List<VocabularyOfPlaces> vocabularies = new ArrayList<VocabularyOfPlaces>();

	/**
	 * Project-specific vocabularies should be added before
	 * <code>LookupPlaceRule</code> will be used.
	 * 
	 * @param v
	 * @throws Exception
	 */
	public static void addVocabulary(VocabularyOfPlaces v) throws Exception {
		if (v == null) {
			throw new Exception("NULL vocabulary passed.");
		}

		vocabularies.add(v);
	}

	@Override
	public String getAnalyticalRuleClass() {
		return "VocabularyOfPlaces";
	}

	private DisambiguationContext country;
	private DisambiguationContext placeType;

	/**
	 * 
	 * Linking to external vocabularies is done directly, without the creation
	 * of local proxy terms.
	 */
	@AnnoCultor.XConverter(include = true, affix = "noLocalTerms")
	public LookupPlaceRule(@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty, Graph dstGraphLiterals, Graph dstGraphLinks,
			Property termsProperty, String termsSignature,
			String termsSplitPattern, VocabularyOfPlaces... termsVocabulary) {
		super(dstProperty, termsProperty, Concepts.RDF.COMMENT, null,
				termsSignature, termsSplitPattern, dstGraphLiterals,
				dstGraphLinks, null, null); // no proxy terms

		setSourcePath(srcPath);
		for (VocabularyOfPlaces vocabulary : termsVocabulary) {
			vocabularies.add(vocabulary);
		}
	}

	/**
	 * Looks triple values up in an external vocabulary.
	 * 
	 * @param linkRecordToTerm
	 *            to create if mappings found
	 * @param linkRecordToLiteral
	 *            to create if no mapping is found
	 * @param passedReportCategory
	 *            report category
	 * @param splitPattern
	 *            to split a triple <code>value</code> into terms to be mapped
	 *            separately. <code>null</code> sets the no-separation logic.
	 * @param target
	 */
	public LookupPlaceRule(Property linkRecordToTerm,
			Property linkRecordToLiteral, Property labelOfLinkTermToVocabulary,
			Lang langLabelTermToVocabulary, Namespace termsNamespace,
			DisambiguationContext country, DisambiguationContext placeType,
			String reportCategory, String splitPattern, Graph graphTerms,
			Graph linksGraph, TermCreatorInt termCreator,
			VocabularyOfPlaces... vocabulary) {
		super(linkRecordToTerm, linkRecordToLiteral,
				labelOfLinkTermToVocabulary, langLabelTermToVocabulary,
				reportCategory, splitPattern, graphTerms, linksGraph,
				termCreator, new ProxyTermDefinition(termsNamespace,
						graphTerms, null));
		this.country = country;
		this.placeType = placeType;
		for (VocabularyOfPlaces voc : vocabulary) {
			vocabularies.add(voc);
		}
	}

	@Override
	protected TermList getDisambiguatedTerms(DataObject dataObject,
			String label, Lang lang) throws Exception {
		return LookupPlaceRule.getDisambiguatedTerms(vocabularies, dataObject,
				label, lang,
				DisambiguationContext.getContextValue(country, dataObject),
				DisambiguationContext.getContextValue(placeType, dataObject));
	}

	public static TermList getDisambiguatedTerms(
			List<VocabularyOfPlaces> vocabularies, DataObject dataObject,
			String label, Lang lang, List<CodeURI> country,
			List<CodeURI> placeType) throws Exception {
		TermList result = new TermList();
		for (VocabularyOfPlaces vocabulary : vocabularies) {
			try {
				TermList place = vocabulary.lookupPlace(label, null, country);
				result.add(place);
			} catch (Exception e) {
				throw new Exception("Vocabulary lookup error on term '" + label
						+ "', vocabulary " + vocabulary.getVocabularyName()
						+ ": " + e.getMessage());
			}
		}
		return result;
	}

	@Override
	public TermList getTermsByUri(CodeURI uri) throws Exception {
		TermList terms = new TermList();
		for (Vocabulary vocabulary : vocabularies) {
			for (Term term : vocabulary.findByCode(uri)) {
				terms.add(term);
			}
		}
		return terms;
	}

}
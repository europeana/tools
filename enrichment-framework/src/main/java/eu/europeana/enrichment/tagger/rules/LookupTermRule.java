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
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Looks terms up in external vocabularies. Should be applied to a
 * <code>srcPath</code> that stores term labels.
 * 
 * @author Borys Omelayenko
 * 
 */
public class LookupTermRule extends AbstractLookupRule {

	protected static List<VocabularyOfTerms> allVocabularies = new ArrayList<VocabularyOfTerms>();

	protected List<VocabularyOfTerms> vocabularies = new ArrayList<VocabularyOfTerms>();

	/**
	 * Project-specific vocabularies should be added before
	 * <code>LookupTermRule</code> will be used.
	 * 
	 * @throws Exception
	 * 
	 */
	public static void addVocabulary(VocabularyOfTerms v) throws Exception {
		if (v == null) {
			throw new Exception("NULL vocabulary passed.");
		}
		allVocabularies.add(v);
	}

	@Override
	public String getAnalyticalRuleClass() {
		return "VocabularyOfTerms";
	}

	private DisambiguationContext parent;

	/**
	 * For each new label, a proxy local term is created and linked to. Then,
	 * this term is mapped to a term in external vocabularies.
	 * 
	 * @param termSplitPattern
	 *            regular expression that defines how to separate multiple term
	 *            labels merged into a single string. For example the following
	 *            pattern (quotes excluded) "( *; *)|( *, *)" would split terms
	 *            by semicolon or comma
	 */
	@AnnoCultor.XConverter(include = true, affix = "withLocalTerms")
	public LookupTermRule(@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty, Graph dstGraphLiterals, Graph dstGraphLinks,
			Property termsProperty, String termsSignature, Graph termsGraph,
			Namespace termsNamespace, Lang termsLang, String termsSplitPattern,
			VocabularyOfTerms termsVocabulary) {
		this(dstProperty, termsProperty, Concepts.RDF.COMMENT, null, null,
				null, termsSignature, termsSplitPattern, dstGraphLiterals,
				dstGraphLinks, null, new ProxyTermDefinition(termsNamespace,
						termsGraph, termsLang), termsVocabulary);
		setSourcePath(srcPath);
	}

	/**
	 * Linking external vocabularies is done directly, without the creation of
	 * local proxy terms.
	 */
	@AnnoCultor.XConverter(include = true, affix = "noLocalTerms")
	public LookupTermRule(@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty, Graph dstGraphLiterals, Graph dstGraphLinks,
			Property termsProperty, String termsSignature,
			String termsSplitPattern, VocabularyOfTerms termsVocabulary) {
		this(dstProperty, termsProperty, Concepts.RDF.COMMENT, null, null,
				null, termsSignature, termsSplitPattern, dstGraphLiterals,
				dstGraphLinks, null, null, // no proxy terms
				termsVocabulary);
		setSourcePath(srcPath);
	}

	/**
	 * Looks triple values up in an external vocabulary.
	 * 
	 * @param langTermLabel
	 *            a non-null value enforces lang on pref labels of the generated
	 *            terms.
	 * @param linkRecordToTerm
	 *            to create if mappings found
	 * @param linkRecordToLiteral
	 *            to create if no mapping is found
	 * @param splitPattern
	 *            to split a triple <code>value</code> into terms to be mapped
	 *            separately. <code>null</code> sets the no-separation logic.
	 * @param graphTerms
	 *            graph for generated terms with local ids
	 * @param graphLinksRecordToTerm
	 *            graph for links between records (often works) and terms with
	 *            local ids
	 */
	public LookupTermRule(Property linkRecordToTerm,
			Property linkRecordToLiteral, Property labelOfLinkTermToVocabulary,
			Lang langLabelTermToVocabulary, DisambiguationContext labels,
			DisambiguationContext parent, String reportCategory,
			String splitPattern, Graph dstGraphLiterals, Graph dstGraphLinks,
			TermCreatorInt termCreator,
			ProxyTermDefinition proxyTermDefinition,
			VocabularyOfTerms... localVocabulary) {
		super(linkRecordToTerm, linkRecordToLiteral,
				labelOfLinkTermToVocabulary, langLabelTermToVocabulary,
				reportCategory, splitPattern, dstGraphLiterals, dstGraphLinks,
				termCreator, proxyTermDefinition);
		// if localVocabularies are provided then we only search them
		for (VocabularyOfTerms vocabulary : localVocabulary) {
			vocabularies.add(vocabulary);
		}
		if (vocabularies.isEmpty()) {
			vocabularies.addAll(allVocabularies);
		}
		this.parent = parent;
	}

	@Override
	protected TermList getDisambiguatedTerms(DataObject converter,
			String label, Lang lang) throws Exception {
		// TODO:change to database
		return new MongoDatabaseUtils().findByLabel(label, "concept");
		//
		//
		// for (VocabularyOfTerms vocabulary : vocabularies)
		// {
		// try
		// {
		// TermList term = vocabulary.lookupTerm(label, null, null);
		// result.add(term);
		// }
		// catch (Exception e)
		// {
		// throw new Exception("Vocabulary lookup error on term '"
		// + label
		// + "', vocabulary '"
		// + vocabulary.getVocabularyName()
		// + "', original message: "
		// + e.getMessage());
		// }
		// }
		// return result;
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
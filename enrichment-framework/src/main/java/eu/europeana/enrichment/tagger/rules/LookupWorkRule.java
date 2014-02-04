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

import eu.europeana.enrichment.common.Language.Lang;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.tagger.terms.CodeURI;
import eu.europeana.enrichment.tagger.terms.Term;
import eu.europeana.enrichment.tagger.terms.TermList;
import eu.europeana.enrichment.tagger.vocabularies.Vocabulary;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfWorks;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;


/**
 * Looks (same as) works up.
 * 
 * @author Borys Omelayenko
 * 
 */
public class LookupWorkRule extends AbstractLookupRule
{
	protected static List<VocabularyOfWorks> vocabularies = new ArrayList<VocabularyOfWorks>();

	/**
	 * Project-specific vocabularies should be added before
	 * <code>LookupWorkRule</code> will be used.
	 * 
	 * @param v
	 */
	public static void addVocabulary(VocabularyOfWorks v)
	{
		vocabularies.add(v);
	}

	@Override
	public String getAnalyticalRuleClass()
	{
		return "AllWorks";
	}

	/**
	 * Looks triple values up in an external vocabulary.
	 * 
	 * @param linkRecordToTerm
	 *          to create if mappings found
	 * @param linkRecordToLiteral
	 *          to create if no mapping is found
	 * @param passedReportCategory
	 *          report category
	 * @param mappedReportCategory
	 *          report category
	 * @param splitPattern
	 *          to split a triple <code>value</code> into terms to be mapped
	 *          separately. <code>null</code> sets the no-separation logic.
	 * @param target
	 */
	public LookupWorkRule(
			Property linkRecordToTerm,
			Property linkRecordToLiteral,
			Property labelOfLinkTermToVocabulary,
			Lang langLabelTermToVocabulary,
			Namespace termsNamespace,
			Path titlePath,
			Path creatorNamePath,
			Path datePath,
			String reportCategory,
			String splitPattern,
			Graph dstGraphLiterals,
			Graph dstGraphLinks,
			TermCreatorInt termCreator)
	{
		super(
				linkRecordToTerm,
				linkRecordToLiteral,
				labelOfLinkTermToVocabulary,
				langLabelTermToVocabulary,
				reportCategory,
				splitPattern,
				dstGraphLiterals,
				dstGraphLinks,
				termCreator, 
				new ProxyTermDefinition(termsNamespace, dstGraphLiterals, null));
	}

	@Override
	protected TermList getDisambiguatedTerms(DataObject converter, String label, Lang lang) throws Exception
	{
		TermList result = new TermList();
		for (VocabularyOfWorks vocabulary : vocabularies)
		{
			try
			{
				TermList terms = vocabulary.lookupWork(label, lang, null, null, null);
				result.add(terms);
			}
			catch (Exception e)
			{
				throw new Exception("Vocabulary lookup error on term '"
					+ label
					+ "', vocabulary "
					+ vocabulary.getVocabularyName()
					+ ": "
					+ e.getMessage());
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
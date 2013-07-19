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
package eu.annocultor.tagger.rules;

import java.util.ArrayList;
import java.util.List;

import eu.annocultor.annotations.AnnoCultor;
import eu.annocultor.api.Rule;
import eu.annocultor.common.Language.Lang;
import eu.annocultor.context.Concepts;
import eu.annocultor.path.Path;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.DisambiguationContext;
import eu.annocultor.tagger.vocabularies.Vocabulary;
import eu.annocultor.tagger.vocabularies.VocabularyOfTime;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Property;
import eu.annocultor.triple.ResourceValue;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.Graph;


/**
 * Looks (temporal) periods up in external vocabularies.
 * Should be applied to a <code>srcPath</code> that stores term labels.
 * 
 * @author Borys Omelayenko
 * 
 */
public class LookupTimeRule extends AbstractLookupRule
{

	protected static List<VocabularyOfTime> allVocabularies = new ArrayList<VocabularyOfTime>();

	protected List<VocabularyOfTime> vocabularies = new ArrayList<VocabularyOfTime>();

	/**
	 * Project-specific vocabularies should be added before
	 * <code>LookupTermRule</code> will be used.
	 * @throws Exception 
	 * 
	 */
	public static void addVocabulary(VocabularyOfTime v) throws Exception
	{
		if (v == null) {
			throw new Exception("NULL vocabulary passed.");
		}
		allVocabularies.add(v);
	}

	@Override
	public String getAnalyticalRuleClass()
	{
		return "VocabularyOfTime";
	}

	private DisambiguationContext parent;

	private static class IntervalTermCreator extends TermCreator {

		public IntervalTermCreator() {
			super(null, TermCreatorInt.LabelCaseOption.KEEP_ORIGINAL_CASE);
		}


		@Override
		public void writeLinkRecordToTerm(
				String recordUri, 
				String termUri,
				TermList terms,
				Property relationRecordToTerm, 
				Graph graphRecords, 
				Rule rule) throws Exception {

			String beginDate = terms.getFirst().getProperty("begin");
			String endDate = terms.getLast().getProperty("end");
			String periodUri = recordUri + "/" + beginDate + "_" + endDate;
			String periodLabel = terms.getFirst().getLabel() + " - " + terms.getLast().getLabel();

			TermList period = new TermList();
			period.add(new Term(periodLabel, null, new CodeURI(periodUri), ""));

			writePeriod(periodUri, terms.getFirst().getCode(), beginDate, Concepts.ANNOCULTOR.PERIOD_BEGIN, Concepts.ANNOCULTOR.DATE_BEGIN, graphRecords, rule);
			writePeriod(periodUri, terms.getLast().getCode(), endDate, Concepts.ANNOCULTOR.PERIOD_END, Concepts.ANNOCULTOR.DATE_END, graphRecords, rule);
			super.writeLinkRecordToTerm(recordUri, periodUri, period, relationRecordToTerm, graphRecords, rule);
		}

		private void writePeriod(
				String recordUri,
				String periodUri,
				String date,
				Property relationPeriod,
				Property relationDate,
				Graph graphRecords,
				Rule rule) 
		throws Exception
		{
			graphRecords.add(
					new Triple(
							recordUri, 
							relationPeriod, 
							new ResourceValue(periodUri), 
							rule));
			if (date != null) {
				graphRecords.add(
						new Triple(
								recordUri, 
								relationDate, 
								new LiteralValue(date), 
								rule));
			}
		}
	}


	/**
	 * Linking external vocabularies is done directly, without the creation of local proxy terms. 
	 * @param startPath path that identifies period start
	 * @param endPath path that identifies period end
	 * 
	 */
	@AnnoCultor.XConverter(include=true, affix = "default")
	public LookupTimeRule(
			@AnnoCultor.XConverter.sourceXMLPath Path srcPath,
			Property dstProperty,
			Graph dstGraphLiterals,
			Graph dstGraphLinks,
			Property termsProperty,
			String termsSignature,			 
			String termsSplitPattern,
			VocabularyOfTime termsVocabulary)
	{
		this(
				dstProperty,
				termsProperty,
				Concepts.RDF.COMMENT,
				null,
				null,
				null,
				termsSignature,
				termsSplitPattern,
				dstGraphLiterals,
				dstGraphLinks,
				new IntervalTermCreator(),
				null,//new ProxyTermDefinition(Namespaces.ANNOCULTOR_TIME, dstGraphLinks, null),
				termsVocabulary);
		setSourcePath(srcPath);
	}

	private LookupTimeRule(
			Property linkRecordToTerm,
			Property linkRecordToLiteral,
			Property labelOfLinkTermToVocabulary,
			Lang langLabelTermToVocabulary,
			DisambiguationContext labels,
			DisambiguationContext parent,
			String reportCategory,
			String splitPattern,
			Graph dstGraphLiterals,
			Graph dstGraphLinks,
			TermCreatorInt termCreator,
			ProxyTermDefinition proxyTermDefinition,
			VocabularyOfTime... localVocabulary)
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
				proxyTermDefinition);
		// if localVocabularies are provided then we only search them
		for (VocabularyOfTime vocabulary : localVocabulary)
		{
			vocabularies.add(vocabulary);
		}
		if (vocabularies.isEmpty())
		{
			vocabularies.addAll(allVocabularies);
		}
		this.parent = parent;
	}

	protected PairOfStrings splitToStartAndEnd(DataObject dataObject, String label, Lang lang) throws Exception {
		return new PairOfStrings(label, label);
	}

	@Override
	protected TermList getDisambiguatedTerms(DataObject converter, String label, Lang lang) throws Exception
	{
		TermList result = new TermList();
		PairOfStrings startEndStrings = splitToStartAndEnd(converter, label, lang);
		for (VocabularyOfTime vocabulary : vocabularies)
		{
			try
			{
				TermList term = vocabulary.lookupTerm(startEndStrings.getFirst(), startEndStrings.getLast(), null, null);
				result.add(term);
			}
			catch (Exception e)
			{
				throw new Exception("Vocabulary lookup error on term '"
						+ label
						+ "', vocabulary '"
						+ vocabulary.getVocabularyName()
						+ "', original message: "
						+ e.getMessage());
			}
		}
		return result;
	}

	@Override
	protected boolean checkIfAmbigous(TermList terms) {
		return terms.size() > 2;
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
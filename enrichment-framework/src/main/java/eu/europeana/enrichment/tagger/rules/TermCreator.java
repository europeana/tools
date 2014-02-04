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

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.api.Label;
import eu.europeana.enrichment.api.Rule;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.common.Language.Lang;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.context.Concepts.RDF;
import eu.europeana.enrichment.context.Concepts.SKOS;
import eu.europeana.enrichment.converter.CoreFactory;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.tagger.terms.Term;
import eu.europeana.enrichment.tagger.terms.TermList;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * 
 * Sometimes we want not only to lookup a term but create one and then try to
 * objectRule it. This is the term creator used from the lookup writer.
 * 
 * 
 * @author Borys Omelayenko
 * 
 */
public class TermCreator implements TermCreatorInt {

	private String scheme;

	public Property getPrefLabelProperty() {
		return SKOS.LABEL_PREFERRED;
	}

	public Property getAltLabelProperty() {
		return SKOS.LABEL_ALT;
	}

	public String getTermType() {
		return SKOS.CONCEPT;
	}

	protected Path labelAltPath;
	protected LabelCaseOption labelCaseOption;

	public LabelCaseOption getLabelCaseOption() {
		return labelCaseOption;
	}

	Task task;

	/**
	 * Create term from label. A <code>triple</code> subject should contain the
	 * record using the term, and the value should contain the label preferred.
	 * 
	 * 
	 * @param targetRecord
	 *            target to put references to the term
	 * @param targetTerm
	 *            target to put terms
	 * 
	 */
	public TermCreator(String scheme, Namespace termUriPrefix,
			Graph targetRecord, Graph targetTerm, Path labelAltPath,
			LabelCaseOption makeLowCaseLabels) {
		// late binding on ObjectRule.addXXXRule
		this.task = null;
		this.scheme = scheme;
		if (this.scheme != null) {
			while (this.scheme.endsWith("/") || this.scheme.endsWith("#")) {
				this.scheme = this.scheme
						.substring(0, this.scheme.length() - 1);
			}
		}
		// this.termUriPrefix = termUriPrefix;
		// this.targetTerm = targetTerm;
		// this.targetRecord = targetRecord;
		this.labelAltPath = labelAltPath;
		this.labelCaseOption = makeLowCaseLabels;
		init();
	}

	protected TermCreator(String scheme, TermCreatorInt tc) {
		this(scheme, tc.getLabelCaseOption());
	}

	public TermCreator(String scheme, LabelCaseOption makeLowCaseLabels) {
		this(scheme, Namespaces.EMPTY_NS, null, null, null, makeLowCaseLabels);
	}

	protected void init() {

	}

	public String getLabelPrefValue(String valueUsedInUri, DataObject dataObject) {
		return valueUsedInUri;
	}

	public Property getRelationTermToVocabulary() {
		return SKOS.EXACT_MATCH;
	}

	public void writeLinkRecordToTerm(String recordUri, String termUri,
			TermList terms, Property relationRecordToTerm, Graph graphRecords,
			Rule rule) throws Exception {
		graphRecords.add(new Triple(recordUri, relationRecordToTerm,
				new ResourceValue(termUri), rule, terms.getFirst().getLabel()));
	}

	public void writeTermDefinition(String termUri, String broaderUri,
			DataObject dataObject, Graph graphTerms, Rule rule,
			Label prefLabel, Label... altLabel) throws Exception {
		graphTerms.add(new Triple(termUri, RDF.TYPE, new ResourceValue(
				getTermType()), rule));

		if (scheme != null) {
			graphTerms.add(new Triple(termUri, SKOS.IN_SCHEME,
					new ResourceValue(scheme), rule));
		}
		// write pref label
		graphTerms
				.add(new Triple(
						termUri,
						getPrefLabelProperty(),
						new LiteralValue(
								(labelCaseOption == LabelCaseOption.TO_LOW_CASE) ? prefLabel
										.getLabel().toLowerCase() : prefLabel
										.getLabel(), prefLabel.getLang()), rule));

		// write alternative labels
		for (Label label : altLabel) {
			graphTerms
					.add(new Triple(
							termUri,
							getAltLabelProperty(),
							new LiteralValue(
									(labelCaseOption == LabelCaseOption.TO_LOW_CASE) ? label
											.getLabel().toLowerCase() : label
											.getLabel(), label.getLang()), rule));
		}

		// write broader
		if (broaderUri != null) {
			graphTerms.add(new Triple(termUri, SKOS.BROADER, new ResourceValue(
					broaderUri), rule));
		}

	}

	public void writeLinkTermToVocabulary(String localTermUri,
			Term externalVocabularyTerm, Property labelOfLinkTermToVocabulary,
			Lang langOfLinkTermToVocabulary, String mappingCategorySignature,
			Environment env, DataObject dataObject, Rule rule) throws Exception {
		// make target
		if (externalVocabularyTerm == null) {
			throw new NullPointerException("External vocabulary term");
		}
		if (task == null) {
			throw new NullPointerException("Task");
		}
		String newTargetId = Common.makeNewNamedGraphId(task.getDatasetId(),
				"map", mappingCategorySignature,
				externalVocabularyTerm.getVocabularyName());
		Graph newTarget = null;

		for (Graph trg : task.getGraphs()) {
			if (trg.getId().equals(newTargetId)) {
				newTarget = trg;
				break;
			}
		}
		if (newTarget == null) {
			newTarget = makeGraphLinkTermToVocabulary(externalVocabularyTerm,
					mappingCategorySignature, env, task);
			task.addGraph(newTarget);
		}

		// link term to vocabulary
		newTarget.add(new Triple(localTermUri, getRelationTermToVocabulary(),
				new ResourceValue(externalVocabularyTerm.getCode()), rule));

		// eventual label for debugging
		if (!localTermUri.equals(externalVocabularyTerm.getCode())
				&& labelOfLinkTermToVocabulary != null) {
			String label = externalVocabularyTerm.getLabel();
			if (externalVocabularyTerm.getDisambiguatingComment() != null)
				label += " ["
						+ externalVocabularyTerm.getDisambiguatingComment()
						+ "]";
			if (externalVocabularyTerm.getConfidenceComment() != null)
				label += " [WARNING! LOW CONFIDENCE IN THIS MATCH:"
						+ externalVocabularyTerm.getConfidenceComment() + "]";
			newTarget.add(new Triple(localTermUri, labelOfLinkTermToVocabulary,
					new LiteralValue(label, langOfLinkTermToVocabulary), rule));
		}
	}

	public Graph makeGraphLinkTermToVocabulary(Term externalVocabularyTerm,
			String mappingCategorySignature, Environment env, Task task) {

		return CoreFactory.makeNamedGraph(task.getDatasetId(), env, "map",
				mappingCategorySignature,
				externalVocabularyTerm.getVocabularyName(), "Mapping of "
						+ task.getDatasetId() + ", " + mappingCategorySignature
						+ " to " + externalVocabularyTerm.getVocabularyName());
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getScheme() {
		return scheme;
	}

}
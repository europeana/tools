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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.api.Label;
import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.api.Reporter;
import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.api.internal.Language.Lang;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.rules.AbstractRenamePropertyRule;
import eu.europeana.enrichment.tagger.preprocessors.LabelFilter;
import eu.europeana.enrichment.tagger.rules.TermCreatorInt.LabelCaseOption;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.Value;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Looks triple values up in an external vocabulary. If a match is found in a
 * vocabulary then it creates a resource property with the vocabulary code. If
 * no match is found then it creates a literal property that may have a
 * different name than the property with successful matches.
 * 
 * Each source triple may contain several values, each of which will be mapped
 * separately. The values are separated with a regular expression.
 * 
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class AbstractLookupRule extends AbstractRenamePropertyRule {

	Logger log = LoggerFactory.getLogger(getClass().getName());

	// split patterns
	public static final String DEFAULT_SPLIT_PATTERN = "( *; *)"; // |( *, *)";
	public static final String NO_SPLIT = null;

	private String splitPattern = DEFAULT_SPLIT_PATTERN;

	private String mappingCategorySignature;

	protected Property linkRecordToTerm;

	protected Property labelOfLinkTermToVocabulary = null;

	private TermCreatorInt termCreator = null;

	/**
	 * Constant for no term creation.
	 */
	public static TermCreatorInt NO_TERM_CREATION = null;

	private Lang langLabelTermToVocabulary;

	private static Set<String> properties = new HashSet<String>();

	protected Reporter reporter;

	private Set<String> lookedUpTermStrings = new HashSet<String>();

	private Graph graphLinksRecordToTerm;

	protected RubbishTermDetector termWarner;

	private List<LabelFilter> labelExtractors = new ArrayList<LabelFilter>();

	public void addLabelExtractor(LabelFilter labelExtractor) {
		labelExtractors.add(labelExtractor);
	}

	/**
	 * For situations when in response to a literal we need to create a term,
	 * and then (maybe) link this term to a vocabulary term.
	 */
	public static class ProxyTermDefinition {
		Namespace namespaceLocalTerms;
		Graph graphTerms;
		Lang langTermLabel;

		public ProxyTermDefinition(Namespace namespaceLocalTerms,
				Graph graphTerms, Lang langTermLabel) {
			this.namespaceLocalTerms = namespaceLocalTerms;
			this.graphTerms = graphTerms;
			this.langTermLabel = langTermLabel;
		}

	}

	ProxyTermDefinition proxyTermDefinition;

	/**
	 * Looks triple values up in an external vocabulary.
	 * 
	 * @param linkRecordToTerm
	 *            to create if mappings found
	 * @param linkRecordToLiteral
	 *            to create if no mapping is found
	 * @param splitPattern
	 *            to split a triple <code>value</code> into terms to be mapped
	 *            separately. <code>null</code> sets the no-separation logic.
	 * @param createProxyTerms
	 *            whether local terms with automatically generated URIs should
	 *            be created and linked to external vocabulary terms. or records
	 *            are linked directly to external terms.
	 */
	public AbstractLookupRule(Property linkRecordToTerm,
			Property linkRecordToLiteral, Property labelOfLinkTermToVocabulary,
			Lang langLabelTermToVocabulary, String mappingCategorySignature,
			String splitPattern, Graph dstGraphLiterals, Graph dstGraphLinks,
			TermCreatorInt termCreator, ProxyTermDefinition proxyTermDefinition) {
		super(linkRecordToLiteral, dstGraphLiterals);
		this.langLabelTermToVocabulary = langLabelTermToVocabulary;
		this.splitPattern = splitPattern;
		this.linkRecordToTerm = linkRecordToTerm;
		if (properties.contains(mappingCategorySignature))
			throw new RuntimeException("Duplicating lookup property "
					+ mappingCategorySignature);
		this.mappingCategorySignature = mappingCategorySignature;
		this.labelOfLinkTermToVocabulary = labelOfLinkTermToVocabulary;
		if (dstGraphLiterals == null)
			throw new RuntimeException(
					"null graph is not allowed - use Factory.graphToIgnore() instead");
		this.graphLinksRecordToTerm = dstGraphLinks;
		this.termWarner = new RubbishTermDetector();

		// parameters for proxy terms
		setTermCreator(termCreator == null ? new TermCreator(
				proxyTermDefinition == null ? null
						: proxyTermDefinition.namespaceLocalTerms.getUri(),
				LabelCaseOption.KEEP_ORIGINAL_CASE) : termCreator);
		this.proxyTermDefinition = proxyTermDefinition;
		if (proxyTermDefinition != null
				&& proxyTermDefinition.graphTerms == null)
			throw new RuntimeException(
					"null graph is not allowed - use Factory.graphToIgnore() instead");
	}

	protected void setTermCreator(TermCreatorInt termCreator) {
		this.termCreator = termCreator;
	}

	private boolean objectRuleIsSet = false;

	@Override
	public void setObjectRule(ObjectRule objectRule) {
		super.setObjectRule(objectRule);
		// propagate to term creator
		// late binding
		getTermCreator().setTask(objectRule.getTask());

		objectRuleIsSet = true;
	}

	/**
	 * Allows overriding to provide custom term creator.
	 * 
	 * @return
	 */
	protected TermCreatorInt getTermCreator() {
		return termCreator;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception {
		// long startfiring = new Date().getTime();
		lastMatch = null;

		if (!initialized) {
			init();
		}
		if (onPreCondition(triple, dataObject)) {
			triple = onInvocation(triple, dataObject);
			/*
			 * Copy the original literal value. Hence, all looked up terms would
			 * be a real 'enrichment' and not a replacement. Makes more sense
			 * when people would be allowed to see and comment on this
			 * enrichment.
			 */
			if (getTargetPropertyName() != null)
				super.fire(triple.changeProperty(getTargetPropertyName()),
						dataObject);

			/*
			 * Check completeness of initializaton. objectRule is not knows at
			 * creation time, but known at execution
			 */
			if (!objectRuleIsSet) {
				throw new Exception(
						"Object rule should be set prior to firing of this rule. Typically it is set in the ObjectRule.addXXX methods.");
			}

			if (getTermCreator() == null) {
				throw new NullPointerException("term creator");
			}
			if (getObjectRule() == null) {
				throw new NullPointerException("object rule");
			}
			getTermCreator().setTask(getObjectRule().getTask());

			// extract more and clean values
			List<String> labels = new ArrayList<String>();
			if (splitPattern == null) {
				labels.add(triple.getValue().getValue());
			} else {
				for (String v : triple.getValue().getValue()
						.split(splitPattern)) {
					labels.add(v);
				}
			}

			for (LabelFilter extractor : labelExtractors) {
				labels = extractor.extract(labels);
			}
			// long startElements = new Date().getTime();
			for (String element : labels) {

				String termUri = null;

				/*
				 * Write term definition & prevent double term creation
				 */
				boolean createTermDefinion = false;
				if (proxyTermDefinition != null) {

					// generate term uri
					termUri = generateTermUri(element);
					if (!lookedUpTermStrings.contains(element)) {
						lookedUpTermStrings.add(element);
						termWarner.warnOnStrangeTerms(element);
						createTermDefinion = true;
					}

					if (createTermDefinion) {
						getTermCreator().writeTermDefinition(
								termUri,
								null,
								dataObject,
								proxyTermDefinition.graphTerms,
								this,
								new Label(element,
										proxyTermDefinition.langTermLabel));
					}
				}

				/*
				 * Find the term in external vocabularies
				 */

				lastMatch = getDisambiguatedTerms(dataObject, element, null);
				if (lastMatch != null) {
					if (!checkIfMissing(lastMatch)
							&& !checkIfAmbigous(lastMatch)) {
						processLookupMatch(lastMatch, termUri,
								triple.getSubject(), dataObject,
								createTermDefinion);
					}
					reportLookup(lastMatch, element);
				}
			}
		}
	}

	TermList lastMatch;

	public TermList getLastMatch() {
		return lastMatch;
	}

	protected String generateTermUri(String element) throws Exception {
		return Common.generateNameBasedUri(
				proxyTermDefinition.namespaceLocalTerms, element);
	}

	private void reportLookup(TermList terms, String element) throws Exception {

		if (checkIfMissing(terms)) {
			// missing
			reportMissing(element);
		} else {
			// something found
			if (checkIfAmbigous(terms)) {
				// ambiguous
				reportAmbigous(element, StringUtils.join(terms.iterator(), ";"));
			} else {
				// single match
				reportMatch(terms);
			}
		}
	}

	protected boolean checkIfAmbigous(TermList terms) {
		return terms.size() > 1;
	}

	protected boolean checkIfMissing(TermList terms) {
		return terms.isEmpty();
	}

	protected void reportMissing(String label) throws Exception {
		// not found
		reporter.reportLookupRuleInvocation(this, label, null,
				VocabularyMatchResult.missed);
	}

	protected void reportAmbigous(String label, String code) throws Exception {
		// ambiguous
		reporter.reportLookupRuleInvocation(this, label, code,
				VocabularyMatchResult.ambigous);
	}

	protected void reportError(String label) throws Exception {
		// error, something went wrong on disambiguation
		reporter.reportLookupRuleInvocation(this, label, null,
				VocabularyMatchResult.error);
	}

	protected void reportMatch(TermList terms) throws Exception {
		// report matched term
		reporter.reportLookupRuleInvocation(this, terms.getFirst().getLabel(),
				terms.getFirst().getCode(), VocabularyMatchResult.matched);
	}

	protected void processLookupMatch(TermList terms, String termUri,
			String subject, DataObject dataObject, boolean createTermDefinion)
			throws Exception {
		// match
		if (createTermDefinion) {
			// writing link vocabulary to term
			getTermCreator().writeLinkTermToVocabulary(termUri,
					terms.getFirst(), labelOfLinkTermToVocabulary,
					langLabelTermToVocabulary, mappingCategorySignature,
					getTask().getEnvironment(), dataObject, this);
		}
		// writing direct link record to term
		if (proxyTermDefinition == null) {
			getTermCreator().writeLinkRecordToTerm(subject,
					terms.getFirst().getCode(), terms, linkRecordToTerm,
					graphLinksRecordToTerm, this);
		}

		// write a link to the term
		if (proxyTermDefinition != null && linkRecordToTerm != null) {
			getTermCreator().writeLinkRecordToTerm(subject, termUri, terms,
					linkRecordToTerm, graphLinksRecordToTerm, this);
		}
	}

	/**
	 * Find and disambiguates the terms for labels. Specific rules have access
	 * to contextual fields that they use to disambiguate.
	 * 
	 * @param dataObject
	 * @param termLabel
	 *            term label
	 * @param termLang
	 *            language of the term label
	 * @return
	 * @throws Exception
	 */
	protected abstract TermList getDisambiguatedTerms(DataObject dataObject,
			String termLabel, Lang termLang) throws Exception;

	/**
	 * Returns first value if the path is not null.
	 * 
	 * @param dataObject
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String getFirstValueIfPathExists(DataObject dataObject,
			Path path) throws Exception {
		if (dataObject == null) {
			return null;
		}
		Value value = dataObject.getFirstValue(path);
		if (value == null) {
			return null;
		} else {
			return value.getValue();
		}
	}

	public abstract TermList getTermsByUri(CodeURI uri) throws Exception;
}
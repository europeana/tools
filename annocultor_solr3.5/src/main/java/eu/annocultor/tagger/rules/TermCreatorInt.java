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

import eu.annocultor.api.Label;
import eu.annocultor.api.Rule;
import eu.annocultor.api.Task;
import eu.annocultor.common.Language.Lang;
import eu.annocultor.context.Environment;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.triple.Property;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.Graph;

public interface TermCreatorInt
{
	public enum LabelCaseOption
	{
		KEEP_ORIGINAL_CASE,
		TO_LOW_CASE
	};

	/**
	 * Generate term definition.
	 * 
	 */
	public abstract void writeTermDefinition(
			String termUri,
			String broaderUri,
			DataObject dataObject,
			Graph graphTerms,
			Rule rule,
			Label prefLabel,
			Label... altLabel) throws Exception;

	/**
	 * Generate link record to (local) term, typically, with a specific property,
	 * such as <code>creator</code> or <code>material</code>.
	 * 
	 */
	public abstract void writeLinkRecordToTerm(
			String recordUri,
			String termUri,
			TermList terms,
			Property relationRecordToTerm,
			Graph graphRecords,
			Rule rule) throws Exception;

	/**
	 * Generate link between (local) term and matching (external) vocabulary
	 * terms.
	 */

	public abstract void writeLinkTermToVocabulary(
			String localTermUri,
			Term externalVocabularyTerm,
			Property labelOfLinkTermToVocabulary,
			Lang langOfLinkTermToVocabulary,
			String mappingCategorySignature,
			Environment env,
			DataObject dataObject,
			Rule rule) throws Exception;

	public abstract Property getRelationTermToVocabulary();

	public abstract String getTermType();

	public abstract Property getAltLabelProperty();

	public abstract Property getPrefLabelProperty();

	public abstract LabelCaseOption getLabelCaseOption();

	public abstract void setTask(Task task);

}
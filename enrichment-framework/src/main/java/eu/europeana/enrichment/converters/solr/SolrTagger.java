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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.enrichment.converters.europeana.Entity;
import eu.europeana.enrichment.converters.europeana.Field;
import eu.europeana.enrichment.converters.fields.AgentFields;
import eu.europeana.enrichment.converters.fields.ConceptFields;
import eu.europeana.enrichment.converters.fields.PlaceFields;
import eu.europeana.enrichment.converters.fields.TimespanFields;
import eu.europeana.enrichment.tagger.rules.AbstractLookupRule;
import eu.europeana.enrichment.tagger.terms.CodeURI;
import eu.europeana.enrichment.tagger.terms.Term;
import eu.europeana.enrichment.tagger.terms.TermList;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;

/**
 * Lookup (tagging) rule.
 * 
 * @author Borys Omelayenko
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
public abstract class SolrTagger {

	public static class FieldRulePair {
		String field;
		AbstractLookupRule rule;

		public FieldRulePair(String field, AbstractLookupRule rule) {
			this.field = field;
			this.rule = rule;
		}

		public String getField() {
			return field;
		}

		public AbstractLookupRule getRule() {
			return rule;
		}

	}

	FieldRulePair[] fieldRulePairs;

	String dbtable;
	String termFieldName;

	String labelFieldName;

	Set<String> broaderLabels;
	String broaderTermFieldName;
	String broaderLabelFieldName;

	public SolrTagger(String dbtable, String termFieldName,
			String labelFieldName, String broaderTermFieldName,
			String broaderLabelFieldName, FieldRulePair... fieldRulePairs) {
		this.dbtable = dbtable;
		this.fieldRulePairs = fieldRulePairs;
		this.termFieldName = termFieldName;
		this.labelFieldName = labelFieldName;
		this.broaderTermFieldName = broaderTermFieldName;
		this.broaderLabelFieldName = broaderLabelFieldName;
	}

	void afterTermMatched(Term term) throws Exception {

	}

	void afterDocument(SolrInputDocument document) {

	}

	void beforeDocument(SolrInputDocument document) {

	}

	List<Entity> tag(SolrInputDocument document) throws Exception {
		List<Entity> entities = new ArrayList<Entity>();
		String className = "";
		if (StringUtils.equals(termFieldName, "skos_concept")) {
			className = "Concept";
			dbtable = "concept";
		} else if (StringUtils.equals(termFieldName, "edm_place")) {
			className = "Place";
			dbtable = "place";
		} else if (StringUtils.startsWith(termFieldName, "edm_timespan")) {
			className = "Timespan";
			dbtable = "period";
		} else {
			className = "Agent";
			dbtable = "people";
		}
		for (FieldRulePair frp : fieldRulePairs) {
			Collection<Object> values = document.getFieldValues(frp.getField());
			if (values != null) {
				for (Object value : values) {
					entities.addAll(findEntities(
							value.toString().toLowerCase(), frp.getField(),
							className));
				}
			}
		}

		return entities;
	}

	private List<Entity> findEntities(String value, String originalField,
			String className) throws MalformedURLException {
		List<Entity> entities = new ArrayList<Entity>();
		TermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms.size() > 0) {
			Term term = terms.getFirst();
			Entity entity = new Entity();
			if (originalField != null) {
				entity.setOriginalField(originalField);
			}
			entity.setClassName(className);

			entity.setUri(term.getCode());
			TermList relatedLabels = MongoDatabaseUtils.findByCode(new CodeURI(
					term.getCode()), dbtable);

			entity.setFields(generateFields(relatedLabels, className));
			entities.add(entity);
			if (relatedLabels.getFirst().getParent() != null) {
				entities.addAll(findParents(relatedLabels.getFirst()
						.getParent(), className));
			}
		}
		return entities;
	}

	private List<Entity> findParents(Term parent, String className)
			throws MalformedURLException {
		List<Entity> parentEntities = new ArrayList<Entity>();
		TermList parents = MongoDatabaseUtils.findByCode(
				new CodeURI(parent.getCode()), dbtable);
		Entity entity = new Entity();
		entity.setClassName(className);
		entity.setUri(parents.getFirst().getCode());
		entity.setFields(generateFields(parents, className));
		parentEntities.add(entity);
		if (parents.getFirst().getParent() != null) {
			parentEntities.addAll(findParents(parents.getFirst().getParent(),
					className));
		}
		return parentEntities;
	}

	private List<Field> generateFields(TermList relatedLabels, String className) {
		if (className.equals("Concept")) {
			return generateConceptFields(relatedLabels);
		}
		if (className.equals("Timespan")) {
			return generateTimespanFields(relatedLabels);
		}
		if (className.equals("Agent")) {
			return generateAgentFields(relatedLabels);
		}
		if (className.equals("Place")) {
			return generatePlaceFields(relatedLabels);
		}
		return null;
	}

	private List<Field> generatePlaceFields(TermList relatedLabel) {
		List<Field> fields = new ArrayList<Field>();
		Term firstTerm = relatedLabel.getFirst();
		// First generate the unique fields
		for (PlaceFields placeField : PlaceFields.values()) {
			if (!placeField.isMulti()
					&& !placeField.equals(PlaceFields.ISPARTOF)) {
				if (firstTerm.getProperty(placeField.getInputField()) != null) {
					Field field = new Field();
					field.setName(placeField.getField());
					Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
					List<String> vals = new ArrayList<String>();
					vals.add(firstTerm.getProperty(placeField.getInputField()));
					fieldValues.put("def", vals);
					field.setValues(fieldValues);
					fields.add(field);
				}
			}
		}
		if (firstTerm.getParent() != null) {
			Field field = new Field();
			field.setName(PlaceFields.ISPARTOF.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(firstTerm.getParent().getCode());
			fieldValues.put("def", vals);
			field.setValues(fieldValues);
			fields.add(field);
		}
		// Then the rest
		for (Term term : relatedLabel) {
			// Get the pref label first
			Field field = new Field();
			field.setName(PlaceFields.PREFLABEL.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(term.getLabel());
			fieldValues.put(term.getLang() != null ? term.getLang().getCode()
					: "def", vals);
			field.setValues(fieldValues);
			fields.add(field);
			// Get the rest after
			for (PlaceFields placeField : PlaceFields.values()) {
				if (placeField.isMulti()
						&& !placeField.equals(PlaceFields.PREFLABEL)) {
					if (term.getProperty(placeField.getInputField()) != null) {
						Field fieldOther = new Field();
						field.setName(placeField.getField());
						Map<String, List<String>> fieldValuesOther = new HashMap<String, List<String>>();
						List<String> valsOther = new ArrayList<String>();
						valsOther.add(term.getProperty(placeField
								.getInputField()));
						fieldValuesOther.put(term.getLang() != null ? term
								.getLang().getCode() : "def", valsOther);
						fieldOther.setValues(fieldValuesOther);
						fields.add(fieldOther);
					}
				}
			}
		}

		return fields;
	}

	private List<Field> generateAgentFields(TermList relatedLabel) {
		List<Field> fields = new ArrayList<Field>();
		Term firstTerm = relatedLabel.getFirst();
		// First generate the unique fields
		for (AgentFields agentField : AgentFields.values()) {
			if (!agentField.isMulti()
					&& !agentField.equals(AgentFields.ISPARTOF)) {
				if (firstTerm.getProperty(agentField.getInputField()) != null) {
					Field field = new Field();
					field.setName(agentField.getField());
					Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
					List<String> vals = new ArrayList<String>();
					vals.add(firstTerm.getProperty(agentField.getInputField()));
					fieldValues.put("def", vals);
					field.setValues(fieldValues);
					fields.add(field);
				}
			}
		}
		if (firstTerm.getParent() != null) {
			Field field = new Field();
			field.setName(AgentFields.ISPARTOF.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(firstTerm.getParent().getCode());
			fieldValues.put("def", vals);
			field.setValues(fieldValues);
			fields.add(field);
		}
		// Then the rest
		for (Term term : relatedLabel) {
			// Get the pref label first
			Field field = new Field();
			field.setName(AgentFields.PREFLABEL.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(term.getLabel());
			fieldValues.put(term.getLang() != null ? term.getLang().getCode()
					: "def", vals);
			field.setValues(fieldValues);
			fields.add(field);
			// Get the rest after
			for (AgentFields agentField : AgentFields.values()) {
				if (agentField.isMulti()
						&& !agentField.equals(AgentFields.PREFLABEL)) {
					if (term.getProperty(agentField.getInputField()) != null) {
						Field fieldOther = new Field();
						field.setName(agentField.getField());
						Map<String, List<String>> fieldValuesOther = new HashMap<String, List<String>>();
						List<String> valsOther = new ArrayList<String>();
						valsOther.add(term.getProperty(agentField
								.getInputField()));
						fieldValuesOther.put(term.getLang() != null ? term
								.getLang().getCode() : "def", valsOther);
						fieldOther.setValues(fieldValuesOther);
						fields.add(fieldOther);
					}
				}
			}
		}

		return fields;
	}

	private List<Field> generateTimespanFields(TermList relatedLabel) {
		List<Field> fields = new ArrayList<Field>();
		Term firstTerm = relatedLabel.getFirst();
		// First generate the unique fields
		for (TimespanFields timespanField : TimespanFields.values()) {
			if (!timespanField.isMulti()
					&& !timespanField.equals(TimespanFields.ISPARTOF)) {
				if (firstTerm.getProperty(timespanField.getInputField()) != null) {
					Field field = new Field();
					field.setName(timespanField.getField());
					Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
					List<String> vals = new ArrayList<String>();
					vals.add(firstTerm.getProperty(timespanField
							.getInputField()));
					fieldValues.put("def", vals);
					field.setValues(fieldValues);
					fields.add(field);
				}
			}
		}
		if (firstTerm.getParent() != null) {
			Field field = new Field();
			field.setName(TimespanFields.ISPARTOF.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(firstTerm.getParent().getCode());
			fieldValues.put("def", vals);
			field.setValues(fieldValues);
			fields.add(field);
		}
		// Then the rest
		for (Term term : relatedLabel) {
			// Get the pref label first
			Field field = new Field();
			field.setName(TimespanFields.PREFLABEL.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(term.getLabel());
			fieldValues.put(term.getLang() != null ? term.getLang().getCode()
					: "def", vals);
			field.setValues(fieldValues);
			fields.add(field);
			// Get the rest after
			for (TimespanFields timespanField : TimespanFields.values()) {
				if (timespanField.isMulti()
						&& !timespanField.equals(TimespanFields.PREFLABEL)) {
					if (term.getProperty(timespanField
							.getInputField()) != null) {
						Field fieldOther = new Field();
						field.setName(timespanField.getField());
						Map<String, List<String>> fieldValuesOther = new HashMap<String, List<String>>();
						List<String> valsOther = new ArrayList<String>();
						valsOther.add(term.getProperty(timespanField
								.getInputField()));
						fieldValuesOther.put(term.getLang() != null ? term
								.getLang().getCode() : "def", valsOther);
						fieldOther.setValues(fieldValuesOther);
						fields.add(fieldOther);
					}

				}
			}
		}

		return fields;
	}

	private List<Field> generateConceptFields(TermList relatedLabel) {
		List<Field> fields = new ArrayList<Field>();
		Term firstTerm = relatedLabel.getFirst();
		// First generate the unique fields
		for (ConceptFields conceptField : ConceptFields.values()) {
			if (!conceptField.isMulti()
					&& !conceptField.equals(ConceptFields.BROADER)) {
				if (firstTerm.getProperty(conceptField.getInputField()) != null) {
					Field field = new Field();
					field.setName(conceptField.getField());
					Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
					List<String> vals = new ArrayList<String>();
					vals.add(firstTerm.getProperty(conceptField.getInputField()));
					fieldValues.put("def", vals);
					field.setValues(fieldValues);
					fields.add(field);
				}
			}
		}
		if (firstTerm.getParent() != null) {
			Field field = new Field();
			field.setName(ConceptFields.BROADER.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(firstTerm.getParent().getCode());
			fieldValues.put("def", vals);
			field.setValues(fieldValues);
			fields.add(field);
		}
		// Then the rest
		for (Term term : relatedLabel) {
			// Get the pref label first
			Field field = new Field();
			field.setName(ConceptFields.PREFLABEL.getField());
			Map<String, List<String>> fieldValues = new HashMap<String, List<String>>();
			List<String> vals = new ArrayList<String>();
			vals.add(term.getLabel());
			fieldValues.put(term.getLang() != null ? term.getLang().getCode()
					: "def", vals);
			field.setValues(fieldValues);
			fields.add(field);
			// Get the rest after
			for (ConceptFields conceptField : ConceptFields.values()) {
				if (conceptField.isMulti()
						&& !conceptField.equals(ConceptFields.PREFLABEL)) {
					if (term.getProperty(conceptField.getInputField()) != null) {
						Field fieldOther = new Field();
						field.setName(conceptField.getField());
						Map<String, List<String>> fieldValuesOther = new HashMap<String, List<String>>();
						List<String> valsOther = new ArrayList<String>();
						valsOther.add(term.getProperty(conceptField
								.getInputField()));
						fieldValuesOther.put(term.getLang() != null ? term
								.getLang().getCode() : "def", valsOther);
						fieldOther.setValues(fieldValuesOther);
						fields.add(fieldOther);
					}
				}
			}
		}

		return fields;
	}

}

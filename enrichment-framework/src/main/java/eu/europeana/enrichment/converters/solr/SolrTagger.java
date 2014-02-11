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

import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.Concept.Choice;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.enrichment.converters.fields.AgentFields;
import eu.europeana.enrichment.converters.fields.ConceptFields;
import eu.europeana.enrichment.converters.fields.PlaceFields;
import eu.europeana.enrichment.converters.fields.TimespanFields;
import eu.europeana.enrichment.model.external.Entity;
import eu.europeana.enrichment.model.external.EntityWrapper;
import eu.europeana.enrichment.model.external.Field;
import eu.europeana.enrichment.model.internal.CodeURI;
import eu.europeana.enrichment.model.internal.Term;
import eu.europeana.enrichment.model.internal.TermList;
import eu.europeana.enrichment.tagger.rules.AbstractLookupRule;
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

	List<Entity> tag2(SolrInputDocument document) throws Exception {
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
	
	

	List<EntityWrapper> tag(SolrInputDocument document) throws Exception {
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
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
					entities.addAll(findEntities2(
							value.toString().toLowerCase(), frp.getField(),
							className));
				}
			}
		}

		return entities;
	}
	
	

	private List<EntityWrapper> findEntities2(String lowerCase,
			String field, String className) throws MalformedURLException {
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		
		if(className.equals("Concept")){
			entities.addAll(findConceptEntities(lowerCase, field));
		}
		if(className.equals("Place")){
			entities.addAll(findPlaceEntities(lowerCase, field));
		}
		if(className.equals("Agent")){
			entities.addAll(findAgentEntities(lowerCase, field));
		}
		if(className.equals("Timespan")){
			entities.addAll(findTimespanEntities(lowerCase, field));
		}
		return entities;
	}

	@Deprecated
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

	private List<EntityWrapper<Concept>> findConceptEntities(String value,
			String originalField) throws MalformedURLException {
		List<EntityWrapper<Concept>> concepts = new ArrayList<EntityWrapper<Concept>>();
		TermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms.size() > 0) {
			Term term = terms.getFirst();
			Concept concept = new Concept();
			concept.setAbout(term.getCode());
			TermList relatedLabels = MongoDatabaseUtils.findByCode(new CodeURI(
					term.getCode()), dbtable);
			concept.setChoiceList(generateConceptChoices(relatedLabels));
			EntityWrapper<Concept> conceptEntity = new EntityWrapper<Concept>();
			conceptEntity.setOriginalField(originalField);
			conceptEntity.setContextualEntity(concept);
			concepts.add(conceptEntity);
			if (relatedLabels.getFirst().getParent() != null) {
				concepts.addAll(findConceptParents(relatedLabels.getFirst()
						.getParent()));
			}
		}

		return concepts;
	}

	private List<EntityWrapper<Concept>> findConceptParents(Term parent)
			throws MalformedURLException {
		List<EntityWrapper<Concept>> parentEntities = new ArrayList<EntityWrapper<Concept>>();
		TermList parents = MongoDatabaseUtils.findByCode(
				new CodeURI(parent.getCode()), dbtable);
		Concept concept = new Concept();
		concept.setAbout(parents.getFirst().getCode());
		concept.setChoiceList(generateConceptChoices(parents));
		EntityWrapper<Concept> entity = new EntityWrapper<Concept>();
		entity.setContextualEntity(concept);
		parentEntities.add(entity);
		if (parents.getFirst().getParent() != null) {
			parentEntities.addAll(findConceptParents(parents.getFirst()
					.getParent()));
		}
		return parentEntities;
	}

	private List<Choice> generateConceptChoices(TermList relatedLabels) {
		List<Choice> choices = new ArrayList<Concept.Choice>();
		Term firstTerm = relatedLabels.getFirst();
		for (ConceptFields conceptField : ConceptFields.values()) {
			if (!conceptField.isMulti()
					&& !conceptField.equals(ConceptFields.BROADER)) {
				if (firstTerm.getProperty(conceptField.getInputField()) != null) {
					choices.add((Concept.Choice) conceptField.generateField(
							firstTerm.getProperty(conceptField.getInputField()),
							null));

				}
			}
		}
		if (firstTerm.getParent() != null) {
			choices.add((Concept.Choice) ConceptFields.BROADER.generateField(
					firstTerm.getParent().getCode(), null));
		}
		for (Term term : relatedLabels) {
			// Get the pref label first
			choices.add((Concept.Choice) ConceptFields.PREFLABEL.generateField(
					term.getLabel(), term.getLang() != null ? term.getLang()
							.getCode() : "def"));

			// Get the rest after
			for (ConceptFields conceptField : ConceptFields.values()) {
				if (conceptField.isMulti()
						&& !conceptField.equals(ConceptFields.PREFLABEL)) {
					if (term.getProperty(conceptField.getInputField()) != null) {
						choices.add((Concept.Choice) conceptField
								.generateField(term.getProperty(conceptField
										.getInputField()),
										term.getLang() != null ? term.getLang()
												.getCode() : "def"));
					}
				}
			}
		}
		return choices;
	}

	private List<EntityWrapper<AgentType>> findAgentEntities(String value,
			String originalField) throws MalformedURLException {
		List<EntityWrapper<AgentType>> agents = new ArrayList<EntityWrapper<AgentType>>();
		TermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms.size() > 0) {
			Term term = terms.getFirst();
			AgentType agent = new AgentType();
			agent.setAbout(term.getCode());
			TermList relatedLabels = MongoDatabaseUtils.findByCode(new CodeURI(
					term.getCode()), dbtable);

			agent = generateAgent(agent, relatedLabels);
			EntityWrapper<AgentType> agentEntity = new EntityWrapper<AgentType>();
			agentEntity.setOriginalField(originalField);
			agentEntity.setContextualEntity(agent);
			agents.add(agentEntity);
			if (relatedLabels.getFirst().getParent() != null) {
				agents.addAll(findAgentParents(relatedLabels.getFirst()
						.getParent()));
			}
		}
		return agents;
	}

	private List<EntityWrapper<AgentType>> findAgentParents(Term parent)
			throws MalformedURLException {
		List<EntityWrapper<AgentType>> parentEntities = new ArrayList<EntityWrapper<AgentType>>();
		TermList parents = MongoDatabaseUtils.findByCode(
				new CodeURI(parent.getCode()), dbtable);
		AgentType agent = new AgentType();
		agent.setAbout(parents.getFirst().getCode());
		agent = generateAgent(agent, parents);
		EntityWrapper<AgentType> entity = new EntityWrapper<AgentType>();
		entity.setContextualEntity(agent);
		parentEntities.add(entity);
		if (parents.getFirst().getParent() != null) {
			parentEntities.addAll(findAgentParents(parents.getFirst()
					.getParent()));
		}
		return parentEntities;
	}

	private AgentType generateAgent(AgentType agent, TermList relatedLabels) {
		Term firstTerm = relatedLabels.getFirst();
		for (AgentFields agentField : AgentFields.values()) {
			if (!agentField.isMulti()
					&& !agentField.equals(AgentFields.ISPARTOF)) {
				if (firstTerm.getProperty(agentField.getInputField()) != null) {

					agent = (AgentType) agentField.generateField(
							firstTerm.getProperty(agentField.getInputField()),
							null, agent);
				}
			}
		}
		if (firstTerm.getParent() != null) {
			agent = (AgentType) AgentFields.ISPARTOF.generateField(firstTerm
					.getParent().getCode(), null, agent);
		}
		for (Term term : relatedLabels) {
			// Get the pref label first
			agent = (AgentType) AgentFields.PREFLABEL.generateField(term
					.getLabel(), term.getLang() != null ? term.getLang()
					.getCode() : "def", agent);

			// Get the rest after
			for (AgentFields agentField : AgentFields.values()) {
				if (agentField.isMulti()
						&& !agentField.equals(AgentFields.PREFLABEL)) {
					if (term.getProperty(agentField.getInputField()) != null) {
						agent = (AgentType) agentField.generateField(term
								.getProperty(agentField.getInputField()), term
								.getLang() != null ? term.getLang().getCode()
								: "def", agent);
					}
				}
			}
		}
		return agent;
	}

	private List<EntityWrapper<PlaceType>> findPlaceEntities(String value,
			String originalField) throws MalformedURLException {
		List<EntityWrapper<PlaceType>> places = new ArrayList<EntityWrapper<PlaceType>>();
		TermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms.size() > 0) {
			Term term = terms.getFirst();
			PlaceType place = new PlaceType();
			place.setAbout(term.getCode());
			TermList relatedLabels = MongoDatabaseUtils.findByCode(new CodeURI(
					term.getCode()), dbtable);

			place = generatePlace(place, relatedLabels);
			EntityWrapper<PlaceType> placeEntity = new EntityWrapper<PlaceType>();
			placeEntity.setOriginalField(originalField);
			placeEntity.setContextualEntity(place);
			places.add(placeEntity);
			if (relatedLabels.getFirst().getParent() != null) {
				places.addAll(findPlaceParents(relatedLabels.getFirst()
						.getParent()));
			}
		}
		return places;
	}

	private List<EntityWrapper<PlaceType>> findPlaceParents(Term parent)
			throws MalformedURLException {
		List<EntityWrapper<PlaceType>> parentEntities = new ArrayList<EntityWrapper<PlaceType>>();
		TermList parents = MongoDatabaseUtils.findByCode(
				new CodeURI(parent.getCode()), dbtable);
		PlaceType place = new PlaceType();
		place.setAbout(parents.getFirst().getCode());
		place = generatePlace(place, parents);
		EntityWrapper<PlaceType> entity = new EntityWrapper<PlaceType>();
		entity.setContextualEntity(place);
		parentEntities.add(entity);
		if (parents.getFirst().getParent() != null) {
			parentEntities.addAll(findPlaceParents(parents.getFirst()
					.getParent()));
		}
		return parentEntities;
	}

	private PlaceType generatePlace(PlaceType place, TermList relatedLabels) {
		Term firstTerm = relatedLabels.getFirst();
		for (PlaceFields placeField : PlaceFields.values()) {
			if (!placeField.isMulti()
					&& !placeField.equals(PlaceFields.ISPARTOF)) {
				if (firstTerm.getProperty(placeField.getInputField()) != null) {

					place = (PlaceType) placeField.generateField(
							firstTerm.getProperty(placeField.getInputField()),
							null, place);
				}
			}
		}
		if (firstTerm.getParent() != null) {
			place = (PlaceType) PlaceFields.ISPARTOF.generateField(firstTerm
					.getParent().getCode(), null, place);
		}
		for (Term term : relatedLabels) {
			// Get the pref label first
			place = (PlaceType) PlaceFields.PREFLABEL.generateField(term
					.getLabel(), term.getLang() != null ? term.getLang()
					.getCode() : "def", place);

			// Get the rest after
			for (PlaceFields placeField : PlaceFields.values()) {
				if (placeField.isMulti()
						&& !placeField.equals(PlaceFields.PREFLABEL)) {
					if (term.getProperty(placeField.getInputField()) != null) {
						place = (PlaceType) placeField.generateField(term
								.getProperty(placeField.getInputField()), term
								.getLang() != null ? term.getLang().getCode()
								: "def", place);
					}
				}
			}
		}
		return place;
	}

	private List<EntityWrapper<TimeSpanType>> findTimespanEntities(
			String value, String originalField) throws MalformedURLException {
		List<EntityWrapper<TimeSpanType>> timespans = new ArrayList<EntityWrapper<TimeSpanType>>();
		TermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms.size() > 0) {
			Term term = terms.getFirst();
			TimeSpanType ts = new TimeSpanType();
			ts.setAbout(term.getCode());
			TermList relatedLabels = MongoDatabaseUtils.findByCode(new CodeURI(
					term.getCode()), dbtable);

			ts = generateTimespan(ts, relatedLabels);
			EntityWrapper<TimeSpanType> timeSpanEntity = new EntityWrapper<TimeSpanType>();
			timeSpanEntity.setOriginalField(originalField);
			timeSpanEntity.setContextualEntity(ts);
			timespans.add(timeSpanEntity);
			if (relatedLabels.getFirst().getParent() != null) {
				timespans.addAll(findTimespanParents(relatedLabels.getFirst()
						.getParent()));
			}
		}
		return timespans;
	}

	private List<EntityWrapper<TimeSpanType>> findTimespanParents(Term parent)
			throws MalformedURLException {
		List<EntityWrapper<TimeSpanType>> parentEntities = new ArrayList<EntityWrapper<TimeSpanType>>();
		TermList parents = MongoDatabaseUtils.findByCode(
				new CodeURI(parent.getCode()), dbtable);
		TimeSpanType ts = new TimeSpanType();
		ts.setAbout(parents.getFirst().getCode());
		ts = generateTimespan(ts, parents);
		EntityWrapper<TimeSpanType> entity = new EntityWrapper<TimeSpanType>();
		entity.setContextualEntity(ts);
		parentEntities.add(entity);
		if (parents.getFirst().getParent() != null) {
			parentEntities.addAll(findTimespanParents(parents.getFirst()
					.getParent()));
		}
		return parentEntities;
	}

	private TimeSpanType generateTimespan(TimeSpanType ts, TermList relatedLabels) {
		Term firstTerm = relatedLabels.getFirst();
		for (TimespanFields timespanField : TimespanFields.values()) {
			if (!timespanField.isMulti()
					&& !timespanField.equals(TimespanFields.ISPARTOF)) {
				if (firstTerm.getProperty(timespanField.getInputField()) != null) {

					ts = (TimeSpanType) timespanField.generateField(firstTerm
							.getProperty(timespanField.getInputField()), null,
							ts);
				}
			}
		}
		if (firstTerm.getParent() != null) {
			ts = (TimeSpanType) TimespanFields.ISPARTOF.generateField(firstTerm
					.getParent().getCode(), null, ts);
		}
		for (Term term : relatedLabels) {
			// Get the pref label first
			ts = (TimeSpanType) TimespanFields.PREFLABEL.generateField(term
					.getLabel(), term.getLang() != null ? term.getLang()
					.getCode() : "def", ts);

			// Get the rest after
			for (TimespanFields timespanField : TimespanFields.values()) {
				if (timespanField.isMulti()
						&& !timespanField.equals(TimespanFields.PREFLABEL)) {
					if (term.getProperty(timespanField.getInputField()) != null) {
						ts = (TimeSpanType) timespanField.generateField(term
								.getProperty(timespanField.getInputField()), term
								.getLang() != null ? term.getLang().getCode()
								: "def", ts);
					}
				}
			}
		}
		return ts;
	}

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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
					if (term.getProperty(timespanField.getInputField()) != null) {
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

	@Deprecated
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

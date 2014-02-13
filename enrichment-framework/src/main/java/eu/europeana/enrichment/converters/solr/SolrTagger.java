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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.enrichment.model.external.EntityWrapper;
import eu.europeana.enrichment.model.internal.MongoTermList;
import eu.europeana.enrichment.model.internal.Term;
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
					entities.addAll(findEntities(
							value.toString().toLowerCase(), frp.getField(),
							className));
				}
			}
		}

		return entities;
	}
	
	

	private List<EntityWrapper> findEntities(String lowerCase,
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

	

	private List<EntityWrapper> findConceptEntities(String value,
			String originalField) throws MalformedURLException  {
		List<EntityWrapper> concepts = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms!=null) {
			
			EntityWrapper conceptEntity = new EntityWrapper();
			conceptEntity.setOriginalField(originalField);
			
		 
			conceptEntity.setContextualEntity(terms.getRepresentation());
			concepts.add(conceptEntity);
			if (terms.getParent() != null) {
				concepts.addAll(findConceptParents(terms.getParent()));
			}
		}

		return concepts;
	}

	private List<EntityWrapper> findConceptParents(String parent) throws MalformedURLException{
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, dbtable);
	
		EntityWrapper entity = new EntityWrapper();
		entity.setContextualEntity(parents.getRepresentation());
		parentEntities.add(entity);
		if (parents.getParent() != null) {
			parentEntities.addAll(findConceptParents(parents.getParent()));
		}
		return parentEntities;
	}

	

	private List<EntityWrapper> findAgentEntities(String value,
			String originalField) throws MalformedURLException {
		List<EntityWrapper> agents = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms!=null) {
			
			EntityWrapper agentEntity = new EntityWrapper();
			agentEntity.setOriginalField(originalField);
			
		 
			agentEntity.setContextualEntity(terms.getRepresentation());
			agents.add(agentEntity);
			if (terms.getParent() != null) {
				agents.addAll(findAgentParents(terms.getParent()));
			}
		}
		return agents;
	}

	private List<EntityWrapper> findAgentParents(String parent) throws MalformedURLException
			 {
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, dbtable);
		
		EntityWrapper entity = new EntityWrapper();
	 
		entity.setContextualEntity(parents.getRepresentation());
		parentEntities.add(entity);
		if (parents.getParent() != null) {
			parentEntities.addAll(findAgentParents(parents.getParent()));
		}
		return parentEntities;
	}

	

	private List<EntityWrapper> findPlaceEntities(String value,
			String originalField) throws MalformedURLException {
		List<EntityWrapper> places = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms!=null) {
			
			EntityWrapper placeEntity = new EntityWrapper();
			placeEntity.setOriginalField(originalField);
			placeEntity.setContextualEntity(terms.getRepresentation());
			places.add(placeEntity);
			if (terms.getParent() != null) {
				places.addAll(findPlaceParents(terms.getParent()));
			}
		}
		return places;
	}

	private List<EntityWrapper> findPlaceParents(String parent)
			throws MalformedURLException {
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, dbtable);
		
		EntityWrapper entity = new EntityWrapper();
		entity.setContextualEntity(parents.getRepresentation());
		parentEntities.add(entity);
		if (parents.getParent() != null) {
			parentEntities.addAll(findPlaceParents(parents.getParent()));
		}
		return parentEntities;
	}

	
	private List<EntityWrapper> findTimespanEntities(
			String value, String originalField) throws MalformedURLException {
		List<EntityWrapper> timespans = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, dbtable);
		if (terms!=null) {
			EntityWrapper timeSpanEntity = new EntityWrapper();
			timeSpanEntity.setOriginalField(originalField);
			timeSpanEntity.setContextualEntity(terms.getRepresentation());
			timespans.add(timeSpanEntity);
			if (terms.getParent() != null) {
				timespans.addAll(findTimespanParents(terms.getParent()));
			}
		}
		return timespans;
	}

	private List<EntityWrapper> findTimespanParents(String parent)
			throws MalformedURLException {
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, dbtable);
		
		EntityWrapper entity = new EntityWrapper();
		entity.setContextualEntity(parents.getRepresentation());
		parentEntities.add(entity);
		if (parents.getParent() != null) {
			parentEntities.addAll(findTimespanParents(parents.getParent()));
		}
		return parentEntities;
	}

}

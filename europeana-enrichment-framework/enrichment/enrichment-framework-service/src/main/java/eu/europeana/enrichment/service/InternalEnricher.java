/*
a * Copyright 2005-2009 the original author or authors.
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
package eu.europeana.enrichment.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;

/**
 * Main enrichment class
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@SuppressWarnings("rawtypes")
public class InternalEnricher {

	private final static ObjectMapper obj = new ObjectMapper();
	private final static String CONCEPT="concept";
	private final static String TIMESPAN = "period";
	private final static String PLACE="place";
	private final static String AGENT = "people";
	
	public InternalEnricher() {
		SimpleModule sm = new SimpleModule("test", Version.unknownVersion());
		sm.addSerializer(new ObjectIdSerializer());
		obj.registerModule(sm);
	}

	/**
	 * The internal enrichment functionality not to be exposed yet as there is a
	 * strong dependency to the external resources to recreate the DB The
	 * enrichment is performed by lowercasing every value so that searchability
	 * in the DB is enhanced, but the Capitalized version is always retrieved
	 * 
	 * @param values
	 *            The values to enrich
	 * @return A list of enrichments
	 * @throws Exception
	 */
	protected List<? extends EntityWrapper> tag(List<InputValue> values)
			throws JsonGenerationException, JsonMappingException, IOException {

		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		for (InputValue inputValue : values) {
			for (EntityClass voc : inputValue.getVocabularies()) {
				entities.addAll(findEntities(inputValue.getValue()
						.toLowerCase(), inputValue.getOriginalField(), voc));
			}
		}
		return entities;
	}

	private List<? extends EntityWrapper> findEntities(String lowerCase,
			String field, EntityClass className)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		switch (className) {
		case AGENT:
			entities.addAll(findAgentEntities(lowerCase, field));
			break;
		case CONCEPT:
			entities.addAll(findConceptEntities(lowerCase, field));
			break;
		case PLACE:
			entities.addAll(findPlaceEntities(lowerCase, field));
			break;
		case TIMESPAN:
			entities.addAll(findTimespanEntities(lowerCase, field));
		default:
			break;
		}
		return entities;
	}

	private List<EntityWrapper> findConceptEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		List<EntityWrapper> concepts = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, CONCEPT);
		if (terms != null) {

			EntityWrapper conceptEntity = new EntityWrapper();
			conceptEntity.setOriginalField(originalField);
			conceptEntity.setClassName(ConceptImpl.class.getName());

			conceptEntity.setContextualEntity(getObjectMapper()
					.writeValueAsString(terms.getRepresentation()));
			conceptEntity.setUrl(terms.getCodeUri());
			conceptEntity.setOriginalValue(value);
			concepts.add(conceptEntity);
			
			if (terms.getParent() != null) {
				concepts.addAll(findConceptParents(terms.getParent()));
			}
		}

		return concepts;
	}

	private List<? extends EntityWrapper> findConceptParents(String parent)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils
				.findByCode(parent, CONCEPT);

		EntityWrapper entity = new EntityWrapper();
		entity.setClassName(ConceptImpl.class.getName());
		entity.setContextualEntity(getObjectMapper().writeValueAsString(
				parents.getRepresentation()));
		entity.setUrl(parents.getCodeUri());
		parentEntities.add(entity);
		if (parents.getParent() != null && !parent.equals(parents.getParent())) {
			parentEntities.addAll(findConceptParents(parents.getParent()));
		}
		return parentEntities;
	}

	private List<? extends EntityWrapper> findAgentEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		List<EntityWrapper> agents = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, AGENT);
		if (terms != null) {

			EntityWrapper agentEntity = new EntityWrapper();
			agentEntity.setOriginalField(originalField);

			agentEntity.setClassName(AgentImpl.class.getName());
			agentEntity.setContextualEntity(getObjectMapper()
					.writeValueAsString(terms.getRepresentation()));
			agentEntity.setUrl(terms.getCodeUri());
			agentEntity.setOriginalValue(value);
			agents.add(agentEntity);
			if (terms.getParent() != null) {
				agents.addAll(findAgentParents(terms.getParent()));
			}
		}
		return agents;
	}

	private List<? extends EntityWrapper> findAgentParents(String parent)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, AGENT);

		EntityWrapper entity = new EntityWrapper();
		entity.setClassName(AgentImpl.class.getName());
		entity.setContextualEntity(getObjectMapper().writeValueAsString(
				parents.getRepresentation()));
		entity.setUrl(parents.getCodeUri());
		parentEntities.add(entity);
		if (parents.getParent() != null&&!parent.equals(parents.getParent())) {
			parentEntities.addAll(findAgentParents(parents.getParent()));
		}
		return parentEntities;
	}

	private List<? extends EntityWrapper> findPlaceEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		List<EntityWrapper> places = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, PLACE);
		if (terms != null) {

			EntityWrapper placeEntity = new EntityWrapper();
			placeEntity.setOriginalField(originalField);
			placeEntity.setClassName(PlaceImpl.class.getName());

			placeEntity.setContextualEntity(getObjectMapper()
					.writeValueAsString(terms.getRepresentation()));
			placeEntity.setUrl(terms.getCodeUri());
			placeEntity.setOriginalValue(value);
			places.add(placeEntity);
			if (terms.getParent() != null) {
				places.addAll(findPlaceParents(terms.getParent()));
			}
		}
		return places;
	}

	private List<? extends EntityWrapper> findPlaceParents(String parent)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, PLACE);

		EntityWrapper entity = new EntityWrapper();
		entity.setClassName(PlaceImpl.class.getName());

		entity.setContextualEntity(getObjectMapper().writeValueAsString(
				parents.getRepresentation()));
		entity.setUrl(parents.getCodeUri());
		parentEntities.add(entity);
		
		if (parents.getParent() != null && !parent.equals(parents.getParent())) {
			
			parentEntities.addAll(findPlaceParents(parents.getParent()));
		}
		return parentEntities;
	}

	private List<? extends EntityWrapper> findTimespanEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		List<EntityWrapper> timespans = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, TIMESPAN);
		if (terms != null) {
			EntityWrapper timeSpanEntity = new EntityWrapper();
			timeSpanEntity.setOriginalField(originalField);
			timeSpanEntity.setClassName(TimespanImpl.class.getName());
			timeSpanEntity.setContextualEntity(getObjectMapper()
					.writeValueAsString(terms.getRepresentation()));
			timeSpanEntity.setOriginalValue(value);
			timeSpanEntity.setUrl(terms.getCodeUri());
			timespans.add(timeSpanEntity);
			if (terms.getParent() != null) {
				timespans.addAll(findTimespanParents(terms.getParent()));
			}
		}
		return timespans;
	}

	private List<? extends EntityWrapper> findTimespanParents(String parent)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<EntityWrapper> parentEntities = new ArrayList<EntityWrapper>();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, TIMESPAN);

		EntityWrapper entity = new EntityWrapper();
		entity.setClassName(TimespanImpl.class.getName());
		entity.setContextualEntity(getObjectMapper().writeValueAsString(
				parents.getRepresentation()));
		entity.setUrl(parents.getCodeUri());
		parentEntities.add(entity);
		if (parents.getParent() != null&&!parent.equals(parents.getParent())) {
			parentEntities.addAll(findTimespanParents(parents.getParent()));
		}
		return parentEntities;
	}

	private ObjectMapper getObjectMapper() {
		return obj;
	}
}

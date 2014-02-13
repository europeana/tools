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
import java.util.List;

import eu.europeana.enrichment.model.external.EntityWrapper;
import eu.europeana.enrichment.model.external.api.EntityClass;
import eu.europeana.enrichment.model.external.api.InputValue;
import eu.europeana.enrichment.model.internal.MongoTermList;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;

/**
 * Lookup (tagging) rule.
 * 
 * @author Borys Omelayenko
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SolrTagger {


	public SolrTagger(){
		
	}

	List<EntityWrapper> tag(List<InputValue> values) throws Exception {
		
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		for (InputValue inputValue : values) {
			for(EntityClass voc:inputValue.getVocabularies()){
				entities.addAll(findEntities(inputValue.getValue().toLowerCase(), inputValue.getOriginalField(), voc));
			}
		}
		return entities;
	}

	private List<EntityWrapper> findEntities(String lowerCase,
			String field, EntityClass className) throws MalformedURLException {
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		switch (className) {
		case AGENT:
			entities.addAll(findAgentEntities(lowerCase, field));
			break;
		case CONCEPT:
			entities.addAll(findConceptEntities(lowerCase, field));
		case PLACE:
			entities.addAll(findPlaceEntities(lowerCase, field));
		case TIMESPAN:
			entities.addAll(findTimespanEntities(lowerCase, field));
		default:
			break;
		}
		return entities;
	}

	

	private List<EntityWrapper> findConceptEntities(String value,
			String originalField) throws MalformedURLException  {
		List<EntityWrapper> concepts = new ArrayList<EntityWrapper>();
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, "concept");
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
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "concept");
	
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
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, "people");
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
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "people");
		
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
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, "place");
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
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "place");
		
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
		MongoTermList terms = MongoDatabaseUtils.findByLabel(value, "period");
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
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "period");
		
		EntityWrapper entity = new EntityWrapper();
		entity.setContextualEntity(parents.getRepresentation());
		parentEntities.add(entity);
		if (parents.getParent() != null) {
			parentEntities.addAll(findTimespanParents(parents.getParent()));
		}
		return parentEntities;
	}

}

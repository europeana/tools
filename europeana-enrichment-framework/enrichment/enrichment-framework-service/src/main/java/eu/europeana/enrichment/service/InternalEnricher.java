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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.TimespanTermList;
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
	private static Map<String, String> agentCache;
	private static Map<String, String> conceptCache;
	private static Map<String, String> placeCache;
	private static Map<String, String> timespanCache;
	private static Map<String, EntityWrapper> agentUriCache;
	private static Map<String, EntityWrapper> conceptUriCache;
	private static Map<String, EntityWrapper> placeUriCache;
	private static Map<String, EntityWrapper> timespanUriCache;
	private static Map<String, List<String>> agentParents;
	private static Map<String, List<String>> conceptParents;
	private static Map<String, List<String>> placeParents;
	private static Map<String, List<String>> timespanParents;
	public InternalEnricher() {
		SimpleModule sm = new SimpleModule("test", Version.unknownVersion());
		sm.addSerializer(new ObjectIdSerializer());
		obj.registerModule(sm);
		agentCache = new ConcurrentHashMap();
		conceptCache = new ConcurrentHashMap();
		placeCache = new ConcurrentHashMap();
		timespanCache = new ConcurrentHashMap();
		agentUriCache = new ConcurrentHashMap();
		conceptUriCache = new ConcurrentHashMap();
		placeUriCache = new ConcurrentHashMap();
		timespanUriCache = new ConcurrentHashMap();
		agentParents = new ConcurrentHashMap();
		conceptParents = new ConcurrentHashMap();
		timespanParents = new ConcurrentHashMap();
		placeParents = new ConcurrentHashMap();
		populate();
	}

	public void populate() {
		Logger.getLogger("Initializing");
		MongoDatabaseUtils.dbExists("localhost",27017);
		Map agentsMongo = MongoDatabaseUtils.getAllAgents();
		Logger.getLogger(InternalEnricher.class.getName()).severe("Found agents: " + agentsMongo.size());
		Iterator conceptsMongo = agentsMongo.entrySet().iterator();

		while(conceptsMongo.hasNext()) {
			Map.Entry placesMongo = (Map.Entry)conceptsMongo.next();

			try {
				EntityWrapper timespanMongo = new EntityWrapper();
				timespanMongo.setOriginalField("");
				timespanMongo.setClassName(AgentImpl.class.getName());
				timespanMongo.setContextualEntity(this.getObjectMapper().writeValueAsString(((MongoTermList)placesMongo.getValue()).getRepresentation()));
				timespanMongo.setUrl(((MongoTermList)placesMongo.getValue()).getCodeUri());
				timespanMongo.setOriginalValue(((MongoTerm)placesMongo.getKey()).getLabel());
				agentCache.put(((MongoTerm)placesMongo.getKey()).getLabel(), ((MongoTerm)placesMongo.getKey()).getCodeUri());
				agentUriCache.put(((MongoTerm)placesMongo.getKey()).getCodeUri(), timespanMongo);
				agentParents.put(((MongoTerm)placesMongo.getKey()).getCodeUri(), this.findAgentParents(((MongoTermList)placesMongo.getValue()).getParent()));
			} catch (IOException var14) {
				Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var14);
			}
		}

		try {
			Thread.sleep(10000L);
		} catch (InterruptedException var13) {
			Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var13);
		}

		Map conceptsMongo1 = MongoDatabaseUtils.getAllConcepts();
		Logger.getLogger(InternalEnricher.class.getName()).severe("Found concepts: " + conceptsMongo1.size());
		Iterator placesMongo1 = conceptsMongo1.entrySet().iterator();

		while(placesMongo1.hasNext()) {
			Map.Entry timespanMongo1 = (Map.Entry)placesMongo1.next();

			try {
				EntityWrapper i$ = new EntityWrapper();
				i$.setOriginalField("");
				i$.setClassName(ConceptImpl.class.getName());
				i$.setContextualEntity(this.getObjectMapper().writeValueAsString(((MongoTermList)timespanMongo1.getValue()).getRepresentation()));
				i$.setUrl(((MongoTermList)timespanMongo1.getValue()).getCodeUri());
				i$.setOriginalValue(((MongoTerm)timespanMongo1.getKey()).getLabel());
				conceptCache.put(((MongoTerm)timespanMongo1.getKey()).getLabel(), ((MongoTerm)timespanMongo1.getKey()).getCodeUri());
				conceptUriCache.put(((MongoTerm)timespanMongo1.getKey()).getCodeUri(), i$);
				conceptParents.put(((MongoTerm)timespanMongo1.getKey()).getCodeUri(), this.findConceptParents(((MongoTermList)timespanMongo1.getValue()).getParent()));
			} catch (IOException var12) {
				Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var12);
			}
		}

		try {
			Thread.sleep(10000L);
		} catch (InterruptedException var11) {
			Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var11);
		}

		Map placesMongo2 = MongoDatabaseUtils.getAllPlaces();
		Logger.getLogger(InternalEnricher.class.getName()).severe("Found places: " + placesMongo2.size());
		Iterator timespanMongo2 = placesMongo2.entrySet().iterator();

		while(timespanMongo2.hasNext()) {
			Map.Entry i$1 = (Map.Entry)timespanMongo2.next();

			try {
				EntityWrapper entry = new EntityWrapper();
				entry.setOriginalField("");
				entry.setClassName(PlaceImpl.class.getName());
				entry.setContextualEntity(this.getObjectMapper().writeValueAsString(((MongoTermList)i$1.getValue()).getRepresentation()));
				entry.setUrl(((MongoTermList)i$1.getValue()).getCodeUri());
				entry.setOriginalValue(((MongoTerm)i$1.getKey()).getLabel());
				placeCache.put(((MongoTerm)i$1.getKey()).getLabel(), ((MongoTerm)i$1.getKey()).getCodeUri());
				placeUriCache.put(((MongoTerm)i$1.getKey()).getCodeUri(), entry);
				placeParents.put(((MongoTerm)i$1.getKey()).getCodeUri(), this.findPlaceParents(((MongoTermList)i$1.getValue()).getParent()));
			} catch (IOException var10) {
				Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var10);
			}
		}

		try {
			Thread.sleep(10000L);
		} catch (InterruptedException var9) {
			Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var9);
		}

		Map timespanMongo3 = MongoDatabaseUtils.getAllTimespans();
		Logger.getLogger(InternalEnricher.class.getName()).severe("Found timespans: " + timespanMongo3.size());
		Iterator i$2 = timespanMongo3.entrySet().iterator();

		while(i$2.hasNext()) {
			Map.Entry entry1 = (Map.Entry)i$2.next();

			try {
				EntityWrapper ex = new EntityWrapper();
				ex.setOriginalField("");
				ex.setClassName(TimespanImpl.class.getName());
				ex.setContextualEntity(this.getObjectMapper().writeValueAsString(((TimespanTermList)entry1.getValue()).getRepresentation()));
				ex.setOriginalValue(((MongoTerm)entry1.getKey()).getOriginalLabel());
				ex.setUrl(((TimespanTermList)entry1.getValue()).getCodeUri());
				timespanCache.put(((MongoTerm)entry1.getKey()).getLabel(), ((MongoTerm)entry1.getKey()).getCodeUri());
				timespanUriCache.put(((MongoTerm)entry1.getKey()).getCodeUri(), ex);
				timespanParents.put(((MongoTerm)entry1.getKey()).getCodeUri(), this.findTimespanParents(((TimespanTermList)entry1.getValue()).getParent()));
			} catch (IOException var8) {
				Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var8);
			}
		}

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
		ArrayList concepts = new ArrayList();
		if(conceptCache.get(value) != null) {
			boolean i = false;
			EntityWrapper entity = (EntityWrapper)conceptUriCache.get(conceptCache.get(value));
			entity.setOriginalField(originalField);
			concepts.add(entity);
			Iterator i$ = ((List)conceptParents.get(entity.getUrl())).iterator();

			while(i$.hasNext()) {
				String uris = (String)i$.next();
				concepts.add(conceptUriCache.get(uris));
			}
		}

		return concepts;
	}

	private List<String> findConceptParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
		ArrayList parentEntities = new ArrayList();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "concept");
		if(parents != null) {
			parentEntities.add(parents.getCodeUri());
			if(parents.getParent() != null && !parent.equals(parents.getParent())) {
				parentEntities.addAll(this.findConceptParents(parents.getParent()));
			}
		}

		return parentEntities;
	}

	private List<? extends EntityWrapper> findAgentEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		ArrayList agents = new ArrayList();
		if(agentCache.get(value) != null) {
			boolean i = false;
			EntityWrapper entity = (EntityWrapper)agentUriCache.get(agentCache.get(value));
			entity.setOriginalField(originalField);
			agents.add(entity);
			Iterator i$ = ((List)agentParents.get(entity.getUrl())).iterator();

			while(i$.hasNext()) {
				String uris = (String)i$.next();
				agents.add(agentUriCache.get(uris));
			}
		}

		return agents;
	}

	private List<String> findAgentParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
		ArrayList parentEntities = new ArrayList();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "people");
		if(parents != null) {
			parentEntities.add(parents.getCodeUri());
			if(parents.getParent() != null && !parent.equals(parents.getParent())) {
				parentEntities.addAll(this.findAgentParents(parents.getParent()));
			}
		}

		return parentEntities;
	}

	private List<? extends EntityWrapper> findPlaceEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		ArrayList places = new ArrayList();
		if(placeCache.get(value) != null) {
			boolean i = false;
			EntityWrapper entity = (EntityWrapper)placeUriCache.get(placeCache.get(value));
			entity.setOriginalField(originalField);
			places.add(entity);
			Iterator i$ = ((List)placeParents.get(entity.getUrl())).iterator();

			while(i$.hasNext()) {
				String uris = (String)i$.next();
				places.add(placeUriCache.get(uris));
			}
		}

		return places;
	}

	private List<String> findPlaceParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
		ArrayList parentEntities = new ArrayList();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "place");
		if(parents != null) {
			parentEntities.add(parents.getCodeUri());
			if(parents.getParent() != null && !parent.equals(parents.getParent())) {
				parentEntities.addAll(this.findPlaceParents(parents.getParent()));
			}
		}

		return parentEntities;
	}


	private List<? extends EntityWrapper> findTimespanEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		ArrayList timespans = new ArrayList();
		if(timespanCache.get(value) != null) {
			boolean i = false;
			EntityWrapper entity = (EntityWrapper)timespanUriCache.get(timespanCache.get(value));
			entity.setOriginalField(originalField);
			timespans.add(entity);
			Iterator i$ = ((List)timespanParents.get(entity.getUrl())).iterator();

			while(i$.hasNext()) {
				String uris = (String)i$.next();
				timespans.add(timespanUriCache.get(uris));
			}
		}

		return timespans;
	}

	private List<String> findTimespanParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
		ArrayList parentEntities = new ArrayList();
		MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "period");
		if(parents != null) {
			parentEntities.add(parents.getCodeUri());
			if(parents.getParent() != null && !parent.equals(parents.getParent())) {
				try {
					Thread.sleep(10L);
				} catch (InterruptedException var5) {
					Logger.getLogger(InternalEnricher.class.getName()).log(Level.SEVERE, (String)null, var5);
				}

				parentEntities.addAll(this.findTimespanParents(parents.getParent()));
			}
		}

		return parentEntities;
	}


	private ObjectMapper getObjectMapper() {
		return obj;
	}





}

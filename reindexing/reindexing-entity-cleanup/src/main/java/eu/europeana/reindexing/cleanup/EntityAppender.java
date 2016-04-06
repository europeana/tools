/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.cleanup;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.*;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import org.apache.commons.codec.binary.*;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author ymamakis
 */
public class EntityAppender {

	private static ObjectMapper om;
	public EntityAppender(){
		om = new ObjectMapper();
	}

    private void addEntities(FullBean fBean,
			ProxyImpl europeanaProxy, List<RetrievedEntity> enrichedEntities)
			throws  IOException {

		for (RetrievedEntity enrichedEntity : enrichedEntities) {
			if (enrichedEntity.getEntity().getClass().getName().equals(
					ConceptImpl.class.getName())) {
				appendConcept(fBean, europeanaProxy,
						enrichedEntity);
			} else if (enrichedEntity.getEntity().getClass().getName().equals(
					AgentImpl.class.getName())) {
				appendAgent(fBean, europeanaProxy,
						enrichedEntity);
			} else if (enrichedEntity.getEntity().getClass().getName().equals(
					PlaceImpl.class.getName())) {
				appendPlace(fBean, europeanaProxy,
						enrichedEntity);
			} else if (enrichedEntity.getEntity().getClass().getName().equals(
					TimespanImpl.class.getName())) {
				appendTimespan(fBean, europeanaProxy,
						enrichedEntity);
			}
		}

	}

	private void appendTimespan(FullBean fBean, ProxyImpl europeanaProxy,
			RetrievedEntity enrichedEntity) throws JsonParseException,
			JsonMappingException, IOException {
		TimespanImpl ts =(TimespanImpl) enrichedEntity.getEntity();
		List<TimespanImpl> tsList = (List<TimespanImpl>) fBean.getTimespans();
		if (tsList == null) {
			tsList = new ArrayList<TimespanImpl>();
		}
		boolean isContained = false;
		for (TimespanImpl contained : tsList) {
			if (contained.getAbout().equals(ts.getAbout())) {
				isContained = true;
			}
		}
		if (!isContained) {
			tsList.add(ts);
		}
		fBean.setTimespans(tsList);
		if (enrichedEntity.getOriginalField() != null) {

			if (enrichedEntity.getOriginalField().equals(
					"proxy_dcterms_temporal")) {
				Map<String, List<String>> map = europeanaProxy
						.getDctermsTemporal();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(ts.getAbout())) {
					values.add(ts.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDctermsTemporal(map);
			} else if (enrichedEntity.getOriginalField()
					.equals("proxy_dc_date")) {
				Map<String, List<String>> map = europeanaProxy.getDcDate();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(ts.getAbout())) {
					values.add(ts.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDcDate(map);
			} 

		}

	}

	private void appendPlace(FullBean fBean,
			ProxyImpl europeanaProxy, RetrievedEntity enrichedEntity)
			throws JsonParseException, JsonMappingException, IOException {
		PlaceImpl place = (PlaceImpl)enrichedEntity.getEntity();

		List<PlaceImpl> placeList = (List<PlaceImpl>) fBean.getPlaces();
		if (placeList == null) {
			placeList = new ArrayList<PlaceImpl>();
		}
		boolean isContained = false;
		for (PlaceImpl contained : placeList) {
			if (contained.getAbout().equals(place.getAbout())) {
				isContained = true;
			}
		}
		if (!isContained) {
			placeList.add(place);
		}
		fBean.setPlaces(placeList);
		if (enrichedEntity.getOriginalField() != null) {

			if (enrichedEntity.getOriginalField().equals(
					"proxy_dcterms_spatial")) {
				Map<String, List<String>> map = europeanaProxy
						.getDctermsSpatial();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(place.getAbout())) {
					values.add(place.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDctermsSpatial(map);
			} else if (enrichedEntity.getOriginalField().equals(
					"proxy_dc_coverage")) {
				Map<String, List<String>> map = europeanaProxy.getDcCoverage();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(place.getAbout())) {
					values.add(place.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDcCoverage(map);
			}

		}

	}

	private void appendAgent( FullBean fBean,
			ProxyImpl europeanaProxy, RetrievedEntity enrichedEntity)
			throws JsonParseException, JsonMappingException, IOException {
		AgentImpl agent = (AgentImpl) enrichedEntity.getEntity();

		List<AgentImpl> agentList = (List<AgentImpl>) fBean.getAgents();
		if (agentList == null) {
			agentList = new ArrayList<AgentImpl>();
		}
		boolean isContained = false;
		for (AgentImpl contained : agentList) {
			if (contained.getAbout().equals(agent.getAbout())) {
				isContained = true;
			}
		}
		if (!isContained) {
			agentList.add(agent);
		}
		fBean.setAgents(agentList);
		if (enrichedEntity.getOriginalField() != null) {

			if (enrichedEntity.getOriginalField().equals("proxy_dc_creator")) {
				Map<String, List<String>> map = europeanaProxy.getDcCreator();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(agent.getAbout())) {
					values.add(agent.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDcCreator(map);
			} else if (enrichedEntity.getOriginalField().equals(
					"proxy_dc_contributor")) {
				Map<String, List<String>> map = europeanaProxy
						.getDcContributor();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(agent.getAbout())) {
					values.add(agent.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDcContributor(map);
			}

		}

	}

	private void appendConcept(FullBean fBean,
			ProxyImpl europeanaProxy, RetrievedEntity enrichedEntity)
			throws JsonParseException, JsonMappingException, IOException {
		ConceptImpl concept = (ConceptImpl)enrichedEntity.getEntity();

		List<ConceptImpl> conceptList = (List<ConceptImpl>) fBean.getConcepts();
		if (conceptList == null) {
			conceptList = new ArrayList<ConceptImpl>();
		}
		boolean isContained = false;
		for (ConceptImpl contained : conceptList) {
			if (contained.getAbout().equals(concept.getAbout())) {
				isContained = true;
			}
		}
		if (!isContained) {
			conceptList.add(concept);
		}
		fBean.setConcepts(conceptList);
		if (enrichedEntity.getOriginalField() != null) {

			if (enrichedEntity.getOriginalField().equals("proxy_dc_subject")) {
				Map<String, List<String>> map = europeanaProxy.getDcSubject();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(concept.getAbout())) {
					values.add(concept.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDcSubject(map);
			} else if (enrichedEntity.getOriginalField()
					.equals("proxy_dc_type")) {
				Map<String, List<String>> map = europeanaProxy.getDcType();
				List<String> values;
				if (map == null) {
					map = new HashMap<String, List<String>>();
					values = new ArrayList<String>();

				} else {
					values = map.get("def");
				}
				if (!values.contains(concept.getAbout())) {
					values.add(concept.getAbout());
				}
				map.put("def", values);
				europeanaProxy.setDcType(map);
			}

		}
	}

	/**
	 * Clean bean from old obsolete data
	 * @param fBean
	 */
	public void cleanFullBean(FullBeanImpl fBean) {
		ProxyImpl europeanaProxy = null;
		fBean = cleanEntitiesFromBean(fBean);
		int index = 0;
		for (ProxyImpl proxy : fBean.getProxies()) {
			if (proxy.isEuropeanaProxy()) {
				europeanaProxy = proxy;
				break;
			}
			index++;
		}

		europeanaProxy.setDcDate(null);
		europeanaProxy.setDcCoverage(null);
		europeanaProxy.setDctermsTemporal(null);
		europeanaProxy.setYear(null);
		europeanaProxy.setDctermsSpatial(null);
		europeanaProxy.setDcType(null);
		europeanaProxy.setDcSubject(null);
		europeanaProxy.setDcCreator(null);
		europeanaProxy.setDcContributor(null);

		fBean.getProxies().set(index, europeanaProxy);
	}

	/**
	 * Clean bean from Timespans, Agents and Places (before appending the enriched data)
	 * @param fBean
	 * @return
	 */
	private FullBeanImpl cleanEntitiesFromBean(FullBeanImpl fBean) {
		ProxyImpl europeanaProxy = null;
		for (ProxyImpl proxy : fBean.getProxies()) {
			if (proxy.isEuropeanaProxy()) {
				europeanaProxy = proxy;
				break;
			}
		}

		//*** TIMESPAN ***//
		List<TimespanImpl> cleanedTs = new ArrayList<>();
		List<TimespanImpl> timespans = fBean.getTimespans();
		if(timespans != null) {
			for (TimespanImpl ts : timespans) {
				if(!StringUtils.contains(ts.getAbout(),"semium")) {
					cleanedTs.add(ts);
				}
			}
		}
		fBean.setTimespans(cleanedTs);

		//*** CONCEPT ***//
		List<ConceptImpl> cleanedConcepts  = new ArrayList<>();
		List<ConceptImpl> concepts = fBean.getConcepts();
		if(concepts != null) {
			for(ConceptImpl concept : concepts) {
				if(!StringUtils.contains(concept.getAbout(),"eionet")) {
					cleanedConcepts.add(concept);
				}
			}
		}
		fBean.setConcepts(cleanedConcepts);
		//FIXME !!! Need Yorgos' code review! !!!
		//**********				AGENT				***********//
		//***			CREATOR				***//
		List<AgentImpl> beanAgents = fBean.getAgents();
		if (beanAgents != null) {
			List<AgentImpl> agents = new CopyOnWriteArrayList<>(beanAgents);
			Map<String,List<String>> creators = europeanaProxy.getDcCreator();
			if(creators != null) {
				for(String creator : creators.get("def")) {
					for(AgentImpl agent : agents){
						if (StringUtils.equals(creator, agent.getAbout())){
							agents.remove(agent);
						}
					}
				}
			}
			//***			CREATOR			***//
			Map<String,List<String>> contributors = europeanaProxy.getDcContributor();
			if(contributors != null) {
				for(String contributor : contributors.get("def")) {
					for(AgentImpl agent : agents) {
						if (StringUtils.equals(contributor, agent.getAbout())) {
							agents.remove(agent);
						}
					}
				}
			}
			fBean.setAgents(agents);
		}
		//**********		END OF AGENT			***********//

		//**********			PLACE				***********//
		Map<String,String> placeMap = new HashMap<>();
		List<PlaceImpl> beanPlaces = fBean.getPlaces();
//        Logger.getGlobal().info("*** Bean " + fBean.getAbout() + " has " + beanPlaces.size() + " places. ***");
		if(beanPlaces != null) {
			for(PlaceImpl place : beanPlaces) {
				if (place != null
						&& place.getAbout() != null
						&& place.getIsPartOf() != null
						&& place.getIsPartOf().get("def") != null
						&& !place.getIsPartOf().get("def").isEmpty()) {
					placeMap.put(place.getAbout(), place.getIsPartOf().get("def").get(0));
				} else {
//					Logger.getGlobal().info("!!! Place "
//									+ (place != null ? place.getAbout() : "")
//									+ " getIsPartOf is null or empty !!!");
				}
			}
		}
		if (beanPlaces != null) {
			Set<String> toRemove = new HashSet<>();

			//*** SPATIAL ***//
			Map<String,List<String>> spatial = europeanaProxy.getDctermsSpatial();
			if(spatial != null) {
				for(String sp : spatial.get("def")) {
					if (placeMap.containsKey(sp)) {
						toRemove.addAll(removeParent(sp, placeMap));
					}
				}
			}
			//*** COVERAGE ***//
			Map<String,List<String>> coverage = europeanaProxy.getDcCoverage();
			if(coverage != null) {
				for(String sp : coverage.get("def")) {
					if (placeMap.containsKey(sp)){
						toRemove.addAll(removeParent(sp, placeMap));
					}
				}
			}

			List<PlaceImpl> places = new CopyOnWriteArrayList<>(beanPlaces);
			for (String uri : toRemove) {
				for (PlaceImpl place : places){
					if (StringUtils.equals(place.getAbout(), uri)){
						places.remove(place);
					}
				}
			}
			fBean.setPlaces(places);
		}
		//**********			END OF PLACE			***********//
		return fBean;
	}

	private List<String> removeParent(String sp, Map<String, String> placeMap) {
		List<String> parents = new ArrayList<>();
		if(sp != null) {
			parents.add(sp);
			if (placeMap.get(sp) != null && !StringUtils.equals(sp, placeMap.get(sp))) {
//                Logger.getGlobal().severe(sp);
				parents.addAll(removeParent(placeMap.get(sp), placeMap));
			}
		}
		return parents;
	}

	public void appendEntities(FullBeanImpl fBean, String entityWrapper) {
		try {
			byte[] decoded = new org.apache.commons.codec.binary.Base64().decode(entityWrapper);
			ByteArrayInputStream byteInput = new ByteArrayInputStream(decoded);
			GZIPInputStream gZipInput = new GZIPInputStream(byteInput);
			EntityWrapperList readValue = om.readValue(gZipInput, EntityWrapperList.class);
			List<EntityWrapper> entities = readValue.getWrapperList();

//            List<EntityWrapper> entities = om.readValue(entityWrapper, EntityWrapperList.class).getWrapperList();
			List<RetrievedEntity> enriched = convertToObjects(entities);
			ProxyImpl europeanaProxy = null;
			int index = 0;
			for (ProxyImpl proxy : fBean.getProxies()) {
				if (proxy.isEuropeanaProxy()) {
					europeanaProxy = proxy;
					break;
				}
				index++;
			}
			addEntities(fBean, europeanaProxy, enriched);
			fBean.getProxies().set(index, europeanaProxy);
		} catch (IOException ex) {
			Logger.getLogger(EntityAppender.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private List<RetrievedEntity> convertToObjects(
			List<EntityWrapper> enrichments) throws IOException {
		List<RetrievedEntity> entities = new ArrayList<>();
		for (EntityWrapper entity : enrichments) {
			RetrievedEntity ret = new RetrievedEntity();
			ret.setOriginalField(entity.getOriginalField());
			ret.setOriginalLabel(entity.getOriginalValue());
			ret.setUri(entity.getUrl());
			if (entity.getClassName().equals(TimespanImpl.class.getName())) {
				ret.setEntity(om.readValue(entity.getContextualEntity(), TimespanImpl.class));
			} else if (entity.getClassName().equals(AgentImpl.class.getName())) {
				ret.setEntity(om.readValue(entity.getContextualEntity(), AgentImpl.class));
			} else if (entity.getClassName().equals(ConceptImpl.class.getName())) {
				ret.setEntity(om.readValue(entity.getContextualEntity(), ConceptImpl.class));
			} else {
				ret.setEntity(om.readValue(entity.getContextualEntity(), PlaceImpl.class));
			}
			entities.add(ret);
		}
		return entities;
	}
}

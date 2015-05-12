/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.enrichment;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 *
 * @author ymamakis
 */
public class EntityAppender {
    public void addEntities(FullBean fBean,
			ProxyImpl europeanaProxy, List<RetrievedEntity> enrichedEntities)
			throws JsonParseException, JsonMappingException, IOException {

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
//		new PlaceSolrCreator().create(basicDocument, place);

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
}

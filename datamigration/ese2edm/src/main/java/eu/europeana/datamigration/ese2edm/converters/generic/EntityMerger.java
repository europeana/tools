package eu.europeana.datamigration.ese2edm.converters.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.converters.europeana.Entity;
import eu.annocultor.converters.europeana.Field;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.definitions.solr.entity.Agent;
import eu.europeana.corelib.definitions.solr.entity.Concept;
import eu.europeana.corelib.definitions.solr.entity.Place;
import eu.europeana.corelib.definitions.solr.entity.Proxy;
import eu.europeana.corelib.definitions.solr.entity.Timespan;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.datamigration.ese2edm.enums.AgentMethods;
import eu.europeana.datamigration.ese2edm.enums.ConceptMethods;
import eu.europeana.datamigration.ese2edm.enums.OriginalField;
import eu.europeana.datamigration.ese2edm.enums.PlaceMethods;
import eu.europeana.datamigration.ese2edm.enums.TimespanMethods;

public class EntityMerger {
	public FullBeanImpl mergeEntities(List<Entity> entities, FullBeanImpl fullBean,
			SolrInputDocument inputDocument) {
		Proxy europeanaProxy = null;
		List<Concept> concepts = new ArrayList<Concept>();
		List<Agent> agents = new ArrayList<Agent>();
		List<Timespan> timespans = new ArrayList<Timespan>();
		List<Place> places = new ArrayList<Place>();
		for (Proxy proxy : fullBean.getProxies()) {
			if (proxy.isEuropeanaProxy()) {
				europeanaProxy = proxy;
				break;
			}
		}
		// First Concepts
		for (Entity entity : entities) {
			if (StringUtils.equals(entity.getClassName(), "Concept")) {
				Concept concept = new ConceptImpl();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"skos_concept")) {
							concept.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							// addToHasMetList(
							// europeanaProxy,
							// field.getValues()
							// .get(field.getValues().keySet()
							// .iterator().next()).get(0));
							inputDocument.addField(
									EdmLabel.SKOS_CONCEPT.toString(),
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
							OriginalField or = OriginalField
									.getOriginalField(entity.getOriginalField());
							if (or != null) {
								europeanaProxy = or.appendField(europeanaProxy,
										concept.getAbout());
							}
						} else {
							if (field.getValues() != null) {
								for (Entry<String, List<String>> entry : field
										.getValues().entrySet()) {
									for (String str : entry.getValue()) {
										ConceptMethods.getMethod(
												field.getName()).appendValue(
												concept, entry.getKey(), str);
										inputDocument
												.addField(
														EdmLabel.getEdmLabel(
																field.getName())
																.toString()
																+ (ConceptMethods
																		.getMethod(
																				field.getName())
																		.isMultiLingual() ? "."
																		+ entry.getKey()
																		: ""),
														str);
									}
								}
							}
						}

					}
				}
				if (concept.getAbout() != null) {
					concepts.add(concept);
				}
				// Then timespans
			} else if (StringUtils.equals(entity.getClassName(), "Timespan")) {
				Timespan ts = new TimespanImpl();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"edm_timespan")) {
							ts.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							// addToHasMetList(
							// europeanaProxy,
							// field.getValues()
							// .get(field.getValues().keySet()
							// .iterator().next()).get(0));
							inputDocument.addField(
									EdmLabel.EDM_TIMESPAN.toString(),
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
							OriginalField or = OriginalField
									.getOriginalField(entity.getOriginalField());
							if (or != null) {
								europeanaProxy = or.appendField(europeanaProxy,
										ts.getAbout());
							}
						} else {
							if (field.getValues() != null) {
								for (Entry<String, List<String>> entry : field
										.getValues().entrySet()) {
									for (String str : entry.getValue()) {
										TimespanMethods.getMethod(
												field.getName()).appendValue(
												ts, entry.getKey(), str);
										inputDocument
												.addField(
														EdmLabel.getEdmLabel(
																field.getName())
																.toString()
																+ (TimespanMethods
																		.getMethod(
																				field.getName())
																		.isMultilingual() ? "."
																		+ entry.getKey()
																		: ""),
														str);
									}
								}
							}
						}

					}
				}
				if (ts.getAbout() != null) {
					timespans.add(ts);
				}
				// then agents
			} else if (StringUtils.equals(entity.getClassName(), "Agent")) {
				Agent ts = new AgentImpl();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"edm_agent")) {
							ts.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							// addToHasMetList(
							// europeanaProxy,
							// field.getValues()
							// .get(field.getValues().keySet()
							// .iterator().next()).get(0));
							inputDocument.addField(
									EdmLabel.EDM_AGENT.toString(),
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
							OriginalField or = OriginalField
									.getOriginalField(entity.getOriginalField());
							if (or != null) {
								europeanaProxy = or.appendField(europeanaProxy,
										ts.getAbout());
							}
						} else {
							if (field.getValues() != null) {

								for (Entry<String, List<String>> entry : field
										.getValues().entrySet()) {
									if (entry.getKey() != null) {
										for (String str : entry.getValue()) {

											AgentMethods
													.getMethod(field.getName())
													.appendValue(ts,
															entry.getKey(), str);
											inputDocument
													.addField(
															EdmLabel.getEdmLabel(
																	field.getName())
																	.toString()
																	+ (AgentMethods
																			.getMethod(
																					field.getName())
																			.isMultilingual() ? "."
																			+ entry.getKey()
																			: ""),
															str);
										}
									}
								}
							}
						}

					}
				}
				if (ts.getAbout() != null) {
					agents.add(ts);
				}
				// then places
			} else if (StringUtils.equals(entity.getClassName(), "Place")) {
				Place ts = new PlaceImpl();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"edm_place")) {
							ts.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							// addToHasMetList(
							// europeanaProxy,
							// field.getValues()
							// .get(field.getValues().keySet()
							// .iterator().next()).get(0));
							inputDocument.addField(
									EdmLabel.EDM_PLACE.toString(),
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
							OriginalField or = OriginalField
									.getOriginalField(entity.getOriginalField());
							if (or != null) {
								europeanaProxy = or.appendField(europeanaProxy,
										ts.getAbout());
							}
						} else {
							if (field.getValues() != null) {
								for (Entry<String, List<String>> entry : field
										.getValues().entrySet()) {
									for (String str : entry.getValue()) {
										PlaceMethods.getMethod(field.getName())
												.appendValue(ts,
														entry.getKey(), str);
										inputDocument
												.addField(
														EdmLabel.getEdmLabel(
																field.getName())
																.toString()
																+ (PlaceMethods
																		.getMethod(
																				field.getName())
																		.isMultilingual() ? "."
																		+ entry.getKey()
																		: ""),
														str);
									}
								}
							}
						}

					}
				}
				if (ts.getAbout() != null) {
					places.add(ts);
				}
			}
		}
		// Append everything
		if (fullBean.getAgents() != null) {
			agents.addAll(fullBean.getAgents());
		}
		if (fullBean.getConcepts() != null) {
			concepts.addAll(fullBean.getConcepts());
		}
		if (fullBean.getPlaces() != null) {
			places.addAll(fullBean.getPlaces());
		}
		if (fullBean.getTimespans() != null) {
			timespans.addAll(fullBean.getTimespans());
		}

		// Add to Fullbean
		fullBean.setConcepts(concepts);
		fullBean.setAgents(agents);
		fullBean.setPlaces(places);
		fullBean.setTimespans(timespans);
		return fullBean;
	}
}

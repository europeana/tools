package eu.europeana.enrichment.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.europeana.enrichment.api.external.*;
import eu.europeana.enrichment.service.EntityRemover;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.service.Enricher;

@Path("/")
@Component
@Scope("request")
public class EnrichmentResource {
	Logger log = Logger.getLogger(this.getClass().getName());
	private static Enricher enricher = new Enricher("");
	static {
		try {
			enricher.init("Europeana");
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response delete(@FormParam("urls") String input) throws JsonParseException,
            JsonMappingException, IOException, Exception {
        ObjectMapper mapper = new ObjectMapper();
        UriList values = mapper.readValue(input, UriList.class);
        EntityRemover remover = new EntityRemover(enricher.getEnricher());
        remover.remove(values.getUris(), null);
        return Response.ok().build();
    }

    @POST
    @Path("enrich")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response enrich(@FormParam("input") String input,
            @FormParam("toXml") String toXML) throws JsonParseException,
            JsonMappingException, IOException, Exception {
        try{
			ObjectMapper mapper = new ObjectMapper();
			InputValueList values = mapper.readValue(input,
					InputValueList.class);
			EntityWrapperList response = new EntityWrapperList();
			List<EntityWrapper> wrapperList = enricher.tagExternal(values
					.getInputValueList());

			ObjectMapper objIdMapper = new ObjectMapper();
			if (!Boolean.parseBoolean(toXML)) {
				SimpleModule sm = new SimpleModule("objId",
						Version.unknownVersion());
				sm.addSerializer(new ObjectIdSerializer());
				objIdMapper.registerModule(sm);
				response.setWrapperList(wrapperList);
			} else {

				response.setWrapperList(convertToXml(wrapperList));
			}

			log.info(objIdMapper.writeValueAsString(response));
			return Response.ok()
					.entity(objIdMapper.writeValueAsString(response)).build();

		} catch (JsonParseException e) {
			log.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		} catch (JsonMappingException e) {
			log.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		} catch (IOException e) {
			log.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		} catch (Exception e) {
			log.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}

	}

	private List<EntityWrapper> convertToXml(List<EntityWrapper> wrapperList)
			throws JsonParseException, JsonMappingException, IOException {
		List<EntityWrapper> entityWrapperList = new ArrayList<EntityWrapper>();
		for (EntityWrapper wrapper : wrapperList) {
			entityWrapperList.add(new EntityWrapper(wrapper.getClassName(),
					wrapper.getOriginalField(),wrapper.getUrl(),wrapper.getOriginalValue(), convertEntity(wrapper)));
		}
		return entityWrapperList;
	}

	private String convertEntity(EntityWrapper wrapper)
			throws JsonParseException, JsonMappingException, IOException {
		if (wrapper.getClassName().equals(ConceptImpl.class.getName())) {
			return convertConcept(wrapper.getContextualEntity());
		} else if (wrapper.getClassName().equals(AgentImpl.class.getName())) {
			return convertAgent(wrapper.getContextualEntity());
		} else if (wrapper.getClassName().equals(PlaceImpl.class.getName())) {
			return convertPlace(wrapper.getContextualEntity());
		} else {
			return convertTimespan(wrapper.getContextualEntity());
		}
	}

	private String convertTimespan(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		TimespanImpl ts = new ObjectMapper().readValue(contextualEntity,
				TimespanImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<edm:Timespan rdf:about=\"");
		sb.append(ts.getAbout());
		sb.append("\">\n");
		addMap(sb, ts.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, ts.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, ts.getBegin(), "edm:begin", "xml:lang", false);
		addMap(sb, ts.getEnd(), "edm:end", "xml:lang", false);
		addMap(sb, ts.getDctermsHasPart(), "dcterms:hasPart", "rdf:resource",
				true);
		addMap(sb, ts.getHiddenLabel(), "skos:hiddenLabel", "xml:lang", false);
		addMap(sb, ts.getIsPartOf(), "dcterms:isPartOf", "rdf:resource", true);
		addMap(sb, ts.getNote(), "skos:note", "xml:lang", false);
		addArray(sb, ts.getOwlSameAs(), "owl:sameAs", "rdf:resource");
		sb.append("</edm:Timespan>");
		log.info(StringEscapeUtils.escapeXml(sb.toString()));

		return StringEscapeUtils.escapeHtml3(sb.toString());
	}

	private void addArray(StringBuilder sb, String[] arrValues, String element,
			String attribute) {
		if (arrValues != null) {
			for (String str : arrValues) {
				sb.append("<");
				sb.append(element);
				sb.append(" ");
				sb.append(attribute);
				sb.append("\"=");
				sb.append(str);
				sb.append("\"/>\n");
			}
		}
	}

	private void addMap(StringBuilder sb, Map<String, List<String>> values,
			String elementName, String attributeName, boolean isResource) {
		if (values != null) {
			for (Entry<String, List<String>> entry : values.entrySet()) {
				for (String str : entry.getValue()) {
					sb.append("<");
					sb.append(elementName);
					sb.append(" ");
					sb.append(attributeName);
					sb.append("=\"");
					if (!isResource) {
						sb.append(entry.getKey());
						sb.append("\">");
						sb.append(str);
						sb.append("</");
						sb.append(elementName);
						sb.append(">\n");
					} else {
						sb.append(str);
						sb.append("\"/>\n");
					}
				}
			}
		}
	}

	private String convertPlace(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		PlaceImpl place = new ObjectMapper().readValue(contextualEntity,
				PlaceImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<edm:Place rdf:about=\"");
		sb.append(place.getAbout());
		sb.append("\">\n");
		addMap(sb, place.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, place.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, place.getDcTermsHasPart(), "dcterms:hasPart",
				"rdf:resource", true);
		addMap(sb, place.getIsPartOf(), "dcterms:isPartOf", "rdf:resource",
				true);
		addMap(sb, place.getNote(), "skos:note", "xml:lang", false);
		addArray(sb, place.getOwlSameAs(), "owl:sameAs", "rdf:resource");
		if ((place.getLatitude()!=null&& place.getLatitude()!= 0) && (place.getLongitude()!=null && place.getLongitude() != 0)) {
			sb.append("<wgs84_pos:long>");
			sb.append(place.getLongitude());
			sb.append("</wgs84_pos:long>\n");
			sb.append("<wgs84_pos:lat>");
			sb.append(place.getLatitude());
			sb.append("</wgs84_pos:lat>\n");
		}
		if (place.getAltitude()!=null && place.getAltitude()!= 0) {
			sb.append("<wgs84_pos:alt>");
			sb.append(place.getAltitude());
			sb.append("</wgs84_pos:alt>\n");
		}
		sb.append("</edm:Place>\n");
		log.info(StringEscapeUtils.escapeXml(sb.toString()));
		return StringEscapeUtils.escapeHtml3(sb.toString());
	}

	private String convertAgent(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		AgentImpl agent = new ObjectMapper().readValue(contextualEntity,
				AgentImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<edm:Agent rdf:about=\"");
		sb.append(agent.getAbout());
		sb.append("\">");
		addMap(sb, agent.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, agent.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, agent.getHiddenLabel(), "skos:hiddenLabel", "xml:lang",
				false);
		addMap(sb, agent.getFoafName(), "foaf:name", "xml:lang", false);
		addMap(sb, agent.getNote(), "skos:note", "xml:lang", false);
		addMap(sb, agent.getBegin(), "edm:begin", "xml:lang", false);
		addMap(sb, agent.getEnd(), "edm:end", "xml:lang", false);
		addMap(sb, agent.getDcIdentifier(), "dc:identifier", "xml:lang", false);
		addMap(sb, agent.getEdmHasMet(), "edm:hasMet", "xml:lang", false);
		addMap(sb, agent.getDcIdentifier(), "dc:identifier", "xml:lang", false);
		addMap(sb, agent.getRdaGr2BiographicalInformation(),
				"rdaGr2:biographicaInformation", "xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfBirth(), "rdaGr2:dateOfBirth",
				"xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfDeath(), "rdaGr2:dateOfDeath",
				"xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfEstablishment(),
				"rdaGr2:dateOfEstablishment", "xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfTermination(),
				"rdaGr2:dateOfTermination", "xml:lang", false);
		addMap(sb, agent.getRdaGr2Gender(), "rdaGr2:gender", "xml:lang", false);
		addMapResourceOrLiteral(sb, agent.getDcDate(), "dc:date");
		addMapResourceOrLiteral(sb, agent.getEdmIsRelatedTo(),
				"edm:isRelatedTo");
		addMapResourceOrLiteral(sb, agent.getRdaGr2ProfessionOrOccupation(),
				"rdaGr2:professionOrOccupation");
		addArray(sb, agent.getEdmWasPresentAt(), "edm:wasPresentAt",
				"rdf:resource");
		addArray(sb, agent.getOwlSameAs(), "owl:sameAs", "rdf:resource");
		sb.append("</edm:Agent>\n");
		log.info(StringEscapeUtils.escapeXml(sb.toString()));
		return StringEscapeUtils.escapeHtml3(sb.toString());
	}

	private void addMapResourceOrLiteral(StringBuilder sb,
			Map<String, List<String>> values, String element) {

		if (values != null) {
			for (Entry<String, List<String>> entry : values.entrySet()) {
				for (String str : entry.getValue()) {
					sb.append("<");
					sb.append(element);
					sb.append(" ");
					if(isUri(str)){
						sb.append("rdf:resource=\"");
						sb.append(str);
						sb.append("\"/>\n");
					} else {
						sb.append("xml:lang=\"");
						sb.append(entry.getKey());
						sb.append("\">");
						sb.append(str);
						sb.append("</");
						sb.append(element);
						sb.append(">\n");
					}
				}
			}
		}
	}

	private boolean isUri(String str) {
		return str.startsWith("http://");
	}

	private String convertConcept(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		ConceptImpl concept = new ObjectMapper().readValue(contextualEntity,
				ConceptImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<skos:Concept rdf:about=\"");
		sb.append(concept.getAbout());
		sb.append("\"/>\n");
		addMap(sb, concept.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, concept.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, concept.getHiddenLabel(), "skos:hiddenLabel", "xml:lang",
				false);
		addMap(sb, concept.getNotation(), "skos:notation", "xml:lang", false);
		addMap(sb, concept.getNote(), "skos:note", "xml:lang", false);
		addArray(sb, concept.getBroader(), "skos:broader", "rdf:resource");
		addArray(sb, concept.getBroadMatch(), "skos:broadMatch", "rdf:resource");
		addArray(sb, concept.getCloseMatch(), "skos:closeMatch", "rdf:resource");
		addArray(sb, concept.getExactMatch(), "skos:exactMatch", "rdf:resource");
		addArray(sb, concept.getInScheme(), "skos:inScheme", "rdf:resource");
		addArray(sb, concept.getNarrower(), "skos:narrower", "rdf:resource");
		addArray(sb, concept.getNarrowMatch(), "skos:narrowMatch",
				"rdf:resource");
		addArray(sb, concept.getRelated(), "skos:related", "rdf:resource");
		addArray(sb, concept.getRelatedMatch(), "skos:relatedMatch",
				"rdf:resource");
		sb.append("</skos:Concept>\n");

		log.info(StringEscapeUtils.escapeXml(sb.toString()));
		return StringEscapeUtils.escapeHtml3(sb.toString());
	}
}

package eu.europeana.enrichment.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.external.UriList;
import eu.europeana.enrichment.converters.ContextualEntityToXmlConverter;
import eu.europeana.enrichment.service.Enricher;
import eu.europeana.enrichment.service.EntityRemover;

@Path("/")
@Component
@Scope("request")
public class EnrichmentResource {

    Logger log = Logger.getLogger(this.getClass().getCanonicalName());

    @Autowired
    private Enricher enricher;

    public EnrichmentResource() {
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response delete(@FormParam("urls") String input) throws JsonParseException,
            JsonMappingException, IOException, Exception {
        ObjectMapper mapper = new ObjectMapper();
        UriList values = mapper.readValue(input, UriList.class);
        EntityRemover remover = new EntityRemover();
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

        enricher.init("Europeana");
        ObjectMapper mapper = new ObjectMapper();
        InputValueList values = mapper.readValue(input, InputValueList.class);
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
        String stringResponse = objIdMapper.writeValueAsString(response);
        log.info(stringResponse);
        return Response.ok().entity(stringResponse).build();

    }

    private List<EntityWrapper> convertToXml(List<EntityWrapper> wrapperList)
            throws JsonParseException, JsonMappingException, IOException {
        List<EntityWrapper> entityWrapperList = new ArrayList<EntityWrapper>();
        for (EntityWrapper wrapper : wrapperList) {
            entityWrapperList.add(new EntityWrapper(wrapper.getClassName(),
                    wrapper.getOriginalField(), wrapper.getUrl(), wrapper
                    .getOriginalValue(), convertEntity(wrapper)));
        }
        return entityWrapperList;
    }

    private String convertEntity(EntityWrapper wrapper)
            throws JsonParseException, JsonMappingException, IOException {
        if (wrapper.getClassName().equals(ConceptImpl.class.getName())) {
            ;
            return new ContextualEntityToXmlConverter()
                    .convertConcept(new ObjectMapper().readValue(
                                    wrapper.getContextualEntity(), ConceptImpl.class));
        } else if (wrapper.getClassName().equals(AgentImpl.class.getName())) {
            return new ContextualEntityToXmlConverter()
                    .convertAgent(new ObjectMapper().readValue(
                                    wrapper.getContextualEntity(), AgentImpl.class));
        } else if (wrapper.getClassName().equals(PlaceImpl.class.getName())) {
            return new ContextualEntityToXmlConverter()
                    .convertPlace(new ObjectMapper().readValue(
                                    wrapper.getContextualEntity(), PlaceImpl.class));
        } else {
            return new ContextualEntityToXmlConverter()
                    .convertTimespan(new ObjectMapper().readValue(
                                    wrapper.getContextualEntity(), TimespanImpl.class));
        }
    }

}

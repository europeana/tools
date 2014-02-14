package eu.europeana.enrichment.rest;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.europeana.corelib.logging.Logger;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.service.Enricher;

@Path("/")
@Component
@Scope("request")
public class EnrichmentResource {
	Logger log = Logger.getLogger(this.getClass());
	private static Enricher enricher = new Enricher();
	static {
		try {
			enricher.init("Europeana");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@POST
	@Path("enrich")
	 @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response enrich(@FormParam("input") String input) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			InputValueList values = mapper.readValue(input, InputValueList.class);
			EntityWrapperList response = new EntityWrapperList();
			response.setWrapperList(enricher.tagExternal(values.getInputValueList()));
			ObjectMapper objIdMapper = new ObjectMapper();
			SimpleModule sm = new SimpleModule("objId", Version.unknownVersion());
			sm.addSerializer(new ObjectIdSerializer());
			objIdMapper.registerModule(sm);
			log.info(objIdMapper.writeValueAsString(response));
			return Response.ok()
					.entity(objIdMapper.writeValueAsString(response))
					.build();
			
			
		} catch (JsonParseException e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (JsonMappingException e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (IOException e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

	}
}

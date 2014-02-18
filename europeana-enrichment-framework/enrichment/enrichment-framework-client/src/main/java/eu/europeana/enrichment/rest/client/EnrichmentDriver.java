package eu.europeana.enrichment.rest.client;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.api.external.InputValueList;

public class EnrichmentDriver {

	JerseyClient client = JerseyClientBuilder.createClient();

	public List<EntityWrapper> enrich(String path, List<InputValue> values, boolean toEdm)
			throws JsonGenerationException, JsonMappingException, IOException {
		InputValueList inList = new InputValueList();
		inList.setInputValueList(values);
		Form form = new Form();
		form.param("input", new ObjectMapper().writeValueAsString(inList));
		form.param("toXml", Boolean.toString(toEdm));
		Response res = client
				.target(path)
				.request()
				.post(Entity
						.entity(form, MediaType.APPLICATION_FORM_URLENCODED),
						Response.class);
		return new ObjectMapper().readValue(res.readEntity(String.class),
				EntityWrapperList.class).getWrapperList();
	}
}

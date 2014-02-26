package eu.europeana.enrichment.rest.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.EnrichmentError;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.api.external.InputValueList;

/**
 * REST API wrapper class abstracting the REST calls and providing a clean POJO
 * implementation
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
public class EnrichmentDriver {

	JerseyClient client = JerseyClientBuilder.createClient();

	/**
	 * Enrich REST call invocation
	 * 
	 * @param path
	 *            The path the REST service is deployed
	 * @param values
	 *            The values to be enriched
	 * @param toEdm
	 *            Whether the enrichments should be retrieved in JSON (parsable
	 *            to POJO through Jackson) or XML (for copy pasting)
	 * @return The enrichments generated for the input values
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public List<EntityWrapper> enrich(String path, List<InputValue> values,
			boolean toEdm) throws JsonGenerationException,
			JsonMappingException, IOException, UnknownException {
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
		if (res.getStatus() == Status.OK.getStatusCode()) {
			return new ObjectMapper().readValue(res.readEntity(String.class),
					EntityWrapperList.class).getWrapperList();
		} else {
			EnrichmentError error = res.readEntity(EnrichmentError.class);
			Class<? extends Exception> e = ExceptionGenerator.exceptions
					.get(error.getCause());

			try {
				Constructor<? extends Exception> c = e
						.getConstructor(String.class);
				throw c.newInstance(error.getDetails());
			} catch (SecurityException e1) {
				throw new UnknownException(e1.getMessage());
			} catch (NoSuchMethodException e1) {
				throw new UnknownException(e1.getMessage());
			} catch (IllegalArgumentException e1) {
				throw new UnknownException(e1.getMessage());
			} catch (Exception e1) {
				throw new UnknownException(e1.getMessage());
			}

		}
	}

	private static class ExceptionGenerator {
		static Map<String, Class<? extends Exception>> exceptions = new HashMap<String, Class<? extends Exception>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 329292412348055056L;

			{
				put(JsonMappingException.class.getName(),
						JsonMappingException.class);
				put(JsonGenerationException.class.getName(),
						JsonGenerationException.class);
				put(UnknownException.class.getName(), UnknownException.class);
				put(IOException.class.getName(), IOException.class);
			}
		};
	}
}

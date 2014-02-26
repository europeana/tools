package eu.europeana.enrichment.rest.exception;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.web.EnrichmentError;

/**
 * Class that holds the implementation of the toResponse methods of each
 * exception mapper
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 * 
 */
public class GenericExceptionMapper {
	private static Logger log = Logger.getLogger(GenericExceptionMapper.class
			.getName());

	/**
	 * Called when a JsonGenerationException occurs
	 * @param exception
	 * @return
	 */
	public Response toResponse(JsonGenerationException exception) {

		return generateResponse(exception, exception.getClass().getSimpleName()
				+ ": " + exception.getMessage());
	}

	/**
	 * Called when a JsonMappingException occurs
	 * @param exception
	 * @return
	 */
	public Response toResponse(JsonMappingException exception) {

		return generateResponse(exception, exception.getClass().getSimpleName()
				+ ": " + exception.getMessage());
	}
	/**
	 * Called when a IOException occurs
	 * @param exception
	 * @return
	 */
	public Response toResponse(IOException exception) {

		return generateResponse(exception, exception.getClass().getSimpleName()
				+ ": " + exception.getMessage());
	}

	/**
	 * Called when a UnknownException occurs
	 * @param exception
	 * @return
	 */
	public Response toResponse(UnknownException exception) {

		return generateResponse(exception, exception.getClass().getSimpleName()
				+ ": " + exception.getMessage());
	}

	private Response generateResponse(Exception e, String txt) {
		log.severe(e.getMessage());
		return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(new EnrichmentError(e.getClass().getName(), txt))
				.build();
	}
}

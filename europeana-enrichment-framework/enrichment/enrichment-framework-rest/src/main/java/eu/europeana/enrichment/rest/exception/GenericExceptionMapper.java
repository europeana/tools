package eu.europeana.enrichment.rest.exception;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.EnrichmentError;

public class GenericExceptionMapper {

	public Response toResponse(JsonGenerationException exception) {

		return generateResponse(exception, exception.getClass().getSimpleName()+": " +exception.getMessage());
	}

	public Response toResponse(JsonMappingException exception) {

		return generateResponse(exception,exception.getClass().getSimpleName()+": " +exception.getMessage());
	}

	public Response toResponse(IOException exception) {

		return generateResponse(exception,exception.getClass().getSimpleName()+": " +exception.getMessage());
	}

	public Response toResponse(UnknownException exception) {

		return generateResponse(exception,exception.getClass().getSimpleName()+": " +exception.getMessage());
	}

	private Response generateResponse(Exception e, String txt) {
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new EnrichmentError(e.getClass().getName(), txt))
				.build();
	}
}

package eu.europeana.enrichment.rest.exception;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonGenerationException;
@Provider
public class JsonGenerationExceptionMapper extends GenericExceptionMapper
		implements ExceptionMapper<JsonGenerationException> {

	
}

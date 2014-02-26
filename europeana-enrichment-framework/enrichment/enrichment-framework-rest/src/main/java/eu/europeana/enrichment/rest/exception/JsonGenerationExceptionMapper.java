package eu.europeana.enrichment.rest.exception;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonGenerationException;

import eu.europeana.enrichment.api.external.web.EnrichmentError;
/**
 * JsonGenerationException mapper holding a contract how these exceptions are going to
 * be cast to @see {@link EnrichmentError}
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@Provider
public class JsonGenerationExceptionMapper extends GenericExceptionMapper
		implements ExceptionMapper<JsonGenerationException> {

	
}

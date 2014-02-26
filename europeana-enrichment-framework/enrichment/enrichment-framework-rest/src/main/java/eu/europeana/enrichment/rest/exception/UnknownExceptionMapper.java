package eu.europeana.enrichment.rest.exception;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.web.EnrichmentError;

/**
 * UnknownException mapper holding a contract how these exceptions are going to
 * be cast to @see {@link EnrichmentError}
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@Provider
public class UnknownExceptionMapper extends GenericExceptionMapper implements
		ExceptionMapper<UnknownException> {

}

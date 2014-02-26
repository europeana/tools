package eu.europeana.enrichment.rest.exception;

import java.io.IOException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import eu.europeana.enrichment.api.external.web.EnrichmentError;
/**
 * IOException mapper holding a contract how these exceptions are going to
 * be cast to @see {@link EnrichmentError}
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@Provider
public class IOExceptionMapper extends GenericExceptionMapper implements
		ExceptionMapper<IOException> {

}

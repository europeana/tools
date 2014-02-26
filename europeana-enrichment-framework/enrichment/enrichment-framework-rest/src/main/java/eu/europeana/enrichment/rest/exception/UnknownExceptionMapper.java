package eu.europeana.enrichment.rest.exception;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import eu.europeana.enrichment.api.exceptions.UnknownException;
@Provider
public class UnknownExceptionMapper extends GenericExceptionMapper implements
		ExceptionMapper<UnknownException> {

}

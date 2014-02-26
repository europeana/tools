package eu.europeana.enrichment.rest.exception;

import java.io.IOException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
@Provider
public class IOExceptionMapper extends GenericExceptionMapper implements
		ExceptionMapper<IOException> {

}

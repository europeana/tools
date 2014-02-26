package eu.europeana.enrichment.rest.exception;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.JsonMappingException;
@Provider
public class JsonMappingExceptionMapper extends GenericExceptionMapper
		implements ExceptionMapper<JsonMappingException> {

}

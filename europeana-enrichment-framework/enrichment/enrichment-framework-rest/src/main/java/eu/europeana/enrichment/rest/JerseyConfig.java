package eu.europeana.enrichment.rest;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

import eu.europeana.enrichment.rest.exception.GenericExceptionMapper;
import eu.europeana.enrichment.rest.exception.IOExceptionMapper;
import eu.europeana.enrichment.rest.exception.JsonGenerationExceptionMapper;
import eu.europeana.enrichment.rest.exception.JsonMappingExceptionMapper;
import eu.europeana.enrichment.rest.exception.UnknownExceptionMapper;


public class JerseyConfig extends ResourceConfig {
	public JerseyConfig() {
		super();
		register(EnrichmentResource.class);
		register(IOExceptionMapper.class);
		register(JsonGenerationExceptionMapper.class);
		register(JsonMappingExceptionMapper.class);
		register(UnknownExceptionMapper.class);
		register(LoggingFilter.class);
	}
}

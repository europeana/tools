package eu.europeana.enrichment.rest;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

import eu.europeana.enrichment.rest.exception.IOExceptionMapper;
import eu.europeana.enrichment.rest.exception.JsonGenerationExceptionMapper;
import eu.europeana.enrichment.rest.exception.JsonMappingExceptionMapper;
import eu.europeana.enrichment.rest.exception.UnknownExceptionMapper;
import io.swagger.jaxrs.config.BeanConfig;

public class JerseyConfig extends ResourceConfig {
	public JerseyConfig() {
		super();
		register(EnrichmentResource.class);
		register(IOExceptionMapper.class);
		register(JsonGenerationExceptionMapper.class);
		register(JsonMappingExceptionMapper.class);
		register(UnknownExceptionMapper.class);
		register(LoggingFilter.class);
		register(io.swagger.jaxrs.listing.ApiListingResource.class);
		register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("1.0.2");
		beanConfig.setSchemes(new String[]{"http"});
		beanConfig.setHost("136.243.103.29:8080");
		beanConfig.setBasePath("/");
		beanConfig.setResourcePackage("eu.europeana.enrichment.rest");
		beanConfig.setScan(true);
	}
        
}

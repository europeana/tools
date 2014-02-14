package eu.europeana.enrichment.rest;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;


public class JerseyConfig extends ResourceConfig {
	public JerseyConfig() {
		super();
		register(EnrichmentResource.class);
		register(LoggingFilter.class);
	}
}

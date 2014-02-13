package eu.europeana.enrichment.model.external.api;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.ALWAYS)
public enum EntityClass {

	CONCEPT, TIMESPAN, AGENT, PLACE;
}

package eu.europeana.enrichment.api.external;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
@XmlRootElement
@JsonSerialize(include=Inclusion.ALWAYS)
public enum EntityClass {

	CONCEPT, TIMESPAN, AGENT, PLACE;
}

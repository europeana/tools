package eu.europeana.enrichment.model.external;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.ALWAYS)
public class EntityWrapper<T> {

	String originalField;
	T contextualEntity;
	
	public String getOriginalField() {
		return originalField;
	}
	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}
	public T getContextualEntity() {
		return contextualEntity;
	}
	public void setContextualEntity(T contextualEntity) {
		this.contextualEntity = contextualEntity;
	}
	
}

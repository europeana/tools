package eu.europeana.enrichment.api.external;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@XmlRootElement
@JsonSerialize(include=Inclusion.ALWAYS)
public class InputValue {
	
	private String originalField;
	
	private String value;
	
	private List<EntityClass> vocabularies;

	public String getOriginalField() {
		return originalField;
	}

	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<EntityClass> getVocabularies() {
		return vocabularies;
	}

	public void setVocabularies(List<EntityClass> vocabularies) {
		this.vocabularies = vocabularies;
	}
	
	
}

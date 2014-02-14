package eu.europeana.enrichment.api.external;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
@XmlRootElement
@JsonSerialize(include=Inclusion.ALWAYS)
public class EntityWrapper {

	public EntityWrapper(String className, String originalField,
			String contextualEntity) {
		this.className = className;
		this.originalField = originalField;
		this.contextualEntity = contextualEntity;
	}
	
	public EntityWrapper(){
		
	}
	@XmlElement
	String className;
	@XmlElement
	String originalField;
	@XmlElement
	String contextualEntity;
	
	public String getOriginalField() {
		return originalField;
	}
	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}
	public String  getContextualEntity(){
		return contextualEntity;
	}
	
	public  void setContextualEntity(String contextualEntity){
		this.contextualEntity = contextualEntity;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
}

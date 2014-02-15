package eu.europeana.enrichment.gui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EntityWrapperDTO implements IsSerializable {
	private String originalField;
	private String className;
	private String contextualEntity;
	public String getOriginalField() {
		return originalField;
	}
	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getContextualEntity() {
		return contextualEntity;
	}
	public void setContextualEntity(String contextualEntity) {
		this.contextualEntity = contextualEntity;
	}
}

package eu.annocultor.converters.europeana;

import java.util.List;
/**
 * A Contextual EDM Entity representation
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class Entity {
	String className;
	
	List<Field> fields;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
}

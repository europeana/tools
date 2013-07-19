package eu.annocultor.converters.europeana;

import java.util.List;
import java.util.Map;

/**
 * A field representation. 
 * name: the name of the field
 * values: the values of the field with language information if applicable 
 * @author gmamakis
 *
 */
public class Field {

	
	String name;
	
	Map<String,List <String>> values;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, List<String>> getValues() {
		return values;
	}

	public void setValues(Map<String, List<String>> values) {
		this.values = values;
	}
	
}

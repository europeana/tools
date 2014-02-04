package eu.europeana.enrichment.converters.europeana;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A field representation. 
 * name: the name of the field
 * values: the values of the field with language information if applicable 
 * @author Yorgos.Mamakis@ kb.nl
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
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{ field name: "+ this.name);
		for(Entry<String, List<String>> entry:values.entrySet()){
			sb.append(", lang: "+entry.getKey());
			for(String str: entry.getValue()){
				sb.append(", value: "+str); 
			}
		}
		return sb.toString();
	}
}

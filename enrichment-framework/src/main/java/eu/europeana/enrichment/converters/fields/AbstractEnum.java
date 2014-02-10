package eu.europeana.enrichment.converters.fields;

public interface AbstractEnum {
	abstract String getField();
	
	abstract boolean isMulti();
	
	abstract String getInputField();
}

package eu.europeana.enrichment.api.external;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize
@XmlRootElement
public class InputValueList {
	private List<InputValue> inputValueList;

	public List<InputValue> getInputValueList() {
		return inputValueList;
	}

	public void setInputValueList(List<InputValue> inputValueList) {
		this.inputValueList = inputValueList;
	}
	
}

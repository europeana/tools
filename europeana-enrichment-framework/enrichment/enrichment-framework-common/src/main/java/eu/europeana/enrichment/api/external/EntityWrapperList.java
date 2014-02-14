package eu.europeana.enrichment.api.external;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@XmlRootElement
@JsonSerialize
public class EntityWrapperList {

	@XmlElement(name = "entities")
	private List<EntityWrapper> wrapperList;
	

	public EntityWrapperList() {
	}

	public List<EntityWrapper> getWrapperList() {
		return wrapperList;
	}

	public void setWrapperList(List<EntityWrapper> wrapperList) {
		this.wrapperList = wrapperList;
	}


}

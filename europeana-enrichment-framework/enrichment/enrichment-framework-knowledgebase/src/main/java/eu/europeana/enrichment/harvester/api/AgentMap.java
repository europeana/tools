package eu.europeana.enrichment.harvester.api;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Property;
import java.net.URI;
import java.util.Date;

import net.vz.mongodb.jackson.Id;


@Entity
public class AgentMap {
	@Id
	private String id;
	private URI agentUri;
	@Property
	private String controlledSourceId;
	private Date storedDate;
	private Date harvestedDate;
	
	public  AgentMap(String id, URI uri, String sourceId, Date storedDate, Date harvestedDate){

		this.id = id;
		this.agentUri =uri;
		this.controlledSourceId = sourceId;
		this.storedDate = storedDate;
		this.harvestedDate = harvestedDate;
	}
	
	public AgentMap(){
		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public URI getAgentUri() {
		return agentUri;
	}
	public void setAgentUri(URI agentUri) {
		this.agentUri = agentUri;
	}
	public String getControlledSourceId() {
		return controlledSourceId;
	}
	public void setControlledSourceId(String controlledSourceId) {
		this.controlledSourceId = controlledSourceId;
	}
	public Date getStoredDate() {
		return storedDate;
	}
	public void setStoredDate(Date storedDate) {
		this.storedDate = storedDate;
	}
	public Date getHarvestedDate() {
		return harvestedDate;
	}
	public void setHarvestedDate(Date harvestedDate) {
		this.harvestedDate = harvestedDate;
	}
	
}

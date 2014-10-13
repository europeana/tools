package eu.europeana.enrichment.harvester.transform.edm.agent;

import java.util.HashMap;
import java.util.Map;

import eu.europeana.corelib.solr.entity.AgentImpl;

public final class AgentTemplate extends Template<AgentImpl>{
	
	private static AgentTemplate instance;
	private static Map<String,String> methodMapping;
	private AgentTemplate(){
		methodMapping = new HashMap<String,String>();
		methodMapping.put("edm:Agent", "setAbout");
		methodMapping.put("rdaGr2:biographicalInformation", "setRdaGr2BiographicalInformation");
		methodMapping.put("rdaGr2:dateOfBirth","setRdaGr2DateOfBirth");
		methodMapping.put("rdaGr2:placeOfBirth", "setRdaGr2PlaceOfBirth");
		methodMapping.put("rdaGr2:placeOfDeath", "setRdaGr2PlaceOfDeath");
		methodMapping.put("skos:prefLabel", "setPrefLabel");
		methodMapping.put("rdaGr2:dateOfDeath","setRdaGr2DateOfDeath");
		methodMapping.put("skos:altLabel", "setAltLabel");
		methodMapping.put("rdaGr2:professionOrOccupation","setRdaGr2ProfessionOrOccupation");
		methodMapping.put("edm:end", "setEnd");
		methodMapping.put("dc:identifier", "setDcIdentifier");
		methodMapping.put("owl:sameAs","setOwlSameAs");
		methodMapping.put("edm:isRelatedTo", "edmIsRelatedTo");
	}
	public AgentImpl transform(String xml, String resourceUri) {
		return parse(new AgentImpl(), resourceUri, xml, methodMapping);
	}

	public static AgentTemplate getInstance(){
		if (instance == null){
			instance = new AgentTemplate();
		}
		return instance;
	}
	
	
	
}

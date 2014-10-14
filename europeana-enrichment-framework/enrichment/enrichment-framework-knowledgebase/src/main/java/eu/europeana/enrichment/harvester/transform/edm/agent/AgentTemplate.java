package eu.europeana.enrichment.harvester.transform.edm.agent;

import java.util.Map;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.harvester.transform.CsvUtils;
import eu.europeana.enrichment.harvester.transform.Template;

public final class AgentTemplate extends Template<AgentImpl>{
	
	private static AgentTemplate instance;
	private static Map<String,String> methodMapping;
	private AgentTemplate(String filePath){
		methodMapping = CsvUtils.readFile(filePath);
		
	}
	public AgentImpl transform(String xml, String resourceUri) {
		return parse(new AgentImpl(), resourceUri, xml, methodMapping);
	}

	
	
	public static AgentTemplate getInstance(){
		if (instance == null){
			instance = new AgentTemplate("src/main/resources/agentMapping.csv");
		}
		return instance;
	}
	
	
	
}

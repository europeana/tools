package eu.europeana.enrichment.controlledsource;


import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;



import com.github.jsonldjava.jena.JenaRDFParser;

import eu.europeana.enrichment.controlledsource.util.DataManager;
import eu.europeana.enrichment.controlledsource.api.internal.AgentMap;

public class DbPediaAgentsCollector {

	 JenaRDFParser parser;
	 DataManager dm = new DataManager();
	 String agentQuery="SELECT * WHERE {?subject ?y <http://dbpedia.org/ontology/Artist>.}";
	 
	 static int qLimit= 100;
	 static int qOffset=0;
	 static boolean maxAgents=false;  //used for testing purposes, if true just qLimit agents are downloaded, use false to download all agents from dbpedia 
	 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DbPediaAgentsCollector dbpc= new DbPediaAgentsCollector();
		
		if (args!=null && args.length>1){
			try{
			qLimit = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException nfe){
				System.out.println ("*************WARNING in defining records limit in query answer, "+args[0]+" is not an int. Using default value: "+qLimit);
			}
		}
		
		dbpc.getDBPediaAgents(false); //get agents from dbpedia and store them locally, (parameter must always have false value, will fix it) ;

	}
	
	public DbPediaAgentsCollector(){
		parser = new JenaRDFParser();
	}
	
	
	private int loadAgentsfromDBPedia(String query, boolean harvestData){
		int i=0;
		
		try {
			Date todayDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			System.out.println(sdf.format(todayDate));
			//QueryEngineHTTP endpoint=new QueryEngineHTTP("http://dbpedia.org/sparql", "SELECT * WHERE {?subject ?y <http://dbpedia.org/ontology/Artist>.} LIMIT "+qLimit+" OFFSET 0");//100");
			QueryEngineHTTP endpoint=new QueryEngineHTTP("http://dbpedia.org/sparql", query);
			System.out.println("getting artists from DBPedia "+query);
			ResultSet rs= endpoint.execSelect();
			//System.out.println("exec query");
			
			while (rs.hasNext()){
				QuerySolution qs=rs.next();
				
				String subject=qs.get("subject").toString();
				
				AgentMap agentMap = new AgentMap(subject, new URI(subject), "DBPedia", todayDate, null);
				
				dm.insertAgentMap(agentMap);
				i= rs.getRowNumber();

				
			}
		 
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return i++;
		
	}
	/*
	 * Harvests agents from DBPedia. Related content can be also harvested if the parameter is true.
	 */
	public void getDBPediaAgents (boolean harvestContent)
	{
		int resultsize=qLimit;
		int limit=qLimit;
		int offset=qOffset;
		
		while (resultsize==limit){

			resultsize=loadAgentsfromDBPedia(agentQuery+" LIMIT "+limit+" OFFSET "+offset, harvestContent);
			if (resultsize == limit)
				offset=offset+limit;
			if (maxAgents)
				resultsize=0;
		}
		
	}
	
}

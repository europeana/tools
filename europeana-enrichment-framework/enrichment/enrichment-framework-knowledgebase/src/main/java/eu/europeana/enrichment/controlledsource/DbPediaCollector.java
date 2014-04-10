package eu.europeana.enrichment.controlledsource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;


import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.jena.JenaJSONLD;
import com.github.jsonldjava.jena.JenaRDFParser;

import eu.europeana.enrichment.controlledsource.util.DataManager;
import eu.europeana.corelib.solr.entity.AgentImpl;

public class DbPediaCollector {

	 JenaRDFParser parser;
	 DataManager dm = new DataManager();
	 String agentKey="";
	 static boolean nolog=true; //use 'false' if you want to see logs on console. WILL replace this with some log frameworks
	 static int qLimit=1000;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DbPediaCollector dbpc= new DbPediaCollector();
		
		if (args!=null && args.length>0){
			try{
			qLimit = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException nfe){
				System.out.println ("*************WARNING in defining records limit in query answer, "+args[0]+" is not an int. Using default value: "+qLimit);
			}
		}
		dbpc.loadAgentsfromDBPedia();
		
		//JenaJSONLD.init(); 
		//dbpc.test();

	}
	public DbPediaCollector(){
		parser = new JenaRDFParser();
	}
	
	private void loadAgentsfromFile(){
		File agentsRDF = new File("/Users/cesare/git/annocultor/annocultor/src/main/resources/dbpediaselectedartists.rdf");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(agentsRDF);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("rdf:Description");

			for (int temp = 0; temp < nList.getLength(); temp++){
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap nnm= nNode.getAttributes();
					Node no= nnm.getNamedItem("rdf:about");

					System.out.println("----"+no.getNodeValue()+"---");

					collectData(no.getNodeValue(), "rdf");
				}
				
			}
		 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	 
	}
	
	private void loadAgentsfromDBPedia(){
		
		try {
			QueryEngineHTTP endpoint=new QueryEngineHTTP("http://dbpedia.org/sparql", "SELECT * WHERE {?subject ?y <http://dbpedia.org/ontology/Artist>.} LIMIT "+qLimit);//100");
			System.out.println("getting artists from DBPedia ");
			ResultSet rs= endpoint.execSelect();
			//System.out.println("exec query");
			while (rs.hasNext()){
				QuerySolution qs=rs.next();
				
				String subject=qs.get("subject").toString();
				
				collectAndMapControlledData(subject);
			}
		 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void test(){
		this.collectAndMapControlledData("http://dbpedia.org/resource/Leah_Goldberg");
	}
	private void collectAndMapControlledData(String key){


		

		QueryEngineHTTP endpoint=new QueryEngineHTTP("http://dbpedia.org/sparql", "describe <"+key+">");
		System.out.println("describing "+key);
		agentKey=key;
		Model model=endpoint.execDescribe();
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		
		//RDFWriter writer= model.getWriter("RDF/XML-ABBREV");
		
		RDFWriter writer= model.getWriter("RDF/XML");
		writer.setProperty("allowBadURIs", "true");
		
		writer.write(model, baos, null);

		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream((baos.toByteArray())));

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
			AgentImpl agent= new AgentImpl();
			
			agent.setAbout(key);

			agent.setPrefLabel(getAgentProperty("dbpedia-owl:birthName", "dbpprop:birthName", doc ));
			
			
			agent.setRdaGr2DateOfBirth(getAgentProperty("dbpedia-owl:birthDate", "dbpprop:birthDate", doc ));
			agent.setRdaGr2BiographicalInformation(getAgentProperty("dbpedia-owl:abstract", "dbpprop:abstract", doc ));
			agent.setRdaGr2ProfessionOrOccupation(getAgentProperty("dbpedia-owl:occupation", "dbpprop:occupation", doc));

			agent.setRdaGr2DateOfDeath(getAgentProperty("dbpedia-owl:deathDate", "dbpprop:deathDate", doc));
			agent.setEnd(getAgentProperty("dbpedia-owl:deathDate", "dbpprop:deathDate", doc));
			agent.setFoafName(getAgentProperty("dbpedia-owl:birthName", "dbpprop:birthName", doc));
			
			agent.setAltLabel(getAgentProperty("dbpedia-owl:alternativeNames", "dbpprop:alternativeNames", doc));
			
			agent.setDcIdentifier(getAgentProperty("dbpedia-owl:viaf", "dbpprop:viaf", doc));
			
			Vector <String> tempsameAsattName= new Vector<String>();
			tempsameAsattName.add("rdf:resource");
			agent.setOwlSameAs(getAgentResource("owl:sameAs", "owl:sameAs", tempsameAsattName, doc));
			
			HashMap <String,List<String>> influenced = new HashMap <String,List<String>>();
			influenced.putAll(getAgentProperty("dbpedia-owl:influenced", "dbprop:influenced", doc));
			HashMap <String,List<String>> influencedBy = new HashMap <String,List<String>>();
			influencedBy.putAll(getAgentProperty("dbpedia-owl:influencedBy", "dbprop:influencedBy", doc));
			
			Iterator <String> ite= influencedBy.keySet().iterator();
			
			while (ite.hasNext()){
				String tempLang= (String) ite.next();
				if (influenced.containsKey(tempLang))
					influenced.get(tempLang).addAll(influencedBy.get(tempLang));
				else
					influenced.put(tempLang, influencedBy.get(tempLang));
				
			}
			agent.setEdmIsRelatedTo(influenced);
			
			dm.insertAgent(agent);
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	
	
	}
	
	private HashMap <String,List<String>> getAgentProperty(String tag, String alternativeTag, Document doc){
		HashMap<String,List<String>>  myM = new HashMap <String, List<String>>();
		String logTag=tag;
		NodeList nodeList = doc.getElementsByTagName(tag);
		if (nodeList.getLength()==0 && alternativeTag!=null){
			nodeList = doc.getElementsByTagName(alternativeTag);
			logTag=alternativeTag;
		}
		String lang="def";
		if (nodeList.getLength()>0 && !nolog)
			System.out.println(logTag+" ("+ nodeList.getLength()+")");
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			
			Node nNode = nodeList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE && nNode.hasChildNodes()) {
				NamedNodeMap nnm= nNode.getAttributes();
				Node langAtt= nnm.getNamedItem("xml:lang");

				
				
				if (langAtt!=null && langAtt.hasChildNodes())
					lang=langAtt.getFirstChild().getNodeValue();
				
				
				if (!myM.containsKey(lang)){
					Vector <String> date= new Vector <String>();
					date.add(nNode.getFirstChild().getNodeValue());
					myM.put(lang, date);
				}
				else{
					myM.get(lang).add(nNode.getFirstChild().getNodeValue());
				}
				if (!nolog)
					System.out.println("  "+lang+", "+nNode.getFirstChild().getNodeValue());
				//agent.setRdaGr2BiographicalInformation(myM);
				
			}
			else{
				if (nNode.getNodeType() == Node.ELEMENT_NODE){
					Vector <String> attrName= new Vector <String> ();
					attrName.add("rdf:resource");
					Vector <String> attrValues=getElementResourceAttribute(nNode, attrName);
					if (attrValues.size()>0){
						if (!nolog)
							System.out.println("  "+lang+", "+attrValues.toString());
						if (!myM.containsKey(lang)){
							myM.put(lang, attrValues);
						}
						else{
							myM.get(lang).addAll(attrValues);
						}
					}
					
				}
			}
		}
		return myM;
	}
	
	private String[] getAgentResource(String tag, String alternativeTag, List <String> attributes, Document doc){
		
		NodeList nodeList = doc.getElementsByTagName(tag);
		ArrayList <String> result= new ArrayList <String>();
		if (nodeList.getLength()==0 && alternativeTag!=null){
			nodeList = doc.getElementsByTagName(alternativeTag);
		}
		if (!nolog)
			System.out.println(tag+"  ("+ nodeList.getLength()+", duplicates will be removed)");
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			
			Node nNode = nodeList.item(temp);
			//System.out.println(temp + " "+ nNode.toString()+" "+nNode.getNodeType());
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				
				NamedNodeMap nnm= nNode.getAttributes();
				//testPrint(nnm);
				for (String atts:attributes){
					Node attValue= nnm.getNamedItem(atts);
					if (attValue!=null && attValue.hasChildNodes()){
						if (!result.contains(attValue.getFirstChild().getNodeValue())){
							result.add(attValue.getFirstChild().getNodeValue());
							if (!nolog)
								System.out.println("  "+attValue.getFirstChild().getNodeValue());
						}
					}
						
				}
				
			}
		}
		return result.toArray(new String[result.size()]);
	}
	private void testPrint(NamedNodeMap nnm){
		for ( int i=0; i< nnm.getLength(); i++){
			System.out.println("ecco "+nnm.item(i));
			
		}
	}
	
  private Vector <String> getElementResourceAttribute(Node nNode, List <String> attributes){
		
	  Vector <String> result= new Vector <String>();
		
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				NamedNodeMap nnm= nNode.getAttributes();
				
				for (String atts:attributes){
					Node attValue= nnm.getNamedItem(atts);
					if (attValue!=null && attValue.hasChildNodes()){
						String attribStr=attValue.getFirstChild().getNodeValue();
						if (!attribStr.trim().equalsIgnoreCase(agentKey))
							result.add(attValue.getFirstChild().getNodeValue());
						else{//check if the value is in the parent node
							Node tmpNode=nNode.getParentNode();
							nnm= tmpNode.getAttributes();
							Node parentAttValue= nnm.getNamedItem("rdf:about"); //change this
							if (parentAttValue!=null && attValue.hasChildNodes())
								result.add(parentAttValue.getFirstChild().getNodeValue());
						}
					}
						
				
				
				
			}
		}
		return result;
	}
	
	
	private void collectData(String key, String serializationLang){


		QueryEngineHTTP endpoint=new QueryEngineHTTP("http://dbpedia.org/sparql", "describe <"+key+">");
		System.out.println("describing "+key);
		Model model=endpoint.execDescribe();
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		JenaJSONLD.init();
				
		model.write(baos, "JSON-LD");
		//System.out.println(baos.toString());
		dm.insertDocument(baos.toString(), "fava");
		//RDFWriter writer= model.getWriter("RDF/XML-ABBREV");
		
		
		//writer.setProperty("allowBadURIs", "true");
		
		//writer.write(model, baos, null);
/*
		try {
			Object json = JsonLdProcessor.fromRDF(model, parser);
			System.out.println(json);
//			dm.insertDocument(json.toString(), "fava");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		/*
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream((baos.toByteArray())));

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("dcterms:subject");
			//System.out.println("---- "+key);

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap nnm= nNode.getAttributes();
					Node no= nnm.getNamedItem("rdf:resource");

					//System.out.println(no.getNodeValue());

					
					//ag.addWeakCandidate(key, no.getNodeValue());
				}
			}
			



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/

	
	}

}

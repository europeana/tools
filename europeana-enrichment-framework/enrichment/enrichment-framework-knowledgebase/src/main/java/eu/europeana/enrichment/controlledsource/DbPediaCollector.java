package eu.europeana.enrichment.controlledsource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

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

import eu.europeana.enrichment.controlledsource.util.DataManager;;

public class DbPediaCollector {

	 JenaRDFParser parser;
	 DataManager dm = new DataManager();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DbPediaCollector dbpc= new DbPediaCollector();
		dbpc.loadAgentsfromDBPedia();
		//JenaJSONLD.init(); 

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
			 
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName()+"---");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	 
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		
	}
	
	private void loadAgentsfromDBPedia(){
		
		try {
			QueryEngineHTTP endpoint=new QueryEngineHTTP("http://dbpedia.org/sparql", "SELECT * WHERE {?subject ?y <http://dbpedia.org/ontology/Artist>.} LIMIT 10");
			System.out.println("getting artists from DBPedia ");
			ResultSet rs= endpoint.execSelect();
			System.out.println("exec query");
			while (rs.hasNext()){
				QuerySolution qs=rs.next();
				
				String subject=qs.get("subject").toString();
				
				//String z=qs.get("z").toString();
				System.out.println(subject+"; ");
				//collectData(subject, "rdf");
				collectAndMapControlledData(subject);
			}
			/*Model model=endpoint.execConstruct();
			ByteArrayOutputStream baos= new ByteArrayOutputStream();
			RDFWriter writer= model.getWriter("RDF/XML-ABBREV");
			
			
			writer.setProperty("allowBadURIs", "true");
			
			writer.write(model, baos, null);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream((baos.toByteArray())));
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName()+"---");
			
			NodeList nList = doc.getElementsByTagName("res:value");

			for (int temp = 0; temp < nList.getLength(); temp++){
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap nnm= nNode.getAttributes();
					Node no= nnm.getNamedItem("rdf:resource");

					System.out.println("----"+no.getNodeValue()+"---");

					collectData(no.getNodeValue(), "rdf");
				}
				
			}*/
		 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	 
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		
	}
	
	private void collectAndMapControlledData(String key){



		QueryEngineHTTP endpoint=new QueryEngineHTTP("http://dbpedia.org/sparql", "describe <"+key+">");
		System.out.println("describing "+key);
		Model model=endpoint.execDescribe();
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		
		RDFWriter writer= model.getWriter("RDF/XML-ABBREV");
		
		
		writer.setProperty("allowBadURIs", "true");
		
		writer.write(model, baos, null);

		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream((baos.toByteArray())));

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			//NodeList nList = doc.getElementsByTagName("dcterms:subject");
			NodeList nList = doc.getElementsByTagName("dbpprop:birthDate");
			//System.out.println("---- "+key);

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					//NamedNodeMap nnm= nNode.getAttributes();
					//Node no= nnm.getNamedItem("rdf:resource");

					System.out.println(nNode.getFirstChild().getNodeValue());

					
					//ag.addWeakCandidate(key, no.getNodeValue());
				}
			}
			



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	
	
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

package eu.europeana.enrichment.harvester.dbpedia;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
 


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import eu.europeana.enrichment.harvester.api.AgentMap;
import eu.europeana.enrichment.harvester.database.DataManager;
import eu.europeana.enrichment.harvester.transform.edm.agent.AgentTransformer;
import eu.europeana.enrichment.harvester.transform.edm.concept.ConceptTransformer;
import eu.europeana.enrichment.harvester.util.MongoDataSerializer;
import eu.europeana.enrichment.converters.ContextualEntityToXmlConverter;

public class DbPediaCollector {

    private static final Logger log = Logger.getLogger(DbPediaCollector.class.getName());

    private static final String AGENT = "Agent";
	private static final String CONCEPT = "Concept";
    private final DataManager dm = new DataManager();
    private String agentKey = "";

    private int gloffset = 0;

    /**
     * @param args
     */
    public static void main(String[] args) {

        DbPediaCollector dbpc = new DbPediaCollector();

       //dbpc.harvestDBPedia(); //fetch agents from local storage and harvests rdf description
     // dbpc.deleteDBPediaConcepts();
       //dbpc.harvestDBPediaConcepts();
       // dbpc.printDBPediaConcepts();
        dbpc.printDbPediaAgents();
       
        //dbpc.testHarvesting();
        //dbpc.getLocalAgents();
        
    }

    public void harvestDBPedia() {

        int resultsize = 1000;
        int limit = 1000;
        int offset = 16000;
        while (resultsize == limit) {

            List<AgentMap> agents = dm.extractAllAgentsFromLocalStorage(limit, offset);
            resultsize = agents.size();
            for (AgentMap am : agents) {
            	if (am.getAgentUri().toASCIIString().contains("dbpedia.org"))
            		collectAndMapControlledData(am.getAgentUri().toASCIIString(), AGENT);
            }
            if (agents.size() == limit) {
                offset = offset + limit;
                gloffset=offset;
            }
        }

    }
    
    
    public void harvestDBPediaConcepts() {
    	File file = new File("src/main/resources/dbpedia_concept_list.txt");
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia"))
    	        	collectAndMapControlledData(line, CONCEPT);
    	    }
    	    
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
    public void deleteDBPediaConcepts() {
    	File file = new File("src/main/resources/dbpedia_concept_list.txt");
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia"))
    	        	dm.deleteConcept(line);
    	    }
    	    
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
    
    public void getDBPediaConcepts() {
    	File file = new File("src/main/resources/dbpedia_concept_list.txt");
    	String id="";
    	ContextualEntityToXmlConverter myXMLConverter= new ContextualEntityToXmlConverter();
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia")){
    	        	id=line;
    	        	if (line!=null && ! line.isEmpty()){
    	        		
    	        		
    	        		if (dm.getConcept(line)!=null)
    	        			System.out.println(myXMLConverter.convertConcept(dm.getConcept(line)));
    	        		//dm.getConcept(line);
    	        	}
    	        }
    	        	
    	    }
    	    
    	} catch (Exception e) {
			System.out.println (id);
			e.printStackTrace();
		}
}
    
    public void printDbPediaAgents(){

    	
    	String id="";

    	MongoDataSerializer ds= new MongoDataSerializer();
    	DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
        
        int resultsize = 1000;
		int limit = 1000;
		int offset = 0;
		try{
		 icBuilder = icFactory.newDocumentBuilder();
         Document doc = icBuilder.newDocument();
         //Element mainRootElement = doc.createElementNS("http://europeana.eu/concepts", "Concepts");
         Element mainRootElement = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
         mainRootElement.setAttributeNS("xmlns:skos", "skos", "http://www.w3.org/2002/02/skos/core#");
         mainRootElement.setAttributeNS("xmlns:rdaGr2", "rdaGr2", "http://RDVocab.info/ElementsGr2/");
         mainRootElement.setAttributeNS("xmlns:foaf", "foaf", "http://xmlns.com/foaf/0.1/");
         mainRootElement.setAttributeNS("xmlns:owl", "owl", "http://www.w3.org/2002/07/owl#");
         
         mainRootElement.setAttributeNS("xmlns:dc", "dc", "http://purl.org/dc/elements/1.1/");
         doc.appendChild(mainRootElement);
		while (resultsize >0) {

			List<AgentMap> agents =dm.extractAllAgentsFromLocalStorage(limit, offset);
			for (AgentMap auri:agents){
				if (auri.getAgentUri().toASCIIString().startsWith("http://dbpedia.org/resource")
						&& dm.getAgent(auri.getAgentUri().toASCIIString()) !=null){
					String aid=auri.getAgentUri().toASCIIString();
					Node conceptElement=ds.serializeAgent(doc, dm.getAgent(aid));
					if (conceptElement!=null)
						mainRootElement.appendChild(conceptElement);
				}
			}
			resultsize = agents.size();
			if (agents.size() >0) {
				offset = offset + limit;
				gloffset=offset;
			}
		}
           
 
		try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				//DOMSource source1 = new DOMSource(conceptElement);
				StreamResult result = new StreamResult(new File("storedagents.xml"));

				// Output to console for testing
				//StreamResult result = new StreamResult(System.out);

				transformer.transform(source, result);
				
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	    
    	} catch (Exception e) {
			System.out.println (id);
			e.printStackTrace();
		}

    }
    public void printDBPediaConcepts() {
    	File file = new File("src/main/resources/dbpedia_concept_list.txt");
    	String id="";
    	ContextualEntityToXmlConverter myXMLConverter= new ContextualEntityToXmlConverter();
    	MongoDataSerializer ds= new MongoDataSerializer();
    	DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
       
           
 
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			 icBuilder = icFactory.newDocumentBuilder();
	            Document doc = icBuilder.newDocument();
	            //Element mainRootElement = doc.createElementNS("http://europeana.eu/concepts", "Concepts");
	            Element mainRootElement = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
	            mainRootElement.setAttributeNS("xmlns:skos", "skos", "http://www.w3.org/2002/02/skos/core#");
	            
	            doc.appendChild(mainRootElement);
    	    for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia")){
    	        	id=line;
    	        	if (line!=null && ! line.isEmpty()){
    	        		
    	        		
    	        		if (dm.getConcept(line)!=null){
    	        			String myxml=myXMLConverter.convertConcept(dm.getConcept(line));
    	        			Node conceptElement=ds.serializeConcept(doc, dm.getConcept(line));
    	        			if (conceptElement!=null)
    	        				mainRootElement.appendChild(conceptElement);
    	        			
    	        		}
    	        		
    	        	}
    	        }
    	        	
    	    }try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				//DOMSource source1 = new DOMSource(conceptElement);
				StreamResult result = new StreamResult(new File("storedconcepts.xml"));

				// Output to console for testing
				//StreamResult result = new StreamResult(System.out);

				transformer.transform(source, result);
				
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	    
    	} catch (Exception e) {
			System.out.println (id);
			e.printStackTrace();
		}
}
    private String cleanString(String str){
    	
    	str= str.replace("&lt;", "");
    	str= str.replace("/&gt;", "");
    	str= str.replace("&gt;", "");
    	return str;
    }
    private void testHarvesting() {

                
                //collectAndMapControlledData("http://dbpedia.org/resource/Expressionism", CONCEPT);
    	collectAndMapLocalControlledData("de", "http://de.dbpedia.org/resource/Ian_Siegal", AGENT);
           

    }

    
    public void getLocalAgents() {

		int resultsize = 1000;
		int limit = 1000;
		int offset = 0;

		while (resultsize >0) {

			List<String> agents = dm.ecxtractLocalizedDbPediaAgentsFromLocalStorage("pt", limit, offset);
			
			for (String am:agents){
				collectAndMapLocalControlledData("pt", am, AGENT);
			}
				
			resultsize = agents.size();
			if (agents.size() >0) {
				offset = offset + limit;
				gloffset=offset;
			}
		}

	}

    private void collectAndMapControlledData(String key, String entity) {
    	
    	if (key.endsWith("Charles_Hamilton_(rapper)"))
    		return;
    	if (key.endsWith("Johannes_Liechtenauer"))
    		return;

        QueryEngineHTTP endpoint = new QueryEngineHTTP("http://dbpedia.org/sparql", "describe <" + key + ">");
        log.log(Level.INFO, "describing " + key+" offset: "+gloffset);
        agentKey = key;
        
        Model model=ModelFactory.createDefaultModel();
        
       
        model = endpoint.execDescribe();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        RDFWriter writer = model.getWriter("RDF/XML");
        writer.setProperty("allowBadURIs", "true");

        writer.write(model, baos, null);
        Source inputDoc = new StreamSource(new ByteArrayInputStream((baos.toByteArray())));
       // System.out.println (baos.toString());
        if (entity.equals(AGENT))
        	dm.insertAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));

        if (entity.equals(CONCEPT))
        	dm.insertConcept(new ConceptTransformer().transform("src/main/resources/dbpedia_skos_concepts.xsl", key, inputDoc));
        	//new ConceptTransformer().transform("src/main/resources/dbpedia_skos_concepts_new.xsl", key, inputDoc);
       // dm.updateAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));
    }
    
    private void collectAndMapLocalControlledData(String localPrefix, String key, String entity){

    	
    	if (key.endsWith("Charles_Hamilton_(rapper)"))
    		return;
    	if (key.endsWith("Johannes_Liechtenauer"))
    		return;

    	String sparqlEndPoint="http://"+localPrefix+".dbpedia.org/sparql";
        QueryEngineHTTP endpoint = new QueryEngineHTTP(sparqlEndPoint, "describe <" + key + ">");
        log.log(Level.INFO, "describing " + key+" offset: "+gloffset);
        agentKey = key;
        
        Model model=ModelFactory.createDefaultModel();
        
       
        model = endpoint.execDescribe();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        RDFWriter writer = model.getWriter("RDF/XML");
        writer.setProperty("allowBadURIs", "true");

        writer.write(model, baos, null);
        Source inputDoc = new StreamSource(new ByteArrayInputStream((baos.toByteArray())));
        System.out.println (inputDoc);
        if (entity.equals(AGENT))
        	dm.insertAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));

        if (entity.equals(CONCEPT))
        	dm.insertConcept(new ConceptTransformer().transform("src/main/resources/dbpedia_skos_concepts.xsl", key, inputDoc));
       // dm.updateAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));
    
    }

    private HashMap<String, List<String>> getAgentProperty(String tag, String alternativeTag, Document doc) {
        HashMap<String, List<String>> myM = new HashMap<>();
        String logTag = tag;
        NodeList nodeList = doc.getElementsByTagName(tag);
        if (nodeList.getLength() == 0 && alternativeTag != null) {
            nodeList = doc.getElementsByTagName(alternativeTag);
            logTag = alternativeTag;
        }
        String lang = "def";
        if (nodeList.getLength() > 0) {
            log.info(logTag + " (" + nodeList.getLength() + ")");
        }
        for (int temp = 0; temp < nodeList.getLength(); temp++) {

            Node nNode = nodeList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE && nNode.hasChildNodes()) {
                NamedNodeMap nnm = nNode.getAttributes();
                Node langAtt = nnm.getNamedItem("xml:lang");

                if (langAtt != null && langAtt.hasChildNodes()) {
                    lang = langAtt.getFirstChild().getNodeValue();
                }

                if (!myM.containsKey(lang)) {
                    List<String> date = new ArrayList<>();
                    date.add(nNode.getFirstChild().getNodeValue());
                    myM.put(lang, date);
                } else {
                    myM.get(lang).add(nNode.getFirstChild().getNodeValue());
                }

                log.log(Level.SEVERE, "  " + lang + ", " + nNode.getFirstChild().getNodeValue());

            } else {
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> attrName = new ArrayList<>();
                    attrName.add("rdf:resource");
                    List<String> attrValues = getElementResourceAttribute(nNode, attrName);
                    if (attrValues.size() > 0) {
                        log.log(Level.INFO, lang + ", " + attrValues.toString());

                        if (!myM.containsKey(lang)) {
                            myM.put(lang, attrValues);
                        } else {
                            myM.get(lang).addAll(attrValues);
                        }
                    }

                }
            }
        }
        return myM;
    }

    private String[] getAgentResource(String tag, String alternativeTag, List<String> attributes, Document doc) {

        NodeList nodeList = doc.getElementsByTagName(tag);
        List<String> result = new ArrayList<>();
        if (nodeList.getLength() == 0 && alternativeTag != null) {
            nodeList = doc.getElementsByTagName(alternativeTag);
        }
        log.log(Level.INFO, tag + "  (" + nodeList.getLength() + ", duplicates will be removed)");

        for (int temp = 0; temp < nodeList.getLength(); temp++) {

            Node nNode = nodeList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap nnm = nNode.getAttributes();
                for (String atts : attributes) {
                    Node attValue = nnm.getNamedItem(atts);
                    if (attValue != null && attValue.hasChildNodes()) {
                        if (!result.contains(attValue.getFirstChild().getNodeValue())) {
                            result.add(attValue.getFirstChild().getNodeValue());
                            log.log(Level.INFO, attValue.getFirstChild().getNodeValue());

                        }
                    }

                }

            }
        }
        return result.toArray(new String[result.size()]);
    }

    private List<String> getElementResourceAttribute(Node nNode, List<String> attributes) {

        List<String> result = new ArrayList<>();

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap nnm = nNode.getAttributes();

            for (String atts : attributes) {
                Node attValue = nnm.getNamedItem(atts);
                if (attValue != null && attValue.hasChildNodes()) {
                    String attribStr = attValue.getFirstChild().getNodeValue();
                    if (!attribStr.trim().equalsIgnoreCase(agentKey)) {
                        result.add(attValue.getFirstChild().getNodeValue());
                    } else {//check if the value is in the parent node
                        Node tmpNode = nNode.getParentNode();
                        nnm = tmpNode.getAttributes();
                        Node parentAttValue = nnm.getNamedItem("rdf:about"); //change this
                        if (parentAttValue != null && attValue.hasChildNodes()) {
                            result.add(parentAttValue.getFirstChild().getNodeValue());
                        }
                    }
                }

            }
        }
        return result;
    }

}

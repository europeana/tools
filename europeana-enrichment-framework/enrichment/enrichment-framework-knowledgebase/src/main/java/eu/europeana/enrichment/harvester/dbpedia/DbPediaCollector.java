package eu.europeana.enrichment.harvester.dbpedia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import eu.europeana.enrichment.harvester.api.AgentMap;
import eu.europeana.enrichment.harvester.database.DataManager;
import eu.europeana.enrichment.harvester.transform.edm.agent.AgentTransformer;

public class DbPediaCollector {

    private static final Logger log = Logger.getLogger(DbPediaCollector.class.getName());

    private final DataManager dm = new DataManager();
    private String agentKey = "";

    private int gloffset = 0;

    /**
     * @param args
     */
    public static void main(String[] args) {

        DbPediaCollector dbpc = new DbPediaCollector();

       dbpc.harvestDBPedia(); //fetch agents from local storage and harvests rdf description

        //dbpc.testHarvesting();
    }

    public void harvestDBPedia() {

        int resultsize = 1000;
        int limit = 1000;
        int offset = 0;
        while (resultsize == limit) {

            List<AgentMap> agents = dm.extractAllAgentsFromLocalStorage(limit, offset);
            resultsize = agents.size();
            for (AgentMap am : agents) {
                collectAndMapControlledData(am.getAgentUri().toASCIIString());
            }
            if (agents.size() == limit) {
                offset = offset + limit;
                gloffset=offset;
            }
        }

    }
    
    private void testHarvesting() {

        
                collectAndMapControlledData("http://dbpedia.org/resource/Charles_Hamilton_(rapper)");
           

    }

    private void collectAndMapControlledData(String key) {
    	
    	if (key.endsWith("Charles_Hamilton_(rapper)"))
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
        dm.insertAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder dBuilder;
//        try {
//            dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(new ByteArrayInputStream((baos.toByteArray())));
//
//            AgentImpl agent = new AgentImpl();
//            agent.setAbout(key);
//            agent.setRdaGr2BiographicalInformation(getAgentProperty("dbpedia-owl:abstract", "dbpprop:abstract", doc));
//            agent.setRdaGr2DateOfBirth(getAgentProperty("dbpedia-owl:birthDate", "dbpprop:birthDate", doc));
//            agent.setRdaGr2PlaceOfBirth(getAgentProperty("dbpedia-owl:birthPlace", "dbpprop:birthPlace", doc));
//            agent.setRdaGr2PlaceOfDeath(getAgentProperty("dbpedia-owl:deathPlace", "dbpprop:deathPlace", doc));
//            agent.setPrefLabel(getAgentProperty("foaf:name", "foaf:name", doc));
//            agent.setRdaGr2DateOfDeath(getAgentProperty("dbpedia-owl:deathDate", "dbpprop:deathDate", doc));
//            agent.setRdaGr2ProfessionOrOccupation(getAgentProperty("dbpedia-owl:occupation", "dbpprop:occupation", doc));
//            agent.setAltLabel(getAgentProperty("dbpedia-owl:alternativeNames", "dbpprop:alternativeNames", doc));
//            agent.setEnd(getAgentProperty("dbpedia-owl:deathDate", "dbpprop:deathDate", doc));
//            agent.setDcIdentifier(getAgentProperty("dbpedia-owl:viaf", "dbpprop:viaf", doc));
//            List<String> tempsameAsattName = new ArrayList<>();
//            tempsameAsattName.add("rdf:resource");
//            agent.setOwlSameAs(getAgentResource("owl:sameAs", "owl:sameAs", tempsameAsattName, doc));
//            HashMap<String, List<String>> influenced = new HashMap<>();
//            influenced.putAll(getAgentProperty("dbpedia-owl:influenced", "dbprop:influenced", doc));
//            HashMap<String, List<String>> influencedBy = new HashMap<>();
//            influencedBy.putAll(getAgentProperty("dbpedia-owl:influencedBy", "dbprop:influencedBy", doc));
//            Iterator<String> ite = influencedBy.keySet().iterator();
//            while (ite.hasNext()) {
//                String tempLang = (String) ite.next();
//                if (influenced.containsKey(tempLang)) {
//                    influenced.get(tempLang).addAll(influencedBy.get(tempLang));
//                } else {
//                    influenced.put(tempLang, influencedBy.get(tempLang));
//                }
//            }
//            agent.setEdmIsRelatedTo(influenced);
//            dm.insertAgent(agent);
//
//        } catch (IOException | ParserConfigurationException | SAXException e) {
//            log.log(Level.SEVERE, e.getMessage());
//        }
        
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

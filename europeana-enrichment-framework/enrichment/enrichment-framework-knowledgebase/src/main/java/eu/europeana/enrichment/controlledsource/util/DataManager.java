package eu.europeana.enrichment.controlledsource.util;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.List;


import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import org.jibx.runtime.JiBXException;
 


import org.mongodb.morphia.*;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import eu.europeana.enrichment.controlledsource.api.internal.AgentMap;

import java.util.Map.Entry;


//import eu.europeana.corelib.solr.entity.AgentImpl;


public class DataManager {
	
	private static JacksonDBCollection<AgentTermList, String> aColl;
	private static JacksonDBCollection<ConceptTermList, String> cColl;
	private static JacksonDBCollection<PlaceTermList, String> pColl;
	private static JacksonDBCollection<TimespanTermList, String> tColl;
	private static JacksonDBCollection<TimespanTermList, String> dbpagentsColl;
	
	//Mongo mongo;
	private static DB db;
	Datastore ds ;
	
	public DataManager(){
		try{
		//	mongo = new Mongo("127.0.0.1",27017);
		//	db = mongo.getDB("ControlledSource");
			dbExists("127.0.0.1",27017);
			Mongo mongodb= new Mongo("127.0.0.1");
			
			ds = new Morphia().createDatastore(mongodb, "agentmap");
			ds.ensureIndexes();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	public static boolean dbExists(String host, int port) {
		try {
			if (db == null) {

				Mongo mongo = new Mongo(host, port);
				db = mongo.getDB("annocultor_db");
				if (db.collectionExists("TermList")) {
					
					cColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"),
							ConceptTermList.class, String.class);

					cColl.ensureIndex("codeUri");

					aColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"), AgentTermList.class,
							String.class);

					aColl.ensureIndex("codeUri");
					
					tColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"),
							TimespanTermList.class, String.class);

					tColl.ensureIndex("codeUri");
					
					pColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"), PlaceTermList.class,
							String.class);

					pColl.ensureIndex("codeUri");

					return true;
				} else {
					cColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"),
							ConceptTermList.class, String.class);

					cColl.ensureIndex("codeUri");

					aColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"), AgentTermList.class,
							String.class);

					aColl.ensureIndex("codeUri");
					tColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"),
							TimespanTermList.class, String.class);

					tColl.ensureIndex("codeUri");
					pColl = JacksonDBCollection.wrap(
							db.getCollection("TermList"), PlaceTermList.class,
							String.class);

					pColl.ensureIndex("codeUri");
					return false;
				}
			}
			return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public void insertAgent(AgentImpl agent){
		try {
			agentToAgentTermList(agent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JiBXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void insertAgentMap(AgentMap agentMap){
		
		System.out.println("Saving map for: "+ agentMap.getAgentUri());
		ds.save(agentMap);
	}
	public void insertDocument(String jsonObject, String collectionName){
		try{
			System.out.println("JSON parse ...");
			 

		     DBCollection collection = db.getCollection("MyCollection");
			 DBObject dbObject = (DBObject) JSON.parse(jsonObject);
			 collection.insert(dbObject);
			 
			// DBCursor cursorDocJSON = collection.find();
			// while (cursorDocJSON.hasNext()) {
			// System.out.println(cursorDocJSON.next());
			 
			// }
			 } catch (MongoException e) {
			 // TODO: handle exception
			 e.printStackTrace();
			 }		
		 
	}
	
	private static void agentToAgentTermList(AgentImpl agent) throws IOException, JiBXException {

		AgentTermList termList = new AgentTermList();
		termList.setCodeUri(agent.getAbout());
		//If it had parents then we should get it now from the dcterms:isPartOf of the Concept and Place or the skos:broader
		//of the Concept and append it here like
		/**
		 * String broader = placeImpl.getIsPartOf().get("def").get(0);
		 * termList.setParent(broader);
		 */

		List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();

		for (Entry<String, List<String>> prefLabel : agent.getPrefLabel().entrySet()) {
			for (String label : prefLabel.getValue()) {
				MongoTerm pTerm = new MongoTerm();
				pTerm.setCodeUri(agent.getAbout());
				pTerm.setLabel(label.toLowerCase());
				String lang = prefLabel.getKey();

				pTerm.setOriginalLabel(label);

				pTerm.setLang(lang);

				JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection.wrap(db.getCollection("people"), MongoTerm.class, String.class);
				pColl.ensureIndex("codeUri");
				pColl.ensureIndex("label");
				WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
				DBRef<MongoTerm, String> pTermRef = new DBRef<MongoTerm, String>(
						res.getSavedObject().getId(), "people");
				pList.add(pTermRef);
			}
		}

		termList.setTerms(pList);

		termList.setRepresentation(agent);
		termList.setEntityType(AgentImpl.class.getSimpleName());
		aColl.insert(termList);

	}
	

}

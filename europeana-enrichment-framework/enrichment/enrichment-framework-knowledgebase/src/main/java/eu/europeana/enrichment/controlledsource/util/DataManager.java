package eu.europeana.enrichment.controlledsource.util;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import java.util.List;


import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jibx.runtime.JiBXException;
 


import org.mongodb.morphia.*;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;

import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
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
	private final static String AGENT = "people";
	private final static ObjectMapper obj = new ObjectMapper();
	
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
	
	public List <AgentMap>extractAllAgentsFromLocalStorage(int limit, int offset){
		
		
		Query <AgentMap> q = ds.createQuery(AgentMap.class);
		//q.criteria("agentUri").contains("Tania");
		q.limit(limit).offset(offset);
		List<AgentMap> res= q.asList();
		return res;
		
	}
	
	public boolean insertAgentMap(AgentMap agentMap){
		
		if (queryToFindAgent(agentMap.getId()).countAll()>0){
			//System.out.println(agentMap.getAgentUri()+" is on board");
			return false;
		}
		else{
			System.out.println("Saving map for: "+ agentMap.getAgentUri());
			ds.save(agentMap);
			return true;
		}
		
	}
	
	//test these
	 private Query<AgentMap> queryToFindAgent(String id) {
	      return ds.createQuery(AgentMap.class).field("id").equal(id);
	   }

	  public void harvested(String id) {
		  Date now = new Date();
	      UpdateOperations<AgentMap> ops = ds.createUpdateOperations(AgentMap.class).set("harvestedDate", now);
	      ds.update(queryToFindAgent(id), ops);

	   }
	  //
	  
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
			//System.out.println(prefLabel.getValue().get(0));
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
	
	private List<? extends EntityWrapper> findEntities(String lowerCase,
			String field, EntityClass className)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		switch (className) {
		case AGENT:
			entities.addAll(findAgentEntities(lowerCase, field));
			break;
		default:
			break;
		}
		return entities;
	}
	
	private List<? extends EntityWrapper> findAgentEntities(String value,
			String originalField) throws JsonGenerationException,
			JsonMappingException, IOException {
		List<EntityWrapper> agents = new ArrayList<EntityWrapper>();
		MongoTermList terms = findByLabel(value, AGENT);
		if (terms != null) {

			EntityWrapper agentEntity = new EntityWrapper();
			agentEntity.setOriginalField(originalField);

			agentEntity.setClassName(AgentImpl.class.getName());
			agentEntity.setContextualEntity(getObjectMapper()
					.writeValueAsString(terms.getRepresentation()));
			
			//agentEntity.setUrl(terms.getCodeUri());
			//agentEntity.setOriginalValue(value);
			agents.add(agentEntity);
			//if (terms.getParent() != null) {
			//	agents.addAll(findAgentParents(terms.getParent()));
			//}
		}
		return agents;
	}
	private ObjectMapper getObjectMapper() {
		return obj;
	}
	
	public static MongoTermList findByLabel(String label, String dbtable)

			throws MalformedURLException {
				
				JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
						.wrap(db.getCollection(dbtable), MongoTerm.class, String.class);
				pColl.ensureIndex("label");

				net.vz.mongodb.jackson.DBCursor<MongoTerm> curs = pColl.find().is("label", label.toLowerCase());
				if (curs.hasNext()) {
					MongoTerm mTerm = curs.next();
					MongoTermList t = findAgentByCode(mTerm.getCodeUri());
					//typeMap.put(label.toLowerCase(), t);
					//memCache.put(dbtable, typeMap);
					return t;
				}
				return null;
			}
	

	private static MongoTermList findAgentByCode(String codeUri) {
		net.vz.mongodb.jackson.DBCursor<AgentTermList> curs = aColl.find().is("codeUri", codeUri);
		if (curs.hasNext()) {
			AgentTermList terms = curs.next();
			return terms;
		}
		return null;
	}
}

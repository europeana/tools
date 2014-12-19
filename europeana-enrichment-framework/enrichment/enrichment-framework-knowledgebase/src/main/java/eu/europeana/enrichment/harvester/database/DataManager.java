package eu.europeana.enrichment.harvester.database;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import org.apache.commons.lang.StringUtils;
import org.jibx.runtime.JiBXException;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import eu.europeana.enrichment.harvester.api.AgentMap;

public class DataManager {

	private static final Logger log = Logger.getLogger(DataManager.class.getName());
	private static final String TERMLIST = "TermList";
	private static final String CODEURI = "codeUri";
	private static JacksonDBCollection<AgentTermList, String> aColl;
	private static JacksonDBCollection<ConceptTermList, String> cColl;
	private static JacksonDBCollection<PlaceTermList, String> pColl;
	private static JacksonDBCollection<TimespanTermList, String> tColl;

	private static DB db;
	private Datastore ds;
	private Datastore dsTL;
	private DBCollection coll;
	private final static String DEFAULT_HOST = "127.0.0.1";
	private final static int DEFAULT_PORT = 27017;

	public DataManager() {
		this(DEFAULT_HOST,DEFAULT_PORT);
	}

	public DataManager(String host,int port){
		try {
			FileHandler fh = new FileHandler("test1_harvester.log");
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
			dbExists(host,port);
			Mongo mongodb = new Mongo(host, port);

			ds = new Morphia().createDatastore(mongodb, "agentmap");
			ds.ensureIndexes();
			dsTL = new Morphia().createDatastore(mongodb, "annocultor_db");

			//dsTL.ensureIndexes();
			coll = db.getCollection(TERMLIST);

		} catch (MongoException | IOException | SecurityException e) {
			log.log(Level.SEVERE, e.getMessage());
		}
	}
	public final  boolean dbExists(String host, int port) {
		try {
			if (db == null) {

				Mongo mongo = new Mongo(host, port);
				db = mongo.getDB("annocultor_db");
				if (db.collectionExists(TERMLIST)) {

					cColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST),
							ConceptTermList.class, String.class);

					cColl.ensureIndex(CODEURI);

					aColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST), AgentTermList.class,
							String.class);

					aColl.ensureIndex(CODEURI);

					tColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST),
							TimespanTermList.class, String.class);

					tColl.ensureIndex(CODEURI);

					pColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST), PlaceTermList.class,
							String.class);

					pColl.ensureIndex(CODEURI);

					return true;
				} else {
					cColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST),
							ConceptTermList.class, String.class);

					cColl.ensureIndex(CODEURI);

					aColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST), AgentTermList.class,
							String.class);

					aColl.ensureIndex(CODEURI);
					tColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST),
							TimespanTermList.class, String.class);

					tColl.ensureIndex(CODEURI);
					pColl = JacksonDBCollection.wrap(
							db.getCollection(TERMLIST), PlaceTermList.class,
							String.class);

					pColl.ensureIndex(CODEURI);
					return false;
				}
			}
			return true;
		} catch (UnknownHostException | MongoException e) {
			log.log(Level.SEVERE, e.getMessage());
		}
		return false;
	}

	public void insertAgent(AgentImpl agent) {
		try {

			if (queryToFindAgentDescription(agent.getAbout()))
				agentToAgentTermList(agent);
		} catch (IOException | JiBXException e) {
			log.log(Level.SEVERE, e.getMessage());
		}
	}

	public List<AgentMap> extractAllAgentsFromLocalStorage(int limit, int offset) {

		Query<AgentMap> q = ds.createQuery(AgentMap.class);
		q.limit(limit).offset(offset);
		return q.asList();

	}

	public List <String> extractFreebaseAgentsFromLocalStorage(int limit, int offset) {

		//'representation.owlSameAs': /.*freebase.*/
		List <String> fbAgent=new  ArrayList<String>();
		Pattern freebaseUrl = Pattern.compile("/.*freebase.*/", Pattern.CASE_INSENSITIVE);
		BasicDBObject query=new BasicDBObject("representation.owlSameAs",freebaseUrl);// "http://rdf.freebase.com/ns/m.064qqly");

		DBCursor cursor = null;
		cursor = coll.find(query).limit(limit).skip(offset);
		while (cursor.hasNext()){

			BasicDBObject representation = (BasicDBObject) cursor.next().get("representation"); 
			BasicDBList owlsameas = (BasicDBList) representation.get("owlSameAs");
			String aboutAgent=(String) representation.get("about");
			for (Iterator <Object> ite=owlsameas.iterator(); ite.hasNext();){

				String freebase = (String) ite.next();
				if (freebase.contains("freebase")){
					System.out.println(freebase+ " "+aboutAgent.toString()+" "+offset);
					addSameAs(aboutAgent.toString(), freebase);
					fbAgent.add(freebase);
				}

			}
		} 
		return fbAgent;
	}

	public boolean insertAgentMap(AgentMap agentMap) {

		if (queryToFindAgent(agentMap.getId()).countAll() > 0) {

			return false;
		} else {
			log.log(Level.INFO, String.format("Saving map for: %s", agentMap.getAgentUri()));
			ds.save(agentMap);
			return true;
		}

	}

	//test
	private Query<AgentMap> queryToFindAgent(String id) {
		return ds.createQuery(AgentMap.class).field("id").equal(id);
	}

	private boolean queryToFindAgentDescription(String id) {

		BasicDBObject query = new BasicDBObject(CODEURI, id);
		DBCursor cursor = null;
		cursor = coll.find(query);
		Boolean res=(cursor.count()==0);
		cursor.close();
		return res;

	}
	public AgentImpl getAgent(String id){
		BasicDBObject query=new BasicDBObject("codeUri",id);// "http://rdf.freebase.com/ns/m.064qqly");
		if (aColl.find(query).hasNext()){
			AgentTermList aTL=(AgentTermList)aColl.find(query).next();
			AgentImpl rAi =  aTL.getRepresentation();
			return (rAi);
		}
		else return null;

	}

	public void harvested(String id) {
		Date now = new Date();
		UpdateOperations<AgentMap> ops = ds.createUpdateOperations(AgentMap.class).set("harvestedDate", now);
		ds.update(queryToFindAgent(id), ops);

	}

	public void addSameAs(String id, String sameAs) {

		UpdateOperations<AgentMap> saops = ds.createUpdateOperations(AgentMap.class).set("sameAs", sameAs);
		ds.update(queryToFindAgent(id), saops);

	}
	//

	public void insertDocument(String jsonObject, String collectionName) {
		try {
			log.log(Level.INFO, "JSON parse ...");
			DBCollection collection = db.getCollection("MyCollection");
			DBObject dbObject = (DBObject) JSON.parse(jsonObject);
			collection.insert(dbObject);

		} catch (MongoException e) {
			log.log(Level.SEVERE, e.getMessage());
		}

	}

	private void agentToAgentTermList(AgentImpl agent) throws IOException, JiBXException {



		AgentTermList termList = new AgentTermList();
		log.info("*********agent prefl "+agent.getPrefLabel());
		if (agent.getPrefLabel() == null || agent.getPrefLabel().entrySet().size()==0)
			return;
		termList.setCodeUri(agent.getAbout());
		List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<>();
		for (Entry<String, List<String>> prefLabel : agent.getPrefLabel().entrySet()) {

			for (String label : prefLabel.getValue()) {
				MongoTerm pTerm = new MongoTerm();
				pTerm.setCodeUri(agent.getAbout());
				pTerm.setLabel(label.toLowerCase());
				String lang = prefLabel.getKey();

				pTerm.setOriginalLabel(label);

				pTerm.setLang(lang);

				JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection.wrap(db.getCollection("people"), MongoTerm.class, String.class);
				pColl.ensureIndex(CODEURI);
				pColl.ensureIndex("label");
				WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
				DBRef<MongoTerm, String> pTermRef = new DBRef<>(
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

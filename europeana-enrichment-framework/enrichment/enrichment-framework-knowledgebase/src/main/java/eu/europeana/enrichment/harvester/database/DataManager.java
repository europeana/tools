package eu.europeana.enrichment.harvester.database;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import org.apache.commons.lang.StringUtils;
import org.jibx.runtime.JiBXException;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
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
            Mongo mongodb = new Mongo(host);

            ds = new Morphia().createDatastore(mongodb, "agentmap");
            ds.ensureIndexes();
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

    public boolean insertAgentMap(AgentMap agentMap) {

        if (queryToFindAgent(agentMap.getId()).countAll() > 0) {
            return false;
        } else {
            log.log(Level.INFO, String.format("Saving map for: %s", agentMap.getAgentUri()));
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

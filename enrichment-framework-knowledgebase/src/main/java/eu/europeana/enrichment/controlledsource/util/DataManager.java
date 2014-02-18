package eu.europeana.enrichment.controlledsource.util;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
 
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
public class DataManager {
	
	Mongo mongo;
	DB db;
	public DataManager(){
		try{
			mongo = new Mongo("127.0.0.1",27017);
			db = mongo.getDB("ControlledSource");
		}
		catch (Exception e){
			e.printStackTrace();
		}
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
	

}

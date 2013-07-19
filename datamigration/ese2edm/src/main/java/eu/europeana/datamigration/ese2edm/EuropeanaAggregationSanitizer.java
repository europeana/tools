package eu.europeana.datamigration.ese2edm;

import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.datamigration.ese2edm.sanitizers.Sanitizer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

public class EuropeanaAggregationSanitizer implements Sanitizer {

	@Override
	public void sanitize() {

		try {
			Mongo mongo = new Mongo(PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort());
			DB db = mongo.getDB("europeana");
			DBCollection eAggrCollection=db.getCollection("EuropeanaAggregation");
			DBCursor curs = eAggrCollection.find();
			int i=0;
			while (curs.hasNext()){
				DBObject obj = curs.next();
				if(!obj.containsField("aggregatedCHO")){
					BasicDBObject upd = new BasicDBObject();
					upd.append("$set", new BasicDBObject().append("aggregatedCHO",StringUtils.substringAfter(obj.get("about").toString(), "/aggregation/europeana")));
					eAggrCollection.update(obj, upd);
					//System.out.println("Updating EuropeanaAggregation " + i +" of "+curs.size()+ " with about: " +obj.get("about").toString());
					
					
				}
				i++;
				System.out.println("Updating EuropeanaAggregation " + i +" of "+curs.size()+ " with about: " +obj.get("about").toString());
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new EuropeanaAggregationSanitizer().sanitize();
	}

}

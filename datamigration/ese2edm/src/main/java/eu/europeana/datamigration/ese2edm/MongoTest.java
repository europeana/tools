package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

public class MongoTest {

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			Mongo mongo = new Mongo(PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort());
			DB db = mongo.getDB(PropertyUtils.getEuropeanaDB());
			DBCollection coll = db.getCollection("record");
			DBCursor cur = coll.find(new BasicDBObject());
			cur.batchSize(5000);
			long start = new Date().getTime();
			int i = 0;
			Map<String, Nums> map = new HashMap<String, Nums>();
			List<String> inputList = FileUtils.readLines(new File(
					args[0]));
			for (String str : inputList) {
				String collection = StringUtils.substringBefore(str, ";");
				if(collection!=null){
				String s = StringUtils.substringBetween(str, ";", ";");
				if (s != null) {
					Nums num = new Nums(Integer.parseInt(s));
					map.put(collection, num);
				}
				}
			}
			while (cur.hasNext()) {
				DBObject obj = cur.next();
				String colId = ((List<String>)obj.get("europeanaCollectionName")).get(0);
				Nums nums = null;
				if (map.containsKey(colId)) {
					nums = map.get(colId);
					nums.setFound(nums.getFound() + 1);

				} else {
					nums = new Nums(0);
					nums.setFound(1);
				}
				map.put(colId, nums);
				i++;
				if (i % 100000 == 0) {
					Logger.getLogger("MongoTest").log(
							Level.INFO,
							"Iterated over 100000 in "
									+ (new Date().getTime() - start) + " ms");
				}
			}
			Logger.getLogger("MongoTest").log(Level.INFO,
					"Iterating took " + (new Date().getTime() - start) + " ms");
			File collections = new File(args[1]);
			collections.createNewFile();
			for (Entry<String, Nums> entry : map.entrySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(entry.getKey());
				sb.append(";");
				sb.append(entry.getValue().getExpected());
				sb.append(";");
				sb.append(entry.getValue().getFound());
				sb.append("\n");
				FileUtils.writeStringToFile(collections, sb.toString(), true);
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

class Nums {

	public Nums(int expected) {
		this.expected = expected;
	}

	private int expected;
	private int found;

	public int getExpected() {
		return expected;
	}

	public void setExpected(int expected) {
		this.expected = expected;
	}

	public int getFound() {
		return found;
	}

	public void setFound(int found) {
		this.found = found;
	}

	@Override
	public String toString() {
		return "[expected=" + expected + "] - [numFound=" + found + "]";
	}

}

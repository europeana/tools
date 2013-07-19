/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

/**
 * Console application that performs sanity checks on the records per collection
 * in the Europeana Mongo database
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class MongoQuery {
	/**
	 * Main method
	 * @param args
	 * 			The location of the colon-delimited csv file with the collections and expected numbers
	 */
	public static void main(String[] args) {
		try {
			Mongo mongo = new Mongo(PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort());
			
			DB europeanaDB = mongo.getDB(PropertyUtils.getEuropeanaDB());
			DB collectionDB = mongo.getDB(PropertyUtils.getCollectionDB());
			DBCollection records = europeanaDB.getCollection("record");
			DBCollection coll = collectionDB.getCollection("Collection");
			 List<String> collections = IOUtils.readLines(new FileInputStream(
			 new File(args[0])));
//			 List<String> collections = IOUtils.readLines(new FileInputStream(
//					 new File("/home/gmamakis/results.csv")));
			BasicDBObject query;
			BasicDBObject colQuery;
			List<String> result = new ArrayList<String>();
			for (String collection : collections) {
				String stripped = StringUtils.split(collection, "_")[0];
				colQuery = new BasicDBObject("oldCollectionId", stripped);
				DBCursor cur = coll.find(colQuery);
				if (cur.hasNext()) {
					DBObject replaceCollection = cur.next();
					stripped = replaceCollection.get("newCollectionId")
							.toString();
				}
				query = new BasicDBObject("about", Pattern.compile("^/"
						+ stripped + "/.*"));
				
				System.out.println("Querying collection " + stripped);
				long start = new Date().getTime();

				int num = records.find(query).count();
				System.out.println("Found " + collection + ";" + num);
				System.out.println("Querying took "
						+ (new Date().getTime() - start) + " ms");
				result.add(collection + ";" + num);

			}

			File file = new File(args[1]);
			file.createNewFile();
			OutputStream os = new FileOutputStream(file);
			for (String col : result) {
				os.write(col.getBytes());
				os.write("\n".getBytes());
			}
			os.flush();
			os.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import eu.europeana.datamigration.ese2edm.mongo.MongoDeleter;

public class MongoCollectionDeleter extends MongoDeleter {
	public static void main(String[] args) {
		// try {
		// Mongo mongo = new Mongo(PropertyUtils.getMongoServer(),
		// PropertyUtils.getMongoPort());
		//
		// DB europeanaDB = mongo.getDB(PropertyUtils.getEuropeanaDB());
		// DB collectionDB = mongo.getDB(PropertyUtils.getCollectionDB());
		// DBCollection records = europeanaDB.getCollection("record");
		// DBCollection proxies = europeanaDB.getCollection("Proxy");
		// DBCollection providedCHOs = europeanaDB.getCollection("ProvidedCHO");
		// DBCollection aggregations = europeanaDB.getCollection("Aggregation");
		// DBCollection europeanaAggregations =
		// europeanaDB.getCollection("EuropeanaAggregation");
		//
		// DBCollection coll = collectionDB.getCollection("Collection");
		// List<String> collections = IOUtils.readLines(new FileInputStream(
		// new File(args[0])));
		// // List<String> collections = IOUtils.readLines(new FileInputStream(
		// // new File("/home/gmamakis/test_delete.csv")));
		// BasicDBObject query;
		// BasicDBObject proxyQuery;
		// BasicDBObject europeanaProxyQuery;
		// BasicDBObject providedCHOQuery;
		// BasicDBObject aggregationQuery;
		// BasicDBObject europeanaAggregationQuery;
		// BasicDBObject colQuery;
		// for (String collection : collections) {
		// String stripped = StringUtils.split(collection, "_")[0];
		// colQuery = new BasicDBObject("oldCollectionId", stripped);
		// DBCursor cur = coll.find(colQuery);
		// if (cur.hasNext()) {
		// DBObject replaceCollection = cur.next();
		// stripped = replaceCollection.get("newCollectionId")
		// .toString();
		// }
		// query = new BasicDBObject("about", Pattern.compile("^/"
		// + stripped + "/"));
		// proxyQuery = new BasicDBObject("about",
		// Pattern.compile("^/proxy/provider/"
		// + stripped + "/"));
		// europeanaProxyQuery = new BasicDBObject("about",
		// Pattern.compile("^/proxy/europeana/"
		// + stripped + "/"));
		// providedCHOQuery = new BasicDBObject("about",
		// Pattern.compile("^/item/"
		// + stripped + "/"));
		// aggregationQuery = new BasicDBObject("about",
		// Pattern.compile("^/aggregation/provider/"
		// + stripped + "/"));
		// europeanaAggregationQuery = new BasicDBObject("about",
		// Pattern.compile("^/aggregation/europeana/"
		// + stripped + "/"));
		// System.out.println("Querying collection " + stripped);
		// long start = new Date().getTime();
		//
		// records.remove(query,WriteConcern.JOURNAL_SAFE);
		// System.out.println("Removing Records took took "
		// + (new Date().getTime() - start) + " ms");
		// proxies.remove(europeanaProxyQuery,WriteConcern.JOURNAL_SAFE);
		// System.out.println("Removing Europeana Proxy took "
		// + (new Date().getTime() - start) + " ms");
		// proxies.remove(proxyQuery,WriteConcern.JOURNAL_SAFE);
		// System.out.println("Removing Provider Proxy took "
		// + (new Date().getTime() - start) + " ms");
		// providedCHOs.remove(providedCHOQuery,WriteConcern.JOURNAL_SAFE);
		// System.out.println("Removing ProvidedCHO took "
		// + (new Date().getTime() - start) + " ms");
		// aggregations.remove(aggregationQuery,WriteConcern.JOURNAL_SAFE);
		// System.out.println("Removing Aggregation took "
		// + (new Date().getTime() - start) + " ms");
		// europeanaAggregations.remove(europeanaAggregationQuery,WriteConcern.JOURNAL_SAFE);
		// System.out.println("Removed " + collection + " from Mongo");
		// System.out.println("Querying took "
		// + (new Date().getTime() - start) + " ms");
		//
		// HttpSolrServer solrServer = new
		// HttpSolrServer(PropertyUtils.getWriteServerUrl());
		// try {
		// solrServer.deleteByQuery("europeana_collectionName:"+stripped+"_*");
		// } catch (SolrServerException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		//
		//
		// } catch (UnknownHostException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (MongoException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		try {
			MongoDeleter mongoDeleter = new MongoCollectionDeleter();

			List<String> collections = IOUtils.readLines(new FileInputStream(
					new File(args[0])));
			mongoDeleter.initializeObjects();
			for (String collection : collections) {
				String stripped = mongoDeleter.findNewId(StringUtils.split(collection, "_")[0]);
				mongoDeleter.deleteObjects(mongoDeleter.setQueries(stripped),
						collection);
				mongoDeleter.deleteFromSolr("europeana_collectionName:"
						+ stripped);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<?> setQueries(String obj) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		patterns.add(Pattern.compile("^/" + obj + "/"));
		patterns.add(Pattern.compile("^/proxy/provider/" + obj + "/"));
		patterns.add(Pattern.compile("^/proxy/europeana/"+ obj + "/"));
		patterns.add(Pattern.compile("^/item/"+ obj + "/"));
		patterns.add(Pattern.compile("^/aggregation/provider/"+ obj + "/"));
		patterns.add(Pattern.compile("^/aggregation/europeana/" + obj + "/"));
		return patterns;
	}
}

package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import eu.europeana.datamigration.ese2edm.mongo.MongoDeleter;

public class MongoRecordDeleter extends MongoDeleter {

	public static void main(String[] args) {
		// try {
		// Mongo mongo = new Mongo(PropertyUtils.getMongoServer(),
		// PropertyUtils.getMongoPort());
		// List<String> recordsToDelete = IOUtils.readLines(new
		// FileInputStream(new File(args[0])));
		// HttpSolrServer solrServer = new
		// HttpSolrServer(PropertyUtils.getWriteServerUrl());
		// DB europeanaDB = mongo.getDB(PropertyUtils.getEuropeanaDB());
		// DB collectionDB = mongo.getDB(PropertyUtils.getCollectionDB());
		// DBCollection records = europeanaDB.getCollection("record");
		// DBCollection proxies = europeanaDB.getCollection("Proxy");
		// DBCollection providedCHOs = europeanaDB.getCollection("ProvidedCHO");
		// DBCollection aggregations = europeanaDB.getCollection("Aggregation");
		// DBCollection europeanaAggregations =
		// europeanaDB.getCollection("EuropeanaAggregation");
		// DBCollection coll = collectionDB.getCollection("Collection");
		// BasicDBObject query;
		// BasicDBObject proxyQuery;
		// BasicDBObject europeanaProxyQuery;
		// BasicDBObject providedCHOQuery;
		// BasicDBObject aggregationQuery;
		// BasicDBObject europeanaAggregationQuery;
		// BasicDBObject colQuery;
		// for(String recordToDelete:recordsToDelete){
		// String stripped = StringUtils.substringBetween(recordToDelete, "/",
		// "/");
		// colQuery = new BasicDBObject("oldCollectionId", stripped);
		// DBCursor cur = coll.find(colQuery);
		// if (cur.hasNext()) {
		// DBObject replaceCollection = cur.next();
		// stripped = replaceCollection.get("newCollectionId")
		// .toString();
		// }
		// String finalId = StringUtils.replace(recordToDelete,
		// StringUtils.substringBetween(recordToDelete, "/", "/"), stripped);
		// query = new BasicDBObject("about", finalId);
		// proxyQuery = new BasicDBObject("about", "/proxy/provider"
		// +finalId);
		// europeanaProxyQuery = new BasicDBObject("about", "/proxy/europeana"
		// + finalId);
		// providedCHOQuery = new BasicDBObject("about", "/item"
		// + finalId);
		// aggregationQuery = new BasicDBObject("about", "/aggregation/provider"
		// + finalId);
		// europeanaAggregationQuery = new
		// BasicDBObject("about","/aggregation/europeana"
		// + finalId);
		// System.out.println("Deleting record " + finalId +":"+
		// recordToDelete);
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
		// System.out.println("Removed " + recordToDelete + " from Mongo");
		// System.out.println("Deleting took "
		// + (new Date().getTime() - start) + " ms");
		// try {
		// solrServer.deleteByQuery("europeana_id:"+finalId);
		// } catch (SolrServerException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		try {
			MongoDeleter mongoDeleter = new MongoRecordDeleter();
			mongoDeleter.initializeObjects();
			List<String> recordsToDelete;

			recordsToDelete = IOUtils.readLines(new FileInputStream(new File(
					args[0])));

			for (String recordToDelete : recordsToDelete) {

				String stripped = mongoDeleter.findNewId(StringUtils.substringBetween(recordToDelete,
						"/", "/"));
				
				String finalId = StringUtils.replace(recordToDelete,
						StringUtils.substringBetween(recordToDelete, "/", "/"),
						stripped);

				mongoDeleter.deleteObjects(mongoDeleter.setQueries(finalId),
						null);
				mongoDeleter.deleteFromSolr("europeana_id:" + ClientUtils.escapeQueryChars(finalId));
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
	public List<?> setQueries(String query) {
		List<String> queries = new ArrayList<String>();
		queries.add(query);
		queries.add("/proxy/provider" + query);
		queries.add("/proxy/europeana"+ query);
		queries.add("/item"+ query);
		queries.add("/aggregation/provider"+ query);
		queries.add("/aggregation/europeana" + query);
		return queries;
	}
}

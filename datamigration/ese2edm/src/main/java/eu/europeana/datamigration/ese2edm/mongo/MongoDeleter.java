package eu.europeana.datamigration.ese2edm.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

public abstract class MongoDeleter {
	Mongo mongo;
	DB europeanaDB;
	DB collectionDB;
	DBCollection records;
	DBCollection proxies;
	DBCollection providedCHOs;
	DBCollection aggregations;
	DBCollection europeanaAggregations;
	DBCollection coll;
	BasicDBObject query;
	BasicDBObject proxyQuery;
	BasicDBObject europeanaProxyQuery;
	BasicDBObject providedCHOQuery;
	BasicDBObject aggregationQuery;
	BasicDBObject europeanaAggregationQuery;
	BasicDBObject colQuery;
	HttpSolrServer solrServer=new HttpSolrServer(PropertyUtils.getWriteServerUrl());
	public void initializeObjects(){
		try {
			mongo = new Mongo(PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		europeanaDB = mongo.getDB(PropertyUtils.getEuropeanaDB());
		collectionDB = mongo.getDB(PropertyUtils.getCollectionDB());
		records = europeanaDB.getCollection("record");
		proxies = europeanaDB.getCollection("Proxy");
		providedCHOs = europeanaDB.getCollection("ProvidedCHO");
		aggregations = europeanaDB.getCollection("Aggregation");
		europeanaAggregations = europeanaDB.getCollection("EuropeanaAggregation");
		
		coll = collectionDB.getCollection("Collection");
	}
	
	public void deleteFromSolr(String query){
		try {
			solrServer.deleteByQuery(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void deleteObjects(List<?> queries,String...collection){
//		query = new BasicDBObject("about", Pattern.compile("^/"
//				+ stripped + "/"));
//		proxyQuery = new BasicDBObject("about", Pattern.compile("^/proxy/provider/"
//				+ stripped + "/"));
//		europeanaProxyQuery = new BasicDBObject("about", Pattern.compile("^/proxy/europeana/"
//				+ stripped + "/"));
//		providedCHOQuery = new BasicDBObject("about", Pattern.compile("^/item/"
//				+ stripped + "/"));
//		aggregationQuery = new BasicDBObject("about", Pattern.compile("^/aggregation/provider/"
//				+ stripped + "/"));
//		europeanaAggregationQuery = new BasicDBObject("about", Pattern.compile("^/aggregation/europeana/"
//				+ stripped + "/"));
		
		query = new BasicDBObject("about", queries.get(0));
		proxyQuery = new BasicDBObject("about", queries.get(1));
		europeanaProxyQuery = new BasicDBObject("about", queries.get(2));
		providedCHOQuery = new BasicDBObject("about", queries.get(3));
		aggregationQuery = new BasicDBObject("about", queries.get(4));
		europeanaAggregationQuery = new BasicDBObject("about", queries.get(5));
		
		long start = new Date().getTime();
	
		records.remove(query,WriteConcern.JOURNAL_SAFE);
		System.out.println("Removing Records took took "
				+ (new Date().getTime() - start) + " ms");
		proxies.remove(europeanaProxyQuery,WriteConcern.JOURNAL_SAFE);
		System.out.println("Removing Europeana Proxy took "
				+ (new Date().getTime() - start) + " ms");
		proxies.remove(proxyQuery,WriteConcern.JOURNAL_SAFE);
		System.out.println("Removing Provider Proxy took "
				+ (new Date().getTime() - start) + " ms");
		providedCHOs.remove(providedCHOQuery,WriteConcern.JOURNAL_SAFE);
		System.out.println("Removing ProvidedCHO took "
				+ (new Date().getTime() - start) + " ms");
		aggregations.remove(aggregationQuery,WriteConcern.JOURNAL_SAFE);
		System.out.println("Removing Aggregation took "
				+ (new Date().getTime() - start) + " ms");
		europeanaAggregations.remove(europeanaAggregationQuery,WriteConcern.JOURNAL_SAFE);
		
		System.out.println("Removed " + collection!=null?collection:queries.get(0) + " from Mongo");
		
		System.out.println("Querying took "
				+ (new Date().getTime() - start) + " ms");
	}
	
	
	public String findNewId(String id){
		colQuery = new BasicDBObject("oldCollectionId",
				id);
		DBCursor cur = coll.find(colQuery);
		if (cur.hasNext()) {
			DBObject replaceCollection = cur.next();
			return replaceCollection.get("newCollectionId")
					.toString();
		}
		return id;
	}
	public abstract List<?> setQueries(String obj);

	
	
}

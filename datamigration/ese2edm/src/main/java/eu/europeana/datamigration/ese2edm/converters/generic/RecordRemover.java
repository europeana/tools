package eu.europeana.datamigration.ese2edm.converters.generic;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import eu.europeana.corelib.solr.server.EdmMongoServer;

public class RecordRemover {
	
	public void clearData(String record, EdmMongoServer edmMongoServer) {
		// TODO Auto-generated method stub
		DBCollection records = edmMongoServer.getDatastore().getDB()
				.getCollection("record");
		DBCollection proxies = edmMongoServer.getDatastore().getDB()
				.getCollection("Proxy");
		DBCollection providedCHOs = edmMongoServer.getDatastore().getDB()
				.getCollection("ProvidedCHO");
		DBCollection aggregations = edmMongoServer.getDatastore().getDB()
				.getCollection("Aggregation");
		DBCollection europeanaAggregations = edmMongoServer.getDatastore().getDB()
				.getCollection("EuropeanaAggregation");
		DBObject query = new BasicDBObject("about", record);
		DBObject proxyQuery = new BasicDBObject("about", "/proxy/provider"
				+ record);
		DBObject europeanaProxyQuery = new BasicDBObject("about",
				"/proxy/europeana" + record);

		DBObject providedCHOQuery = new BasicDBObject("about", "/item"
				+ record);
		DBObject aggregationQuery = new BasicDBObject("about",
				"/aggregation/provider" + record);
		DBObject europeanaAggregationQuery = new BasicDBObject("about",
				"/aggregation/europeana" + record);
		
		europeanaAggregations.remove(europeanaAggregationQuery);
		records.remove(query);
		proxies.remove(europeanaProxyQuery);
		proxies.remove(proxyQuery);
		providedCHOs.remove(providedCHOQuery);
		aggregations.remove(aggregationQuery);
	}
}

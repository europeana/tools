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
package eu.europeana.record.management.server.components;

import java.net.UnknownHostException;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

import eu.europeana.record.management.shared.dto.Record;
import org.apache.commons.lang3.StringUtils;

/**
 * Mongo Server implementation
 *
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class MongoServer implements Server {

    String url;

    public void deleteRecord(Record record) {
        try {
            Mongo mongo = new Mongo(url, 27017);
            DB europeanaDB = mongo.getDB("europeana");
            DBCollection records = europeanaDB.getCollection("record");
            DBCollection proxies = europeanaDB.getCollection("Proxy");
            DBCollection providedCHOs = europeanaDB
                    .getCollection("ProvidedCHO");
            DBCollection aggregations = europeanaDB
                    .getCollection("Aggregation");
            DBCollection europeanaAggregations = europeanaDB
                    .getCollection("EuropeanaAggregation");
            DBCollection physicalThings = europeanaDB.getCollection("PhysicalThing");
            DBObject query = new BasicDBObject("about", record.getValue());
            DBObject proxyQuery = new BasicDBObject("about", "/proxy/provider/"
                    + record.getValue());
            DBObject europeanaProxyQuery = new BasicDBObject("about",
                    "/proxy/europeana/" + record.getValue());
            DBObject providedCHOQuery = new BasicDBObject("about", "/item/"
                    + record.getValue());
            DBObject aggregationQuery = new BasicDBObject("about",
                    "/aggregation/provider/" + record.getValue());
            DBObject europeanaAggregationQuery = new BasicDBObject("about",
                    "/aggregation/europeana/" + record.getValue());

            records.remove(query, WriteConcern.JOURNAL_SAFE);
            proxies.remove(europeanaProxyQuery, WriteConcern.JOURNAL_SAFE);
            proxies.remove(proxyQuery, WriteConcern.JOURNAL_SAFE);
            physicalThings.remove(europeanaProxyQuery, WriteConcern.JOURNAL_SAFE);
            physicalThings.remove(proxyQuery, WriteConcern.JOURNAL_SAFE);
            providedCHOs.remove(providedCHOQuery, WriteConcern.JOURNAL_SAFE);
            aggregations.remove(aggregationQuery, WriteConcern.JOURNAL_SAFE);
            europeanaAggregations.remove(europeanaAggregationQuery,
                    WriteConcern.JOURNAL_SAFE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCollection(String collectionName) {

        try {
            Mongo mongo = new Mongo(url, 27017);
            DB europeanaDB = mongo.getDB("europeana");
            DBCollection records = europeanaDB.getCollection("record");
            DBCollection proxies = europeanaDB.getCollection("Proxy");
            DBCollection providedCHOs = europeanaDB
                    .getCollection("ProvidedCHO");
            DBCollection aggregations = europeanaDB
                    .getCollection("Aggregation");
            DBCollection europeanaAggregations = europeanaDB
                    .getCollection("EuropeanaAggregation");
            DBCollection physicalThings = europeanaDB.getCollection("PhysicalThing");
            if (!StringUtils.isNumeric(collectionName)){
                collectionName = StringUtils.substring(collectionName, 0, collectionName.length()-1);
            }
            DBObject query = new BasicDBObject("about", Pattern.compile("^/"
                    + collectionName + "/"));
            DBObject proxyQuery = new BasicDBObject("about",
                    Pattern.compile("^/proxy/provider/" + collectionName + "/"));
            DBObject europeanaProxyQuery = new BasicDBObject(
                    "about",
                    Pattern.compile("^/proxy/europeana/" + collectionName + "/"));
            DBObject providedCHOQuery = new BasicDBObject("about",
                    Pattern.compile("^/item/" + collectionName + "/"));
            DBObject aggregationQuery = new BasicDBObject("about",
                    Pattern.compile("^/aggregation/provider/" + collectionName
                            + "/"));
            DBObject europeanaAggregationQuery = new BasicDBObject("about",
                    Pattern.compile("^/aggregation/europeana/" + collectionName
                            + "/"));

            records.remove(query, WriteConcern.JOURNAL_SAFE);
            proxies.remove(europeanaProxyQuery, WriteConcern.JOURNAL_SAFE);
            proxies.remove(proxyQuery, WriteConcern.JOURNAL_SAFE);
            physicalThings.remove(europeanaProxyQuery, WriteConcern.JOURNAL_SAFE);
            physicalThings.remove(proxyQuery, WriteConcern.JOURNAL_SAFE);
            providedCHOs.remove(providedCHOQuery, WriteConcern.JOURNAL_SAFE);
            aggregations.remove(aggregationQuery, WriteConcern.JOURNAL_SAFE);
            europeanaAggregations.remove(europeanaAggregationQuery,
                    WriteConcern.JOURNAL_SAFE);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MongoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Record identifyRecord(Record record) {
        throw new UnsupportedOperationException();

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.extract;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.reindexing.common.ReindexingFields;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymamakis
 */
public class EnrichmentFieldsBolt extends BaseRichBolt {
    
    private String[] mongoAddresses;
    private EdmMongoServerImpl mongoServer;
    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
        ofd.declare(new Fields(ReindexingFields.TASKID, ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY, ReindexingFields.PARAMS));
    }

    @Override
    public void prepare(Map map, TopologyContext tc, OutputCollector oc) {
         List<ServerAddress> addresses = new ArrayList<>();
            for (String mongoStr : mongoAddresses) {
             try {
                 ServerAddress address;
                 
                address = new ServerAddress(mongoStr, 27017);
                addresses.add(address);
                Mongo mongo = new Mongo(addresses);
                mongoServer = new EdmMongoServerImpl(mongo, "europeana", null, null);
             } catch (UnknownHostException ex) {
                 Logger.getLogger(EnrichmentFieldsBolt.class.getName()).log(Level.SEVERE, null, ex);
             } catch (MongoDBException ex) {
                 Logger.getLogger(EnrichmentFieldsBolt.class.getName()).log(Level.SEVERE, null, ex);
             }
            }
           
    }

    @Override
    public void execute(Tuple tuple) {
        
    }
    
    
}

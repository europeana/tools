/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.enrichment;

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
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.ReindexingTuple;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author ymamakis
 */
public class EnrichmentBolt extends BaseRichBolt {

    private OutputCollector collector;
    
    private ObjectMapper om;
    private EnrichmentDriver driver;
    private EdmMongoServerImpl mongoServer;
    private String path;
    private String[] mongoAddresses;
    public EnrichmentBolt(String path, String[] mongoAddresses) {
        this.path = path;
        this.mongoAddresses= mongoAddresses;
    }

    
    
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
        ofd.declare(new Fields(ReindexingFields.TASKID, ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY, ReindexingFields.ENTITYWRAPPER));
    }

    @Override
    public void prepare(Map map, TopologyContext tc, OutputCollector oc) {
        this.collector = oc;
        driver = new EnrichmentDriver(path);
        om = new ObjectMapper();
        List<ServerAddress> addresses = new ArrayList<>();
            for (String mongoStr : mongoAddresses) {
            try {
                ServerAddress address;

                address = new ServerAddress(mongoStr, 27017);
                addresses.add(address);
                Mongo mongo = new Mongo(addresses);
            mongoServer = new EdmMongoServerImpl(mongo, "europeana", null, null);
            } catch (UnknownHostException | MongoDBException ex) {
                Logger.getLogger(EnrichmentBolt.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            
    }

    @Override
    public void execute(Tuple tuple) {
        ReindexingTuple task = ReindexingTuple.fromTuple(tuple);

        try {
            long startRetrieve = new Date().getTime();
            FullBeanImpl fBean =mongoServer.searchByAbout(FullBeanImpl.class, task.getIdentifier());
            
            Logger.getGlobal().log(Level.INFO,"Got bean with rdf:about "+ fBean.getAbout() + " in " + (new Date().getTime() - startRetrieve) + " ms");
           // cleanFullBean(fBean);
            List<InputValue> values = getEnrichmentFields(fBean);
            
            long startEnrich = new Date().getTime();
           
            List<EntityWrapper> entities = driver.enrich(values, false);
            
             Logger.getGlobal().log(Level.INFO,"Enrichemnt for "+ fBean.getAbout() + " took " + (new Date().getTime() - startEnrich) + " ms");
            EntityWrapperList lst = new EntityWrapperList();
            lst.setWrapperList(entities);
           // appendEntities(fBean, entities);
           
            collector.emit(tuple,new ReindexingTuple(task.getTaskId(), task.getIdentifier(), task.getNumFound(), task.getQuery(), om.writeValueAsString(lst)).toTuple());
            collector.ack(tuple);
        } catch (IOException ex) {
            Logger.getLogger(EnrichmentBolt.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    

    private List<InputValue> getEnrichmentFields(FullBeanImpl fBean) {
        ProxyImpl providerProxy = null;
        for (ProxyImpl proxy : fBean.getProxies()) {
            if (!proxy.isEuropeanaProxy()) {
                providerProxy = proxy;
                break;
            }
        }
        return EnrichmentFieldsCreator.extractEnrichmentFieldsFromProxy(providerProxy);
    }

   
}

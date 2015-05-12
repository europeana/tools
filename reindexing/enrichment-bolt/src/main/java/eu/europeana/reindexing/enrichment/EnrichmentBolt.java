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
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.ReindexingTuple;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ymamakis
 */
public class EnrichmentBolt extends BaseRichBolt {

    private OutputCollector collector;
    
    
    private EnrichmentDriver driver;
    private Jedis jedis;

    public EnrichmentBolt(String path, Jedis jedis) {
        this.jedis = jedis;
        driver = new EnrichmentDriver(path);
    }

    
    
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
        ofd.declare(new Fields(ReindexingFields.TASKID, ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY));
    }

    @Override
    public void prepare(Map map, TopologyContext tc, OutputCollector oc) {
        this.collector = oc;
    }

    @Override
    public void execute(Tuple tuple) {
        ReindexingTuple task = ReindexingTuple.fromTuple(tuple);

        byte[] serialized = jedis.get(task.getIdentifier().getBytes());
        try {
            ObjectInputStream oIn = new ObjectInputStream(new ByteArrayInputStream(serialized));
            FullBeanImpl fBean = (FullBeanImpl) oIn.readObject();
            cleanFullBean(fBean);
            List<InputValue> values = getEnrichmentFields(fBean);
            List<EntityWrapper> entities = driver.enrich(values, true);
            appendEntities(fBean, entities);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oOut = new ObjectOutputStream(bout);
            oOut.writeObject(fBean);
            jedis.set(fBean.getAbout().getBytes(), bout.toByteArray());
            collector.emit(new ReindexingTuple(task.getTaskId(), task.getIdentifier(), task.getNumFound(), task.getQuery()).toTuple());
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(EnrichmentBolt.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void cleanFullBean(FullBeanImpl fBean) {
        ProxyImpl europeanaProxy = null;
        int index = 0;
        for (ProxyImpl proxy : fBean.getProxies()) {
            if (proxy.isEuropeanaProxy()) {
                europeanaProxy = proxy;
                break;
            }
            index++;
        }

        europeanaProxy.setDcDate(null);
        europeanaProxy.setDcCoverage(null);
        europeanaProxy.setDctermsTemporal(null);
        europeanaProxy.setYear(null);
        europeanaProxy.setDctermsSpatial(null);
        europeanaProxy.setDcType(null);
        europeanaProxy.setDcSubject(null);
        europeanaProxy.setDcCreator(null);
        europeanaProxy.setDcContributor(null);

        fBean.getProxies().set(index, europeanaProxy);
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

    private void appendEntities(FullBeanImpl fBean, List<EntityWrapper> entities) {
        try {
            List<RetrievedEntity> enriched = convertToObjects(entities);
            ProxyImpl europeanaProxy = null;
            int index = 0;
            for (ProxyImpl proxy : fBean.getProxies()) {
                if (proxy.isEuropeanaProxy()) {
                    europeanaProxy = proxy;
                    break;
                }
                index++;
            }
            new EntityAppender().addEntities(fBean, europeanaProxy, enriched);
            fBean.getProxies().set(index, europeanaProxy);

        } catch (IOException ex) {
            Logger.getLogger(EnrichmentBolt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<RetrievedEntity> convertToObjects(
            List<EntityWrapper> enrichments) throws IOException {
        List<RetrievedEntity> entities = new ArrayList<>();
        for (EntityWrapper entity : enrichments) {
            RetrievedEntity ret = new RetrievedEntity();
            ret.setOriginalField(entity.getOriginalField());
            ret.setOriginalLabel(entity.getOriginalValue());
            ret.setUri(entity.getUrl());
            if (entity.getClassName().equals(TimespanImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), TimespanImpl.class));
            } else if (entity.getClassName().equals(AgentImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), AgentImpl.class));
            } else if (entity.getClassName().equals(ConceptImpl.class.getName())) {
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), ConceptImpl.class));
            } else {
                ret.setEntity(new ObjectMapper().readValue(entity.
                        getContextualEntity(), PlaceImpl.class));
            }
            entities.add(ret);
        }

        return entities;
    }
}

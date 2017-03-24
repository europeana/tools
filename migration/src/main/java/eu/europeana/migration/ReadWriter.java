/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * Read from a Mongo and a Solr and Write to a Mongo and Solr
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class ReadWriter implements Runnable {

  private List<SolrDocument> batches;
  private EdmMongoServer sourceMongo;
  private CountDownLatch latch;

  private SolrDocumentHandler solrHandler;
  private CloudSolrServer targetCloudSolr;
  private EdmMongoServer targetMongo;
  private FullBeanHandler mongoHandler;
  private EnrichmentDriver enrichmentDriver;
  private MigrationUtils utils = new MigrationUtils();


  public void setEnrichmentDriver(EnrichmentDriver enrichmentDriver) {
    this.enrichmentDriver = enrichmentDriver;
  }

  /**
   * To set all Ingestion settings at once.
   */

  public void setConnectionTargets(SolrDocumentHandler solrHandler,
      CloudSolrServer cloudServer,
      EdmMongoServer targetMongo,
      FullBeanHandler fBeanHandler) {
    setSolrHandler(solrHandler);
    setMongoHandler(fBeanHandler);
    setTargetMongo(targetMongo);
    setTargetCloudSolr(cloudServer);
  }

  @Override
  public void run() {
    try {



    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      latch.countDown();
    }
  }

  private void save(SolrDocumentHandler solrHandlerProd,
      CloudSolrServer cloudServerProd,
      EdmMongoServer targetMongoProd,
      FullBeanHandler fBeanHandlerProd,
      EnrichmentDriver driver) {
    List<SolrInputDocument> docList = new ArrayList<>();
    //For every document
    for (SolrDocument doc : batches) {
      //Fix bug with double slashes
      String id = doc.getFieldValue("europeana_id").toString();
      if (id.startsWith("//")) {
        id = id.replace("//", "/");
      }
      try {
        //Find the bean
        FullBeanImpl fBean = (FullBeanImpl) sourceMongo.getFullBean(id);

        //Replace empty timestampCreated with 0 (to make it searchable)
        if (fBean.getTimestampCreated() == null) {
          fBean.setTimestampCreated(new Date(0));
        }
        if (fBean.getTimestampUpdated() == null) {
          fBean.setTimestampUpdated(new Date(0));
        }
        if (fBean.getAggregations().get(0).getEdmUgc() == null) {
          AggregationImpl aggr = fBean.getAggregations().get(0);
          aggr.setEdmUgc("false");
          fBean.getAggregations().set(0, aggr);
        }
        //Adding missing rdf:resource in the EuropeanaAggregation and aggregation
        String pchoAbout = fBean.getProvidedCHOs().get(0).getAbout();
        EuropeanaAggregation aggr = fBean.getEuropeanaAggregation();
        aggr.setAggregatedCHO(pchoAbout);
        fBean.setEuropeanaAggregation(aggr);
        AggregationImpl aggregation = fBean.getAggregations().get(0);
        aggregation.setAggregatedCHO(pchoAbout);
        fBean.getAggregations().set(0, aggregation);
        appendWebResources(fBean);

        //Fix for the edm:rights
        if (fBean.getAggregations().get(0).getEdmRights() != null) {

          Map<String, List<String>> edmRights = fBean.getAggregations().get(0).getEdmRights();
          for (String rightsKey : edmRights.keySet()) {
            List<String> rights = edmRights.get(rightsKey);
            if (rights.size() > 1) {
              List<String> filteredRights = new ArrayList<>();
              for (String right : rights) {
                if (StringUtils.isNotBlank(right)) {
                  filteredRights.add(right);
                  break;
                }
              }
              edmRights.put(rightsKey, filteredRights);
            }
          }

          fBean.getAggregations().get(0).setEdmRights(edmRights);
        }
        //Generate Solr document from bean
        clean(fBean);
        enrich(fBean);

        SolrInputDocument inputDoc = solrHandlerProd.generate(fBean);

        //Add to list for saving later
        docList.add(inputDoc);

        //Save the individual classes in the Mongo cluster
        fBeanHandlerProd.saveEdmClasses(fBean, true);
        //mongoHandler.saveEdmClasses(fBean,true);
        //and then save the records themselves (this does not happen in one go, because of UIM)
        targetMongoProd.getDatastore().save(fBean);
        //targetMongo.getDatastore().save(fBean);
      } catch (Exception ex) {
        Logger.getLogger(Migration.class.getName())
            .log(Level.SEVERE, "Got exception for id: " + id, ex);

      }

    }

    try {
      //add the documents in Solr..they will become available..no need to commit.. PATIENZA
      // cloudServer.add(docList);
      cloudServerProd.add(docList);
    } catch (SolrServerException | IOException ex) {
      Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void clean(FullBeanImpl fBean) {

    ProxyImpl provProxy = findProviderProxy(fBean);
    ProxyImpl euProxy = findEuropeanaProxy(fBean);

    Set<String> uris = utils.extractAllUris(provProxy);

    List<AgentImpl> cloneAgent = new ArrayList<>();
    List<PlaceImpl> clonePlaces = new ArrayList<>();
    List<ConceptImpl> cloneConcepts = new ArrayList<>();
    List<TimespanImpl> cloneTimespans = new ArrayList<>();

    for (String uri : uris) {

      if (fBean.getAgents() != null) {
        for (AgentImpl agent : fBean.getAgents()) {
          if (StringUtils.equals(agent.getAbout(), uri)) {
            cloneAgent.add(agent);
          }
        }

      }

      if (fBean.getConcepts() != null) {
        for (ConceptImpl agent : fBean.getConcepts()) {
          if (StringUtils.equals(agent.getAbout(), uri)) {
            cloneConcepts.add(agent);
          }
        }
      }

      if (fBean.getTimespans() != null) {
        for (TimespanImpl agent : fBean.getTimespans()) {
          if (StringUtils.equals(agent.getAbout(), uri)) {
            cloneTimespans.add(agent);
          }
        }
      }

      if (fBean.getPlaces() != null) {
        for (PlaceImpl agent : fBean.getPlaces()) {
          if (StringUtils.equals(agent.getAbout(), uri)) {
            clonePlaces.add(agent);
          }
        }
      }

    }
    fBean.setConcepts(cloneConcepts);
    fBean.setAgents(cloneAgent);
    fBean.setPlaces(clonePlaces);
    fBean.setTimespans(cloneTimespans);
    fBean.getProxies().remove(findEuropeanaProxyPosition(fBean));
    fBean.getProxies().add(new MigrationUtils().cleanEuropeanaProxy(euProxy));
  }

  private void enrich(FullBeanImpl fBean) {
    ProxyImpl provProxy = findProviderProxy(fBean);
    List<LangValue> provProxyValues = utils.extractValuesFromProxy(provProxy);
    if (provProxyValues.size() > 0) {
      List<InputValue> values = new ArrayList<>();
      for (LangValue val : provProxyValues) {
        for (String langVal : val.getValue()) {
          InputValue value = new InputValue();
          value.setLanguage(val.getLanguage());
          value.setValue(langVal);
          List<EntityClass> vocs = new ArrayList<>();
          vocs.add(EntityClass.valueOf(val.getVocabulary()));
          value.setVocabularies(vocs);
          value.setOriginalField(val.getOriginalField());
          values.add(value);
        }
      }

      try {

        List<EntityWrapper> entities = enrichmentDriver.enrich(values, false);
        mergeEntitiesToBean(entities, fBean);


      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void mergeEntitiesToBean(List<EntityWrapper> entities, FullBeanImpl fBean)
      throws IOException {
    ProxyImpl euProxy = findEuropeanaProxy(fBean);
    if (entities != null && entities.size() > 0) {
      for (EntityWrapper entity : entities) {
        ProxyImpl euProxyRet = utils.addValueToProxy(entity, euProxy);
        replaceProxy(fBean, euProxyRet);
        utils.addContextualClassToBean(fBean, entity);
      }
    }

  }

  private void replaceProxy(FullBeanImpl fBean, ProxyImpl proxy) {
    List<ProxyImpl> proxies = fBean.getProxies();
    int i = 0;
    for (ProxyImpl pr : proxies) {
      if (StringUtils.equals(pr.getAbout(), proxy.getAbout())) {
        proxies.set(i, proxy);
      }
      i++;
    }
    fBean.setProxies(proxies);
  }

  private ProxyImpl findEuropeanaProxy(FullBeanImpl fBean) {
    for (ProxyImpl proxy : fBean.getProxies()) {
      if (proxy.isEuropeanaProxy()) {
        return proxy;
      }
    }
    return null;
  }

  private int findEuropeanaProxyPosition(FullBeanImpl fBean) {
    int i = 0;
    for (ProxyImpl proxy : fBean.getProxies()) {
      if (proxy.isEuropeanaProxy()) {
        return i;
      }
      i++;
    }
    return 0;
  }

  private ProxyImpl findProviderProxy(FullBeanImpl fBean) {
    for (ProxyImpl proxy : fBean.getProxies()) {
      if (!proxy.isEuropeanaProxy()) {
        return proxy;
      }
    }
    return null;
  }

  /**
   * Create web resources for edm:object, edm:isShownBy, edm:isShownAt and edm:hasView
   */
  private void appendWebResources(FullBeanImpl fBean) {
    AggregationImpl aggregation = fBean.getAggregations().get(0);
    Set<String> resources = new HashSet<>();
    if (aggregation.getEdmIsShownBy() != null) {
      resources.add(aggregation.getEdmIsShownBy());
    }
    if (aggregation.getEdmIsShownAt() != null) {
      resources.add(aggregation.getEdmIsShownAt());
    }
    if (aggregation.getEdmObject() != null) {
      resources.add(aggregation.getEdmObject());
    }
    String[] hasView = aggregation.getHasView();
    if (hasView != null) {
      for (String str : hasView) {
        resources.add(str);
      }
    }
    List<WebResourceImpl> wrs = (List<WebResourceImpl>) aggregation.getWebResources();
    List<WebResourceImpl> toAdd = new ArrayList<>();
    for (String res : resources) {
      boolean exists = false;
      for (WebResourceImpl wr : wrs) {
        if (StringUtils.equals(res, wr.getAbout())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        WebResourceImpl wr = new WebResourceImpl();
        wr.setAbout(res);
        toAdd.add(wr);
      }
    }
    wrs.addAll(toAdd);
    fBean.getAggregations().get(0).setWebResources(wrs);
  }


  public List<SolrDocument> getBatches() {
    return batches;
  }

  public void setBatches(List<SolrDocument> batches) {
    this.batches = batches;
  }

  public EdmMongoServer getSourceMongo() {
    return sourceMongo;
  }

  public void setSourceMongo(EdmMongoServer sourceMongo) {
    this.sourceMongo = sourceMongo;
  }

  public void setLatch(CountDownLatch latch) {
    this.latch = latch;
  }

  public SolrDocumentHandler getSolrHandler() {
    return solrHandler;
  }

  public void setSolrHandler(SolrDocumentHandler solrHandler) {
    this.solrHandler = solrHandler;
  }

  public CloudSolrServer getTargetCloudSolr() {
    return targetCloudSolr;
  }

  public void setTargetCloudSolr(CloudSolrServer targetCloudSolr) {
    this.targetCloudSolr = targetCloudSolr;
  }

  public EdmMongoServer getTargetMongo() {
    return targetMongo;
  }

  public void setTargetMongo(EdmMongoServer targetMongo) {
    this.targetMongo = targetMongo;
  }

  public FullBeanHandler getMongoHandler() {
    return mongoHandler;
  }

  public void setMongoHandler(FullBeanHandler mongoHandler) {
    this.mongoHandler = mongoHandler;
  }

}

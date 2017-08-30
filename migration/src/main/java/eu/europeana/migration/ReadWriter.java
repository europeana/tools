/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer.RemoteSolrException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read from a Mongo and a Solr and Write to a Mongo and Solr
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class ReadWriter implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReadWriter.class);
  private List<String> idsBatch;
  private final int counterBatch;
  private final int counterSegment;
  private EdmMongoServer sourceMongo;
  private CountDownLatch latch;

  private SolrDocumentHandler solrHandler;
  private CloudSolrServer targetCloudSolr;
  private EdmMongoServer targetMongo;
  private FullBeanHandler mongoHandler;
  private EnrichmentDriver enrichmentDriver;
  private MigrationUtils utils = new MigrationUtils();
  private String europeana_id = null;

  private HttpSolrServer sourceSolr;

  public ReadWriter(List<String> idsBatch, int counterBatch, int counterSegment) {
    this.idsBatch = idsBatch;
    this.counterBatch = counterBatch;
    this.counterSegment = counterSegment;
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Uncaught Exception caught when processing id: " + europeana_id, e);
        LOGGER.error(LogMarker.errorIdsUncaughtMarker, europeana_id);
        LOGGER.error(LogMarker.errorIdsMarker, europeana_id);
      }
    });
  }

  public void setEnrichmentDriver(EnrichmentDriver enrichmentDriver) {
    this.enrichmentDriver = enrichmentDriver;
  }

  /**
   * To set all Ingestion settings at once.
   */

  public void setConnectionTargets(SolrDocumentHandler solrHandler, CloudSolrServer cloudServer,
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
      LOGGER.info(LogMarker.currentStateMarker,
          "*** MIGRATING BATCH: " + counterBatch + ", SEGMENT: " + counterSegment + ", WITH SIZE: "
              + idsBatch.size() + " ***");
      List<SolrInputDocument> docList = new ArrayList<>();
      for (int i = 0; i < idsBatch.size(); i++) {
        europeana_id = idsBatch.get(i);

        FullBeanImpl fBean;
        try {
          fBean = (FullBeanImpl) sourceMongo.getFullBean(europeana_id);
        } catch (MongoDBException | RuntimeException e) {
          LOGGER.error("Could not retrieve fullbean with id: " + europeana_id + " from mongo", e);
          LOGGER.error(LogMarker.errorIdsMarker, europeana_id);
          continue;
        }
        if (fBean == null) {
          LOGGER.error("Fullbean return null with id: " + europeana_id + " from mongo");
          LOGGER.error(LogMarker.errorIdsNullMongoMarker, europeana_id);
          LOGGER.error(LogMarker.errorIdsMarker, europeana_id);
          continue;
        }

//        removeSemiumTimespanEntities(fBean);
//        removeSemiumReferences(fBean);
//        enrich(fBean);
//        addFalseToNullUgc(fBean);

        SolrInputDocument inputDoc;
        try {
          inputDoc = solrHandler.generate(fBean);
        } catch (SolrServerException e) {
          LOGGER
              .error("Could not convert fullbean to solrInputDocument with id: " + europeana_id, e);
          LOGGER.error(LogMarker.errorIdsMarker, europeana_id);
          continue;
        }

        //Add to list for saving later
        docList.add(inputDoc);

//        //Save the individual classes in the Mongo cluster
//        try {
//          mongoHandler.saveEdmClasses(fBean, true);
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//          LOGGER.error("Error when saving edm classes with id: " + europeana_id + " in mongo", e);
//          LOGGER.error(LogMarker.errorIdsMarker, europeana_id);
////          i--;
//          continue;
//        }
//        //and then save the records themselves (this does not happen in one go, because of UIM)
//        targetMongo.getDatastore().save(fBean);
      }
      if (docList.size() != 0) {
        try {
          //add documents to Solr, no need to commit, they will become available
          targetCloudSolr.add(docList);
        } catch (SolrServerException | IOException | RemoteSolrException ex) {
          LOGGER.error(
              "Error when adding list of documents with size: " + docList.size() + " in solr", ex);
          for (SolrInputDocument solrInputDocument : docList
              ) {
            LOGGER.error(LogMarker.errorIdsMissingStreamMarker,
                solrInputDocument.getFieldValue("europeana_id").toString());
            LOGGER.error(LogMarker.errorIdsMarker,
                solrInputDocument.getFieldValue("europeana_id").toString());
          }
        }
      }
    } finally {
      LOGGER.info(LogMarker.currentStateMarker,
          "*** MIGRATED BATCH: " + counterBatch + ", SEGMENT: " + counterSegment + ", WITH SIZE: "
              + idsBatch.size() + " ***");
      latch.countDown();
    }
  }

  private void removeSemiumTimespanEntities(FullBeanImpl fullBean) {
    List<TimespanImpl> timespans = fullBean.getTimespans();
    timespans.removeIf(new Predicate<TimespanImpl>() {
      @Override
      public boolean test(TimespanImpl timespan) {
        boolean semium = StringUtils.contains(timespan.getAbout(), "semium");
        if (semium) {
          LOGGER.info("Removing Timespan Entity with about: " + timespan.getAbout() + " from id: "
              + europeana_id);
        }
        return semium;
      }
    });
  }

  private void removeSemiumReferences(FullBeanImpl fullBean) {
    ProxyImpl europeanaProxy = findEuropeanaProxy(fullBean);
    if (europeanaProxy != null) {
      removeSemiumDcDate(europeanaProxy);
      removeSemiumDctermsCreated(europeanaProxy);
      removeSemiumDctermsIssued(europeanaProxy);
      removeSemiumDctermsSpatial(europeanaProxy);
      removeSemiumDctermsTemporal(europeanaProxy);
    }
  }

  private void removeSemiumDcDate(ProxyImpl proxy) {
    Map<String, List<String>> dcDateMap = proxy.getDcDate();
    if (dcDateMap != null) {
      removeSemiumFromMap(dcDateMap);
      if (dcDateMap.size() == 0) {
        proxy.setDcDate(null);
      }
    }
  }

  private void removeSemiumDctermsCreated(ProxyImpl proxy) {
    Map<String, List<String>> dctermsCreated = proxy.getDctermsCreated();
    if (dctermsCreated != null) {
      removeSemiumFromMap(dctermsCreated);
      if (dctermsCreated.size() == 0) {
        proxy.setDctermsCreated(null);
      }
    }
  }

  private void removeSemiumDctermsIssued(ProxyImpl proxy) {
    Map<String, List<String>> dctermsIssued = proxy.getDctermsIssued();
    if (dctermsIssued != null) {
      removeSemiumFromMap(dctermsIssued);
      if (dctermsIssued.size() == 0) {
        proxy.setDctermsIssued(null);
      }
    }
  }

  private void removeSemiumDctermsSpatial(ProxyImpl proxy) {
    Map<String, List<String>> dctermsSpatial = proxy.getDctermsSpatial();
    if (dctermsSpatial != null) {
      removeSemiumFromMap(dctermsSpatial);
      if (dctermsSpatial.size() == 0) {
        proxy.setDctermsSpatial(null);
      }
    }
  }

  private void removeSemiumDctermsTemporal(ProxyImpl proxy) {
    Map<String, List<String>> dctermsTemporal = proxy.getDctermsTemporal();
    if (dctermsTemporal != null) {
      removeSemiumFromMap(dctermsTemporal);
      if (dctermsTemporal.size() == 0) {
        proxy.setDctermsTemporal(null);
      }
    }
  }

  private void removeSemiumFromMap(Map<String, List<String>> map) {
    for (Iterator<Entry<String, List<String>>> it = map.entrySet().iterator();
        it.hasNext(); ) {
      Entry<String, List<String>> entry = it.next();
      List<String> valueList = entry.getValue();
      valueList.removeIf(new Predicate<String>() {
        @Override
        public boolean test(String value) {
          boolean semium = StringUtils.contains(value, "semium");
          if (semium) {
            LOGGER.info("Removing reference with value: " + value + " from id: " + europeana_id);
          }
          return semium;
        }
      });
      if (valueList.isEmpty()) {
        it.remove();
      }
    }
  }

  private void addFalseToNullUgc(FullBeanImpl fBean) {
    if (fBean.getAggregations().get(0).getEdmUgc() == null || fBean.getAggregations().get(0)
        .getEdmUgc().equals("")) {
      AggregationImpl aggr = fBean.getAggregations().get(0);
      aggr.setEdmUgc("false");
      fBean.getAggregations().set(0, aggr);
    }
  }

  private void enrich(FullBeanImpl fBean) {
    ProxyImpl provProxy = findProviderProxy(fBean);
    List<LangValue> provProxyValues = utils.extractValuesFromProxy(provProxy);
    //Convert LangValue list to a list of InputValue that enrichment accepts
    if (provProxyValues.size() > 0) {
      List<InputValue> inputValues = new ArrayList<>();
      for (LangValue langValue : provProxyValues) {
        for (String value : langValue.getValues()) {
          InputValue inputValue = new InputValue();
          inputValue.setLanguage(langValue.getLanguage());
          inputValue.setValue(value);
          List<EntityClass> vocabularies = new ArrayList<>();
          vocabularies.add(EntityClass.valueOf(langValue.getVocabulary()));
          inputValue.setVocabularies(vocabularies);
          inputValue.setOriginalField(langValue.getOriginalField());
          inputValues.add(inputValue);
        }
      }

      try {
        List<EntityWrapper> entities = enrichmentDriver.enrich(inputValues, false);
        mergeEntitiesToBean(entities, fBean);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void mergeEntitiesToBean(List<EntityWrapper> entities, FullBeanImpl fBean)
      throws IOException {
    if (entities != null && entities.size() > 0) {
      ProxyImpl europeanaProxy = findEuropeanaProxy(fBean);
      if (europeanaProxy == null) {
        System.out.println("europeana proxy is null");
      }
      for (EntityWrapper entity : entities) {
        utils.addValueToProxy(entity, europeanaProxy);
        utils.addContextualClassToBean(fBean, entity);
      }
    }
  }

  private ProxyImpl findProviderProxy(FullBeanImpl fBean) {
    for (ProxyImpl proxy : fBean.getProxies()) {
      if (!proxy.isEuropeanaProxy()) {
        return proxy;
      }
    }
    return null;
  }

  private ProxyImpl findEuropeanaProxy(FullBeanImpl fBean) {
    for (ProxyImpl proxy : fBean.getProxies()) {
      if (proxy.isEuropeanaProxy()) {
        return proxy;
      }
    }
    return null;
  }

  private void addTimestampSolrFields(FullBeanImpl fBean, SolrInputDocument inputDoc)
  {

  }

//  private void replaceProxy(FullBeanImpl fBean, ProxyImpl proxy) {
//    List<ProxyImpl> proxies = fBean.getProxies();
//    int i = 0;
//    for (ProxyImpl pr : proxies) {
//      if (StringUtils.equals(pr.getAbout(), proxy.getAbout())) {
//        proxies.set(i, proxy);
//      }
//      i++;
//    }
//    fBean.setProxies(proxies);
//  }

//  private void save(SolrDocumentHandler solrHandlerProd,
//      CloudSolrServer cloudServerProd,
//      EdmMongoServer targetMongoProd,
//      FullBeanHandler fBeanHandlerProd,
//      EnrichmentDriver driver) {
//    List<SolrInputDocument> docList = new ArrayList<>();
//    //For every document
//    for (SolrDocument doc : solrDocuments) {
//      //Fix bug with double slashes
//      String id = doc.getFieldValue("europeana_id").toString();
//      if (id.startsWith("//")) {
//        id = id.replace("//", "/");
//      }
//      try {
//        //Find the bean
//        FullBeanImpl fBean = (FullBeanImpl) sourceMongo.getFullBean(id);
//
//        //Replace empty timestampCreated with 0 (to make it searchable)
//        if (fBean.getTimestampCreated() == null) {
//          fBean.setTimestampCreated(new Date(0));
//        }
//        if (fBean.getTimestampUpdated() == null) {
//          fBean.setTimestampUpdated(new Date(0));
//        }
//        if (fBean.getAggregations().get(0).getEdmUgc() == null) {
//          AggregationImpl aggr = fBean.getAggregations().get(0);
//          aggr.setEdmUgc("false");
//          fBean.getAggregations().set(0, aggr);
//        }
//        //Adding missing rdf:resource in the EuropeanaAggregation and aggregation
//        String pchoAbout = fBean.getProvidedCHOs().get(0).getAbout();
//        EuropeanaAggregation aggr = fBean.getEuropeanaAggregation();
//        aggr.setAggregatedCHO(pchoAbout);
//        fBean.setEuropeanaAggregation(aggr);
//        AggregationImpl aggregation = fBean.getAggregations().get(0);
//        aggregation.setAggregatedCHO(pchoAbout);
//        fBean.getAggregations().set(0, aggregation);
//        appendWebResources(fBean);
//
//        //Fix for the edm:rights
//        if (fBean.getAggregations().get(0).getEdmRights() != null) {
//
//          Map<String, List<String>> edmRights = fBean.getAggregations().get(0).getEdmRights();
//          for (String rightsKey : edmRights.keySet()) {
//            List<String> rights = edmRights.get(rightsKey);
//            if (rights.size() > 1) {
//              List<String> filteredRights = new ArrayList<>();
//              for (String right : rights) {
//                if (StringUtils.isNotBlank(right)) {
//                  filteredRights.add(right);
//                  break;
//                }
//              }
//              edmRights.put(rightsKey, filteredRights);
//            }
//          }
//
//          fBean.getAggregations().get(0).setEdmRights(edmRights);
//        }
//        //Generate Solr document from bean
//        clean(fBean);
//        enrich(fBean);
//
//        SolrInputDocument inputDoc = solrHandlerProd.generate(fBean);
//
//        //Add to list for saving later
//        docList.add(inputDoc);
//
//        //Save the individual classes in the Mongo cluster
//        fBeanHandlerProd.saveEdmClasses(fBean, true);
//        //mongoHandler.saveEdmClasses(fBean,true);
//        //and then save the records themselves (this does not happen in one go, because of UIM)
//        targetMongoProd.getDatastore().save(fBean);
//        //targetMongo.getDatastore().save(fBean);
//      } catch (Exception ex) {
//        Logger.getLogger(Migration.class.getName())
//            .log(Level.SEVERE, "Got exception for id: " + id, ex);
//
//      }
//
//    }
//
//    try {
//      //add the documents in Solr..they will become available..no need to commit.. PATIENZA
//      // cloudServer.add(docList);
//      cloudServerProd.add(docList);
//    } catch (SolrServerException | IOException ex) {
//      Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
//    }
//  }
//
//  private void clean(FullBeanImpl fBean) {
//
//    ProxyImpl provProxy = findProviderProxy(fBean);
//    ProxyImpl euProxy = findEuropeanaProxy(fBean);
//
//    Set<String> uris = utils.extractAllUris(provProxy);
//
//    List<AgentImpl> cloneAgent = new ArrayList<>();
//    List<PlaceImpl> clonePlaces = new ArrayList<>();
//    List<ConceptImpl> cloneConcepts = new ArrayList<>();
//    List<TimespanImpl> cloneTimespans = new ArrayList<>();
//
//    for (String uri : uris) {
//
//      if (fBean.getAgents() != null) {
//        for (AgentImpl agent : fBean.getAgents()) {
//          if (StringUtils.equals(agent.getAbout(), uri)) {
//            cloneAgent.add(agent);
//          }
//        }
//
//      }
//
//      if (fBean.getConcepts() != null) {
//        for (ConceptImpl agent : fBean.getConcepts()) {
//          if (StringUtils.equals(agent.getAbout(), uri)) {
//            cloneConcepts.add(agent);
//          }
//        }
//      }
//
//      if (fBean.getTimespans() != null) {
//        for (TimespanImpl agent : fBean.getTimespans()) {
//          if (StringUtils.equals(agent.getAbout(), uri)) {
//            cloneTimespans.add(agent);
//          }
//        }
//      }
//
//      if (fBean.getPlaces() != null) {
//        for (PlaceImpl agent : fBean.getPlaces()) {
//          if (StringUtils.equals(agent.getAbout(), uri)) {
//            clonePlaces.add(agent);
//          }
//        }
//      }
//
//    }
//    fBean.setConcepts(cloneConcepts);
//    fBean.setAgents(cloneAgent);
//    fBean.setPlaces(clonePlaces);
//    fBean.setTimespans(cloneTimespans);
//    fBean.getProxies().remove(findEuropeanaProxyPosition(fBean));
//    fBean.getProxies().add(new MigrationUtils().cleanEuropeanaProxy(euProxy));
//  }
//

//
//
//  private int findEuropeanaProxyPosition(FullBeanImpl fBean) {
//    int i = 0;
//    for (ProxyImpl proxy : fBean.getProxies()) {
//      if (proxy.isEuropeanaProxy()) {
//        return i;
//      }
//      i++;
//    }
//    return 0;
//  }
//
//
//  /**
//   * Create web resources for edm:object, edm:isShownBy, edm:isShownAt and edm:hasView
//   */
//  private void appendWebResources(FullBeanImpl fBean) {
//    AggregationImpl aggregation = fBean.getAggregations().get(0);
//    Set<String> resources = new HashSet<>();
//    if (aggregation.getEdmIsShownBy() != null) {
//      resources.add(aggregation.getEdmIsShownBy());
//    }
//    if (aggregation.getEdmIsShownAt() != null) {
//      resources.add(aggregation.getEdmIsShownAt());
//    }
//    if (aggregation.getEdmObject() != null) {
//      resources.add(aggregation.getEdmObject());
//    }
//    String[] hasView = aggregation.getHasView();
//    if (hasView != null) {
//      for (String str : hasView) {
//        resources.add(str);
//      }
//    }
//    List<WebResourceImpl> wrs = (List<WebResourceImpl>) aggregation.getWebResources();
//    List<WebResourceImpl> toAdd = new ArrayList<>();
//    for (String res : resources) {
//      boolean exists = false;
//      for (WebResourceImpl wr : wrs) {
//        if (StringUtils.equals(res, wr.getAbout())) {
//          exists = true;
//          break;
//        }
//      }
//      if (!exists) {
//        WebResourceImpl wr = new WebResourceImpl();
//        wr.setAbout(res);
//        toAdd.add(wr);
//      }
//    }
//    wrs.addAll(toAdd);
//    fBean.getAggregations().get(0).setWebResources(wrs);
//  }

//  public void setIdsBatch(List<String> idsBatch) {
//    this.idsBatch = idsBatch;
//  }


  public HttpSolrServer getSourceSolr() {
    return sourceSolr;
  }

  public void setSourceSolr(HttpSolrServer sourceSolr) {
    this.sourceSolr = sourceSolr;
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

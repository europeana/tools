/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.sun.source.tree.AssertTree;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.definitions.edm.entity.Timespan;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.MongoUtils;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;

/**
 * Integration test over a sample (or more than a sample) of the migrated data
 *
 * @author yorgos.mamakis@ europeana.eu
 */
public class MigrationTest {

    private static EdmMongoServer sourceMongo;

    private static CloudSolrServer targetSolrIngst;
    private static EdmMongoServer targetMongoIngst;
    
    private static CloudSolrServer targetSolrProd;
    private static EdmMongoServer targetMongoProd;
    
    private static Properties properties;
    private static int i=0;
    @Before
    public void setup() {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(new File("src/test/resources/migration.properties")));
            String srcMongoUrl = properties.getProperty("source.mongo");
            String zookeeperHost = properties.getProperty("zookeeper.host");

            String[] targetSolrUrlIngst = properties.getProperty("target.ingestion.solr").split(",");
            String[] targetMongoUrlIngst = properties.getProperty("target.ingestion.mongo").split(",");
            String targetCollectionIngst = properties.getProperty("target.ingestion.collection");
            
            String[] targetSolrUrlProd = properties.getProperty("target.production.solr").split(",");
            String[] targetMongoUrlProd = properties.getProperty("target.production.mongo").split(",");
            String targetCollectionProd = properties.getProperty("target.production.collection");

            //Connect to source Solr and Mongo
            Mongo mongo = new MongoClient(srcMongoUrl, 27017);
            sourceMongo = new EdmMongoServerImpl(mongo, "europeana", null, null);
            
            //Connect to target Solr and Mongo (ingestion)
            LBHttpSolrServer lbTargetIngst = new LBHttpSolrServer(targetSolrUrlIngst);
            targetSolrIngst = new CloudSolrServer(zookeeperHost, lbTargetIngst);
            targetSolrIngst.setDefaultCollection(targetCollectionIngst);
            targetSolrIngst.connect();
            List<ServerAddress> addressesIngst = new ArrayList<>();
            for (String mongoStr : targetMongoUrlIngst) {
                ServerAddress address = new ServerAddress(mongoStr, 27017);
                addressesIngst.add(address);
            }
            Mongo tgtMongoIngst = new MongoClient(addressesIngst);
            targetMongoIngst = new EdmMongoServerImpl(tgtMongoIngst, "europeana", null, null);
            
            
            //Connect to target Solr and Mongo (production)
            LBHttpSolrServer lbTargetProd = new LBHttpSolrServer(targetSolrUrlProd);
            targetSolrProd = new CloudSolrServer(zookeeperHost, lbTargetProd);
            targetSolrProd.setDefaultCollection(targetCollectionProd);
            targetSolrProd.connect();
            List<ServerAddress> addressesProd = new ArrayList<>();
            for (String mongoStr : targetMongoUrlProd) {
                ServerAddress address = new ServerAddress(mongoStr, 27017);
                addressesProd.add(address);
            }
            Mongo tgtMongoProd = new MongoClient(addressesProd);
            targetMongoProd = new EdmMongoServerImpl(tgtMongoProd, "europeana", null, null);
            
        } catch (IOException | MongoDBException ex) {
            Logger.getLogger(MigrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testIngst() {
        //Retrieve migrated collections from Ingestion
        List<Count> collections = getMigratedCollections(targetSolrIngst);
        
        if (collections != null) {
            Logger.getLogger(MigrationTest.class.getName()).log(Level.INFO, "Found " + collections.size() + " collections");
            for (Count collection : collections) {
                checkSampleBeansInCollection(collection, targetSolrIngst, targetMongoIngst);
            }
        }
    }
 
    //FIXME: commented for now since the both targets are identical in a properties file.
//    @Test
//    public void testProd() {
//        //Retrieve migrated collections from Production
//        List<Count> collections = getMigratedCollections(targetSolrProd);
//        
//        if (collections != null) {
//            Logger.getLogger(MigrationTest.class.getName()).log(Level.INFO, "Found " + collections.size() + " collections");
//            for (Count collection : collections) {
//                checkSampleBeansInCollection(collection, targetSolrProd, targetMongoProd);
//            }
//        }
//    }

    private List<Count> getMigratedCollections(SolrServer targetSolr) {
        //Get all the migrated collections (faceting is faster for this one of query
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(0);
        query.setFacetLimit(10000);
        query.setFacet(true);
        query.addFacetField("europeana_collectionName");
        try {
            return targetSolr.query(query).getFacetField("europeana_collectionName").getValues();
        } catch (SolrServerException ex) {
            Logger.getLogger(MigrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void checkSampleBeansInCollection(Count collection, SolrServer targetSolr, EdmMongoServer targetMongo) {
        //Sample up to 1000 records - some have less - records from a specific Europeana Collection
        SolrQuery query = new SolrQuery("europeana_collectionName:" + collection.getName());
        query.setFields("europeana_id");
        query.setRows(1000);
        SolrDocumentList list;
        try {
            list = targetSolr.query(query).getResults();
            for (SolrDocument doc : list) {
                //Compare the original and the migrated bean
                checkBean(doc.getFieldValue("europeana_id").toString(), targetMongo);
                i++;
                Logger.getLogger(MigrationTest.class.getName()).log(Level.INFO, "Checked " + i + " documents");
            }
        } catch (SolrServerException ex) {
            Logger.getLogger(MigrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkBean(String id, EdmMongoServer targetMongo) {
        try {
            FullBean sourceBean = sourceMongo.getFullBean(id);
            FullBean targetBean = targetMongo.getFullBean(id);
            //assert the non contextual classes first
            Assert.assertTrue(StringUtils.equals(sourceBean.getAbout(), targetBean.getAbout()));
            Assert.assertTrue("Optout for " + sourceBean.getAbout(), Objects.equals(sourceBean.isOptedOut(), targetBean.isOptedOut()));
            Assert.assertArrayEquals("Country for " + sourceBean.getAbout(), sourceBean.getCountry(), targetBean.getCountry());
            Assert.assertArrayEquals("CollectionName for " + sourceBean.getAbout(), sourceBean.getEuropeanaCollectionName(), targetBean.getEuropeanaCollectionName());
            Assert.assertTrue("completeness for " + sourceBean.getAbout(), sourceBean.getEuropeanaCompleteness() == targetBean.getEuropeanaCompleteness());
            Assert.assertArrayEquals("Language for " + sourceBean.getAbout(), sourceBean.getLanguage(), targetBean.getLanguage());
            Assert.assertArrayEquals("Provider for " + sourceBean.getAbout(), sourceBean.getProvider(), targetBean.getProvider());
            Assert.assertArrayEquals("Year for " + sourceBean.getAbout(), sourceBean.getYear(), targetBean.getYear());
            Assert.assertArrayEquals("Title for " + sourceBean.getAbout(), sourceBean.getTitle(), targetBean.getTitle());
            Assert.assertEquals("Timestamp for " + sourceBean.getAbout(), sourceBean.getTimestamp(), targetBean.getTimestamp());
            Assert.assertEquals("TimestampCreated for " + sourceBean.getAbout(), sourceBean.getTimestampCreated(), targetBean.getTimestampCreated());
            Assert.assertEquals("TimestampUpdated for " + sourceBean.getAbout(), sourceBean.getTimestampUpdated(), targetBean.getTimestampUpdated());
            //now assert the references
            compareProvideCHO(sourceBean.getProvidedCHOs().get(0), targetBean.getProvidedCHOs().get(0));
            compareEuropeanaAggregation(sourceBean.getEuropeanaAggregation(), targetBean.getEuropeanaAggregation());
            compareAggregation(sourceBean.getAggregations().get(0), targetBean.getAggregations().get(0));
            compareProxies(sourceBean.getProxies(), targetBean.getProxies());
            compareAgents(sourceBean.getAgents(), targetBean.getAgents());
            compareTimespans(sourceBean.getTimespans(), targetBean.getTimespans());
            comparePlaces(sourceBean.getPlaces(), targetBean.getPlaces());
            compareConcepts(sourceBean.getConcepts(), targetBean.getConcepts());
            compareLicenses(sourceBean.getLicenses(),targetBean.getLicenses());
        } catch (MongoDBException ex) {
            Logger.getLogger(MigrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void compareAggregation(Aggregation sourceAggregation, Aggregation targetAggregation) {
        Assert.assertNotNull("Aggregation cant be null", sourceAggregation);
        Assert.assertNotNull("Aggregation cant be null", targetAggregation);
        Assert.assertEquals("aggregation rdf:about for" + sourceAggregation.getAbout(), sourceAggregation.getAbout(), targetAggregation.getAbout());
        Assert.assertEquals("aggregation aggregatedcho rdf:about for" + sourceAggregation.getAbout(), sourceAggregation.getAggregatedCHO(), targetAggregation.getAggregatedCHO());
        Assert.assertEquals("aggregation isShownBy for" + sourceAggregation.getAbout(), sourceAggregation.getEdmIsShownBy(), targetAggregation.getEdmIsShownBy());
        Assert.assertEquals("aggregation object for" + sourceAggregation.getAbout(), sourceAggregation.getEdmObject(), targetAggregation.getEdmObject());
        Assert.assertEquals("aggregation isShownAt for" + sourceAggregation.getAbout(), sourceAggregation.getEdmIsShownAt(), targetAggregation.getEdmIsShownAt());
        Assert.assertEquals("aggregation ugc for" + sourceAggregation.getAbout(), sourceAggregation.getEdmUgc(), targetAggregation.getEdmUgc());
        Assert.assertEquals("aggregation previewnodistribute for" + sourceAggregation.getAbout(), sourceAggregation.getEdmPreviewNoDistribute(), targetAggregation.getEdmPreviewNoDistribute());
        Assert.assertTrue("aggregation hasView for" + sourceAggregation.getAbout(), MongoUtils.arrayEquals(sourceAggregation.getHasView(), targetAggregation.getHasView()));
        Assert.assertTrue("aggregation aggregates for" + sourceAggregation.getAbout(), MongoUtils.arrayEquals(sourceAggregation.getAggregates(), targetAggregation.getAggregates()));
        Assert.assertTrue("aggregation dcrights for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getDcRights(), targetAggregation.getDcRights()));
        Assert.assertTrue("aggregation edmprovider for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getEdmProvider(), targetAggregation.getEdmProvider()));
        Assert.assertTrue("aggregation edmdataprovider for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getEdmDataProvider(), targetAggregation.getEdmDataProvider()));
        Assert.assertTrue("aggregation edmrights for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getEdmRights(), targetAggregation.getEdmRights()));
        compareWebResources(sourceAggregation, targetAggregation);
    }

    private void compareProxies(List<? extends Proxy> sourceProxies, List<? extends Proxy> targetProxies) {
        Assert.assertTrue("Expected 2 proxies", sourceProxies.size() == 2);
        Assert.assertTrue("Proxies should be equal", sourceProxies.size() == targetProxies.size());
        for (Proxy sourceProxy : sourceProxies) {
            Proxy targetProxy = null;
            for (Proxy temp : targetProxies) {
                if (StringUtils.equals(sourceProxy.getAbout(), temp.getAbout())) {
                    targetProxy = temp;
                    break;
                }
            }
            compareProxy(sourceProxy, targetProxy);
        }

    }

    private void compareProxy(Proxy sourceProxy, Proxy targetProxy) {
        Assert.assertEquals("Proxy rdf:about for" + sourceProxy.getAbout(), sourceProxy.getAbout(), targetProxy.getAbout());
        Assert.assertTrue("proxy edmrights for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getEdmRights(), targetProxy.getEdmRights()));
        Assert.assertTrue("proxy contributor for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcContributor(), targetProxy.getDcContributor()));
        Assert.assertTrue("proxy coverage for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcCoverage(), targetProxy.getDcCoverage()));
        Assert.assertTrue("proxy creator for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcCreator(), targetProxy.getDcCreator()));
        Assert.assertTrue("proxy dcdate for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcDate(), targetProxy.getDcDate()));
        Assert.assertTrue("proxy dcdescription for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcDescription(), targetProxy.getDcDescription()));
        Assert.assertTrue("proxy dcformat for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcFormat(), targetProxy.getDcFormat()));
        Assert.assertTrue("proxy identifier for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcIdentifier(), targetProxy.getDcIdentifier()));
        Assert.assertTrue("proxy dclanguage for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcLanguage(), targetProxy.getDcLanguage()));
        Assert.assertTrue("proxy publisher for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcPublisher(), targetProxy.getDcPublisher()));
        Assert.assertTrue("proxy relation for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcRelation(), targetProxy.getDcRelation()));
        Assert.assertTrue("proxy dcrights for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcRights(), targetProxy.getDcRights()));
        Assert.assertTrue("proxy dcsource for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcSource(), targetProxy.getDcSource()));
        Assert.assertTrue("proxy dcsubject for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcSubject(), targetProxy.getDcSubject()));
        Assert.assertTrue("proxy dctitle for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcTitle(), targetProxy.getDcTitle()));
        Assert.assertTrue("proxy dctype for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDcType(), targetProxy.getDcType()));
        Assert.assertTrue("proxy dctermsAlternative for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsAlternative(), targetProxy.getDctermsAlternative()));
        Assert.assertTrue("proxy dctermsconformsto for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsConformsTo(), targetProxy.getDctermsConformsTo()));
        Assert.assertTrue("proxy dctermsCreated for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsCreated(), targetProxy.getDctermsCreated()));
        Assert.assertTrue("proxy dctermsextent for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsExtent(), targetProxy.getDctermsExtent()));
        Assert.assertTrue("proxy dctermshasformat for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsHasFormat(), targetProxy.getDctermsHasFormat()));
        Assert.assertTrue("proxy hspart for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsHasPart(), targetProxy.getDctermsHasPart()));
        Assert.assertTrue("proxy hasVersion for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsHasVersion(), targetProxy.getDctermsHasVersion()));
        Assert.assertTrue("proxy isformatof for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsIsFormatOf(), targetProxy.getDctermsIsFormatOf()));
        Assert.assertTrue("proxy ispartof for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsIsPartOf(), targetProxy.getDctermsIsPartOf()));
        Assert.assertTrue("proxy isreferencedby for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsIsReferencedBy(), targetProxy.getDctermsIsReferencedBy()));
        Assert.assertTrue("proxy isreplacedby for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsIsReplacedBy(), targetProxy.getDctermsIsReplacedBy()));
        Assert.assertTrue("proxy isrequiredby for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsIsRequiredBy(), targetProxy.getDctermsIsRequiredBy()));
        Assert.assertTrue("proxy isversionof for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsIsVersionOf(), targetProxy.getDctermsIsVersionOf()));
        Assert.assertTrue("proxy issued for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsIssued(), targetProxy.getDctermsIssued()));
        Assert.assertTrue("proxy medium for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsMedium(), targetProxy.getDctermsMedium()));
        Assert.assertTrue("proxy provenance for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsProvenance(), targetProxy.getDctermsProvenance()));
        Assert.assertTrue("proxy references for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsReferences(), targetProxy.getDctermsReferences()));
        Assert.assertTrue("proxy replaces for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsReplaces(), targetProxy.getDctermsReplaces()));
        Assert.assertTrue("proxy requires for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsRequires(), targetProxy.getDctermsRequires()));
        Assert.assertTrue("proxy spatial for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsSpatial(), targetProxy.getDctermsSpatial()));
        Assert.assertTrue("proxy TOC for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsTOC(), targetProxy.getDctermsTOC()));
        Assert.assertTrue("proxy temporal for" + sourceProxy.getAbout(), MongoUtils.mapEquals(sourceProxy.getDctermsTemporal(), targetProxy.getDctermsTemporal()));
        Assert.assertTrue("proxy hasMet for"+sourceProxy.getAbout(),MongoUtils.mapEquals(sourceProxy.getEdmHasMet(),targetProxy.getEdmHasMet()));
        Assert.assertTrue("proxy hasType for"+sourceProxy.getAbout(),MongoUtils.mapEquals(sourceProxy.getEdmHasType(),targetProxy.getEdmHasType()));
        Assert.assertTrue("proxy isRelatedTo for"+sourceProxy.getAbout(),MongoUtils.mapEquals(sourceProxy.getEdmIsRelatedTo(),targetProxy.getEdmIsRelatedTo()));
        Assert.assertTrue("proxy usertags for"+sourceProxy.getAbout(),MongoUtils.mapEquals(sourceProxy.getUserTags(),targetProxy.getUserTags()));
        Assert.assertTrue("proxy year for"+sourceProxy.getAbout(),MongoUtils.mapEquals(sourceProxy.getYear(),targetProxy.getYear()));
        Assert.assertTrue("proxy incorporates for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getEdmIncorporates(),targetProxy.getEdmIncorporates()));
        Assert.assertTrue("proxy isderivativeof for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getEdmIsDerivativeOf(),targetProxy.getEdmIsDerivativeOf()));
        Assert.assertTrue("proxy isNextInSequence for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getEdmIsNextInSequence(),targetProxy.getEdmIsNextInSequence()));
        Assert.assertTrue("proxy issimilarto for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getEdmIsSimilarTo(),targetProxy.getEdmIsSimilarTo()));
        Assert.assertTrue("proxy issuccessorof for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getEdmIsSuccessorOf(),targetProxy.getEdmIsSuccessorOf()));
        Assert.assertTrue("proxy realizes for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getEdmRealizes(),targetProxy.getEdmRealizes()));
        Assert.assertTrue("proxy waspresentat for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getEdmWasPresentAt(),targetProxy.getEdmWasPresentAt()));
        Assert.assertTrue("proxy proxyin for"+sourceProxy.getAbout(),MongoUtils.arrayEquals(sourceProxy.getProxyIn(),targetProxy.getProxyIn()));
        Assert.assertTrue("proxy currentlocation for"+sourceProxy.getAbout(),StringUtils.equals(sourceProxy.getEdmCurrentLocation(),targetProxy.getEdmCurrentLocation()));
        Assert.assertTrue("proxy isRepresentationof for"+sourceProxy.getAbout(),StringUtils.equals(sourceProxy.getEdmIsRepresentationOf(),targetProxy.getEdmIsRepresentationOf()));
        Assert.assertTrue("proxy proxyFor for"+sourceProxy.getAbout(),StringUtils.equals(sourceProxy.getProxyFor(),targetProxy.getProxyFor()));
        
    }

    private void compareEuropeanaAggregation(EuropeanaAggregation sourceAggregation, EuropeanaAggregation targetAggregation) {
        Assert.assertNotNull("EuropeanaAggregation cant be null", sourceAggregation);
        Assert.assertNotNull("EuropeanaAggregation cant be null", targetAggregation);
        Assert.assertEquals("Europeana aggregation rdf:about for" + sourceAggregation.getAbout(), sourceAggregation.getAbout(), targetAggregation.getAbout());
        Assert.assertEquals("Europeana aggregation aggregatedcho rdf:about for" + sourceAggregation.getAbout(), sourceAggregation.getAggregatedCHO(), targetAggregation.getAggregatedCHO());
        Assert.assertEquals("Europeana aggregation isShownBy for" + sourceAggregation.getAbout(), sourceAggregation.getEdmIsShownBy(), targetAggregation.getEdmIsShownBy());
        Assert.assertEquals("Europeana aggregation landingpage for" + sourceAggregation.getAbout(), sourceAggregation.getEdmLandingPage(), targetAggregation.getEdmLandingPage());
        Assert.assertEquals("Europeana aggregation preview for" + sourceAggregation.getAbout(), sourceAggregation.getEdmPreview(), targetAggregation.getEdmPreview());
        Assert.assertTrue("Europeana aggregation hasView for" + sourceAggregation.getAbout(), MongoUtils.arrayEquals(sourceAggregation.getEdmHasView(), targetAggregation.getEdmHasView()));
        Assert.assertTrue("Europeana aggregation aggregates for" + sourceAggregation.getAbout(), MongoUtils.arrayEquals(sourceAggregation.getAggregates(), targetAggregation.getAggregates()));
        Assert.assertTrue("Europeana aggregation dccreator for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getDcCreator(), targetAggregation.getDcCreator()));
        Assert.assertTrue("Europeana aggregation edmcountry for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getEdmCountry(), targetAggregation.getEdmCountry()));
        Assert.assertTrue("Europeana aggregation edmlanguage for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getEdmLanguage(), targetAggregation.getEdmLanguage()));
        Assert.assertTrue("Europeana aggregation edmrights for" + sourceAggregation.getAbout(), MongoUtils.mapEquals(sourceAggregation.getEdmRights(), targetAggregation.getEdmRights()));
    }

    private void compareTimespan(Timespan sourceTimespan, Timespan targetTimespan) {
        if (targetTimespan == null) {
            Assert.assertTrue("Timespan should exist", targetTimespan == null);
        } else {
            Assert.assertTrue("Altlabel for Timespan " + sourceTimespan.getAbout(), MongoUtils.mapEquals(sourceTimespan.getAltLabel(), targetTimespan.getAltLabel()));
            Assert.assertTrue("Begin for Timespan " + sourceTimespan.getAbout(), MongoUtils.mapEquals(sourceTimespan.getBegin(), targetTimespan.getBegin()));
            Assert.assertTrue("HiddenLabel for Timespan " + sourceTimespan.getAbout(), MongoUtils.mapEquals(sourceTimespan.getHiddenLabel(), targetTimespan.getHiddenLabel()));
            Assert.assertTrue("Preflabel for Timespan " + sourceTimespan.getAbout(), MongoUtils.mapEquals(sourceTimespan.getPrefLabel(), targetTimespan.getPrefLabel()));
            Assert.assertTrue("IsPartOf for Timespan " + sourceTimespan.getAbout(), MongoUtils.mapEquals(sourceTimespan.getIsPartOf(), targetTimespan.getIsPartOf()));
            Assert.assertTrue("Note for Timespan " + sourceTimespan.getAbout(), MongoUtils.mapEquals(sourceTimespan.getNote(), targetTimespan.getNote()));
            Assert.assertTrue("owlsameas for Timespan " + sourceTimespan.getAbout(), MongoUtils.arrayEquals(sourceTimespan.getOwlSameAs(), targetTimespan.getOwlSameAs()));
            Assert.assertTrue("End for Timespan " + sourceTimespan.getAbout(), MongoUtils.mapEquals(sourceTimespan.getEnd(), targetTimespan.getEnd()));
        }
    }

    private void compareAgent(Agent sourceAgent, Agent targetAgent) {
        if (targetAgent == null) {
            Assert.assertTrue("Agent should exist", targetAgent == null);
        } else {
            Assert.assertTrue("Altlabel for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getAltLabel(), targetAgent.getAltLabel()));
            Assert.assertTrue("HasPart for place " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getBegin(), targetAgent.getBegin()));
            Assert.assertTrue("HiddenLabel for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getHiddenLabel(), targetAgent.getHiddenLabel()));
            Assert.assertTrue("Preflabel for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getPrefLabel(), targetAgent.getPrefLabel()));
            Assert.assertTrue("DcDate for place " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getDcDate(), targetAgent.getDcDate()));
            Assert.assertTrue("Note for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getNote(), targetAgent.getNote()));
            Assert.assertTrue("owlsameas for agent " + sourceAgent.getAbout(), MongoUtils.arrayEquals(sourceAgent.getOwlSameAs(), targetAgent.getOwlSameAs()));
            Assert.assertTrue("EdmWaspresentAt for agent " + sourceAgent.getAbout(), MongoUtils.arrayEquals(sourceAgent.getEdmWasPresentAt(), targetAgent.getEdmWasPresentAt()));
            Assert.assertTrue("DcIdentifier for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getDcIdentifier(), targetAgent.getDcIdentifier()));
            Assert.assertTrue("EdmHasMet for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getEdmHasMet(), targetAgent.getEdmHasMet()));
            Assert.assertTrue("EdmIsRelatedTo for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getEdmIsRelatedTo(), targetAgent.getEdmIsRelatedTo()));
            Assert.assertTrue("End for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getEnd(), targetAgent.getEnd()));
            Assert.assertTrue("Name for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getFoafName(), targetAgent.getFoafName()));
            Assert.assertTrue("Biographical Information for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2BiographicalInformation(), targetAgent.getRdaGr2BiographicalInformation()));
            Assert.assertTrue("Date of Birth for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2DateOfBirth(), targetAgent.getRdaGr2DateOfBirth()));
            Assert.assertTrue("Date of Death for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2DateOfDeath(), targetAgent.getRdaGr2DateOfDeath()));
            Assert.assertTrue("Date of Establishment for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2DateOfEstablishment(), targetAgent.getRdaGr2DateOfEstablishment()));
            Assert.assertTrue("Date of Termination for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2DateOfTermination(), targetAgent.getRdaGr2DateOfTermination()));
            Assert.assertTrue("Gender for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2Gender(), targetAgent.getRdaGr2Gender()));
            Assert.assertTrue("Place of Birth for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2PlaceOfBirth(), targetAgent.getRdaGr2PlaceOfBirth()));
            Assert.assertTrue("Place of Death for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2PlaceOfDeath(), targetAgent.getRdaGr2PlaceOfDeath()));
            Assert.assertTrue("Profession or occupation for agent " + sourceAgent.getAbout(), MongoUtils.mapEquals(sourceAgent.getRdaGr2ProfessionOrOccupation(), targetAgent.getRdaGr2ProfessionOrOccupation()));
        }
    }

    private void compareConcept(Concept sourceConcept, Concept targetConcept) {
        if (targetConcept == null) {
            Assert.assertTrue("Concept should exist", targetConcept == null);
        } else {
            Assert.assertTrue("Altlabel for place " + sourceConcept.getAbout(), MongoUtils.mapEquals(sourceConcept.getAltLabel(), targetConcept.getAltLabel()));
            Assert.assertTrue("HiddenLabel for place " + sourceConcept.getAbout(), MongoUtils.mapEquals(sourceConcept.getHiddenLabel(), targetConcept.getHiddenLabel()));
            Assert.assertTrue("Preflabel for place " + sourceConcept.getAbout(), MongoUtils.mapEquals(sourceConcept.getPrefLabel(), targetConcept.getPrefLabel()));
            Assert.assertTrue("Note for place " + sourceConcept.getAbout(), MongoUtils.mapEquals(sourceConcept.getNote(), targetConcept.getNote()));
            Assert.assertTrue("Notation for place " + sourceConcept.getAbout(), MongoUtils.mapEquals(sourceConcept.getNotation(), targetConcept.getNotation()));
            Assert.assertTrue("narrowmatch for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getNarrowMatch(), targetConcept.getNarrowMatch()));
            Assert.assertTrue("narrower for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getNarrower(), targetConcept.getNarrower()));
            Assert.assertTrue("broadmatch for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getBroadMatch(), targetConcept.getBroadMatch()));
            Assert.assertTrue("broader for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getBroader(), targetConcept.getBroader()));
            Assert.assertTrue("closematch for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getCloseMatch(), targetConcept.getCloseMatch()));
            Assert.assertTrue("exactmatch for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getExactMatch(), targetConcept.getExactMatch()));
            Assert.assertTrue("inscheme for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getInScheme(), targetConcept.getInScheme()));
            Assert.assertTrue("related for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getRelated(), targetConcept.getRelated()));
            Assert.assertTrue("relatedmatch for place " + sourceConcept.getAbout(), MongoUtils.arrayEquals(sourceConcept.getRelatedMatch(), targetConcept.getRelatedMatch()));

        }
    }

    private void comparePlace(Place sourcePlace, Place targetPlace) {
        if (targetPlace == null) {
            Assert.assertTrue("Place should exist", targetPlace == null);
        } else {
            Assert.assertTrue("Altlabel for place " + sourcePlace.getAbout(), MongoUtils.mapEquals(sourcePlace.getAltLabel(), targetPlace.getAltLabel()));
            Assert.assertTrue("HasPart for place " + sourcePlace.getAbout(), MongoUtils.mapEquals(sourcePlace.getDcTermsHasPart(), targetPlace.getDcTermsHasPart()));
            Assert.assertTrue("HiddenLabel for place " + sourcePlace.getAbout(), MongoUtils.mapEquals(sourcePlace.getHiddenLabel(), targetPlace.getHiddenLabel()));
            Assert.assertTrue("Preflabel for place " + sourcePlace.getAbout(), MongoUtils.mapEquals(sourcePlace.getPrefLabel(), targetPlace.getPrefLabel()));
            Assert.assertTrue("IsPartOf for place " + sourcePlace.getAbout(), MongoUtils.mapEquals(sourcePlace.getIsPartOf(), targetPlace.getIsPartOf()));
            Assert.assertTrue("Note for place " + sourcePlace.getAbout(), MongoUtils.mapEquals(sourcePlace.getNote(), targetPlace.getNote()));
            Assert.assertTrue("owlsameas for place " + sourcePlace.getAbout(), MongoUtils.arrayEquals(sourcePlace.getOwlSameAs(), targetPlace.getOwlSameAs()));
            Assert.assertTrue("Altitude for place " + sourcePlace.getAbout(), Objects.equals(sourcePlace.getAltitude(), targetPlace.getAltitude()));
            Assert.assertTrue("Latitude for place " + sourcePlace.getAbout(), Objects.equals(sourcePlace.getLatitude(), targetPlace.getLatitude()));
            Assert.assertTrue("Longitude for place " + sourcePlace.getAbout(), Objects.equals(sourcePlace.getLongitude(), targetPlace.getLongitude()));
        }
    }

    private void compareTimespans(List<? extends Timespan> sourceTimespans, List<? extends Timespan> targetTimespans) {
        if (sourceTimespans == null) {
            Assert.assertTrue("Timespans should be null", targetTimespans == null);
        } else {
            Assert.assertTrue("Timespans should be equal", sourceTimespans.size() == targetTimespans.size());

            for (Timespan sourceTimespan : sourceTimespans) {
                Timespan targetTimespan = null;
                for (Timespan temp : targetTimespans) {
                    if (StringUtils.equals(sourceTimespan.getAbout(), temp.getAbout())) {
                        targetTimespan = temp;
                        break;
                    }
                }
                compareTimespan(sourceTimespan, targetTimespan);
            }
        }
    }

    private void compareAgents(List<? extends Agent> sourceAgents, List<? extends Agent> targetAgents) {
        if (sourceAgents == null) {
            Assert.assertTrue("Agents should be null", targetAgents == null);
        } else {
            Assert.assertTrue("Agents should be equal", sourceAgents.size() == targetAgents.size());

            for (Agent sourceAgent : sourceAgents) {
                Agent targetAgent = null;
                for (Agent temp : targetAgents) {
                    if (StringUtils.equals(sourceAgent.getAbout(), temp.getAbout())) {
                        targetAgent = temp;
                        break;
                    }
                }
                compareAgent(sourceAgent, targetAgent);
            }
        }
    }

    private void compareConcepts(List<? extends Concept> sourceConcepts, List<? extends Concept> targetConcepts) {
        if (sourceConcepts == null) {
            Assert.assertTrue("Concepts should be null", targetConcepts == null);
        } else {
            Assert.assertTrue("Concepts should be equal", sourceConcepts.size() == targetConcepts.size());

            for (Concept sourceConcept : sourceConcepts) {
                Concept targetConcept = null;
                for (Concept temp : targetConcepts) {
                    if (StringUtils.equals(sourceConcept.getAbout(), temp.getAbout())) {
                        targetConcept = temp;
                        break;
                    }
                }
                compareConcept(sourceConcept, targetConcept);
            }
        }
    }

    private void comparePlaces(List<? extends Place> sourcePlaces, List<? extends Place> targetPlaces) {
        if (sourcePlaces == null) {
            Assert.assertTrue("Places should be null", targetPlaces == null);
        } else {
            Assert.assertTrue("Places should be equal", sourcePlaces.size() == targetPlaces.size());

            for (Place sourcePlace : sourcePlaces) {
                Place targetPlace = null;
                for (Place temp : targetPlaces) {
                    if (StringUtils.equals(sourcePlace.getAbout(), temp.getAbout())) {
                        targetPlace = temp;
                        break;
                    }
                }
                comparePlace(sourcePlace, targetPlace);
            }
        }

    }

    private void compareWebResource(WebResource sourceWebResource, WebResource targetWebResource) {
        if (targetWebResource == null) {
            Assert.assertTrue("WebResource should exist", targetWebResource == null);
        } else {
            Assert.assertTrue("DcCreator for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDcCreator(), targetWebResource.getDcCreator()));
            Assert.assertTrue("HasPart for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDctermsConformsTo(), targetWebResource.getDctermsConformsTo()));
            Assert.assertTrue("HiddenLabel for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDctermsCreated(), targetWebResource.getDctermsCreated()));
            Assert.assertTrue("Preflabel for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDctermsExtent(), targetWebResource.getDctermsExtent()));
            Assert.assertTrue("IsPartOf for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDcDescription(), targetWebResource.getDcDescription()));
            Assert.assertTrue("DcFormat for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDcFormat(), targetWebResource.getDcFormat()));
            Assert.assertTrue("DcSource for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDcSource(), targetWebResource.getDcSource()));
            Assert.assertTrue("HasPart for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDctermsHasPart(), targetWebResource.getDctermsHasPart()));
            Assert.assertTrue("IsFormatOf for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDctermsIsFormatOf(), targetWebResource.getDctermsIsFormatOf()));
            Assert.assertTrue("Issued for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getDctermsIssued(), targetWebResource.getDctermsIssued()));
            Assert.assertTrue("DcRights for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getWebResourceDcRights(), targetWebResource.getWebResourceDcRights()));
            Assert.assertTrue("EdmRights for web resource " + sourceWebResource.getAbout(), MongoUtils.mapEquals(sourceWebResource.getWebResourceEdmRights(), targetWebResource.getWebResourceEdmRights()));
            Assert.assertTrue("owlsameas for web resource " + sourceWebResource.getAbout(), MongoUtils.arrayEquals(sourceWebResource.getOwlSameAs(), targetWebResource.getOwlSameAs()));
            Assert.assertTrue("Isnextinsequence for web resource " + sourceWebResource.getAbout(), StringUtils.equals(sourceWebResource.getIsNextInSequence(), targetWebResource.getIsNextInSequence()));
        }
    }
    
    private void compareWebResources(Aggregation sourceAggregation, Aggregation targetAggregation) {
    	String edmIsShownAt = targetAggregation.getEdmIsShownAt();
    	String edmIsShownBy = targetAggregation.getEdmIsShownBy();
    	String edmObject = targetAggregation.getEdmObject();
    	String[] edmHasView = targetAggregation.getHasView();
    	
    	List<? extends WebResource> targetWebResources = targetAggregation.getWebResources();
    	WebResource isShownAtWebResource = null;
    	WebResource isShownByWebResource = null;
    	WebResource aboutWebResource = null;
    	WebResource hasViewWebResource = null;
		for (WebResource resource : targetWebResources) { 
    		String about = resource.getAbout();
			if (edmIsShownAt != null && StringUtils.equals(about, edmIsShownAt)) {
    			isShownAtWebResource = resource;
    		} else if (edmIsShownBy != null && StringUtils.equals(about, edmIsShownBy)) {
    			isShownByWebResource = resource;
    		} else if (edmObject != null && StringUtils.equals(about, edmObject)) {
    			aboutWebResource = resource;
    		} else if (edmHasView != null && edmHasView.length > 0 && StringUtils.equals(about, edmHasView[0])) {
    			hasViewWebResource = resource;
    		}		
    	}
		
		if (edmIsShownAt != null) { 
			Assert.assertNotNull(isShownAtWebResource);
		} else {
			Assert.assertNull(isShownAtWebResource);
		}
		if (edmIsShownBy != null) { 
			Assert.assertNotNull(isShownByWebResource);
		} else {
			Assert.assertNull(isShownByWebResource);
		}
		if (edmObject != null) { 
			Assert.assertNotNull(aboutWebResource);
		} else {
			Assert.assertNull(aboutWebResource);
		}
		if (edmHasView != null && edmHasView.length > 0) { 
			Assert.assertNotNull(hasViewWebResource);
		} else {
			Assert.assertNull(hasViewWebResource);
		}
    }

    private void compareProvideCHO(ProvidedCHO sourceCHO, ProvidedCHO targetCHO) {
        Assert.assertEquals("provideCHO rdf about for " + sourceCHO.getAbout(), sourceCHO.getAbout(), targetCHO.getAbout());
        Assert.assertArrayEquals("owl same as for providedCHO", sourceCHO.getOwlSameAs(), targetCHO.getOwlSameAs());
    }

    private void compareLicenses(List<? extends License> sourceLicenses, List<? extends License> targetLicenses) {
        if (sourceLicenses == null) {
            Assert.assertTrue("WebResources should be null", targetLicenses == null);
        } else {
            Assert.assertTrue("WebResources should be equal", sourceLicenses.size() == targetLicenses.size());

            for (License sourceLicense : sourceLicenses) {
                License targetLicense = null;
                for (License temp : targetLicenses) {
                    if (StringUtils.equals(sourceLicense.getAbout(), temp.getAbout())) {
                        targetLicense = temp;
                        break;
                    }
                }
                compareLicense(sourceLicense, targetLicense);
            }
        }
    }

    private void compareLicense(License sourceLicense, License targetLicense) {
       Assert.assertEquals("license rdf about for " + sourceLicense.getAbout(), sourceLicense.getAbout(), targetLicense.getAbout());
       Assert.assertEquals("license odrliheritedfrom for " + sourceLicense.getAbout(), sourceLicense.getOdrlInheritFrom(), targetLicense.getOdrlInheritFrom());
       Assert.assertEquals("license ccdeprecatedfor for " + sourceLicense.getAbout(), sourceLicense.getCcDeprecatedOn(), targetLicense.getCcDeprecatedOn());
    }
}

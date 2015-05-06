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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author gmamakis
 */
public class ReadWriter implements Runnable {

    private List<SolrDocument> segment;
    private FullBeanHandler fBeanHandler;
    private SolrDocumentHandler solrHandler;
    private CloudSolrServer cloudServer;
    private EdmMongoServer sourceMongo;
    private EdmMongoServer targetMongo;
    private CountDownLatch latch;

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
    
    public List<SolrDocument> getSegment() {
        return segment;
    }

    public void setSegment(List<SolrDocument> segment) {
        this.segment = segment;
    }

    public FullBeanHandler getfBeanHandler() {
        return fBeanHandler;
    }

    public void setfBeanHandler(FullBeanHandler fBeanHandler) {
        this.fBeanHandler = fBeanHandler;
    }

    public SolrDocumentHandler getSolrHandler() {
        return solrHandler;
    }

    public void setSolrHandler(SolrDocumentHandler solrHandler) {
        this.solrHandler = solrHandler;
    }

    public CloudSolrServer getCloudServer() {
        return cloudServer;
    }

    public void setCloudServer(CloudSolrServer cloudServer) {
        this.cloudServer = cloudServer;
    }

    public EdmMongoServer getSourceMongo() {
        return sourceMongo;
    }

    public void setSourceMongo(EdmMongoServer sourceMongo) {
        this.sourceMongo = sourceMongo;
    }

    public EdmMongoServer getTargetMongo() {
        return targetMongo;
    }

    public void setTargetMongo(EdmMongoServer targetMongo) {
        this.targetMongo = targetMongo;
    }

    @Override
    public void run() {
         List<SolrInputDocument> docList = new ArrayList<>();
        for (SolrDocument doc : segment) {
            String id = doc.getFieldValue("europeana_id").toString();
            if (id.startsWith("//")) {
                id = id.replace("//", "/");
            }
            try {

                long read = new Date().getTime();
                FullBeanImpl fBean = (FullBeanImpl) sourceMongo.getFullBean(id);

             //    Logger.getLogger(Migration.class.getName()).log(Level.INFO, "Reading took "+ (new Date().getTime()-read) + " ms");
                SolrInputDocument inputDoc = solrHandler.generate(fBean);
                docList.add(inputDoc);
                long write = new Date().getTime();
                fBeanHandler.saveEdmClasses(fBean, true);
                targetMongo.getDatastore().save(fBean);
                  //  Logger.getLogger(Migration.class.getName()).log(Level.INFO, "Writing took "+ (new Date().getTime()-write) + " ms");
            } catch (MongoDBException | SolrServerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        try {
            cloudServer.add(docList);
        } catch (SolrServerException | IOException ex) {
            Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
        }
        latch.countDown();
       Logger.getLogger(Migration.class.getName()).log(Level.INFO, "Added 200 documents");
    }

}

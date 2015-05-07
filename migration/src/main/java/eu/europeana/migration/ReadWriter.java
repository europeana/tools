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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 *  Read from a Mongo and a Solr and Write to a Mongo and Solr
 * @author Yorgos.Mamakis@ europeana.eu
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
         //For every document
         for (SolrDocument doc : segment) {
            //Fix bug with double slashes
            String id = doc.getFieldValue("europeana_id").toString();
            if (id.startsWith("//")) {
                id = id.replace("//", "/");
            }
            try {
                //Find the bean
                FullBeanImpl fBean = (FullBeanImpl) sourceMongo.getFullBean(id);
                //Generate Solr document from bean
                SolrInputDocument inputDoc = solrHandler.generate(fBean);
                //Add to list for saving later
                docList.add(inputDoc);
                //Save the individual classes in the Mongo cluster
                fBeanHandler.saveEdmClasses(fBean, true);
                //and then save the records themselves (this does not happen in one go, because of UIM)
                targetMongo.getDatastore().save(fBean);
            } catch (MongoDBException | SolrServerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        try {
            //add the documents in Solr..they will become available..no need to commit.. PATIENZA
            cloudServer.add(docList);
        } catch (SolrServerException | IOException ex) {
            Logger.getLogger(Migration.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Notify the main thread that you finished and that it does not have to wait for you now
        latch.countDown();
    }

}

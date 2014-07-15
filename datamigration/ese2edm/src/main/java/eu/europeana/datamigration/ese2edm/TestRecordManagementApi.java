/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.datamigration.ese2edm;

import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gmamakis
 */
public class TestRecordManagementApi {
    public static void main(String[] args){
        try {
            Morphia morphia = new Morphia();
            morphia.createDatastore(new Mongo("record-management.eanadev.org",33023), "apilog");
        } catch (UnknownHostException ex) {
            Logger.getLogger(TestRecordManagementApi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoException ex) {
            Logger.getLogger(TestRecordManagementApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

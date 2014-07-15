package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;

public class FullBeanSerializer {

	public static void main(String[] args){
		try {
			EdmMongoServerImpl mongo = new EdmMongoServerImpl(new Mongo("sandbox41.isti.cnr.it",27017), "europeana","", "");
			FullBean fBean = mongo.getFullBean("/2022702/3BC768153E93DE2FDE13C159A54F1CB7E06619C9");
			FileOutputStream os = new FileOutputStream(new File("temporary"));
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(fBean);
			oos.close();
			
		} catch (MongoDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

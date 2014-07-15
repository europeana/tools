package eu.europeana.test;

import java.net.UnknownHostException;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;

public class FullBeanTest {

	public static void main(String[] args) throws MongoDBException, UnknownHostException, MongoException{
		EdmMongoServerImpl mongoServer = new EdmMongoServerImpl(new Mongo("localhost",27017), "europeana", "", "");
		FullBeanImpl fBean = (FullBeanImpl) mongoServer.getFullBean("/11604/_NOTEBOOKS_UH_FINLAND_17119");
		
	}
}

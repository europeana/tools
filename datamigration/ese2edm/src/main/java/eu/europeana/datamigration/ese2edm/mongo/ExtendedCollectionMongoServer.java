package eu.europeana.datamigration.ese2edm.mongo;

import com.google.code.morphia.Datastore;
import com.mongodb.Mongo;

import eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServerImpl;

public class ExtendedCollectionMongoServer extends CollectionMongoServerImpl {

	private Datastore datastore;
	
	public ExtendedCollectionMongoServer(Mongo mongo,String database, Datastore datastore){
		super(mongo, database);
		setDatastore(datastore);
	}
	public Datastore getDatastore(){
		return this.datastore;
	}
}

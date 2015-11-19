package eu.europeana.record.management.database.dao;

import java.util.List;

import eu.europeana.record.management.database.entity.SystemObj;

public interface SystemObjectDao<T extends SystemObj> extends Dao<T>{
	
	List<T> findSystems();
	
	T findByURLs(String urls);

}

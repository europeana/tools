package eu.europeana.record.management.database.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import eu.europeana.record.management.database.dao.SystemObjectDao;
import eu.europeana.record.management.database.entity.MongoSystemObj;

public class MongoSystemDaoImpl  extends DaoImpl<MongoSystemObj> implements SystemObjectDao<MongoSystemObj>{

	public MongoSystemDaoImpl(EntityManager em) {
		super(MongoSystemObj.class, em);
	}

	@Override
	public List<MongoSystemObj> findSystems() {
		return findByQuery("findMongoSystems");
	}

	@Override
	public MongoSystemObj findByURLs(String urls) {
		List<MongoSystemObj> list = findByQuery("findOneMongoSystem", urls);
		if (list==null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

}

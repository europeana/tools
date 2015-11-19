package eu.europeana.record.management.database.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import eu.europeana.record.management.database.dao.SystemObjectDao;
import eu.europeana.record.management.database.entity.SolrSystemObj;

public class SolrSystemDaoImpl  extends DaoImpl<SolrSystemObj> implements SystemObjectDao<SolrSystemObj>{

	public SolrSystemDaoImpl(EntityManager em) {
		super(SolrSystemObj.class, em);
	}

	@Override
	public List<SolrSystemObj> findSystems() {
		return findByQuery("findSolrSystems");
	}

	@Override
	public SolrSystemObj findByURLs(String urls) {
		List<SolrSystemObj> list = findByQuery("findOneSolrSystem", urls);
		if (list==null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

}

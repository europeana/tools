/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.record.management.database.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import eu.europeana.record.management.database.dao.DBEntity;
import eu.europeana.record.management.database.dao.Dao;

/**
 * @see Dao.java
 * @author Yorgos.Mamakis@ kb.nl
 *
 * @param <E>
 */
public class DaoImpl<E extends DBEntity> implements Dao<E> {

	
	private EntityManager em;
	
	private Class<E> clazz;

	/**
	 * Constructor of Dao
	 * @param clazz The domain class
	 * @param em the Persistence Entity Manager
	 */
	public DaoImpl(Class<E> clazz, EntityManager em){
		this.clazz = clazz;
		this.em = em;
	}
	
	public void rollback(){
		em.getTransaction().rollback();
	}
	public E findByPK(long id) {
		return em.find(clazz, id);
	}

	@SuppressWarnings("unchecked")
	public List<E> findAll(Class<E> clazz) {
		String query= "SELECT e FROM "+ clazz.getSimpleName()+ " e";
		
		return em.createQuery(query).getResultList();
	}

	public void save(E obj) {
		
		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();

	}

	public void update(E obj) {
		em.getTransaction().begin();
		em.merge(obj);
		em.getTransaction().commit();
	}

	public void delete(E obj) {
		em.getTransaction().begin();
		em.remove(obj);
		em.getTransaction().commit();
	}

	public List<E> findByQuery(String q, Object... args) {
		int parnr = 1;
		TypedQuery<E> query = em.createNamedQuery(q, clazz);
		if ((args != null) && (args.length > 0)) {
			try{
			for (Object object : args) {
				query.setParameter(parnr++, object);
			}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return query.getResultList();
	}

	public void close(){
		em.close();
	}
	
	@Override
	public boolean  isOpen() {
		return em.isOpen();
		
	}
}

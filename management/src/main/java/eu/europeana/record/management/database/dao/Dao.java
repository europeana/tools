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
package eu.europeana.record.management.database.dao;

import java.util.List;

/**
 * Generic Interface for database operations
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 * @param <I>
 *            A class that extends a generic DB entity
 */
public interface Dao<I extends DBEntity> {

	/**
	 * Find by public key
	 * 
	 * @param id
	 *            The key
	 * @return The Entity identifed by this public key
	 */
	I findByPK(long id);

	/**
	 * Retrieve all entities
	 * 
	 * @param clazz
	 *            The domain class to search on
	 * @return The list of domain entities
	 */
	List<I> findAll(Class<I> clazz);

	/**
	 * Save an entity
	 * 
	 * @param obj
	 *            The entity to save
	 */
	void save(I obj);

	/**
	 * Update an entity
	 * 
	 * @param obj
	 *            The entity to update
	 */
	void update(I obj);

	/**
	 * Delete an entity
	 * 
	 * @param obj
	 *            The entity to delete
	 */
	void delete(I obj);

	/**
	 * Rollback the latest transaction in cases of errors
	 */
	void rollback();

	/**
	 * FInd a list of entities using a named query
	 * 
	 * @param query
	 *            The named query to execute
	 * @param args
	 *            The arguments of the query
	 * @return The list of domain entities that correspond to the query
	 */
	List<I> findByQuery(String query, Object... args);

	/**
	 * Close the connection to the database
	 */
	void close();
	
	/**
	 * Checks is the entity manager is open
	 */
	boolean isOpen();
}

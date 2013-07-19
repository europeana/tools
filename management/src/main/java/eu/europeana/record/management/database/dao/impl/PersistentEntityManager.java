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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Helper class to create the EntityManager
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class PersistentEntityManager {
	static EntityManagerFactory usersEmf;

	private PersistentEntityManager() {
		// Avoid initialization
	}

	static {
		usersEmf = Persistence.createEntityManagerFactory("users");
	}

	/**
	 * Create an instance of the Entity Manager
	 * @return The newly created instance of the EntityManager
	 */
	public static EntityManager getManager() {
		return usersEmf.createEntityManager();
	}
}

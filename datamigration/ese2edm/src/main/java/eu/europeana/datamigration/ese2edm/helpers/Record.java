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
package eu.europeana.datamigration.ese2edm.helpers;

/**
 * Helper class used to create a sorted SOLR index according to its hash
 * @author Yorgos.Mamakis@ kb.bl
 *
 */
@Deprecated
public class Record {

	
	private String id;
	
	private String object;
	
	private String hash;


	/**
	 * Get the id of the record
	 * @return The record id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the record id
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * The edm:object of the record
	 * @return
	 */
	public String getObject() {
		return object;
	}

	/**
	 * The edm:object of the record
	 * @param object
	 */
	public void setObject(String object) {
		this.object = object;
	}

	/**
	 * The SHA-256 of the record's edm:object
	 * @return
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * The SHA-256 of the record's edm:object
	 * @param hash
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
}

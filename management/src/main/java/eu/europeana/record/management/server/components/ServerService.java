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
package eu.europeana.record.management.server.components;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;

import eu.europeana.record.management.database.entity.SystemObj;
import eu.europeana.record.management.shared.dto.Record;

/**
 * Generic Interface for a server system representation
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public interface ServerService<T extends SystemObj> {

	/**
	 * Delete a provided record
	 * @param The record to delete
	 * @throws MalformedURLException 
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	void deleteRecord(T systemObj, Record record) throws MalformedURLException, SolrServerException, IOException;
	
	/**
	 * Delete a collection based on the dataset ID
	 * @param collectionName The dataset ID
	 * @throws MalformedURLException 
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	void deleteCollection(T systemObj, String collectionName) throws MalformedURLException, SolrServerException, IOException;
	
	
	/**
	 * Identify if a record exists
	 * @param record The record to check
	 * @return The record if it exists, null else
	 * @throws MalformedURLException 
	 * @throws SolrServerException 
	 */
	Record identifyRecord(T systemObj, Record record) throws MalformedURLException, SolrServerException;
}

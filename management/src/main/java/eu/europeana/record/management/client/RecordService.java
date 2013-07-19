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
package eu.europeana.record.management.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import eu.europeana.record.management.shared.dto.Record;
import eu.europeana.record.management.shared.dto.UserDTO;
import eu.europeana.record.management.shared.exceptions.NoRecordException;
import eu.europeana.record.management.shared.exceptions.UniqueRecordException;

/**
 * The record management service (deleting from SOLR and Mongo)
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@RemoteServiceRelativePath("record")
public interface RecordService extends RemoteService {

	/**
	 * Delete a Record from the subsystems
	 * 
	 * @param record
	 *            The record to remove
	 * @param userDTO
	 *            The user that removed the selected record
	 */
	void delete(Record record, UserDTO userDTO) throws UniqueRecordException,NoRecordException;

	/**
	 * Delete a Dataset from the subsystems
	 * 
	 * @param collectionName
	 *            The dataset identifier of the record
	 * @param userDTO
	 *            The user that removes the collection
	 */
	void deleteCollection(String collectionName, UserDTO userDTO);

	/**
	 * Batch deleting records
	 * 
	 * @param records
	 *            The records to delte
	 * @param userDTO
	 *            The user that deletes the records
	 */
	void delete(List<Record> records, UserDTO userDTO);
}

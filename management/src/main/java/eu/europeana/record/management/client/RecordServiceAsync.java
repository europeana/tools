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

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.europeana.record.management.shared.dto.Record;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * @see RecordService.java
 * 
 */
public interface RecordServiceAsync {

	void delete(Record record, UserDTO userDTO, AsyncCallback<Void> callback);

	void deleteCollection(String collectionName, UserDTO userDTO,
			AsyncCallback<Void> callback);

	void delete(List<Record> records, UserDTO userDTO,
			AsyncCallback<Void> callback);

}

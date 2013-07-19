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

import eu.europeana.record.management.shared.dto.LogEntryDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * Class retrieving the log entries of a specific or all users
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@RemoteServiceRelativePath("log")
public interface LogEntryService extends RemoteService {

	/**
	 * Find the log entries of a specific user
	 * 
	 * @param user
	 *            UserDTO representing the selected user
	 * @return A list of the selected user log entries
	 */
	public List<LogEntryDTO> findEntryByUser(UserDTO user);

	/**
	 * Find the log entries of all users
	 * 
	 * @param user
	 *            UserDTO to log the selected action to
	 * @return A list of the selected user log entries
	 */
	public List<LogEntryDTO> findAllEntries(UserDTO user);
}

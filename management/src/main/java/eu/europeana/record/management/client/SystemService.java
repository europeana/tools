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

import eu.europeana.record.management.shared.dto.SystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * System management service. Add/Update/Remove and show all the systems that
 * will communicate with the record removal service
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@RemoteServiceRelativePath("system")
public interface SystemService extends RemoteService {

	/**
	 * Create a new system
	 * 
	 * @param systemDTO
	 *            The system to create
	 * @param userDTO
	 *            The user that creates the system
	 */
	void createSystem(SystemDTO systemDTO, UserDTO userDTO);

	/**
	 * Update a selected system
	 * 
	 * @param systemDTO
	 *            The system to update
	 * @param userDTO
	 *            The user that updates the system
	 */
	void updateSystem(SystemDTO systemDTO, UserDTO userDTO);

	/**
	 * Delete a system
	 * 
	 * @param systemDTO
	 *            The system to delete
	 * @param userDTO
	 *            The user that deletes the system
	 */
	void deleteSystem(SystemDTO systemDTO, UserDTO userDTO);

	/**
	 * Show a list of all the systems the system can communicate with
	 * 
	 * @param userDTO
	 *            the user that requested the systems
	 * @return A list of all the available systems
	 */
	List<SystemDTO> showAllSystems(UserDTO userDTO);

}

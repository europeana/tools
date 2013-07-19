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

import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * User Management service to create/modify ansd show users (deleting not
 * supported currently)
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService {

	/**
	 * Update an existing user
	 * 
	 * @param user
	 *            The user to update
	 * @param who
	 *            The user who performs the update operation
	 */
	void updateUser(UserDTO user, UserDTO who);

	/**
	 * Create a new user. Note that the user is updated if the username exists
	 * 
	 * @param user
	 *            The user to create
	 * @param who
	 *            The user that performs the create operation
	 */
	void createUser(UserDTO user, UserDTO who);

	/**
	 * Delete a user. This operation is currently not exposed to the UI, as it
	 * has implications on the logging functionality.
	 * 
	 * @param user
	 *            The user to delete
	 * @param who
	 *            The user who performs the delete opertion
	 */
	void deleteUser(UserDTO user, UserDTO who);

	/**
	 * Show all available users
	 * 
	 * @param who
	 *            The user who requests to retrieve all the available users
	 * @return The list of available users
	 */
	List<UserDTO> showUsers(UserDTO who);
}

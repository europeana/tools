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
package eu.europeana.record.management.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.europeana.record.management.client.UserService;
import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.Session;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.database.enums.Role;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.server.util.PasswordUtils;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * @seee UserService.java
 * @author gmamakis
 * 
 */
public class UserServiceImpl extends RemoteServiceServlet implements
		UserService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3937919944853986562L;
	private Dao<UserObj> userDao;
	boolean enableLogging = true;

	public Dao<UserObj> getUserDao() {
		return userDao;
	}

	public void setUserDao(Dao<UserObj> userDao) {
		this.userDao = userDao;
	}

	private Dao<UserObj> createDao() {
		return new DaoImpl<UserObj>(UserObj.class,
				PersistentEntityManager.getManager());
	}

	public void enableLogging(boolean enable) {
		this.enableLogging = enable;
	}

	public void updateUser(UserDTO userDTO, UserDTO who) {
		createUser(userDTO, who);
		// try {
		// Dao<UserObj> userDao = this.userDao == null ? createDao()
		// : this.userDao;
		//
		// UserObj user = userDao.findByPK(userDTO.getId());
		// user.setActive(true);
		// userDao.update(convertUserFromDTO(user, userDTO));
		// UserObj whonew = userDao.findByPK(who.getId());
		//
		// userDao.close();
		// if (enableLogging) {
		// LogUtils.createLogEntry(LogEntryType.UPDATE, whonew,
		// "updated user " + user.getUsername(), new Date());
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	private UserObj convertUserFromDTO(UserObj user, UserDTO userDTO) {

		user.setName(userDTO.getName());
		user.setPassword(PasswordUtils.generateMD5(userDTO.getPassword()));
		user.setRole(Role.valueOf(userDTO.getRole()));
		user.setSurname(userDTO.getSurname());
		user.setUsername(userDTO.getUsername());
		return user;
	}

	public void createUser(UserDTO userDTO, UserDTO who) {
		Dao<UserObj> userDao = this.userDao == null ? createDao()
				: this.userDao;
		try {
			List<UserObj> userList= userDao.findByQuery("findInactiveUsers", userDTO.getUsername());
			UserObj user = null;
			boolean create = false;
			if (userList.size()==0) {
				user = new UserObj();
				create = true;
			} else {
				user = userList.get(0);
			}
			user.setActive(true);
			if (create) {
				userDao.save(convertUserFromDTO(user, userDTO));
			} else {
				user.setName(userDTO.getName());
				user.setPassword(PasswordUtils.generateMD5(userDTO.getPassword()));
				user.setSurname(userDTO.getSurname());
				user.setRole(Role.valueOf(userDTO.getRole()));
				userDao.update(user);
			}
			UserObj whonew = null;
			if (!userDTO.equals(who)) {
				whonew = userDao.findByPK(who.getId());
			}
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.ADD,
						whonew != null ? whonew : user,
						"created user " + user.getUsername(), new Date());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// try {
		// UserObj user = new UserObj();
		// user.setActive(true);
		// userDao.save(convertUserFromDTO(user, userDTO));
		// UserObj whonew = null;
		// if (!userDTO.equals(who)) {
		// whonew = userDao.findByPK(who.getId());
		// }
		// if (enableLogging) {
		// LogUtils.createLogEntry(LogEntryType.ADD,
		// whonew != null ? whonew : user,
		// "created user " + user.getUsername(), new Date());
		// }
		// } catch (Exception e) {
		// UserObj retUser = userDao.findByPK(userDTO.getId());
		// retUser.setActive(true);
		// UserObj whonew = userDao.findByPK(who.getId());
		// userDao.rollback();
		// userDao.update(convertUserFromDTO(retUser, userDTO));
		// if (enableLogging) {
		// LogUtils.createLogEntry(LogEntryType.UPDATE, whonew,
		// "updated user " + retUser.getUsername(), new Date());
		// }
		// } finally {
		// userDao.close();
		// }
	}

	public void deleteUser(UserDTO userDTO, UserDTO who) {
		Dao<UserObj> userDao = this.userDao == null ? createDao()
				: this.userDao;
		UserObj user = userDao.findByPK(userDTO.getId());
		user.setActive(false);
		userDao.delete(user);
		UserObj whonew = userDao.findByPK(who.getId());
		userDao.close();
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.REMOVE, whonew,
					"removed user " + user.getUsername(), new Date());
		}
	}

	public List<UserDTO> showUsers(UserDTO who) {
		try {
			Dao<UserObj> userDao = this.userDao == null ? createDao()
					: this.userDao;
			List<UserObj> users = userDao.findByQuery("findAllUsers");
			UserObj whonew = userDao.findByPK(who.getId());
			userDao.close();
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.RETRIEVE, whonew,
						"retrieved all users", new Date());
			}
			return convertUsersToDTO(users);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<UserDTO> convertUsersToDTO(List<UserObj> users) {
		List<UserDTO> userDTOs = new ArrayList<UserDTO>();
		for (UserObj user : users) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(user.getId());
			Dao<Session> sessionDao = new DaoImpl<Session>(Session.class, PersistentEntityManager.getManager());
			List<Session> sessions = sessionDao.findByQuery(
					"findLoginByUser", user);
			if(sessions.size()>0){
				userDTO.setDate(sessions.get(0).getLastLogin().toGMTString());
			}
			userDTO.setName(user.getName());
			userDTO.setPassword(user.getPassword());
			userDTO.setRole(user.getRole().toString());
			userDTO.setSurname(user.getSurname());
			userDTO.setUsername(user.getUsername());
			
			userDTOs.add(userDTO);
		}
		return userDTOs;
	}

}

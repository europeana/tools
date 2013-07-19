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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.europeana.record.management.client.LoginService;
import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.Session;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.server.util.PasswordUtils;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * @see LoginService.java
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {

	Dao<Session> sessionDao;
	Dao<UserObj> userDao;
	boolean enableLogging = true;

	public Dao<Session> getSessionDao() {
		return sessionDao;
	}

	public void setSessionDao(Dao<Session> sessionDao) {
		this.sessionDao = sessionDao;
	}

	public Dao<UserObj> getUserDao() {
		return userDao;
	}

	public void setUserDao(Dao<UserObj> userDao) {
		this.userDao = userDao;
	}

	public Dao<UserObj> createUserDao() {
		return new DaoImpl(UserObj.class,
				PersistentEntityManager.getManager());
	}

	public Dao<Session> createSessionDao() {
		return new DaoImpl(Session.class,
				PersistentEntityManager.getManager());
	}
	public void enableLogging(boolean enable) {
		this.enableLogging = enable;
	}

	@SuppressWarnings("deprecation")
	public UserDTO userExists(String uname, String pass) {

		try {
			Dao<Session> sessionDao = this.sessionDao == null ? (Dao<Session>) createSessionDao()
					: this.sessionDao;
			Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
					: this.userDao;
			List<UserObj> users = userDao.findByQuery("findUsers", uname);
			if (users != null) {

				if (StringUtils.equals(users.get(0).getPassword(),
						PasswordUtils.generateMD5(pass))) {

					UserObj user = users.get(0);
					List<Session> sessions = sessionDao.findByQuery(
							"findLoginByUser", user);
					Date when = new Date();
					Session session;
					if (sessions.size() == 0) {
						session = new Session();
						session.setUser(user);
						session.setLastLogin(when);
						sessionDao.save(session);
					} else {
						session = sessions.get(0);
						session.setLastLogin(when);
						sessionDao.update(session);
					}

					UserDTO userDTO = new UserDTO();
					userDTO.setName(user.getName());
					userDTO.setPassword(user.getPassword());
					userDTO.setRole(user.getRole().toString());
					userDTO.setSurname(user.getSurname());
					userDTO.setDate(session.getLastLogin().toGMTString());
					userDTO.setUsername(user.getUsername());
					userDTO.setId(user.getId());
					if (enableLogging) {
						LogUtils.createLogEntry(LogEntryType.LOGIN, user, "",
								when);
					}
					return userDTO;
				}
			}
			userDao.close();
			sessionDao.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

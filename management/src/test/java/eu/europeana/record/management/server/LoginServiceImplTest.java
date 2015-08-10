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

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.entity.LogEntry;
import eu.europeana.record.management.database.entity.Session;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.Role;
import eu.europeana.record.management.shared.dto.UserDTO;
import org.junit.Ignore;

/**
 * Unit tests for the login service
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@Ignore
public class LoginServiceImplTest {

	LoginServiceImpl loginService;
	Dao<Session> sessionDao;
	Dao<UserObj> userDao;
	Dao<LogEntry> logEntryDao;
	UserServiceImpl userService;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		userDao = EasyMock.createMock(Dao.class);
		logEntryDao = EasyMock.createMock(Dao.class);
		sessionDao = EasyMock.createMock(Dao.class);
		loginService = new LoginServiceImpl();
		loginService.setUserDao(userDao);
		loginService.setSessionDao(sessionDao);
		userService = new UserServiceImpl();

	}

	@Test
	public void testLogin() {

		List<UserObj> users = new ArrayList<UserObj>();
		UserObj user = new UserObj();
		user.setUsername("username");
		user.setPassword("password");
		user.setName("name");
		user.setRole(Role.GOD);
		user.setSurname("surname");
		users.add(user);

		List<Session> sessions = new ArrayList<Session>();
		EasyMock.expect(userDao.findByQuery("findUsers", "name")).andReturn(
				users);
		EasyMock.expect(sessionDao.findByQuery("findLoginByUser", user))
				.andReturn(sessions);

		sessionDao.save(EasyMock.anyObject(Session.class));
		sessionDao.close();
		userDao.close();
		Session session = new Session();
		session.setId(1l);
		session.setLastLogin(new Date());
		session.setUser(user);
		sessions.add(session);
		EasyMock.expect(userDao.findByQuery("findUsers", "name")).andReturn(
				users);
		EasyMock.expect(sessionDao.findByQuery("findLoginByUser", user))
				.andReturn(sessions);
		sessionDao.update(EasyMock.anyObject(Session.class));
		sessionDao.close();
		userDao.close();
		EasyMock.replay(userDao);
		EasyMock.replay(sessionDao);
		UserDTO userDTO = loginService.userExists(user.getName(),
				user.getPassword());
		Assert.assertNull(userDTO);
	}
}

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
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.Role;
import eu.europeana.record.management.shared.dto.UserDTO;
import org.junit.Ignore;

/**
 * Unit tests for the UserService
 * 
 * @author Yorgos.Mamakis@ kb.nl
 */
@Ignore
public class UserServiceImplTest {
	Dao<UserObj> userDao;
	UserServiceImpl userService;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		userDao = EasyMock.createMock(Dao.class);
		userService = new UserServiceImpl();
	}

	@Test
	public void createUser() {
		UserObj user = new UserObj();
		user.setUsername("username");
		user.setPassword("password");
		user.setName("name");
		user.setRole(Role.GOD);
		user.setSurname("surname");
		UserDTO userDTO = new UserDTO();
		userDTO.setUsername(user.getUsername());
		userDTO.setPassword(user.getPassword());
		userDTO.setRole(user.getRole().toString());
		userDTO.setName(user.getName());
		userDTO.setSurname(user.getSurname());
		userDTO.setId(1l);
		userDao.save(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.rollback();
		userDao.update(EasyMock.anyObject(UserObj.class));

		userService.setUserDao(userDao);
		userService.enableLogging(false);
		userService.createUser(userDTO, userDTO);
	}

	@Test
	public void updateUser() {
		UserObj user = new UserObj();
		user.setUsername("username");
		user.setPassword("password");
		user.setName("name");
		user.setRole(Role.GOD);
		user.setSurname("surname");
		UserDTO userDTO = new UserDTO();
		userDTO.setUsername(user.getUsername());
		userDTO.setPassword(user.getPassword());
		userDTO.setRole(user.getRole().toString());
		userDTO.setName(user.getName());
		userDTO.setSurname(user.getSurname());
		userDTO.setId(1l);
		userDao.save(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.rollback();
		userDao.update(EasyMock.anyObject(UserObj.class));

		userService.setUserDao(userDao);
		userService.enableLogging(false);
		userService.createUser(userDTO, userDTO);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		EasyMock.replay(userDao);
		userService.updateUser(userDTO, userDTO);
	}

	@Test
	public void deleteUser() {
		UserObj user = new UserObj();
		user.setUsername("username");
		user.setPassword("password");
		user.setName("name");
		user.setRole(Role.GOD);
		user.setSurname("surname");
		UserDTO userDTO = new UserDTO();
		userDTO.setUsername(user.getUsername());
		userDTO.setPassword(user.getPassword());
		userDTO.setRole(user.getRole().toString());
		userDTO.setName(user.getName());
		userDTO.setSurname(user.getSurname());
		userDTO.setId(1l);
		userDao.save(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.rollback();
		userDao.update(EasyMock.anyObject(UserObj.class));

		userService.setUserDao(userDao);
		userService.enableLogging(false);
		userService.createUser(userDTO, userDTO);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.delete(user);
		EasyMock.replay(userDao);
		userService.deleteUser(userDTO, userDTO);
	}

	@Test
	public void showAll() {
		UserObj user = new UserObj();
		user.setUsername("username");
		user.setPassword("password");
		user.setName("name");
		user.setRole(Role.GOD);
		user.setSurname("surname");
		UserDTO userDTO = new UserDTO();
		userDTO.setUsername(user.getUsername());
		userDTO.setPassword(user.getPassword());
		userDTO.setRole(user.getRole().toString());
		userDTO.setName(user.getName());
		userDTO.setSurname(user.getSurname());
		userDTO.setId(1l);
		userDao.save(user);
		List<UserObj> users = new ArrayList<UserObj>();
		users.add(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.rollback();
		userDao.update(EasyMock.anyObject(UserObj.class));

		userService.setUserDao(userDao);
		userService.enableLogging(false);
		userService.createUser(userDTO, userDTO);
		EasyMock.expect(userDao.findAll(UserObj.class)).andReturn(users);
		EasyMock.replay(userDao);
		List<UserDTO> ret = userService.showUsers(userDTO);
		Assert.assertEquals(1, ret.size());
	}
}

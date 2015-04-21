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
import eu.europeana.record.management.database.entity.SystemObj;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.Role;
import eu.europeana.record.management.database.enums.SystemType;
import eu.europeana.record.management.shared.dto.SystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;
import org.junit.Ignore;

/**
 * Unit tests for SystemService
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@Ignore
public class SystemServiceImplTest {

	Dao<SystemObj> systemDao;
	Dao<UserObj> userDao;
	SystemServiceImpl systemService;
	UserDTO userDTO = new UserDTO();
	UserObj user = new UserObj();
	SystemObj system = new SystemObj();
	SystemDTO systemDTO = new SystemDTO();

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		userDao = EasyMock.createMock(Dao.class);
		systemDao = EasyMock.createMock(Dao.class);
		systemService = new SystemServiceImpl();
		systemService.enableLogging(false);
		userDTO.setId(1l);
		userDTO.setName("name");
		userDTO.setPassword("password");
		userDTO.setRole(Role.GOD.toString());
		userDTO.setSurname("surname");
		userDTO.setUsername("username");
		user.setId(1l);
		user.setName(userDTO.getName());
		user.setPassword(userDTO.getPassword());
		user.setRole(Role.GOD);
		user.setSurname(userDTO.getSurname());
		user.setUsername(userDTO.getUsername());
		system.setType(SystemType.SOLR);
		system.setUrl("http://test");
		system.setId(1l);
		systemDTO.setId(1l);
		systemDTO.setType(system.getType().toString());
		systemDTO.setUrl(system.getUrl());
	}

	@Test
	public void createSystem() {

		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		systemDao.save(EasyMock.anyObject(SystemObj.class));
		userDao.close();
		systemDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		List<SystemObj> systems = new ArrayList<SystemObj>();
		systems.add(system);
		EasyMock.expect(systemDao.findAll(SystemObj.class)).andReturn(systems);
		userDao.close();
		systemDao.close();
		EasyMock.replay(userDao);
		EasyMock.replay(systemDao);

		systemService.setSystemDao(systemDao);
		systemService.setUserDao(userDao);
		systemService.createSystem(systemDTO, userDTO);
		Assert.assertEquals(1, systemService.showAllSystems(userDTO).size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateSystem() {
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		systemDao.save(EasyMock.anyObject(SystemObj.class));
		userDao.close();
		systemDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		List<SystemObj> systems = new ArrayList<SystemObj>();
		systems.add(system);
		EasyMock.expect(systemDao.findAll(SystemObj.class)).andReturn(systems);
		userDao.close();
		systemDao.close();
		EasyMock.replay(userDao);
		EasyMock.replay(systemDao);

		systemService.setSystemDao(systemDao);
		systemService.setUserDao(userDao);
		systemService.createSystem(systemDTO, userDTO);
		Assert.assertEquals(1, systemService.showAllSystems(userDTO).size());
		userDao = EasyMock.createMock(Dao.class);
		systemDao = EasyMock.createMock(Dao.class);
		EasyMock.expect(systemDao.findByPK(1l)).andReturn(system);
		systemDao.update(system);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		systemDao.close();
		EasyMock.expect(systemDao.findAll(SystemObj.class)).andReturn(systems);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		systemDao.close();

		EasyMock.replay(userDao);
		EasyMock.replay(systemDao);
		systemService.setSystemDao(systemDao);
		systemService.setUserDao(userDao);
		systemService.updateSystem(systemDTO, userDTO);
		Assert.assertEquals(1, systemService.showAllSystems(userDTO).size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteSystem() {
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		systemDao.save(EasyMock.anyObject(SystemObj.class));
		userDao.close();
		systemDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		List<SystemObj> systems = new ArrayList<SystemObj>();
		systems.add(system);
		EasyMock.expect(systemDao.findAll(SystemObj.class)).andReturn(systems);
		userDao.close();
		systemDao.close();
		EasyMock.replay(userDao);
		EasyMock.replay(systemDao);

		systemService.setSystemDao(systemDao);
		systemService.setUserDao(userDao);
		systemService.createSystem(systemDTO, userDTO);
		Assert.assertEquals(1, systemService.showAllSystems(userDTO).size());
		userDao = EasyMock.createMock(Dao.class);
		systemDao = EasyMock.createMock(Dao.class);
		EasyMock.expect(systemDao.findByPK(1l)).andReturn(system);
		systemDao.delete(system);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		systemDao.close();
		systems.clear();
		EasyMock.expect(systemDao.findAll(SystemObj.class)).andReturn(systems);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		systemDao.close();

		EasyMock.replay(userDao);
		EasyMock.replay(systemDao);
		systemService.setSystemDao(systemDao);
		systemService.setUserDao(userDao);
		systemService.deleteSystem(systemDTO, userDTO);
		Assert.assertEquals(0, systemService.showAllSystems(userDTO).size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void showSystems() {
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		systemDao.save(EasyMock.anyObject(SystemObj.class));
		userDao.close();
		systemDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		List<SystemObj> systems = new ArrayList<SystemObj>();
		systems.add(system);
		EasyMock.expect(systemDao.findAll(SystemObj.class)).andReturn(systems);
		userDao.close();
		systemDao.close();
		EasyMock.replay(userDao);
		EasyMock.replay(systemDao);

		systemService.setSystemDao(systemDao);
		systemService.setUserDao(userDao);
		systemService.createSystem(systemDTO, userDTO);
		Assert.assertEquals(1, systemService.showAllSystems(userDTO).size());
		userDao = EasyMock.createMock(Dao.class);
		systemDao = EasyMock.createMock(Dao.class);
		EasyMock.expect(systemDao.findByPK(1l)).andReturn(system);
		systemDao.update(system);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		systemDao.close();
		EasyMock.expect(systemDao.findAll(SystemObj.class)).andReturn(systems);
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		systemDao.close();

		EasyMock.replay(userDao);
		EasyMock.replay(systemDao);
		systemService.setSystemDao(systemDao);
		systemService.setUserDao(userDao);
		systemService.updateSystem(systemDTO, userDTO);
		Assert.assertEquals(1, systemService.showAllSystems(userDTO).size());

	}
}

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

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.entity.LogEntry;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.database.enums.Role;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.shared.dto.LogEntryDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * Unit tests for Log Entries
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class LogEntryServiceImplTest {

	private Dao<UserObj> userDao;
	private Dao<LogEntry> logEntryDao;
	private LogEntryServiceImpl logEntryService;
	UserObj user;
	UserDTO userDTO;

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Before
	public void setUp() {
		userDao = EasyMock.createMock(Dao.class);
		logEntryDao = EasyMock.createMock(Dao.class);
		logEntryService = new LogEntryServiceImpl();

		logEntryService.setLogEntryDao(logEntryDao);
		logEntryService.setUserDao(userDao);

		LogUtils.setLogEntryDao(logEntryDao);
		user = new UserObj();
		user.setId(1l);
		user.setName("test name");
		user.setPassword("test");
		user.setRole(Role.USER);
		user.setSurname("test surname");
		user.setUsername("username");
		userDTO = new UserDTO();
		userDTO.setId(1l);
		userDTO.setDate(new Date().toGMTString());
		userDTO.setName(user.getName());
		userDTO.setPassword(user.getPassword());
		userDTO.setUsername(user.getUsername());
		userDTO.setSurname(user.getSurname());
		userDTO.setRole(user.getRole().toString());

	}

	@Test
	public void testFindEntryByUser() {

		LogEntry logEntry = new LogEntry();
		logEntry.setUser(user);
		logEntry.setMessage("test");
		logEntry.setTimestamp(new Date());
		logEntry.setAction(LogEntryType.ADD);
		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		logEntries.add(logEntry);
		logEntryDao.save(EasyMock.anyObject(LogEntry.class));

		logEntryDao.close();
		logEntryDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		EasyMock.expect(logEntryDao.findByQuery("findByUser", user)).andReturn(
				logEntries);
		EasyMock.replay(logEntryDao);
		EasyMock.replay(userDao);
		List<LogEntryDTO> result = logEntryService.findEntryByUser(userDTO);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("test",result.get(0).getMessage());
	}
	
	@Test
	public void testFindAllEntries(){
		LogEntry logEntry = new LogEntry();
		logEntry.setUser(user);
		logEntry.setMessage("test");
		logEntry.setTimestamp(new Date());
		logEntry.setAction(LogEntryType.ADD);
		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		logEntries.add(logEntry);
		logEntryDao.save(EasyMock.anyObject(LogEntry.class));

		logEntryDao.close();
		logEntryDao.close();
		EasyMock.expect(userDao.findByPK(1l)).andReturn(user);
		userDao.close();
		EasyMock.expect(logEntryDao.findAll(LogEntry.class)).andReturn(logEntries);
		EasyMock.replay(logEntryDao);
		EasyMock.replay(userDao);
		
		List<LogEntryDTO> result = logEntryService.findAllEntries(userDTO);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("test",result.get(0).getMessage());
	}
}

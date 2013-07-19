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
package eu.europeana.record.management.server.utils;

import java.util.Date;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.entity.LogEntry;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.database.enums.Role;
import eu.europeana.record.management.server.util.LogUtils;

/**
 * Unit test for LogUtils
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class LogUtilsTest {

	Dao<LogEntry> logEntryDao;
	UserObj user;
	@SuppressWarnings("unchecked")
	@Before
	public void setup(){
		logEntryDao = EasyMock.createMock(Dao.class);
		user = new UserObj();
		user.setId(1l);
		user.setName("test name");
		user.setPassword("test");
		user.setRole(Role.USER);
		user.setSurname("test surname");
		user.setUsername("username");
	}
	
	@Test
	public void test(){
		LogUtils.setLogEntryDao(logEntryDao);
		logEntryDao.save(EasyMock.anyObject(LogEntry.class));
		logEntryDao.close();
		EasyMock.replay(logEntryDao);
		LogUtils.createLogEntry(LogEntryType.ADD, user, "test", new Date());
		EasyMock.verify(logEntryDao);
		
	}
}

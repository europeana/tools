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
package eu.europeana.record.management.server.util;

import java.util.Date;

import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.LogEntry;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;

/**
 * LogUtil class that generates a log entry
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class LogUtils {

	static Dao<LogEntry> logEntryDao;

	/**
	 * The Dao to perform the save operation
	 * 
	 * @return
	 */
	public static Dao<LogEntry> getLogEntryDao() {
		return logEntryDao;
	}

	public static void setLogEntryDao(Dao<LogEntry> logEntryDao) {
		LogUtils.logEntryDao = logEntryDao;
	}

	private static Dao<LogEntry> createDao() {
		return new DaoImpl<LogEntry>(LogEntry.class,
				PersistentEntityManager.getManager());
	}

	/**
	 * Method that creates a log entry
	 * 
	 * @param type
	 *            The type of log entry to create
	 * @param user
	 *            The user to log the action to
	 * @param message
	 *            The message of the action
	 * @param date
	 *            when the action was performed
	 */
	public static void createLogEntry(LogEntryType type, UserObj user,
			String message, Date date) {
		LogEntry logEntry = new LogEntry();
		logEntry.setAction(type);
		logEntry.setUser(user);
		logEntry.setMessage(message);
		logEntry.setTimestamp(date);
		Dao<LogEntry> dao = logEntryDao == null ? createDao() : logEntryDao;
		try {
			dao.save(logEntry);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dao.close();
		}

	}

}

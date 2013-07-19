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

import eu.europeana.record.management.client.LogEntryService;
import eu.europeana.record.management.database.dao.DBEntity;
import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.LogEntry;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.shared.dto.LogEntryDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * @see LogEntryService.java
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class LogEntryServiceImpl extends RemoteServiceServlet implements
		LogEntryService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6510653025367285701L;
	Dao<LogEntry> logEntryDao;
	Dao<UserObj> userDao;
	boolean enableLogging = true;

	public Dao<LogEntry> getLogEntryDao() {
		return logEntryDao;
	}

	public void setLogEntryDao(Dao<LogEntry> logEntryDao) {
		this.logEntryDao = logEntryDao;
	}

	public Dao<UserObj> getUserDao() {
		return userDao;
	}

	public void setUserDao(Dao<UserObj> userDao) {
		this.userDao = userDao;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Dao<UserObj> createUserDao(){
		return new DaoImpl(UserObj.class,
				PersistentEntityManager.getManager());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Dao<LogEntry> createLogEntryDao(){
		return new DaoImpl(LogEntry.class,
				PersistentEntityManager.getManager());
	}
	public void enableLogging(boolean enable) {
		this.enableLogging = enable;
	}

	public List<LogEntryDTO> findEntryByUser(UserDTO userDTO) {
		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		try {
			@SuppressWarnings("unchecked")
			Dao<LogEntry> logEntryDao = this.logEntryDao == null ? (Dao<LogEntry>) createLogEntryDao()
					: this.logEntryDao;

			@SuppressWarnings("unchecked")
			Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
					: this.userDao;
			UserObj user = userDao.findByPK(userDTO.getId());
			userDao.close();
			logEntries = logEntryDao.findByQuery("findByUser", user);
			logEntryDao.close();
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.RETRIEVE, user,
						"found user logs", new Date());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertToDTO(logEntries);
	}

	private List<LogEntryDTO> convertToDTO(List<LogEntry> logEntries) {
		List<LogEntryDTO> logEntryDTOs = new ArrayList<LogEntryDTO>();
		for (LogEntry logEntry : logEntries) {
			LogEntryDTO logDTO = new LogEntryDTO();
			logDTO.setId(logEntry.getId());
			UserDTO userDTO = new UserDTO();
			userDTO.setName(logEntry.getUser().getName());
			userDTO.setSurname(logEntry.getUser().getSurname());
			userDTO.setUsername(logEntry.getUser().getUsername());
			logDTO.setUser(userDTO);
			logDTO.setMessage(logEntry.getMessage());
			logDTO.setType(logEntry.getAction().toString());
			logDTO.setTimestamp(logEntry.getTimestamp().toGMTString());
			logEntryDTOs.add(logDTO);
		}
		return logEntryDTOs;
	}

	public List<LogEntryDTO> findAllEntries(UserDTO userDTO) {
		@SuppressWarnings("unchecked")
		Dao<LogEntry> logEntryDao = this.logEntryDao == null ? (Dao<LogEntry>) createLogEntryDao()
				: this.logEntryDao;

		@SuppressWarnings("unchecked")
		Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
				: this.userDao;
		List<LogEntry> logEntries = logEntryDao.findAll(LogEntry.class);
		UserObj user = userDao.findByPK(userDTO.getId());
		userDao.close();
		logEntryDao.close();
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.RETRIEVE, user,
					"found all logs", new Date());
		}
		return convertToDTO(logEntries);
	}

}

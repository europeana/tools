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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.europeana.record.management.client.RecordService;
import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.SystemObj;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.database.enums.SystemType;
import eu.europeana.record.management.server.components.MongoServer;
import eu.europeana.record.management.server.components.Server;
import eu.europeana.record.management.server.components.SolrServer;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.shared.dto.Record;
import eu.europeana.record.management.shared.dto.UserDTO;
import eu.europeana.record.management.shared.exceptions.NoRecordException;
import eu.europeana.record.management.shared.exceptions.UniqueRecordException;
/**
 * @see RecordService.java
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class RecordServiceImpl extends RemoteServiceServlet implements
		RecordService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7222025566809300662L;
	Dao<SystemObj> systemDao;
	Dao<UserObj> userDao;
	boolean enableLogging = true;

	public Dao<SystemObj> getSystemDao() {
		return systemDao;
	}

	public void setSystemDao(Dao<SystemObj> systemDao) {
		this.systemDao = systemDao;
	}

	public Dao<UserObj> getUserDao() {
		return userDao;
	}

	public void setUserDao(Dao<UserObj> userDao) {
		this.userDao = userDao;
	}

	public void enableLogging(boolean enable) {
		enableLogging = enable;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Dao<UserObj> createUserDao() {
		return new DaoImpl(UserObj.class,
				PersistentEntityManager.getManager());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Dao<SystemObj> createSystemDao() {
		return new DaoImpl(SystemObj.class,
				PersistentEntityManager.getManager());
	}
	@SuppressWarnings("unchecked")
	public void delete(Record record, UserDTO userDTO) throws UniqueRecordException,NoRecordException{
		try {
			record = find(record, userDTO);
			if (record != null) {
				Dao<SystemObj> systemDao = this.systemDao == null ? (Dao<SystemObj>) createSystemDao()
						: this.systemDao;
				Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
						: this.userDao;
				List<SystemObj> systems = systemDao.findAll(SystemObj.class);
				UserObj user = userDao.findByPK((userDTO.getId()));
				for (SystemObj system : systems) {
					Server server;
					if (system.getType().equals(SystemType.SOLR)) {
						server = new SolrServer();

					} else {
						server = new MongoServer();
					}
					server.setUrl(system.getUrl());

					server.deleteRecord(record);
					if (enableLogging) {
						LogUtils.createLogEntry(LogEntryType.REMOVE, user,
								"removed record " + record.getValue(),
								new Date());
					}
				}
				userDao.close();
				systemDao.close();
			} else {
				throw new NoRecordException("Record(s) could not be found");
			}
		} catch (Exception e) {
			if(e instanceof UniqueRecordException){
				throw (UniqueRecordException)e;
			}else if (e instanceof NoRecordException){
				throw (NoRecordException)e;
			} else {
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private Record find(Record record, UserDTO userDTO) throws UniqueRecordException{
		Dao<SystemObj> systemDao = this.systemDao == null ? (Dao<SystemObj>) createSystemDao()
				: this.systemDao;
		Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
				: this.userDao;
		List<SystemObj> systems = systemDao.findAll(SystemObj.class);
		UserObj user = userDao.findByPK(userDTO.getId());
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.RETRIEVE, user,
					"found systems", new Date());
		}
		try{
		for (SystemObj system : systems) {
			if (system.getType().equals(SystemType.SOLR)) {
				SolrServer server = new SolrServer();
				server.setUrl(system.getUrl());
				
				return server.identifyRecord(record);
			}
		}
		}catch(UniqueRecordException e){
			throw e;
		}
		userDao.close();
		systemDao.close();
		return null;
	}

	@SuppressWarnings("unchecked")
	public void deleteCollection(String collectionName, UserDTO userDTO) {
		try {
			Dao<SystemObj> systemDao = this.systemDao == null ? (Dao<SystemObj>) createSystemDao()
					: this.systemDao;
			Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
					: this.userDao;
			List<SystemObj> systems = systemDao.findAll(SystemObj.class);

			for (SystemObj system : systems) {
				System.out.println("in Systems");
				Server server;
				if (system.getType().equals(SystemType.SOLR)) {
					server = new SolrServer();
				} else {
					server = new MongoServer();
				}
				server.setUrl(system.getUrl());
				server.deleteCollection(collectionName);
			}
			UserObj user = userDao.findByPK(userDTO.getId());
			userDao.close();
			systemDao.close();
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.REMOVE, user,
						"removed collection " + collectionName, new Date());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(List<Record> records, UserDTO userDTO) {
		for (Record record : records) {
			delete(record, userDTO);
		}

	}

}

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

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.europeana.record.management.client.RecordService;
import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.MongoSystemObj;
import eu.europeana.record.management.database.entity.SolrSystemObj;
import eu.europeana.record.management.database.entity.SystemObj;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.server.components.MongoService;
import eu.europeana.record.management.server.components.SolrService;
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
public class RecordServiceImpl extends RemoteServiceServlet implements RecordService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7222025566809300662L;
	private Dao<SystemObj> systemDao;
	private Dao<UserObj> userDao;
	private SolrService solrService;
	private MongoService mongoService;

	boolean enableLogging = true;

	public Dao<SystemObj> getSystemDao() {
		if (this.systemDao == null || !this.userDao.isOpen() ) {
			this.systemDao = createSystemDao();
		}

		return this.systemDao;
	}

	public void setSystemDao(Dao<SystemObj> systemDao) {
		this.systemDao = systemDao;
	}

	public Dao<UserObj> getUserDao() {
		if (this.userDao == null || !this.userDao.isOpen() ) {
			this.userDao = (Dao<UserObj>) createUserDao();
		}

		return this.userDao;
	}

	public SolrService getSolrService() {
		if (this.solrService == null) {
			this.solrService = new SolrService();
		}

		return this.solrService;
	}

	public void setSolrService(SolrService solrService) {
		this.solrService = solrService;
	}

	public MongoService getMongoService() {
		if (this.mongoService == null) {
			this.mongoService = new MongoService();
		}

		return this.mongoService;
	}

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	public void setUserDao(Dao<UserObj> userDao) {
		this.userDao = userDao;
	}

	public void enableLogging(boolean enable) {
		enableLogging = enable;
	}

	public Dao<UserObj> createUserDao() {
		return new DaoImpl<UserObj>(UserObj.class, PersistentEntityManager.getManager());
	}


	public Dao<SystemObj> createSystemDao() {
		return new DaoImpl<SystemObj>(SystemObj.class, PersistentEntityManager.getManager());
	}


	public void delete(Record record, UserDTO userDTO) throws UniqueRecordException, NoRecordException {
		try {
			record = find(record, userDTO);
			if (record != null) {

				List<SystemObj> systems = getSystemDao().findAll(SystemObj.class);
				UserObj user = getUserDao().findByPK((userDTO.getId()));
				for (SystemObj system : systems) {

					if (system instanceof SolrSystemObj) {
						getSolrService().deleteRecord((SolrSystemObj) system, record);
					} else {
						getMongoService().deleteRecord((MongoSystemObj) system, record);
					}

					if (enableLogging) {
						LogUtils.createLogEntry(LogEntryType.REMOVE, user, "removed record " + record.getValue(),
								new Date());
					}
				}
				getUserDao().close();
				getSystemDao().close();
			} else {
				throw new NoRecordException("Record(s) could not be found");
			}
		} catch (Exception e) {
			if (e instanceof UniqueRecordException) {
				throw (UniqueRecordException) e;
			} else if (e instanceof NoRecordException) {
				throw (NoRecordException) e;
			} else {
				e.printStackTrace();
			}
		}

	}

	private Record find(Record record, UserDTO userDTO) throws UniqueRecordException {

		List<SystemObj> systems = getSystemDao().findAll(SystemObj.class);
		UserObj user = getUserDao().findByPK(userDTO.getId());
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.RETRIEVE, user, "found systems", new Date());
		}
		try {
			for (SystemObj system : systems) {
				if (system instanceof SolrSystemObj) {
					return getSolrService().identifyRecord((SolrSystemObj) system, record);
				}
			}
		} catch (UniqueRecordException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		getUserDao().close();
		getSystemDao().close();
		return null;
	}

	public void deleteCollection(String collectionName, UserDTO userDTO) {
		try {

			List<SystemObj> systems = getSystemDao().findAll(SystemObj.class);

			for (SystemObj system : systems) {
				System.out.println("in Systems");

				if (system instanceof SolrSystemObj) {					
					getSolrService().deleteCollection((SolrSystemObj) system, collectionName);
				} else {
					getMongoService().deleteCollection((MongoSystemObj) system, collectionName);
				}

			}
			UserObj user = getUserDao().findByPK(userDTO.getId());
			getUserDao().close();
			getSystemDao().close();
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.REMOVE, user, "removed collection " + collectionName, new Date());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void delete(List<Record> records, UserDTO userDTO) {
		for (Record record : records) {
			delete(record, userDTO);
		}

	}

}

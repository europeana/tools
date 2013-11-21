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

import eu.europeana.record.management.client.SystemService;
import eu.europeana.record.management.database.dao.DBEntity;
import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.SystemObj;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.database.enums.SystemType;
import eu.europeana.record.management.server.components.SolrServer;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.shared.dto.SystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * @see SystemService.java
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class SystemServiceImpl extends RemoteServiceServlet implements
		SystemService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 70354978513002771L;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Dao<UserObj> createUserDao() {
		return new DaoImpl(UserObj.class, PersistentEntityManager.getManager());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Dao<SystemObj> createSystemDao() {
		return new DaoImpl(SystemObj.class,
				PersistentEntityManager.getManager());
	}

	public void enableLogging(boolean enable) {
		this.enableLogging = enable;
	}

	@SuppressWarnings("unchecked")
	public void createSystem(SystemDTO systemDTO, UserDTO userDTO) {
		try {
			Dao<SystemObj> systemDao = this.systemDao == null ? (Dao<SystemObj>) createSystemDao()
					: this.systemDao;
			Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
					: this.userDao;
			UserObj user = userDao.findByPK(userDTO.getId());
			List<SystemObj> systems = systemDao.findByQuery("findOneSystem",
					systemDTO.getUrl());
			SystemObj system = null;
			boolean create = false;
			if (systems.size() == 0) {
				system = new SystemObj();
				create = true;
			} else {
				system = systems.get(0);
			}
			system.setActive(true);
			if (create) {
				systemDao.save(converSystemFromDTO(system, systemDTO));
			} else {
				systemDao.update(converSystemFromDTO(system, systemDTO));
			}
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.ADD, user, "added system",
						new Date());
			}
			userDao.close();
			systemDao.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// try {
		//
		// SystemObj system = new SystemObj();
		// system.setActive(true);
		// systemDao.save(converSystemFromDTO(system, systemDTO));
		// if (enableLogging) {
		// LogUtils.createLogEntry(LogEntryType.ADD, user, "added system",
		// new Date());
		// }
		// } catch (Exception e) {
		// systemDao.rollback();
		// SystemObj systemObj = systemDao.findByPK(systemDTO.getId());
		// systemObj.setActive(true);
		// systemDao.update(converSystemFromDTO(systemObj, systemDTO));
		// if (enableLogging) {
		// LogUtils.createLogEntry(LogEntryType.UPDATE, user,
		// "updated system", new Date());
		// }
		// } finally {
		// userDao.close();
		// systemDao.close();
		// }
	}

	@SuppressWarnings("unchecked")
	public void updateSystem(SystemDTO systemDTO, UserDTO userDTO) {
		createSystem(systemDTO, userDTO);
		// Dao<SystemObj> systemDao = this.systemDao == null ? (Dao<SystemObj>)
		// createSystemDao()
		// : this.systemDao;
		// Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>)
		// createUserDao()
		// : this.userDao;
		// SystemObj system = systemDao.findByPK(systemDTO.getId());
		// system.setActive(true);
		// systemDao.update(converSystemFromDTO(system, systemDTO));
		//
		// UserObj user = userDao.findByPK(userDTO.getId());
		// if (enableLogging) {
		// LogUtils.createLogEntry(LogEntryType.UPDATE, user,
		// "updated system", new Date());
		// }
		// userDao.close();
		// systemDao.close();
	}

	@SuppressWarnings("unchecked")
	public void deleteSystem(SystemDTO systemDTO, UserDTO userDTO) {
		Dao<SystemObj> systemDao = this.systemDao == null ? (Dao<SystemObj>) createSystemDao()
				: this.systemDao;
		Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
				: this.userDao;
		SystemObj system = systemDao.findByPK(systemDTO.getId());
		system.setActive(false);
		systemDao.delete(system);

		UserObj user = userDao.findByPK(userDTO.getId());
		userDao.close();
		systemDao.close();
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.REMOVE, user,
					"deleted system", new Date());
		}
	}

	@SuppressWarnings("unchecked")
	public List<SystemDTO> showAllSystems(UserDTO userDTO) {
		Dao<SystemObj> systemDao = this.systemDao == null ? (Dao<SystemObj>) createSystemDao()
				: this.systemDao;
		Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
				: this.userDao;
		UserObj user = userDao.findByPK(userDTO.getId());
		List<SystemObj> systems = systemDao.findByQuery("findSystems");
		userDao.close();
		systemDao.close();
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.RETRIEVE, user,
					"retrieved all systems", new Date());
		}
		return convertSystemsToDTO(systems);
	}

	public boolean optimize(SystemDTO systemDTO,UserDTO userDTO){
		Dao<UserObj> userDao = this.userDao == null ? (Dao<UserObj>) createUserDao()
				: this.userDao;
		UserObj user = userDao.findByPK(userDTO.getId());
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.OPTIMIZE, user,
					"optimized server "+ systemDTO.getUrl(), new Date());
		}
		SolrServer server = new SolrServer();
		server.setUrl(systemDTO.getUrl());
		return server.optimize();
	}
	
	private SystemObj converSystemFromDTO(SystemObj system, SystemDTO systemDTO) {
		
		system.setType(SystemType.valueOf(systemDTO.getType()));
		system.setUrl(systemDTO.getUrl());
		return system;
	}

	private List<SystemDTO> convertSystemsToDTO(List<SystemObj> systems) {
		List<SystemDTO> systemDTOs = new ArrayList<SystemDTO>();
		for (SystemObj system : systems) {
			SystemDTO systemDTO = new SystemDTO();
			systemDTO.setId(system.getId());
			systemDTO.setType(system.getType().toString());
			systemDTO.setUrl(system.getUrl());
			systemDTOs.add(systemDTO);
		}
		return systemDTOs;
	}
}

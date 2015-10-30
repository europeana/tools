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

import eu.europeana.record.management.client.SolrSystemService;
import eu.europeana.record.management.database.dao.Dao;
import eu.europeana.record.management.database.dao.SystemObjectDao;
import eu.europeana.record.management.database.dao.impl.DaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.SystemObj;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.server.components.ServerService;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.shared.dto.SystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * @see SolrSystemService.java
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public abstract class SystemServiceImpl<T extends SystemObj, G extends SystemDTO> extends RemoteServiceServlet{



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SystemObjectDao<T> systemDao;
	private Dao<UserObj> userDao;
	boolean enableLogging = true;
	
	abstract protected SystemObjectDao<T> createSystemDao();
	
	abstract protected T convertSystemFromDTO(T system, G systemDTO);

	abstract protected List<G> convertSystemsToDTO(List<T> systems);

	abstract protected T initializeSystemObl();

	public SystemObjectDao<T> getSystemDao() {
		if (this.systemDao == null || !this.systemDao.isOpen()) {
			this.systemDao = createSystemDao();
		}
		return this.systemDao;
	}

	public void setSystemDao(SystemObjectDao<T> systemDao) {
		this.systemDao = systemDao;
	}

	public Dao<UserObj> getUserDao() {
		if (this.userDao == null || !this.userDao.isOpen() ) {
			this.userDao = (Dao<UserObj>) createUserDao();
		}

		return this.userDao;
	}

	public void setUserDao(Dao<UserObj> userDao) {
		this.userDao = userDao;
	}

	
	public Dao<UserObj> createUserDao() {
		return new DaoImpl<UserObj>(UserObj.class, PersistentEntityManager.getManager());
	}

	public void enableLogging(boolean enable) {
		this.enableLogging = enable;
	}

	public void createSystem(G systemDTO, UserDTO userDTO) {
		try {
			

			UserObj user = getUserDao().findByPK(userDTO.getId());
			T system = getSystemDao().findByURLs(systemDTO.getUrls());

			boolean create = false;
			if (system == null) {
				system = initializeSystemObl();
				create = true;
			}
		
			if (create) {
				getSystemDao().save(convertSystemFromDTO(system, systemDTO));
			} else {
				getSystemDao().update(convertSystemFromDTO(system, systemDTO));
			}
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.ADD, user, "added system", new Date());
			}
			getUserDao().close();
			getSystemDao().close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateSystem(G systemDTO, UserDTO userDTO) {
		createSystem(systemDTO, userDTO);
	}

	public void deleteSystem(G systemDTO, UserDTO userDTO) {
		
		T system = getSystemDao().findByPK(systemDTO.getId());
		getSystemDao().delete(system);

		UserObj user = getUserDao().findByPK(userDTO.getId());
		getUserDao().close();
		getSystemDao().close();
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.REMOVE, user, "deleted system", new Date());
		}
	}

	public List<G> showAllSystems(UserDTO userDTO) {
		
		UserObj user = getUserDao().findByPK(userDTO.getId());
		List<T> systems = getSystemDao().findSystems();
		getUserDao().close();
		getSystemDao().close();
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.RETRIEVE, user, "retrieved all systems", new Date());
		}
		return convertSystemsToDTO(systems);
	}



}

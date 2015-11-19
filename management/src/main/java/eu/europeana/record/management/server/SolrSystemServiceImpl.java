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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;

import eu.europeana.record.management.client.SolrSystemService;
import eu.europeana.record.management.database.dao.SystemObjectDao;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.dao.impl.SolrSystemDaoImpl;
import eu.europeana.record.management.database.entity.SolrSystemObj;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.database.enums.LogEntryType;
import eu.europeana.record.management.database.enums.ProfileType;
import eu.europeana.record.management.server.components.SolrService;
import eu.europeana.record.management.server.util.LogUtils;
import eu.europeana.record.management.shared.dto.SolrSystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * @see SolrSystemService.java
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class SolrSystemServiceImpl extends SystemServiceImpl<SolrSystemObj, SolrSystemDTO>implements SolrSystemService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void createSolrSystem(SolrSystemDTO systemDTO, UserDTO userDTO) {
		createSystem(systemDTO, userDTO);

	}

	@Override
	public void updateSolrSystem(SolrSystemDTO systemDTO, UserDTO userDTO) {
		updateSystem(systemDTO, userDTO);

	}

	@Override
	public void deleteSolrSystem(SolrSystemDTO systemDTO, UserDTO userDTO) {
		deleteSystem(systemDTO, userDTO);

	}

	@Override
	public List<SolrSystemDTO> showAllSolrSystems(UserDTO userDTO) {
		return showAllSystems(userDTO);
	}

	@Override
	public boolean optimize(SolrSystemDTO systemDTO, UserDTO userDTO) {

		UserObj user = getUserDao().findByPK(userDTO.getId());
		getUserDao().close();
		if (enableLogging) {
			LogUtils.createLogEntry(LogEntryType.OPTIMIZE, user, "optimized solr server " + systemDTO.getUrls(),
					new Date());
		}

		try {
			(new SolrService()).optimize(convertSystemFromDTO(initializeSystemObl(), systemDTO));
			return true;
		} catch (SolrServerException e) {
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.EXCEPTION, user,
						"optimized solr server " + systemDTO.getUrls() + " failed!", new Date());
			}
			e.printStackTrace();
		} catch (IOException e) {
			if (enableLogging) {
				LogUtils.createLogEntry(LogEntryType.EXCEPTION, user,
						"optimized solr server " + systemDTO.getUrls() + " failed!", new Date());
			}
			e.printStackTrace();
		}
		return false;

	}

	@Override
	protected SystemObjectDao<SolrSystemObj> createSystemDao() {
		return new SolrSystemDaoImpl(PersistentEntityManager.getManager());
	}

	@Override
	protected SolrSystemObj convertSystemFromDTO(SolrSystemObj system, SolrSystemDTO systemDTO) {

		system.setPassword(systemDTO.getPassword());
		system.setProfileType(ProfileType.valueOf(systemDTO.getProfileType()));
		system.setSolrCore(systemDTO.getSolrCore());
		system.setUrls(systemDTO.getUrls());
		system.setUserName(systemDTO.getUserName());
		system.setZookeeperURL(systemDTO.getZookeeperURL());
		return system;
	}

	@Override
	protected List<SolrSystemDTO> convertSystemsToDTO(List<SolrSystemObj> systems) {
		List<SolrSystemDTO> systemDTOs = new ArrayList<SolrSystemDTO>();
		for (SolrSystemObj system : systems) {
			SolrSystemDTO systemDTO = new SolrSystemDTO();

			systemDTO.setId(system.getId());
			systemDTO.setPassword(system.getPassword());
			systemDTO.setProfileType(system.getProfileType() != null ? system.getProfileType().toString() : null);
			systemDTO.setSolrCore(system.getSolrCore());
			systemDTO.setUrls(system.getUrls());
			systemDTO.setUserName(system.getUserName());
			systemDTO.setZookeeperURL(system.getZookeeperURL());
			
			systemDTOs.add(systemDTO);
		}
		return systemDTOs;
	}

	@Override
	protected SolrSystemObj initializeSystemObl() {
		return new SolrSystemObj();
	}




}

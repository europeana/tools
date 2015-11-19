package eu.europeana.record.management.server;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.record.management.client.MongoSystemService;
import eu.europeana.record.management.database.dao.SystemObjectDao;
import eu.europeana.record.management.database.dao.impl.MongoSystemDaoImpl;
import eu.europeana.record.management.database.dao.impl.PersistentEntityManager;
import eu.europeana.record.management.database.entity.MongoSystemObj;
import eu.europeana.record.management.database.enums.ProfileType;
import eu.europeana.record.management.shared.dto.MongoSystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

public class MongoSystemServiceImpl extends SystemServiceImpl<MongoSystemObj, MongoSystemDTO>
		implements MongoSystemService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void createMongoSystem(MongoSystemDTO systemDTO, UserDTO userDTO) {
		createSystem(systemDTO, userDTO);

	}

	@Override
	public void updateMongoSystem(MongoSystemDTO systemDTO, UserDTO userDTO) {
		updateSystem(systemDTO, userDTO);

	}

	@Override
	public void deleteMongoSystem(MongoSystemDTO systemDTO, UserDTO userDTO) {
		deleteSystem(systemDTO, userDTO);

	}

	@Override
	public List<MongoSystemDTO> showAllMongoSystems(UserDTO userDTO) {
		return showAllSystems(userDTO);
	}

	@Override
	protected SystemObjectDao<MongoSystemObj> createSystemDao() {
		return new MongoSystemDaoImpl(PersistentEntityManager.getManager());
	}

	@Override
	protected MongoSystemObj convertSystemFromDTO(MongoSystemObj system, MongoSystemDTO systemDTO) {
		
		system.setPassword(systemDTO.getPassword());
		system.setProfileType(ProfileType.valueOf(systemDTO.getProfileType()));
		system.setMongoDBName(systemDTO.getMongoDBName());
		system.setUrls(systemDTO.getUrls());
		system.setUserName(systemDTO.getUserName());
		return system;
	}

	@Override
	protected List<MongoSystemDTO> convertSystemsToDTO(List<MongoSystemObj> systems) {
		List<MongoSystemDTO> systemDTOs = new ArrayList<MongoSystemDTO>();
		for (MongoSystemObj system : systems) {
			MongoSystemDTO systemDTO = new MongoSystemDTO();
			
			systemDTO.setId(system.getId());
			systemDTO.setPassword(system.getPassword());
			systemDTO.setProfileType(system.getProfileType() != null ? system.getProfileType().toString() : null);
			systemDTO.setMongoDBName(system.getMongoDBName());
			systemDTO.setUrls(system.getUrls());
			systemDTO.setUserName(system.getUserName());
			
			systemDTOs.add(systemDTO);
		}
		return systemDTOs;
	}

	@Override
	protected MongoSystemObj initializeSystemObl() {
		return new MongoSystemObj();
	}

}

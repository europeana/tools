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
package eu.europeana.record.management.database.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.europeana.record.management.database.dao.DBEntity;
import eu.europeana.record.management.database.enums.ProfileType;
import eu.europeana.record.management.database.enums.SystemType;

/**
 * A system object representation
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "STYPE", discriminatorType = DiscriminatorType.STRING)
@Table(name = "SYSTEM_OBJS", uniqueConstraints = @UniqueConstraint(columnNames = {
		"STYPE", "URLS" }))
public class SystemObj implements DBEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5281432844703329086L;

	private Long id;

	private SystemType type;
	
	private ProfileType profileType;

	private String urls;

	
	private String userName;
	
	private String password;

	/**
	 * Solr or Mongo
	 * 
	 * @return SOLR or MONGO
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "STYPE", insertable = false, updatable = false)
	public SystemType getType() {
		return type;
	}

	public void setType(SystemType type) {
		this.type = type;
	}

	/**
	 * The url the system is accessible
	 * 
	 * @return The system url
	 */
	@Column(name = "URLS", updatable = true)
	public String getUrls() {
		return urls;
	}

	public void setUrls(String urls) {
		this.urls = urls;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "PROFILE_TYPE", insertable = true, updatable = true)
	public ProfileType getProfileType() {
		return profileType;
	}

	public void setProfileType(ProfileType profileType) {
		this.profileType = profileType;
	}

	@Column(name = "USERNAME")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Column(name = "PASSWORD")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}

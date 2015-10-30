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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import eu.europeana.record.management.database.dao.DBEntity;

/**
 * Class that denotes when a user was last connected
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@Entity
@Table(name = "SESSIONS")
@NamedQueries({ @NamedQuery(name = "findLoginByUser", query = "Select s from Session s where s.user = ?1") })
public class Session implements DBEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private UserObj user;

	private Date lastLogin;

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	public Long getId() {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * The user that logged in
	 * 
	 * @return
	 */
	@OneToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "id")
	public UserObj getUser() {
		return user;
	}

	public void setUser(UserObj user) {
		this.user = user;
	}

	/**
	 * When was the user last logged in
	 * 
	 * @return
	 */
	@Column(name = "LAST_LOGIN")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}



}

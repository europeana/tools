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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import eu.europeana.record.management.database.dao.DBEntity;
import eu.europeana.record.management.database.enums.LogEntryType;

/**
 * A LogEntry representation
 * 
 * @author Yorgos.Mamakis
 * 
 */
@Entity
@Table(name = "LOG_ENTRIES")
@NamedQueries({ @NamedQuery(name = "findByUser", query = "Select l from LogEntry l where l.user = ?1") })
public class LogEntry implements DBEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	
	private UserObj user;
	
	private LogEntryType action;
	
	private String message;

	private Date timestamp;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * The user that created the log entry
	 * 
	 * @return The user
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID", referencedColumnName = "ID")
	public UserObj getUser() {
		return user;
	}

	public void setUser(UserObj user) {
		this.user = user;
	}

	/**
	 * The type of action
	 * 
	 * @return The type of action
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "ACTION")
	public LogEntryType getAction() {
		return action;
	}

	public void setAction(LogEntryType action) {
		this.action = action;
	}

	/**
	 * The message of the action (can be empty but not null)
	 * 
	 * @return
	 */
	@Column(name = "MESSAGE")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * When was this log entry created
	 * 
	 * @return
	 */
	@Column(name = "TIME_STAMP")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}



}

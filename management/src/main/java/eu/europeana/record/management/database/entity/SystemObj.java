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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import eu.europeana.record.management.database.dao.DBEntity;
import eu.europeana.record.management.database.enums.SystemType;

/**
 * A system object representation
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@Entity
@Table(name = "SystemObj")
@NamedQueries({ @NamedQuery(name = "findSystems", query = "Select s from SystemObj s where s.active=true"),
	@NamedQuery(name = "findOneSystem", query = "Select s from SystemObj s where s.url = ?1")})
public class SystemObj implements DBEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5281432844703329086L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	SystemType type;
	@Column(name = "url", unique = true, updatable = true)
	String url;
	@Column(name = "active")
	Boolean active;
	
	
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	/**
	 * Solr or Mongo
	 * 
	 * @return SOLR or MONGO
	 */
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
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		// TODO Auto-generated method stub
		return id;
	}

}

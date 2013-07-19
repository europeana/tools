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
package eu.europeana.record.management.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A data transformation object that corresponds to a UserObj
 * @author gmamakis
 *
 */
public class UserDTO implements IsSerializable {

	private String username;
	private String password;
	private String name;
	private String surname;
	private String role;
	private String date;
	private Long id;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		try{
		String s = "User = [id: "+this.id+"],[name: " + this.name + "],[surname: " + this.surname
				+ "],[username: " + this.username + "],[password: " + password +"],[role: "+role+"],[date:"+this.date+"]";
		return s;
		} catch (Exception e){
			
			e.printStackTrace();
			
		}
		return "";
	}
}

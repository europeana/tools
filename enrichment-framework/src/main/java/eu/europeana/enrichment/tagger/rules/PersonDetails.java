/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.tagger.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Person details parsed from a name. Built from the Europeana experiences.
 * 
 * @author Borys Omelayenko
 * 
 */
public class PersonDetails {

	private String firstName;
	private String lastName;
	private String birthDate;
	private String deathDate;
	private String birthPlace;
	private String deathPlace;

	final String COMMA = ",";

	Pattern datePattern = Pattern
			.compile("(.+)\\((\\d\\d\\d\\d)-(\\d\\d\\d\\d)\\)");

	public PersonDetails(String name) {

		// default - last name
		lastName = name;

		// remove dates
		Matcher dateMatch = datePattern.matcher(lastName);
		if (dateMatch.matches()) {
			lastName = dateMatch.group(1);
			birthDate = dateMatch.group(2);
			deathDate = dateMatch.group(3);
		}
		// normalise first - last
		int index = lastName.indexOf(COMMA);
		if (index > 0 && index == lastName.lastIndexOf(COMMA)) {
			firstName = lastName.substring(index + COMMA.length()).trim();
			lastName = lastName.substring(0, index).trim();
		}

		//
	}

	public String getFullName() {
		return hasFirstName() ? (firstName + " " + lastName) : lastName;
	}

	private boolean hasFirstName() {
		return firstName != null && !firstName.isEmpty();
	}

	/* Getters */

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public String getDeathDate() {
		return deathDate;
	}

	public String getBirthPlace() {
		return birthPlace;
	}

	public String getDeathPlace() {
		return deathPlace;
	}

}
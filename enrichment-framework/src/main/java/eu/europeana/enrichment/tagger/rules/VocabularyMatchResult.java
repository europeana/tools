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



/**
 * Vocabulary match results.
 * 
 * 
 * @author Borys Omelayenko
 * 
 */
public enum VocabularyMatchResult {
	
	matched("matched", "A match is found and considered valid and unambigous"),
	ambigous("ambigous", "Multiple ambigous matches found, disambiguation failed"),
	missed("missed", "No match found"),
	error("error", "Error occurred");

	private String name;
	private String description;

	private VocabularyMatchResult(String name, String description) {
		this.name = name;
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}

}
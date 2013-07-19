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
package eu.annocultor.reports;

import java.io.File;
import java.io.IOException;

import eu.annocultor.reports.parts.ReportCounter;
import eu.annocultor.reports.parts.ReportList;



/**
 * Abstract reporter, with persistence handling.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class AbstractReporter
{
	public static class Lookup {
		private String dataset;
		private String vocabulary;
		private String rule;
		private String path;
		private String result;
		private String label;
		private String code;
		public Lookup(
				String dataset, 
				String vocabulary, 
				String rule,
				String path,
				String result, 
				String label, 
				String code) {
			this.dataset = dataset;
			this.vocabulary = vocabulary;
			this.rule = rule;
			this.path = path;
			this.result = result;
			this.label = label;
			this.code = code;
		}
		public String getDataset() {
			return dataset;
		}
		public String getVocabulary() {
			return vocabulary;
		}
		public String getRule() {
			return rule;
		}
		public String getPath() {
			return path;
		}
		public String getResult() {
			return result;
		}
		public String getLabel() {
			return label;
		}
		public String getCode() {
			return code;
		}
		@Override
		public String toString() {
			return "Lookup " + asString();
		}
		public String asString() {
			return "[code=" + code + ", dataset=" + dataset + ", label="
			+ label + ", result=" + result + ", rule=" + rule
			+ ", vocabulary=" + vocabulary + "]";
		}
		@Override
		public int hashCode() {
			return asString().hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Lookup other = (Lookup) obj;
			return asString().equals(other.asString());
		}
	}

	public static class Id {
		
		private String id;

		public String getId() {
			return id;
		}

		public Id(String id) {
			super();
			this.id = id;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Id other = (Id) obj;
			return id.equals(other.id);
		}
	}
	
	public static class Graphs extends Id {
		private Integer subjects;
		private Integer triples;
		private String diff;
		public Integer getSubjects() {
			return subjects;
		}
		public Integer getTriples() {
			return triples;
		}
		public String getDiff() {
			return diff;
		}
		public Graphs(String id, Integer subjects, Integer triples, String diff) {
			super(id);
			this.subjects = subjects;
			this.triples = triples;
			this.diff = diff;
		}
		public String asString() {
			return getId() + ":" + getSubjects() + ":" + getTriples();
		}
		@Override
		public int hashCode() {
			return asString().hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Graphs other = (Graphs) obj;
			return asString().equals(other.asString());
		}
		
	}

	public static class RuleInvocation extends Id {
		
		private String rule;
		private String path;
		
		public String getRule() {
			return rule;
		}
		public String getPath() {
			return path;
		}
		public RuleInvocation(String id, String rule, String path) {
			super(id);
			this.rule = rule;
			this.path = path;
		}
		public String asString() {
			return getId() + ":" + getRule() + ":" + getPath();
		}
		@Override
		public int hashCode() {
			return asString().hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			RuleInvocation other = (RuleInvocation) obj;
			return asString().equals(other.asString());
		}
		
	}

	public static class KeyValuePair {

		private String key;
		private String value;

		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
		public KeyValuePair(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}	
		public String asSring() {
			return key + "=" + value;
		}
		@Override
		public int hashCode() {
			return asSring().hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			KeyValuePair other = (KeyValuePair) obj;
			return asSring().equals(other.asSring());
		}

	}

	protected ReportCounter<Lookup> lookupCounters;

	protected ReportList<String> messages;

	protected ReportList<KeyValuePair> environment;

	protected ReportCounter<Id> forgottenPaths;

	protected ReportCounter<RuleInvocation> invokedRules;
	
	protected ReportList<Graphs> graphs;

	public static final String FILE_LOOKUPS = "lookups";
	public static final String FILE_MESSAGES = "messages";
	public static final String FILE_FORGOTTEN_RULES = "forgottenTags";
	public static final String FILE_RULES = "invokedRules";
	public static final String FILE_ENV = "environment";
	public static final String FILE_GRAPHS = "graphs";

	private static final int MAX_SIZE = 10000;

	private File reportDir;
	
	public File getReportDir() {
		return reportDir;
	}
	
	public AbstractReporter(File reportDir, String datasetId ) {
		this.reportDir = new File(reportDir, datasetId);
		this.reportDir.mkdirs();
	}
	
	protected void init() throws Exception {
		lookupCounters = new ReportCounter<AbstractReporter.Lookup>(reportDir, FILE_LOOKUPS, MAX_SIZE);
		messages = new ReportList<String>(reportDir, FILE_MESSAGES, MAX_SIZE);
		environment = new ReportList<KeyValuePair>(reportDir, FILE_ENV, MAX_SIZE);
		forgottenPaths = new ReportCounter<Id>(reportDir, FILE_FORGOTTEN_RULES, MAX_SIZE);
		invokedRules = new ReportCounter<RuleInvocation>(reportDir, FILE_RULES, MAX_SIZE);
		graphs = new ReportList<Graphs>(reportDir, FILE_GRAPHS, MAX_SIZE);
	}

	protected void load() throws Exception {
		invokedRules.load();
		lookupCounters.load();
		messages.load();
		environment.load();
		forgottenPaths.load();
		graphs.load();
	}

	protected void flush() throws IOException {
		lookupCounters.flush();
		messages.flush();
		environment.flush();
		forgottenPaths.flush();
		invokedRules.flush();
		graphs.flush();
	}

}

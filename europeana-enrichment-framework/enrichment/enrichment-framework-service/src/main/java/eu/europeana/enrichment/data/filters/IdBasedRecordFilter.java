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
package eu.europeana.enrichment.data.filters;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.common.Utils;

/**
 * A filter that operates in two independent runs:
 * <ul>
 * <li>collect a list of values and persist it to a file. Typically these are
 * IDs</li>
 * <li>read the list of values and check if a new value is included in the list</li>
 * </ul>
 * 
 * 
 * @author Borys Omelayenko
 * 
 */
public class IdBasedRecordFilter implements TwoIterationsRecordFilter {

	Logger log = LoggerFactory.getLogger(getClass().getName());

	public static final String ANNOCULTOR_FILTERS_FILE_TO_READ = "ANNOCULTOR_FILTERS_FILE_TO_READ";
	public static final String ANNOCULTOR_FILTERS_FILE_TO_WRITE = "ANNOCULTOR_FILTERS_FILE_TO_WRITE";

	private File fileToRead;
	private File fileToWrite;

	protected Set<String> includedRecords = new HashSet<String>();

	@Override
	public void init() throws Exception {

		String fileToReadName = Utils
				.getLocalOrGlobalEnvironmentVariable(ANNOCULTOR_FILTERS_FILE_TO_READ);
		if (!StringUtils.isEmpty(fileToReadName)) {
			fileToRead = new File(fileToReadName);
		}
		String fileToWriteName = Utils
				.getLocalOrGlobalEnvironmentVariable(ANNOCULTOR_FILTERS_FILE_TO_WRITE);
		if (!StringUtils.isEmpty(fileToWriteName)) {
			fileToWrite = new File(fileToWriteName);
		}

		if (isReadMode()) {
			log.info("Read mode is on");
		}
		if (isWriteMode()) {
			log.info("Write mode is on");
		}
	}

	@Override
	public boolean isIncluded(String id) {
		if (isReadMode()) {
			return isIncludedRecordInternal(id);
		} else {
			return true;
		}
	}

	boolean isIncludedRecordInternal(String id) {
		return includedRecords.contains(id);
	}

	private boolean isReadMode() {
		return fileToRead != null;
	}

	@Override
	public void addIncludedRecord(String id) {
		if (isWriteMode()) {
			includedRecords.add(id);
		}
	}

	private boolean isWriteMode() {
		return fileToWrite != null;
	}

	@Override
	public void load() throws Exception {
		if (isReadMode()) {
			includedRecords.addAll(FileUtils.readLines(fileToRead, "UTF-8"));
		}
	}

	@Override
	public void save() throws Exception {
		if (isWriteMode()) {
			FileUtils.writeLines(fileToWrite, includedRecords, "\n");
		}
	}

}

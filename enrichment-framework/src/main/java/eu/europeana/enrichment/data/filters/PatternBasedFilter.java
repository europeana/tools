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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A filter that reads patterns for files to include and exclude from files.
 * 
 * 
 * @author Borys Omelayenko
 * 
 */
public class PatternBasedFilter implements RecordFilter {

	Logger log = LoggerFactory.getLogger(getClass().getName());

	private List<String> includePatterns = new ArrayList<String>();
	private List<String> excludePatterns = new ArrayList<String>();

	public PatternBasedFilter(File includeFile, File excludeFile) throws IOException {
		load(includeFile, includePatterns);
		load(excludeFile, excludePatterns);
	}

	void load(File file, List<String> patterns) throws IOException {
		for (Object o : FileUtils.readLines(file, "UTF-8")) {
			String line = o.toString();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			patterns.add(line);
		}
		if (patterns.isEmpty()) {
			log.warn("Did not loaded much filters from " + file);
		}	
	}

	public boolean isIncluded(String url) { 

		if (StringUtils.isEmpty(url)) {
			return false;
		}
		if (match(url, excludePatterns)) {
			return false;
		}
		return match(url, includePatterns);
	}    

	boolean match(String url, List<String> patterns) {
		for (String pattern : patterns) {
			if (url.matches(pattern)) {
				return true;
			}
		}  	
		return false;
	}
}

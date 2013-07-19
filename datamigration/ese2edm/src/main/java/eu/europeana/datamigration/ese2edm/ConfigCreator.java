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
package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.tools.utils.PopularTermSearchWarmer;
import eu.europeana.datamigration.ese2edm.enums.FieldMapping;

/**
 * Creator of the search warmer snippet
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class ConfigCreator {

	/**
	 * Method that creates the search warmer snippet
	 * 
	 * @param args
	 *            Expect the input file with the search queries to explicitly
	 *            warm and an output file with the snippet to be appended to 
	 *            SOLR searches in solrconfig.xml
	 */
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		try {
			for (String val : FileUtils.readLines(new File(args[0]))) {
				for (FieldMapping fm : FieldMapping.values()) {
					if (fm.isContainedIn(val) != null) {
						val = StringUtils.replace(val, fm.getEseField(),
								fm.getEdmField());
					}
				}
				sb.append(PopularTermSearchWarmer.createTermsSection(val));
			}
			FileUtils.write(new File(args[1]), sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

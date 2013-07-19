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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.datamigration.ese2edm.converters.Ese2EdmConverter;

/**
 * Metadata mass migration console application from ESE to EDM
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class SolrConverter {

	/**
	 * Main method
	 * 
	 * @param args
	 *            The location with ranges of records to index
	 */
	public static void main(String[] args) {

		//int no_of_threads = 10;
		int i = 0;
		List<String> chunks = new ArrayList<String>();
		try {
			//chunks = IOUtils.readLines(new FileInputStream(new File(args[0])));
			chunks = IOUtils.readLines(new FileInputStream(new File("/home/gmamakis/chunks/for_yorgos")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (i < chunks.size()) {
			Ese2EdmConverter converter = new Ese2EdmConverter();
			long start = Long
					.parseLong(StringUtils.split(chunks.get(i), "-")[0]);
			long difference = Long.parseLong(StringUtils.split(chunks.get(i),
					"-")[1]);
			converter.setFrom(start);
			converter.setThreadName(i);
			converter.setTo(start + difference);
			converter.setModel("ese");
			// This should be enabled in the case of EDM 2 EDM migration
//			if (args[1] != null) {
//				converter.setModel(args[1]);
//			}
			Thread t = new Thread(converter);
			t.start();
			i++;
		}

	}
}

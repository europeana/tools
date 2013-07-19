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

import eu.europeana.datamigration.ese2edm.converters.Ese2EdmCollectionConverter;

/**
 * Console application for collection-based metadata mass migration from ESE to
 * EDM
 * 
 * @author Yorgos Mamakis
 * 
 */
public class SolrCollectionConverter {

	/**
	 * Main method
	 * 
	 * @param args
	 *            1. The path to a file containing the collections to index, and
	 *            the ranges on each collection each thread should index
	 *            (whitespace separated). Threads will be generated every four
	 *            collections. Collections can be split according to their
	 *            ranges
	 *            2. whether we want to output RDF or not
	 */
	public static void main(String[] args) {
		List<String> collections;
		try {
			collections = IOUtils.readLines(new FileInputStream(new File(
					args[0])));
//			collections = IOUtils.readLines(new FileInputStream(new File(
//					"/home/gmamakis/Documents/final_migration/chunks/artaxerxes.csv")));
			List<String> inputCollections = new ArrayList<String>();
			List<Integer> starts = new ArrayList<Integer>();
			List<Integer> ends = new ArrayList<Integer>();
			int i = 0;
			for (String collection : collections) {
				String[] arguments = StringUtils.split(collection, " ");
				
				inputCollections.add(arguments[0]);
				starts.add(Integer.parseInt(arguments[1]));
				ends.add(Integer.parseInt(arguments[2]));
				if (inputCollections.size() == Integer.parseInt(args[2])
						|| i== collections
								.size()) {
//				if (inputCollections.size() ==4
//						|| i== collections
//								.size()) {
					Ese2EdmCollectionConverter converter = new Ese2EdmCollectionConverter();
					converter.setCollections(inputCollections);
					converter.setStarts(starts);
					converter.setEnds(ends);
					converter.setThreadName(i/Integer.parseInt(args[2]));

					converter.setShouldDelete(Boolean.parseBoolean(args[3]));

					//converter.setCreateRDF(Boolean.parseBoolean(args[1]));
					converter.setCreateRDF(false);
					Thread t = new Thread(converter);
					t.start();
					
//					System.out.println("# of Threads:" +i/4);
//					System.out.println("In collection:" +i);
					inputCollections = new ArrayList<String>();
					starts = new ArrayList<Integer>();
					ends = new ArrayList<Integer>();
				}
				i++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

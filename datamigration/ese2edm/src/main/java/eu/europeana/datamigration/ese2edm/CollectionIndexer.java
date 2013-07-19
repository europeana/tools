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

import java.net.UnknownHostException;

import com.mongodb.MongoException;

import eu.europeana.datamigration.ese2edm.collectionmanagement.CollectionManager;

/**
 * Command-line application that creates the collection mappings for datasets
 * that have changed ID. Important in generating the correct record ID
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */

public class CollectionIndexer {
	private final static String MESSAGE="Expecting an argument to specify the file with the collections to map";
	
	/**
	 * Main method
	 * @param args The path to a colon-delimited CSV file with the collection mappings
	 */
	public static void main(String[] args) {
		if (args[0] != null) {
			CollectionManager cm;
			try {
				cm = new CollectionManager(args[0]);
				cm.index();
				cm.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MongoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.err.println(MESSAGE);
		}
	}
}

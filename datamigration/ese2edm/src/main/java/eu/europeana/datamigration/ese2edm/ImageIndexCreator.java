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

import eu.europeana.datamigration.ese2edm.image.File2MongoImageConverter;
/**
 * Deprecate command line tool that generates a sorted index of records having the necessary only information to generate the thumbnails.
 * TODO: check if sharding this index can actually help
 *
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
@Deprecated
public class ImageIndexCreator {

	/**
	 * Main method
	 * @param args 
	 * 			One argument to specify where to start from the index, 0 if nothing is provided
	 */
	public static void main(String... args){
		
		File2MongoImageConverter converter = new File2MongoImageConverter();
		converter.setStart(args.length>0?Integer.parseInt(args[0]):0);
		converter.setEnd(25000000);
		converter.createIndex();
	}
}

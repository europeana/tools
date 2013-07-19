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

import org.springframework.context.ApplicationContext;

import com.mongodb.MongoException;

import eu.europeana.corelib.db.service.ThumbnailService;
import eu.europeana.datamigration.ese2edm.image.File2MongoImageConverter;
import eu.europeana.datamigration.ese2edm.spring.AppContext;

/**
 * Command line to migrate the image thumbnails from files to a DB
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class ImageConverter {
	/**
	 * The number of threads
	 */
	private static final int NO_OF_THREADS = 4;

	/**
	 * Main method
	 * 
	 * @param args
	 *            Five numbers specifying the range of records each thread is to check for thumbnails
	 */
	public static void main(String[] args) {
		if (args.length < 5) {
			System.out
					.println("Expected 5 arguments: the start index of each of the 4 threads and the maximum number of documents to read.");
		} else {
			ApplicationContext applicationContext = AppContext
					.getApplicationContext();
			ThumbnailService thumbnailService = (ThumbnailService) applicationContext
					.getBean("thumbnailService");
			try {
				int i = 0;

				while (i < NO_OF_THREADS) {
					File2MongoImageConverter imgConverter = new File2MongoImageConverter(
							thumbnailService);
					imgConverter.setStart(Integer.parseInt(args[i]));
					imgConverter.setEnd(Integer.parseInt(args[i + 1]));
					imgConverter.setTimeout(150);
					Thread t = new Thread(imgConverter);

					t.start();
					System.out.println("Started thread " + t.getId());
					i++;
				}
			} catch (MongoException e) {
				e.printStackTrace();
			}
		}
	}

}

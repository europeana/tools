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
package eu.europeana.datamigration.ese2edm.collectionmanagement;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.tools.lookuptable.Collection;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServerImpl;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

/**
 * Class that creates and saves the Collection mappings
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class CollectionManager {

	private String filePath;
	private CollectionMongoServerImpl cm;
	/**
	 * Constructor for CollectionManager
	 * @param filePath The path where the file with the collections is located
	 * @throws UnknownHostException
	 * @throws MongoException
	 */
	public CollectionManager(String filePath) throws UnknownHostException, MongoException{
		this.filePath = filePath;
		 cm = new CollectionMongoServerImpl(
					new Mongo(PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()), PropertyUtils.getCollectionDB());
	}
	
	/**
	 * Method to index the collections
	 */
	public void index(){
		File file = new File(filePath);
		try{
			System.out.println("Reading file");
		Map<String,String> map = readFile(file);
		for(Entry<String,String>entry:map.entrySet()){
			Collection collection = new Collection();
			collection.setId(new ObjectId());
			System.out.println("adding entry" + entry.getKey());
			//Old CollectionID first
			collection.setOldCollectionId(entry.getKey().split("_")[0]);
			//New CollectiondID afterwards
			collection.setNewCollectionId(entry.getValue());
			cm.saveCollection(collection);
		}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}

	/**
	 * Close the connection to the Mongo Server
	 */
	public void close(){
		cm.close();
	}
	
	private Map<String, String> readFile(File file) throws FileNotFoundException {
			String strFileContents = "";
			FileInputStream fin;
			try {
				fin = new FileInputStream(file);
				BufferedInputStream bin = new BufferedInputStream(fin);
				byte[] contents = new byte[1024];
				int bytesRead = 0;
				while ((bytesRead = bin.read(contents)) != -1) {
					strFileContents += new String(contents, 0, bytesRead);
				}
				fin.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			Map<String,String> map = new HashMap<String,String>();
			String[] collections = StringUtils.split(strFileContents,"\n");
			for(String collection:collections){
				String[] mapping = StringUtils.split(collection,";");
				map.put(mapping[0].trim(), mapping[1].trim());
			}
			return map;
		}
	
}

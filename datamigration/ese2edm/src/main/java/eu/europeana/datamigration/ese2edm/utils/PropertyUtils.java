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
package eu.europeana.datamigration.ese2edm.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Property loader
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class PropertyUtils {

	private static String readServerUrl;
	private static String writeServerUrl;
	private static String mongoServer;
	private static String readSharderUrl;
	private static String writeSharderUrl;
	private static String europeanaIdDB;
	private static String collectionDB;
	private static String europeanaDB;
	private static String indexFixerReadUrl;
	private static String indexFixerWriteUrl;
	private static String mongoPort;
	private final static String PROPERTIES = "datamigration.properties";
	private final static String READSERVER="read.solrServer";
	private final static String WRITESERVER="write.sorlServer";
	private final static String MONGOSERVER="mongo.server";
	private final static String MONGODB="mongo.europeana.db";
	private final static String COLLECTIONDB="mongo.collection.db";
	private final static String EUROPEANAIDDB="mongo.europeanaid.db";
	private final static String READSHARDER="read.sharderUrl";
	private final static String WRITESHARDER="write.sharderUrl";
	private final static String INDEXFIXERREADURL="read.fixerUrl";
	private final static String INDEXFIXERWRITEURL="write.fixerUrl";
	private final static String MONGOPORT = "mongo.port";

	static {
		new PropertyUtils();
	}
	private PropertyUtils(){
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(PROPERTIES));
			readServerUrl = props.getProperty(READSERVER);
			writeServerUrl = props.getProperty(WRITESERVER);
			mongoServer = props.getProperty(MONGOSERVER);
			europeanaDB = props.getProperty(MONGODB);
			collectionDB = props.getProperty(COLLECTIONDB);
			europeanaIdDB = props.getProperty(EUROPEANAIDDB);
			readSharderUrl = props.getProperty(READSHARDER);
			writeSharderUrl = props.getProperty(WRITESHARDER);
			indexFixerReadUrl = props.getProperty(INDEXFIXERREADURL);
			indexFixerWriteUrl = props.getProperty(INDEXFIXERWRITEURL);
			mongoPort = props.getProperty(MONGOPORT);
			if(mongoPort!=null){
				if(!StringUtils.isNumeric(mongoPort)){
					Logger.getLogger(this.getClass()).fatal("Mongo Port is not a number");
					System.exit(0);
				}
			}
		} catch (FileNotFoundException e) {
			Logger.getLogger(this.getClass()).fatal("The properties file does not exist");
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getReadServerUrl() {
		return readServerUrl;
	}
	public static String getWriteServerUrl() {
		return writeServerUrl;
	}
	public static String getMongoServer() {
		return mongoServer;
	}
	
	public static String getReadSharderUrl() {
		return readSharderUrl;
	}
	public static String getWriteSharderUrl() {
		return writeSharderUrl;
	}
	public static String getEuropeanaIdDB() {
		return europeanaIdDB;
	}
	public static String getCollectionDB() {
		return collectionDB;
	}
	public static String getEuropeanaDB() {
		return europeanaDB;
	}

	public static String getIndexFixerReadUrl() {
		return indexFixerReadUrl;
	}

	public static String getIndexFixerWriteUrl() {
		return indexFixerWriteUrl;
	}
	
	public static int getMongoPort(){
		return Integer.parseInt(mongoPort);
	}
}

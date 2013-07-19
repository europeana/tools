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
package eu.europeana.datamigration.ese2edm.slitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.datamigration.ese2edm.converters.ESEReader;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;
/**
 * Create Solr shards of the index for more efficient record retrieval
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class Sharder {
	private final static int MAX_SIZE=27000000;
	private final static int INDEX_SIZE=3000000;
	private final static int READ_SIZE=1000;
	
	private final static String READ_SERVER=PropertyUtils.getReadSharderUrl();
	private static String writeServer = PropertyUtils.getWriteSharderUrl();
	/**
	 * Method to create the shards
	 * @param args
	 * 			Expected 2 arguments, where to start and where to end
	 */
	public void start(String[] args){
		ESEReader reader = new ESEReader(new HttpSolrServer(READ_SERVER));
		ESEReader writer;
		int index=0;
		int endIndex = MAX_SIZE;
		if(args.length>0){
			index=Integer.parseInt(args[0]);
		}
		if(args.length>1){
			endIndex = Integer.parseInt(args[1]);
		}
		while (index<endIndex){
			try {
				Logger.getLogger("new").log(Level.INFO, "Reading "+ index +" documents");
				
				SolrDocumentList readList = reader.readEseDocuments(MAX_SIZE, index, READ_SIZE);
				writer = new ESEReader(new HttpSolrServer(writeServer+(index/INDEX_SIZE)));
				List<SolrInputDocument> writeList = new ArrayList<SolrInputDocument>();
				for(SolrDocument solrDocument: readList){
					writeList.add(ClientUtils.toSolrInputDocument(solrDocument));
				}
				
				writer.getServer().add(writeList);
				Logger.getLogger("new").log(Level.INFO, "Wrote "+ index +" documents");
				index+=READ_SIZE;
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}

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
package eu.europeana.datamigration.ese2edm.converters;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * Reader for solr records class
 * @author gmamakis
 *
 */
public class ESEReader {

	private SolrServer solrServer;
	

	/**
	 * Retrieves the number of records in the SOLR index
	 * @return
	 * @throws SolrServerException 
	 */
	public long getMaxDocumentIndex() throws SolrServerException	{
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("*:*");
		solrQuery.setRows(0);
		solrQuery.setStart(0);
		try {
			QueryResponse response = solrServer.query(solrQuery);
			return response.getResults().getNumFound();
		} catch (SolrServerException e) {

			throw e;
			
		}
	}

	/**
	 * Reads in chunks ESE documents
	 * @param maxDocuments The number of records
	 * @param from The starting point
	 * @param fetch The number of records to fetch
	 * @return A List with ESE Solr documents
	 * @throws SolrServerException 
	 */
	public SolrDocumentList readEseDocuments(long maxDocuments, long from, int fetch) throws SolrServerException {
		QueryResponse response = null;
		
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.setQuery("*:*");
			
			if (maxDocuments - from > 1000) {
				solrQuery.setRows(fetch);
			} else {
				solrQuery.setRows((int) (maxDocuments - from));
			}
			solrQuery.setStart((int) from);
			try {
				 response = solrServer.query(solrQuery);
				
			} catch (SolrServerException e) {
				throw e;
			}
			

		return response.getResults();
	}

	/**
	 * Method that reads sorted documents on the edm:object_hash. Required for Image migration on sorted index
	 * @param maxDocuments
	 * @param from
	 * @param fetch
	 * @return
	 * @throws SolrServerException
	 */
	@Deprecated
	public SolrDocumentList readSortedDocuments(long maxDocuments,int from, int fetch) throws SolrServerException {
		QueryResponse response = null;
		
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("*:*");
		solrQuery.setSortField("provider_aggregation_edm_object_hash", ORDER.asc);
		
		if (maxDocuments - from > 1000) {
			solrQuery.setRows(fetch);
		} else {
			solrQuery.setRows((int) (maxDocuments - from));
		}
		solrQuery.setStart(from);
		try {
			 response = solrServer.query(solrQuery);
			
		} catch (SolrServerException e) {
			throw e;
		}
		

	return response.getResults();
	}
	
	/**
	 * Reader method for Collections
	 * @param query The query to execute
	 * @param start Where to start from
	 * @return A SolrDocumentList with the results
	 * @throws SolrServerException
	 */
	public SolrDocumentList readCollection(String query,int start) throws SolrServerException{
		QueryResponse response = null;
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q", query);
		solrParams.set("start",start);
		solrParams.set("rows",300);
		
		
		try {
			 response = solrServer.query(solrParams);
			
					
		} catch (SolrServerException e) {
			throw e;
		}
		
		return response.getResults();
	}
	
	/**
	 * Constructor for ESEReader
	 * @param solrServer A Solr Server
	 */
	public ESEReader(SolrServer solrServer) {
		this.solrServer = solrServer;
	}
	
	/**
	 * Get the instance Solr Server
	 * @return The instance SolrServer
	 */
	public SolrServer getServer(){
		return this.solrServer;
	}
	
	/**
	 * Get the total number records for a query
	 * @param query The query to search for
	 * @return The number of results
	 * @throws SolrServerException
	 */
	public long getMax(String query) throws SolrServerException{
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q", query);
		solrParams.set("rows",0);
		try {
			 return  solrServer.query(solrParams).getResults().getNumFound();
			
					
		} catch (SolrServerException e) {
			throw e;
		}
		
	}
	
	public SolrDocument fetchRecord(String id) throws SolrServerException {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q",id);
		SolrDocumentList solrList = solrServer.query(solrParams).getResults();
		if(solrList.size()>0){
			return solrList.get(0);
		}
		return null;
	}
}

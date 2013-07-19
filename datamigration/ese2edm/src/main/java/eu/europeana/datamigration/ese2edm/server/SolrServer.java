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
package eu.europeana.datamigration.ese2edm.server;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.util.NamedList;
/**
 * SolrServer implementation
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class SolrServer extends org.apache.solr.client.solrj.SolrServer {

	
	private static final long serialVersionUID = 8511548496762644263L;
	org.apache.solr.client.solrj.SolrServer solrServer;
	
	/**
	 * Create a Solr Server from a URL
	 * @param serverUrl
	 * @throws MalformedURLException
	 */
	public void createReadSolrServer(String serverUrl) throws MalformedURLException{
		solrServer = new HttpSolrServer(serverUrl);
		
	}
	
	/**
	 * Create a Solr Server from a URL
	 * @param serverUrl
	 * @throws MalformedURLException
	 */
	public void createWriteSolrServer(String serverUrl) throws MalformedURLException{
		solrServer = new HttpSolrServer(serverUrl);
	}
	
	@Override
	public NamedList<Object> request(SolrRequest request)
			throws SolrServerException, IOException {
		return solrServer.request(request);
	}

	@Override
	public void shutdown() {
		solrServer.shutdown();
		
	}

}

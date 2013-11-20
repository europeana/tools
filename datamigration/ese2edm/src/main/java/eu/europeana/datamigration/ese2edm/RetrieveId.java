package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import eu.europeana.datamigration.ese2edm.server.SolrServer;

public class RetrieveId {
	private final static int WINDOW=1000;
	private final static int LIMIT=27;
	private final static String COLLECTION="09314";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SolrServer solrServer = new SolrServer();
		try {
			solrServer.createReadSolrServer("http://localhost:9595/solr/search2");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int i=0;
		while (i<LIMIT){
			List<String> uris = new ArrayList<String>();
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.setQuery("europeana_collectionName:"+COLLECTION+"*");
			solrQuery.setRows(WINDOW);
			solrQuery.setStart(i*WINDOW);
		try {
			QueryResponse qr = solrServer.query(solrQuery);
			SolrDocumentList lst = qr.getResults();
			for(SolrDocument doc: lst){
				uris.add(doc.getFieldValue("europeana_uri").toString());
			}
			FileUtils.writeLines(new File("/home/gmamakis/data_migration sets/final2/"+COLLECTION+"_original"), uris, true);
			System.out.println(i);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		i++;
		}
	}

}

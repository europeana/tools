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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;
/**
 * Collection appender for ESE records
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class IndexFixer {

	public static void main(String[] args) {

		HttpSolrServer readSolrServer = new HttpSolrServer(PropertyUtils.getIndexFixerReadUrl());
		readSolrServer.setParser(new XMLResponseParser());
		HttpSolrServer writeSolrServer = new HttpSolrServer(PropertyUtils.getIndexFixerWriteUrl());
		writeSolrServer.setParser(new XMLResponseParser());
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q", args[0]);
		solrParams.set("start",0);
		solrParams.set("rows",args[1]);
		try {
			SolrDocumentList docList = readSolrServer.query(solrParams).getResults();
			List<SolrInputDocument> inputList = new ArrayList<SolrInputDocument>();
			//In order to avoid data duplication when the solr document is saved
			for(SolrDocument solrDoc : docList){
				SolrInputDocument inDoc = ClientUtils.toSolrInputDocument(solrDoc);
				for(CopyField cf : CopyField.values()){
					inDoc.removeField(cf.getVal());
				}
				inputList.add(inDoc);
			}
			writeSolrServer.add(inputList);
			Logger.getLogger(IndexFixer.class).info("Finished " + inputList.size() +" documents");
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Europeana ESE SOLR copyfields
	 *
	 */
	private enum CopyField{
		WHAT("what"),SKOS_PREFLABEL("skos_prefLabel"),SKOS_ALTLABEL("skos_altLabel"),WHEN("when"),WHO("who"),WHERE("where"),CREATOR("creator"),
		DATE("date"),DESCRIPTION("description"),IDENTIFIER("identifier"),LOCATION("location"),SUBJECT("subject"),SOURCE("source"),
		TITLE("title"),FORMAT("format"),RELATION("relation"),TYPE("TYPE"),USERTAGS("USERTAGS"),LANGUAGE("LANGUAGE"),COUNTRY("COUNTRY"),
		YEAR("YEAR"),PROVIDER("PROVIDER"),DATA_PROVIDER("DATA_PROVIDER"),RIGHTS("RIGHTS"),COMPLETENESS("COMPLETENESS"),UGC("UGC");
		String val;
		private CopyField(String val){
			this.val=val;
		}
		public String getVal(){
			return this.val;
		}
	}
}

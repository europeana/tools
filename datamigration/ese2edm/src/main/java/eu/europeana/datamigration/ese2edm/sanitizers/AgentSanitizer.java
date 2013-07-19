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
package eu.europeana.datamigration.ese2edm.sanitizers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.utils.MongoDatabaseUtils;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.datamigration.ese2edm.server.SolrServer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;
/**
 * Fix edm:agent contextual entities
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class AgentSanitizer implements Sanitizer{

	/**
	 * Sanitization method. It fixes the values of agents in SOLR and Mongo by appending the missing skos:prefLabel
	 */
	public void sanitize() {
		try {
			EdmMongoServer mongoServer = new EdmMongoServerImpl(new Mongo(
					PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()), PropertyUtils.getEuropeanaDB(), "", "");
			SolrServer solrServer = new SolrServer();
			solrServer
					.createWriteSolrServer(PropertyUtils.getWriteServerUrl());
			//Find all agents that are stored in  the DB
			List<AgentImpl> agents = mongoServer.getDatastore()
					.find(AgentImpl.class).asList();
			int k = 0;
			for (AgentImpl agent : agents) {
				//Initiate the connection to the AnnocultorDB
				MongoDatabaseUtils.dbExists();
				TermList terms = MongoDatabaseUtils.findByCode(new CodeURI(
						agent.getAbout()), "people");
				if (!terms.isEmpty()) {
					Iterator<Term> iterator = terms.iterator();
					UpdateOperations<AgentImpl> ops = mongoServer
							.getDatastore().createUpdateOperations(
									AgentImpl.class);
					Query<AgentImpl> query = mongoServer.getDatastore()
							.find(AgentImpl.class)
							.filter("about", agent.getAbout());
					Map<String, List<String>> prefLabel = new HashMap<String, List<String>>();
					if (agent.getPrefLabel() != null) {
						prefLabel = agent.getPrefLabel();
					}
					while (iterator.hasNext()) {
						Term term = iterator.next();
						List<String> values = new ArrayList<String>();
						String lang = term.getLang() != null ? term.getLang()
								.toString() : "def";
						if (prefLabel.containsKey(lang)) {
							values = prefLabel.get(lang.toString());
						}
						if (!values.contains(term.getLabel())) {
							values.add(term.getLabel());
						}
						prefLabel.put(lang, values);
					}
					ops.set("prefLabel", prefLabel);
					//Fix the skos:prefLabel of the edm:Agent in Mongo
					mongoServer.getDatastore().update(query, ops);
					Logger.getLogger("AgentSanitizer").log(Level.INFO,
							"Modified Agent " + k + " in Mongo");
					ModifiableSolrParams params = new ModifiableSolrParams();
					params.set("q", EdmLabel.EDM_AGENT.toString() + ":"
							+ QueryParser.escape(agent.getAbout()));
					params.set("rows", 0);
					long numFound = solrServer.query(params).getResults()
							.getNumFound();
					ModifiableSolrParams paramsNew = new ModifiableSolrParams();
					paramsNew.set("q", EdmLabel.EDM_AGENT.toString() + ":"
							+ QueryParser.escape(agent.getAbout()));
					paramsNew.set("rows", (int) numFound);
					paramsNew.set("start", 0);
					SolrDocumentList lst = solrServer.query(paramsNew)
							.getResults();
					List<SolrInputDocument> inputList = new ArrayList<SolrInputDocument>();
					//Fix them in SOLR 
					for (SolrDocument doc : lst) {
						SolrInputDocument inputDocument = ClientUtils
								.toSolrInputDocument(doc);
						for (Entry<String, List<String>> entry : prefLabel
								.entrySet()) {
							inputDocument.addField(
									EdmLabel.AG_SKOS_PREF_LABEL.toString()
											+ "." + entry.getKey(), entry
											.getValue().toArray());
						}
						inputList.add(inputDocument);

					}
					if (inputList.size() > 0) {
						solrServer.add(inputList);
					}
				}

				Logger.getLogger("AgentSanitizer").log(Level.INFO,
						"Modified Agent " + k + " in Solr");
				k++;
			}
		} catch (MongoDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoException.DuplicateKey;

import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.definitions.solr.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.solr.entity.WebResource;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.datamigration.ese2edm.converters.generic.RecordRemover;

/**
 * Class to write EDM records to SOLR and Mongo
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class EDMWriter {
	private static Logger log;
	private SolrServer solrServer;
	private EdmMongoServer edmMongoServer;
	static {
		FileHandler hand;
		try {
			if (new File("europeana.log").exists()) {
				new File("europeana.log").renameTo(new File("europeana.log."
						+ new Date()));
			}
			hand = new FileHandler("europeana.log");
			log = Logger.getLogger("log_file");
			log.addHandler(hand);
		} catch (SecurityException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Add a list of documents in SOLR
	 * 
	 * @param documents
	 */
	public void writeEDMDocumentsInSolr(List<SolrInputDocument> documents) {
		try {
			solrServer.add(documents);
		} catch (SolrServerException e) {
			for (SolrInputDocument solrDocument : documents) {
				addOneDocument(solrDocument);

			}
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Method to generate XML solr documents from SolrInputDocuments
	 * 
	 * @param documents
	 *            The documents to convert to XML files
	 */
	public void writeToXML(List<SolrInputDocument> documents) {
		for (SolrInputDocument doc : documents) {
			String docXML = ClientUtils.toXML(doc);

			File f = new File("doc"
					+ doc.getFieldValue(EdmLabel.EUROPEANA_ID.toString())
					+ ".xml");
			try {
				FileUtils.write(f, docXML);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// In case something goes wrong index record by record
	private void addOneDocument(SolrInputDocument solrDocument) {
		try {
			solrServer.add(solrDocument);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Save a list of documents in Mongo
	 * 
	 * @param fullBeans
	 * @throws IOException
	 * @throws SecurityException
	 */
	public boolean writeEDMDocumentInMongo2(List<FullBeanImpl> fullBeans, boolean shouldRemove)
			throws DuplicateKey, SecurityException, IOException {
		String s="";
		RecordRemover recordRemover = new RecordRemover();
		try {
			for (FullBeanImpl fullBean : fullBeans) {
				s = fullBean.getAbout();
				try {
					if(shouldRemove){
						recordRemover.clearData(s, edmMongoServer);
					}
					fullBean = saveEntities(fullBean);
					// FullBeanImpl retrievedFullBean = edmMongoServer
					// .searchByAbout(FullBeanImpl.class,
					// fullBean.getAbout());
					// if (retrievedFullBean == null) {
					// edmMongoServer.getDatastore().save(fullBean);
					// } else {
					// // log.log(Level.SEVERE, "Record " + fullBean.getAbout()
					// // + " was encountered twice.\n");
					// }
					edmMongoServer.getDatastore().save(fullBean);
//					edmMongoServer.getDatastore().updateFirst(
//							edmMongoServer.getDatastore()
//									.createQuery(FullBeanImpl.class)
//									.filter("about", fullBean.getAbout()),
//							fullBean, true);
				} catch (MongoException.DuplicateKey e) {
					e.printStackTrace();
					log.log(Level.SEVERE, "Key " + fullBean.getAbout()
							+ " was encountered twice.\n");
					return true;
				} 
			}
			return false;
		} catch (Exception e) {
			recordRemover.clearData(s, edmMongoServer);
			e.printStackTrace();
			return true;
		}
	}

	public boolean writeEDMDocumentInMongo(List<FullBeanImpl> fullBeans, boolean shouldRemove)
			throws DuplicateKey, SecurityException, IOException {
		String s="";
		RecordRemover recordRemover = new RecordRemover();
		try {
			
			if(shouldRemove){
				for(FullBeanImpl fullBean: fullBeans){
					s = fullBean.getAbout();
					recordRemover.clearData(s, edmMongoServer);
				}
				
			}
			saveEntities(fullBeans);
			
			edmMongoServer.getDatastore().save(fullBeans);
			return false;
		} catch (Exception e){
			e.printStackTrace();
			for(FullBeanImpl fullBean: fullBeans){
				s = fullBean.getAbout();
				recordRemover.clearData(s, edmMongoServer);
			}
			return writeEDMDocumentInMongo2(fullBeans, false);
		}
		
	}
	
	private List<FullBeanImpl> saveEntities(List<FullBeanImpl> fullBeans) {
		List<FullBeanImpl> fullBeansModified = new ArrayList<FullBeanImpl>();
		List<AggregationImpl> aggregations = new ArrayList<AggregationImpl>();
		List<EuropeanaAggregation> eAggregations = new ArrayList<EuropeanaAggregation>();
		List<ProxyImpl> proxies = new ArrayList<ProxyImpl>();
		List<ProvidedCHOImpl> providedChos = new ArrayList<ProvidedCHOImpl>();
		List<WebResource> wResources = new ArrayList<WebResource>();

		
		for(FullBeanImpl fullBean:fullBeans){
			aggregations.addAll(fullBean.getAggregations());
			eAggregations.add(fullBean.getEuropeanaAggregation());
			proxies.addAll(fullBean.getProxies());
			providedChos.addAll(fullBean.getProvidedCHOs());
			for(AggregationImpl aggr : fullBean.getAggregations()){
				wResources.addAll(aggr.getWebResources());
			}
			if(fullBean.getEuropeanaAggregation().getWebResources()!=null){
			wResources.addAll(fullBean.getEuropeanaAggregation().getWebResources());
			}
			
			List<PlaceImpl> places = new ArrayList<PlaceImpl>();
			List<AgentImpl> agents = new ArrayList<AgentImpl>();
			List<TimespanImpl> timespans = new ArrayList<TimespanImpl>();
			List<ConceptImpl> concepts = new ArrayList<ConceptImpl>();
			if (fullBean.getAgents() != null) {
				for (AgentImpl agent : fullBean.getAgents()) {
					AgentImpl retrievedAgent = edmMongoServer.searchByAbout(
							AgentImpl.class, agent.getAbout());
					if (retrievedAgent != null) {
						agents.add(retrievedAgent);
					} else {
						agent.setId(new ObjectId());
						//edmMongoServer.getDatastore().save(agent,WriteConcern.JOURNAL_SAFE);
						edmMongoServer.getDatastore().save(agent);
						agents.add(agent);
					}
				}
			}
			if (fullBean.getConcepts() != null) {
				for (ConceptImpl concept : fullBean.getConcepts()) {
					ConceptImpl retrievedConcept = edmMongoServer.searchByAbout(
							ConceptImpl.class, concept.getAbout());
					if (retrievedConcept != null) {
						concepts.add(retrievedConcept);
					} else {
						concept.setId(new ObjectId());
						//edmMongoServer.getDatastore().save(concept,WriteConcern.JOURNAL_SAFE);
						edmMongoServer.getDatastore().save(concept);
						concepts.add(concept);
					}
				}

			}
			if (fullBean.getTimespans() != null) {
				for (TimespanImpl timespan : fullBean.getTimespans()) {
					TimespanImpl retrievedTimespan = edmMongoServer.searchByAbout(
							TimespanImpl.class, timespan.getAbout());
					if (retrievedTimespan != null) {
						timespans.add(retrievedTimespan);
					} else {
						timespan.setId(new ObjectId());
						//edmMongoServer.getDatastore().save(timespan,WriteConcern.JOURNAL_SAFE);
						edmMongoServer.getDatastore().save(timespan);
						timespans.add(timespan);
					}
				}
			}
			if (fullBean.getPlaces() != null) {
				for (PlaceImpl place : fullBean.getPlaces()) {
					PlaceImpl retrievedPlace = edmMongoServer.searchByAbout(
							PlaceImpl.class, place.getAbout());
					if (retrievedPlace != null) {
						places.add(retrievedPlace);
					} else {
						place.setId(new ObjectId());
						//edmMongoServer.getDatastore().save(place,WriteConcern.JOURNAL_SAFE);
						edmMongoServer.getDatastore().save(place);
						places.add(place);
					}
				}
			}
			
			fullBean.setPlaces(places);
			fullBean.setAgents(agents);
			fullBean.setTimespans(timespans);
			fullBean.setConcepts(concepts);
			
			
			fullBeansModified.add(fullBean);
		}
		edmMongoServer.getDatastore().save(aggregations);
		edmMongoServer.getDatastore().save(proxies);
		edmMongoServer.getDatastore().save(providedChos);
		edmMongoServer.getDatastore().save(eAggregations);
		edmMongoServer.getDatastore().save(wResources);
		return fullBeansModified;
	}

	
	// Method that saves the entities in MongoDB
	public FullBeanImpl  saveEntities(FullBeanImpl fullBean) {
		List<AgentImpl> agents = new ArrayList<AgentImpl>();
		List<ConceptImpl> concepts = new ArrayList<ConceptImpl>();
		List<TimespanImpl> timespans = new ArrayList<TimespanImpl>();
		List<PlaceImpl> places = new ArrayList<PlaceImpl>();
		List<AggregationImpl> aggregations = new ArrayList<AggregationImpl>();
		EuropeanaAggregationImpl europeanaAggregation = (EuropeanaAggregationImpl) fullBean
				.getEuropeanaAggregation();
		List<ProxyImpl> proxies = new ArrayList<ProxyImpl>();
		List<ProvidedCHOImpl> providedCHOs = new ArrayList<ProvidedCHOImpl>();
		List<WebResource> webResources = new ArrayList<WebResource>();
		if (fullBean.getAgents() != null) {
			for (AgentImpl agent : fullBean.getAgents()) {
				AgentImpl retrievedAgent = edmMongoServer.searchByAbout(
						AgentImpl.class, agent.getAbout());
				if (retrievedAgent != null) {
					agents.add(retrievedAgent);
				} else {
					agent.setId(new ObjectId());
					//edmMongoServer.getDatastore().save(agent,WriteConcern.JOURNAL_SAFE);
					edmMongoServer.getDatastore().save(agent);
					agents.add(agent);
				}
			}
		}
		if (fullBean.getConcepts() != null) {
			for (ConceptImpl concept : fullBean.getConcepts()) {
				ConceptImpl retrievedConcept = edmMongoServer.searchByAbout(
						ConceptImpl.class, concept.getAbout());
				if (retrievedConcept != null) {
					concepts.add(retrievedConcept);
				} else {
					concept.setId(new ObjectId());
					//edmMongoServer.getDatastore().save(concept,WriteConcern.JOURNAL_SAFE);
					edmMongoServer.getDatastore().save(concept);
					concepts.add(concept);
				}
			}

		}
		if (fullBean.getTimespans() != null) {
			for (TimespanImpl timespan : fullBean.getTimespans()) {
				TimespanImpl retrievedTimespan = edmMongoServer.searchByAbout(
						TimespanImpl.class, timespan.getAbout());
				if (retrievedTimespan != null) {
					timespans.add(retrievedTimespan);
				} else {
					timespan.setId(new ObjectId());
					//edmMongoServer.getDatastore().save(timespan,WriteConcern.JOURNAL_SAFE);
					edmMongoServer.getDatastore().save(timespan);
					timespans.add(timespan);
				}
			}
		}
		if (fullBean.getPlaces() != null) {
			for (PlaceImpl place : fullBean.getPlaces()) {
				PlaceImpl retrievedPlace = edmMongoServer.searchByAbout(
						PlaceImpl.class, place.getAbout());
				if (retrievedPlace != null) {
					places.add(retrievedPlace);
				} else {
					place.setId(new ObjectId());
					//edmMongoServer.getDatastore().save(place,WriteConcern.JOURNAL_SAFE);
					edmMongoServer.getDatastore().save(place);
					places.add(place);
				}
			}
		}
		if (fullBean.getAggregations() != null) {
			for (AggregationImpl aggregation : fullBean.getAggregations()) {
				AggregationImpl retrievedFullBean = edmMongoServer
						.searchByAbout(AggregationImpl.class,
								aggregation.getAbout());
				if (retrievedFullBean == null) {

					aggregation.setId(new ObjectId());
					for (WebResource wr : aggregation.getWebResources()) {
						webResources.add(wr);
					}
					//edmMongoServer.getDatastore().save(aggregation,WriteConcern.JOURNAL_SAFE);
					edmMongoServer.getDatastore().save(aggregation);
					aggregations.add(aggregation);
				} else{
					aggregations.add(retrievedFullBean);
				}
					
			}

		}

		if (fullBean.getProxies() != null) {
			for (ProxyImpl proxy : fullBean.getProxies()) {
				ProxyImpl retrievedFullBean = edmMongoServer.searchByAbout(
						ProxyImpl.class, proxy.getAbout());
				if (retrievedFullBean == null) {
					proxy.setId(new ObjectId());
					//edmMongoServer.getDatastore().save(proxy,WriteConcern.JOURNAL_SAFE);
					edmMongoServer.getDatastore().save(proxy);
					proxies.add(proxy);
				}
				else {
					proxies.add(retrievedFullBean);
				}
			}
		}
		if (fullBean.getProvidedCHOs() != null) {
			ProvidedCHOImpl retrievedFullBean = edmMongoServer.searchByAbout(
					ProvidedCHOImpl.class, fullBean.getProvidedCHOs().get(0)
							.getAbout());
			if (retrievedFullBean == null) {
				ProvidedCHOImpl providedCHO = fullBean.getProvidedCHOs().get(0);
				providedCHO.setId(new ObjectId());
				//edmMongoServer.getDatastore().save(providedCHO,WriteConcern.JOURNAL_SAFE);
				edmMongoServer.getDatastore().save(providedCHO);
				providedCHOs.add(providedCHO);
			}
			else {
				providedCHOs.add(retrievedFullBean);
			}
		}

		FullBeanImpl retrievedFullBean = edmMongoServer.searchByAbout(
				FullBeanImpl.class, fullBean.getAbout());
		if (retrievedFullBean == null) {
			europeanaAggregation.setId(new ObjectId());
			if (europeanaAggregation.getWebResources() != null) {
				for (WebResource wr : europeanaAggregation.getWebResources()) {
					webResources.add(wr);
				}
			}
			europeanaAggregation.setAbout(fullBean.getAbout());
			EuropeanaAggregationImpl eAggregation = edmMongoServer
					.searchByAbout(EuropeanaAggregationImpl.class,
							europeanaAggregation.getAbout());
			
			if (eAggregation != null) {
				if(eAggregation.getAggregatedCHO()==null){
					eAggregation.setAggregatedCHO(fullBean.getAbout());
				}
				europeanaAggregation = eAggregation;
			} else {
				//edmMongoServer.getDatastore().save(europeanaAggregation,WriteConcern.JOURNAL_SAFE);
				edmMongoServer.getDatastore().save(europeanaAggregation);
			}
		}
		edmMongoServer.getDatastore().save(webResources);
		fullBean.setAggregations(aggregations);
		fullBean.setPlaces(places);
		fullBean.setTimespans(timespans);
		fullBean.setEuropeanaAggregation(europeanaAggregation);
		fullBean.setProxies(proxies);
		fullBean.setAgents(agents);
		fullBean.setConcepts(concepts);
		fullBean.setProvidedCHOs(providedCHOs);
		return fullBean;
	}

	/**
	 * Close the connection to Mongo Server
	 */
	public void close() {
		edmMongoServer.close();
	}

	/**
	 * Commit and optimize the Solr Index
	 */
	public void optimize() {
		try {
			solrServer.commit();
			solrServer.optimize();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Constructor for EDM writer
	 * 
	 * @param solrServer
	 *            Solr server to save to
	 * @param edmMongoServer
	 *            Mongo Server to save to
	 */
	public EDMWriter(SolrServer solrServer, EdmMongoServer edmMongoServer) {
		this.solrServer = solrServer;
		this.edmMongoServer = edmMongoServer;
	}
}

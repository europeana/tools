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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
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
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.datamigration.ese2edm.server.SolrServer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

/**
 * Sanitizer class for edm:Place
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class PlaceSanitizer implements Sanitizer {

	/**
	 * Sanitization method for places adding the lat,long pair where missing
	 */
	public void sanitize() {
		try {
			EdmMongoServer mongoServer = new EdmMongoServerImpl(new Mongo(
					PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()), PropertyUtils.getEuropeanaDB(), "", "");
			SolrServer solrServer = new SolrServer();
			solrServer
					.createWriteSolrServer(PropertyUtils.getWriteServerUrl());
			List<PlaceImpl> places = mongoServer.getDatastore()
					.find(PlaceImpl.class).asList();
			Map<String, PlaceImpl> placeMap = new TreeMap<String, PlaceImpl>();
			for (PlaceImpl place : places) {
				if (place.getPrefLabel() != null) {
					String about = null;
					if (place.getPrefLabel().get("def") != null) {
						about = place.getPrefLabel().get("def").get(0);
					} else if (place.getPrefLabel().get("en") != null) {
						about = place.getPrefLabel().get("en").get(0);
					}
					if (about != null) {
						placeMap.put(about, place);
					}
				}
			}
			int k = 0;
			MongoDatabaseUtils.dbExists("localhost",27017);
			for (Entry<String, PlaceImpl> place : placeMap.entrySet()) {

				Query<PlaceImpl> query = mongoServer.getDatastore()
						.find(PlaceImpl.class)
						.filter("about", place.getValue().getAbout());
				Logger.getLogger("PlaceSanitizer")
						.log(Level.INFO,
								"Before the check "
										+ place.getValue().getPrefLabel() != null ? place
										.getValue().getPrefLabel().toString()
										: place.getValue().getAbout());

				if (place.getValue().getAbout().startsWith("http")) {
					TermList terms = MongoDatabaseUtils.findByCode(new CodeURI(
							place.getValue().getAbout()), "place");
					if (terms != null && !terms.isEmpty()) {
						Logger.getLogger("PlaceSanitizer")
								.log(Level.INFO,
										"The place value is "
												+ place.getValue()
														.getPrefLabel() != null ? place
												.getValue().getPrefLabel()
												.toString()
												: place.getValue().getAbout());
						Iterator<Term> iterator = terms.iterator();
						UpdateOperations<PlaceImpl> ops = mongoServer
								.getDatastore().createUpdateOperations(
										PlaceImpl.class);

						String latitude = null;
						String longitude = null;
						while (iterator.hasNext()) {
							Term term = iterator.next();
							if (!StringUtils.endsWith(
									term.getProperty("division"), "A.PCLI")) {
								latitude = term.getProperty("latitude");
								longitude = term.getProperty("longitude");
							}
						}
						if (latitude != null && longitude != null
								&& Float.parseFloat(latitude) != 0
								&& Float.parseFloat(longitude) != 0) {

							ModifiableSolrParams params = new ModifiableSolrParams();
							params.set(
									"q",
									EdmLabel.EDM_PLACE.toString()
											+ ":"
											+ QueryParser.escape(place
													.getValue().getAbout()));
							params.set("rows", 0);
							long numFound = solrServer.query(params)
									.getResults().getNumFound();
							Logger.getLogger("PlaceSanitizer").log(
									Level.INFO,
									"Number of records referencing place "
											+ numFound);
							ModifiableSolrParams paramsNew = new ModifiableSolrParams();
							paramsNew.set(
									"q",
									EdmLabel.EDM_PLACE.toString()
											+ ":"
											+ QueryParser.escape(place
													.getValue().getAbout()));
							int i = 0;
							while (i < numFound) {
								paramsNew.set("rows", 1000);
								paramsNew.set("start", i);
								SolrDocumentList lst = solrServer.query(
										paramsNew).getResults();
								List<SolrInputDocument> inputList = new ArrayList<SolrInputDocument>();

								for (SolrDocument doc : lst) {
									SolrInputDocument inputDocument = ClientUtils
											.toSolrInputDocument(doc);

									inputDocument.addField(
											EdmLabel.PL_WGS84_POS_LAT
													.toString(), latitude);
									inputDocument.addField(
											EdmLabel.PL_WGS84_POS_LONG
													.toString(), longitude);
									inputList.add(inputDocument);
								}

								if (inputList.size() > 0) {
									solrServer.add(inputList);
								}
								Logger.getLogger("PlaceSanitizer").log(
										Level.INFO,
										"Imported " + inputList.size()
												+ " records");
								i += 1000;
							}
						}
						Logger.getLogger("PlaceSanitizer").log(Level.INFO,
								"Added Place in Solr " + k);
					}
				}
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

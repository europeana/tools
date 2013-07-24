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
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.impl.EuropeanaIdMongoServerImpl;
import eu.europeana.corelib.tools.utils.EuropeanaUriUtils;
import eu.europeana.datamigration.ese2edm.converters.generic.GenericEse2EdmConverter;
import eu.europeana.datamigration.ese2edm.enrichment.EuropeanaTagger;
import eu.europeana.datamigration.ese2edm.exception.EntityNotFoundException;
import eu.europeana.datamigration.ese2edm.server.SolrServer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

/**
 * Collection-based converter for ESE records to EDM
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class Ese2EdmSolrCollectionChecker implements Runnable {
	private final EuropeanaTagger tagger = new EuropeanaTagger();
	private static final String COLLECTION_FIELD = "europeana_collectionName:";
	private final static String EUROPEANA_ISSHOWNAT = "europeana_isShownAt";
	private final static String EUROPEANA_ISSHOWNBY = "europeana_isShownBy";
	private final static String EUROPEANA_OBJECT = "europeana_object";
	private final static String EUROPEANA_URI = "europeana_uri";
	private final static String EUROPEANA_RECORD = "http://www.europeana.eu/resolve/record/";
	private final static String EUROPEANA_COLLECTIONNAME = "europeana_collectionName";
	CollectionMongoServer collectionMongoServer;
	EuropeanaIdMongoServer europeanaIdMongoServer;
	static EdmMongoServer mongoServer;
	private List<SolrInputDocument> solrList = new ArrayList<SolrInputDocument>();
	private List<FullBeanImpl> mongoList = new ArrayList<FullBeanImpl>();
	private List<String> collections;
	private List<Integer> starts;
	private List<Integer> ends;
	private String threadName;
	private boolean createRDF;
	private List<Integer> diff;
	// Initializer for Annocultor
	{
		try {
			tagger.init("Europeana");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	static {
		try {
			if (mongoServer == null) {
				mongoServer = new EdmMongoServerImpl(new Mongo(
						PropertyUtils.getMongoServer(),
						PropertyUtils.getMongoPort()),
						PropertyUtils.getEuropeanaDB(), "", "");
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
		}
	}

	/**
	 * Returns the list of range starts for this thread
	 * 
	 * @return the list of range starts for this thread
	 */
	public List<Integer> getStarts() {
		return starts;
	}

	/**
	 * Sets the list of range starts for this thread
	 * 
	 * @param starts
	 */
	public void setStarts(List<Integer> starts) {
		this.starts = starts;
	}

	/**
	 * Returns the list of range ends for this thread
	 * 
	 * @return
	 */
	public List<Integer> getEnds() {
		return ends;
	}

	/**
	 * Sets the list of range ends for this thread
	 * 
	 * @param ends
	 */
	public void setEnds(List<Integer> ends) {
		this.ends = ends;
	}

	/**
	 * Sets the collections to index
	 * 
	 * @param collections
	 */
	public void setCollections(List<String> collections) {
		this.collections = collections;
	}

	public void setThreadName(int threadName) {
		this.threadName = "Thread-" + threadName;
	}

	public void setCreateRDF(boolean createRdf) {
		this.createRDF = createRdf;
	}

	public void setDiff(List<Integer> diff) {
		this.diff = diff;
	}

	/**
	 * Method that converts ESE Records to EDM in chunks of 300s
	 * 
	 * @throws MongoDBException
	 * @throws MongoException
	 * @throws SolrServerException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 *             * @throws EntityNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	public void convert() throws MongoDBException, MongoException,
			SolrServerException, SecurityException, IllegalArgumentException,
			EntityNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, IOException {
		String resume = null;
		if (!new File("./logs/").exists()) {
			FileUtils.forceMkdir(new File("./logs/"));
		} else {
			if (new File("./logs/" + this.getClass().getSimpleName() + "-"
					+ this.threadName).exists()) {
				resume = FileUtils.readFileToString(new File("./logs/"
						+ this.getClass().getSimpleName() + "-"
						+ this.threadName));
			}
		}

		SolrServer writeServer = new SolrServer();
		// collectionMongoServer = new CollectionMongoServer(new Mongo(
		// "192.168.34.54",27017), "collections");
		// europeanaIdMongoServer = new EuropeanaIdMongoServer(new Mongo(
		// "192.168.34.54", 27017), "EuropeanaId");
		collectionMongoServer = new CollectionMongoServerImpl(new Mongo(
				PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()),
				PropertyUtils.getCollectionDB());
		europeanaIdMongoServer = new EuropeanaIdMongoServerImpl(new Mongo(
				PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()),
				PropertyUtils.getEuropeanaIdDB(),"","");
		// EdmMongoServer mongoServer = new EdmMongoServerImpl(new Mongo(
		// "192.168.34.54", 27017), "europeana", "", "");
		// instantiateMongoServer();
		// writeServer.createWriteSolrServer("http://192.168.34.54:9595/solr/search");
		writeServer.createWriteSolrServer(PropertyUtils.getWriteServerUrl());
		int k = 0;
		int resumeIndex = 0;
		int from = 0;
		boolean startIsSet = false;
		if (resume != null) {
			resumeIndex = Integer.parseInt(resume.split(" ")[0]);
			from = Integer.parseInt(resume.split(" ")[1]);
			startIsSet = true;
		}
		for (String collection : collections) {
			int i = 0;
			if (!startIsSet) {
				from = starts.get(k);
			}
			System.out.println("from1 " + from);
			int totals = ends.get(k);

			int index = 0;
			int difference = diff.get(k);
			if (resumeIndex == k) {

				System.out.println("Starting from collection "
						+ collections.get(k) + " from " + from);
				if (difference > 0) {
					while (i < 9) {
						// HttpSolrServer readServer = new HttpSolrServer(
						// "http://192.168.34.54:9494/solr/search" + i);
						HttpSolrServer readServer = new HttpSolrServer(
								PropertyUtils.getReadServerUrl() + i);
						ESEReader reader = new ESEReader(readServer);
						long max = reader.getMax(COLLECTION_FIELD + collection
								+ "*");

						int inc = 300;

						if (from < max) {
							long lim = (totals > max) == true ? max : totals;
							while (from < lim) {
								SolrDocumentList solrDocumentList = reader
										.readCollection(COLLECTION_FIELD
												+ collection + "*", from);
								EDMWriter edmWriter = new EDMWriter(
										writeServer, mongoServer);
								SolrDocumentList diffDocList = new SolrDocumentList();

								for (SolrDocument doc : solrDocumentList) {
									String oldCollectionId = StringUtils
											.split(((ArrayList<String>) doc
													.get(EUROPEANA_COLLECTIONNAME))
													.get(0), "_")[0];
									String newCollectionId = collectionMongoServer
											.findNewCollectionId(oldCollectionId);

									String uri = (String) doc
											.getFieldValue(EUROPEANA_URI);
									String strippedURI = StringUtils.replace(
											uri, EUROPEANA_RECORD, "");
									String id = "";
									String hash = StringUtils.split(
											strippedURI, "/")[1];
									String collId = oldCollectionId;
									if (newCollectionId != null) {
										id = StringUtils.replace(strippedURI,
												oldCollectionId,
												newCollectionId);
										collId = newCollectionId;
									} else {
										// Create the europeanaID
										id = EuropeanaUriUtils
												.createEuropeanaId(
														oldCollectionId, hash);
									}
									ModifiableSolrParams params = new ModifiableSolrParams();
									String idNew = ClientUtils
											.escapeQueryChars(id);
									params.add("q", "europeana_collectionName:"
											+ collId + "_*");
									params.add("fq", "europeana_id:" + idNew);
									params.add("rows", "0");
									SolrDocumentList results = writeServer
											.query(params).getResults();
									if (results.getNumFound() == 0) {
										diffDocList.add(doc);
										difference--;
									}
									//System.out.println(results);
								}
								if (diffDocList.size() > 0) {
									solrList.addAll(new GenericEse2EdmConverter().convertEse2EdmSolr(diffDocList,collectionMongoServer,europeanaIdMongoServer,tagger,mongoList,createRDF));
									boolean gotException = true;

									int attempts = 0;
									if (diffDocList.size() > 0) {
										while (gotException) {

											if (attempts > 0) {

												// try {
												// //instantiateMongoServer();
												// Thread.sleep(2000);
												// } catch (InterruptedException
												// e) {
												// // TODO Auto-generated catch
												// block
												// e.printStackTrace();
												// }
											}
											gotException = edmWriter
													.writeEDMDocumentInMongo(
															mongoList, false);
											edmWriter
													.writeEDMDocumentsInSolr(solrList);
											attempts++;
										}
									}
								}
								from += inc == solrDocumentList.size() ? inc
										: solrDocumentList.size();
								index += inc == solrDocumentList.size() ? inc
										: solrDocumentList.size();
								solrList.clear();
								mongoList.clear();
								FileUtils.write(new File("./logs/"
										+ this.getClass().getSimpleName() + "-"
										+ threadName), k + " " + (index));

								System.out.println(Calendar.getInstance()
										.getTime()
										+ " Added "
										+ from
										+ " documents in collection "
										+ collection);
							}

						}

						from -= max;
						index = (int) max;
						totals -= max;
						System.out.println("from2" + from);
						if (max > 0) {
							System.out.println("Finished collection "
									+ collection + " in server "
									+ PropertyUtils.getReadServerUrl() + i);
						}
						i++;
					}
				}
			}
			k++;
			resumeIndex = k;
		}
		System.out.println("finished");
	}

	
	


	@Override
	public void run() {
		try {
			convert();
		} catch (MongoDBException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// public static void instantiateMongoServer() {
	//
	// }

}

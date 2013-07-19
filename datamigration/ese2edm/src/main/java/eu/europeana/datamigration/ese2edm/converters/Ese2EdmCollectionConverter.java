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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.annocultor.converters.europeana.Entity;
import eu.europeana.corelib.definitions.solr.entity.Aggregation;
import eu.europeana.corelib.definitions.solr.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.solr.entity.Proxy;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.utils.EDMUtils;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.datamigration.ese2edm.converters.generic.EntityMerger;
import eu.europeana.datamigration.ese2edm.converters.generic.FieldCreator;
import eu.europeana.datamigration.ese2edm.converters.generic.GenericEse2EdmConverter;
import eu.europeana.datamigration.ese2edm.enrichment.EuropeanaTagger;
import eu.europeana.datamigration.ese2edm.enums.FieldMapping;
import eu.europeana.datamigration.ese2edm.exception.EntityNotFoundException;
import eu.europeana.datamigration.ese2edm.exception.MultipleUniqueFieldsException;
import eu.europeana.datamigration.ese2edm.server.SolrServer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

/**
 * Collection-based converter for ESE records to EDM
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class Ese2EdmCollectionConverter implements Runnable {
	private final EuropeanaTagger tagger = new EuropeanaTagger();
	private static final String COLLECTION_FIELD = "europeana_collectionName:";
	
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
	private boolean shouldDelete;
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
			if (mongoServer==null) {
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

	public void setShouldDelete(boolean shouldDelete){
		this.shouldDelete = shouldDelete;
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
		collectionMongoServer = new CollectionMongoServer(new Mongo(
				PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()),
				PropertyUtils.getCollectionDB());
		europeanaIdMongoServer = new EuropeanaIdMongoServer(new Mongo(
				PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()),
				PropertyUtils.getEuropeanaIdDB());
		europeanaIdMongoServer.createDatastore();
		// EdmMongoServer mongoServer = new EdmMongoServerImpl(new Mongo(
		// "192.168.34.54", 27017), "europeana", "", "");
		//instantiateMongoServer();
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
			System.out.println("from1" + from);
			int totals = ends.get(k);

			int index = 0;

			if (resumeIndex == k) {

				System.out.println("Starting from collection "
						+ collections.get(k) + " from " + from);
				while (i < 9) {
					// HttpSolrServer readServer = new HttpSolrServer(
					// "http://192.168.34.54:9494/solr/search" + i);
					HttpSolrServer readServer = new HttpSolrServer(
							PropertyUtils.getReadServerUrl() + i);
					ESEReader reader = new ESEReader(readServer);
					long max = reader.getMax(COLLECTION_FIELD + collection +"*");

					int inc = 300;

					if (from < max) {
						long lim = (totals > max) == true ? max : totals;
						while (from < lim) {
							SolrDocumentList solrDocumentList = reader
									.readCollection(COLLECTION_FIELD
											+ collection+"*", from);
							EDMWriter edmWriter = new EDMWriter(writeServer,
									mongoServer);
							solrList.addAll(new GenericEse2EdmConverter().convertEse2EdmSolr(solrDocumentList,collectionMongoServer,europeanaIdMongoServer,tagger,mongoList,createRDF));
							boolean gotException = true;
							boolean shouldDelete=this.shouldDelete;
							int attempts = 0;
							while (gotException) {
								
								if (attempts > 0) {
									shouldDelete=true;
//									try {
//										//instantiateMongoServer();
//										Thread.sleep(2000);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
								}
								gotException = edmWriter
										.writeEDMDocumentInMongo(mongoList,shouldDelete);
								edmWriter.writeEDMDocumentsInSolr(solrList);
								attempts++;
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

							System.out.println(Calendar.getInstance().getTime()
									+ " Added " + from
									+ " documents in collection " + collection);
						}
					}
					from -= max;
					index = (int) max;
					totals -= max;
					System.out.println("from2" + from);
					if (max > 0) {
						System.out.println("Finished collection " + collection
								+ " in server "
								+ PropertyUtils.getReadServerUrl() + i);
					}
					i++;
				}
			}
			k++;
			resumeIndex = k;
		}
		System.out.println("finished");
	}

	

	// Add to edm:hasMet - to be removed
	// private void addToHasMetList(Proxy europeanaProxy, String value) {
	// Map<String, List<String>> hasMet = europeanaProxy.getEdmHasMet();
	// if (hasMet == null) {
	// hasMet = new HashMap<String, List<String>>();
	// List<String> hasMetList = new ArrayList<String>();
	// hasMet.put("def", hasMetList);
	// }
	//
	// List<String> hasMetList = hasMet.get("def");
	// hasMetList.add(value);
	// europeanaProxy.setEdmHasMet(hasMet);
	// }

	// Merge the different contextual entities to the FullBean
	

	// Generate EDM fields from their ESE counterparts


	

	

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

//	public static void instantiateMongoServer() {
//		
//	}

}

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
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.datamigration.ese2edm.converters.generic.GenericEse2EdmConverter;
import eu.europeana.datamigration.ese2edm.converters.generic.RecordRemover;
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
public class Ese2EdmRecordConverter implements Runnable {
	private final EuropeanaTagger tagger = new EuropeanaTagger();
	private static final String COLLECTION_FIELD = "europeana_collectionName:";
	private final static String EUROPEANA_URI = "europeana_uri";
	private final static String EUROPEANA_RECORD = "http://www.europeana.eu/resolve/record/";
	CollectionMongoServer collectionMongoServer;
	EuropeanaIdMongoServer europeanaIdMongoServer;
	EdmMongoServer mongoServer;
	private List<SolrInputDocument> solrList = new ArrayList<SolrInputDocument>();
	private List<FullBeanImpl> mongoList = new ArrayList<FullBeanImpl>();
	private List<String> collections;
	private List<Integer> starts;
	private List<Integer> ends;
	private String threadName;
	private int start;
	private int end;
	private boolean createRDF;
	// Initializer for Annocultor
	{
		try {
			tagger.init("Europeana");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setEnd(int end){
		this.end = end;
	}
	
	public void setStart(int start){
		this.start = start;
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
	public void convertCollection() throws MongoDBException, MongoException,
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
		instantiateMongoServer();
		// writeServer.createWriteSolrServer("http://192.168.34.54:9595/solr/search");
		writeServer.createWriteSolrServer(PropertyUtils.getWriteServerUrl());
		int k = 0;
		int resumeIndex = 0;
		int from = 0;
		// // boolean startIsSet = false;
		// if (resume != null) {
		// resumeIndex = Integer.parseInt(resume.split(" ")[0]);
		// from = Integer.parseInt(resume.split(" ")[1]);
		// startIsSet = true;
		// }
		for (String collection : collections) {
			int i = 0;

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
					long max = reader.getMax(COLLECTION_FIELD + collection
							+ "_*");

					int inc = 300;

					if (from < max) {
						// long lim = (totals > max) == true ? max : totals;
						while (from < max) {
							SolrDocumentList solrDocumentList = reader
									.readCollection(COLLECTION_FIELD
											+ collection + "_*", from);
							EDMWriter edmWriter = new EDMWriter(writeServer,
									mongoServer);
							SolrDocumentList newList = new SolrDocumentList();
							for (SolrDocument doc : solrDocumentList) {
								// DBObject query = new BasicDBObject();
								// query.put("about",
								// StringUtils.replace((String)doc.get(EUROPEANA_URI),
								// EUROPEANA_RECORD,
								// ""));
								// DBCursor curs =
								// mongoServer.getDatastore().getDB().getCollection("record").find(query);
								String record = StringUtils.replace(
										(String) doc.get(EUROPEANA_URI),
										EUROPEANA_RECORD, "");
								RecordRemover recordRemover = new RecordRemover();
								try {
									FullBean fBean = mongoServer
											.getFullBean("/" + record);
									if (fBean == null) {
										newList.add(doc);
									} else {
										if (fBean.getAggregations() == null) {
											recordRemover.clearData(record,mongoServer);
											newList.add(doc);
										}
									}
								} catch (Exception e) {
									// Remove referenced resources
									recordRemover.clearData(record,mongoServer);
									newList.add(doc);
								}

							}

							if (newList.size() > 0) {
								solrList.addAll(new GenericEse2EdmConverter().convertEse2EdmSolr(newList,collectionMongoServer,europeanaIdMongoServer,tagger,mongoList,createRDF));
								boolean gotException = true;
								int attempts = 0;
								while (gotException) {
									gotException = edmWriter
											.writeEDMDocumentInMongo(mongoList,true);
									edmWriter.writeEDMDocumentsInSolr(solrList);
									if (attempts > 0) {
										try {
											instantiateMongoServer();
											Thread.sleep(2000);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									attempts++;
								}
								newList = new SolrDocumentList();
							}
							from += inc == solrDocumentList.size() ? inc
									: solrDocumentList.size();
							index += inc == solrDocumentList.size() ? inc
									: solrDocumentList.size();
							solrList.clear();
							mongoList.clear();
							FileUtils.write(new File("./logs/"
									+ this.getClass().getSimpleName() + "-"
									+ threadName), k + " " + (from + index));

							System.out.println(Calendar.getInstance().getTime()
									+ " Added " + from
									+ " documents in collection " + collection);
						}
					}
					// from -= max;
					// index = (int) max;
					// totals -= max;
					// System.out.println("from2" + from);
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

	

	


	@Override
	public void run() {
		try {
			convert();
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

	private void convert() throws IOException, SolrServerException,
			MongoException, SecurityException, IllegalArgumentException,
			EntityNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

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
		// EdmMongoServer mongoServer = new EdmMongoServerImpl(new Mongo(
		// "192.168.34.54", 27017), "europeana", "", "");
		instantiateMongoServer();
		// writeServer.createWriteSolrServer("http://192.168.34.54:9595/solr/search");
		writeServer.createWriteSolrServer(PropertyUtils.getWriteServerUrl());
		int k = 0;
		
		int from = start;
		int to = end;
		if(resume!=null){
			from=Integer.parseInt(resume);
		}
		// // boolean startIsSet = false;
		// if (resume != null) {
		// resumeIndex = Integer.parseInt(resume.split(" ")[0]);
		// from = Integer.parseInt(resume.split(" ")[1]);
		// startIsSet = true;
		// }
		int i = from/3000000;
		int j = to/3000000+1;
		int recordsAdded=0;
		
		while (i < j) {
			// HttpSolrServer readServer = new HttpSolrServer(
			// "http://192.168.34.54:9494/solr/search" + i);
			HttpSolrServer readServer = new HttpSolrServer(
					PropertyUtils.getReadServerUrl() + (from / 3000000));
			ESEReader reader = new ESEReader(readServer);
			//long max = reader.getMax("*:*");

			int inc = 300;
			
			
				//long lim = (to > max) == true ? max : to;
				
				while (from < to) {
					SolrDocumentList solrDocumentList = reader.readCollection(
							"*:*", from-i*3000000);
					EDMWriter edmWriter = new EDMWriter(writeServer,
							mongoServer);
					SolrDocumentList newList = new SolrDocumentList();
					long start = new Date().getTime();
					for (SolrDocument doc : solrDocumentList) {
						// DBObject query = new BasicDBObject();
						// query.put("about",
						// StringUtils.replace((String)doc.get(EUROPEANA_URI),
						// EUROPEANA_RECORD,
						// ""));
						// DBCursor curs =
						// mongoServer.getDatastore().getDB().getCollection("record").find(query);
						String record = StringUtils.replace(
								(String) doc.get(EUROPEANA_URI),
								EUROPEANA_RECORD, "");
						RecordRemover recordRemover = new RecordRemover();
						try {
							FullBean fBean = mongoServer.getFullBean("/"
									+ record);
							if (fBean == null) {
								newList.add(doc);
								
							} else {
								if (fBean.getAggregations() == null||fBean.getProvidedCHOs()==null||fBean.getEuropeanaAggregation()==null||fBean.getProxies()==null) {
									recordRemover.clearData(record,mongoServer);
									newList.add(doc);
								}
							}
						} catch (Exception e) {
							// Remove referenced resources
							recordRemover.clearData(record,mongoServer);
							newList.add(doc);
						}

					}

					if (newList.size() > 0) {
						solrList.addAll(new GenericEse2EdmConverter().convertEse2EdmSolr(newList,collectionMongoServer,europeanaIdMongoServer,tagger,mongoList,createRDF));
						recordsAdded +=newList.size();
						boolean gotException = true;
						int attempts = 0;
						while (gotException) {
							gotException = edmWriter
									.writeEDMDocumentInMongo(mongoList,true);
							edmWriter.writeEDMDocumentsInSolr(solrList);
							if (attempts > 0) {
								
								//try {
									instantiateMongoServer();
									//Thread.sleep(2000);
							//	} catch (InterruptedException e) {
									// TODO Auto-generated catch block
							//		e.printStackTrace();
							//	}
							}
							attempts++;
						}
						newList = new SolrDocumentList();
					}
					from += inc == solrDocumentList.size() ? inc
							: solrDocumentList.size();

					solrList.clear();
					mongoList.clear();
					
					FileUtils.write(new File("./logs/"
							+ this.getClass().getSimpleName() + "-"
							+ threadName), Long.toString((from)));

					System.out.println(Calendar.getInstance().getTime()
							+ " Passed " + from + " documents in " +(new Date().getTime()-start) +" ms and added in total " +  recordsAdded +" records");
					if(from%3000000==0){
						i++;
					}
				}
			
			
			
		}

		System.out.println("finished");
	}

	public void instantiateMongoServer() {
		try {
			if (mongoServer == null
					|| !mongoServer.getDatastore().getMongo().getConnector()
							.isOpen()) {
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

}

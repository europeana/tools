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
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.solr.client.solrj.SolrServerException;
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
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.impl.EuropeanaIdMongoServerImpl;
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
 * Range-based converter for ESE records to EDM
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class Ese2EdmConverter implements Runnable {

	private long maxDocuments;
	private long from;
	private int index;
	private String model;
	private final static int DOCUMENTS_TO_READ = 300;
	private final static int DOCUMENTS_TO_WRITE = 300;
	private final static int SIZE = 25999999;
	private final static int CORE_SIZE = 3000000;
	List<SolrInputDocument> solrList;
	List<FullBeanImpl> mongoList;
	EDMWriter edmWriter;
	EdmMongoServer mongoServer;
	CollectionMongoServer collectionMongoServer;
	EuropeanaIdMongoServer europeanaIdMongoServer;
	private static Logger log;
	private final EuropeanaTagger tagger = new EuropeanaTagger();
	private String threadName;

	// Initializer for Annocultor
	{
		FileHandler hand;

		try {
			if (new File("EuropeanaId.log").exists()) {
				new File("EuropeanaId.log").renameTo(new File(
						"EuropeanaId.log." + new Date()));
			}

			hand = new FileHandler("EuropeanaId.log");
			log = Logger.getLogger("log_file");
			log.addHandler(hand);
			tagger.init("Europeana");
		} catch (SecurityException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method that converts ESE records to EDM
	 * 
	 * @throws MultipleUniqueFieldsException
	 * @throws EntityNotFoundException
	 * @throws SolrServerException
	 * @throws MongoDBException
	 * @throws IOException
	 */
	public void convert() throws MultipleUniqueFieldsException,
			EntityNotFoundException, SolrServerException, MongoDBException,
			IOException {
		SolrServer readServer = new SolrServer();
		SolrServer writeServer = new SolrServer();
		if (!new File("./logs/").exists()) {
			FileUtils.forceMkdir(new File("./logs/"));
		} else {
			if (new File("./logs/" + this.getClass().getSimpleName() + "-"
					+ this.threadName).exists()) {
				from = Integer.parseInt(FileUtils.readFileToString(new File(
						"./logs/" + this.getClass().getSimpleName() + "-"
								+ this.threadName)));
			}
		}
		try {
			// mongoServer = new EdmMongoServerImpl(
			// new Mongo("10.101.38.1", 27017), "europeana", "", "");
			collectionMongoServer = new CollectionMongoServerImpl(new Mongo(
					PropertyUtils.getMongoServer(),
					PropertyUtils.getMongoPort()),
					PropertyUtils.getCollectionDB());
			europeanaIdMongoServer = new EuropeanaIdMongoServerImpl(new Mongo(
					PropertyUtils.getMongoServer(),
					PropertyUtils.getMongoPort()),
					PropertyUtils.getEuropeanaIdDB(),"","");
			europeanaIdMongoServer.createDatastore();
			instantiateMongoServer();
			// writeServer.createWriteSolrServer("http://10.101.38.1:8282/solr/");
			writeServer
					.createWriteSolrServer(PropertyUtils.getWriteServerUrl());

			edmWriter = new EDMWriter(writeServer, mongoServer);
			solrList = new ArrayList<SolrInputDocument>();
			mongoList = new ArrayList<FullBeanImpl>();
			if (maxDocuments == 0) {
				maxDocuments = SIZE;
			}

			while (from < maxDocuments) {
				readServer.createReadSolrServer(PropertyUtils
						.getReadServerUrl() + (from / CORE_SIZE));
				ESEReader reader = new ESEReader(readServer);
				System.out.println("["
						+ DateFormatUtils.format(new Date().getTime(),
								"dd/MM/yy HH:mm:ss") + "] Start index " + from);
				SolrDocumentList solrDocumentList = reader.readEseDocuments(
						maxDocuments, from % CORE_SIZE, DOCUMENTS_TO_READ);
				solrList.addAll(new GenericEse2EdmConverter().convertEse2EdmSolr(solrDocumentList,collectionMongoServer,europeanaIdMongoServer,tagger,mongoList,false));

				from += DOCUMENTS_TO_READ;
				System.out.println("["
						+ DateFormatUtils.format(new Date().getTime(),
								"dd/MM/yy HH:mm:ss") + "] Read " + from
						+ " documents");

				if (solrList.size() == DOCUMENTS_TO_WRITE
						|| index + DOCUMENTS_TO_WRITE >= maxDocuments) {
					boolean gotException = true;
					int attempts = 0;
					while (gotException) {
						gotException = edmWriter
								.writeEDMDocumentInMongo(mongoList,false);
						edmWriter.writeEDMDocumentsInSolr(solrList);
						if (attempts > 0) {
							instantiateMongoServer();
							Thread.sleep(2000);
						}
						attempts++;
					}
					FileUtils.write(new File("./logs/"
							+ this.getClass().getSimpleName() + "-"
							+ this.threadName), "" + from);
					index += DOCUMENTS_TO_WRITE;
					System.out.println("["
							+ DateFormatUtils.format(new Date().getTime(),
									"dd/MM/yy HH:mm:ss") + "] Imported " + from
							+ " documents");
					solrList.clear();
					mongoList.clear();
				}

			}
			tagger.clearCache();
			edmWriter.close();
		} catch (MalformedURLException e) {
			throw e;
		} catch (UnknownHostException e) {
			throw e;
		} catch (SecurityException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setThreadName(int name) {
		this.threadName = "Thread-" + name;
	}

	private List<SolrInputDocument> convertEse2EdmSolr(
			SolrDocumentList solrDocumentList) throws UnknownHostException,
			MongoException, EntityNotFoundException, SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		List<SolrInputDocument> outputList = new ArrayList<SolrInputDocument>();
		for (SolrDocument document : solrDocumentList) {
			SolrInputDocument inputDocument = new SolrInputDocument();
			FullBeanImpl fullBean = new FullBeanImpl();
			if (StringUtils.equals(model, "ese")) {
				Proxy proxy = new ProxyImpl();
				proxy.setEuropeanaProxy(false);
				Proxy europeanaProxy = new ProxyImpl();
				europeanaProxy.setEuropeanaProxy(true);
				List<Proxy> proxies = new ArrayList<Proxy>();
				proxies.add(proxy);
				proxies.add(europeanaProxy);
				fullBean.setProxies(proxies);
				Aggregation aggregation = new AggregationImpl();
				List<Aggregation> aggregations = new ArrayList<Aggregation>();
				aggregations.add(aggregation);
				fullBean.setAggregations(aggregations);
				EuropeanaAggregation europeanaAggregation = new EuropeanaAggregationImpl();
				fullBean.setEuropeanaAggregation(europeanaAggregation);
				for (String fieldName : document.getFieldNames()) {
					try {
						fullBean = new FieldCreator().createFields(inputDocument, fieldName,
								document.getFieldValue(fieldName), fullBean,
								collectionMongoServer, europeanaIdMongoServer,
								document);
					} catch (MultipleUniqueFieldsException e) {
						log.log(Level.SEVERE, e.getMessage());
					}
				}
			} else {
				for (String fieldName : document.getFieldNames()) {
					try {
						createEdmFields(inputDocument, fieldName,
								document.getFieldValue(fieldName));
					} catch (MultipleUniqueFieldsException e) {
						log.log(Level.SEVERE, e.getMessage());
					}
				}
			}
			try {
				List<Entity> entities = null;
				synchronized (tagger) {
					entities = tagger.tagDocument(inputDocument);
				}
				for (FieldMapping enrichmentField : FieldMapping
						.getFieldMappings()) {
					inputDocument.removeField(enrichmentField.getEdmField());
				}
				if (entities.size() > 0) {
					fullBean = new EntityMerger().mergeEntities(entities, fullBean, inputDocument);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mongoList.add(fullBean);
			outputList.add(inputDocument);
		}
		return outputList;
	}

	// TODO: check if to be used
	private void createEdmFields(SolrInputDocument inputDocument,
			String fieldName, Object fieldValue) {
		inputDocument.addField(fieldName, fieldValue);

	}

	// Add to edm:hasMet
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

	// Merge contextual entities

	// Create ESE fields
	

	public void setFrom(long from) {
		this.from = from;
	}

	public void setTo(long to) {
		this.maxDocuments = to;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public void run() {
		try {
			convert();
		} catch (MultipleUniqueFieldsException e) {
			e.printStackTrace();
		} catch (MongoDBException e) {
			e.printStackTrace();
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void instantiateMongoServer() {
		try {
			if (mongoServer==null||!mongoServer.getDatastore().getMongo().getConnector().isOpen()) {
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

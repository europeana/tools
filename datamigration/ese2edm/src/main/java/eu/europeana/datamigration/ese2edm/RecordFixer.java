package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
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
import eu.europeana.datamigration.ese2edm.converters.EDMWriter;
import eu.europeana.datamigration.ese2edm.converters.ESEReader;
import eu.europeana.datamigration.ese2edm.converters.generic.EntityMerger;
import eu.europeana.datamigration.ese2edm.converters.generic.FieldCreator;
import eu.europeana.datamigration.ese2edm.enrichment.EuropeanaTagger;
import eu.europeana.datamigration.ese2edm.enums.FieldMapping;
import eu.europeana.datamigration.ese2edm.exception.EntityNotFoundException;
import eu.europeana.datamigration.ese2edm.exception.MultipleUniqueFieldsException;
import eu.europeana.datamigration.ese2edm.server.SolrServer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;
//TODO: Refactor all the converte classes

public class RecordFixer {
	private static Logger log;
	private final static EuropeanaTagger tagger = new EuropeanaTagger();
	static EDMWriter edmWriter;
	static EdmMongoServer mongoServer;
	
	
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
			instantiateMongoServer();
		} catch (SecurityException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			List<String> records = IOUtils.readLines(new FileInputStream(
					args[0]));

			for (String record : records) {
				for (int i = 0; i < 9; i++) {
					HttpSolrServer readServer = new HttpSolrServer(
							PropertyUtils.getReadServerUrl() + i);
					ESEReader reader = new ESEReader(readServer);
					SolrDocument doc = reader.fetchRecord(record);
					if (doc != null) {
						convertEse2EdmSolr(doc);
					}
					
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private static SolrInputDocument convertEse2EdmSolr(
			SolrDocument document) throws UnknownHostException,
			MongoException, EntityNotFoundException, SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, MalformedURLException {
		SolrServer writeServer = new SolrServer();
		CollectionMongoServer collectionMongoServer = new CollectionMongoServer(new Mongo(
				PropertyUtils.getMongoServer(),
				PropertyUtils.getMongoPort()),
				PropertyUtils.getCollectionDB());
		EuropeanaIdMongoServer europeanaIdMongoServer = new EuropeanaIdMongoServer(new Mongo(
				PropertyUtils.getMongoServer(),
				PropertyUtils.getMongoPort()),
				PropertyUtils.getEuropeanaIdDB());
		europeanaIdMongoServer.createDatastore();
		instantiateMongoServer();
		// writeServer.createWriteSolrServer("http://10.101.38.1:8282/solr/");
		writeServer
				.createWriteSolrServer(PropertyUtils.getWriteServerUrl());

		edmWriter = new EDMWriter(writeServer, mongoServer);
			SolrInputDocument inputDocument = new SolrInputDocument();
			FullBeanImpl fullBean = new FullBeanImpl();
			
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
			fullBean = edmWriter.saveEntities(fullBean);
		mongoServer.getDatastore().save(fullBean);
			return inputDocument;
		
	
	}
	public static void instantiateMongoServer() {
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

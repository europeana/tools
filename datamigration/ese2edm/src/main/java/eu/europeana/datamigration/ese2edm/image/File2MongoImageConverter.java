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
package eu.europeana.datamigration.ese2edm.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ThumbnailService;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.DataProvider;
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
import eu.europeana.corelib.definitions.jibx.IsShownAt;
import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.Provider;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.Rights;
import eu.europeana.corelib.definitions.jibx.Rights1;
import eu.europeana.corelib.definitions.jibx.Title;
import eu.europeana.corelib.definitions.jibx._Object;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.definitions.solr.entity.Proxy;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.utils.HashUtils;
import eu.europeana.datamigration.ese2edm.converters.ESEReader;
import eu.europeana.datamigration.ese2edm.enums.FieldMapping;
import eu.europeana.datamigration.ese2edm.helpers.Record;
import eu.europeana.datamigration.ese2edm.server.SolrServer;

/**
 * Converter of images from file system to Mongo
 * @author gmamakis
 *
 */
@SuppressWarnings("deprecation")
public class File2MongoImageConverter implements Runnable {
	ThumbnailService thumbnailService;
	private SolrServer solrServer;
	private long maxDocuments;
	private int from = 0;
	private int timeout;
	private final static int DOCUMENTS_TO_READ = 1000;
	private static Logger log;
	private final String EUROPEANA_URI = FieldMapping.EUROPEANA_URI
			.getEdmField();
	private final String EUROPEANA_OBJECT = FieldMapping.EUROPEANA_OBJECT
			.getEdmField();
	private final String EUROPEANA_HASH = EUROPEANA_OBJECT + "_hash";
	private final String REPOSITORY = "/repository/ingestion/img_cache/thumbler";
	private final String CHAR_SEPARATOR = "/";
	private final String FILE_SEPARATOR = System.getProperty("file.separator");
	private final String FULLDOC = "FULL_DOC";
	CollectionMongoServer collectionMongoServer;
	EdmMongoServerImpl edmMongoServer;

	//Logger initializer
	static {
		FileHandler hand;
		try {
			if (new File("logs/ImageConversion.log").exists()) {
				new File("logs/ImageConversion.log").renameTo(new File(
						"logs/ImageConversion.log." + new Date()));
			}
			hand = new FileHandler("logs/ImageConversion.log");
			log = Logger.getLogger("log_file");
			log.addHandler(hand);
		} catch (SecurityException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Constructor for File2ImageMongoConverter
	 * @param thumbnailService The ThumbnailService to generate the thumbnails through
	 */
	public File2MongoImageConverter(ThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	public File2MongoImageConverter(){
		
	}
	
	/**
	 * Where to start from
	 * @param from
	 */
	public void setStart(int from) {
		this.from = from;
	}

	/**
	 * Where to end to
	 * @param to
	 */
	public void setEnd(int to) {
		this.maxDocuments = to;
	}

	/**
	 * The timeout between to consequent calls
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Convert an image from file-based to db-based with XMP-metadata
	 * @param params
	 * @throws SolrServerException
	 * @throws UnknownHostException
	 * @throws MongoException
	 * @throws MalformedURLException
	 * @throws InterruptedException
	 */
	public void convert(String... params) throws SolrServerException,
			UnknownHostException, MongoException, MalformedURLException,
			InterruptedException {

		if (params.length > 0) {
			from = Integer.parseInt(params[0]);
		}
		collectionMongoServer = new CollectionMongoServer(new Mongo(
				"europeana-ese2edm.isti.cnr.it", 27017), "collections");
		try {
			edmMongoServer = new EdmMongoServerImpl(new Mongo(
					"europeana-ese2edm.isti.cnr.it", 27017), "europeana",
					"europeana", "culture");
		} catch (MongoDBException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		solrServer = new SolrServer();
		solrServer.createReadSolrServer("http://europeana-ese2edm.isti.cnr.it:9595/solr/search");

		// donot be confused, it is just a solr reader!!!
		ESEReader reader = new ESEReader(solrServer);
		int i = from;
		while (from < maxDocuments) {
			SolrDocumentList solrList = reader.readEseDocuments(
					maxDocuments, from, DOCUMENTS_TO_READ);
			for (SolrDocument document : solrList) {
				try {
					log.log(Level.INFO,
							"Trying document number " + i + " with id "
									+ document.getFieldValue("europeana_id"));
					createImageCache(document);
				} catch (IOException e) {
					log.log(Level.INFO, "Image not found for document "
							+ document.getFieldValue("europeana_id"));
				}
				i++;
			}
			from += DOCUMENTS_TO_READ;
			System.out.println("Already passed " + from + "images");
		}
	}

	@SuppressWarnings("unused")
	@Deprecated
	private SolrDocumentList constructFromUris(List<String> strList) {
		SolrDocumentList list = new SolrDocumentList();
		for (String str:strList){
			SolrDocument doc = new SolrDocument();
			doc.setField(EUROPEANA_URI, StringUtils.substringBefore(str, ";"));
			doc.setField(EUROPEANA_OBJECT, StringUtils.substringBetween(str, ";", ";"));
			doc.setField(EUROPEANA_HASH, StringUtils.substringAfterLast(str, ";"));
			list.add(doc);
		}
		
		return list;
	}

	@SuppressWarnings("unchecked")
	private void createImageCache(SolrDocument document) throws IOException,
			InterruptedException {

		if (document.getFieldValue(EUROPEANA_OBJECT) instanceof String) {

			String originalUrl = (String) document
					.getFieldValue(EUROPEANA_OBJECT);
			createImage(originalUrl, document, 0,
					(String) document.getFieldValue(EUROPEANA_HASH));
		} else if (document.getFieldValue(EUROPEANA_OBJECT) instanceof ArrayList) {
			int i = 0;
			for (String originalUrl : (ArrayList<String>) document
					.getFieldValue(EUROPEANA_OBJECT)) {

				createImage(originalUrl, document, i,
						((ArrayList<String>) document
								.getFieldValue(EUROPEANA_HASH)).get(i));
				i++;
			}
		}
	}

	private void createImage(String originalUrl, SolrDocument document, int i,
			String imageHash) throws IOException, InterruptedException {
		// recordData[0]=>old collectionID, recordData[1]=>recordID
		String[] recordData = StringUtils.split(
				(String) document.getFieldValue(EUROPEANA_URI), CHAR_SEPARATOR);
		String collectionID = findCollectionID(recordData[0]);
		String recordHashFirstTwo = StringUtils.substring(imageHash, 0, 2);
		String recordHashSecondTwo = StringUtils.substring(imageHash, 2, 4);
		String filePath = REPOSITORY + FILE_SEPARATOR + FULLDOC
				+ FILE_SEPARATOR + recordHashFirstTwo + FILE_SEPARATOR
				+ recordHashSecondTwo + FILE_SEPARATOR + imageHash + ".jpg";
		BufferedImage imageFulldoc = null;
		if (new File(filePath).exists()) {
			imageFulldoc = ImageIO.read(new File(filePath));
		} else {
			URL apiUrl = new URL(
					String.format(
							"http://img1.europeana.sara.nl/api/image?type=%s&uri=%s&size=FULL_DOC",
							document.getFieldValue(StringUtils
									.substringBetween(
											EdmLabel.PROVIDER_EDM_TYPE
													.toString(), "[", "]")),
							originalUrl));

			URLConnection conn = apiUrl.openConnection();
			if (!StringUtils.contains(conn.getContentType(), "png")) {
				imageFulldoc = ImageIO.read(apiUrl);
			}
		}

		if (imageFulldoc != null) {
			FullBean fb = edmMongoServer.getFullBean(CHAR_SEPARATOR
					+ recordData[0] + CHAR_SEPARATOR + recordData[1]) != null ? edmMongoServer
					.getFullBean(CHAR_SEPARATOR + recordData[0]
							+ CHAR_SEPARATOR + recordData[1]) : edmMongoServer
					.getFullBean(CHAR_SEPARATOR + collectionID + CHAR_SEPARATOR
							+ recordData[1]);
			if (fb != null) {
				RDF edmRecord = createRDF(fb);
				try {
					thumbnailService
							.storeThumbnail(recordData[1], recordData[0],
									imageFulldoc, originalUrl, edmRecord);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			} else {
				try {
					thumbnailService
							.storeThumbnail(recordData[1], recordData[0],
									imageFulldoc, originalUrl, new RDF());
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				log.log(Level.INFO,
						"Record does not exist in Mongo. Investigated: "
								+ CHAR_SEPARATOR + recordData[0]
								+ CHAR_SEPARATOR + recordData[1]);
			}
		}
		Thread.sleep(timeout);
	}

	private RDF createRDF(FullBean fullBean) {
		RDF rdf = new RDF();

		ProxyType proxy = new ProxyType();
		for (Proxy mongoProxy : fullBean.getProxies()) {
			if (!mongoProxy.isEuropeanaProxy()) {

				proxy.setAbout(mongoProxy.getAbout());
				List<EuropeanaType.Choice> dcchoices = proxy.getChoiceList();
				if (mongoProxy.getDcTitle() != null) {
					for (Entry<String, List<String>> objList : mongoProxy
							.getDcTitle().entrySet()) {
						for (String rdfStr : objList.getValue()) {
							EuropeanaType.Choice proxyChoice = new EuropeanaType.Choice();
							Title obj = new Title();
							Lang lang = new Lang();
							lang.setLang(objList.getKey());
							obj.setLang(lang);
							obj.setString(rdfStr);
							proxyChoice.setTitle(obj);
							dcchoices.add(proxyChoice);
						}
					}

				}

				if (mongoProxy.getDcCreator() != null) {
					for (Entry<String, List<String>> objList : mongoProxy
							.getDcCreator().entrySet()) {
						for (String rdfStr : objList.getValue()) {
							EuropeanaType.Choice proxyChoice = new EuropeanaType.Choice();
							Creator obj = new Creator();
							ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
							lang.setLang(objList.getKey());
							obj.setLang(lang);
							
							if (!isUri(rdfStr)) {
								obj.setString(rdfStr);
							} else {
								ResourceOrLiteralType.Resource res = new ResourceOrLiteralType.Resource();
								res.setResource(rdfStr);
								obj.setResource(res);
							}
							proxyChoice.setCreator(obj);
							dcchoices.add(proxyChoice);
						}
					}
				}
				if (mongoProxy.getEdmRights() != null) {
					for (Entry<String, List<String>> objList : mongoProxy
							.getEdmRights().entrySet()) {
						for (String rdfStr : objList.getValue()) {
							EuropeanaType.Choice proxyChoice = new EuropeanaType.Choice();
							Rights obj = new Rights();
							ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
							lang.setLang(objList.getKey());
							obj.setLang(lang);
							if (!isUri(rdfStr)) {
								obj.setString(rdfStr);
							} else {
								ResourceOrLiteralType.Resource res = new ResourceOrLiteralType.Resource();
								res.setResource(rdfStr);
								obj.setResource(res);
							}
							proxyChoice.setRights(obj);
							dcchoices.add(proxyChoice);
						}
					}
				}
				proxy.setChoiceList(dcchoices);
			}
		}
		List<ProxyType> proxyList = new ArrayList<ProxyType>();
		proxyList.add(proxy);
		rdf.setProxyList(proxyList);

		Aggregation aggregation = new Aggregation();
		for (eu.europeana.corelib.definitions.solr.entity.Aggregation mongoAggregation : fullBean
				.getAggregations()) {
			if (mongoAggregation.getEdmRights() != null) {
				for (Entry<String, List<String>> objList : mongoAggregation
						.getEdmRights().entrySet()) {
					for (String rdfStr : objList.getValue()) {
						Rights1 obj = new Rights1();
						ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
						lang.setLang(objList.getKey());
						obj.setLang(lang);
						if (!isUri(rdfStr)) {
							obj.setString(rdfStr);
						} else {
							ResourceOrLiteralType.Resource res = new ResourceOrLiteralType.Resource();
							res.setResource(rdfStr);
							obj.setResource(res);
						}
						aggregation.setRights(obj);
					}
				}
			}
			if (mongoAggregation.getEdmProvider() != null) {
				for (Entry<String, List<String>> objList : mongoAggregation
						.getEdmProvider().entrySet()) {
					for (String rdfStr : objList.getValue()) {
						Provider obj = new Provider();
						ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
						lang.setLang(objList.getKey());
						obj.setLang(lang);
						if (!isUri(rdfStr)) {
							obj.setString(rdfStr);
						} else {
							ResourceOrLiteralType.Resource res = new ResourceOrLiteralType.Resource();
							res.setResource(rdfStr);
							obj.setResource(res);
						}
						aggregation.setProvider(obj);
					}
				}
			}
			if (mongoAggregation.getEdmDataProvider() != null) {
				for (Entry<String, List<String>> objList : mongoAggregation
						.getEdmDataProvider().entrySet()) {
					for (String rdfStr : objList.getValue()) {
						DataProvider obj = new DataProvider();
						ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
						lang.setLang(objList.getKey());
						obj.setLang(lang);
						if (!isUri(rdfStr)) {
							obj.setString(rdfStr);
						} else {
							ResourceOrLiteralType.Resource res = new ResourceOrLiteralType.Resource();
							res.setResource(rdfStr);
							obj.setResource(res);
						}
						aggregation.setDataProvider(obj);
					}
				}
			}
			if (mongoAggregation.getEdmObject() != null) {

				_Object obj = new _Object();

				obj.setResource(mongoAggregation.getEdmObject());

				aggregation.setObject(obj);

			}
			if (mongoAggregation.getEdmIsShownAt() != null) {

				IsShownAt obj = new IsShownAt();

				obj.setResource(mongoAggregation.getEdmIsShownAt());

				aggregation.setIsShownAt(obj);

			}
		}
		List<Aggregation> aggregations = new ArrayList<Aggregation>();
		aggregations.add(aggregation);
		rdf.setAggregationList(aggregations);

		return rdf;
	}

	private boolean isUri(String str) {
		if (str.startsWith("http://")) {
			return true;
		}
		return false;
	}

	private String findCollectionID(String oldCollectionId) {
		String newCollectionId = collectionMongoServer
				.findNewCollectionId(oldCollectionId);

		return newCollectionId != null ? newCollectionId : oldCollectionId;
	}

	public void run() {
		try {
			convert();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public void createIndex() {
		SolrServer readSolrServer = new SolrServer();
		//SolrServer writeSolrServer = new SolrServer();
		
		try {
			readSolrServer
					.createReadSolrServer("http://europeana-ese2edm.isti.cnr.it:9191/solr/search");
			ESEReader reader = new ESEReader(readSolrServer);
			List<Record> records = new ArrayList<Record>();
			while (from < maxDocuments) {
				SolrDocumentList solrList = reader.readEseDocuments(
						maxDocuments, from, DOCUMENTS_TO_READ);
				
				for (SolrDocument document: solrList){
					Record record = new Record();
					
					record.setId((String) document.getFieldValue(EUROPEANA_URI));
					if (document.getFieldValue(EUROPEANA_OBJECT) instanceof String) {
						record.setObject((String)document.getFieldValue(EUROPEANA_OBJECT));
						
						record.setHash(HashUtils.createHashSHA256((String)document.getFieldValue(EUROPEANA_OBJECT)));
					} else if (document.getFieldValue(EUROPEANA_OBJECT) instanceof ArrayList){
						String obj = ((ArrayList<String>)document.getFieldValue(EUROPEANA_OBJECT)).get(0);
						record.setObject(obj);
						record.setHash(HashUtils.createHashSHA256(obj));
					}
					records.add(record);
				}
				
				
				//log.log(Level.INFO,"Added " + from + " records to the image conversion index");
				System.out.println("Added " + from + " records to the image conversion csv");
				from += DOCUMENTS_TO_READ;
				
			}
			
			Collections.sort(records,new RecordByHashComparator());
			
			for(Record rec:records){
				StringBuilder sb = new StringBuilder();
				sb.append(rec.getId());
				sb.append(";");
				sb.append(rec.getObject());
				sb.append(";");
				sb.append(rec.getHash());
				sb.append("\n");
				FileUtils.write(new File("/repository/img_migr.csv"), sb.toString(),true);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	@Deprecated
	private class RecordByHashComparator implements Comparator<Record>{

		@Override
		public int compare(Record o1, Record o2) {
			
			return o1.getHash().compareTo(o2.getHash());
		}
		
		
	}
}

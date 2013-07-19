package eu.europeana.datamigration.ese2edm.converters.generic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.mongodb.MongoException;

import eu.annocultor.converters.europeana.Entity;
import eu.europeana.corelib.definitions.solr.entity.Aggregation;
import eu.europeana.corelib.definitions.solr.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.solr.entity.Proxy;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.utils.EDMUtils;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.datamigration.ese2edm.enrichment.EuropeanaTagger;
import eu.europeana.datamigration.ese2edm.enums.FieldMapping;
import eu.europeana.datamigration.ese2edm.exception.EntityNotFoundException;
import eu.europeana.datamigration.ese2edm.exception.MultipleUniqueFieldsException;

public class GenericEse2EdmConverter {

	// Convert ESE Solr documents to EDM
	public List<SolrInputDocument> convertEse2EdmSolr(
			SolrDocumentList solrDocumentList, CollectionMongoServer collectionMongoServer, EuropeanaIdMongoServer europeanaIdMongoServer, EuropeanaTagger tagger, List<FullBeanImpl> mongoList, boolean createRDF) throws UnknownHostException,
			MongoException, EntityNotFoundException, SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		List<SolrInputDocument> outputList = new ArrayList<SolrInputDocument>();
		for (SolrDocument document : solrDocumentList) {
			outputList.add(convertEse2EdmSolr(document,collectionMongoServer, europeanaIdMongoServer, tagger, mongoList, createRDF));
		}
		return outputList;
	}

	// Convert ESE Solr documents to EDM
	public SolrInputDocument convertEse2EdmSolr(SolrDocument document, CollectionMongoServer collectionMongoServer, EuropeanaIdMongoServer europeanaIdMongoServer, EuropeanaTagger tagger, List<FullBeanImpl> mongoList, boolean createRDF)
			throws UnknownHostException, MongoException,
			EntityNotFoundException, SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

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

				fullBean = new FieldCreator()
						.createFields(inputDocument, fieldName,
								document.getFieldValue(fieldName), fullBean,
								collectionMongoServer, europeanaIdMongoServer,
								document);
			} catch (MultipleUniqueFieldsException e) {

			}
		}
		try {
			List<Entity> entities = tagger.tagDocument(inputDocument);
			for (FieldMapping enrichmentField : FieldMapping.getFieldMappings()) {
				inputDocument.removeField(enrichmentField.getEdmField());
			}
			if (entities.size() > 0) {
				fullBean = new EntityMerger().mergeEntities(entities, fullBean,
						inputDocument);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mongoList.add(fullBean);
		if (createRDF) {
			try {
				FileUtils.write(
						new File("/home/gmamakis/rdf/" + fullBean.getAbout()
								+ ".xml"), EDMUtils.toEDM(fullBean));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return inputDocument;
	}
}

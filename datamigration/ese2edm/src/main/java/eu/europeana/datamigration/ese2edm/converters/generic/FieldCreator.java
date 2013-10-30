package eu.europeana.datamigration.ese2edm.converters.generic;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.mongodb.MongoException;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.tools.lookuptable.Collection;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.datamigration.ese2edm.enums.FieldMapping;
import eu.europeana.datamigration.ese2edm.exception.EntityNotFoundException;
import eu.europeana.datamigration.ese2edm.exception.MultipleUniqueFieldsException;

public class FieldCreator {
	private final static String EUROPEANA_ISSHOWNAT = "europeana_isShownAt";
	private final static String EUROPEANA_ISSHOWNBY = "europeana_isShownBy";
	private final static String EUROPEANA_OBJECT = "europeana_object";
	private final static String EUROPEANA_URI = "europeana_uri";
	private final static String EUROPEANA_RECORD = "http://www.europeana.eu/resolve/record/";
	private final static String EUROPEANA_COLLECTIONNAME = "europeana_collectionName";
	private final static String EDM_PREVIEW = "";
	@SuppressWarnings("unchecked")
		public FullBeanImpl createFields(SolrInputDocument inputDocument,
				String fieldName, Object fieldValue, FullBeanImpl fullBean,
				CollectionMongoServer collectionMongoServer,
				EuropeanaIdMongoServer europeanaIdMongoServer, SolrDocument doc)
				throws UnknownHostException, MongoException,
				EntityNotFoundException, SecurityException,
				IllegalArgumentException, NoSuchMethodException,
				IllegalAccessException, InvocationTargetException,
				MultipleUniqueFieldsException {
	
			// Get the mapping between ESE and EDM if it exists
			FieldMapping fieldMapping = FieldMapping.getFieldMapping(fieldName);
			if (fieldMapping != null) {
				// if it is the europeana id
				if (StringUtils.equals(fieldName, EUROPEANA_URI)) {
					String uri = (String) fieldValue;
					String strippedURI = StringUtils.replace(uri, EUROPEANA_RECORD,
							"");
					String oldCollectionId = findCollectionId(doc.get(EUROPEANA_COLLECTIONNAME));
					String hash = StringUtils.split(strippedURI, "/")[1];
					// Check if the collection has changed
					String newCollectionId = collectionMongoServer
							.findNewCollectionId(oldCollectionId);
					if (newCollectionId != null) {
						fieldValue = new EuropeanaIdGenerator().saveNewEuropeanaId(europeanaIdMongoServer,
								inputDocument, uri, hash, newCollectionId);
	
					} else {
						// Create the europeanaID
						String id = EuropeanaUriUtils.createEuropeanaId(
								oldCollectionId, hash);
						inputDocument.addField(
								FieldMapping.EUROPEANA_URI.getEdmField(), id);
						String colId = StringUtils.substringBetween(id, "/", "/");
						if (!StringUtils.equals(oldCollectionId, colId)) {
							Collection collection = new Collection();
							collection.setOldCollectionId(oldCollectionId);
							collection.setNewCollectionId(colId);
							collectionMongoServer.saveCollection(collection);
							 new EuropeanaIdGenerator().saveNewEuropeanaId(europeanaIdMongoServer,
									inputDocument, uri, hash, colId);
						}
						fieldValue = id;
					}
					// if it is the Europeana CollectionName
				} else if (StringUtils.equals(fieldName, EUROPEANA_COLLECTIONNAME)) {
					String collectionId = findCollectionId(fieldValue);
	
					String newCollectionId = collectionMongoServer
							.findNewCollectionId(collectionId);
					// If the collection has not change save as it is
					if (newCollectionId == null) {
						inputDocument.addField(fieldMapping.getEdmField(),
								fieldValue);
					} else {
						if(fieldValue instanceof ArrayList){
						String colId = StringUtils.replace(((ArrayList<String>) fieldValue).get(0),
								collectionId, newCollectionId);
						inputDocument.addField(fieldMapping.getEdmField(), colId);
						fieldValue = colId;
						} else {
							String colId = StringUtils.replace(((String) fieldValue),
									collectionId, newCollectionId);
							inputDocument.addField(fieldMapping.getEdmField(), colId);
							fieldValue = colId;
						}
					}
				} else {
					// If it is any other field check if it is a dynamic field
					if (fieldMapping.getHasDynamicField()) {
						inputDocument.addField(fieldMapping.getEdmField() + ".def",
								fieldValue);
						if (fieldMapping.isEnrichmentField()) {
							inputDocument.addField(fieldMapping.getEdmField(),
									fieldValue);
						}
					} else {
						// else if it is one of the isShownAt or isShownBy or object
						// and it already exists ignore it
						if (StringUtils.equals(fieldName, EUROPEANA_ISSHOWNAT)
								|| StringUtils.equals(fieldName,
										EUROPEANA_ISSHOWNBY)
								|| StringUtils.equals(fieldName, EUROPEANA_OBJECT)) {
							if (!inputDocument.containsKey(fieldName)) {
								inputDocument.addField(fieldMapping.getEdmField(),
										fieldValue);
							}
						} else {
							inputDocument.addField(fieldMapping.getEdmField(),
									fieldValue);
						}
					}
				}
	
				// Append to FullBean
				if (fieldValue != null) {
	
					if (fieldValue instanceof ArrayList) {
						for (String value : (ArrayList<String>) fieldValue) {
							fieldMapping.setField(fullBean, value);
						}
					}
	
					else if (fieldValue instanceof String[]) {
						for (String value : (String[]) fieldValue) {
							fieldMapping.setField(fullBean, value);
						}
					} else {
						ClassType clt = ClassType.getClassType(fieldValue
								.getClass().getCanonicalName());
						fieldMapping.setField(fullBean, clt.toString(fieldValue));
	
					}
				}
	
			}
			return fullBean;
		}

	
	@SuppressWarnings("unchecked")
	private String findCollectionId(Object val){
		
		if(val instanceof String){
			return StringUtils.split(((String)val
				), "_")[0];
		} else {
			return StringUtils.split(((ArrayList<String>)val
					).get(0), "_")[0];
		}
	}
	/**
	 * Enumeration holding the available class types that can be appended to a
	 * FullBean
	 * 
	 * @author Yorgos.Mamakis@ kb.nl
	 * 
	 */
	private enum ClassType {
		java_lang_Integer() {
			@Override
			public String toString(Object fieldValue) {

				return Integer.toString((Integer) fieldValue);
			}
		},
		java_lang_Float() {
			@Override
			public String toString(Object fieldValue) {
				return Float.toString((Float) fieldValue);
			}
		},
		java_lang_String() {
			@Override
			public String toString(Object fieldValue) {

				return (String) fieldValue;
			}
		},
		java_lang_Boolean() {
			@Override
			public String toString(Object fieldValue) {

				return Boolean.toString((Boolean) fieldValue);
			}
		},
		java_util_Date() {
			@Override
			public String toString(Object fieldValue) {
				return DateFormatUtils.formatUTC((Date) fieldValue,
						"yyyy-MM-dd", new Locale("en"));

			}
		};

		public static ClassType getClassType(String value) {
			return ClassType.valueOf(StringUtils.replace(value, ".", "_"));
		}

		public abstract String toString(Object fieldValue);
	}
}

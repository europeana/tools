package eu.europeana.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.converters.europeana.Entity;
import eu.annocultor.converters.europeana.Field;
import eu.annocultor.converters.solr.BuiltinSolrDocumentTagger;
import eu.europeana.corelib.definitions.jibx.Alt;
import eu.europeana.corelib.definitions.jibx.Lat;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx._Long;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.corelib.dereference.impl.RdfMethod;
import eu.europeana.datamigration.ese2edm.enrichment.EuropeanaTagger;

public class AnnocultorTest {

	public static void main(String[] args) {
		BuiltinSolrDocumentTagger solrTagger = new EuropeanaTagger();
		try {
			solrTagger.init("europeana");
			SolrInputDocument solrDoc = new SolrInputDocument();
			solrDoc.addField("proxy_dcterms_spatial", "paris");

			List<Entity> enrichments = solrTagger.tagDocument(solrDoc);
			for (Entity entity : enrichments) {
				System.out.println(entity);
			}

			List<PlaceType> places = createPlaces(enrichments);
			System.out.println(places.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<PlaceType> createPlaces(List<Entity> entities) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		List<PlaceType> places = new ArrayList<PlaceType>();
		for (Entity entity : entities) {
			PlaceType ts = new PlaceType();

			List<Field> fields = entity.getFields();
			if (fields != null && fields.size() > 0) {
				for (Field field : fields) {
					if (StringUtils.equalsIgnoreCase(field.getName(),
							"edm_place")) {
						ts.setAbout(field
								.getValues()
								.get(field.getValues().keySet().iterator()
										.next()).get(0));

					} else {
						if (field.getValues() != null) {
							for (Entry<String, List<String>> entry : field
									.getValues().entrySet()) {
								for (String str : entry.getValue()) {
									appendValue(PlaceType.class, ts,
											field.getName(), str, "_@xml:lang",
											entry.getKey());
								}
							}
						}
					}

				}
				
				places.add(ts);
				
			}
		}
		return places;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T> T appendValue(Class<T> clazz, T obj, String edmLabel,
			String val, String edmAttr, String valAttr)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		RdfMethod RDF = null;
		for (RdfMethod rdfMethod : RdfMethod.values()) {
			if (StringUtils.equals(rdfMethod.getSolrField(), edmLabel)) {
				RDF = rdfMethod;
			}
		}

		//
		if (RDF != null) {
			if (RDF.getMethodName().endsWith("List")) {

				Method mthd = clazz.getMethod(RDF.getMethodName());

				List lst = mthd.invoke(obj) != null ? (ArrayList) mthd
						.invoke(obj) : new ArrayList();
				if (RDF.getClazz().getSuperclass()
						.isAssignableFrom(ResourceType.class)) {

					ResourceType rs = new ResourceType();
					rs.setResource(val);
					lst.add(RDF.returnObject(RDF.getClazz(), rs));

				} else if (RDF.getClazz().getSuperclass()
						.isAssignableFrom(ResourceOrLiteralType.class)) {
					ResourceOrLiteralType rs = new ResourceOrLiteralType();
					if (isURI(val)) {

						Resource res = new Resource();
						res.setResource(val);
						rs.setResource(res);

					} else {
						rs.setString(val);
					}
					Lang lang = new Lang();

					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {
						lang.setLang(StringUtils.isEmpty(valAttr) ? "def"
								: valAttr);

					} else {
						lang.setLang("def");
					}
					rs.setLang(lang);
					lst.add(RDF.returnObject(RDF.getClazz(), rs));
				} else if (RDF.getClazz().getSuperclass()
						.isAssignableFrom(LiteralType.class)) {
					LiteralType rs = new LiteralType();
					rs.setString(val);
					LiteralType.Lang lang = new LiteralType.Lang();
					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {

						lang.setLang(StringUtils.isEmpty(valAttr) ? "def"
								: valAttr);

					} else {
						lang.setLang("def");
					}
					rs.setLang(lang);
					lst.add(RDF.returnObject(RDF.getClazz(), rs));
				}

				Class<?>[] cls = new Class<?>[1];
				cls[0] = List.class;
				Method method = obj.getClass().getMethod(
						StringUtils.replace(RDF.getMethodName(), "get", "set"),
						cls);
				method.invoke(obj, lst);
			} else {
				if (RDF.getClazz().isAssignableFrom(ResourceType.class)) {
					ResourceType rs = new ResourceType();
					rs.setResource(val);
					Class<?>[] cls = new Class<?>[1];
					cls[0] = RDF.getClazz();
					Method method = obj.getClass().getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
					method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
				} else if (RDF.getClazz().isAssignableFrom(LiteralType.class)) {
					LiteralType rs = new LiteralType();
					rs.setString(val);
					LiteralType.Lang lang = new LiteralType.Lang();
					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {

						lang.setLang(StringUtils.isEmpty(valAttr) ? "def"
								: valAttr);

					} else {
						lang.setLang("def");
					}
					rs.setLang(lang);
					Class<?>[] cls = new Class<?>[1];
					cls[0] = RDF.getClazz();
					Method method = obj.getClass().getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
					method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));

				} else if (RDF.getClazz().isAssignableFrom(
						ResourceOrLiteralType.class)) {
					ResourceOrLiteralType rs = new ResourceOrLiteralType();
					if (isURI(val)) {
						Resource res = new Resource();
						res.setResource(val);
						rs.setResource(res);
					} else {
						rs.setString(val);
					}
					Lang lang = new Lang();
					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {

						lang.setLang(StringUtils.isEmpty(valAttr) ? "def"
								: valAttr);

					} else {
						lang.setLang("def");
					}
					rs.setLang(lang);
					Class<?>[] cls = new Class<?>[1];
					cls[0] = clazz;
					Method method = obj.getClass().getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
					method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
				} else if (RDF.getClazz().isAssignableFrom(_Long.class)) {
					Float rs = Float.parseFloat(val);
					_Long lng = new _Long();
					lng.setLong(rs);
					((PlaceType) obj).setLong(lng);

				} else if (RDF.getClazz().isAssignableFrom(Lat.class)) {
					Float rs = Float.parseFloat(val);
					Lat lng = new Lat();
					lng.setLat(rs);
					((PlaceType) obj).setLat(lng);

				} else if (RDF.getClazz().isAssignableFrom(Alt.class)) {
					Float rs = Float.parseFloat(val);
					Alt lng = new Alt();
					lng.setAlt(rs);
					((PlaceType) obj).setAlt(lng);

				}
			}
		}
		//
		return obj;
	}
	
	private static boolean isURI(String uri) {

		try {
			new URL(uri);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}

	}
}

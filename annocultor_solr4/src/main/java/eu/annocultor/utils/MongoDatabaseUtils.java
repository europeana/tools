package eu.annocultor.utils;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.annocultor.common.Language;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.AbstractVocabulary;
import eu.annocultor.tagger.vocabularies.VocabularyOfPeople;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.tagger.vocabularies.VocabularyOfTime;

/**
 * Util class for saving and retrieving TermLists from Mongo It is used to
 * bypass the memory-based Annocultor enrichment, for use within UIM. The
 * TermList uses MongoTerm, MongoTermList, PlaceTerm and PeriodTerm to reflect
 * the stored Entities.
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class MongoDatabaseUtils {

	static JacksonDBCollection<MongoTermList, String> coll;
	static DB db;
	static Map<String,Map<String, TermList>> memCache = new HashMap<String,Map<String, TermList>>();
	
	/**
	 * Check if DB exists and initialization of the db
	 * 
	 * @return
	 */
	
	
	
	public static boolean dbExists(String host, int port) {
		try {
			if(db==null){
			Mongo mongo = new Mongo(host, port);
			db = mongo.getDB("annocultor_db");
			if (db.collectionExists("TermList")) {
				coll = JacksonDBCollection.wrap(db.getCollection("TermList"),
						MongoTermList.class, String.class);

				coll.ensureIndex("codeUri");

				return true;
			} else {
				coll = JacksonDBCollection.wrap(db.getCollection("TermList"),
						MongoTermList.class, String.class);
				coll.ensureIndex("codeUri");
				return false;
			}
			} 
			return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Save TermList in mongodB
	 * 
	 * @param dbtabl
	 * @param vocabulary
	 */
	public static void save(String dbtabl, AbstractVocabulary vocabulary) {

		if (vocabulary instanceof VocabularyOfTime) {
			saveTimeTerms((VocabularyOfTime) vocabulary);
		} else if (vocabulary instanceof VocabularyOfTerms) {
			saveTerms(vocabulary, "concept");
		} else if (vocabulary instanceof VocabularyOfPeople) {
			saveTerms(vocabulary, "people");
		} else {
			savePlaceTerms((VocabularyOfPlaces) vocabulary);
		}
	}

	public static void emptyCache() {
		memCache = new HashMap<String,Map<String, TermList>>();
	}

	/**
	 * Find TermList by codeURI
	 * 
	 * @param codeUri
	 * @param dbtable
	 * @return
	 * @throws MalformedURLException
	 */
	public static TermList findByCode(CodeURI codeUri, String dbtable)
			throws MalformedURLException {
		Map<String,TermList> typeMap = memCache.get(dbtable)!=null?memCache.get(dbtable):new ConcurrentHashMap<String,TermList>();
		if (typeMap.containsKey(codeUri.getUri())) {
			return typeMap.get(codeUri.getUri());
		}
		
		if(coll.find().is("codeUri", codeUri.getUri()).hasNext()){
		MongoTermList mongoTermList = coll.find()
				.is("codeUri", codeUri.getUri()).next();
		if (mongoTermList != null) {
			if (StringUtils.equals(dbtable, "concept")
					|| StringUtils.equals(dbtable, "people")) {
				TermList tList = retrieveTerms(mongoTermList, dbtable);
					typeMap.put(codeUri.getUri(), tList);
					memCache.put(dbtable, typeMap);
				return tList;
			} else if (StringUtils.equals(dbtable, "place")) {
				TermList tList = retrievePlaceTerms(mongoTermList, dbtable);
					typeMap.put(codeUri.getUri(), tList);
					memCache.put(dbtable, typeMap);
				return tList;
			} else if (StringUtils.equals(dbtable, "period")) {
				TermList tList = retrievePeriodTerms(mongoTermList, dbtable);
					typeMap.put(codeUri.getUri(), tList);
					memCache.put(dbtable, typeMap);
				return tList;
			}
		}
		}
		return null;
	}

	/**
	 * Retrieve people and concept terms
	 * 
	 * @param mongoTermList
	 *            The MongoTermList that will be used to create the period or
	 *            concept
	 * @param dbtable
	 *            The table to search on
	 * @return The TermList required by annocultor to perform enrichment
	 * @throws MalformedURLException
	 */
	private static TermList retrieveTerms(MongoTermList mongoTermList,
			String dbtable) throws MalformedURLException {

		List<MongoTerm> refList = fetch(
				normalize(new MongoTerm(), mongoTermList.getTerms()), dbtable);
		TermList tList = new TermList();
		for (MongoTerm mTerm : refList) {
			CodeURI codeUri = new CodeURI(mTerm.codeUri);
			String label = mTerm.label;
			String lang = mTerm.lang != null ? mTerm.lang : null;
			JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
					.wrap(db.getCollection(dbtable), MongoTerm.class,
							String.class);
			DBCursor<MongoTerm> mParent = null;
			if (mTerm.parent != null) {
				mParent = pColl.find().is("_id",
						new ObjectId(mTerm.parent.getId()));
			}
			Term parent = null;
			if (mParent != null && mParent.hasNext()) {
				MongoTerm pTerm = mParent.next();
				CodeURI codeUri2 = new CodeURI(pTerm.codeUri);
				String label2 = pTerm.label;
				String lang2 = pTerm.lang != null ? pTerm.lang : null;
				parent = new Term(label2,
						lang2 != null ? Language.Lang.valueOf(lang2) : null,
						codeUri2, dbtable);
			}
			Term term = new Term(label,
					lang != null ? Language.Lang.valueOf(lang) : null, codeUri,
					dbtable);
			if (parent != null) {
				term.setParent(parent);
			}
			tList.add(term);
		}
		return tList;
	}

	private static List<MongoTerm> fetch(
			List<DBRef<MongoTerm, String>> normalize, String dbtable) {
		List<MongoTerm> list = new ArrayList<MongoTerm>();

		JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
				.wrap(db.getCollection(dbtable), MongoTerm.class, String.class);
		for (DBRef<MongoTerm, String> mongoRef : normalize) {
			DBCursor<MongoTerm> mongoTerm = pColl.find().is("_id",
					new ObjectId(mongoRef.getId()));
			if (mongoTerm.hasNext()) {
				list.add(mongoTerm.next());
			}
		}

		return list;
	}

	private static List<PlaceTerm> fetchPlace(
			List<DBRef<PlaceTerm, String>> normalize, String dbtable) {
		List<PlaceTerm> list = new ArrayList<PlaceTerm>();
		JacksonDBCollection<PlaceTerm, String> pColl = JacksonDBCollection
				.wrap(db.getCollection(dbtable), PlaceTerm.class, String.class);
		for (DBRef<PlaceTerm, String> mongoRef : normalize) {
			DBCursor<PlaceTerm> cur = pColl.find().is("_id",
					new ObjectId(mongoRef.getId()));
			if (cur.hasNext()) {
				list.add(cur.next());
			}
		}

		return list;
	}

	private static List<PeriodTerm> fetchPeriod(
			List<DBRef<PeriodTerm, String>> normalize, String dbtable) {
		List<PeriodTerm> list = new ArrayList<PeriodTerm>();
		JacksonDBCollection<PeriodTerm, String> pColl = JacksonDBCollection
				.wrap(db.getCollection(dbtable), PeriodTerm.class, String.class);
		for (DBRef<PeriodTerm, String> mongoRef : normalize) {
			DBCursor<PeriodTerm> cur = pColl.find().is("_id",
					new ObjectId(mongoRef.getId()));
			if (cur.hasNext()) {
				list.add(cur.next());
			}
		}

		return list;
	}

	/**
	 * Retrieve place terms
	 * 
	 * @param mongoTermList
	 *            The MongoTermList that will be used to create the place
	 * @param dbtable
	 *            The table to search on
	 * @return The TermList required by annocultor to perform enrichment
	 * @throws MalformedURLException
	 */
	private static TermList retrievePlaceTerms(MongoTermList mongoTermList,
			String dbtable) throws MalformedURLException {

		List<PlaceTerm> refList = fetchPlace(
				normalize(new PlaceTerm(), mongoTermList.getTerms()), "place");
		TermList tList = new TermList();
		for (PlaceTerm mTerm : refList) {
			CodeURI codeUri = new CodeURI(mTerm.codeUri);
			String label = mTerm.label;
			String lang = mTerm.lang != null ? mTerm.lang : null;
			float lat = mTerm.lat;
			float lon = mTerm.lon;

			JacksonDBCollection<PlaceTerm, String> pColl = JacksonDBCollection
					.wrap(db.getCollection(dbtable), PlaceTerm.class,
							String.class);
			DBCursor<PlaceTerm> mParent = null;
			if (mTerm.parent != null) {
				mParent = pColl.find().is("_id",
						new ObjectId(mTerm.parent.getId()));
			}
			Term parent = null;
			if (mParent != null && mParent.hasNext()) {
				PlaceTerm pTerm = mParent.next();
				CodeURI codeUri2 = new CodeURI(pTerm.codeUri);
				String label2 = pTerm.label;
				String lang2 = pTerm.lang != null ? pTerm.lang : null;
				float lat1 = pTerm.lat;
				float lon1 = pTerm.lon;
				parent = new Term(label2,
						lang2 != null ? Language.Lang.valueOf(lang2) : null,
						codeUri2, dbtable);
				
				parent.setProperty("latitude", Float.toString(lat1));
				parent.setProperty("longitude", Float.toString(lon1));
			}
			Term term = new Term(label,
					lang != null ? Language.Lang.valueOf(lang) : null, codeUri,
					dbtable);
			if (parent != null) {
				term.setParent(parent);
			}
			term.setProperty("latitude", Float.toString(lat));
			term.setProperty("longitude", Float.toString(lon));
			tList.add(term);
		}
		return tList;
	}

	/**
	 * Retrieve timespan terms
	 * 
	 * @param mongoTermList
	 *            The MongoTermList that will be used to create the timespan
	 * @param dbtable
	 *            The table to search on
	 * @return The TermList required by annocultor to perform enrichment
	 * @throws MalformedURLException
	 */
	private static TermList retrievePeriodTerms(MongoTermList mongoTermList,
			String dbtable) throws MalformedURLException {

		List<PeriodTerm> refList = fetchPeriod(
				normalize(new PeriodTerm(), mongoTermList.getTerms()), "period");
		TermList tList = new TermList();
		for (PeriodTerm mTerm : refList) {
			CodeURI codeUri = new CodeURI(mTerm.codeUri);
			String label = mTerm.label;
			String lang = mTerm.lang != null ? mTerm.lang : null;
			JacksonDBCollection<PeriodTerm, String> pColl = JacksonDBCollection
					.wrap(db.getCollection(dbtable), PeriodTerm.class,
							String.class);
			DBCursor<PeriodTerm> mParent = null;
			if (mTerm.parent != null) {
				mParent = pColl.find().is("_id",
						new ObjectId(mTerm.parent.getId()));
			}
			Term parent = null;
			if (mParent != null && mParent.hasNext()) {
				PeriodTerm pTerm = mParent.next();
				CodeURI codeUri2 = new CodeURI(pTerm.codeUri);
				String label2 = pTerm.label;
				String lang2 = pTerm.lang != null ? pTerm.lang : null;
				parent = new Term(label2,
						lang2 != null ? Language.Lang.valueOf(lang2) : null,
						codeUri2, dbtable);
				if (pTerm.begin != null && pTerm.end != null) {
					parent.setProperty("begin", pTerm.begin);
					parent.setProperty("end", pTerm.end);
				}
			}
			Term term = new Term(label,
					lang != null ? Language.Lang.valueOf(lang) : null, codeUri,
					dbtable);
			if (parent != null) {
				term.setParent(parent);
			}
			if (mTerm.begin != null && mTerm.end != null) {
				term.setProperty("begin", mTerm.begin);
				term.setProperty("end", mTerm.end);
			}
			tList.add(term);
		}
		return tList;
	}

	/**
	 * Normalize method to deal with extension of MongoTerm
	 * 
	 * @param obj
	 * @param terms
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<DBRef<T, String>> normalize(T obj,
			List<DBRef<? extends MongoTerm, String>> terms) {
		List<DBRef<T, String>> norm = new ArrayList<DBRef<T, String>>();
		for (DBRef<? extends MongoTerm, String> ref : terms) {
			DBRef<T, String> refNew = (DBRef<T, String>) ref;
			norm.add(refNew);
		}
		return norm;
	}

	public static void prePopulateCache() {

	}

	/**
	 * Find terms by label
	 * 
	 * @param label
	 *            The label to search for
	 * @param dbtable
	 *            The table to search on
	 * @return The TermList that corresponds to the label
	 * @throws MalformedURLException
	 */

	public static  TermList findByLabel(String label, String dbtable)

			throws MalformedURLException {
		Map<String,TermList> typeMap= memCache.get(dbtable)!=null?memCache.get(dbtable):new ConcurrentHashMap<String,TermList>();
		if (typeMap.containsKey(label)) {
			return typeMap.get(label);
		}
		TermList tList = null;
		if (StringUtils.equals("people", dbtable)
				|| StringUtils.equals("concept", dbtable)) {
			tList = new TermList();
			JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
					.wrap(db.getCollection(dbtable), MongoTerm.class,
							String.class);
			pColl.ensureIndex("label");
			
			DBCursor<MongoTerm> curs = pColl.find().is("label", label);
			if (curs.hasNext()) {
				MongoTerm mTerm = curs.next();
				CodeURI codeUri = new CodeURI(mTerm.codeUri);
				String lang = mTerm.lang != null ? mTerm.lang : null;

				MongoTerm mParent = null;
				if (mTerm.parent != null) {
					mParent = pColl.findOneById(mTerm.parent.getId());
				}
				Term parent = null;
				if (mParent != null) {
					CodeURI codeUri2 = new CodeURI(mParent.codeUri);
					String label2 = mParent.originalLabel;
					String lang2 = mParent.lang != null ? mParent.lang : null;
					parent = new Term(label2,
							lang != null ? Language.Lang.valueOf(lang2) : null,
							codeUri2, dbtable);
				}
				Term term = new Term(label,
						lang != null ? Language.Lang.valueOf(lang) : null,
						codeUri, dbtable);
				if (parent != null) {
					term.setParent(parent);
				}

				tList.add(term);
			}
		} else if (StringUtils.equals("period", dbtable)) {
			tList = new TermList();
			JacksonDBCollection<PeriodTerm, String> pColl = JacksonDBCollection
					.wrap(db.getCollection(dbtable), PeriodTerm.class,
							String.class);
			pColl.ensureIndex("label");
			DBCursor<PeriodTerm> curs = pColl.find().is("label", label);
			if (curs.hasNext()) {
				PeriodTerm mTerm = curs.next();
				CodeURI codeUri = new CodeURI(mTerm.codeUri);
				String lang = mTerm.lang != null ? mTerm.lang : null;
				PeriodTerm mParent = null;
				if (mTerm.parent != null) {
					mParent = pColl.findOneById(mTerm.parent.getId());
				}
				Term parent = null;
				if (mParent != null) {
					CodeURI codeUri2 = new CodeURI(mParent.codeUri);
					String label2 = mParent.originalLabel;
					String lang2 = mParent.lang != null ? mParent.lang : null;
					parent = new Term(label2,
							lang != null ? Language.Lang.valueOf(lang2) : null,
							codeUri2, dbtable);
					parent.setProperty("begin", mParent.begin);
					parent.setProperty("end", mParent.end);
				}
				Term term = new Term(label,
						lang != null ? Language.Lang.valueOf(lang) : null,
						codeUri, dbtable);
				if (parent != null) {
					term.setParent(parent);
				}
				term.setProperty("begin", mTerm.begin);
				term.setProperty("end", mTerm.end);
				tList.add(term);
			}
		} else if (StringUtils.equals("place", dbtable)) {

			tList = new TermList();
			JacksonDBCollection<PlaceTerm, String> pColl = JacksonDBCollection
					.wrap(db.getCollection(dbtable), PlaceTerm.class,
							String.class);
			pColl.ensureIndex("label");
			DBCursor<PlaceTerm> curs = pColl.find().is("label", label);
			if (curs.hasNext()) {
				PlaceTerm mTerm = curs.next();
				CodeURI codeUri = new CodeURI(mTerm.codeUri);
				String lang = mTerm.lang != null ? mTerm.lang : null;
				PlaceTerm mParent = null;
				if (mTerm.parent != null) {
					mParent = pColl.findOneById(mTerm.parent.getId());
				}
				Term parent = null;
				if (mParent != null) {
					CodeURI codeUri2 = new CodeURI(mParent.codeUri);
					String label2 = mParent.originalLabel;
					String lang2 = mParent.lang != null ? mParent.lang : null;
					parent = new Term(label2,
							lang != null ? Language.Lang.valueOf(lang2) : null,
							codeUri2, dbtable);
					parent.setProperty("latitude", Float.toString(mParent.lat));
					parent.setProperty("longitude", Float.toString(mParent.lon));
				}
				Term term = new Term(label,
						lang != null ? Language.Lang.valueOf(lang) : null,
						codeUri, dbtable);
				if (parent != null) {
					term.setParent(parent);
				}
				term.setProperty("latitude", Float.toString(mTerm.lat));
				term.setProperty("longitude", Float.toString(mTerm.lon));
				tList.add(term);
			}
		}
			typeMap.put(label, tList);
			memCache.put(dbtable, typeMap);
			return tList;
	}

	/**
	 * Save time terms
	 * 
	 * @param voc
	 *            The time vocabulary to use
	 */
	private static void saveTimeTerms(VocabularyOfTime voc) {
		Iterable<TermList> tlList = voc.listAllByCode();
		int i = 0;
		for (TermList tl : tlList) {
			Term firstTerm = tl.getFirst();
			MongoTermList termList = new MongoTermList();
			List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();
			termList.setCodeUri(firstTerm.getCode());
			Iterator<Term> iter = tl.iterator();
			while (iter.hasNext()) {
				Term term = iter.next();
				PeriodTerm pTerm = new PeriodTerm();
				pTerm.codeUri = term.getCode();
				pTerm.label = term.getLabel().toLowerCase();
				pTerm.originalLabel = term.getLabel();
				if (term.getLang() != null) {
					pTerm.lang = term.getLang().getCode();
				}
				pTerm.begin = term.getProperty("begin");
				pTerm.end = term.getProperty("end");
				JacksonDBCollection<PeriodTerm, String> pColl = JacksonDBCollection
						.wrap(db.getCollection("period"), PeriodTerm.class,
								String.class);
				pColl.ensureIndex("codeUri");
				pColl.ensureIndex("label");
				PeriodTerm parentTerm = new PeriodTerm();
				Term parent = term.getParent();
				if (parent != null) {
					parentTerm.codeUri = parent.getCode();
					parentTerm.label = parent.getLabel().toLowerCase();
					parentTerm.originalLabel = parent.getLabel();
					if (parent.getLang() != null) {
						parentTerm.lang = parent.getLang().getCode();
					}
					parentTerm.begin = parent.getProperty("begin");
					parentTerm.end = parent.getProperty("end");
					DBRef<PeriodTerm, String> parentTermRef;
					if (pColl.find().is("codeUri", parentTerm.codeUri)
							.hasNext()) {
						parentTermRef = new DBRef<PeriodTerm, String>(pColl
								.find().is("codeUri", parentTerm.codeUri)
								.next().id, "period");
					} else {
						WriteResult<PeriodTerm, String> resP = pColl
								.insert(parentTerm);
						parentTermRef = new DBRef<PeriodTerm, String>(
								resP.getSavedObject().id, "period");
					}
					pTerm.parent = parentTermRef;
				}
				WriteResult<PeriodTerm, String> res = pColl.insert(pTerm);
				DBRef<PeriodTerm, String> pTermRef = new DBRef<PeriodTerm, String>(
						res.getSavedObject().id, "period");
				pList.add(pTermRef);
			}
			termList.setTerms(pList);
			coll.insert(termList);
		}

	}

	/**
	 * Save place terms in MongoDB
	 * 
	 * @param voc
	 *            The place vocabulary instance
	 */
	private static void savePlaceTerms(VocabularyOfPlaces voc) {
		Iterable<TermList> tlList = voc.listAllByCode();
		int i = 0;
		for (TermList tl : tlList) {
			Term firstTerm = tl.getFirst();
			MongoTermList termList = new MongoTermList();
			List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();
			termList.setCodeUri(firstTerm.getCode());
			Iterator<Term> iter = tl.iterator();
			while (iter.hasNext()) {
				Term term = iter.next();
				PlaceTerm pTerm = new PlaceTerm();
				pTerm.codeUri = term.getCode();
				pTerm.label = term.getLabel().toLowerCase();
				pTerm.originalLabel = term.getLabel();
				if (term.getLang() != null) {
					pTerm.lang = term.getLang().getCode();
				}
				if (!StringUtils.endsWith(term.getProperty("division"),
						"A.PCLI")) {
					pTerm.lat = Float.parseFloat(term.getProperty("latitude"));
					pTerm.lon = Float.parseFloat(term.getProperty("longitude"));
				}
				JacksonDBCollection<PlaceTerm, String> pColl = JacksonDBCollection
						.wrap(db.getCollection("place"), PlaceTerm.class,
								String.class);
				pColl.ensureIndex("codeUri");
				pColl.ensureIndex("label");
				PlaceTerm parentTerm = new PlaceTerm();
				Term parent = term.getParent();
				if (parent != null) {
					parentTerm.codeUri = parent.getCode();
					parentTerm.label = parent.getLabel().toLowerCase();
					parentTerm.originalLabel = parent.getLabel();
					if (parent.getLang() != null) {
						parentTerm.lang = parent.getLang().getCode();
					}
					if (!StringUtils.endsWith(parent.getProperty("division"),
							"A.PCLI")) {
						parentTerm.lat = Float.parseFloat(parent
								.getProperty("latitude"));
						parentTerm.lon = Float.parseFloat(parent
								.getProperty("longitude"));
					}
					DBRef<PlaceTerm, String> parentTermRef;
					if (pColl.find().is("codeUri", parentTerm.codeUri)
							.hasNext()) {
						parentTermRef = new DBRef<PlaceTerm, String>(pColl
								.find().is("codeUri", parentTerm.codeUri)
								.next().id, "place");
					} else {
						WriteResult<PlaceTerm, String> resP = pColl
								.insert(parentTerm);
						parentTermRef = new DBRef<PlaceTerm, String>(
								resP.getSavedObject().id, "place");
					}
					pTerm.parent = parentTermRef;
				}
				WriteResult<PlaceTerm, String> res = pColl.insert(pTerm);
				DBRef<PlaceTerm, String> pTermRef = new DBRef<PlaceTerm, String>(
						res.getSavedObject().id, "place");
				pList.add(pTermRef);
			}

			termList.setTerms(pList);
			coll.insert(termList);
		}

	}

	/**
	 * Save Concept and Agent Terms with their parents in the TermList, people
	 * and concept DB
	 * 
	 * @param voc
	 *            Vocabulary to use
	 * @param collection
	 *            The collection to save to
	 */
	private static void saveTerms(AbstractVocabulary voc, String collection) {
		// Get all terms by code
		Iterable<TermList> tlList = voc.listAllByCode();
		int i = 0;
		// For each term list
		for (TermList tl : tlList) {
			// Get the first tirm to create the searchable uri
			Term firstTerm = tl.getFirst();
			// Create the mongo term list object
			MongoTermList termList = new MongoTermList();
			// Create the list of references to terms
			List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();
			termList.setCodeUri(firstTerm.getCode());
			Iterator<Term> iter = tl.iterator();
			while (iter.hasNext()) {
				Term term = iter.next();
				MongoTerm pTerm = new MongoTerm();
				pTerm.codeUri = term.getCode();
				pTerm.label = term.getLabel().toLowerCase();
				pTerm.originalLabel = term.getLabel();
				if (term.getLang() != null) {
					pTerm.lang = term.getLang().getCode();
				}

				MongoTerm parentTerm = new MongoTerm();
				Term parent = term.getParent();
				JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
						.wrap(db.getCollection(collection), MongoTerm.class,
								String.class);
				pColl.ensureIndex("codeUri");
				pColl.ensureIndex("label");
				if (parent != null) {
					parentTerm.codeUri = parent.getCode();
					parentTerm.label = parent.getLabel().toLowerCase();
					parentTerm.originalLabel = parent.getLabel();
					if (parent.getLang() != null) {
						parentTerm.lang = parent.getLang().getCode();
					}

					DBRef<MongoTerm, String> parentTermRef;
					if (pColl.find().is("codeUri", parentTerm.codeUri)
							.hasNext()) {
						parentTermRef = new DBRef<MongoTerm, String>(pColl
								.find().is("codeUri", parentTerm.codeUri)
								.next().id, collection);
					} else {
						WriteResult<MongoTerm, String> resP = pColl
								.insert(parentTerm);
						parentTermRef = new DBRef<MongoTerm, String>(
								resP.getSavedObject().id, collection);
					}
					pTerm.parent = parentTermRef;
				}
				WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
				DBRef<MongoTerm, String> pTermRef = new DBRef<MongoTerm, String>(
						res.getSavedObject().id, collection);
				pList.add(pTermRef);
			}
			termList.setTerms(pList);
			coll.insert(termList);
		}
	}
}

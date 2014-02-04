package eu.europeana.enrichment.utils;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.common.Language.Lang;
import eu.europeana.enrichment.tagger.terms.CodeURI;
import eu.europeana.enrichment.tagger.terms.Term;
import eu.europeana.enrichment.tagger.terms.TermList;
import eu.europeana.enrichment.tagger.vocabularies.AbstractVocabulary;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPeople;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPlaces;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTime;

public class DatabaseUtils {
	//IT IS PAINFULLY SLOW
	static Connection connection;
	private final static String connectionURL = "jdbc:derby:annocultor_db;create=false;";
	private final static String creationURL = "jdbc:derby:annocultor_db;create=true;";
	private final static String REL = "rel";
	private final static String TL = "tl";
	private static String tableCreationPrefix = "CREATE TABLE ";
	private static String tableCreationPostfix = " (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), term VARCHAR(100) NOT NULL"
			+ ", label VARCHAR(150) NOT NULL, lang VARCHAR(3))";
	private static String tablePlaceCreationPostfix = " (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), term VARCHAR(100) NOT NULL"
			+ ", label VARCHAR(100) NOT NULL, lang VARCHAR(3), lat FLOAT, long FLOAT)";
	private static String tableTimeCreationPostfix = " (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), term VARCHAR(100) NOT NULL"
			+ ", label VARCHAR(100) NOT NULL, lang VARCHAR(3), beg VARCHAR(50), en VARCHAR(50))";
	private static String relationTableCreationPostfix = " (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),tid INTEGER NOT NULL, pid INTEGER NOT NULL)";
	private static String insertPrefix = "INSERT INTO ";
	private static String insertRecordPostfix = " (term,label,lang) VALUES(?,?,?)";
	private static String insertPlaceRecordPostfix = " (term,label,lang,lat,long) VALUES(?,?,?,?,?)";
	private static String insertTimeRecordPostfix = " (term,label,lang,beg,en) VALUES(?,?,?,?,?)";
	private static String insertRelationPostfix = " (tid,pid) VALUES(?,?)";
	private static String tableTermListPostfix = " (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), termlisturi VARCHAR(100) NOT NULL, tid INTEGER NOT NULL)";
	private static String insertTermListPostfix = " (termlisturi,tid) VALUES(?,?)";

	static {
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if DB exists
	 * 
	 * @return true or false
	 * @throws SQLException
	 */
	public static boolean dbExists() throws SQLException {

		try {
			connection = DriverManager.getConnection(connectionURL);
			return true;
		} catch (SQLException e) {
			connection = DriverManager.getConnection(creationURL);
		}
		return false;
	}

	/**
	 * Create a db with the map information
	 * 
	 * @param dbtable
	 *            one table per Map
	 * @param vocabulary
	 *            The vocabulary to save
	 * @throws SQLException
	 */
	public static void save(String dbtable, AbstractVocabulary vocabulary)
			throws SQLException {
		connection = DriverManager.getConnection(connectionURL);
		Statement stmt = connection.createStatement();
		if (StringUtils.equals(dbtable, "place")) {
			System.out.println(tableCreationPrefix + dbtable
					+ tablePlaceCreationPostfix);
			stmt.executeUpdate(tableCreationPrefix + dbtable
					+ tablePlaceCreationPostfix);
		} else if (StringUtils.equals(dbtable, "period")) {
			System.out.println(tableCreationPrefix + dbtable
					+ tableTimeCreationPostfix);
			stmt.executeUpdate(tableCreationPrefix + dbtable
					+ tableTimeCreationPostfix);
			System.out.println(tableCreationPrefix + dbtable + TL
					+ tableTermListPostfix);
		} else {
			stmt.executeUpdate(tableCreationPrefix + dbtable
					+ tableCreationPostfix);
		}
		System.out.println(tableCreationPrefix + dbtable + REL
				+ relationTableCreationPostfix);
		
		stmt.executeUpdate(tableCreationPrefix + dbtable + REL
				+ relationTableCreationPostfix);
		System.out.println(tableCreationPrefix + dbtable + TL
				+ tableTermListPostfix);
		stmt.executeUpdate(tableCreationPrefix + dbtable + TL
				+ tableTermListPostfix);
		if (vocabulary instanceof VocabularyOfPeople) {
			Iterable<TermList> iterator = ((VocabularyOfPeople) vocabulary)
					.listAllByCode();
			
			for (TermList termList : iterator) {
				Term first = termList.getFirst();
				String termListUri = first.getCode();
				Iterator<Term> iter = termList.iterator();
				while (iter.hasNext()) {
					Term term = iter.next();
					int tid = createTerm(term, dbtable);
					int pid = createTerm(term.getParent(), dbtable);
					PreparedStatement ps = connection
							.prepareStatement(insertPrefix + dbtable + REL
									+ insertRelationPostfix);
					ps.setInt(1, tid);
					ps.setInt(2, pid);
					ps.executeUpdate();
					PreparedStatement ps2 = connection
							.prepareStatement(insertPrefix + dbtable + TL
									+ insertTermListPostfix);
					ps2.setString(1, termListUri);
					ps2.setInt(2, tid);
					ps2.executeUpdate();
				}
			}
		} else if (vocabulary instanceof VocabularyOfPlaces) {
			Iterable<TermList> iterator = ((VocabularyOfPlaces) vocabulary)
					.listAllByCode();
			for (TermList termList : iterator) {
				Term first = termList.getFirst();
				String termListUri = first.getCode();
				Iterator<Term> iter = termList.iterator();
				while (iter.hasNext()) {
					Term term = iter.next();
					int tid = createPlaceTerm(term, dbtable);
					int pid = createPlaceTerm(term.getParent(), dbtable);
					PreparedStatement ps = connection
							.prepareStatement(insertPrefix + dbtable + REL
									+ insertRelationPostfix);
					ps.setInt(1, tid);
					ps.setInt(2, pid);
					ps.executeUpdate();
					PreparedStatement ps2 = connection
							.prepareStatement(insertPrefix + dbtable + TL
									+ insertTermListPostfix);
					ps2.setString(1, termListUri);
					ps2.setInt(2, tid);
					ps2.executeUpdate();
				}
			}
		} else if (vocabulary instanceof VocabularyOfTerms) {
			Iterable<TermList> iterator = ((VocabularyOfTerms) vocabulary)
					.listAllByCode();
			System.out.println(vocabulary.codeSet().size());
			int i=0;
			for (TermList termList : iterator) {
				i++;
				System.out.println(i);
				Term first = termList.getFirst();
				String termListUri = first.getCode();
				Iterator<Term> iter = termList.iterator();
				while (iter.hasNext()) {
					Term term = iter.next();
					int tid = createTerm(term, dbtable);
					if(term.getParent()!=null){
					int pid = createTerm(term.getParent(), dbtable);
					if (pid > 0) {
						PreparedStatement ps = connection
								.prepareStatement(insertPrefix + dbtable + REL
										+ insertRelationPostfix);
						ps.setInt(1, tid);
						ps.setInt(2, pid);
						ps.executeUpdate();
					}
					}
					PreparedStatement ps2 = connection
							.prepareStatement(insertPrefix + dbtable + TL
									+ insertTermListPostfix);
					ps2.setString(1, termListUri);
					ps2.setInt(2, tid);
					ps2.executeUpdate();
				}
			}
		} else if (vocabulary instanceof VocabularyOfTime) {
			Iterable<TermList> iterator = ((VocabularyOfTime) vocabulary)
					.listAllByCode();
			for (TermList termList : iterator) {
				Term first = termList.getFirst();
				String termListUri = first.getCode();
				Iterator<Term> iter = termList.iterator();
				while (iter.hasNext()) {
					Term term = iter.next();
					int tid = createTimeTerm(term, dbtable);
					
					int pid = createTimeTerm(term.getParent(), dbtable);
					if(pid>0){
					PreparedStatement ps = connection
							.prepareStatement(insertPrefix + dbtable + REL
									+ insertRelationPostfix);
					ps.setInt(1, tid);
					ps.setInt(2, pid);
					ps.executeUpdate();
					}
					PreparedStatement ps2 = connection
							.prepareStatement(insertPrefix + dbtable + TL
									+ insertTermListPostfix);
					ps2.setString(1, termListUri);
					ps2.setInt(2, tid);
					ps2.executeUpdate();
				}
			}
		}
	}

	/**
	 * Create a term in the db
	 * 
	 * @param term
	 *            the term to create typically consisting of codeuir, label and
	 *            optionally lang
	 * @param dbtable
	 *            the table to save to
	 * @return the generated id
	 * @throws SQLException
	 */
	private static int createTerm(Term term, String dbtable)
			throws SQLException {
		String query = insertPrefix + dbtable + insertRecordPostfix;
		PreparedStatement ps = connection.prepareStatement(query,
				Statement.RETURN_GENERATED_KEYS);
		if(term!=null){
		ps.setString(1, term.getCode());
		ps.setString(2, term.getLabel());
		if (term.getLang() != null) {
			ps.setString(3, term.getLang().getCode());
		} else {
			ps.setString(3, null);
		}
		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
		}
		return 0;
	}

	private static int createPlaceTerm(Term term, String dbtable)
			throws SQLException {
		String query = insertPrefix + dbtable + insertPlaceRecordPostfix;
		PreparedStatement ps = connection.prepareStatement(query,
				Statement.RETURN_GENERATED_KEYS);
		if (term != null) {
			ps.setString(1, term.getCode());
			ps.setString(2, term.getLabel());

			if (term.getLang() != null) {
				ps.setString(3, term.getLang().getCode());
			} else {
				ps.setString(3, null);
			}

			if (!StringUtils.endsWith(term.getProperty("division"), "A.PCLI")) {
				ps.setFloat(4, Float.parseFloat(term.getProperty("latitude")));
				ps.setFloat(5, Float.parseFloat(term.getProperty("longitude")));
			} else {
				ps.setFloat(4, 0f);
				ps.setFloat(5, 0f);
			}
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			rs.next();
			return rs.getInt(1);
		}
		return 0;
	}

	private static int createTimeTerm(Term term, String dbtable)
			throws SQLException {
		String query = insertPrefix + dbtable + insertTimeRecordPostfix;
		PreparedStatement ps = connection.prepareStatement(query,
				Statement.RETURN_GENERATED_KEYS);
		if(term!=null){
		ps.setString(1, term.getCode());
		ps.setString(2, term.getLabel());

		if (term.getLang() != null) {
			ps.setString(3, term.getLang().getCode());
		} else {
			ps.setString(3, null);
		}
		ps.setString(4, term.getProperty("begin"));
		ps.setString(5, term.getProperty("end"));
		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		
		return rs.getInt(1);
		}
		return 0;
	}

	/**
	 * Mock the same functionality as in Annocultor memory based implementation
	 * 
	 * @param codeuri
	 *            the code to search
	 * @return A termlist with
	 * @throws SQLException
	 * @throws MalformedURLException
	 */

	public static TermList findByCode(CodeURI codeuri, String dbtable)
			throws SQLException, MalformedURLException {
		TermList termList = new TermList();
		String q1 = "SELECT * FROM " + dbtable + TL
				+ " INNER JOIN (SELECT * FROM " + dbtable
				+ ") as t1 ON t1.id = " + dbtable + TL + ".tid";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(q1);
		while (rs.next()) {
			String label = rs.getString("label");
			Lang lang = rs.getString("lang") != null ? Lang.valueOf(rs
					.getString("lang")) : null;
			CodeURI codeUri = new CodeURI(rs.getString("term"));
			Term term = new Term(label, lang, codeUri, dbtable);
			String q2 = "SELECT * FROM " + dbtable + REL
					+ " INNER JOIN (SELECT * FROM " + dbtable
					+ ") as t1 ON t1.id = " + dbtable + REL + ".pid";
			ResultSet rs2 = st.executeQuery(q2);
			while (rs2.next()) {
				String label2 = rs.getString("label");
				Lang lang2 = rs2.getString("lang") != null ? Lang.valueOf(rs2
						.getString("lang")) : null;
				CodeURI codeUri2 = new CodeURI(rs2.getString("term"));
				Term parent = new Term(label2, lang2, codeUri2, dbtable);
				if (StringUtils.equals(dbtable, "place")) {
					term.setProperty("lat", Float.toString(rs2.getFloat("lat")));
					term.setProperty("long",
							Float.toString(rs2.getFloat("long")));
				}
				if (StringUtils.equals(dbtable, "period")) {
					term.setProperty("begin", rs2.getString("beg"));
					term.setProperty("end", rs2.getString("en"));
				}
				term.setParent(parent);
			}

			if (StringUtils.equals(dbtable, "place")) {
				term.setProperty("lat", Float.toString(rs.getFloat("lat")));
				term.setProperty("long", Float.toString(rs.getFloat("long")));
			}
			if (StringUtils.equals(dbtable, "period")) {
				term.setProperty("begin", rs.getString("beg"));
				term.setProperty("end", rs.getString("en"));
			}
			termList.add(term);
		}

		return termList;
	}
	
	public static TermList findByLabel(String label, String dbtable) throws SQLException, MalformedURLException{
		TermList termList = new TermList();
		String q1 = "SELECT * FROM " + dbtable + " WHERE label = '"+label+"'";
		Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = st.executeQuery(q1);
		
		while (rs.next()) {
			String label2 = rs.getString("label");
			Lang lang = rs.getString("lang") != null ? Lang.valueOf(rs
					.getString("lang")) : null;
			CodeURI codeUri = new CodeURI(rs.getString("term"));
			Term term = new Term(label2, lang, codeUri, dbtable);
			String q2 = "SELECT * FROM " + dbtable + REL
					+ " INNER JOIN (SELECT * FROM " + dbtable
					+ ") as t1 ON t1.id = " + dbtable + REL + ".pid";
			ResultSet rs2 = st.executeQuery(q2);
			
			rs2.last();
			
			System.out.println(rs2.getRow());
			rs2.beforeFirst();
			while (rs2.next()) {
				
				String label3 = rs2.getString("label");
				Lang lang2 = rs2.getString("lang") != null ? Lang.valueOf(rs2
						.getString("lang")) : null;
				CodeURI codeUri2 = new CodeURI(rs2.getString("term"));
				Term parent = new Term(label3, lang2, codeUri2, dbtable);
				if (StringUtils.equals(dbtable, "place")) {
					term.setProperty("lat", Float.toString(rs2.getFloat("lat")));
					term.setProperty("long",
							Float.toString(rs2.getFloat("long")));
				}
				if (StringUtils.equals(dbtable, "period")) {
					term.setProperty("begin", rs2.getString("beg"));
					term.setProperty("end", rs2.getString("en"));
				}
				term.setParent(parent);
			}

			if (StringUtils.equals(dbtable, "place")) {
				term.setProperty("lat", Float.toString(rs.getFloat("lat")));
				term.setProperty("long", Float.toString(rs.getFloat("long")));
			}
			if (StringUtils.equals(dbtable, "period")) {
				term.setProperty("begin", rs.getString("beg"));
				term.setProperty("end", rs.getString("en"));
			}
			termList.add(term);
		}

		return termList;
	}
}

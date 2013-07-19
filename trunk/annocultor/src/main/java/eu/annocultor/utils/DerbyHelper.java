/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.annocultor.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

import eu.annocultor.common.Helper;
import eu.annocultor.context.Namespaces;
import eu.annocultor.reports.ReporterImpl.TermCounter;

/**
 * 
 * @author Borys Omelayenko
 * 
 */
public class DerbyHelper
{

	public static void main(String[] args) throws Exception
	{
		ConversionResultsDb.createConverterTables("E:/DerbyDB/convert");
		Repository rdf = Helper.createLocalRepository();
		RepositoryConnection con = rdf.getConnection();
		con.add(new File("E:\\develop\\eculture\\collections\\bibliopolis\\rdf\\terms.rdf"), Namespaces.NS
				.toString(), RDFFormat.RDFXML);
		// writeRdfToDb("E:/DerbyDB/convert", "bibliopolis", rdf);
	}

	public static class ConversionResultsDb
	{

		public static void createConverterTables(String dbPath) throws Exception
		{
			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
			Class.forName(driver);

			String connectionURL = "jdbc:derby:" + dbPath + ";create=true";
			Connection conn = DriverManager.getConnection(connectionURL);
			Statement st = conn.createStatement();

			try
			{
				st.executeUpdate("DROP TABLE RELEASES");
				st.executeUpdate("DROP TABLE TRIPLES");
				st.executeUpdate("DROP TABLE TRANSACTIONS");
				st.executeUpdate("DROP TABLE DATASETS");
				// st.executeUpdate("DROP INDEX TRIPLE_HASH_IDX");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				st.executeUpdate("CREATE TABLE DATASETS ("
					+ "DS_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT DATASET_PK PRIMARY KEY, "
					+ "DATASET VARCHAR(32) NOT NULL, SUBSET VARCHAR(32) NOT NULL)");
				st.executeUpdate("CREATE TABLE TRANSACTIONS ("
					+ "TRANS_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT TRANS_PK PRIMARY KEY, "
					+ "DS_ID INTEGER NOT NULL, FOREIGN KEY (DS_ID) REFERENCES DATASETS(DS_ID), "
					+ "DATE TIMESTAMP NOT NULL, "
					+ "USER_ID INTEGER)");
				st
						.executeUpdate("CREATE TABLE TRIPLES ("
							+ "TRANS_ID INTEGER NOT NULL, FOREIGN KEY (TRANS_ID) REFERENCES TRANSACTIONS(TRANS_ID), "
							+ "TRIPLE_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT TRIPLE_PK PRIMARY KEY, "
							+ "S VARCHAR(150) NOT NULL, P VARCHAR(150) NOT NULL, P_TYPE CHAR NOT NULL, V VARCHAR(5000) NOT NULL, LANG VARCHAR(2), "
							+ "HASH INTEGER NOT NULL)");
				st.executeUpdate("CREATE INDEX TRIPLE_HASH_IDX ON TRIPLES(HASH)");
				st
						.executeUpdate("CREATE TABLE RELEASES ("
							+ "TRANS_ID INTEGER NOT NULL CONSTRAINT RELEASE_PK PRIMARY KEY, FOREIGN KEY (TRANS_ID) REFERENCES TRANSACTIONS(TRANS_ID),"
							+ "COMMENT VARCHAR(256))");
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
			finally
			{
				conn.close();
			}
		}

		public static int writeRdfToDb(String dbPath, String dataset, String subset, Repository rdf)
				throws Exception
		{
			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
			try
			{
				Class.forName(driver);
			}
			catch (java.lang.ClassNotFoundException e)
			{
				e.printStackTrace();
			}

			String connectionURL = "jdbc:derby:" + dbPath;
			Connection conn = DriverManager.getConnection(connectionURL);

			try
			{
				// dataset id
				Statement stmt = conn.createStatement();
				ResultSet rs =
						stmt
								.executeQuery(String
										.format("SELECT DS_ID FROM DATASETS where DATASET = '%s' and SUBSET = '%s'",
												dataset,
												subset));
				if (!rs.next())
				{
					// new dataset needed
					PreparedStatement st = conn.prepareStatement("INSERT INTO DATASETS (DATASET, SUBSET) VALUES (?,?)");
					st.setString(1, dataset);
					st.setString(2, subset);
					st.executeUpdate();
				}
				rs.close();
			}
			catch (Exception e)
			{
				// may happen :(
				System.out.printf("Info: Dataset <%s, %s> already in db (%s)", dataset, subset, e.getMessage());
			}

			int lastTransactionId = 0;
			try
			{
				// dataset id
				Statement stmt = conn.createStatement();
				ResultSet rs =
						stmt.executeQuery(String
								.format("SELECT MAX(DS_ID) FROM DATASETS where DATASET = '%s' and SUBSET = '%s'",
										dataset,
										subset));
				rs.next();
				int datasetId = rs.getInt(1);
				rs.close();

				// transaction
				PreparedStatement st =
						conn.prepareStatement("INSERT INTO TRANSACTIONS (DATE, DS_ID, USER_ID) VALUES (?,?,?)");
				st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				st.setInt(2, datasetId);
				st.setInt(3, 0);
				st.executeUpdate();

				// last id
				stmt = conn.createStatement();
				rs =
						stmt.executeQuery(String.format("SELECT MAX(TRANS_ID) FROM TRANSACTIONS where DS_ID = %d",
								datasetId));
				rs.next();
				lastTransactionId = rs.getInt(1);
				rs.close();

				// triples
				conn.setAutoCommit(false);
				RepositoryConnection con = rdf.getConnection();
				RepositoryResult<org.openrdf.model.Statement> statements = con.getStatements(null, null, null, false);
				try
				{
					while (statements.hasNext())
					{
						org.openrdf.model.Statement triple = statements.next();
						st =
								conn
										.prepareStatement("INSERT INTO TRIPLES (TRANS_ID, S, P, P_TYPE, V, LANG, HASH) VALUES (?,?,?,?,?,?,?)");
						st.setInt(1, lastTransactionId);
						st.setString(2, triple.getSubject().toString());
						st.setString(3, triple.getPredicate().toString());
						String type = (triple.getObject() instanceof Resource) ? "R" : "L";
						st.setString(4, type);
						st.setString(5, triple.getObject().toString());
						String lang = null;
						if (type.equals("L"))
							lang = ((Literal) triple.getObject()).getLanguage();
						st.setString(6, lang);
						st.setInt(7, triple.hashCode());
						st.executeUpdate();
					}
				}
				finally
				{
					statements.close();
				}
				conn.commit();
				conn.setAutoCommit(true);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
			finally
			{
				conn.close();
			}
			try
			{
				// DriverManager.getConnection("jdbc:derby:;shutdown=true");
			}
			finally
			{
				//
			}
			return lastTransactionId;
		}

	}

	public static class ConversionReportsDb
	{

		private static final int MAX_VALUES_STORED_PER_COUNTER = 300;
		private static final String TERM_FOR_SKIPPED_TERMS = "*OTHER TERMS*";

		public static void closeReporterDB(String dbPath) throws Exception
		{
			try
			{
				DriverManager.getConnection("jdbc:derby:" + dbPath + "/reporter;shutdown=true");
			}
			catch (Exception e)
			{
				if (!e.getMessage().contains("shutdown"))
					throw e;
			}
		}

		public static void createReporterTables(String dbPath) throws Exception
		{
			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
			Class.forName(driver);

			String connectionURL = "jdbc:derby:" + dbPath + "/reporter;user=reporter;password=none;create=true";
			Connection conn = DriverManager.getConnection(connectionURL);
			Statement st = conn.createStatement();

			dropTable(st, "DROP TABLE QUERY_PROPERTIES");
			dropTable(st, "DROP TABLE RESULT_PROPERTIES");
			dropTable(st, "DROP TABLE TERMS");
			dropTable(st, "DROP TABLE REPORTS");
			dropTable(st, "DROP TABLE TRANSACTS");
			dropTable(st, "DROP TABLE TERM_STRINGS");
			dropTable(st, "DROP TABLE TMP_TABLES");
			dropTable(st, "DROP INDEX TERMS_STRINGS_IDX");

			try
			{
				st.executeUpdate("CREATE TABLE TMP_TABLES ("
					+ "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT TT_PK PRIMARY KEY) ");
				st.executeUpdate("CREATE TABLE TRANSACTS ("
					+ "TR_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT TR_PK PRIMARY KEY, "
					+ "DATASET VARCHAR(64) NOT NULL, "
					+ "AUSER VARCHAR(64),"
					+ "ISLAST CHAR, "
					+ "DATE TIMESTAMP NOT NULL)");
				st.executeUpdate("CREATE TABLE REPORTS ("
					+ "R_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT R_PK PRIMARY KEY, "
					+ "TRANS INTEGER NOT NULL, FOREIGN KEY (TRANS) REFERENCES TRANSACTS(TR_ID), "
					+ "TARGET VARCHAR(64) NOT NULL,"
					+ "RULE VARCHAR(64),"
					+ "CATEGORY VARCHAR(64)," // PROPERTY
					+ "SUBCATEGORY VARCHAR(64)," // VOCABULARY
					+ "COUNTER VARCHAR(128))" // e.g. mapped, converted, skipped,
					// ignored
				);
				st.executeUpdate("CREATE TABLE TERM_STRINGS ("
					+ "STRING_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY, "
					+ "TERM VARCHAR(128) NOT NULL, "
					+ "PRIMARY KEY (STRING_ID, TERM))");
				st.executeUpdate("CREATE TABLE TERMS ("
					+ "T_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT T_PR PRIMARY KEY, "
					+ "R_ID INTEGER NOT NULL, FOREIGN KEY (R_ID) REFERENCES REPORTS(R_ID), "
					+ "TERM_ID INTEGER NOT NULL,"
					+ "COUNT INTEGER, "
					+ "CONSTRAINT FK UNIQUE (T_ID, R_ID, TERM_ID))");
				st.executeUpdate("CREATE TABLE QUERY_PROPERTIES ("
					+ "QP_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT QP_PK PRIMARY KEY, "
					+ "T_ID INTEGER NOT NULL, FOREIGN KEY (T_ID) REFERENCES TERMS(T_ID), "
					+ "PROPERTY VARCHAR(32) NOT NULL,"
					+ "VALUE VARCHAR(32) NOT NULL)");
				st.executeUpdate("CREATE TABLE RESULT_PROPERTIES ("
					+ "RP_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT RP_PK PRIMARY KEY, "
					+ "T_ID INTEGER NOT NULL, FOREIGN KEY (T_ID) REFERENCES TERMS(T_ID), "
					+ "PROPERTY VARCHAR(32) NOT NULL,"
					+ "VALUE VARCHAR(128) NOT NULL)");
				st.executeUpdate("CREATE INDEX TERMS_STRINGS_IDX ON TERM_STRINGS (TERM)");
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
			finally
			{
				conn.close();
			}
		}

		private static String sanitizeTermString(String term)
		{
			// sanitize for SQL
			StringBuilder cleanTerm = new StringBuilder();
			for (int i = 0; i < term.length(); i++)
			{
				char c = term.charAt(i);
				if (c == '\'')
					cleanTerm.append("''");
				else
					cleanTerm.append(c);
				if (i == 120)
				{
					cleanTerm.append("...");
					break;
				}
			}
			return cleanTerm.toString();
		}

		public static void writeCounterToDb(
				String dbPath,
				Long transactionId,
				String target,
				String writer,
				String categoryOrContextProperty,
				String subCategoryOrVocabulary,
				String counterName,
				List<TermCounter> termCounters) throws Exception
		{
			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
			Class.forName(driver);

			String connectionURL = "jdbc:derby:" + dbPath + "/reporter;user=reporter;password=none";
			Connection conn = DriverManager.getConnection(connectionURL);

			System.out.print("Started: "
				+ writer
				+ "#"
				+ categoryOrContextProperty
				+ "#"
				+ subCategoryOrVocabulary
				+ "#"
				+ counterName
				+ ", total: "
				+ termCounters.size());
			// reports
			Statement stmt = conn.createStatement();
			PreparedStatement st =
					conn.prepareStatement("INSERT INTO REPORTS (TRANS, TARGET, RULE, CATEGORY, SUBCATEGORY, COUNTER)"
						+ "VALUES (?,?,?,?,?,?)");
			st.setLong(1, transactionId);
			st.setString(2, target);
			st.setString(3, writer);
			st.setString(4, categoryOrContextProperty);
			st.setString(5, subCategoryOrVocabulary);
			st.setString(6, counterName);
			st.executeUpdate();
			st.close();

			// find last report id
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(R_ID) FROM REPORTS");
			rs.next();
			long lastReportId = rs.getInt(1);
			rs.close();

			// terms
			conn.setAutoCommit(false);

			// loading all terms used here into a tmp table
			dropTable(st, "drop table TMP");
			st = conn.prepareStatement("create table TMP (TERM VARCHAR(128), PRIMARY KEY (TERM))");
			st.executeUpdate();
			st.close();

			// local reduced copy of termCounters
			List<TermCounter> termCountersLimited = new LinkedList<TermCounter>();
			{
				long totalTermsPassed = 0;
				long skippedTermsCount = 0;
				for (TermCounter term : termCounters)
				{
					if (totalTermsPassed < MAX_VALUES_STORED_PER_COUNTER)
					{
						totalTermsPassed++;
						termCountersLimited.add(term);
					}
					else
					{
						skippedTermsCount += term.getCount();
					}
				}
				if (skippedTermsCount > 0)
					termCountersLimited.add(new TermCounter(TERM_FOR_SKIPPED_TERMS, skippedTermsCount));
			}

			Set<String> sanitizedStrings = new HashSet<String>();
			st = conn.prepareStatement("insert into TMP (TERM) values (?)");
			for (TermCounter entry : termCountersLimited)
			{
				String term = sanitizeTermString(entry.getTerm());
				// strings, truncated at sanitization, may duplicate
				if (!sanitizedStrings.contains(term))
				{
					st.setString(1, term);
					st.addBatch();
				}
				sanitizedStrings.add(term);
			}
			st.executeBatch();
			st.close();

			// merging the tmp table with all terms used here
			// with the terms already in db
			st =
					conn
							.prepareStatement("insert into TERM_STRINGS (TERM) "
								+ "select TERM "
								+ "from TMP as RT where "
								+ "not exists (select TERM_STRINGS.TERM from TMP, TERM_STRINGS where TERM_STRINGS.TERM = TMP.TERM and TMP.TERM = RT.TERM)");
			st.executeUpdate();
			st.close();
			st = conn.prepareStatement("drop table TMP");
			st.executeUpdate();
			st.close();
			conn.commit();

			// writing terms where term labels are replaced with codes
			st =
					conn.prepareStatement("INSERT INTO TERMS (R_ID, TERM_ID, COUNT) "
						+ "SELECT ?, STRING_ID, ? "
						+ "from TERM_STRINGS where "
						+ "TERM = ?");
			for (TermCounter entry : termCountersLimited)
			{
				String term = sanitizeTermString(entry.getTerm());
				st.setLong(1, lastReportId);
				st.setLong(2, entry.getCount());
				st.setString(3, term);
				st.addBatch();
			}
			st.executeBatch();
			conn.setAutoCommit(true);
			st.close();
			System.out.println("Committed. ");
			conn.close();
		}

		public static long writeTransactionToDb(String dbPath, String user, String dataset) throws Exception
		{
			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
			Class.forName(driver);

			String connectionURL = "jdbc:derby:" + dbPath + "/reporter;user=reporter;password=none";
			Connection conn = DriverManager.getConnection(connectionURL);

			// transactions
			Statement stmt = conn.createStatement();
			PreparedStatement st =
					conn.prepareStatement("UPDATE TRANSACTS SET ISLAST = 'F' WHERE DATASET = '" + dataset + "'");
			st.executeUpdate();

			stmt = conn.createStatement();
			st =
					conn
							.prepareStatement("INSERT INTO TRANSACTS (DATASET, AUSER, ISLAST, DATE)" + "VALUES (?,?,'T',?)");
			st.setString(1, dataset);
			st.setString(2, user);
			st.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
			st.executeUpdate();

			// find last transaction id
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(TR_ID) FROM TRANSACTS");
			rs.next();
			long lastTransactionId = rs.getInt(1);
			rs.close();

			return lastTransactionId;
		}

	}

	private static void dropTable(Statement st, String query)
	{
		try
		{
			st.executeUpdate(query);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
	}

	public static void computeDiff(String dbPath, String dataset, String subset, Repository rdf)
			throws Exception
	{
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		try
		{
			Class.forName(driver);
		}
		catch (java.lang.ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		String connectionURL = "jdbc:derby:" + dbPath;
		Connection conn = DriverManager.getConnection(connectionURL);
		// get transaction id of the last release
		Statement stmt = conn.createStatement();
		ResultSet rs =
				stmt
						.executeQuery(String
								.format("SELECT MAX(R.TRANS_ID) FROM DATASETS as D, RELEASES as R, TRANSACTIONS as T where D.DATASET = '%s' and D.SUBSET = '%s' "
									+ " and D.DS_ID = T.DS_ID and R.TRANS_ID = T.TRANS_ID",
										dataset,
										subset));
		rs.next();
		if (rs.getInt(1) == 0)
		{
			System.out.printf("Diff: Dataset <%s,%s> is not yet released, nothing to compare to.\n",
					dataset,
					subset);
			return;
		}
		int oldTransactionId = rs.getInt(1);
		rs.close();

		rs =
				stmt
						.executeQuery(String
								.format("SELECT MAX(T.TRANS_ID) FROM DATASETS as D, TRANSACTIONS as T where D.DATASET = '%s' and D.SUBSET = '%s' "
									+ " and D.DS_ID = T.DS_ID ",
										dataset,
										subset));
		rs.next();
		if (rs.getInt(1) == 0)
		{
			System.out.printf("Diff: Dataset <%s,%s> is not converted, no transaction.\n", dataset, subset);
			return;
		}
		int newTransactionId = rs.getInt(1);
		rs.close();

		try
		{
			System.out.println("Diff: Getting list of triple hashes");
			Statement st = conn.createStatement();
			// list of duplicates
			Set<Integer> duplicatedTripleHashes = new HashSet<Integer>();
			ResultSet rsDup =
					st.executeQuery(String.format("select D.HASH from TRIPLES as D, TRIPLES as N where "
						+ "D.TRANS_ID = %d and N.TRANS_ID = %d and D.HASH = N.HASH", oldTransactionId, newTransactionId));
			while (rsDup.next())
			{
				duplicatedTripleHashes.add(rsDup.getInt(1));
			}
			rsDup.close();

			System.out.println("Diff: Getting list of triples");
			rs =
					st.executeQuery(String.format("select TRANS_ID, S, P, P_TYPE, V, LANG, HASH from TRIPLES "
						+ "where TRANS_ID = %d or TRANS_ID = %d "
						+ " order by S, P, V", oldTransactionId, newTransactionId));

			while (rs.next())
			{
				if (!duplicatedTripleHashes.contains(rs.getInt("HASH")))
				{
					System.out.printf("%s %s-%s-%s \n", (rs.getInt("TRANS_ID") == oldTransactionId) ? "-" : "+", rs
							.getString("S"), rs.getString("P"), rs.getString("V"));
				}
			}
			rs.close();
		}
		finally
		{
			conn.close();
		}
	}
}

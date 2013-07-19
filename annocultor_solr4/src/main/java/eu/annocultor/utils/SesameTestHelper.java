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
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import eu.annocultor.api.ConverterTester;
import eu.annocultor.common.Helper;
import eu.annocultor.context.Namespaces;
import eu.annocultor.triple.Property;

/**
 * Wrap-up of <a href="http://openrdf.org">Sesame</a> repository to run tests.
 * 
 * @author Borys Omelayenko
 * 
 */
public class SesameTestHelper implements ConverterTester
{
	private Repository rdf;
	private RepositoryConnection connection;
	private ValueFactory vf;

	File[] files;
	File tmp;

	public SesameTestHelper(File tmp, File... files) throws Exception
	{
	}

	public String loadDataAndTest() throws Exception
	{
		rdf = Helper.createLocalRepository(tmp);
		vf = rdf.getValueFactory();
		connection = rdf.getConnection();
		if (files != null)
			Helper.importRDFXMLFile(rdf, Namespaces.NS.toString(), files);
		String result = null;
		try
		{
			result = testConversion();
		}
		finally
		{
			connection.close();
		}
		return result;
	}

	/**
	 * Post-conversion tests.
	 * 
	 * @return <code>null</code> if all tests are passed, or an error message.
	 */
	public String testConversion() throws Exception
	{

		String result = "";
		return result;
	}

	/**
	 * Checks if a triple is mentioned in the conversion results. Used in tests.
	 * 
	 * @return count this pattern occurred
	 */
	private RepositoryResult<Statement> isTripleIn(
			String subject,
			String property,
			String value,
			boolean isLiteral) throws RepositoryException
	{
		Resource subj = null;
		URI pred = null;
		Value val = null;

		if (subject != null)
			subj = vf.createURI(subject);
		if (property != null)
			pred = vf.createURI(property);
		if (value != null)
			if (isLiteral)
				val = vf.createLiteral(value);
			else
				val = vf.createURI(value);
		return connection.getStatements(subj, pred, val, false);
	}

	/**
	 * Tests if the triple is present in the results.
	 * 
	 * @param subject
	 *          null means 'all'
	 * @param property
	 *          null means 'all'
	 * @param value
	 *          null means 'all'
	 * @param isLiteral
	 * @return
	 */
	protected String assertPresence(String subject, String property, String value, boolean isLiteral)
			throws RepositoryException
	{
		return assertTriple(true, subject, property, value, isLiteral);
	}

	/**
	 * Tests if the triple is absent in the results.
	 * 
	 * @param subject
	 *          null means 'all'
	 * @param property
	 *          null means 'all'
	 * @param value
	 *          null means 'all'
	 * @param isLiteral
	 * @return
	 */
	protected String assertAbsence(String subject, String property, String value, boolean isLiteral)
			throws RepositoryException
	{
		return assertTriple(false, subject, property, value, isLiteral);
	}

	protected RepositoryResult<Statement> selectTriples(
			String subject,
			String property,
			String value,
			boolean isLiteral) throws RepositoryException
	{
		return isTripleIn(subject, property, value, isLiteral);
	}

	private String assertTriple(
			boolean shouldBeIn,
			String subject,
			String property,
			String value,
			boolean isLiteral) throws RepositoryException
	{
		String triple = "<" + subject + "," + property + "," + value + ">";
		if (shouldBeIn && isTripleIn(subject, property, value, isLiteral).hasNext())
			return "";
		if (!shouldBeIn && !isTripleIn(subject, property, value, isLiteral).hasNext())
			return "";

		if (shouldBeIn)
			return "Missed expected triple " + triple + " \n";
		else
			return "Unexpected triple " + triple + " \n";
	}

	public String checkTraces(Property relationToCheck, Property relationToFind, String message)
			throws Exception
	{
		String query =
				"SELECT X FROM {X} <"
					+ relationToCheck.getUri()
					+ "> {Y} "
					+ "MINUS "
					+ "SELECT X FROM {X} <"
					+ relationToCheck.getUri()
					+ "> {} <"
					+ relationToFind.getUri()
					+ "> {Y} ";

		TupleQueryResult resulst = connection.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
		List<String> bindingNames = resulst.getBindingNames();
		String result = "";
		// QueryResultsTable resultsTable =
		// rdf.performTableQuery(QueryLanguage.SERQL, query);
		try
		{
			while (resulst.hasNext())
			{
				BindingSet bindingSet = resulst.next();
				int rowCount = bindingNames.size();
				for (int row = 0; row < rowCount; row++)
				{
					if (result.length() == 0)
						result = "The following teachers (relation " + message + " are not mapped to anyone\n";
					result += bindingSet.getValue(bindingNames.get(0)).stringValue() + "\n";
				}
			}
			if (result.length() == 0)
				result = "Suspiciously enough, it seems that the " + message + " links are all ok.";
		}
		finally
		{
			resulst.close();
		}
		return result;
	}
}

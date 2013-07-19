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
package eu.annocultor.data.sources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.xml.sax.helpers.DefaultHandler;

import eu.annocultor.context.Environment;
import eu.annocultor.converter.ConverterHandlerDataObjects;
import eu.annocultor.path.Path;
import eu.annocultor.triple.LiteralValue;


/**
 * Source dataset consisting of an SQL query ResultSet.
 * 
 * @author Borys Omelayenko
 * 
 */
public class SqlDataSource extends AbstractQueryDataSource {

	private Connection connection;

	public SqlDataSource(Environment environment, String jdbcDriver, String jdbcUrl, String... sqlQuery) 
	throws ClassNotFoundException {
		Class.forName(jdbcDriver);
		setConnectionUrl(jdbcUrl);
		for (String query : sqlQuery) {
			addQuery(query);			
		}
	}

	@Override
	protected boolean parseQueries(DefaultHandler handler, Path recordSeparatingPath, Path recordIdentifyingPath) 
	throws Exception {

	    try {
	        connection = DriverManager.getConnection(getConnectionUrl());
	    } catch (Exception e) {
	        log.warn("Cannot connect to " + getConnectionUrl(), e);
	        return false;
        }
		try {
			return super.parseQueries(handler, recordSeparatingPath, recordIdentifyingPath);
		} finally {
			connection.close();
		}
	}

	@Override
	protected boolean parseQuery(DefaultHandler handler, String query, Path recordSeparatingPath, Path recordIdentifyingPath) 
	throws Exception {

		ConverterHandlerDataObjects flatHandler = makeHandler(handler, recordSeparatingPath);

		boolean passedARecord = false;
		ResultSet resultSet = connection.createStatement().executeQuery(query);

		try {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int numColumns = rsmd.getColumnCount();

			flatHandler.startDocument();

			while (resultSet.next()) {

				passedARecord = true;

				flatHandler.attemptDataObjectChange(resultSet.getString(recordIdentifyingPath.getPath()));

				// iterate result set fields
				for (int i = 1; i < numColumns + 1; i++) {
					String columnName = rsmd.getColumnName(i);
					String columnValue = null;

					// get column name and value
					try {
						columnValue = resultSet.getString(columnName);
					} catch (Exception e) {
						// no data found
						columnValue = null;
					}

					// populate literal value
					if (columnValue != null) {
						columnValue = columnValue.trim();
						String preprocessedValue = preprocessValue(columnName, columnValue);
						if (preprocessedValue != null) {
							flatHandler.addField(columnName, new LiteralValue(preprocessedValue));
						}
					}

				}
			}
			flatHandler.endDocument();
		} finally {
			resultSet.close();
		}
		return passedARecord;
	}

}

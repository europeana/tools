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
package eu.europeana.enrichment.api;

/**
 * Post-conversion tests.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface ConverterTester
{

	/**
	 * Post-conversion tests to be populated by the user.
	 * 
	 * @return <code>null</code> if all tests are passed, or an error message.
	 */
	public String testConversion() throws Exception;

	/**
	 * Post-conversion tests to be called by the converter, include loading files.
	 * 
	 * @return <code>null</code> if all tests are passed, or an error message.
	 */
	public String loadDataAndTest() throws Exception;

}
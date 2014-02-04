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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import eu.europeana.enrichment.context.Environment;


/**
 * Interface for converter kernel. Needed only for very special converters.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface ConverterKernel
{
	/**
	 * Converts a task to RDF.
	 * 
	 * @see Environment
	 */
	public int convert() throws Exception;

	/**
	 * Limit the number of records when conversion will stop, for debugging.
	 * 
	 * @param maximalRecordsToPass
	 */
	public abstract void setMaximalRecordsToPass(int maximalRecordsToPass);

	/**
	 * Sets a class with tests.
	 * 
	 * @param tester
	 */
	public void setTester(ConverterTester tester);

	public BufferedInputStream makeInputStream(File src) throws FileNotFoundException;
}

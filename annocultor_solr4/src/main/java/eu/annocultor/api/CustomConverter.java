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
package eu.annocultor.api;

/**
 * Custom converters created by users to treat their specific datasets. Any
 * custom converter should extend this class and keep a default constructor.
 * Mostly, this class is needed as a signature of a custom converter to be able
 * to find and analyze all converters created in a project.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class CustomConverter
{

	/**
	 * Initializes the converter. Here all static property maps should be created.
	 * No heavy processing or huge memory allocation here, put them to #run().
	 * Once initialized, as converter should be able to run multiple times. A
	 * separate light-weight constructor is also used for code analysis as it
	 * allows listing the (property) writers used in various converters.
	 * 
	 * @throws Exception
	 */
	public CustomConverter() throws Exception
	{
		// TODO: check for default constructor 
		//		if (this.getClass().getConstructor() == null)
		//			throw new Exception("No default constructor defined for converter.");
	}

	/**
	 * Runs the converter. All the data specific for a conversion run should be
	 * created and released here.
	 * 
	 * @throws Exception
	 */
	public int run(Task task, ConverterTester tester) throws Exception
	{
		return run(task, tester, -1);
	}

	public int run(Task task, ConverterTester tester, int maxRecords) throws Exception
	{
		ConverterKernel converter;
		try
		{
			task.getEnvironment().initializeVocabularies();
			converter = Factory.makeConverter(task, null);
			converter.setMaximalRecordsToPass(maxRecords);
			converter.setTester(tester);
		}
		catch (Exception e) {
			throw new Exception("Exception preparing a converter", e);
		}
		try
		{
			return converter.convert();			
		}
		catch (Exception e) {
			throw new Exception("Exception running a converter", e);
		}
	}

	public int run() throws Exception
	{
		return -1;
	}

	public void setTask(Task task)
	{
		this.task = task;
	}

	public Task getTask()
	{
		return task;
	}

	private Task task;

}

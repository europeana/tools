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
package eu.annocultor.converter;

import eu.annocultor.api.ConverterTester;
import eu.annocultor.api.Task;
import eu.annocultor.context.Environment;
import eu.annocultor.context.Namespace;
import eu.annocultor.data.destinations.IgnoredRdfGraph;
import eu.annocultor.data.destinations.RdfGraph;
import eu.annocultor.xconverter.api.Graph;

/**
 * Core factory.
 * 
 * @author Borys Omelayenko
 * 
 */
public class CoreFactory
{

	/**
	 * Makes a new conversion task.
	 * 
	 * @param signature
	 *          signature of the dataset, e.g. a museum name.
	 * @param subsignature
	 *          subsignature of the dataset, e.g. <code>works</code>,
	 *          <code>terms</code>, <code>artists</code>.
	 * @param description
	 *          description of the dataset that will appear in the output RDF
	 *          files
	 * @param targetNamespace
	 *          namespace of the dataset objects to appear in the output RDF files
	 * @throws Exception 
	 */

	public static Task makeTask(
			String signature,
			String subsignature,
			String description,
			Namespace targetNamespace,
			Environment environment) 
	throws Exception
	{
		if (!environment.checkSignatureForDuplicates(signature)) {
			throw new Exception("Duplicated task " + signature);			
		}

		return new TaskImpl(signature, subsignature, description, targetNamespace, environment);
	}

	public static Converter makeConverter(Task task, ConverterTester tester)
	{
		return new Converter(task, null, tester);
	}

	public static Graph makeGraph(
			Task task,
			String objectType,
			String propertyType,
			boolean addThisGraphToTask,
			boolean ignoreIt, 
			String... comment)
	{
		// checking name of this graph
		if (objectType.contains("."))
			throw new RuntimeException("Dot is not allowed in target object type signature " + objectType);

		Graph newGraph = 
			ignoreIt ? 
					makeIgnoredGraph(
							task.getDatasetId(),
							task.getEnvironment(),
							null,
							objectType,
							propertyType,
							comment)
							:	
								makeNamedGraph(
										task.getDatasetId(),
										task.getEnvironment(),
										null,
										objectType,
										propertyType,
										comment);
							if (addThisGraphToTask)
								task.addGraph(newGraph);
							return newGraph;
	}

	public static Graph makeNamedGraph(
			String datasetId,
			Environment environment,
			String datasetModifier,
			String objectType,
			String propertyType,
			String... comment)
	{
		return new RdfGraph(datasetId, environment, datasetModifier, objectType, propertyType, comment);
	}

	private static Graph makeIgnoredGraph(
			String datasetId,
			Environment environment,
			String datasetModifier,
			String objectType,
			String propertyType,
			String... comment)
	{
		return new IgnoredRdfGraph(datasetId, environment, datasetModifier, objectType, propertyType, comment);
	}


}

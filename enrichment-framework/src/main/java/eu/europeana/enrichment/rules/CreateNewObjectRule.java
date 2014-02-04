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
package eu.europeana.enrichment.rules;

import java.util.HashSet;
import java.util.Set;

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;
import eu.europeana.enrichment.xconverter.api.PropertyRule;


/**
 * Generates a new object with its ID either derived from the value of the
 * current property or generated. Then one writer creates a connection to the
 * main object (it gets the ID as the value), and the other writers create the
 * new object (they get ID as the subject).
 * 
 * @author Anna Tordai
 * @author Borys Omelayenko
 * 
 */
public class CreateNewObjectRule extends SequenceRule
{

	public enum NewObjectId
	{
		useTripleValue,
		useGeneratedPartId,
		useGeneratedNameBased
	}

	private static Set<String> generatedPartIds = new HashSet<String>();

	private Property connectorToMainObject;

	private Graph graph;

	private Namespace namespace;

	private NewObjectId objectIdType;

	@Override
	public String getAnalyticalRuleClass()
	{
		return "NewObject";
	}

	/**
	 * 
	 * @param connectorToMainObject
	 * @param namespace
	 *          the namespace where the IDs of the new objects should belong to.
	 *          it is assumed that no one else is creating IDs in this namespace,
	 *          otherwise they may conflict with these new IDs.
	 * @param objectIdType
	 * @param rules
	 */
	public CreateNewObjectRule(
			Property connectorToMainObject,
			Graph connectorTarget,
			Namespace namespace,
			NewObjectId objectIdType,
			PropertyRule... rules)
	{
		super(rules);
		this.connectorToMainObject = connectorToMainObject;
		this.namespace = namespace;
		this.objectIdType = objectIdType;
		this.graph = connectorTarget;
	}

	public static String generatePartId(Namespace namespace, String parentIdLocalPart, String separator)
			throws Exception
	{
		if (!parentIdLocalPart.contains(separator))
			throw new Exception("Generated id needs the local part of id (after '"
				+ separator
				+ "'), "
				+ "but cannot find '"
				+ separator
				+ "' in resource "
				+ parentIdLocalPart);
		parentIdLocalPart = parentIdLocalPart.substring(parentIdLocalPart.indexOf(separator) + 1);

		int iteration = 0;
		String result = null;
		do
		{
			result = namespace + parentIdLocalPart + "_xPART_" + iteration;
			iteration++;
		}
		while (generatedPartIds.contains(result));
		generatedPartIds.add(result);
		return result;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception
	{
		// new sub object id
		String newObjectId = null;
		switch (objectIdType)
		{
		case useTripleValue:
			newObjectId = namespace + triple.getValue().getValue().replaceAll("[^\\w]", "_");
			break;

		case useGeneratedPartId:
			String localPartOfSubject = triple.getSubject();
			newObjectId = generatePartId(namespace, localPartOfSubject, "#");
			break;

		case useGeneratedNameBased:
			newObjectId = Common.generateNameBasedUri(namespace, triple.getValue().getValue());
			break;
		default:
			throw new Exception("Coding error");
		}

		// connection to the main object
		if (connectorToMainObject != null)
			graph.add(triple
					.changeProperty(connectorToMainObject)
					.changeValue(new ResourceValue(newObjectId)));

		// properties of the new object
		super.fire(triple.changeSubject(newObjectId), dataObject);
	}

}

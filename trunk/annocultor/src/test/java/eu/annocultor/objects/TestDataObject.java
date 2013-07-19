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
package eu.annocultor.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;

import eu.annocultor.api.ObjectRule;
import eu.annocultor.api.Task;
import eu.annocultor.path.Path;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;

@Ignore
public class TestDataObject implements DataObject
{

	Path recordSeparating;
	TestDataObject(Task task) throws Exception
	{
		super();
		this.task = task;
		recordSeparating = new Path("dc:separating");
	}

	Task task;

	private Map<Path, ListOfValues> fieldValuesMap = new HashMap<Path, ListOfValues>();

	public long size()
	{
		return fieldValuesMap.size();
	}

	public ListOfValues getValues(Path propertyName)
	{
		ListOfValues result = fieldValuesMap.get(propertyName);
		if (result == null)
			result = new ListOfValues();
		return result;
	}

//	public void clearValues(Path propertyName) throws Exception
//	{
//		fieldValuesMap.put(propertyName, new ListOfValues());
//	}

	public XmlValue getFirstValue(Path propertyName)
	{
		List<XmlValue> allValues = getValues(propertyName);
		if (allValues.isEmpty())
			return null;
		if (allValues.size() > 1)
			System.out.println("Possibly missing other values of " + propertyName + ", value " + allValues.get(0));
		return allValues.get(0);
	}

	public void addValue(Path propertyName, XmlValue newValue) throws Exception
	{
		fieldValuesMap.put(propertyName, new ListOfValues(Collections.singleton(newValue)));
	}

	@Override
	public Iterator<Path> iterator()
	{
		return fieldValuesMap.keySet().iterator();
	}

//	public ListOfValues getValueByExactMatch(Path pathToMatch)
//	{
//		return fieldValuesMap.get(pathToMatch);
//	}

	public DataObject getParent()
	{
		return null; //To change body of implemented methods use File | Settings | File Templates.
	}

	public List<DataObject> getChildren()
	{
		return null; //To change body of implemented methods use File | Settings | File Templates.
	}

	public List<DataObject> findAllChildren()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Path getSeparatingPath()
	{
		return recordSeparating;
	}

	public Path getIdPath() throws Exception
	{
		return new Path("id");
	}

	public ObjectRule getDataObjectRule()
	{
		try {
			return ObjectRuleImpl.makeObjectRule(null, new Path(""), new Path(""), new Path(""), null, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Task getTask()
	{
		return task;
	}

}
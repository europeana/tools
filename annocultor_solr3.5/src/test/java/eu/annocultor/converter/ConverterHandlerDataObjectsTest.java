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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eu.annocultor.TestEnvironment;
import eu.annocultor.api.Factory;
import eu.annocultor.context.Namespaces;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.path.Path;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.triple.LiteralValue;

public class ConverterHandlerDataObjectsTest extends TestRulesSetup
{

    final String fieldId = "ID";
    final String fieldName = "Name";

    List<String> dataObjects = new ArrayList<String>();

    public void testDO() throws Exception
    {
        final Path recordSeparatingPath = new Path("");
        task = Factory.makeTask("bibliopolis", "terms", "Bibliopolis, KB", Namespaces.DC, new TestEnvironment());
        objectRule = ObjectRuleImpl.makeObjectRule(task, recordSeparatingPath, new Path(fieldId), new Path(""), null, true);

        assertEquals(1, task.getRuleForSourcePath(recordSeparatingPath).size());

        ConverterHandler defaultHandler = new ConverterHandler(task) {

            @Override
            protected void processDataObject() throws Exception
            {
                ListOfValues values = getValues(new Path(fieldName));
                Collections.sort(values);
                dataObjects.add(StringUtils.join(values, ","));
            }			
        };
        ConverterHandlerDataObjects handler = new ConverterHandlerDataObjects(defaultHandler, recordSeparatingPath);

        // non-aggregating (default)
        handler.setAggregate(false);
        imitateDataInsertionSequence(handler);

        // checks
        assertEquals(4, dataObjects.size());
        assertEquals("2A,2B",dataObjects.get(1));
        assertEquals("1A",dataObjects.get(2));

        // aggregating
        handler.setAggregate(true);
        imitateDataInsertionSequence(handler);

        // checks
        assertEquals(3, dataObjects.size());
        assertEquals("1A,2A,2B",dataObjects.get(1));
        assertEquals("3A",dataObjects.get(2));
    }

    private void imitateDataInsertionSequence(ConverterHandlerDataObjects handler) 
    throws Exception {
        dataObjects.clear();
        handler.startDocument();
        makeObject(handler, "1", "1A", "1B");
        makeObject(handler, "2", "2A", "2B");
        makeObject(handler, "2", "1A");
        makeObject(handler, "3", "3A");
        handler.endDocument();
    }

    private void makeObject(ConverterHandlerDataObjects handler, String id, String ... names) throws Exception {
        handler.attemptDataObjectChange(id);
        handler.addField(fieldId, new LiteralValue(id));
        for (String name : names) {
            handler.addField(fieldName, new LiteralValue(name));
        }
    }
}

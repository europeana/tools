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
package eu.europeana.enrichment.converter;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Value;

/**
 * A facade for a handler at the level of data objects.
 * 
 * @author Borys Omelayenko
 */
public class ConverterHandlerDataObjects {

    Logger log = LoggerFactory.getLogger(getClass().getName());

    private DefaultHandler handler;
    private String recordSeparatingPath;
    private Map<String, Set<Value>> record = new HashMap<String, Set<Value>>();
    private String subject = null;

    public ConverterHandlerDataObjects(DefaultHandler handler, Path recordSeparatingPath) {
        this.handler = handler;
        this.recordSeparatingPath = recordSeparatingPath.getPath();
    }

    public void attemptDataObjectChange(String newSubject)
    throws Exception {
        if (aggregate) {
            if (!newSubject.equals(this.subject)) {
                finishDataObject();
                this.subject = newSubject;
            }
        } else {
            finishDataObject();
        }
    }

    public void finishDataObject()
    throws SAXException {
        if (!record.isEmpty()) {
            handler.startElement("", recordSeparatingPath, "", null);
            flush();
            handler.endElement("", recordSeparatingPath, "");
        }
    }

    protected void flush()
    throws SAXException {
        for (String fieldName : record.keySet()) {
            Set<Value> values = record.get(fieldName);
            for (Value value : values) {
                handler.startElement("", fieldName, "", extractLang(value));
                handler.characters(value.getValue().toCharArray(), 0, value.getValue().length());
                handler.endElement("", fieldName, "");
            }
        }
        record.clear();
    }

    public void addField(String fieldName, Value value)
    throws Exception {
        if (value != null) {
            if (! record.containsKey(fieldName)) {
                record.put(fieldName, new HashSet<Value>());
            }

            Set<Value> values = record.get(fieldName);
            values.add(value);
        }
    }

    public void endDocument() throws SAXException {
        finishDataObject();
        handler.endDocument();
    }

    public void startDocument() throws SAXException {
        handler.startDocument();
    }

    private boolean aggregate = false;

    public void setAggregate(boolean aggregate) {
        this.aggregate = aggregate;
    }

    static Attributes extractLang(Value value) {
        if (value != null && value instanceof LiteralValue) {
            String lang = ((LiteralValue)value).getLang();
            if (StringUtils.length(lang) == 2) {
                return new AttributesProxy(lang);
            }
        }
        return null;
    }

    private static class AttributesProxy implements Attributes {

        String lang;
        public AttributesProxy(String lang) {
            this.lang = lang;
        }

        // these are actually used in PathElement
        public int getLength()
        {
            return 1;
        }

        public String getLocalName(int index)
        {
            return (index == 0) ? "lang" : null;
        }

        public String getURI(int index)
        {
            return (index == 0) ? "http://www.w3.org/XML/1998/namespace" : null;
        }

        public String getValue(int index)
        {
            return (index == 0) ? lang : null;
        }

        // these are not used
        public int getIndex(String uri, String localName)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }

        public int getIndex(String name)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }

        public String getQName(int index)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }

        public String getType(int index)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }

        public String getType(String uri, String localName)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }

        public String getType(String name)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }


        public String getValue(String uri, String localName)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }

        public String getValue(String name)
        {
            throw new RuntimeException("Not implemented, as we believe it is never called");
        }
    }
}
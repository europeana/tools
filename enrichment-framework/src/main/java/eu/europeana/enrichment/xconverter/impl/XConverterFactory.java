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
package eu.europeana.enrichment.xconverter.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import eu.europeana.enrichment.common.Language.Lang;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPeople;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPlaces;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTime;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.xconverter.api.Graph;
import eu.europeana.enrichment.xconverter.api.Resource;


/**
 * XML API factory.
 * 
 * @author Borys Omelayenko
 * 
 */
public class XConverterFactory
{

	public static final String MERGED_SOURCES_OUTER_TAG_FILE = "file";
    public static final String MERGED_SOURCES_OUTER_TAG_FILESET = "fileset";

    public void init()
	throws Exception
	{

	}

	public XConverterFactory(Environment environment)
	{
		super();
		this.environment = environment;
		try
		{
			init();
		}
		catch (Exception e) 
		{
			//throw new RuntimeException(e);
		}	
	}

	Map<String, Graph> destinations = new HashMap<String, Graph>();

	public void addGraph(String id, Graph graph)
	{
		if (graph == null)
		{
			throw new RuntimeException("Null graph");
		}
		destinations.put(id, graph);
	}

	public Graph makeGraph(String id)
	throws Exception
	{
		if (!destinations.containsKey(id))
		{
			throw new Exception("Named graph with id " + id + " was not declared in the destinations section");
		}
		return destinations.get(id);
	}

	public void addNamespace(String nick, String url)
	throws Exception
	{
		environment.getNamespaces().addNamespace(url, nick);
	}

	private Environment environment;

	public Environment getEnvironment()
	{
		return environment;
	}

	public String makeString(String string)
	{
		return string;
	}

	public Resource makeResource(String uri)
	throws Exception
	{
		return new Resource(new URI(new Path(uri, environment.getNamespaces()).getPath()));
	}

	public Path makePath(String path)
	throws Exception
	{
    // Prefix fileset/file/ is here for a purpose: 
    // (multiple) source files are wrapped into a single XML file
    // where each file is surrounded with fileset/file tag
		return new Path(path.startsWith("/") ? ("/" + MERGED_SOURCES_OUTER_TAG_FILESET + "/" + MERGED_SOURCES_OUTER_TAG_FILE + path) : path, environment.getNamespaces());
	}

	public Namespace makeNamespace(String nick)
	throws Exception
	{
		if (environment == null || environment.getNamespaces() == null)
		{
			throw new Exception("Null environment or namespaces");
		}
		String uri = environment.getNamespaces().getUri(nick);
		if (uri == null) {
			throw new Exception("Namespaces with nick " + nick + " is not declared in the <namespaces> section");
		}
		return new Namespace(uri, nick, true);
	}

	public Property makeProperty(String path)
	throws Exception
	{
		return new Property(new Path(path, environment.getNamespaces()));
	}

	public Lang makeLang(String lang)
	throws Exception
	{
		return Lang.valueOf(lang);
	}

	private Map<String, VocabularyOfTerms> vocabulariesOfTerms = new HashMap<String, VocabularyOfTerms>();
	private Map<String, VocabularyOfPeople> vocabulariesOfPeople = new HashMap<String, VocabularyOfPeople>();
	private Map<String, VocabularyOfPlaces> vocabulariesOfPlaces = new HashMap<String, VocabularyOfPlaces>();
	private Map<String, VocabularyOfTime> vocabulariesOfTime = new HashMap<String, VocabularyOfTime>();

	public VocabularyOfTerms makeVocabularyOfTerms(String id)
	throws Exception
	{
		VocabularyOfTerms vocabulary = vocabulariesOfTerms.get(id);
		if (vocabulary == null)
			throw new NullPointerException("Vocabulary " + id + " is not defined in XML under tag <vocabularies>");
		return vocabulary;
	}

	public void addVocabularyOfTerms(String id, VocabularyOfTerms vocabulary)
	throws Exception
	{
		if (vocabulariesOfTerms.containsKey(id))
		{
			throw new Exception("Vocabulary " + id + " already exists");
		}
		vocabulariesOfTerms.put(id, vocabulary);
	}

	public VocabularyOfTime makeVocabularyOfTime(String id)
	throws Exception
	{
		VocabularyOfTime vocabulary = vocabulariesOfTime.get(id);
		if (vocabulary == null)
			throw new NullPointerException("Vocabulary " + id + " is not defined in XML under tag <vocabularies>");
		return vocabulary;
	}

	public void addVocabularyOfTime(String id, VocabularyOfTime vocabulary)
	throws Exception
	{
		if (vocabulariesOfTime.containsKey(id))
		{
			throw new Exception("Vocabulary " + id + " already exists");
		}
		vocabulariesOfTime.put(id, vocabulary);
	}

//	public VocabularyOfTerms getVocabularyOfTerms(String id)
//	{
//		return vocabulariesOfTerms.get(id);
//	}

	public VocabularyOfPeople makeVocabularyOfPeople(String id)
	throws Exception
	{
		VocabularyOfPeople vocabulary = vocabulariesOfPeople.get(id);
		if (vocabulary == null)
			throw new NullPointerException("Vocabulary " + id + " is not defined in XML under tag <vocabularies>");
		return vocabulary;
	}

	public void addVocabularyOfPeople(String id, VocabularyOfPeople vocabulary)
	throws Exception
	{
		if (vocabulariesOfPeople.containsKey(id))
		{
			throw new Exception("Vocabulary " + id + " already exists");
		}
		vocabulariesOfPeople.put(id, vocabulary);
	}

//	public VocabularyOfPeople getVocabularyOfPeople(String id)
//	{
//		return vocabulariesOfPeople.get(id);
//	}


	public VocabularyOfPlaces makeVocabularyOfPlaces(String id)
	throws Exception
	{
		VocabularyOfPlaces vocabulary = vocabulariesOfPlaces.get(id);
		if (vocabulary == null)
			throw new NullPointerException("Vocabulary " + id + " is not defined in XML under tag <vocabularies>");
		return vocabulary;
	}

	public void addVocabularyOfPlaces(String id, VocabularyOfPlaces vocabulary)
	throws Exception
	{
		if (vocabulariesOfPlaces.containsKey(id))
		{
			throw new Exception("Vocabulary " + id + " already exists");
		}
		vocabulariesOfPlaces.put(id, vocabulary);
	}

//	public VocabularyOfPlaces getVocabularyOfPlaces(String id)
//	{
//		return vocabulariesOfPlaces.get(id);
//	}
	
	public class MapPathToProperty
	{
		public MapPathToProperty(Path srcPath, Property dstProperty)
		{
			super();
			this.srcPath = srcPath;
			this.dstProperty = dstProperty;
		}
		private Path srcPath;
		private Property dstProperty;
		
		public Path getSrcPath()
		{
			return srcPath;
		}
		public Property getDstProperty()
		{
			return dstProperty;
		}
		
	}

	public MapObjectToObject makeMap(String srcValue, String dstValue)
	throws Exception
	{
		return new MapObjectToObject(makeString(srcValue), makeString(dstValue));
	}

	public MapObjectToObject makeMap(Path srcValue, Property dstValue)
	throws Exception
	{
		return new MapObjectToObject(srcValue, dstValue);
	}

	public class MapObjectToObject
	{
		public MapObjectToObject(Object srcValue, Object dstValue)
		{
			super();
			this.srcValue = srcValue;
			this.dstValue = dstValue;
		}
		private Object srcValue;
		private Object dstValue;
		
		public Object getSrcValue()
		{
			return srcValue;
		}
		public Object getDstValue()
		{
			return dstValue;
		}
		
	}
}

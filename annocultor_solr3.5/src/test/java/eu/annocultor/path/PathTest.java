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
package eu.annocultor.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import eu.annocultor.context.Concepts;
import eu.annocultor.context.EnvironmentImpl;
import eu.annocultor.context.Namespace;
import eu.annocultor.context.Namespaces;
import eu.annocultor.converter.DataObjectTest;
import eu.annocultor.path.PathMap.MatchResult;
import eu.annocultor.triple.XmlValue;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.DataObject.ListOfValues;

public class PathTest extends TestCase
{

    public DataObject makeTestDataObject() throws Exception
    {
        //JDomFactory.copyStaticNamespacesToJDom();
        DataObject dataObject = DataObjectTest.makeDataObject();
        // 1 simple element
        dataObject.addValue(new Path("dc:title"), new XmlValue("1"));

        // 2 with attr
        dataObject.addValue(new Path("dc:language[@a='av']"), new XmlValue("2"));

        // 3 with another attr
        dataObject.addValue(new Path("dc:language[@a='avv']"), new XmlValue("3"));
        dataObject.addValue(new Path("dc:language[@a='avv']"), new XmlValue("4"));

        // 5 two attrs
        dataObject.addValue(new Path("dc:language[@a='av' and @b='bv']"), new XmlValue("5"));

        // 6 another two attrs
        dataObject.addValue(new Path("dc:language[@a='avv' and @b='bvv']"), new XmlValue("6"));

        dataObject.addValue(new Path("dc:description[@dc:creator='full']"), new XmlValue("7"));
        // 7 el(atts)/el(atts)
        dataObject.addValue(new Path("dc:language[@a='avv' and @b='bvv']/dc:type[@a='avv' and @b='bvv']"), new XmlValue("7"));

        // 8 hamespaced attr
        dataObject.addValue(new Path("dc:location[@rdf:a='store']", new Namespaces()), new XmlValue("London"));

        // 9 deep value
        dataObject.addValue(new Path("adlibXML/recordList/record/dc:title", new Namespaces()), new XmlValue("adlib"));

        return dataObject;
    }

    String syntacticallyWrongPathExpressions[] = {
            "[@a]",
            "dc:language@a='1'",
            "dc:language[@a='avv' and @b='bvv'",
            "dc:language@a='avv' and @b='bvv']",
            "dc:language[@a='avv' or @b='bvv']",
            "dc:language[@a='avv",
            "dc:language[@a='avv'and",
            "dc:language/@a",
            "dc:language/@dc:a",
            "dc:language/dc:x@a",
            "dc:language/dc:x@dc:a",
            "dc:language/dc:[@dc:a]",
            "dc:language/dc:x@dc:",
            "dc:x@a",
            "dc:x@dc:a",
            "dc:[@dc:a]",
            "dc:[@dc:a cc]",
            "dc:x@dc:",
            "dc:x/dc:y@attr"
    };

    List<String> semanticallyWrongDataPathExpressions = new ArrayList<String>();
    {
        semanticallyWrongDataPathExpressions.add("dc:language[@a]");
    }

    public void testPathSerialization() throws Exception
    {
        assertEquals(Concepts.DC.TITLE + "", new Path("dc:title").getPath());
        assertEquals(Concepts.DC.TITLE + Path.PE_SEPARATOR + Concepts.DC.DATE + "/@type", 
                new Path("dc:title/dc:date[@type]").getPath());
        assertEquals(Namespaces.ANNOCULTOR_CONVERTER.getUri() + "usePartId", new Path("ac:usePartId", new Namespaces()).getPath());

        assertEquals(Concepts.DC.TITLE + "/@" + Concepts.DC.TITLE, new Path("dc:title[@dc:title]").getPath());
        assertEquals(Concepts.DC.TITLE + "[@" + Concepts.DC.TITLE + "='x']", new Path("dc:title[@dc:title='x']").getPath());
        assertEquals(Concepts.DC.TITLE + "[@" + Concepts.DC.DATE + "='date' and @" + Concepts.DC.TITLE + "='title']", 
                new Path("dc:title[@dc:title='title' and @dc:date='date']").getPath());
        assertEquals(Concepts.DC.TITLE + "[@a='sss']", new Path("dc:title[@a='sss']").getPath());
        Path p1 = new Path("dc:title[@b='2' and @a='1']");
        //		p1.appendAttribute(new NamespacedName("a", ""), "1");
        //		p1.appendAttribute(new NamespacedName("b", ""), "2");
        assertEquals(Concepts.DC.TITLE + "[@a='1' and @b='2']", p1.getPath());

        assertEquals(Concepts.DC.TITLE + "[@a='s']" + Path.PE_SEPARATOR + Concepts.DC.LOCATION +"[@b='d']", 
                new Path("dc:title[@a='s']/dc:location[@b='d']").getPath());

        assertEquals(Concepts.DC.TITLE + "[@a='s']" + Path.PE_SEPARATOR + Concepts.DC.LOCATION +"[@b='d']", 
                new Path(new Path("dc:title[@a='s']"), new Path("dc:location[@b='d']")).getPath());
        assertEquals(Concepts.DC.TITLE + "[@a='s']" + Path.PE_SEPARATOR + Concepts.DC.LOCATION +"[@b='d']"+ Path.PE_SEPARATOR + Concepts.DC.DATE +"[@c='d']", 
                new Path(new Path("dc:title[@a='s']"), new Path("dc:location[@b='d']/dc:date[@c='d']")).getPath());
    }

    public void testSyntax() throws Exception
    {
        for (String pathExpression : syntacticallyWrongPathExpressions)
        {
            try
            {
                Path path = new Path(pathExpression);
                fail("Wrong syntax in path expression sneaked in: " + pathExpression + ", expanded as " + path.getPath());
            }
            catch (Exception e)
            {
                // just continue, this exception is expected
            }
        }
    }

    public void testPositive() throws Exception
    {
        new Path(
                new Path("data/oeuvre"), 
                new Path("listeauteurs/auteur[@id='http://www.europeana.eu/ns/1000']"));
        assertEquals("e[@a='x' and @b='y']" + Path.PE_SEPARATOR + "z", 
                new Path("e[@a='x' and @b='y']/z").getPath());
    }

    public void testDataPathNegative() throws Exception
    {
        DataObject dataObject = makeTestDataObject();
        for (String pathExpression : semanticallyWrongDataPathExpressions)
        {
            try
            {
                Path path = new Path(pathExpression);
                dataObject.addValue(path, new XmlValue(""));
                fail("Wrong data path expression sneaked in: " + pathExpression + " **** as " + path.getPath());
            }
            catch (Exception e)
            {
                // just continue, this exception is expected
            }
        }
    }

    public static <T> List<T> asList(Iterable<T> iterator) {
        List<T> list = new ArrayList<T>();
        for (T matchResult : iterator) {
            list.add(matchResult);
        }
        return list;
    }

    public void testPathSamePathsDiffValues() throws Exception
    {
        DataObject dataObject = DataObjectTest.makeDataObject();
        dataObject.addValue(new Path("dc:language[@a='avv']"), new XmlValue("3"));
        dataObject.addValue(new Path("dc:language[@a='avv']"), new XmlValue("4"));

        assertEquals(2, dataObject.size());

        assertEquals(2, dataObject.getValues(new Path("dc:language")).size());
        assertEquals(2, dataObject.getValues(new Path("dc:language[@a]")).size());
    }

    public void testPathAsk() throws Exception
    {
        DataObject dataObject = makeTestDataObject();
        assertEquals(1, dataObject.getValues(new Path("dc:title")).size());

        assertEquals(5, dataObject.getValues(new Path("dc:language")).size());

        // atts
        Path path1 = new Path("dc:language[@a]");
        Path path2 = new Path("dc:language[@a]", new Namespaces());
        assertEquals(Concepts.DC.LANGUAGE + "/@a", path1.getPath());
        assertEquals(Concepts.DC.LANGUAGE + "/@a", path2.getPath());

        ListOfValues v1 = dataObject.getValues(path1);
        assertEquals(6, v1.size());
        ListOfValues v2 = dataObject.getValues(path2);
        assertEquals(6, v2.size());

        assertTrue(v1.indexOf(new XmlValue("avv")) >= 0);
        assertTrue(v1.indexOf(new XmlValue("av")) >= 0);

        assertEquals(3, dataObject.getValues(new Path("dc:language[@b]")).size());
        assertEquals(1, dataObject.getValues(new Path("dc:location[@rdf:a]", new Namespaces())).size());

        assertEquals(0, dataObject.getValues(new Path("dc:language[@c]")).size());
        assertEquals(2, dataObject.getValues(new Path("dc:language[@a='av']")).size());
        assertEquals(1, dataObject.getValues(new Path("dc:language[@b='bv']")).size());
        assertEquals(0, dataObject.getValues(new Path("dc:language[@b='not there']")).size());

        assertEquals(1, dataObject.getValues(
                new Path("dc:description[@dc:creator='full']")).size());
        assertEquals("7", dataObject.getValues(
                new Path("dc:description[@dc:creator='full']")).get(0).getValue());

        assertEquals(1, dataObject
                .getValues(new Path("dc:description[@dc:creator]"))
                .size());
        assertEquals("full", dataObject.getValues(new Path("dc:description[@dc:creator]")).get(0).getValue());

        assertEquals(0, dataObject.getValues(new Path("dc:description[@dc:contributor]")).size());

        // ns
        assertEquals(1, dataObject.getValues(new Path("dc:location[@rdf:a='store']")).size());
        assertEquals(1, dataObject.getValues(new Path("dc:location[@rdf:a='store']", new Namespaces())).size());
        assertNotSame("Londonw", dataObject
                .getValues(new Path("dc:location[@rdf:a='store']", new Namespaces()))
                .get(0).getValue());
        assertEquals("London", dataObject
                .getValues(new Path("dc:location[@rdf:a='store']", new Namespaces()))
                .get(0).getValue());
        assertEquals(Concepts.DC.LOCATION + "[@" + Namespaces.RDF + "a='store']", 
                new Path("dc:location[@rdf:a='store']", new Namespaces()).getPath());

        assertEquals("London", dataObject
                .getValues(new Path("dc:location[@rdf:a='store']", new Namespaces()))
                .get(0).getValue());

        //		assertEquals("London", dataObject
        //				.getValues(new Path("dc:location[@rdf:a='store']", new Namespaces()))
        //				.get(0).getValue());

        // misc bugs

        // ... and ...
        PathMap<String> map = new PathMap<String>();
        map.put(new Path("file/BASE/NOTICIES/REF", new Namespaces()), "X");
        map.put(new Path("adlibXML/recordList/record/dc:title[@rdf:resource='XX' and @dc:try='YY']"), "ZZ");
        {
            List<MatchResult<String>> answer = asList(map.ask(new Path("adlibXML/recordList/record/dc:title[@dc:try]")));
            assertEquals(1, answer.size());
            assertEquals("YY", answer.get(0).getAttributeValue());
            assertEquals("ZZ", answer.get(0).getStoredObject());
        }
        {
            List<MatchResult<String>> answer = asList(map.ask(new Path("adlibXML/recordList/record/dc:title[@rdf:resource]")));
            assertEquals(1, answer.size());
            assertEquals("XX", answer.get(0).getAttributeValue());
            assertEquals("ZZ", answer.get(0).getStoredObject());
        }
    }

    private void checkResultContains(List<MatchResult<String>> list, String storedObjectValue, String attributeValue) 
    throws Exception {
        for (MatchResult<String> mr : list) {
            assertNotNull(mr.getStoredObject());
            if (mr.getStoredObject().equals(storedObjectValue)) {
                if (mr.getAttributeValue() == null) {
                    if (attributeValue == null) {
                        return ;
                    }
                } else {
                    if (mr.getAttributeValue().equals(attributeValue)) {
                        return;
                    }
                }
            }
        }
        fail("missing pair " + storedObjectValue + "-" + attributeValue);
    }

    public void testPathAskVsAnswer() throws Exception
    {
        PathMap<String> map = new PathMap<String>();
        map.put(new Path("A/B[@b='1']/C", new Namespaces()), "X");
        map.put(new Path("A/B", new Namespaces()), "Y");
        map.put(new Path("A/B/C[@c='2']", new Namespaces()), "Z");

        List<MatchResult<String>> ask = asList(
                map.ask(
                        new Path("A/B/C", new Namespaces())));
        assertEquals(2, ask.size());
        checkResultContains(ask, "X", null);
        checkResultContains(ask, "Z", null);

        ask = asList(
                map.ask(
                        new Path("A/B/C[@c]", new Namespaces())));
        assertEquals(1, ask.size());
        checkResultContains(ask, "Z", "2");

        map.put(new Path("A/B/C[@c]", new Namespaces()), "ZZ");

        List<MatchResult<String>> answer = asList(
                map.answer(
                        new Path("A/B/C", new Namespaces())));
        assertEquals(0, answer.size());

        answer = asList(
                map.answer(
                        new Path("A/B/C[@c='3']", new Namespaces())));
        assertEquals(1, answer.size());
        checkResultContains(answer, "ZZ", "3");

        answer = asList(
                map.answer(
                        new Path("A/B", new Namespaces())));
        assertEquals(1, answer.size());
        checkResultContains(answer, "Y", null);

        map.put(new Path("A/B[@b]", new Namespaces()), "ZZAB");
        answer = asList(
                map.answer(
                        new Path("A/B[@b='1']", new Namespaces())));
        assertEquals(2, answer.size());
        checkResultContains(answer, "Y", null);
        checkResultContains(answer, "ZZAB", "1");
    }

    // for rules: getRule(query) on XML path
    public void testPathAnswer() throws Exception
    {
        PathMap<String> map = new PathMap<String>();
        map.put(new Path("file/BASE/NOTICIES/REF", new Namespaces()), "X");
        map.put(new Path("file/BASE/NOTICIES", new Namespaces()), "Y");
        map.put(new Path("file/BASE/NOTICIES[@REF]", new Namespaces()), "Z");
        map.put(new Path("file/BASE/NOTICIES[@REF]", new Namespaces()), "Z");
        map.put(new Path("adlibXML/recordList/record/dc:language[@lang='ua' and @type='langsource']/dc:source"), "src");

        // ... and ...
        map.put(new Path("dc:language[@lang]"), "ZZ");
        try {
            map.answer(new Path("adlibXML/recordList/record/dc:title[@dc:try]"));
            fail("Exception expected");
        } catch (Exception e) {
            // expected
        } 

        map = new PathMap<String>();
        map.put(new Path("dc:language[@type]"), "src");
        List<MatchResult<String>> answer = asList(
                map.answer(
                        new Path("dc:language[@lang='ua' and @type='langsource']/dc:source")));
        assertEquals(1, answer.size());
        checkResultContains(answer, "src", "langsource");

        map.put(new Path("dc:language[@lang]"), "UA");
        answer = asList(
                map.answer(
                        new Path("dc:language[@lang='ua' and @type='langsource']/dc:source")));
        assertEquals(2, answer.size());
        checkResultContains(answer, "UA", "ua");
    }

    public void testPathEmpty() throws Exception
    {
        assertEquals("", new Path("").getPath());
        assertEquals("", new Path("").getLastTagExpanded());
        PathMap<String> map = new PathMap<String>();
        assertEquals(0, asList(map.answer(new Path(""))).size());

        map.put(new Path("file/BASE/NOTICIES/REF", new Namespaces()), "X");
        assertEquals(0, asList(map.answer(new Path(""))).size());

        map.put(new Path("file/BASE/NOTICIES[@REF]", new Namespaces()), "Y");
        assertEquals(0, asList(map.answer(new Path(""))).size());

        map.put(new Path(""), "Z");
        assertEquals(1, asList(map.answer(new Path(""))).size());
        assertEquals("Z", asList(map.answer(new Path(""))).get(0).getStoredObject());
    }


    public void testPathEmptyAsParent() throws Exception
    {
        assertEquals(0, new Path("").size());
        assertEquals(0, new Path(new Path(""), new Path("")).size());
        assertEquals(0, new Path(new Path(""), "", "", null).size());
        assertEquals("BASE", new Path(new Path(""), new Path("BASE")).getPath());
    }

    public void testPathInternals() throws Exception
    {
        assertEquals("file" + Path.PE_SEPARATOR + "BASE" + Path.PE_SEPARATOR + "NOTICIES", new Path("file/BASE/NOTICIES", new Namespaces()).getPath());
        assertEquals("file" + Path.PE_SEPARATOR + "BASE" + Path.PE_SEPARATOR + "NOTICIES", new Path("/file/BASE/NOTICIES", new Namespaces()).getPath());
        assertEquals("file" + Path.PE_SEPARATOR + "BASE" + Path.PE_SEPARATOR + "NOTICIES", new Path("file/BASE[@R='d']/NOTICIES", new Namespaces()).getPathElementsOnly());
        assertEquals("file" + Path.PE_SEPARATOR + "BASE" + Path.PE_SEPARATOR + "NOTICIES", new Path("file/BASE/NOTICIES[@D]", new Namespaces()).getPathElementsOnly());
        assertEquals("file" + Path.PE_SEPARATOR + "BASE" + Path.PE_SEPARATOR + "NOTICIES/@http://purl.org/dc/elements/1.1/try", new Path("file/BASE/NOTICIES[@dc:try]", new Namespaces()).getPath());
        assertEquals("file" + Path.PE_SEPARATOR + "BASE" + Path.PE_SEPARATOR + "NOTICIES[@http://purl.org/dc/elements/1.1/try='xx']", new Path("file/BASE/NOTICIES[@dc:try='xx']", new Namespaces()).getPath());
    }


    public void testPathInsertion() throws Exception
    {
        DataObject dataObject = makeTestDataObject();
        dataObject.addValue(new Path("dc:language[@a='avv' and @b='bvv']"), new XmlValue("6"));
        //    DataObject testedDataObject = new TestDataObject();
    }

    public void testDataObject() throws Exception
    {
        DataObject dataObject = DataObjectTest.makeDataObject();

        dataObject.addValue(new Path("dc:title[@type='u']/dc:date[@type='creation']/TERM"), new XmlValue("1990"));
        dataObject.addValue(new Path("dc:title[@type='u']/dc:title[@type='mark' and @kind='try']/TERM"), new XmlValue("2000"));

        {
            List<Path> list = asList(dataObject);

            assertEquals(2, list.size());
            //			assertEquals(list.get(0).iterator().next().getRoot(), list.get(1).iterator().next().getRoot());
            //			assertNotSame(list.get(0).iterator().next().getRoot(), list.get(1).iterator().next().getRoot());
            //			assertNotNull(list.get(0).iterator().next().getRoot());
        }

        {
            // root-children for 1
            //			Path query = new Path("dc:title");
            //			PathElement pathElement = query.iterator().next();
            //			assertTrue(pathElement.getChildren().isEmpty());
            //			assertTrue(pathElement.getRoot() == pathElement);
        }

        {
            // root-children for 2
            List<PathElement> query = asList(new Path("dc:title/dc:date"));
            PathElement title = query.get(1);
            PathElement date = query.get(0);

            //			assertEquals(1, title.getChildren().size());
            //			assertEquals(0, date.getChildren().size());
            //
            //			assertSame(title, title.getRoot());
            //			assertSame(title, date.getRoot());
        }

        {
            // root-children for 3
            Path query = new Path("dc:title/dc:date[@type='creation']/TERM");
            //			List<PathElement> list = asList(query);
            //			PathElement title = list.get(2);
            //
            //			for (int i = 0; i < list.size(); i++) {
            //				PathElement pathElement = list.get(i);
            //				if (i == 0) {
            //					assertTrue(pathElement.getChildren().isEmpty());
            //				} else {
            //					assertEquals(pathElement.getExpanded(), 1, pathElement.getChildren().size());
            //				}
            //				assertSame(title, pathElement.getRoot());
            //			}

            // check data object
            assertEquals(1, dataObject.getValues(query).size());

            assertEquals("1990", dataObject.getValues(query).get(0).getValue());
            assertEquals("2000", dataObject.getValues(
                    new Path("dc:title/dc:title[@type='mark' and @kind='try']/TERM")).get(0).getValue());

            // here the real test is:
            // one att
            assertEquals(1, dataObject.getValues(
                    new Path("dc:title/dc:date[@type]")).size());
            assertEquals("creation", dataObject.getValues(
                    new Path("dc:title/dc:date[@type]")).get(0).getValue());

            // two atts
            assertEquals("mark", dataObject.getValues(new Path("dc:title/dc:title[@type]")).get(0).getValue());
            assertEquals("try", dataObject.getValues(new Path("dc:title/dc:title[@kind]")).get(0).getValue());
            assertEquals(0, dataObject.getValues(new Path("dc:title/dc:title[@third]")).size());
        }

        // just to check if it goes in
        //fileset*file[@name='/Users/borys/Documents/workspace/annocultor/converters/geonames/input_source/EU/AD.rdf']*http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF*http://www.geonames.org/ontology#Feature[@http://www.w3.org/1999/02/22-rdf-syntax-ns#about='http://sws.geonames.org/3038814/']*http://www.geonames.org/ontology#featureClass[@http://www.w3.org/1999/02/22-rdf-syntax-ns#resource='http://www.geonames.org/ontology#T'] 
    }


    public void testAttrInsidePathAnswer() throws Exception
    {
        DataObject dataObject = DataObjectTest.makeDataObject();
        dataObject.addValue(new Path("dc:title[@type='u']/dc:date[@type='creation']/TERM"), new XmlValue("1990"));
        dataObject.addValue(new Path("dc:title[@type='u']/dc:title[@type='mark' and @kind='try']/TERM"), new XmlValue("2000"));

        assertEquals(1, dataObject.getValues(
                new Path("dc:title/dc:date[@type='creation']/TERM")).size());
        assertEquals("1990", dataObject.getValues(
                new Path("dc:title/dc:date[@type='creation']/TERM")).get(0).getValue());
        assertEquals("2000", dataObject.getValues(
                new Path("dc:title/dc:title[@type='mark' and @kind='try']/TERM")).get(0).getValue());

        // here the real test is:
        // one att
        assertEquals(1, dataObject.getValues(
                new Path("dc:title/dc:date[@type]")).size());
        assertEquals("creation", dataObject.getValues(
                new Path("dc:title/dc:date[@type]")).get(0).getValue());

        // two atts
        assertEquals("mark", dataObject.getValues(new Path("dc:title/dc:title[@type]")).get(0).getValue());
        assertEquals("try", dataObject.getValues(new Path("dc:title/dc:title[@kind]")).get(0).getValue());
        assertEquals(0, dataObject.getValues(new Path("dc:title/dc:title[@third]")).size());
    }

    public void testPathAttrQueryClone() throws Exception
    {
        assertEquals(true, new Path("E7L/ELEMENT[@a]").getLast().isAttributeQuery());
        assertEquals(true, new Path(new Path("SS"), new Path("E7L/ELEMENT[@a]")).getLast().isAttributeQuery());

        assertEquals(false, new Path("E7L/ELEMENT").getLast().isAttributeQuery());
    }

    //	public void testPathPrefix() throws Exception
    //	{
    //		Path path = new Path(new Path("X/Y[@z]"), "http://e", "http://a");
    //		assertEquals("http://eX", path.getElements().get(0).getExpanded());
    //		assertEquals("http://eY", path.getElements().get(1).getExpanded());
    //		assertEquals("http://az", path.getElements().get(1).getAttributesAsList().get(0).getExpanded());
    //	}
    //
    //	public void testPathPrefix2() throws Exception
    //	{
    //		Path path = new Path(new Path("X/Y[@z]"), "", "");
    //		assertEquals("X", path.getElements().get(0).getExpanded());
    //		assertEquals("Y", path.getElements().get(1).getExpanded());
    //		assertEquals("z", path.getElements().get(1).getAttributesAsList().get(0).getExpanded());
    //	}

    private static Path getPath() throws Exception
    {
        return new Path("dc:title/dc:date[@type='creation']/TERM");
    }

    public void testPathMultipleResults() throws Exception
    {
        DataObject dataObject = DataObjectTest.makeDataObject();
        dataObject.addValue(new Path("dc:title[@type='u']/dc:date[@type='creation']/TERM"), new XmlValue("1990"));
        dataObject.addValue(new Path("dc:title[@type='u']/dc:title[@type='mark' and @kind='try']/TERM"), new XmlValue("2000"));

        assertEquals(2, dataObject.getValues(new Path("dc:title[@type]")).size());
        assertEquals("u", dataObject.getValues(new Path("dc:title[@type]")).get(0).getValue());
    }
    /*
	private void clearValuesTest(Path pathToDelete) throws Exception
	{
		DataObject dataObject = makeTestDataObjectAttr();
		assertEquals(1, dataObject.getValues(getPath()).size());
		assertEquals("1990", dataObject.getValues(getPath()).get(0).getValue());
		assertEquals("2000", dataObject.getValues(
				new Path("dc:title[@type='u']/dc:title[@type='mark' and @kind='try']/TERM")).get(0).getValue());
		dataObject.clearValues(pathToDelete);
		assertTrue(dataObject.getValues(pathToDelete).isEmpty());
	}

	public void testClearValuesWithAttr() throws Exception
	{
		clearValuesTest(getPath());
	}

	public void testClearValuesSimple() throws Exception
	{
		clearValuesTest(new Path("dc:title"));
	}

	public void testClearValuesAtRootWithAttr() throws Exception
	{
		try
		{
			clearValuesTest(new Path("dc:title@u"));
			fail();
		}
		catch (Exception e)
		{
			// expected
		}
		clearValuesTest(new Path("dc:title[@type='creation']"));
	}

	public void testClearValuesOneAttrOfFew() throws Exception
	{
		clearValuesTest(new Path("dc:title[@type='u']/dc:title[@type='mark']/TERM"));
	}
     */
    public void testXPath() throws Exception
    {
        new Path("/rdf:RDF/dc:Feature[@rdf:about]", new Namespaces());
        new Path("/rdf:RDF/dc:Feature", new Namespaces());
        new Path("/rdf:RDF/dc:Feature/dc:name", new Namespaces());
        assertEquals(
                Namespaces.RDF + "RDF" + Path.PE_SEPARATOR + Namespaces.DC+"Feature[@"+Namespaces.RDF+"about='http://value']", 
                new Path("/rdf:RDF/dc:Feature[@rdf:about='http://value']", new Namespaces()).getPath());
        assertEquals(
                "fileset" + Path.PE_SEPARATOR + "file" + Path.PE_SEPARATOR + Namespaces.RDF+"Vocabulary" + Path.PE_SEPARATOR + ""+Namespaces.DC+"Subjectz/@Subject_ID", 
                new Path("/fileset/file/rdf:Vocabulary/dc:Subjectz[@Subject_ID]", new Namespaces()).getPath());	
        assertFalse(
                new Path("fileset/file/rdf:Vocabulary/dc:Subjectz[@Subject_ID]", new Namespaces())
                .isAbsolute());	
        assertTrue(
                new Path("/fileset/file/rdf:Vocabulary/dc:Subjectz[@Subject_ID]", new Namespaces())
                .getLast().isAttributeQuery());	
        assertTrue(
                new Path("/fileset/file/rdf:Vocabulary/dc:Subjectz[@rdf:resource]", new Namespaces())
                .getLast().isAttributeQuery());	
        assertTrue(
                new Path("/fileset/file/rdf:Vocabulary/dc:Subjectz[@Subject_ID]", new Namespaces())
                .isAbsolute());	


        assertTrue(
                new Path("/dc:Vocabulary/dc:Subject/dc:Terms/dc:Non-Preferred_Term", new Namespaces())
                .isAbsolute());	


    }

    private void makeExplicateTest(Path path, Path... expectation) throws Exception
    {
        List<Path> pp = path.explicate();
        Collections.sort(pp);
        List<Path> ex = new ArrayList<Path>();
        for (Path e : expectation)
        {
            ex.add(e);
        }
        Collections.sort(ex);

        assertEquals(ex.size(), pp.size());
        for (int i = 0; i < ex.size(); i++)
        {
            assertEquals(ex.get(i).getPath(), pp.get(i).getPath());
            if (ex.get(i).getValue() != null)
            {
                assertEquals(ex.get(i).getValue(), pp.get(i).getValue());
            }
        }
    }

    private Path path(String p, String value) throws Exception
    {
        Path pp = new Path(p);
        pp.appendValue(value);
        return pp;
    }

    public void testExplicate() throws Exception
    {
        makeExplicateTest(new Path("e"), new Path("e"));
        makeExplicateTest(new Path("e[@a]"), new Path("e[@a]"), new Path("e"));
        makeExplicateTest(new Path("e[@a='x']"), path("e[@a]", "x"), new Path("e"));
        makeExplicateTest(new Path("e[@a='x' and @b='y']"), path("e[@b]", "y"), path("e[@a]", "x"), new Path("e"));
        makeExplicateTest(new Path("e[@a='x' and @b='y']/z"),
                path("e[@b]", "y"),
                path("e[@a]", "x"),
                new Path("e/z"));
        makeExplicateTest(new Path("e[@a='x' and @b='y']/z/c"),
                path("e[@b]", "y"),
                path("e[@a]", "x"),
                new Path("e/z/c"));
    }

    public void testAttrRepeatingInsidePath() throws Exception
    {
        DataObject dataObject = makeTestDataObject();
        //	System.out.print(dataObject);
        //		assertEquals(1, dataObject.getValues(new Path("dc:language/@a")).size());
    }

    @Test
    public void testDotInPath() throws Exception
    {
        new Path("dc:location.currentRepository");
        Namespaces ns = new Namespaces();
        ns.addNamespace("http://www.com", "wgs84_pos");
        assertTrue(Character.isJavaIdentifierPart('_'));
        new Path("wgs84_pos:lat", ns);

    }

    @Test
    public void testCascadingLang() throws Exception
    {
        assertEquals("en", new Path("dc:location.currentRepository[@xml:lang='en']").getLang());
        assertEquals("nl", new Path("dc:location.currentRepository[@xml:lang='en']/dc:location[@xml:lang='nl']").getLang());
        assertEquals("nl", new Path("dc:location.currentRepository[@xml:lang='en']/dc:location/dc:another[@xml:lang='nl']").getLang());
        assertEquals("nl", new Path("dc:location.currentRepository/dc:location/dc:another[@xml:lang='nl']/dc:titile").getLang());
    }

    @Test
    public void testPathAttrNsEquals() throws Exception
    {
        String paths[] = {"dc:xxx", "dc:rty/dc:n", "dc:x/dc:y[@attr]"};
        for (String path : paths) {
            assertEquals(new Path(path), new Path(path + ""));
        }
    }

    @Test
    public void testTreeLinking() throws Exception {

        Path path = new Path("dc:date/dc:creator/dc:title");
        List<PathElement> list = new ArrayList<PathElement>();
        for (PathElement pathElement : path) {
            list.add(pathElement);
        }

        PathElement title = list.get(0);
        PathElement creator = list.get(1);
        PathElement date = list.get(2);

        // parent
        //		assertEquals(creator, title.getParent());
        //		assertEquals(date, creator.getParent());
        //		assertEquals(null, date.getParent());
        //
        //		// child
        //		assertEquals(0, title.getChildren().size());
        //		assertEquals(1, creator.getChildren().size());
        //		assertEquals(title, creator.getChildren().iterator().next());
        //		assertEquals(1, date.getChildren().size());
        //		assertEquals(creator, date.getChildren().iterator().next());		
        //
        //		// root all the same
        //		for (PathElement pathElement : list) {
        //			assertEquals(date, pathElement.getRoot());			
        //		}
    }
    
    @Test
    public void testChangeNs() throws Exception {

        Namespaces nss = new Namespaces();
        Namespace ns = new Namespace(nss.getUri("dc"), "dc", true);
        Path path = new Path("ac_time:beginDate", nss );
        assertEquals(ns.getUri() + "beginDate", Path.changeNamespace(ns, path).getPath());
        path = new Path("beginDate", nss );
        assertEquals(ns.getUri() + "beginDate", Path.changeNamespace(ns, path).getPath());
    }
}
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
package eu.annocultor.converters.solr;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;


public class WordCountTest {

    @Test
    public void testWC() throws Exception {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("_e", "X; Y");
        assertEquals(
                0,
                SolrDocumentTagger.countWords(document, "_"));
        document.addField("_x", "Xxxx; Yyyy");
        assertEquals(
                2,
                SolrDocumentTagger.countWords(document, "_"));
        document.addField("_y", "Xxxx; Yyyy");
        assertEquals(
                4,
                SolrDocumentTagger.countWords(document, "_"));
        document.addField("_z", "Xxxx;Yxxx, Xxxx");
        assertEquals(
                7,
                SolrDocumentTagger.countWords(document, "_"));
        document.addField("_bb", "Xxxx");
        assertEquals(
                8,
                SolrDocumentTagger.countWords(document, "_"));
        assertEquals(
                0,
                SolrDocumentTagger.countWords(document, "another_prefix"));
    }

    @Test
    public void testCollectionsInWC() throws Exception {
        SolrInputDocument document = new SolrInputDocument();
        List<String> values = new ArrayList<String>();
        values.add("horse");
        values.add("pferd");
        values.add("konj");
        document.addField("_e", values);
        assertEquals(
                3,
                SolrDocumentTagger.countWords(document, "_"));
    }

}

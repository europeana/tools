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
package eu.annocultor.analyzers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Test;

import eu.annocultor.converters.solr.SolrDocumentTagger;


public class SolrPropertyHitsAnalyzerTest {

    @Test
    public void testQE() {
        Assert.assertEquals("Gaelic", SolrPropertyHitsAnalyzer.extractQuery(
                "11:05:01:130 [action=FULL_RESULT_HMTL, europeana_uri=http://www.europeana.eu/resolve/record/00401/418ABBE8008DC6708A019C3C9D77A1A91321FB94, query=Gaelic, start=72, numFound=668, userId=, lang=EN, req=http://acceptance.europeana.vancis.nl:80/portal/record/00401/418ABBE8008DC6708A019C3C9D77A1A91321FB94.html?query=Gaelic&start=72&startPage=61&pageId=brd&view=list&tab=all, date=2011-05-24T11:05:01.130+02:00, ip=81.147.31.222, user-agent=Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27, referer=http://www.google.com/search?client=safari&rls=en&q=donald+thomson+mod&ie=UTF-8&oe=UTF-8, utma=, utmb=, utmc=, v=1.0]"));
        Assert.assertEquals("Absolutissimae in Hebraicam linguam Institutiones Accuratissime", SolrPropertyHitsAnalyzer.extractQuery(
                "23:46:42:005 [action=FULL_RESULT_HMTL, europeana_uri=http://www.europeana.eu/resolve/record/03486/65BE2A35DCC73A67B6A86BFE7675A188266E3C9B, query=, start=, numFound=, userId=, lang=EN, req=http://www.europeana.eu:80/portal/record/03486/65BE2A35DCC73A67B6A86BFE7675A188266E3C9B.html, date=2011-05-23T23:46:42.005+02:00, ip=76.84.83.8, user-agent=Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1, referer=http://www.google.com/search?client=safari&rls=en&q=Absolutissimae+in+Hebraicam+linguam+Institutiones+Accuratissime&ie=UTF-8&oe=UTF-8, utma=, utmb=, utmc=, v=1.0]"));
        }

//    @Test
//    public void testHasWord() {
//        Assert.assertTrue(SolrPropertyHitsAnalyzer.hasWord(SolrPropertyHitsAnalyzer.split("horse and cow"), "horse"));
//        Assert.assertTrue(SolrPropertyHitsAnalyzer.hasWord(SolrPropertyHitsAnalyzer.split("horse, and cow"), "horse"));
//        Assert.assertFalse(SolrPropertyHitsAnalyzer.hasWord(SolrPropertyHitsAnalyzer.split("horses and cow"), "horse"));
//        Assert.assertTrue(SolrPropertyHitsAnalyzer.hasWord(SolrPropertyHitsAnalyzer.split("Monographs published later than in 1880 are digitally"), "later"));
//        Assert.assertTrue(SolrPropertyHitsAnalyzer.hasWord(SolrPropertyHitsAnalyzer.split("Another. Example: Monographs published later than in 1880 are digitally"), "later"));
//    }

    @Test
    public void testWC() throws Exception {
        SolrDocument document = new SolrDocument();
        document.addField("_e", "X; Y");
        Assert.assertFalse(SolrPropertyHitsAnalyzer.hasWord(document, "_", "X"));
        document.addField("_x", "Xxxx; Yyyy");
        Assert.assertTrue(SolrPropertyHitsAnalyzer.hasWord(document, "_", "Xxxx"));
        document.addField("_y", "Xxxx; Yyyy");
        Assert.assertTrue(SolrPropertyHitsAnalyzer.hasWord(document, "_", "Yyyy"));
        Assert.assertFalse(SolrPropertyHitsAnalyzer.hasWord(document, "another_prefix", "keyeee"));
    }

    @Test
    public void testCollections() throws Exception {
        SolrDocument document = new SolrDocument();
        List<String> values = new ArrayList<String>();
        values.add("horse");
        values.add("pferd");
        values.add("konj");
        document.addField("_e", values);
        Assert.assertTrue(SolrPropertyHitsAnalyzer.hasWord(document, "_", "horse"));
    }

}

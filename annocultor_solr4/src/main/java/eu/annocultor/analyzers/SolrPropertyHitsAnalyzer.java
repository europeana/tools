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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrPropertyHitsAnalyzer {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        String solrUrl = args[0];
        SolrServer solr = new HttpSolrServer(solrUrl);

        String prefixOne = args[1];
        String prefixTwo = args[2];

        long prefixOneCount = 0;
        long prefixTwoCount = 0;

        long totalPassedCount = 0;

        for (File logLocation : FileUtils.listFiles(new File(args[3]), null, true)) {
            System.out.println("Parsing " + logLocation);
           
            for (String line : FileUtils.readLines(logLocation)) {
                if (StringUtils.contains(line, "FULL_RESULT_HMTL")) {
                    line = StringUtils.substringAfter(line, "europeana_uri=");
                    String solrDocumentId = StringUtils.substringBefore(line, ",");
                    String query = extractQuery(line);
                    if (StringUtils.startsWith(solrDocumentId, "http://") && isLongEnoughToCount(query)) {

                        SolrQuery solrQuery = new SolrQuery("europeana_uri:\"" + solrDocumentId + "\"");
                        QueryResponse response = solr.query(solrQuery);                        
                        SolrDocumentList sourceDocs = response.getResults();
                        if (sourceDocs.isEmpty()) {
                            System.out.println("Could not find object " + solrDocumentId);
                        } else {
                            SolrDocument document = sourceDocs.get(0);

                            if (hasWord(document, prefixOne, query)) {
                                prefixOneCount ++;
                            } else {
                                if (hasWord(document, prefixTwo, query)) {
                                    prefixTwoCount ++;
                                }
                            }
                        }
                    }
                    totalPassedCount ++;
                }
            }
            System.out.println(prefixOne + " : " + prefixOneCount + " " + prefixTwo + " : " + prefixTwoCount + " of total passed entries " + totalPassedCount);
        }
    }

    static boolean hasWord(SolrDocument document, String fieldNamePrefix, String textToFind) {

        String[] wordsToFind = textToFind.toString().split("[\\W]+");  
        Set<String> docToLog = new HashSet<String>();
        Set<String> queryToLog = new HashSet<String>();
        for (String keyword : wordsToFind) {
            if (isLongEnoughToCount(keyword)) {
                queryToLog.add(keyword);
                for (String fieldName : document.getFieldNames()) {
                    if (fieldName.startsWith(fieldNamePrefix)) {
                        for(Object fieldValue : document.getFieldValues(fieldName)) {
                            if (fieldValue != null) {
                                for (String word : split(fieldValue)) {
                                    docToLog.add(word);
                                    if (StringUtils.equalsIgnoreCase(word, keyword)) {
                                        System.out.println(fieldNamePrefix + "-T: Q: " + StringUtils.join(queryToLog, ",") + "; doc: "+ StringUtils.join(docToLog, ","));
                                        return true;
                                    }                                    
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println(fieldNamePrefix + "-F: Q: " + StringUtils.join(queryToLog, ",") + "; doc: "+ StringUtils.join(docToLog, ","));
        return false;
    }

    static Collection<String> split(Object fieldValue) {
        List<String> words = new ArrayList<String>();
        for (String word : fieldValue.toString().split("[\\W]+")) {
            if (isLongEnoughToCount(word)) {
                words.add(word);
            }
        }
        return words;
    }

    static boolean isLongEnoughToCount(String word) {
        return StringUtils.length(word) > 3 && StringUtils.isAlphaSpace(word);
    }

    static String extractQuery(String line) {
        line = StringUtils.substringAfter(line, ", query=");
        String query = StringUtils.substringBefore(line, ", ");
        if (StringUtils.length(query) < 3) {
            // try referal
            query = StringUtils.substringAfter(line, ", referer=");
            query = StringUtils.substringAfter(query, "&q=");
            if (!StringUtils.isBlank(query)) {
                query = StringUtils.substringBefore(query, "&");
                query = StringUtils.replace(query, "+", " ");
                if (isLongEnoughToCount(query)) {
                    return query;
                } else {
                    return "";
                }
            }
        }
        return query;
    }
}

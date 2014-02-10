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
package eu.europeana.enrichment.converters.solr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.enrichment.converters.europeana.Entity;


/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 *
 */
public abstract class SolrDocumentTagger {

    private static final int MIN_WORD_LENGTH_TO_INCLUDE_IN_WORD_COUNT = 3;

    String[] FIELDS_TO_EXTRACT = new String[] {
            //"europeana_uri","europeana_collectionName","dc_date","dcterms_spatial","dc_coverage"
    };

    private int MAX_RETRIES = 10;
    private int DOCUMENT_QUEUE_SIZE_TO_FLUSH = 1000;
    private int DOCUMENTS_PER_READ = 100000;
    private int MAX_PAGES_TO_TAG = 1000;


    HttpSolrServer solrServerFrom;
    HttpSolrServer solrServerTo;
    int start;

    private String query;

    private String idFieldName;

//    PrintWriter log;

    private long originalWordCount;

    private long enrichmentWordCount;

    public SolrDocumentTagger(
            String idFieldName,
            String query,
            String solrServerFrom,
            String solrServerTo,
            int start,
            PrintWriter log) throws MalformedURLException {
        this.idFieldName = idFieldName;
        this.query = query;
        this.solrServerFrom = new HttpSolrServer(solrServerFrom);
        this.solrServerTo =  new HttpSolrServer(solrServerTo);
        this.solrServerTo.setConnectionTimeout(6000000);
        this.start = start;
    }

    public SolrDocumentTagger(){

    }
    public abstract boolean shouldReplicateThisField(String fieldName);

    public abstract void preProcess(SolrInputDocument document, String id);

    public abstract List<Entity> tagDocument(SolrInputDocument document) throws Exception;


    private void flush(List<SolrInputDocument> destDocs) throws Exception {
        if (destDocs.size() > DOCUMENT_QUEUE_SIZE_TO_FLUSH) {
            solrServerTo.add(destDocs);
            destDocs.clear();
        }
    }

   
    
    public int tag() throws Exception {
        int recordsPassed = 0;
        for (int page = 0; true; page++) {
            int queryStart = page * DOCUMENTS_PER_READ;
            int queryEnd = queryStart + DOCUMENTS_PER_READ;
            if (queryEnd > start) {

                SolrQuery solrQuery = new SolrQuery(query);
                solrQuery.setStart(queryStart);
                solrQuery.setRows(DOCUMENTS_PER_READ);
                if (FIELDS_TO_EXTRACT.length > 0) {
                    solrQuery.setFields(FIELDS_TO_EXTRACT);
                }
                QueryResponse response = solrServerFrom.query(solrQuery);
                SolrDocumentList sourceDocs = response.getResults();
                System.out.println("retrieved document query OK: "+sourceDocs.getNumFound());
                if (sourceDocs.isEmpty() || page > MAX_PAGES_TO_TAG) {
                    return recordsPassed;
                }

                int retry = 0;
                while (retry < MAX_RETRIES) {
                    try {
                        List<SolrInputDocument> destDocs = new ArrayList<SolrInputDocument>();
                        tagDocumentList(sourceDocs, destDocs);
                        recordsPassed += sourceDocs.size();
                        System.out.println("Let's try");
                        solrServerTo.add(destDocs);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.sleep(60000 * retry);
                        retry ++;
                    } finally {
                    }
                }

                if (retry >= MAX_RETRIES) {
                    throw new Exception("Failed completely.");
                }
            } else {
            }
        }
    }

    void tagDocumentList(SolrDocumentList sourceDocs, List<SolrInputDocument> destDocs) 
    throws Exception, IOException, SolrServerException {
        for (SolrDocument sourceDocument : sourceDocs) {
            SolrInputDocument destDocument = new SolrInputDocument();
            for (String fieldName : sourceDocument.getFieldNames()) {
                if (shouldReplicateThisField(fieldName)) {
                    final Object fieldValue = sourceDocument.getFieldValue(fieldName);
                    destDocument.addField(fieldName, fieldValue);
                }
            }

            // there is one below

            String id = sourceDocument.getFirstValue(idFieldName).toString();
            preProcess(destDocument, id);
            tagDocument(destDocument);            
            destDocs.add(destDocument);

            // there is one above

            flush(destDocs);
        }

    }

    static int countWords(SolrInputDocument document, String fieldNamePrefix) {
        int count = 0;
        for (String fieldName : document.getFieldNames()) {
            if (fieldName.startsWith(fieldNamePrefix)) {
                for(Object fieldValue : document.getFieldValues(fieldName)) {
                    if (fieldValue != null) {
                        String delims = "[\\W]+";
                        String[] words = fieldValue.toString().split(delims);  
                        for (String word : words) {
                            if (!StringUtils.isBlank(word) && word.length() >= MIN_WORD_LENGTH_TO_INCLUDE_IN_WORD_COUNT) {
                                count ++;
                            }
                        }
                    }   
                }
            }
        }
        return count;
    }
    
    protected String fieldNamePrefixForOriginalMetadata() {
        return "dc";
    }

    protected String fieldNamePrefixForEnrichmentMetadata() {
        return "enrichment";
    }
}

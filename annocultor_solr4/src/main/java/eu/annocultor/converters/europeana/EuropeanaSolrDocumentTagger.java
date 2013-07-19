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
package eu.annocultor.converters.europeana;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.context.Environment;
import eu.annocultor.context.EnvironmentImpl;
import eu.annocultor.converters.solr.BuiltinSolrDocumentTagger;
import eu.annocultor.reports.ReportPresenter;


/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 *
 */
public class EuropeanaSolrDocumentTagger extends BuiltinSolrDocumentTagger
{

    @Override
    public boolean shouldReplicateThisField(String fieldName) {
        // remove old enrichments
        if (fieldName.startsWith("europeana_")) {
            return false;
        }
        return fieldName.contains("_") || fieldName.equalsIgnoreCase("timestamp");
    }

    // copied from IngestionUtils
    String makeRecordHash(String resolveUri) {
        if (resolveUri.contains("/")) {
            resolveUri = StringUtils.substringAfterLast(resolveUri, "/");
        }
        return StringUtils.substringBeforeLast(resolveUri, ".");
    }

    // copied from ESEImporterImpl
    private static final String FIELD_HASH_FIRST_SIX = "europeana_recordHashFirstSix";
    private static final String FIELD_HASH = "europeana_recordHash";
    private static final String FIELD_EDM_CLASS = "europeana_edm_class";
    
    public  void addHash(SolrInputDocument document, String id) {
        final String hash = makeRecordHash(id);
        document.setField(FIELD_HASH_FIRST_SIX, StringUtils.substring(hash, 0, 6));
        document.setField(FIELD_HASH, hash);
        document.setField(FIELD_EDM_CLASS, "EuropeanaProxy");

    }

    private static final String COMPLETENESS = "europeana_completeness";
    @Override
    public void preProcess(SolrInputDocument document, String id) {
        addHash(document, id);
        document.setField(COMPLETENESS, RecordCompletenessRanking.rankRecordCompleteness(document));
    }

    public EuropeanaSolrDocumentTagger(
            String query, 
            String solrServerFrom,
            String solrServerTo, 
            int start,
            PrintWriter log) 
    throws MalformedURLException {
        super("europeana_id", query, solrServerFrom, solrServerTo, start, log);
    }

    public static void main(String... commandLine) throws Exception {

        if (commandLine.length != 4) {
            System.err.println("Expected: solrServerFrom solrServerTo query start");
        } else {
            String solrServerFrom = commandLine[0];
            String solrServerTo = commandLine[1];
            String query = commandLine[2];
            int start = Integer.parseInt(commandLine[3]);

            start(solrServerFrom, solrServerTo, query, start);
        }
    }

    public void delete(String collectionName, String solrServerFrom, String solrServerTo, int start, PrintWriter log ) throws SolrServerException, IOException{
    	EuropeanaSolrDocumentTagger tagger = new EuropeanaSolrDocumentTagger(collectionName, solrServerFrom, solrServerTo, start, log);
    	tagger.deleteCollection(collectionName);
    }
    
    public static void start(String solrServerFrom, String solrServerTo, String query, int start)
    throws MalformedURLException, Exception {

        Environment environment = new EnvironmentImpl();

        PrintWriter log = new PrintWriter(new FileWriter(new File(environment.getAnnoCultorHome(), "enrichment.log")));
        try {
            EuropeanaSolrDocumentTagger tagger = new EuropeanaSolrDocumentTagger(query, solrServerFrom, solrServerTo, start, log);
            tagger.init("Europeana");
            if (start == 0) {
                tagger.clearDestination(query);
            }
            tagger.tag();
            tagger.report();
            ReportPresenter.generateReport(environment.getAnnoCultorHome());
        }
        catch (Exception e) {
            e.printStackTrace(new PrintWriter(log));
        } finally {
            log.write("SEMANTIC ENRICHMENT COMPLETED");
            log.flush();
            log.close();
        }
    }

}

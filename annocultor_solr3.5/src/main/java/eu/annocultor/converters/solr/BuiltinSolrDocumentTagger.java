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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.api.Factory;
import eu.annocultor.api.ObjectRule;
import eu.annocultor.api.Task;
import eu.annocultor.common.Language;
import eu.annocultor.context.Environment;
import eu.annocultor.context.EnvironmentImpl;
import eu.annocultor.context.Namespaces;
import eu.annocultor.converters.europeana.Entity;
import eu.annocultor.converters.europeana.EuropeanaLabelExtractor;
import eu.annocultor.data.destinations.AbstractFileWritingGraph;
import eu.annocultor.path.Path;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.tagger.rules.LookupPersonRule;
import eu.annocultor.tagger.rules.LookupPlaceRule;
import eu.annocultor.tagger.rules.LookupTermRule;
import eu.annocultor.tagger.rules.LookupTimeRule;
import eu.annocultor.tagger.rules.PairOfStrings;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.VocabularyOfPeople;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.tagger.vocabularies.VocabularyOfTime;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Property;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.DataObject;


/**
 * Tagging (aka semantic enrichment) of records from SOLR with built-in vocabularies.
 * 
 * @author Borys Omelayenko
 *
 */
public abstract class BuiltinSolrDocumentTagger extends SolrDocumentTagger
{

    @Override
    public List<Entity> tagDocument(SolrInputDocument document) throws Exception {
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(periodsTagger.tag(document));
        entities.addAll(placesTagger.tag(document));
        entities.addAll(categoriesTagger.tag(document));
        entities.addAll(peopleTagger.tag(document));
        return entities;
    }

    @Override
    public void report() throws Exception {
        task.getReporter().report();
    }

    protected VocabularyOfTime vocabularyOfPeriods = new VocabularyOfTime("vocabularyOfTime", null) {

        @Override
        public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
            return label.toLowerCase();
        }

        @Override
        protected void logMessage(String message) throws IOException {
            log.write(message + "\n");
            log.flush();
        }

    };

    protected VocabularyOfPlaces vocabularyOfPlaces = new VocabularyOfPlaces("vocabularyOfPlaces", null) {

        @Override
        public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
            return label.toLowerCase();
        }

        @Override
        protected void logMessage(String message) throws IOException {
            log.write(message + "\n");
            log.flush();
        }

    };

    protected VocabularyOfTerms vocabularyOfTerms = new VocabularyOfTerms("vocabularyOfConcepts", null) {

        @Override
        public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
            return StringUtils.lowerCase(label);
        }

        @Override
        protected void logMessage(String message) throws IOException {
            log.write(message + "\n");
            log.flush();
        }

    };

    protected VocabularyOfPeople vocabularyOfPeople = new VocabularyOfPeople("vocabularyOfActors", null) {

        @Override
        public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
            return StringUtils.lowerCase(label);
        }

        @Override
        protected void logMessage(String message) throws IOException {
            log.write(message + "\n");
            log.flush();
        }

    };

    protected Environment environment = new EnvironmentImpl();

    Task task;

    ObjectRule objectRule;

    SolrTagger periodsTagger;
    SolrTagger placesTagger;
    SolrTagger categoriesTagger;
    SolrTagger peopleTagger;

    public BuiltinSolrDocumentTagger(
            String idFieldName,
            String query,
            String solrServerFrom,
            String solrServerTo,
            int start,
            PrintWriter log) throws MalformedURLException {

        super(idFieldName, query, solrServerFrom, solrServerTo, start, log);
    }

    public BuiltinSolrDocumentTagger(){
    	
    }
    private String makePlaceCoordinateQuery(String property) {
        return "PREFIX places: <http://www.w3.org/2003/01/geo/wgs84_pos#> " +    
        "SELECT ?code ?" + property + " " +
        "WHERE { ?code places:" + property + " ?" + property + " }";
    }

    private String makePlacePropertyQuery(String property) {
        return "PREFIX places: <http://www.europeana.eu/resolve/ontology/> " +    
        "SELECT ?code ?" + property + " " +
        "WHERE { ?code places:" + property + " ?" + property + " }";
    }

    private String makeTimePropertyQuery(String property) {
        return "PREFIX time: <http://semium.org/time/> " +     
        "SELECT ?code ?endpoint " +
        "WHERE { ?code time:" + property + " ?endpoint} ";
    }

    private String makePeoplePropertyQuery(String property) {
        return "PREFIX people: <http://dbpedia.org/ontology/> " +    
        "SELECT ?code ?" + property + " " +
        "WHERE { ?code people:" + property + " ?" + property + " }";
    }

    public void clearDestination(String query) throws Exception {
        solrServerTo.deleteByQuery(query);
        if ("*:*".equals(query)) {
            optimize();
        }
    }
    
    public void deleteCollection(String collection) throws SolrServerException, IOException{
    	log.println("Deleting collection by query: "+collection);
    	System.out.println("Deleting collection by query: "+collection);
    	solrServerTo.deleteByQuery(collection);
    	log.println("Deleted collection by query: "+collection);
    	System.out.println("Deleted collection by query: "+collection);
    	solrServerTo.commit();
    }

    public void optimize() throws Exception {
        log.println("Started optimize...");
        
        solrServerTo.optimize();       
        log.println("done.\n");
        log.flush();
    }
    

    
    public void init(String name) throws Exception {

        task = Factory.makeTask(name, "", "Solr tagging with time and place", Namespaces.ANNOCULTOR_CONVERTER, environment); 

        environment.getDocDir().delete();
        AbstractFileWritingGraph.createTempDocToShowWhileWorking(
                new File(environment.getDocDir(), task.getDatasetId() + "/index.html"));

        objectRule = ObjectRuleImpl.makeObjectRule(task, 
                new Path(""), 
                new Path(""), 
                new Path(""), 
                null, 
                false);

        File cacheDir = new File(environment.getVocabularyDir() + "/tmp");
        File baseDir = environment.getVocabularyDir();

        String placeFiles = "places/EU/*.rdf";
        String countryFiles = "places/countries/*.rdf";
        vocabularyOfPlaces.loadTermsSPARQL(vocabularyOfPlaces.makeTermsQuery("dcterms:isPartOf"), cacheDir, baseDir, placeFiles, countryFiles);
        vocabularyOfPlaces.loadTermPropertiesSPARQL("population", makePlacePropertyQuery("population"), cacheDir, baseDir, placeFiles, countryFiles);
        vocabularyOfPlaces.loadTermPropertiesSPARQL("division", makePlacePropertyQuery("division"), cacheDir, baseDir, placeFiles, countryFiles);
        vocabularyOfPlaces.loadTermPropertiesSPARQL("latitude", makePlaceCoordinateQuery("lat"), cacheDir, baseDir, placeFiles, countryFiles);
        vocabularyOfPlaces.loadTermPropertiesSPARQL("longitude", makePlaceCoordinateQuery("long"), cacheDir, baseDir, placeFiles, countryFiles);
        vocabularyOfPlaces.loadTermPropertiesSPARQL("country", makePlacePropertyQuery("country"), cacheDir, baseDir, placeFiles, countryFiles);

        String timeFiles = "time/*.rdf";
        vocabularyOfPeriods.loadTermsSPARQL(vocabularyOfPeriods.makeTermsQuery("dcterms:isPartOf"), cacheDir, baseDir, timeFiles);
        vocabularyOfPeriods.loadTermPropertiesSPARQL("begin", 
                makeTimePropertyQuery("beginDate"),
                cacheDir, baseDir, timeFiles
        );
        vocabularyOfPeriods.loadTermPropertiesSPARQL("end", 
                makeTimePropertyQuery("endDate"),
                cacheDir, baseDir, timeFiles
        );

        vocabularyOfTerms.loadTermsSPARQL(vocabularyOfTerms.makeTermsQuery("skos:broader"), cacheDir, baseDir, "concepts/gemet/gemet*.rdf");
        vocabularyOfTerms.loadTermsSPARQL(vocabularyOfTerms.makeTermsQuery("skos:broader"), cacheDir, baseDir, "concepts/wikipedia/*.rdf");

        String peopleFiles = "people/*.rdf";
        vocabularyOfPeople.loadTermsSPARQL(vocabularyOfPeople.makeTermsQuery("dcterms:isPartOf"), cacheDir, baseDir, peopleFiles);
        vocabularyOfPeople.loadTermPropertiesSPARQL("birth", makePeoplePropertyQuery("birth"), cacheDir, baseDir, peopleFiles);
        vocabularyOfPeople.loadTermPropertiesSPARQL("death", makePeoplePropertyQuery("death"), cacheDir, baseDir, peopleFiles);

        periodsTagger = new SolrPeriodsTagger(
                vocabularyOfPeriods, 

                "edm_timespan", 
                "ts_skos_prefLabel",
                "ts_edm_begin",
                "ts_edm_end",
                "ts_dcterms_isPartOf",
                "ts_dcterms_isPartOf_label",

                new SolrTagger.FieldRulePair("proxy_dc_date", makePeriodLookupRule("proxy_dc_date")),
                new SolrTagger.FieldRulePair("proxy_dc_coverage", makePeriodLookupRule("proxy_dc_coverage")),
                new SolrTagger.FieldRulePair("proxy_dcterms_temporal", makePeriodLookupRule("proxy_dcterms_temporal")),
                new SolrTagger.FieldRulePair("proxy_edm_year",makePeriodLookupRule("proxy_edm_year"))
        		);

        placesTagger = new SolrPlacesTagger(
                vocabularyOfPlaces,

                "edm_place", 
                "pl_skos_prefLabel", 
                "pl_dcterms_isPartOf",
                "pl_dcterms_isPartOf_label",

                new SolrTagger.FieldRulePair("proxy_dcterms_spatial", makePlaceLookupRule("proxy_dcterms_spatial")),
                new SolrTagger.FieldRulePair("proxy_dc_coverage", makePlaceLookupRule("proxy_dc_coverage")));
        
        categoriesTagger = new SolrConceptsTagger(
                vocabularyOfTerms,

                "skos_concept", 
                "cc_skos_prefLabel",
                "cc_skos_broader",
                "cc_skos_altLabel",
               

                new SolrTagger.FieldRulePair("proxy_dc_type", makeTermLookupRule("proxy_dc_type")),
                new SolrTagger.FieldRulePair("proxy_dc_subject", makeTermLookupRule("proxy_dc_subject")));

        peopleTagger = new SolrPeopleTagger(
                vocabularyOfPeople,

                "edm_agent", 
                "agent_prefLabel", 

                new SolrTagger.FieldRulePair("proxy_dc_creator", makePersonLookupRule("proxy_dc_creator")),
                new SolrTagger.FieldRulePair("proxy_dc_contributor", makePersonLookupRule("proxy_dc_contributor")));
    }

    LookupTimeRule makePeriodLookupRule(String field) throws Exception {
        LookupTimeRule rule = new LookupTimeRule(
                null, 
                null, 
                eu.annocultor.api.Factory.makeIgnoreGraph(task, ""), 
                null, 
                null, 
                "periods", 
                "(no_split_should_ever_happen)", 
                vocabularyOfPeriods) {

            @Override
            protected void processLookupMatch(
                    TermList terms,
                    String termUri, 
                    String subject,
                    DataObject dataObject, 
                    boolean createTermDefinion)
            throws Exception {
                // skip it
            }

            // missing years are reported all together
            @Override
            protected void reportMatch(TermList terms) throws Exception {

                for (Term term : terms) {
                    if (!term.getLabel().matches("^(\\d\\d\\d\\d)$")) {
                        super.reportMatch(terms);
                        return;
                    }
                }

                TermList genericYear = new TermList();
                genericYear.add(new Term("A year of four digits", null, new CodeURI("http://semium.org/time/year/XXXX"), "time"));
                super.reportMatch(genericYear);
            }

            @Override
            protected PairOfStrings splitToStartAndEnd(DataObject converter, String label, Language.Lang lang) {
                return eu.annocultor.converters.europeana.EuropeanaTimeUtils.splitToStartAndEnd(label);
            }

            @Override
            public Triple onInvocation(Triple sourceTriple, DataObject sourceDataObject) throws Exception {

                String termLabel = sourceTriple.getValue().getValue();
                // removing "made" and "printed" used by some providers 
                termLabel = StringUtils.removeEnd(termLabel, " made");
                termLabel = StringUtils.removeEnd(termLabel, " printed");
                termLabel = StringUtils.removeEnd(termLabel, " built");
                termLabel = StringUtils.removeEnd(termLabel, " existed");
                termLabel = StringUtils.removeEnd(termLabel, " written");
                termLabel = StringUtils.removeEnd(termLabel, " photographed");
                termLabel = StringUtils.removeEnd(termLabel, " surveyed");
                termLabel = StringUtils.removeEnd(termLabel, " manufactured");
                termLabel = StringUtils.removeEnd(termLabel, " taken");
                termLabel = StringUtils.removeEnd(termLabel, " first published");
                termLabel = StringUtils.removeEnd(termLabel, " published");
                termLabel = StringUtils.removeEnd(termLabel, " cuttings collected");

                // remove trailing ,, e.g  19 siete,
                termLabel = StringUtils.removeEnd(termLabel, ",");

                return sourceTriple.changeValue(new LiteralValue(termLabel));
            }

        };

        rule.setObjectRule(objectRule);
        rule.setTask(task);
        rule.setSourcePath(new Path(field));
        //        rule.addLabelExtractor(new EuropeanaLabelExtractor(false));
        return rule;
    }

    LookupPlaceRule makePlaceLookupRule(String field) throws Exception {
        LookupPlaceRule rule = new LookupPlaceRule(
                null, 
                null, 
                eu.annocultor.api.Factory.makeIgnoreGraph(task, ""), 
                null, 
                null, 
                "places", 
                "(no_split_should_ever_happen)", 
                vocabularyOfPlaces) {

            @Override
            protected void processLookupMatch(
                    TermList terms,
                    String termUri, 
                    String subject,
                    DataObject dataObject, 
                    boolean createTermDefinion)
            throws Exception {
                // skip it
            }

        };

        rule.setObjectRule(objectRule);
        rule.setTask(task);
        rule.setSourcePath(new Path(field));
        rule.addLabelExtractor(new EuropeanaLabelExtractor(false));

        return rule;
    }

    LookupTermRule makeTermLookupRule(String field) throws Exception {
        LookupTermRule rule = new LookupTermRule(
                null, 
                null, 
                eu.annocultor.api.Factory.makeIgnoreGraph(task, ""), 
                null, 
                null, 
                "places", 
                "(no_split_should_ever_happen)", 
                vocabularyOfTerms) {

            @Override
            protected void processLookupMatch(
                    TermList terms,
                    String termUri, 
                    String subject,
                    DataObject dataObject, 
                    boolean createTermDefinion)
            throws Exception {
                // skip it
            }

        };

        rule.setObjectRule(objectRule);
        rule.setTask(task);
        rule.setSourcePath(new Path(field));
        rule.addLabelExtractor(new EuropeanaLabelExtractor(false));

        return rule;
    }

    LookupPersonRule makePersonLookupRule(String field) throws Exception {
        LookupPersonRule rule = new LookupPersonRule(
                null, 
                new Property("dummy"), 
                eu.annocultor.api.Factory.makeIgnoreGraph(task, ""), 
                eu.annocultor.api.Factory.makeIgnoreGraph(task, ""), 
                new Path("dummyBirthDatePath"),
                new Path("dummyDeathDatePath"),
                new Property("dummy"), 
                "actors", 
                "(no_split_should_ever_happen)", 
                vocabularyOfPeople) {

            @Override
            protected void processLookupMatch(
                    TermList terms,
                    String termUri, 
                    String subject,
                    DataObject dataObject, 
                    boolean createTermDefinion)
            throws Exception {
                // skip it
            }

        };

        rule.setObjectRule(objectRule);
        rule.setTask(task);
        rule.setSourcePath(new Path(field));
        rule.addLabelExtractor(new EuropeanaLabelExtractor(false));

        return rule;
    }

}

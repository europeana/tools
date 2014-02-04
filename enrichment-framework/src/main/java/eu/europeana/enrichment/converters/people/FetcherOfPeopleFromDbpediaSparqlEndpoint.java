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
package eu.europeana.enrichment.converters.people;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.context.EnvironmentImpl;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPeople;


/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 *
 */
public class FetcherOfPeopleFromDbpediaSparqlEndpoint 
{
    protected VocabularyOfPeople vocabularyOfPeople = new VocabularyOfPeople("dbpedia.selected.artists", null) {

        @Override
        public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
            return label;//StringUtils.lowerCase(label);
        }

    };

    protected Environment environment = new EnvironmentImpl();
 
    public void fetch() throws Exception {

        // from http://dbpedia.org/snorql/?describe=http%3A//dbpedia.org/class/yago/Painter110391653
        String[] dbpediaCategories = {
                "GermanRenaissancePainters",
                "BolognesePainters",
                "ImpressionistPainters",
                "ModernPainters",
                "RenaissancePainters",
                "VenetianPainters",
                "JapanesePainters",
                "Post-impressionistPainters",
                "FlemishBaroquePainters",
                "PaintersFromSeville",
                "SwedishPainters",
                "CzechPainters",
                "GreekPainters",
                "BulgarianPainters",
                "AustralianPainters",
                "FinnishPainters",
                "HungarianPainters",
                "SovietPainters",
                "IsraeliPainters",
                "ItalianStillLifePainters",
                "FrenchRenaissancePainters",
                "PeruvianPainters",
                "LatvianPainters",
                "AncientGreekVasePainters",
                "AlbanianPainters",
                "ValencianPainters",
                "VeronesePainters",
                "PorcelainPainters",
                "PaintersFromVicenza",
                "LandscapePainters",
                "VicenzanPainters",
                "CatalanPainters",
                "FlemishPainters",
                "DutchGoldenAgePainters",
                "ContemporaryPainters",
                "GothicPainters",
                "NeoclassicalPainters",
                "RealistPainters",
                "SymbolistPainters",
                "NorwegianPainters",
                "AustrianPainters",
                "SwissPainters",
                "UkrainianPainters",
                "AncientGreekPainters",
                "BaroquePainters",
                "SpanishRenaissancePainters",
                "LithuanianPainters",
                "SerbianPainters",
                "LuxembourgianPainters",
                "AncientRomanPainters",
                "SpanishGenrePainters",
                "NeoclassicaPainters",
                "FrenchBaroquePainters",
                "TuscanPainters",
                "FrenchPainters",
                "GermanPainters",
                "ItalianBaroquePainters",
                "RomanticPainters",
                "RococoPainters",
                "JewishPainters",
                "EarlyNetherlandishPainters",
                "IrishPainters",
                "RussianPainters",
                "BritishPainters",
                "WelshPainters",
                "BasquePainters",
                "BelarusianPainters",
                "SpanishBaroquePainters",
                "IcelandicPainters",
                "PortuguesePainters",
                "SlovenianPainters",
                "SicilianPainters",
                "GeorgianPainters",
                "GenoesePainters",
                "RomaniPainters",
                "MoldovanPainters",
                "SpanishFloralStillLifePainters",
                "SevillianPainters",
                "ViennesePainters",
                "AbstractPainters",
                "ItalianPainters",
                "DutchPainters",
                "ByzantinePainters",
                "MedievalPainters",
                "SpanishPainters",
                "EnglishPainters",
                "FlemishRenaissancePainters",
                "PolishPainters",
                "DanishPainters",
                "BelgianPainters",
                "ScottishPainters",
                "NeapolitanPainters",
                "UmbrianPainters",
                "RomanianPainters",
                "ItalianBattlePainters",
                "EstonianPainters",
                "CroatianPainters",
                "MaltesePainters",
                "PersianPainters",
                "MilanesePainters",
                "SlovakPainters",
                "MontenegrinPainters",
                "MacedonianPainters",
                "ExpressionistPainters",
                "NeoclassicPainters",
                "SpanishBattlePainters",
                "ItalianNeoclassicalPainters",
                "18th-centuryPainters",
                "KosovarPainters",
                "DanishBaroquePainters",
                "BelgianNeoclassicalPainters",
                "14th-centuryPainters",
                "FrenchNeoclassicalPainters",
                "ImperialRussianPainters",
                "PaintersFromSaintPetersburg",
                "ItalianRenaissancePainters",
                "ItalianFuturistPainters",
                "BritishBaroquePainters",
                "GermanBaroquePainters",
                "GermanNeoclassicalPainters",
                "BritishNeoclassicalPainters",
                "PaintersFromNorthernIreland",
                "17th-centuryPainters",
                "16th-centuryPainters",
                "20th-centuryPainters",
                "19th-centuryPainters",
                "15th-centuryPainters",
                "19thCenturyItalianPainters"
        };

        File cacheDir = new File(environment.getVocabularyDir() + "/tmp");

        for (String category : dbpediaCategories) {
            try {
            vocabularyOfPeople.loadTermsFromSparqlEndpoint(
                    makeDbpediaSparqlQuery(category, "name", "rdfs:label"), cacheDir, new URL("http://dbpedia.org/sparql"));    
            vocabularyOfPeople.loadTermPropertiesFromSparqlEndpoint(
                    "birth", makeDbpediaSparqlQuery(category, "birth", "dbo:birthDate"), cacheDir, new URL("http://dbpedia.org/sparql"));    
            vocabularyOfPeople.loadTermPropertiesFromSparqlEndpoint(
                    "death", makeDbpediaSparqlQuery(category, "death", "dbo:deathDate"), cacheDir, new URL("http://dbpedia.org/sparql"));
            } catch (Exception e) {
                System.out.println("dbpedia category " + category + " does not have anything under it");
                e.printStackTrace();
            }
        }
    }

    String makeDbpediaSparqlQuery(String category, String field, String property) {
        return
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
        "PREFIX dbo: <http://dbpedia.org/ontology/> " +
        "SELECT ?person ?" + field + " WHERE { " +
        " ?person rdf:type <http://dbpedia.org/class/yago/" + category + "> . " +
        " ?person " + property + " ?" + field + " . " + 
        " } " +
        "ORDER BY ?person ";
    }

    public void save() throws Exception {
        environment.getNamespaces().addNamespace("http://dbpedia.org/ontology/", "dbpedia");
        Map<String, String> propertiesToExport = new HashMap<String, String>();
        propertiesToExport.put("birth", "dbpedia:birth");
        propertiesToExport.put("death", "dbpedia:death");
        
        vocabularyOfPeople.saveAsRDF(
                "Selection from DBPedia painters: names and birth/death dates \n" 
                + "Extracted from http://dbpedia.org/snorql/ \n"
                + "Original data is distributed under the GNU General Public License", 
                environment.getNamespaces(),
                propertiesToExport, 
                null);
    }
    
    public static void main(String[] args) throws Exception {
        FetcherOfPeopleFromDbpediaSparqlEndpoint fetcher = new FetcherOfPeopleFromDbpediaSparqlEndpoint();
        fetcher.fetch();
        fetcher.save();
    }

 }
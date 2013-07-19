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

import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.common.Language.Lang;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.vocabularies.Vocabulary;


/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 *
 */
public class SolrPlacesTagger extends SolrTagger {

    private class Coordinates {
        String latitude = null;
        String longitude = null;
        public Coordinates(String latitude, String longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }      

        boolean isAssigned() {
            return latitude != null && longitude != null;
        }
    }

    public SolrPlacesTagger(
            Vocabulary vocabulary, 
            String termFieldName, 
            String labelFieldName, 
            String broaderTermFieldName,
            String broaderLabelFieldName,            
            FieldRulePair... fieldRulePairs) {
    	super("place", termFieldName, labelFieldName, broaderTermFieldName, broaderLabelFieldName, fieldRulePairs);
    }

    Coordinates coordinates = null;

    @Override
    void beforeDocument(SolrInputDocument document) {
        coordinates = null;
    }

    @Override
    void afterTermMatched(Term term) {
        if (!StringUtils.endsWith(term.getProperty("division"), "A.PCLI")) {
            if (coordinates == null || !coordinates.isAssigned()) {
                coordinates = new Coordinates(term.getProperty("latitude"), term.getProperty("longitude"));
            }
        }
    }

    @Override
    void afterDocument(SolrInputDocument document) {
        if (coordinates != null && coordinates.isAssigned()) {
            document.addField(getPlaceTagLatitudeFieldName(), coordinates.latitude);
            document.addField(getPlaceTagLongitudeFieldName(), coordinates.longitude);
        }
    }

    String getPlaceTagLatitudeFieldName() {
        return "pl_wgs84_pos_lat";
    }

    String getPlaceTagLongitudeFieldName() {
        return "pl_wgs84_pos_long";
    }

    HashSet<Lang> languagesForAltLabels = new HashSet<Lang>();
    {
        languagesForAltLabels.add(Lang.en);
        languagesForAltLabels.add(Lang.ru);
        languagesForAltLabels.add(Lang.uk);
        languagesForAltLabels.add(Lang.de);
        languagesForAltLabels.add(Lang.fr);
        languagesForAltLabels.add(Lang.nl);
        languagesForAltLabels.add(Lang.es);
        languagesForAltLabels.add(Lang.pl);
        languagesForAltLabels.add(Lang.it);
        languagesForAltLabels.add(Lang.pt);
        languagesForAltLabels.add(Lang.el);
        languagesForAltLabels.add(Lang.bg);
        languagesForAltLabels.add(Lang.sv);
        languagesForAltLabels.add(Lang.fi);
        languagesForAltLabels.add(Lang.no);
        languagesForAltLabels.add(Lang.hu);
        languagesForAltLabels.add(Lang.da);
        languagesForAltLabels.add(Lang.sk);
        languagesForAltLabels.add(Lang.sl);
        languagesForAltLabels.add(Lang.la);
        languagesForAltLabels.add(Lang.lt);
        languagesForAltLabels.add(Lang.et);
        languagesForAltLabels.add(Lang.ro);
        languagesForAltLabels.add(Lang.cs);
        languagesForAltLabels.add(Lang.zh);
        languagesForAltLabels.add(Lang.id);
    }

    @Override
    boolean shouldInclude(Term term) {
        return term.getLang() == null || languagesForAltLabels.contains(term.getLang());
    }

}

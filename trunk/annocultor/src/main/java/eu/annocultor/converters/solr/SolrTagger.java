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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.tagger.rules.AbstractLookupRule;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.ParentTermReconstructor;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.Vocabulary;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Triple;


/**
 * Lookup (tagging) rule.
 * 
 * @author Borys Omelayenko
 *
 */
public abstract class SolrTagger 
{

    public static class FieldRulePair {
        String field;
        AbstractLookupRule rule;

        public FieldRulePair(String field, AbstractLookupRule rule) {
            this.field = field;
            this.rule = rule;
        }

        public String getField() {
            return field;
        }

        public AbstractLookupRule getRule() {
            return rule;
        }

    }

    FieldRulePair[] fieldRulePairs;

    Vocabulary vocabulary;

    String termFieldName;

    String labelFieldName;

    List<Term> broaderTerms;
    Set<String> broaderLabels;
    String broaderTermFieldName;
    String broaderLabelFieldName;

    ParentTermReconstructor parentTermReconstructor = new ParentTermReconstructor(10000);

    public SolrTagger(
            Vocabulary vocabulary, 
            String termFieldName, 
            String labelFieldName, 
            String broaderTermFieldName,
            String broaderLabelFieldName,
            FieldRulePair... fieldRulePairs) {
        this.vocabulary = vocabulary;
        this.fieldRulePairs = fieldRulePairs;
        this.termFieldName = termFieldName;
        this.labelFieldName = labelFieldName;
        this.broaderTermFieldName = broaderTermFieldName;
        this.broaderLabelFieldName = broaderLabelFieldName;
    }

    void afterTermMatched(Term term) throws Exception {

    }

    void afterDocument(SolrInputDocument document) {

    }

    void beforeDocument(SolrInputDocument document) {

    }

    void tag(SolrInputDocument document) throws Exception, ParseException {


        broaderTerms = new ArrayList<Term>();
        broaderLabels = new HashSet<String>();
        beforeDocument(document);

        Set<String> codes = new HashSet<String>();
        Set<String> labels = new HashSet<String>();

        for (FieldRulePair frp : fieldRulePairs) {
            Collection<Object> values = document.getFieldValues(frp.getField());            

            if (values != null) {
                for(Object valueObject : values) {
                    if (valueObject != null) {
                        String value = valueObject.toString();
                        if (!StringUtils.isBlank(value)) {
                            
                            Triple triple = new Triple("http://xxx", null, new LiteralValue(value), null);

                            frp.getRule().fire(triple, null);

                            TermList terms = frp.getRule().getLastMatch(); 

                            if (terms != null) {
                                for (Term term : terms) {
                                    codes.add(term.getCode());
                                    TermList altTerms = vocabulary.findByCode(new CodeURI(term.getCode()));
                                    for (Term altTerm : altTerms) {
                                        if (shouldInclude(altTerm)) {
                                            labels.add(altTerm.getLabel());
                                        }
                                    }
                                    afterTermMatched(term);
                                    broaderTerms.addAll(parentTermReconstructor.allParents(term));
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String code : codes) {
            document.addField(termFieldName, code);
        }
        for (String label : labels) {
            document.addField(labelFieldName, label);
        }

        computeBroaderLabels();
        afterDocument(document);
        addBroaderTermsAndLabels(document);
    }

    void computeBroaderLabels() throws Exception {
        for (Term broaderTerm : broaderTerms) {
            TermList altTerms = vocabulary.findByCode(new CodeURI(broaderTerm.getCode()));
            for (Term altTerm : altTerms) {
                if (altTerm != null && !StringUtils.isEmpty(altTerm.getLabel()) && shouldInclude(altTerm)) {
                    broaderLabels.add(altTerm.getLabel());
                }
            }
        }
    }

    void addBroaderTermsAndLabels(SolrInputDocument document) {
        if (!StringUtils.isEmpty(broaderTermFieldName)) {
            
            Set<String> broaderCodes = new HashSet<String>();
            for (Term term : broaderTerms) {
                broaderCodes.add(term.getCode());
            }

            for (String code : broaderCodes) {
                document.addField(broaderTermFieldName, code);
            }
        }

        if (!StringUtils.isEmpty(broaderLabelFieldName)) {
            for (String label : broaderLabels) {
                document.addField(broaderLabelFieldName, label);
            }
        }
    }

    boolean shouldInclude(Term term) {
        return true;   
    }
}

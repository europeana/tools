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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.enrichment.tagger.terms.Term;
import eu.europeana.enrichment.tagger.vocabularies.Vocabulary;


/**
 * Tagging periods from SOLR.
 * 
 * @author Borys Omelayenko
 *
 */
public class SolrPeriodsTagger extends SolrTagger {

    String enrichmentPeriodBegin;
    String enrichmentPeriodEnd;

    public SolrPeriodsTagger(
            Vocabulary vocabulary, 
            String termFieldName, 
            String labelFieldName, 
            String enrichmentPeriodBegin,
            String enrichmentPeriodEnd,
            String broaderTermFieldName,
            String broaderLabelFieldName,            
            FieldRulePair... fieldRulePairs) {
        //super(vocabulary, termFieldName, labelFieldName, broaderTermFieldName, broaderLabelFieldName, fieldRulePairs);
    	super("period", termFieldName, labelFieldName, broaderTermFieldName, broaderLabelFieldName, fieldRulePairs);
    	this.enrichmentPeriodBegin = enrichmentPeriodBegin;
        this.enrichmentPeriodEnd = enrichmentPeriodEnd;
    }

    Date beginDate = null;
    Date endDate = null;

    @Override
    void beforeDocument(SolrInputDocument document) {
        beginDate = null;
        endDate = null;        
    }


    @Override
    void afterDocument(SolrInputDocument document) {
        if (beginDate != null) {
            document.addField(enrichmentPeriodBegin, beginDate);
        }
        if (endDate != null) {
            document.addField(enrichmentPeriodEnd, endDate);
        }
    }

    @Override
    void afterTermMatched(Term term) throws Exception {
        beginDate = minDate(beginDate, parseDate(term.getProperty("begin"), "-01-01"));
        endDate = endOfDay(minDate(endDate, parseDate(term.getProperty("end"), "-12-31")));
    }

    Date minDate(Date one, Date two) {
        if (one == null) {
            return two;
        } 
        if (two == null) {
            return one;
        }
        if (one.before(two)) {
            return one;
        }
        return two;
    }

    static Date parseDate(String dateString, String affixToTryOnYearOnly) {
        try {
            if (dateString.length() == 4 && dateString.matches("\\d\\d\\d\\d")) {
                dateString += affixToTryOnYearOnly;
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (StringUtils.isEmpty(dateString)) {
                return null;
            }
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    // TODO: set to 23:59
    static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

}

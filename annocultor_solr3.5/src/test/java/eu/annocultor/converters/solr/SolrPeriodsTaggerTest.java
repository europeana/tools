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

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;


public class SolrPeriodsTaggerTest {

    @Test
    public void test2359() throws Exception {
        DateFormat df = DateFormat.getInstance();
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = df.parse("07/10/96 00:00, GMT");
        assertEquals(
                "7 Oct 1996 23:59:59 GMT",
                SolrPeriodsTagger.endOfDay(date).toGMTString());
 
        assertEquals(
                "1 Jan 2000 00:00:00 GMT",
                SolrPeriodsTagger.parseDate("2000-01-01", "").toGMTString());
        
    }

}

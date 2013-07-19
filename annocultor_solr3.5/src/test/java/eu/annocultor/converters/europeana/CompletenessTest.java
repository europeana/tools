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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;


public class CompletenessTest {

    @Test
    public void testPriority() throws Exception {
        Set<String> words = new HashSet<String>();

        // same words dont sum up
        assertEquals(
                1,
                RecordCompletenessRanking.computePoints(words, RecordCompletenessRanking.toList("Paris", "Paris"), 1));

        words.clear();
        assertEquals(
                2,
                RecordCompletenessRanking.computePoints(words, RecordCompletenessRanking.toList("Paris", "London"), 1));

        // 7 words per point
        words.clear();
        assertEquals(
                0,
                RecordCompletenessRanking.computePoints(words, RecordCompletenessRanking.toList("Paris", "London"), 7));

        words.clear();
        assertEquals(
                1,
                RecordCompletenessRanking.computePoints(words, RecordCompletenessRanking.toList("Title", "Tallinn, Amsterdam, Paris, London, Utrecht, Kiev, Nice"), 7));


    }

}

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
package eu.europeana.enrichment.data.destinations;

import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.triple.Triple;

/**
 * A named graph that is never written to.
 * 
 * @author Borys Omelayenko
 *
 */
public class IgnoredRdfGraph extends RdfGraph
{

    public IgnoredRdfGraph(String datasetId, 
            Environment environment,
            String datasetModifier, 
            String objectType, 
            String propertyType,
            String... comment) {
        super(datasetId, environment, datasetModifier, objectType, propertyType, comment);
    }

    @Override
    public void writeTriple(Triple triple) throws Exception {
        // ignorant
    }

    @Override
    public void endRdf() throws Exception {
        // ignorant
    }

    @Override
    public void startRdf() throws Exception {
        // ignorant
    }

    @Override
    public boolean writingHappened() throws Exception {
        return false;
    }


}

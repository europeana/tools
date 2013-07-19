/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.datamigration.ese2edm.enrichment;

import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.converters.solr.BuiltinSolrDocumentTagger;
/**
 * Europeana Annocultor instance
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class EuropeanaTagger extends BuiltinSolrDocumentTagger {

	@Override
	public boolean shouldReplicateThisField(String fieldName) {
		 if (fieldName.startsWith("europeana_")) {
	            return false;
	        }
	        return fieldName.contains("_") || fieldName.equalsIgnoreCase("timestamp");
	}

	@Override
	public void preProcess(SolrInputDocument document, String id) {

	}

	
}

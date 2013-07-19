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
package eu.europeana.datamigration.ese2edm;

import eu.europeana.datamigration.ese2edm.sanitizers.AgentSanitizer;
import eu.europeana.datamigration.ese2edm.sanitizers.PlaceSanitizer;
import eu.europeana.datamigration.ese2edm.sanitizers.Sanitizer;

/**
 * Util class for Entity fixing
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class EntitySanitizer {

	
	public static void main(String[] args) {
		Sanitizer agentSanitizer = new AgentSanitizer();
		agentSanitizer.sanitize();
		Sanitizer placeSanitizer = new PlaceSanitizer();
		placeSanitizer.sanitize();

	}

}

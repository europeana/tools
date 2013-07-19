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
package eu.europeana.record.management.shared.exceptions;

/**
 * Exception thrown when SOLR returns more than one records for the selected non
 * collection-based query
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class UniqueRecordException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5097315093076435472L;
	String record;

	public UniqueRecordException(){
		
	}
	
	public UniqueRecordException(String record) {
		this.record = record;
	}

	@Override
	public String getMessage() {
		return "Record " + record + " is ambiguous";
	}
}

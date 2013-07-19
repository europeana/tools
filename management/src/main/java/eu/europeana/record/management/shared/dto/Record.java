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
package eu.europeana.record.management.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The representation of a record. Since the system supports operations not only
 * on the main SOLR index field of Europeana the structure is 
 * Record->field: A EDM SOLR field 
 * Record->value: The value of the field
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class Record implements IsSerializable {

	String field;
	String value;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}

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
package eu.europeana.record.management.database.enums;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Enumeration holding the different types of log entries LOGIN: when a user is
 * logged in REMOVE: when a user removes a DBEntity EXCEPTION: when an exception
 * occured ADD: when the user adds something RETRIEVE: when the users retrieves
 * one or more DBEntities UPDATE: when the users updates a DBEntity
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public enum LogEntryType implements IsSerializable {

	LOGIN, REMOVE, EXCEPTION, ADD, RETRIEVE, UPDATE;
}

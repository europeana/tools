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
 * Enumeration holding the different roles a user can have. GOD: Superuser that
 * can remove records, manage users and systems USERADMIN: User that can remove
 * records and manage users USER: User that can only remove records
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public enum Role implements IsSerializable {
	GOD, USERADMIN, USER;
}

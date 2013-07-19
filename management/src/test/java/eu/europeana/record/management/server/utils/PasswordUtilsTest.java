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
package eu.europeana.record.management.server.utils;

import org.junit.Assert;
import org.junit.Test;

import eu.europeana.record.management.server.util.PasswordUtils;

/**
 * Unit test for PasswordUtils
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class PasswordUtilsTest {

	@Test
	public void generateMD5() {
		Assert.assertEquals("098f6bcd4621d373cade4e832627b4f6",
				PasswordUtils.generateMD5("test"));
	}
}

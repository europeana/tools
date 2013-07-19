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
package eu.europeana.record.management.server.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password utility class
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class PasswordUtils {
	/**
	 * Generate an MD5 representation of the user password
	 * 
	 * @param The
	 *            password to create the MD5 hash
	 * @return The MD5 hash of the password
	 */
	public static String generateMD5(String pass) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] md5Sequence = md.digest(pass.getBytes());
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < md5Sequence.length; i++) {
				String hex = Integer.toHexString(0xff & md5Sequence[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}

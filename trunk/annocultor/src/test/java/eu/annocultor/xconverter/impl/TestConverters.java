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
package eu.annocultor.xconverter.impl;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import junit.framework.TestCase;
import eu.annocultor.common.Utils;

public class TestConverters extends TestCase
{
	public void testProfiles() throws Exception
	{
		List<String> files = Utils.readResourceFileFromSamePackageAsList(this.getClass(), "/profiles2load.txt");
		for (String file : files) {
			File f = new File("." + file);
			System.out.println("Load & build a converter " + f.getCanonicalPath());
			System.out.flush();
			assertNotNull(
					f.getCanonicalPath(),
					Converter.compile(
					f,					
					new File("."),
					new PrintWriter(System.out),
					new StringOutputStream()));			
		}
	}


}

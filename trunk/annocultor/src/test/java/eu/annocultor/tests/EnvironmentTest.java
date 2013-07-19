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
package eu.annocultor.tests;

import junit.framework.TestCase;

import org.junit.Ignore;

@Ignore
public class EnvironmentTest extends TestCase
{
	public void testDummy() throws Exception {
		
	}

/*
 * TODO: fix
 * This test runs int he working directory and kills the top-level annocultor.properties
	public void testANNOCULTOR_HOME() throws Exception
	{
		File local = new File("annocultor.properties");
		if (local.exists())
			if (!local.delete())
				fail("Failed to delete " + local.getCanonicalPath());

		try
		{
			Environment env1 = new Environment()
			{

				@Override
				protected String getEnv(String parameter)
				{
					return ".";
				}

			};

			assertEquals(new File(".").getCanonicalPath(), env1.getAnnoCultorHome().getCanonicalPath());
			assertEquals("environment variable ANNOCULTOR_HOME", env1.getParameter(PARAMETERS.annoCultorHomeSource));

			Writer w = new FileWriter(local);
			w.append(PARAMETERS.annoCultorHome + "=.");
			w.close();

			Environment env2 = new Environment()
			{

				@Override
				protected String getEnv(String parameter)
				{
					return ".";
				}

			};

			assertEquals(new File(".").getCanonicalPath(), env2.getAnnoCultorHome().getCanonicalPath());
			assertEquals("file annocultor.properties", env2.getParameter(PARAMETERS.annoCultorHomeSource));
		}
		finally
		{
			if (!local.delete())
				throw new Exception("Failed to clean up: delete file " + local.getCanonicalPath());
			;
		}
	}
	*/
}
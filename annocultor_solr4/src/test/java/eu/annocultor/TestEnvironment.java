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
package eu.annocultor;

import java.io.File;

import org.junit.Ignore;

import eu.annocultor.context.EnvironmentImpl;

/**
 * Describes the conversion environment.
 * 
 * @author Borys Omelayenko
 * 
 */
@Ignore
public class TestEnvironment extends EnvironmentImpl
{
	@Override
	public void completeWithDefaults() throws Exception
	{
		super.completeWithDefaults();
		// trying first system properties set up by Maven, then environment properties set up by Eclipse run
		String basedir = System.getProperty("basedir");
		if (basedir == null)
			basedir = System.getenv("basedir");

		if (basedir == null)
			basedir = System.getProperty("user.dir");

		if (basedir == null)
			throw new NullPointerException("Parameter basedir is not set. If you are running Maven then it is not set in the pom.xml, "
					+ " and if you are running eclipse then it is not set in the launch file");

		File workDir =
			new File(new File(basedir), System.getProperty("annocultor.core.test.working.dir", "test-dir"));
		if (!workDir.exists())
		{
			workDir.mkdir();
		}

		File tmpDir =
			new File(workDir, "tmp");
		if (!tmpDir.exists())
		{
			tmpDir.mkdir();
		}

		File docDir = new File(workDir, "doc");
		if (!docDir.exists())
		{
			docDir.mkdir();
		}
		
		File rdfDir = new File(workDir, "rdf");
		if (!rdfDir.exists())
		{
			rdfDir.mkdir();
		}
		
		setParameter(PARAMETERS.ANNOCULTOR_HOME, basedir);
		setParameter(PARAMETERS.ANNOCULTOR_TMP_DIR, tmpDir.getCanonicalPath());
		setParameter(PARAMETERS.ANNOCULTOR_DOC_DIR, docDir.getCanonicalPath());
		setParameter(PARAMETERS.ANNOCULTOR_OUTPUT_DIR, rdfDir.getCanonicalPath());

		setParameter(PARAMETERS.ANNOCULTOR_VOCABULARY_DIR, basedir);
	}

}
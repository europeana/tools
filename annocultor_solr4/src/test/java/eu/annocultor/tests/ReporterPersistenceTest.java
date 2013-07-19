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

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import eu.annocultor.api.Reporter;
import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.reports.AbstractReporter;
import eu.annocultor.reports.ReportPresenter;
import eu.annocultor.reports.ReporterImpl;
import eu.annocultor.reports.parts.ReportCounter;
import eu.annocultor.reports.parts.ReportCounter.ObjectCountPair;

public class ReporterPersistenceTest extends TestRulesSetup
{

	private static final String TEST = "test";

	@Test
	@SuppressWarnings("unchecked")
	public void testRW() throws Exception
	{
		final String[] lines = {
				"line 1",
				"line 13",
				"l34ine 144",
				"4line ,d 1",
				"line 1'",
				"'line 1",
				""				
		};
		
		File dir = new File(task.getEnvironment().getTmpDir(), "reportPersistence");
		dir.mkdir();

		// init & persist
		{
			ReportCounter<String> c = new ReportCounter<String>(dir, TEST + "." + AbstractReporter.FILE_LOOKUPS, 1000);
			int i = 0;
			for (String line : lines) {
				c.inc(line, i);
				i ++;
			}
			c.flush();
		}
		// load from file
		{
			ReportCounter<String> c = new ReportCounter<String>(dir, TEST + "." + AbstractReporter.FILE_LOOKUPS, 1000);
			c.load();
			int i = 0;
			for (ObjectCountPair<String> entry : c.asList()) {
// difficult to test				Assert.assertEquals("On line " + i, lines[i], entry.getObject());
				i ++;
			}

			Assert.assertEquals(lines.length, i);
		}
		// generate html report
		Reporter reporter = new ReporterImpl(TEST, task.getEnvironment());
		for (String line : lines) {
			reporter.log(line);
		}
		reporter.report();
		
		ReportPresenter presenter = new ReportPresenter(task.getEnvironment().getDocDir(), TEST);
		presenter.makeHtmlReport();
		FileInputStream is = new FileInputStream(new File(task.getEnvironment().getDocDir(),TEST + "/ReportData.js"));
		List<String> js = IOUtils.readLines(is);
		String joined = StringUtils.join(js, ",");
		{
			for (String line : lines) {
				Assert.assertTrue("On line: " + line, joined.contains(line));
			}
		}
		is.close();

		
	}

}
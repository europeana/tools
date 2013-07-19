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
import eu.annocultor.context.Namespaces;
import eu.annocultor.path.Path;

public class ReporterPathTest extends TestCase
{
	public void testPathFormatter() throws Exception
	{
		Namespaces namespaces = new Namespaces();
		namespaces.addNamespace("http://ns#", "ns");
		namespaces.addNamespace("http://ns/", "ns2");

		String pre = "<abbr class='namespaceAbbr' title=\"";
		String mid = "\">";
		String pos = ":</abbr> ";

		assertEquals(pre + "http://ns/" + mid + "ns2" + pos + "Tag", 
				Path.formatPath(new Path("ns2:Tag", namespaces), namespaces));

		assertEquals(pre + "http://ns#" + mid + "ns" + pos + "Tag", 
				Path.formatPath(new Path("ns:Tag", namespaces), namespaces));

		assertEquals(pre
			+ "http://ns#"
			+ mid
			+ "ns"
			+ pos
			+ "Tag/"
			+ pre
			+ Namespaces.DC
			+ mid
			+ "dc"
			+ pos
			+ "Tag", Path.formatPath(new Path("ns:Tag/dc:Tag", namespaces), namespaces));

		assertEquals(pre
			+ Namespaces.DC
			+ mid
			+ "dc"
			+ pos
			+ "Tag/"
			+ pre
			+ Namespaces.DC
			+ mid
			+ "dc"
			+ pos
			+ "Tag2", Path.formatPath(new Path("dc:Tag/dc:Tag2", namespaces),namespaces));

		namespaces.addNamespace("http://ns", "test");

		assertEquals(pre + "http://ns" + mid + "test" + pos + "Tag", 
				Path.formatPath(new Path("test:Tag", namespaces), namespaces));

		assertEquals(pre + "http://ns" + mid + "test" + pos + "Tag[@aa='x' and @att='TT']", 
				Path.formatPath(new Path("test:Tag[@att='TT' and @aa='x']", namespaces), namespaces));

		// no-ns path
		assertEquals("tag", Path.formatPath(new Path("tag"), namespaces));

	}
}
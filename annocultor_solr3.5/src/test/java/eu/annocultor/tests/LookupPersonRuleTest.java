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

import org.junit.Assert;

import eu.annocultor.objects.TestRulesSetup;
import eu.annocultor.tagger.rules.PersonDetails;

public class LookupPersonRuleTest extends TestRulesSetup
{
	public void testSwapLastFirstNames() throws Exception
	{
		Assert.assertEquals("Tilman RIEMENSCHNEIDER", new PersonDetails("RIEMENSCHNEIDER, Tilman").getFullName());
		Assert.assertEquals("Tilman Pal RIEMENSCHNEIDER", new PersonDetails("Tilman Pal RIEMENSCHNEIDER").getFullName());
		Assert.assertEquals("Peter Paul RUBENS", new PersonDetails("RUBENS, Peter Paul").getFullName());
		Assert.assertEquals("RUBENS- Peter Paul", new PersonDetails("RUBENS- Peter Paul").getFullName());
		Assert.assertEquals("RUBENS-, Peter Paul,", new PersonDetails("RUBENS-, Peter Paul,").getFullName());

	}

}

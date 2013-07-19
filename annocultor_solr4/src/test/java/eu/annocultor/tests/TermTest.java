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
import eu.annocultor.common.Language.Lang;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;

public class TermTest extends TestCase
{

	String label = "label";
	String codeOk = "http://code";
	String codeFail = "zzzhttp://code";
	String voc = "vocab";

	public void testTransparency() throws Exception
	{
		Term t = new Term(label, Lang.en, new CodeURI(codeOk), voc);
		assertEquals(label, t.getLabel());
		assertEquals(codeOk, t.getCode());
		assertEquals(voc, t.getVocabularyName());
		assertEquals(Lang.en, t.getLang());
	}
}

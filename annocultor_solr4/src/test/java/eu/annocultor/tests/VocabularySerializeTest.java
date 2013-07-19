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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.TestCase;
import eu.annocultor.common.Language.Lang;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.DisambiguationContext;
import eu.annocultor.tagger.vocabularies.Vocabulary;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.tagger.vocabularies.VocabularySerializer;
import eu.annocultor.tagger.vocabularies.VocabularySerializer.SerializedProperties;

public class VocabularySerializeTest extends TestCase
{

	private static final String PARIS_LABEL = "Paris";

	private static final String NS = "http://";

	private final CodeURI PARIS_CODE = new CodeURI(NS + "Paris");

	public VocabularySerializeTest() throws Exception
	{
		super();
	}

	CodeURI[] parisOkCodes =
			{
					new CodeURI(NS + ".3"),
					new CodeURI(NS + "."),
					new CodeURI(NS + "30000"),
					new CodeURI(NS + "30000"),
					new CodeURI(NS + "30000;"),
					new CodeURI(NS + "30000"),
					new CodeURI(NS + ";"),
					new CodeURI(NS + "300en"),
					new CodeURI(NS + "3000@en"),
					new CodeURI(NS + "3000@nl"),
					new CodeURI(NS + "4000"),
			//new CodeURI(NS + "\"4000\"@nl"),
			//new CodeURI(NS + "\";\"@nl"),
			//new CodeURI(NS + "\"5000\"")
			};
	//	String[] parisFailCodes = {
	//	"\"4000\"@nld",
	//	"\"4000\"@"
	//	};

	Set<CodeURI> parisOkCodesSet = new HashSet<CodeURI>();

	@Override
	protected void setUp() throws Exception
	{
		for (int i = 0; i < parisOkCodes.length; i++)
		{
			CodeURI code = parisOkCodes[i];
			//			if (code.endsWith("@nl"))
			//			code = code.substring(0, code.lastIndexOf("@"));
			//TODO: separate parse with lang code
			parisOkCodesSet.add(code);
		}

		super.setUp();
	}

	private class V implements Vocabulary
	{
		Collection<Term> terms = new LinkedList<Term>();

		public V()
		{
			super();
		}

		@Override
		public void init() throws Exception
		{
		// ok	
		}

		public void putTerm(Term term) throws Exception
		{
			terms.add(term);
		}

		public String onNormaliseLabel(String value, NormaliseCaller caller) throws Exception
		{
			return value;
		}

		public Iterable<TermList> listAllByCode()
		{
			Collection<TermList> result = new LinkedList<TermList>();
			TermList tl = new TermList();
			for (Term term : terms)
			{
				tl.add(term);
			}
			return result;
		}

		/**
		 * Called when the vocabulary term is loaded from the RDF file into memory.
		 * 
		 * @param label
		 * @return
		 */
		public Collection<Term> expandVocabularyTermOnLoad(Term term)
		{
			return new LinkedList<Term>();
		}

		/*
		 * We dont really care about the rest
		 */

		public Set<CodeURI> codeSet()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public TermList findByCode(CodeURI code) throws Exception
		{
			// TODO Auto-generated method stub
			return null;
		}

		public TermList findByLabel(String label, DisambiguationContext disambiguationContext) throws Exception
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String findLabel(CodeURI code) throws Exception
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getVocabularyName()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Set<String> labelSet()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public void loadTermPropertiesSPARQL(String attributeName, String query, File cacheDir, File dir, String... files)
				throws Exception
		{
			// TODO Auto-generated method stub

		}

		public void loadTermsSeRQL(String query, File cacheDir, File dir, String... files) throws Exception
		{
			// TODO Auto-generated method stub

		}

		public void loadTermsSPARQL(String query, File cacheDir, File dir, String... files) throws Exception
		{
			// TODO Auto-generated method stub

		}

		public void loadVocabulary(File tmpDir, FileSign sign, String... file) throws Exception
		{
			// TODO Auto-generated method stub

		}

		public String onLoadTermCode(String code)
		{
			// TODO Auto-generated method stub
			return null;
		}

	}

	private VocabularyOfTerms v;

	private VocabularySerializer makeSerializableVocabulary()
	{
		v = new VocabularyOfTerms("tt", null);
		return new VocabularySerializer(v, "test");
	}

	public void testParse() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		for (int i = 0; i < parisOkCodes.length; i++)
		{
			CodeURI code = parisOkCodes[i];
			// no exception should happen
			h.parseValue(code, null, PARIS_LABEL);
		}
		assertEquals(parisOkCodesSet.size(), v.codeSet().size());

		//		for (int i = 0; i < parisFailCodes.length; i++) 
		//		{
		//		String code = parisFailCodes[i];
		//		try
		//		{
		//		h.parseValue(PARIS_LABEL, code);
		//		}
		//		catch (Exception e) 
		//		{
		//		continue;
		//		}
		//		fail("Exception expected at code " + code);
		//		}
	}

	public void testEmptylabel() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		try
		{
			h.parseValue(PARIS_CODE, null, "");
		}
		catch (Exception e)
		{
			return;
		}
		fail("Allowed empty label to sneak in");
	}

	public void testNullCode() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		try
		{
			h.parseValue(null, null, "ssss");
		}
		catch (Exception e)
		{
			return;
		}
		fail("Allowed null code to sneak in");
	}

	public void testEmptyCode() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		try
		{
			h.parseValue(new CodeURI(""), null, "ddd");
		}
		catch (Exception e)
		{
			return;
		}
		fail("Allowed empty code to sneak in");
	}

	public void testSerializeVerbatim() throws Exception
	{
		SerializedProperties p1;
		VocabularySerializer h = makeSerializableVocabulary();
		h.parseValue(PARIS_CODE, null, PARIS_LABEL);
		p1 = h.serializeToProperties();
		assertEquals(PARIS_LABEL.length() + "." + PARIS_LABEL, p1
				.getTerms()
				.get(PARIS_CODE.toString())
				.toString());
		h.parseValue(PARIS_CODE, null, "2");
		p1 = h.serializeToProperties();
		assertEquals("2".length() + "," + PARIS_LABEL.length() + "." + "2" + ";" + PARIS_LABEL, p1
				.getTerms()
				.get(PARIS_CODE.toString())
				.toString());
	}

	public void testSerializeVerbatimLang() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		h.parseValue(PARIS_CODE, null, "1", Lang.en);
		SerializedProperties p1 = h.serializeToProperties();
		assertEquals("1+2.1@en", p1.getTerms().get(PARIS_CODE.toString()).toString());
		h.parseValue(PARIS_CODE, null, "@en");
		p1 = h.serializeToProperties();
		assertEquals("1+2,3.1@en;@en", p1.getTerms().get(PARIS_CODE.toString()).toString());
		h.parseValue(PARIS_CODE, null, "2", Lang.nl);
		p1 = h.serializeToProperties();
		assertEquals("1+2,1+2,3.1@en;2@nl;@en", p1.getTerms().get(PARIS_CODE.toString()).toString());
	}

	public void testSerializeVerbatimLangSame() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		h.parseValue(PARIS_CODE, null, "1", Lang.en);
		SerializedProperties p1 = h.serializeToProperties();
		assertEquals("1+2.1@en", p1.getTerms().get(PARIS_CODE.toString()).toString());
		h.parseValue(PARIS_CODE, null, "1@en");
		p1 = h.serializeToProperties();
		assertEquals("1+2,4.1@en;1@en", p1.getTerms().get(PARIS_CODE.toString()).toString());
	}

	public void testSerializeDecerialize() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		for (int i = 0; i < parisOkCodes.length; i++)
		{
			CodeURI code = parisOkCodes[i];
			h.parseValue(code, null, PARIS_LABEL);
		}
		h.parseValue(PARIS_CODE, null, "X", Lang.en);
		h.parseValue(PARIS_CODE, null, "X", Lang.nl);

		// reproducable serialize - decerialize
		SerializedProperties p1 = h.serializeToProperties();
		assertEquals("Serialized size", parisOkCodesSet.size() + 1, p1.getTerms().size());
		h = makeSerializableVocabulary();
		h.deserializeFromProperties(p1);
		SerializedProperties p2 = h.serializeToProperties();

		assertEquals("Serialize-decerialize failed", p1.getTerms(), p2.getTerms());
	}

	public void testContent() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		for (int i = 0; i < parisOkCodes.length; i++)
		{
			CodeURI code = parisOkCodes[i];
			h.parseValue(code, null, PARIS_LABEL);
		}

		Set<String> codes = new HashSet<String>();
		int count = 0;
		for (TermList tl : v.listAllByCode())
		{
			for (Term paris : tl)
			{
				assertTrue("Unexpected code " + paris, parisOkCodesSet.contains(new CodeURI(paris.getCode())));
				assertFalse("Duplicating code " + paris, codes.contains(paris.getLabel()));
				codes.add(paris.getCode());
				count++;
			}
		}
		assertEquals(parisOkCodesSet.size(), count);
	}

	public void testInternalRepresentation() throws Exception
	{
		VocabularySerializer h = makeSerializableVocabulary();
		for (int i = 0; i < parisOkCodes.length; i++)
		{
			CodeURI code = parisOkCodes[i];
			h.parseValue(code, null, PARIS_LABEL);
		}
		SerializedProperties p1 = h.serializeToProperties();

		assertEquals(parisOkCodesSet.size(), p1.getTerms().size());
		String intValue = p1.getTerms().getProperty(PARIS_CODE.toString());
		assertNull(intValue);
		intValue = p1.getTerms().getProperty(NS + "30000");
		assertTrue(intValue.indexOf(".") > 0);
	}

	/* public void testLoadCacheStackOverflow()
	 throws Exception
	 {
	   SerializedProperties props = new SerializedProperties();
	   File prop = new File(this.getClass().getResource("/cache/cache1.txt").getFile());  
	   props.getTerms().load(new BufferedInputStream(new FileInputStream(prop), 1024 * 10));          
	   VocabularySerializer h = makeSerializableVocabulary();
	   h.deserializeFromProperties(props);
	   assertTrue(h.serializeToProperties().getTerms().size() > 300000);
	 }*/
}

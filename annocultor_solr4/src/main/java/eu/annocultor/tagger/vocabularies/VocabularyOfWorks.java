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
package eu.annocultor.tagger.vocabularies;

import java.util.List;

import eu.annocultor.common.Language.Lang;
import eu.annocultor.tagger.terms.TermList;


/**
 * Is not a real vocabulary but a collection of works that are looked up to find
 * sameAs, with its specific disambiguation context.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface VocabularyOfWorks extends Vocabulary
{

	public TermList lookupWork(
			String title,
			Lang lang,
			String creationDate,
			List<String> creatorsNames,
			String description) throws Exception;

}

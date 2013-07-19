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
package eu.annocultor.tagger.terms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.collections.map.LRUMap;


/**
 * Reconstruct parent terms.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ParentTermReconstructor 
{
    LRUMap cache;
    
   	public ParentTermReconstructor(int maxCacheSize) {
   	    cache = new LRUMap(maxCacheSize);
    }

    @SuppressWarnings("unchecked")
    List<Term> empty = UnmodifiableList.decorate(new ArrayList<Term>());

   	
    public List<Term> allParents(Term term) {
        Term immediateParent = term.getParent();
        if (immediateParent == null) {
            return empty;
        }
        String code = immediateParent.getCode();
        if (cache.containsKey(code)) {
            return (List<Term>)cache.get(code);
        }
        List<Term> parents = thisAndAllParents(immediateParent);
        cache.put(code, parents);
        return parents;
    }
    
    private List<Term> thisAndAllParents(Term parent) {
	    List<Term> parentsToReturn = new ArrayList<Term>();
	    Set<Term> parentsToCheckForCycles = new HashSet<Term>();
	    
	    Term chainParent = parent;
	    while (chainParent != null && !parentsToCheckForCycles.contains(chainParent)) {
	        parentsToCheckForCycles.add(chainParent);
	        parentsToReturn.add(chainParent);
	        chainParent = chainParent.getParent();
	    }
	    return parentsToReturn;
	}
}

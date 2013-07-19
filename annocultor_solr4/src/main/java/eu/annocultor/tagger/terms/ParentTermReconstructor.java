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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.collections.map.LRUMap;

import eu.annocultor.utils.MongoDatabaseUtils;


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

   	
//    public List<Term> allParents(TermList terms){
//    	Iterator<Term> termIter = terms.iterator();
//    	List<Term> parents = new ArrayList<Term>();
//    	while(termIter.hasNext()){
//    		Term term = termIter.next();
//    		parents.addAll(thisAndAllParents(term.getParent()));
//    	}
//    	return parents;
//    }
    public List<Term> allParents(TermList term, String vocabulary) {
        if(term.getFirst()!=null){
    	Term immediateParent = term.getFirst().getParent();
        if (immediateParent == null) {
            return empty;
        }
    	
        String code = immediateParent.getCode();
//        if (cache.containsKey(code)) {
//            return (List<Term>)cache.get(code);
//        }
        try {
        	List<Term> terms = new ArrayList<Term>();
        	TermList tList = MongoDatabaseUtils.findByCode(new CodeURI(code), vocabulary);
			Iterator<Term> tIterator = tList.iterator();
        	while( tIterator.hasNext()){
        		Term trm = tIterator.next();
        		if(!terms.contains(trm)){
				terms.add(trm);
        		}
			}
        	return terms;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
        List<Term> parents = thisAndAllParents(immediateParent);
        //cache.put(code, parents);
        return parents;
        }
        return null;
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

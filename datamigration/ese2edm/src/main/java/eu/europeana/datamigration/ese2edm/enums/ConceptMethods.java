/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.datamigration.ese2edm.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.definitions.solr.entity.Concept;
import eu.europeana.corelib.utils.StringArrayUtils;

/**
 * Enumeration setting values for the Concept Fields
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public enum ConceptMethods {

	PREF_LABEL(EdmLabel.CC_SKOS_PREF_LABEL.toString(),true) {
		@Override
		public void appendValue(Concept concept, String language, String value) {
			Map<String, List<String>> prefLabels = concept.getPrefLabel();
			if(prefLabels==null){
				prefLabels = new HashMap<String, List<String>>();
			}
			List<String> prefLabel = prefLabels.get(language);
			if (prefLabel == null) {
				prefLabel = new ArrayList<String>();
			}
			if(!contains(prefLabel,value)){
				prefLabel.add(value);
			}
			
			prefLabels.put(language, prefLabel);
			concept.setPrefLabel(prefLabels);
		}
	},
	ALT_LABEL(EdmLabel.CC_SKOS_ALT_LABEL.toString(),true) {
		@Override
		public void appendValue(Concept concept, String language, String value) {
			Map<String, List<String>> altLabels = concept.getAltLabel();
			if(altLabels==null){
				altLabels = new HashMap<String, List<String>>();
			}
			List<String> altLabel = altLabels.get(language);
			if (altLabel == null) {
				altLabel = new ArrayList<String>();
			}
			if(!contains(altLabel,value)){
			altLabel.add(value);
			}
			altLabels.put(language, altLabel);
			concept.setAltLabel(altLabels);
		}
	},
	NOTE(EdmLabel.CC_SKOS_NOTE.toString(),true) {
		@Override
		public void appendValue(Concept concept, String language, String value) {
			Map<String, List<String>> notes = concept.getNote();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note,value)){
				note.add(value);
			}
			notes.put(language, note);
			
			concept.setNote(notes);
		}
	}, HIDDEN_LABEL(EdmLabel.CC_SKOS_HIDDEN_LABEL.toString(),true) {
		@Override
		public void appendValue(Concept concept, String language, String value) {
			Map<String, List<String>> hiddenLabels = concept.getHiddenLabel();
			if(hiddenLabels==null){
				hiddenLabels = new HashMap<String, List<String>>();
			}
			List<String> hiddenLabel = hiddenLabels.get(language);
			if (hiddenLabel == null) {
				hiddenLabel = new ArrayList<String>();
			}
			if(!contains(hiddenLabel,value)){
			hiddenLabel.add(value);
			}
			hiddenLabels.put(language, hiddenLabel);
			concept.setHiddenLabel(hiddenLabels);
			
		}
	}, BROADER(EdmLabel.CC_SKOS_BROADER.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getBroader();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
					strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setBroader(strArray);
		}

	
	}, BROADMATCH(EdmLabel.CC_SKOS_BROADMATCH.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getBroadMatch();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setBroadMatch(strArray);
		}
	}, CLOSEMATCH(EdmLabel.CC_SKOS_CLOSEMATCH.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getCloseMatch();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setCloseMatch(strArray);
		}
	}, EXACTMATCH(EdmLabel.CC_SKOS_EXACTMATCH.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getExactMatch();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setExactMatch(strArray);
		}
	}, INSCHEME(EdmLabel.CC_SKOS_INSCHEME.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getInScheme();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setInScheme(strArray);
		}
	}, NARROWER(EdmLabel.CC_SKOS_NARROWER.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getNarrower();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setNarrower(strArray);
		}
	}, NARROWMATCH(EdmLabel.CC_SKOS_NARROWMATCH.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getNarrowMatch();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setNarrowMatch(strArray);
		}
	}, NOTATION(EdmLabel.CC_SKOS_NOTATIONS.toString(),true){
		public void appendValue(Concept concept, String language, String value) {
			Map<String, List<String>> notes = concept.getNotation();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if(!contains(note,value)){
			note.add(value);
			}
			notes.put(language, note);
			concept.setNotation(notes);
		}
	}, RELATED(EdmLabel.CC_SKOS_RELATED.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getRelated();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setRelated(strArray);
		}
	}, RELATED_MATCH(EdmLabel.CC_SKOS_RELATEDMATCH.toString(),false){
		public void appendValue(Concept concept, String language, String value) {
			String[] strArray = concept.getRelatedMatch();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			concept.setRelatedMatch(strArray);
		}
	}
	;

	String field;
	Boolean isMulti;
	private ConceptMethods(String field, Boolean isMulti) {
		this.field = field;
		this.isMulti=isMulti;
	}

	/**
	 * Method that returns the appropriate Concept methods according to the input
	 * @param val The value to check
	 * @return The appropriate ConceptMethod
	 */
	public static ConceptMethods getMethod(String val){
		for(ConceptMethods cm: ConceptMethods.values()){
			if(StringUtils.equals(val, cm.field)){
				return cm;
			}
		}
		return null;
	}

	/**
	 * Check if the field is multilingual
	 * @return
	 */
	public  Boolean isMultiLingual(){
		return isMulti;
	}
	
	private static boolean contains(String[] strArray, String value) {
		for(String str:strArray){
			if(StringUtils.equals(str,value)){
				return true;
			}
		}
		return false;
	}
	
	private static boolean contains(List<String> strList, String value){
		if(strList.contains(value)){
			return true;
		}
		return false;
	}
	/**
	 * Abstract method that appends a language-value pair to a Concept
	 * @param concept The concept to append the values to
	 * @param language The language to append
	 * @param value The value for the given language
	 */
	public abstract void appendValue(Concept concept, String language,
			String value);
}

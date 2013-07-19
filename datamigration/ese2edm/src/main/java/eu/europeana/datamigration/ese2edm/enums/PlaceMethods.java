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
import eu.europeana.corelib.definitions.solr.entity.Place;
import eu.europeana.corelib.utils.StringArrayUtils;


/**
 * Enumeration holding the available edm:Place methods
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public enum PlaceMethods {

	PREFLABEL(EdmLabel.PL_SKOS_PREF_LABEL.toString(),true){
		public void appendValue(Place place, String language, String value) {
			Map<String, List<String>> notes = place.getPrefLabel();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if(!contains(note, value)){
				note.add(value);
				}
			notes.put(language, note);
			place.setPrefLabel(notes);
		}
	}, ALTLABEL(EdmLabel.PL_SKOS_ALT_LABEL.toString(),true){
		@Override
		public void appendValue(Place place, String language, String value) {
			Map<String, List<String>> notes = place.getAltLabel();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if(!contains(note, value)){
				note.add(value);
				}
			notes.put(language, note);
			place.setAltLabel(notes);			
		}
	}, HIDDENLABEL(EdmLabel.PL_SKOS_HIDDENLABEL.toString(),true){
		@Override
		public void appendValue(Place place, String language, String value) {
			Map<String, List<String>> notes = place.getHiddenLabel();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if(!contains(note, value)){
				note.add(value);
				}
			notes.put(language, note);
			place.setHiddenLabel(notes);					
		}
	}, NOTE(EdmLabel.PL_SKOS_NOTE.toString(),true){
		@Override
		public void appendValue(Place place, String language, String value) {
			Map<String, List<String>> notes = place.getNote();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if(!contains(note, value)){
				note.add(value);
				}
			notes.put(language, note);
			place.setNote(notes);					
		}
	}, HASPART (EdmLabel.PL_DCTERMS_HASPART.toString(),true){
		@Override
		public void appendValue(Place place, String language, String value) {
			Map<String, List<String>> notes = place.getDcTermsHasPart();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if(!contains(note, value)){
				note.add(value);
				}
			notes.put(language, note);
			place.setDcTermsHasPart(notes);					
		}
	}, ISPARTOF(EdmLabel.PL_DCTERMS_ISPART_OF.toString(),true){
		@Override
		public void appendValue(Place place, String language, String value) {
			Map<String, List<String>> notes = place.getIsPartOf();
			if(notes==null){
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if(!contains(note, value)){
				note.add(value);
				}
			notes.put(language, note);
			place.setIsPartOf(notes);					
		}
	}, SAMEAS(EdmLabel.PL_OWL_SAMEAS.toString(),false){
		@Override
		public void appendValue(Place place, String language, String value) {
			String[] strArray = place.getOwlSameAs();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			place.setOwlSameAs(strArray);
		}
	}, LAT(EdmLabel.PL_WGS84_POS_LAT.toString(),false){
		@Override
		public void appendValue(Place place, String language, String value) {
			place.setLatitude(Float.parseFloat(value));
		}
	}, LONG(EdmLabel.PL_WGS84_POS_LONG.toString(),false){
		@Override
		public void appendValue(Place place, String language, String value) {
			place.setLongitude(Float.parseFloat(value));
		}
	}, ALT(EdmLabel.PL_WGS84_POS_ALT.toString(),false){
		@Override
		public void appendValue(Place place, String language, String value) {
			place.setAltitude(Float.parseFloat(value));
		}
	}
	;
	
	
	String field;
	Boolean isMulti;
	
	public static PlaceMethods getMethod(String val){
		for(PlaceMethods pl: PlaceMethods.values()){
			if(StringUtils.equals(val, pl.field)){
				return pl;
			}
		}
		return null;
	}
	private PlaceMethods(String field,Boolean isMulti) {
		this.field = field;
		this.isMulti = isMulti;
	}

	/**
	 * Append a language-value pair to an edm:Place
	 * @param place The Place to append the value to
	 * @param language The language to use
	 * @param value The value to use
	 */
	public abstract void appendValue(Place place, String language,
			String value);
	
	/**
	 * Check if the field is Multilingual (dynamic)
	 * @return
	 */
	public Boolean isMultilingual(){
		return this.isMulti;
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
	
}

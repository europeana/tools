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
import eu.europeana.corelib.definitions.solr.entity.Agent;
import eu.europeana.corelib.utils.StringArrayUtils;

/**
 * Enumeration holding the available edm:Agent methods
 * @author gmamakis
 *
 */
public enum AgentMethods {

	PREFLABEL(EdmLabel.AG_SKOS_PREF_LABEL.toString(),true){

		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getPrefLabel();
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
			agent.setPrefLabel(notes);
		}
		
	}, ALTLABEL(EdmLabel.AG_SKOS_ALT_LABEL.toString(),true){

		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getAltLabel();
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
			agent.setAltLabel(notes);
		}
	}, NOTE(EdmLabel.AG_SKOS_NOTE.toString(),true){
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getNote();
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
			agent.setNote(notes);
			
		}
	}, DATE(EdmLabel.AG_DC_DATE.toString(),true){
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getDcDate();
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
			agent.setDcDate(notes);
		}
	}, IDENTIFIER(EdmLabel.AG_DC_IDENTIFIER.toString(),true){
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getDcIdentifier();
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
			agent.setDcIdentifier(notes);
		}
	}, BEGIN (EdmLabel.AG_EDM_BEGIN.toString(),true){
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getBegin();
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
			agent.setBegin(notes);
		}
	}, END(EdmLabel.AG_EDM_END.toString(),true){
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getEnd();
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
			agent.setEnd(notes);
		}
	}, HASMET(EdmLabel.AG_EDM_HASMET.toString(),true){
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getEdmHasMet();
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
			agent.setEdmHasMet(notes);
		}
	}, ISRELATEDTO(EdmLabel.AG_EDM_ISRELATEDTO.toString(),true){
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getEdmIsRelatedTo();
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
			agent.setEdmIsRelatedTo(notes);
		}
	}, WASPRESENTAT(EdmLabel.AG_EDM_WASPRESENTAT.toString(),false){
		public void appendValue(Agent agent, String language, String value) {
			String[] strArray = agent.getEdmWasPresentAt();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			agent.setEdmWasPresentAt(strArray);
		}
	}, NAME(EdmLabel.AG_FOAF_NAME.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getFoafName();
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
			agent.setFoafName(notes);
		}
	}, SAMEAS(EdmLabel.AG_OWL_SAMEAS.toString(),false){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			String[] strArray = agent.getOwlSameAs();
			if (strArray == null){
				strArray = new String[]{value};
			}
			else {
				if(!contains(strArray,value)){
				strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			agent.setOwlSameAs(strArray);
		}
		
	}, BIOGRAPHICAL_INFORMATION(EdmLabel.AG_RDAGR2_BIOGRAPHICALINFORMATION.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getRdaGr2BiographicalInformation();
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
			agent.setRdaGr2BiographicalInformation(notes);
		}
	}, DATEOFBIRTH(EdmLabel.AG_RDAGR2_DATEOFBIRTH.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getRdaGr2DateOfBirth();
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
			agent.setRdaGr2DateOfBirth(notes);
		}
	}, DATEOFDEATH(EdmLabel.AG_RDAGR2_DATEOFDEATH.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getRdaGr2DateOfDeath();
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
			agent.setRdaGr2DateOfDeath(notes);
		}
	}, DATEOFESTABLISHMENT(EdmLabel.AG_RDAGR2_DATEOFESTABLISHMENT.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getRdaGr2DateOfEstablishment();
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
			agent.setRdaGr2DateOfEstablishment(notes);
		}
	}, DATEOFTERMINATION(EdmLabel.AG_RDAGR2_DATEOFTERMINATION.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getRdaGr2DateOfTermination();
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
			agent.setRdaGr2DateOfTermination(notes);
		}
	}, GENDER(EdmLabel.AG_RDAGR2_GENDER.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getRdaGr2Gender();
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
			agent.setRdaGr2Gender(notes);			
		}
	}, PROFESSIONOOROCCUPATION(EdmLabel.AG_RDAGR2_PROFESSIONOROCCUPATION.toString(),true){
		@Override
		public void appendValue(Agent agent, String language, String value) {
			Map<String, List<String>> notes = agent.getRdaGr2ProfessionOrOccupation();
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
			agent.setRdaGr2ProfessionOrOccupation(notes);	
		}
	}
	;
	
	String field;
	Boolean isMulti;

	private AgentMethods(String field,Boolean isMulti) {
		this.field = field;
		this.isMulti = isMulti;
	}
	
	/**
	 * Return the Agent method for the supplied value
	 * @param val The value to check
	 * @return The corresponding Agent Method
	 */
	public static AgentMethods getMethod(String val){
		for(AgentMethods ag: AgentMethods.values()){
			if(StringUtils.equals(val, ag.field)){
				return ag;
			}
		}
		return null;
	}
	
	/**
	 * Abstact method that appends the appropriate language and value to an Agent
	 * @param agent The agent to use
	 * @param language The language to append
	 * @param value The value to append
	 */
	public abstract void appendValue(Agent agent, String language,
			String value);
	
	/**
	 * Check if the field is multilingual (dynamic)
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

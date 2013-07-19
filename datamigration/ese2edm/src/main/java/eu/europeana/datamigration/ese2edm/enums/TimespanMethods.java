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
import eu.europeana.corelib.definitions.solr.entity.Timespan;
import eu.europeana.corelib.utils.StringArrayUtils;

/**
 * The available Timespan methods
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public enum TimespanMethods {

	BEGIN(EdmLabel.TS_EDM_BEGIN.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getBegin();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setBegin(notes);
		}
	},
	END(EdmLabel.TS_EDM_END.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getEnd();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setEnd(notes);
		}
	},
	ISPARTOF(EdmLabel.TS_DCTERMS_ISPART_OF.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getIsPartOf();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setIsPartOf(notes);
		}
	},
	HASPART(EdmLabel.TS_DCTERMS_HASPART.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getDctermsHasPart();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setDctermsHasPart(notes);
		}
	},
	ALTLABEL(EdmLabel.TS_SKOS_ALT_LABEL.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getAltLabel();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setAltLabel(notes);
		}
	},
	PREFLABEL(EdmLabel.TS_SKOS_PREF_LABEL.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getPrefLabel();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setPrefLabel(notes);
		}
	},
	HIDDENLABEL(EdmLabel.TS_SKOS_HIDDENLABEL.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getHiddenLabel();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setHiddenLabel(notes);
		}
	},
	NOTE(EdmLabel.TS_SKOS_NOTE.toString(), true) {
		public void appendValue(Timespan ts, String language, String value) {
			Map<String, List<String>> notes = ts.getNote();
			if (notes == null) {
				notes = new HashMap<String, List<String>>();
			}
			List<String> note = notes.get(language);
			if (note == null) {
				note = new ArrayList<String>();
			}
			if (!contains(note, value)) {
				note.add(value);
			}
			notes.put(language, note);
			ts.setNote(notes);
		}
	},
	OWLSAMEAS(EdmLabel.TS_OWL_SAMEAS.toString(), false) {

		@Override
		public void appendValue(Timespan ts, String language, String value) {
			String[] strArray = ts.getOwlSameAs();
			if (strArray == null) {
				strArray = new String[] { value };
			} else {
				if (!contains(strArray, value)) {
					strArray = StringArrayUtils.addToArray(strArray, value);
				}
			}
			ts.setOwlSameAs(strArray);

		}

	};
	String field;
	Boolean isMulti;

	/**
	 * Retrieve the appropriate Timespan method according to a value
	 * 
	 * @param val The value to check
	 * @return
	 */
	public static TimespanMethods getMethod(String val) {
		for (TimespanMethods ag : TimespanMethods.values()) {
			if (StringUtils.equals(val, ag.field)) {
				return ag;
			}
		}
		return null;
	}

	private TimespanMethods(String field, Boolean isMulti) {
		this.field = field;
		this.isMulti = isMulti;
	}

	/**
	 * Append a language-value pair to a Timespan
	 * 
	 * @param ts
	 *            The Timesapn to use
	 * @param language
	 *            the language to append
	 * @param value
	 *            The value for the given language
	 */
	public abstract void appendValue(Timespan ts, String language, String value);

	/**
	 * Check if the field is Multilingual
	 * 
	 * @return
	 */
	public Boolean isMultilingual() {
		return this.isMulti;
	}

	private static boolean contains(String[] strArray, String value) {
		for (String str : strArray) {
			if (StringUtils.equals(str, value)) {
				return true;
			}
		}
		return false;
	}

	private static boolean contains(List<String> strList, String value) {
		if (strList.contains(value)) {
			return true;
		}
		return false;
	}
}

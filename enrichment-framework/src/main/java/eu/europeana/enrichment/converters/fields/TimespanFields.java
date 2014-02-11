package eu.europeana.enrichment.converters.fields;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Begin;
import eu.europeana.corelib.definitions.jibx.End;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;

public enum TimespanFields implements AbstractEnum {
	PREFLABEL {
		@Override
		public String getField() {
			return "ts_skos_prefLabel";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return null;
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			TimeSpanType ts = (TimeSpanType) entity[0];
			PrefLabel obj = new PrefLabel();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				obj.setLang(language);
			}
			obj.setString(value);
			List<PrefLabel> lst = ts.getPrefLabelList();
			if(lst==null){
				lst = new ArrayList<PrefLabel>();
			}
			lst.add(obj);
			ts.setPrefLabelList(lst);
			return (T) ts;
		}
	},ALTLABEL {
		@Override
		public String getField() {
			return "ts_skos_altLabel";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return "altLabel";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			TimeSpanType ts = (TimeSpanType) entity[0];
			AltLabel obj = new AltLabel();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				obj.setLang(language);
			}
			obj.setString(value);
			List<AltLabel> lst = ts.getAltLabelList();
			if(lst==null){
				lst = new ArrayList<AltLabel>();
			}
			lst.add(obj);
			ts.setAltLabelList(lst);
			return (T) ts;
		}
	},NOTE {
		@Override
		public String getField() {
			return "ts_skos_note";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "note";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			TimeSpanType ts = (TimeSpanType) entity[0];
			Note obj = new Note();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				obj.setLang(language);
			}
			obj.setString(value);
			List<Note> lst = ts.getNoteList();
			if(lst==null){
				lst = new ArrayList<Note>();
			}
			lst.add(obj);
			ts.setNoteList(lst);
			return (T) ts;
		}
	},ISPARTOF {
		@Override
		public String getField() {
			return "ts_dcterms_isPartOf";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return null;
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			TimeSpanType ts = (TimeSpanType) entity[0];
			IsPartOf obj = new IsPartOf();
			ResourceOrLiteralType.Resource rs = new ResourceOrLiteralType.Resource();
			rs.setResource(value);
			obj.setResource(rs);
			List<IsPartOf> lst = ts.getIsPartOfList();
			if(lst==null){
				lst = new ArrayList<IsPartOf>();
			}
			lst.add(obj);
			ts.setIsPartOfList(lst);
			return (T) ts;
		}
	},BEGIN {
		@Override
		public String getField() {
			return "ts_edm_begin";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "begin";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			TimeSpanType ts = (TimeSpanType) entity[0];
			Begin obj = new Begin();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				obj.setLang(language);
			}
			obj.setString(value);
			ts.setBegin(obj);
			return (T) ts;
		}
	},END {
		@Override
		public String getField() {
			return "ts_edm_end";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "end";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			TimeSpanType ts = (TimeSpanType) entity[0];
			End obj = new End();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				obj.setLang(language);
			}
			obj.setString(value);
			ts.setEnd(obj);
			return (T) ts;
		}
	};

}

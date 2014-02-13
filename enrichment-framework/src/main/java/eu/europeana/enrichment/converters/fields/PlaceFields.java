package eu.europeana.enrichment.converters.fields;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.corelib.definitions.jibx.Alt;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.Lat;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx._Long;


public enum PlaceFields implements AbstractEnum {
	LAT {
		@Override
		public String getField() {
			return "pl_wgs84_pos_lat";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "latitude";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			PlaceType place = (PlaceType) entity[0];
			Lat lat = new Lat();
			lat.setLat(Float.parseFloat(value));
			place.setLat(lat);
			return (T) place;
		}
	},
	LONG {
		@Override
		public String getField() {
			return "pl_wgs84_pos_long";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "longitude";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			PlaceType place = (PlaceType) entity[0];
			_Long _long = new _Long();
			_long.setLong(Float.parseFloat(value));
			place.setLong(_long);
			return (T) place;
		}
	},
	ALT {
		@Override
		public String getField() {
			return "pl_wgs84_pos_alt";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "altitude";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			PlaceType place = (PlaceType) entity[0];
			Alt alt = new Alt();
			alt.setAlt(Float.parseFloat(value));
			place.setAlt(alt);
			return (T) place;
		}
	},
	PREFLABEL {
		@Override
		public String getField() {
			return "pl_skos_prefLabel";
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
			PlaceType place = (PlaceType) entity[0];
			PrefLabel pl = new PrefLabel();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			List<PrefLabel> prefLabelList = place.getPrefLabelList();
			if(prefLabelList==null){
				prefLabelList = new ArrayList<PrefLabel>();
			}
			prefLabelList.add(pl);
			place.setPrefLabelList(prefLabelList);
			return (T) place;
		}
	},
	ALTLABEL {
		@Override
		public String getField() {
			return "pl_skos_altLabel";
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
			PlaceType place = (PlaceType) entity[0];
			AltLabel pl = new AltLabel();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			List<AltLabel> lst = place.getAltLabelList();
			if(lst==null){
				lst= new ArrayList<AltLabel>();
			}
			lst.add(pl);
			place.setAltLabelList(lst);
			return (T) place;
		}
	},
	NOTE {
		@Override
		public String getField() {
			return "pl_skos_note";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return "note";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			PlaceType place = (PlaceType) entity[0];
			Note pl = new Note();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			List<Note> lst = place.getNoteList();
			if(lst==null){
				lst= new ArrayList<Note>();
			}
			lst.add(pl);
			place.setNoteList(lst);
			return (T) place;
		}
	},
	ISPARTOF {
		@Override
		public String getField() {
			return "pl_dcterms_isPartOf";
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
			PlaceType place = (PlaceType) entity[0];
			IsPartOf pl = new IsPartOf();
			ResourceOrLiteralType.Resource rs = new ResourceOrLiteralType.Resource();
			rs.setResource(value);
			pl.setResource(rs);
			List<IsPartOf> lst = place.getIsPartOfList();
			if(lst==null){
				lst= new ArrayList<IsPartOf>();
			}
			lst.add(pl);
			place.setIsPartOfList(lst);
			return (T) place;
		}
	};
}

package eu.europeana.enrichment.converters.fields;

import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Broader;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PrefLabel;

@SuppressWarnings("unchecked")
public enum  ConceptFields implements AbstractEnum {
	PREFLABEL {
		@Override
		public String getField() {
			return "cc_skos_prefLabel";
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
		public <T> T generateField(String value, String lang, T... choices) {
			PrefLabel prefLabel = new PrefLabel();
			LiteralType.Lang language = new LiteralType.Lang();
			if (lang != null) {
				language.setLang(lang);
				prefLabel.setLang(language);
			}
			prefLabel.setString(value);
			Concept.Choice choice = new Concept.Choice();
			choice.setPrefLabel(prefLabel);
			return (T)choice;
		}
	},
	ALTLABEL {
		@Override
		public String getField() {
			return "cc_skos_altLabel";
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
		public <T> T generateField(String value, String lang, T... choices) {
			AltLabel altLabel = new AltLabel();
			LiteralType.Lang language = new LiteralType.Lang();
			if (lang != null) {
				language.setLang(lang);
				altLabel.setLang(language);
			}
			altLabel.setString(value);
			Concept.Choice choice = new Concept.Choice();
			choice.setAltLabel(altLabel);
			return (T)choice;
		}
	},
	BROADER {
		@Override
		public String getField() {
			return "cc_skos_broader";
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
		public <T> T generateField(String value, String lang, T... choices) {
			Broader broader = new Broader();
			broader.setResource(value);
			Concept.Choice choice = new Concept.Choice();
			choice.setBroader(broader);
			return (T) choice;
		}
	},
	NOTE {
		@Override
		public String getField() {
			return "cc_skos_note";
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
		public <T> T generateField(String value, String lang, T... choices) {
			Note note = new Note();
			LiteralType.Lang language = new LiteralType.Lang();
			if (lang != null) {
				language.setLang(lang);
				note.setLang(language);
			}
			note.setString(value);
			Concept.Choice choice = new Concept.Choice();
			choice.setNote(note);
			return (T)choice;
		}
	};

}

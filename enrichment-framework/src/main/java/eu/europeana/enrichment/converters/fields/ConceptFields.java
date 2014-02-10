package eu.europeana.enrichment.converters.fields;

public enum ConceptFields implements AbstractEnum {
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
	},ALTLABEL {
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
	},BROADER {
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
	},NOTE {
		@Override
		public String getField() {
			return "cc_skos_note";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "note";
		}
	}
	;

}

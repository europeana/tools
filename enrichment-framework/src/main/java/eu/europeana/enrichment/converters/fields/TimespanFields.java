package eu.europeana.enrichment.converters.fields;

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
	};

}

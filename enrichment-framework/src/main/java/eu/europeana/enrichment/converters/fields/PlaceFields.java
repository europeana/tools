package eu.europeana.enrichment.converters.fields;

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
	},
	NOTE {
		@Override
		public String getField() {
			return "pl_skos_note";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "note";
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
	};
}

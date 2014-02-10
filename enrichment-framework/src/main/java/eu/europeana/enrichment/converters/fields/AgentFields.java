package eu.europeana.enrichment.converters.fields;

public enum AgentFields implements AbstractEnum {
	PREFLABEL {
		@Override
		public String getField() {
			return "ag_skos_prefLabel";
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
			return "ag_skos_altLabel";
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
			return "ag_skos_note";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return "note";
		}
	},
	DATE {
		@Override
		public String getField() {
			return "ag_dc_date";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "date";
		}
	},
	ISPARTOF {
		@Override
		public String getField() {
			return "ag_dcterms_isPartOf";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return null;
		}
	},
	BEGIN {
		@Override
		public String getField() {
			return "ag_edm_begin";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "begin";
		}
	},
	END {
		@Override
		public String getField() {
			return "ag_edm_end";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "end";
		}
	},
	NAME {
		@Override
		public String getField() {
			return "ag_foaf_name";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return "name";
		}
	},
	BIOGRAPHICALINFORMATION {
		@Override
		public String getField() {
			return "ag_rdagr2_biographicalInformation";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "biographicalInformation";
		}
	},
	DATEOFBIRTH {
		@Override
		public String getField() {
			return "ag_rdagr2_dateOfBirth";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "dateOfBirth";
		}
	},
	DATEOFDEATH {
		@Override
		public String getField() {
			return "ag_rdagr2_dateOfDeath";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "dateOfDeath";
		}
	},
	DATEOFESTABLISHMENT {
		@Override
		public String getField() {
			return "ag_rdagr2_dateOfEstablishment";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "dateOfEstablishment";
		}
	},
	DATEOFTERMINATION {
		@Override
		public String getField() {
			return "ag_rdagr2_dateOfTermination";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "dateOfTermination";
		}
	},
	GENDER {
		@Override
		public String getField() {
			return "ag_rdagr2_gender";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "gender";
		}
	},
	PROFESSIONOROCCUPATION {
		@Override
		public String getField() {
			return "ag_rdagr2_professionOrOccupation";
		}

		@Override
		public boolean isMulti() {
			return false;
		}

		@Override
		public String getInputField() {
			return "professionOrOccupation";
		}
	};

}

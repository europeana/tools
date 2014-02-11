package eu.europeana.enrichment.converters.fields;


import java.util.ArrayList;
import java.util.List;

import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Begin;
import eu.europeana.corelib.definitions.jibx.BiographicalInformation;
import eu.europeana.corelib.definitions.jibx.DateOfBirth;
import eu.europeana.corelib.definitions.jibx.DateOfDeath;
import eu.europeana.corelib.definitions.jibx.DateOfEstablishment;
import eu.europeana.corelib.definitions.jibx.DateOfTermination;
import eu.europeana.corelib.definitions.jibx.End;
import eu.europeana.corelib.definitions.jibx.Gender;
import eu.europeana.corelib.definitions.jibx.Identifier;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.IsRelatedTo;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.Name;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.jibx.ProfessionOrOccupation;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.corelib.definitions.jibx.SameAs;

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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			PrefLabel pl = new PrefLabel();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			List<PrefLabel> plList = agent.getPrefLabelList();
			if(plList == null){
				plList = new ArrayList<PrefLabel>();
			}
			plList.add(pl);
			agent.setPrefLabelList(plList);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			AltLabel pl = new AltLabel();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			
			List<AltLabel> plList = agent.getAltLabelList();
			if(plList == null){
				plList = new ArrayList<AltLabel>();
			}
			plList.add(pl);
			agent.setAltLabelList(plList);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			Note pl = new Note();
			if(lang!=null){
				Lang language = new Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			List<Note> plList = agent.getNoteList();
			if(plList == null){
				plList = new ArrayList<Note>();
			}
			plList.add(pl);
			agent.setNoteList(plList);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			IsPartOf pl = new IsPartOf();
			Resource rs = new Resource();
			rs.setResource(value);
			pl.setResource(rs);
			List<IsPartOf> isPartOfList = agent.getIsPartOfList();
			if(isPartOfList==null){
				isPartOfList = new ArrayList<IsPartOf>();
			}
			isPartOfList.add(pl);
			agent.setIsPartOfList(isPartOfList);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			Begin pl = new Begin();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setBegin(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			End pl = new End();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setEnd(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			Name pl = new Name();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			List<Name> plList = agent.getNameList();
			if(plList == null){
				plList = new ArrayList<Name>();
			}
			plList.add(pl);
			agent.setNameList(plList);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			BiographicalInformation pl = new BiographicalInformation();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setBiographicalInformation(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			DateOfBirth pl = new DateOfBirth();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setDateOfBirth(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			DateOfDeath pl = new DateOfDeath();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setDateOfDeath(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			DateOfEstablishment pl = new DateOfEstablishment();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setDateOfEstablishment(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			DateOfTermination pl = new DateOfTermination();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setDateOfTermination(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			Gender pl = new Gender();
			if(lang!=null){
				LiteralType.Lang language = new LiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setGender(pl);
			return (T) agent;
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

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			ProfessionOrOccupation pl = new ProfessionOrOccupation();
			if(lang!=null){
				ResourceOrLiteralType.Lang language = new ResourceOrLiteralType.Lang();
				language.setLang(lang);
				pl.setLang(language);
			}
			pl.setString(value);
			agent.setProfessionOrOccupation(pl);
			return (T) agent;
		}
	},
	SAMEAS {

		@Override
		public String getField() {
			
			return "ag_owl_sameAs";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return "sameAs";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			SameAs pl = new SameAs();
			pl.setResource(value);
			List<SameAs> sameAsList = agent.getSameAList();
			if(sameAsList == null){
				sameAsList = new ArrayList<SameAs>();
			}
			sameAsList.add(pl);
			agent.setSameAList(sameAsList);
			return (T) agent;
		}
		
	},
	ISRELATEDTO{

		@Override
		public String getField() {
			return "ag_edm_isRelatedTo";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return "isRelatedTo";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			IsRelatedTo pl = new IsRelatedTo();
			Resource rs = new Resource();
			rs.setResource(value);
			pl.setResource(rs);
			List<IsRelatedTo> lst = agent.getIsRelatedToList();
			if(lst == null){
				lst = new ArrayList<IsRelatedTo>();
			}
			lst.add(pl);
			agent.setIsRelatedToList(lst);
			return (T) agent;
		}
		
	},
	IDENTIFIER{

		@Override
		public String getField() {
			return "ag_dc_identifier";
		}

		@Override
		public boolean isMulti() {
			return true;
		}

		@Override
		public String getInputField() {
			return "identifier";
		}

		@Override
		public <T> T generateField(String value, String lang, T... entity) {
			AgentType agent = (AgentType) entity[0];
			Identifier pl = new Identifier();
			pl.setString(value);
			List<Identifier> lst = agent.getIdentifierList();
			if(lst == null){
				lst = new ArrayList<Identifier>();
			}
			lst.add(pl);
			agent.setIdentifierList(lst);
			return (T) agent;
		}
		
	}
	;

}

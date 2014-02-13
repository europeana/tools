/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.converters.solr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.api.Factory;
import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.common.Language;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.context.EnvironmentImpl;
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.converters.europeana.EuropeanaLabelExtractor;
import eu.europeana.enrichment.model.external.EntityWrapper;
import eu.europeana.enrichment.model.external.api.InputValue;
import eu.europeana.enrichment.model.internal.CodeURI;
import eu.europeana.enrichment.model.internal.Term;
import eu.europeana.enrichment.model.internal.TermList;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.rules.ObjectRuleImpl;
import eu.europeana.enrichment.tagger.rules.LookupPersonRule;
import eu.europeana.enrichment.tagger.rules.LookupPlaceRule;
import eu.europeana.enrichment.tagger.rules.LookupTermRule;
import eu.europeana.enrichment.tagger.rules.LookupTimeRule;
import eu.europeana.enrichment.tagger.rules.PairOfStrings;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPeople;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPlaces;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTime;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;
import eu.europeana.enrichment.xconverter.api.DataObject;

/**
 * Tagging (aka semantic enrichment) of records from SOLR with built-in
 * vocabularies.
 * 
 * @author Borys Omelayenko
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class BuiltinSolrDocumentTagger {

	@SuppressWarnings("rawtypes")
	public List<EntityWrapper> tagExternal(List<InputValue> values)
			throws Exception {
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		entities.addAll(new SolrTagger().tag(values));
		return entities;
	}

	public void clearCache() {
		MongoDatabaseUtils.emptyCache();
	}

	protected VocabularyOfTime vocabularyOfPeriods = new VocabularyOfTime(
			"vocabularyOfTime", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return label.toLowerCase();
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	protected VocabularyOfPlaces vocabularyOfPlaces = new VocabularyOfPlaces(
			"vocabularyOfPlaces", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return label.toLowerCase();
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	protected VocabularyOfTerms vocabularyOfTerms = new VocabularyOfTerms(
			"vocabularyOfConcepts", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return StringUtils.lowerCase(label);
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	protected VocabularyOfPeople vocabularyOfPeople = new VocabularyOfPeople(
			"vocabularyOfActors", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return StringUtils.lowerCase(label);
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	protected Environment environment = new EnvironmentImpl();

	Task task;

	ObjectRule objectRule;

	SolrTagger periodsTagger;
	SolrTagger placesTagger;
	SolrTagger categoriesTagger;
	SolrTagger peopleTagger;
	final String DEFAULT_HOST = "localhost";
	final int DEFAULT_PORT = 27017;

	public BuiltinSolrDocumentTagger() {

	}

	private String makePlaceCoordinateQuery(String property) {
		return "PREFIX places: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
				+ "SELECT ?code ?" + property + " " + "WHERE { ?code places:"
				+ property + " ?" + property + " }";
	}

	private String makePlacePropertyQuery(String property) {
		return "PREFIX places: <http://www.europeana.eu/resolve/ontology/> "
				+ "SELECT ?code ?" + property + " " + "WHERE { ?code places:"
				+ property + " ?" + property + " }";
	}

	private String makeTimePropertyQuery(String property) {
		return "PREFIX time: <http://semium.org/time/> "
				+ "SELECT ?code ?endpoint " + "WHERE { ?code time:" + property
				+ " ?endpoint} ";
	}

	private String makePeoplePropertyQuery(String property) {
		return "PREFIX people: <http://dbpedia.org/ontology/> "
				+ "SELECT ?code ?" + property + " " + "WHERE { ?code people:"
				+ property + " ?" + property + " }";
	}

	public void init(String name, String... args) throws Exception {

		task = Factory.makeTask(name, "", "Solr tagging with time and place",
				Namespaces.ANNOCULTOR_CONVERTER, environment);
		environment.getDocDir().delete();
		objectRule = ObjectRuleImpl.makeObjectRule(task, new Path(""),
				new Path(""), new Path(""), null, false);
		String host = DEFAULT_HOST;
		int port = DEFAULT_PORT;
		if (args != null && args.length > 1) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}
		if (!MongoDatabaseUtils.dbExists(host, port)) {
			File cacheDir = new File(
					"/home/gmamakis/git/tools/annocultor_solr4/converters/vocabularies/tmp");
			File baseDir = new File(
					"/home/gmamakis/git/tools/annocultor_solr4/converters/vocabularies/");
			String placeFiles = "places/EU/*.rdf";
			String countryFiles = "places/countries/*.rdf";
			vocabularyOfPlaces.loadTermsSPARQL(
					vocabularyOfPlaces.makeTermsQuery("dcterms:isPartOf"),
					cacheDir, baseDir, placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("population",
					makePlacePropertyQuery("population"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("division",
					makePlacePropertyQuery("division"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("latitude",
					makePlaceCoordinateQuery("lat"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("longitude",
					makePlaceCoordinateQuery("long"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("country",
					makePlacePropertyQuery("country"), cacheDir, baseDir,
					placeFiles, countryFiles);

			MongoDatabaseUtils.save("place", vocabularyOfPlaces);
			String timeFiles = "time/*.rdf";
			vocabularyOfPeriods.loadTermsSPARQL(
					vocabularyOfPeriods.makeTermsQuery("dcterms:isPartOf"),
					cacheDir, baseDir, timeFiles);
			vocabularyOfPeriods.loadTermPropertiesSPARQL("begin",
					makeTimePropertyQuery("beginDate"), cacheDir, baseDir,
					timeFiles);
			vocabularyOfPeriods.loadTermPropertiesSPARQL("end",
					makeTimePropertyQuery("endDate"), cacheDir, baseDir,
					timeFiles);
			MongoDatabaseUtils.save("period", vocabularyOfPeriods);
			vocabularyOfTerms.loadTermsSPARQL(
					vocabularyOfTerms.makeTermsQuery("skos:broader"), cacheDir,
					baseDir, "concepts/gemet/gemet*.rdf");
			vocabularyOfTerms.loadTermsSPARQL(
					vocabularyOfTerms.makeTermsQuery("skos:broader"), cacheDir,
					baseDir, "concepts/wikipedia/*.rdf");

			MongoDatabaseUtils.save("concept", vocabularyOfTerms);

			String peopleFiles = "people/*.rdf";
			vocabularyOfPeople.loadTermsSPARQL(
					vocabularyOfPeople.makeTermsQuery("dcterms:isPartOf"),
					cacheDir, baseDir, peopleFiles);
			vocabularyOfPeople.loadTermPropertiesSPARQL("birth",
					makePeoplePropertyQuery("birth"), cacheDir, baseDir,
					peopleFiles);
			vocabularyOfPeople.loadTermPropertiesSPARQL("death",
					makePeoplePropertyQuery("death"), cacheDir, baseDir,
					peopleFiles);

			MongoDatabaseUtils.save("people", vocabularyOfPeople);
		}

	}

	LookupTimeRule makePeriodLookupRule(String field) throws Exception {
		LookupTimeRule rule = new LookupTimeRule(null, null,
				eu.europeana.enrichment.api.Factory.makeIgnoreGraph(task, ""),
				null, null, "periods", "(no_split_should_ever_happen)",
				vocabularyOfPeriods) {

			@Override
			protected void processLookupMatch(TermList terms, String termUri,
					String subject, DataObject dataObject,
					boolean createTermDefinion) throws Exception {
				// skip it
			}

			// missing years are reported all together
			@Override
			protected void reportMatch(TermList terms) throws Exception {

				for (Term term : terms) {
					if (!term.getLabel().matches("^(\\d\\d\\d\\d)$")) {
						super.reportMatch(terms);
						return;
					}
				}

				TermList genericYear = new TermList();
				genericYear
						.add(new Term(
								"A year of four digits",
								null,
								new CodeURI("http://semium.org/time/year/XXXX"),
								"time"));
				super.reportMatch(genericYear);
			}

			@Override
			protected PairOfStrings splitToStartAndEnd(DataObject converter,
					String label, Language.Lang lang) {
				return eu.europeana.enrichment.converters.europeana.EuropeanaTimeUtils
						.splitToStartAndEnd(label);
			}

			@Override
			public Triple onInvocation(Triple sourceTriple,
					DataObject sourceDataObject) throws Exception {

				String termLabel = sourceTriple.getValue().getValue();
				// removing "made" and "printed" used by some providers
				termLabel = StringUtils.removeEnd(termLabel, " made");
				termLabel = StringUtils.removeEnd(termLabel, " printed");
				termLabel = StringUtils.removeEnd(termLabel, " built");
				termLabel = StringUtils.removeEnd(termLabel, " existed");
				termLabel = StringUtils.removeEnd(termLabel, " written");
				termLabel = StringUtils.removeEnd(termLabel, " photographed");
				termLabel = StringUtils.removeEnd(termLabel, " surveyed");
				termLabel = StringUtils.removeEnd(termLabel, " manufactured");
				termLabel = StringUtils.removeEnd(termLabel, " taken");
				termLabel = StringUtils
						.removeEnd(termLabel, " first published");
				termLabel = StringUtils.removeEnd(termLabel, " published");
				termLabel = StringUtils.removeEnd(termLabel,
						" cuttings collected");

				// remove trailing ,, e.g 19 siete,
				termLabel = StringUtils.removeEnd(termLabel, ",");

				return sourceTriple.changeValue(new LiteralValue(termLabel));
			}

		};

		rule.setObjectRule(objectRule);
		rule.setTask(task);
		rule.setSourcePath(new Path(field));
		rule.addLabelExtractor(new EuropeanaLabelExtractor(false));
		return rule;
	}

	LookupPlaceRule makePlaceLookupRule(String field) throws Exception {
		LookupPlaceRule rule = new LookupPlaceRule(null, null,
				eu.europeana.enrichment.api.Factory.makeIgnoreGraph(task, ""),
				null, null, "places", "(no_split_should_ever_happen)",
				vocabularyOfPlaces) {

			@Override
			protected void processLookupMatch(TermList terms, String termUri,
					String subject, DataObject dataObject,
					boolean createTermDefinion) throws Exception {
				// skip it
			}

		};

		rule.setObjectRule(objectRule);
		rule.setTask(task);
		rule.setSourcePath(new Path(field));
		rule.addLabelExtractor(new EuropeanaLabelExtractor(false));

		return rule;
	}

	LookupTermRule makeTermLookupRule(String field) throws Exception {
		LookupTermRule rule = new LookupTermRule(null, null,
				eu.europeana.enrichment.api.Factory.makeIgnoreGraph(task, ""),
				null, null, "places", "(no_split_should_ever_happen)",
				vocabularyOfTerms) {

			@Override
			protected void processLookupMatch(TermList terms, String termUri,
					String subject, DataObject dataObject,
					boolean createTermDefinion) throws Exception {
				// skip it
			}

		};

		rule.setObjectRule(objectRule);
		rule.setTask(task);
		rule.setSourcePath(new Path(field));
		rule.addLabelExtractor(new EuropeanaLabelExtractor(false));

		return rule;
	}

	LookupPersonRule makePersonLookupRule(String field) throws Exception {
		LookupPersonRule rule = new LookupPersonRule(null,
				new Property("dummy"),
				eu.europeana.enrichment.api.Factory.makeIgnoreGraph(task, ""),
				eu.europeana.enrichment.api.Factory.makeIgnoreGraph(task, ""),
				new Path("dummyBirthDatePath"), new Path("dummyDeathDatePath"),
				new Property("dummy"), "actors",
				"(no_split_should_ever_happen)", vocabularyOfPeople) {

			@Override
			protected void processLookupMatch(TermList terms, String termUri,
					String subject, DataObject dataObject,
					boolean createTermDefinion) throws Exception {
				// skip it
			}

		};

		rule.setObjectRule(objectRule);
		rule.setTask(task);
		rule.setSourcePath(new Path(field));
		rule.addLabelExtractor(new EuropeanaLabelExtractor(false));

		return rule;
	}

}

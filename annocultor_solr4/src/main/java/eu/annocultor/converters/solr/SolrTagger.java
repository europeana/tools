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
package eu.annocultor.converters.solr;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.converters.europeana.Entity;
import eu.annocultor.converters.europeana.Field;
import eu.annocultor.tagger.rules.AbstractLookupRule;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.terms.ParentTermReconstructor;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Triple;
import eu.annocultor.utils.MongoDatabaseUtils;

/**
 * Lookup (tagging) rule.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class SolrTagger {

	public static class FieldRulePair {
		String field;
		AbstractLookupRule rule;

		public FieldRulePair(String field, AbstractLookupRule rule) {
			this.field = field;
			this.rule = rule;
		}

		public String getField() {
			return field;
		}

		public AbstractLookupRule getRule() {
			return rule;
		}

	}

	FieldRulePair[] fieldRulePairs;

	// Vocabulary vocabulary;

	String dbtable;
	String termFieldName;

	String labelFieldName;

	Set<String> broaderLabels;
	String broaderTermFieldName;
	String broaderLabelFieldName;

	ParentTermReconstructor parentTermReconstructor = new ParentTermReconstructor(
			10000);

	public SolrTagger(
			// Vocabulary vocabulary,
			String dbtable, String termFieldName, String labelFieldName,
			String broaderTermFieldName, String broaderLabelFieldName,
			FieldRulePair... fieldRulePairs) {
		// this.vocabulary = vocabulary;
		this.dbtable = dbtable;
		this.fieldRulePairs = fieldRulePairs;
		this.termFieldName = termFieldName;
		this.labelFieldName = labelFieldName;
		this.broaderTermFieldName = broaderTermFieldName;
		this.broaderLabelFieldName = broaderLabelFieldName;
	}

	void afterTermMatched(Term term) throws Exception {

	}

	void afterDocument(SolrInputDocument document) {

	}

	void beforeDocument(SolrInputDocument document) {

	}

	List<Entity> tag(SolrInputDocument document) throws Exception,
			ParseException {

		List<Term> broaderTerms = new ArrayList<Term>();

		beforeDocument(document);

		Set<String> codes = new HashSet<String>();
		Set<String> labels = new HashSet<String>();
		// modified by gmamakis
		List<Entity> entities = new ArrayList<Entity>();

		for (FieldRulePair frp : fieldRulePairs) {

			Collection<Object> values = document.getFieldValues(frp.getField());

			if (values != null) {

				for (Object valueObject : values) {
					if (valueObject != null) {
						String vocab = "";
						// long startEntity = new Date().getTime();
						Entity entity = new Entity();
						if (StringUtils.equals(termFieldName, "skos_concept")) {
							entity.setClassName("Concept");
							vocab = "concept";
						} else if (StringUtils.equals(termFieldName,
								"edm_place")) {
							entity.setClassName("Place");
							vocab = "place";
						} else if (StringUtils.startsWith(termFieldName,
								"edm_timespan")) {
							entity.setClassName("Timespan");
							vocab = "period";
						} else {
							entity.setClassName("Agent");
							vocab = "people";
						}
						entity.setOriginalField(frp.getField());
						
						String value = valueObject.toString();
						if (!StringUtils.isBlank(value)) {
							TermList terms = MongoDatabaseUtils.findByLabel(
									value.toLowerCase(), vocab);
							List<Field> entityFields = new ArrayList<Field>();
							if (terms != null && terms.size() > 0) {
								for (Term term : terms) {
									if (!entityExists(term.getCode(), entities)) {
										entity.setUri(term.getCode());
										codes.add(term.getCode());
										Field codeField = new Field();
										codeField.setName(termFieldName);
										Map<String, List<String>> codeFieldValueLang = new HashMap<String, List<String>>();
										List<String> codeVal = new ArrayList<String>();
										codeVal.add(term.getCode());
										codeFieldValueLang.put("def", codeVal);
										codeField.setValues(codeFieldValueLang);
										entityFields.add(codeField);
										Field labelField = new Field();
										labelField.setName(labelFieldName);
										if (StringUtils.equals(
												entity.getClassName(), "Place")) {
											if (!StringUtils.endsWith(term
													.getProperty("division"),
													"A.PCLI")) {
												String latitude = term
														.getProperty("latitude");
												String longitude = term
														.getProperty("longitude");
												if (Float.parseFloat(latitude) != 0
														&& Float.parseFloat(longitude) != 0) {
													Field latField = new Field();
													latField.setName("pl_wgs84_pos_lat");
													Map<String, List<String>> latValues = new HashMap<String, List<String>>();
													List<String> lats = new ArrayList<String>();
													lats.add(latitude);
													latValues.put("def", lats);
													latField.setValues(latValues);
													entityFields.add(latField);
													Field longField = new Field();
													longField
															.setName("pl_wgs84_pos_long");
													Map<String, List<String>> longValues = new HashMap<String, List<String>>();
													List<String> longs = new ArrayList<String>();
													longs.add(longitude);
													longValues
															.put("def", longs);
													longField
															.setValues(longValues);
													entityFields.add(longField);
												}

											}
										}

										TermList altTerms = MongoDatabaseUtils
												.findByCode(
														new CodeURI(term
																.getCode()),
														dbtable);
										Map<String, List<String>> fieldValuesLang = new HashMap<String, List<String>>();
										List<String> fieldVals = new ArrayList<String>();
										for (Term altTerm : altTerms) {
											if (shouldInclude(altTerm)) {
												String key = altTerm.getLang() != null ? altTerm
														.getLang().toString()
														: "def";

												if (fieldValuesLang
														.containsKey(key)) {
													fieldVals = fieldValuesLang
															.get(key);
												} else {
													fieldVals = new ArrayList<String>();
												}

												fieldVals.add(altTerm
														.getLabel());
												fieldValuesLang.put(key,
														fieldVals);
												labelField
														.setValues(fieldValuesLang);
												labels.add(altTerm.getLabel());
											}
										}
										entityFields.add(labelField);
										if (broaderTermFieldName != null) {
											List<Term> parents = parentTermReconstructor
													.allParents(altTerms, vocab);
											if(parents!=null){
											broaderTerms.addAll(parents);

											Field broaderField = new Field();
											broaderField
													.setName(broaderTermFieldName);
											Map<String, List<String>> langVals = new HashMap<String, List<String>>();
											List<String> vals = new ArrayList<String>();

											for (Term termEuropeana : parents) {

												String key = termEuropeana
														.getLang() != null ? termEuropeana
														.getLang().toString()
														: "def";

												if (langVals.containsKey(key)) {
													vals = langVals
															.get(termEuropeana
																	.getLang() != null ? termEuropeana
																	.getLang()
																	.toString()
																	: "def");
												} else {
													vals = new ArrayList<String>();
												}
												if (!vals
														.contains(termEuropeana
																.getCode())) {
													vals.add(termEuropeana
															.getCode());
												}
												langVals.put(
														termEuropeana.getLang() != null ? termEuropeana
																.getLang()
																.toString()
																: "def", vals);

											}
											broaderField.setValues(langVals);
											entityFields.add(broaderField);

											entities.addAll(computeBroaderLabels2(
													vocab, frp.getField(),
													broaderTerms));
											}
										}
									}
								}

							}
							entity.setFields(entityFields);
						}
						if (entity.getFields() != null
								&& entity.getFields().size() > 0) {
							entities.add(entity);
						}
					}
				}
			}
		}

		return clean(entities);
	}

	private List<Entity> clean(List<Entity> entities) {
		for (int i = 0; i < entities.size() - 1; i++) {
			for (int k = i + 1; k < entities.size(); k++) {
				if (StringUtils.equals(entities.get(i).getUri(), entities
						.get(k).getUri())) {
					entities.remove(k);
					k--;
				}
			}
		}
		return entities;
	}

	private boolean entityExists(String code, List<Entity> entities) {
		for (Entity entity : entities) {
			if (StringUtils.equals(code, entity.getUri())) {
				return true;
			}
		}

		return false;
	}

	void computeBroaderLabels(String vocab, List<Term> broaderTerms, MongoDatabaseUtils db)
			throws Exception {
		for (Term broaderTerm : broaderTerms) {
			TermList altTerms = db.findByCode(new CodeURI(
					broaderTerm.getCode()), dbtable);

			for (Term altTerm : altTerms) {
				if (altTerm != null && !StringUtils.isEmpty(altTerm.getLabel())
						&& shouldInclude(altTerm)) {
					broaderLabels.add(altTerm.getLabel());
				}
			}
		}
	}


	 List<Entity> computeBroaderLabels2(String vocab, String originalField,
			List<Term> broaderTerms) throws Exception {
		List<Entity> entities = new ArrayList<Entity>();

		for (Term broaderTerm : broaderTerms) {

			TermList altTerms = MongoDatabaseUtils.findByCode(new CodeURI(
					broaderTerm.getCode()), dbtable);
			if (altTerms != null && altTerms.size() > 0) {
				if (!entityExists(altTerms.getFirst().getCode(), entities)) {
					Entity entity = new Entity();
					if (StringUtils.equals(termFieldName, "skos_concept")) {
						entity.setClassName("Concept");
					} else if (StringUtils.equals(termFieldName, "edm_place")) {
						entity.setClassName("Place");
					} else if (StringUtils.startsWith(termFieldName,
							"edm_timespan")) {
						entity.setClassName("Timespan");
					} else {
						entity.setClassName("Agent");
					}

					entity.setUri(altTerms.getFirst().getCode());
					entity.setOriginalField("");
					Field codeField = new Field();
					codeField.setName(termFieldName);
					Map<String, List<String>> codeFieldValueMap = new HashMap<String, List<String>>();
					List<String> codeFieldValue = Collections
							.synchronizedList(new ArrayList<String>());
					if (!codeFieldValue.contains(altTerms.getFirst().getCode())) {
						codeFieldValue.add(altTerms.getFirst().getCode());
						codeFieldValueMap.put("def", codeFieldValue);
						codeField.setValues(codeFieldValueMap);
					}
					List<Field> fields = new ArrayList<Field>();
					fields.add(codeField);
					for (Term altTerm : altTerms) {
						Field field = new Field();
						field.setName(labelFieldName);
						Map<String, List<String>> fieldValuesLang = new HashMap<String, List<String>>();
						List<String> fieldValues = new ArrayList<String>();
						fieldValues.add(altTerm.getLabel());
						fieldValuesLang.put(altTerm.getLang() != null ? altTerm
								.getLang().toString() : "def", fieldValues);
						field.setValues(fieldValuesLang);
						fields.add(field);
						if (StringUtils.equals(entity.getClassName(), "Place")) {
							if (!StringUtils.endsWith(
									altTerm.getProperty("division"), "A.PCLI")) {
								Field latField = new Field();
								latField.setName("pl_wgs84_pos_lat");
								Map<String, List<String>> latValues = new HashMap<String, List<String>>();
								List<String> lats = new ArrayList<String>();
								lats.add(altTerm.getProperty("latitude"));
								latValues.put("def", lats);
								latField.setValues(latValues);
								fields.add(latField);
								Field longField = new Field();
								longField.setName("pl_wgs84_pos_long");
								Map<String, List<String>> longValues = new HashMap<String, List<String>>();
								List<String> longs = new ArrayList<String>();
								longs.add(altTerm.getProperty("longitude"));
								longValues.put("def", longs);
								longField.setValues(longValues);
								fields.add(longField);

							}
						}
					}
					entity.setFields(fields);
					entities.add(entity);
				}
			}
		}
		return entities;
	}

	void addBroaderTermsAndLabels(SolrInputDocument document,
			List<Term> broaderTerms) {
		if (!StringUtils.isEmpty(broaderTermFieldName)) {

			Set<String> broaderCodes = new HashSet<String>();
			for (Term term : broaderTerms) {
				broaderCodes.add(term.getCode());
			}

			for (String code : broaderCodes) {
				document.addField(broaderTermFieldName, code);
			}
		}

		if (!StringUtils.isEmpty(broaderLabelFieldName)) {
			for (String label : broaderLabels) {
				document.addField(broaderLabelFieldName, label);
			}
		}
	}

	boolean shouldInclude(Term term) {
		return true;
	}
}

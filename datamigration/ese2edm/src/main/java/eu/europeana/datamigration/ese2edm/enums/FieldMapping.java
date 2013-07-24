/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.datamigration.ese2edm.enums;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.definitions.solr.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.solr.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.utils.StringArrayUtils;
import eu.europeana.datamigration.ese2edm.exception.EntityNotFoundException;
import eu.europeana.datamigration.ese2edm.exception.MultipleUniqueFieldsException;

/**
 * The mappings between ESE and EDM
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@SuppressWarnings("unchecked")
public enum FieldMapping {

	EUROPEANA_URI("europeana_uri", "europeana_id", false, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			fullBean.setEuropeanaId(ObjectId.massageToObjectId(value));
			fullBean.setAbout(value);
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			aggregation.setAbout("/aggregation/provider" + value);
			if (aggregations.size() > 0) {
				aggregations.set(0, aggregation);
			} else {
				aggregations.add(aggregation);
			}
			fullBean.setAggregations(aggregations);
			EuropeanaAggregation europeanaAggregation = fullBean
					.getEuropeanaAggregation();
			europeanaAggregation.setAbout("/aggregation/europeana" + value);
			europeanaAggregation
					.setEdmLandingPage("http://www.europeana.eu/portal/record"
							+ value + ".html");
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);

			proxy.setAbout("/proxy/provider" + value);
			proxy.setProxyFor("/item" + value);
			proxy.setProxyIn(new String[] { "/aggregation/provider" + value });

			ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
			europeanaProxy.setAbout("/proxy/europeana" + value);
			europeanaProxy.setProxyFor("/item" + value);
			europeanaProxy.setProxyIn(new String[] { "/aggregation/europeana"
					+ value });

			if (proxies.size() > 0) {
				proxies.set(0, proxy);
			} else {
				proxies.add(proxy);
			}
			if (proxies.size() > 1) {
				proxies.set(1, europeanaProxy);
			} else {
				proxies.add(europeanaProxy);
			}
			fullBean.setProxies(proxies);
			ProvidedCHOImpl providedCHO = new ProvidedCHOImpl();
			providedCHO.setAbout("/item" + value);
			List<ProvidedCHOImpl> providedChos = new ArrayList<ProvidedCHOImpl>();
			providedChos.add(providedCHO);
			fullBean.setProvidedCHOs(providedChos);
			fullBean.setEuropeanaAggregation(europeanaAggregation);
		}
	},
	EUROPEANA_COLLECTIONNAME("europeana_collectionName",
			"europeana_collectionName", false, false) {
		@Override
		public void setField(FullBean fullBean, String value) {
			String[] arr = new String[] { value };
			fullBean.setEuropeanaCollectionName(arr);
		}
	},
	EUROPEANA_TYPE("europeana_type", "proxy_edm_type", false, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			proxy.setEdmType(DocType.safeValueOf(value));
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
			fullBean.setType(DocType.safeValueOf(value));
		}
	},
	EUROPEANA_OBJECT("europeana_object", "provider_aggregation_edm_object",
			false, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			if (StringUtils.isNotEmpty(aggregation.getEdmObject())) {
				throw new MultipleUniqueFieldsException("Duplicate Object",
						fullBean.getAbout(), aggregation.getEdmObject(), value);
			} else {
				aggregation.setEdmObject(value);
					fullBean.getEuropeanaAggregation().setEdmPreview("http://europeanastatic.eu/api/image?uri="+value+"&size=LARGE&type=TEXT");
				List<WebResourceImpl> webResources = (List<WebResourceImpl>) (aggregation
						.getWebResources() != null ? aggregation
						.getWebResources() : new ArrayList<WebResourceImpl>());
				if (!webResourceExists(webResources, value)) {
					WebResourceImpl webResource = new WebResourceImpl();
					webResource.setAbout(value);

					webResources.add(webResource);
					aggregation.setWebResources(webResources);
				}
				if (aggregations.size() > 0) {
					aggregations.set(0, aggregation);
				} else {
					aggregations.add(aggregation);
				}
				fullBean.setAggregations(aggregations);
			}
		}
	},
	EUROPEANA_ISSHOWNAT("europeana_isShownAt",
			"provider_aggregation_edm_isShownAt", false, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			if (StringUtils.isNotEmpty(aggregation.getEdmIsShownAt())) {
				throw new MultipleUniqueFieldsException("Duplicate isShownAt",
						fullBean.getAbout(), aggregation.getEdmIsShownAt(),
						value);
			} else {
				aggregation.setEdmIsShownAt(value);
				List<WebResourceImpl> webResources = (List<WebResourceImpl>) (aggregation
						.getWebResources() != null ? aggregation
						.getWebResources() : new ArrayList<WebResourceImpl>());
				if (!webResourceExists(webResources, value)) {
					WebResourceImpl webResource = new WebResourceImpl();
					webResource.setAbout(value);

					webResources.add(webResource);
					aggregation.setWebResources(webResources);
				}
				if (aggregations.size() > 0) {
					aggregations.set(0, aggregation);
				} else {
					aggregations.add(aggregation);
				}
				fullBean.setAggregations(aggregations);
			}

		}
	},
	EUROPEANA_ISSHOWNBY("europeana_isShownBy",
			"provider_aggregation_edm_isShownBy", false, false) {

		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			if (StringUtils.isNotEmpty(aggregation.getEdmIsShownBy())) {
				throw new MultipleUniqueFieldsException("Duplicate isShownBy",
						fullBean.getAbout(), aggregation.getEdmIsShownBy(),
						value);
			} else {
				aggregation.setEdmIsShownBy(value);
				List<WebResourceImpl> webResources = (List<WebResourceImpl>) (aggregation
						.getWebResources() != null ? aggregation
						.getWebResources() : new ArrayList<WebResourceImpl>());
				if (!webResourceExists(webResources, value)) {
					WebResourceImpl webResource = new WebResourceImpl();
					webResource.setAbout(value);

					webResources.add(webResource);
					aggregation.setWebResources(webResources);
				}
				if (aggregations.size() > 0) {
					aggregations.set(0, aggregation);
				} else {
					aggregations.add(aggregation);
				}
				fullBean.setAggregations(aggregations);

			}
		}

	},
	EUROPEANA_PROVIDER("europeana_provider",
			"provider_aggregation_edm_provider", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			Map<String, List<String>> map = aggregation.getEdmProvider() != null ? aggregation
					.getEdmProvider() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			aggregation.setEdmProvider(map);
			if (aggregations.size() > 0) {
				aggregations.set(0, aggregation);
			} else {
				aggregations.add(aggregation);
			}
			fullBean.setAggregations(aggregations);
		}
	},
	EUROPEANA_DATAPROVIDER("europeana_dataProvider",
			"provider_aggregation_edm_dataProvider", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			Map<String, List<String>> map = aggregation.getEdmDataProvider() != null ? aggregation
					.getEdmDataProvider() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			aggregation.setEdmDataProvider(map);
			if (aggregations.size() > 0) {
				aggregations.set(0, aggregation);
			} else {
				aggregations.add(aggregation);
			}
			fullBean.setAggregations(aggregations);

		}
	},
	EUROPEANA_RIGHTS("europeana_rights", "provider_aggregation_edm_rights",
			true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			Map<String, List<String>> map = aggregation.getEdmRights() != null ? aggregation
					.getEdmProvider() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			aggregation.setEdmRights(map);
			if (aggregations.size() > 0) {
				aggregations.set(0, aggregation);
			} else {
				aggregations.add(aggregation);
			}
			fullBean.setAggregations(aggregations);
		}
	},
	EUROPEANA_UGC("europeana_UGC", "edm_UGC", false, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			aggregation.setEdmUgc(value);
			if (aggregations.size() > 0) {
				aggregations.set(0, aggregation);
			} else {
				aggregations.add(aggregation);
			}
			fullBean.setAggregations(aggregations);
		}
	},

	EUROPEANA_COMPLETENESS("europeana_completeness", "europeana_completeness",
			false, false) {
		@Override
		public void setField(FullBean fullBean, String value) {
			fullBean.setEuropeanaCompleteness(Integer.parseInt(value));
		}
	},
	EUROPEANA_PREVIEWNODISTRIBUTE("europeana_previewNoDistribute",
			"europeana_previewNoDistribute", false, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<AggregationImpl> aggregations = (List<AggregationImpl>) fetchEntity(
					AggregationImpl.class, fullBean);
			AggregationImpl aggregation = aggregations.size() > 0 ? aggregations
					.get(0) : new AggregationImpl();
			aggregation.setEdmPreviewNoDistribute(Boolean.parseBoolean(value));
			if (aggregations.size() > 0) {
				aggregations.set(0, aggregation);
			} else {
				aggregations.add(aggregation);
			}
			fullBean.setAggregations(aggregations);
		}
	},

	DC_COVERAGE("dc_coverage", "proxy_dc_coverage", true, true) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcCoverage() != null ? proxy
					.getDcCoverage() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcCoverage(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_CONTRIBUTOR("dc_contributor", "proxy_dc_contributor", true, true) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcContributor() != null ? proxy
					.getDcContributor() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcContributor(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_DESCRIPTION("dc_description", "proxy_dc_description", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcDescription() != null ? proxy
					.getDcDescription() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcDescription(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_CREATOR("dc_creator", "proxy_dc_creator", true, true) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcCreator() != null ? proxy
					.getDcCreator() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcCreator(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DC_DATE("dc_date", "proxy_dc_date", true, true) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcDate() != null ? proxy
					.getDcDate() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcDate(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DC_FORMAT("dc_format", "proxy_dc_format", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcFormat() != null ? proxy
					.getDcFormat() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcFormat(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_IDENTIFIER("dc_identifier", "proxy_dc_identifier", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcIdentifier() != null ? proxy
					.getDcIdentifier() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcIdentifier(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_LANGUAGE("dc_language", "proxy_dc_language", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcLanguage() != null ? proxy
					.getDcLanguage() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcLanguage(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_PUBLISHER("dc_publisher", "proxy_dc_publisher", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcPublisher() != null ? proxy
					.getDcPublisher() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcPublisher(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_RELATION("dc_relation", "proxy_dc_relation", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcRelation() != null ? proxy
					.getDcRelation() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcRelation(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_RIGHTS("dc_rights", "proxy_dc_rights", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcRights() != null ? proxy
					.getDcRights() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcRights(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_SOURCE("dc_source", "proxy_dc_source", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcSource() != null ? proxy
					.getDcSource() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcSource(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_SUBJECT("dc_subject", "proxy_dc_subject", true, true) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcSubject() != null ? proxy
					.getDcSubject() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcSubject(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DC_TITLE("dc_title", "proxy_dc_title", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcTitle() != null ? proxy
					.getDcTitle() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcTitle(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

			List<String> titles = fullBean.getTitle() != null ? new ArrayList<String>(
					Arrays.asList(fullBean.getTitle()))
					: new ArrayList<String>();
			titles.add(value);
			fullBean.setTitle(StringArrayUtils.toArray(titles));
		}
	},
	DC_TYPE("dc_type", "proxy_dc_type", true, true) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDcType() != null ? proxy
					.getDcType() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDcType(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_ALTERNATIVE("dcterms_alternative", "proxy_dcterms_alternative",
			true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsAlternative() != null ? proxy
					.getDctermsAlternative()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsAlternative(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

			List<String> titles = fullBean.getTitle() != null ? new ArrayList<String>(
					Arrays.asList(fullBean.getTitle()))
					: new ArrayList<String>();
			titles.add(value);
			fullBean.setTitle(StringArrayUtils.toArray(titles));
		}
	},
	DCTERMS_CREATED("dcterms_created", "proxy_dcterms_created", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsCreated() != null ? proxy
					.getDctermsCreated() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsCreated(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_CONFORMSTO("dcterms_conformsTo", "proxy_dcterms_conformsTo", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsConformsTo() != null ? proxy
					.getDctermsConformsTo()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsConformsTo(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DCTERMS_EXTENT("dcterms_extent", "proxy_dcterms_extent", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsExtent() != null ? proxy
					.getDctermsExtent() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsExtent(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DCTERMS_HASFORMAT("dcterms_hasFormat", "proxy_dcterms_hasFormat", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsHasFormat() != null ? proxy
					.getDctermsHasFormat()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsHasFormat(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DCTERMS_HASVERSION("dcterms_hasVersion", "proxy_dcterms_hasVersion", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsHasVersion() != null ? proxy
					.getDctermsHasVersion()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsHasVersion(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_ISFORMATOF("dcterms_isFormatOf", "proxy_dcterms_isFormatOf", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsIsFormatOf() != null ? proxy
					.getDctermsIsFormatOf()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsIsFormatOf(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DCTERMS_ISPARTOF("dcterms_isPartOf", "proxy_dcterms_isPartOf", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsIsPartOf() != null ? proxy
					.getDctermsIsPartOf() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsIsPartOf(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DCTERMS_ISREFERENCEDBY("dcterms_isReferencedBy",
			"proxy_dcterms_isReferencedBy", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsIsReferencedBy() != null ? proxy
					.getDctermsIsReferencedBy()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsIsReferencedBy(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_ISREPLACEDBY("dcterms_isReplacedBy", "proxy_dcterms_isReplacedBy",
			true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsIsReplacedBy() != null ? proxy
					.getDctermsIsReplacedBy()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsIsReplacedBy(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_ISREQUIREDBY("dcterms_isRequiredBy", "proxy_dcterms_isRequiredBy",
			true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsIsRequiredBy() != null ? proxy
					.getDctermsIsRequiredBy()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsIsRequiredBy(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_ISSUED("dcterms_issued", "proxy_dcterms_issued", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsIssued() != null ? proxy
					.getDctermsIssued() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsIssued(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	DCTERMS_ISVERSIONOF("dcterms_isVersionOf", "proxy_dcterms_isVersionOf",
			true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsIsVersionOf() != null ? proxy
					.getDctermsIsVersionOf()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsIsVersionOf(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_MEDIUM("dcterms_medium", "proxy_dcterms_medium", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsMedium() != null ? proxy
					.getDctermsMedium() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsMedium(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_PROVENANCE("dcterms_provenance", "proxy_dcterms_provenance", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsProvenance() != null ? proxy
					.getDctermsProvenance()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsProvenance(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_REFERENCES("dcterms_references", "proxy_dcterms_references", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsReferences() != null ? proxy
					.getDctermsReferences()
					: new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsReferences(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_REPLACES("dcterms_replaces", "proxy_dcterms_replaces", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsReplaces() != null ? proxy
					.getDctermsReplaces() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsReplaces(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_REQUIRES("dcterms_requires", "proxy_dcterms_requires", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsRequires() != null ? proxy
					.getDctermsRequires() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsRequires(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_SPATIAL("dcterms_spatial", "proxy_dcterms_spatial", true, true) {

		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsSpatial() != null ? proxy
					.getDctermsSpatial() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsSpatial(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}

	},
	DCTERMS_TOC("dcterms_tableOfContents", "proxy_dcterms_tableOfContents",
			true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsTOC() != null ? proxy
					.getDctermsTOC() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsTOC(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	},
	DCTERMS_TEMPORAL("dcterms_temporal", "proxy_dcterms_temporal", true, true) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsTemporal() != null ? proxy
					.getDctermsTemporal() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsTemporal(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);
		}
	},
	// SKOS_PREFLABEL("skos_prefLabel", "cc_skos_prefLabel", true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<ConceptImpl> concepts = (List<ConceptImpl>) fetchEntity(
	// ConceptImpl.class, fullBean);
	// ConceptImpl concept = concepts.size() > 0 ? concepts.get(0)
	// : new ConceptImpl();
	// Map<String, List<String>> map = new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// concept.setPrefLabel(map);
	// if (concepts.size() > 0) {
	// concepts.set(0, concept);
	// } else {
	// concepts.add(concept);
	// }
	// fullBean.setConcepts(concepts);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getDcSubject(), "DcSubject", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// SKOS_ALTLABEL("skos_altLabel", "cc_skos_altLabel", true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<ConceptImpl> concepts = (List<ConceptImpl>) fetchEntity(
	// ConceptImpl.class, fullBean);
	// ConceptImpl concept = concepts.size() > 0 ? concepts.get(0)
	// : new ConceptImpl();
	// Map<String, List<String>> map = new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// concept.setAltLabel(map);
	// if (concepts.size() > 0) {
	// concepts.set(0, concept);
	// } else {
	// concepts.add(concept);
	// }
	// fullBean.setConcepts(concepts);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getDcSubject(), "DcSubject", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// SKOS_BROADER("skos_broader", "cc_skos_broader", false) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<ConceptImpl> concepts = (List<ConceptImpl>) fetchEntity(
	// ConceptImpl.class, fullBean);
	// ConceptImpl concept = concepts.size() > 0 ? concepts.get(0)
	// : new ConceptImpl();
	// String[] broader = new String[] { value };
	// concept.setBroader(broader);
	// if (concepts.size() > 0) {
	// concepts.set(0, concept);
	// } else {
	// concepts.add(concept);
	// }
	// fullBean.setConcepts(concepts);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getDcSubject(), "DcSubject", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// PERIOD_BEGIN("period_begin", "ts_edm_begin", true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// Map<String, List<String>> map = new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// timespan.setBegin(map);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// PERIOD_END("period_end", "ts_edm_end", true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// Map<String, List<String>> map = new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// timespan.setEnd(map);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// WGS_LAT("wgs_lat", "pl_wgs84_pos_lat", true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// place.setLatitude(Float.parseFloat(value));
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getDctermsSpatial(), "DctermsSpatial", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// WGS_LON("wgs_lon", "pl_wgs84_pos_long", true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// place.setLongitude(Float.parseFloat(value));
	//
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getDctermsSpatial(), "DctermsSpatial", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_PLACE_TERM("enrichment_place_term", "edm_place", false) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// place.setAbout(value);
	//
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_PLACE_LABEL("enrichment_place_label", "pl_skos_prefLabel",
	// true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// Map<String, List<String>> map = place.getPrefLabel() != null ? place
	// .getPrefLabel() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// place.setPrefLabel(map);
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// ENRICHMENT_PLACE_LAT("enrichment_place_latitude", "pl_wgs84_pos_lat",
	// false) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// place.setLatitude(Float.parseFloat(value));
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_PLACE_LON("enrichment_place_longitude", "pl_wgs84_pos_long",
	// false) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// place.setLongitude(Float.parseFloat(value));
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// ENRICHMENT_PERIOD_TERM("enrichment_period_term", "edm_timespan", false) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// timespan.setAbout(value);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },

	// ENRICHMENT_PERIOD_LABEL("enrichment_period_label", "ts_skos_prefLabel",
	// true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// Map<String, List<String>> map = timespan.getPrefLabel() != null ?
	// timespan
	// .getPrefLabel() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// timespan.setPrefLabel(map);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },

	// ENRICHMENT_PERIOD_BEGIN("enrichment_period_begin", "ts_edm_begin", true)
	// {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// Map<String, List<String>> map = timespan.getBegin() != null ? timespan
	// .getBegin() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// timespan.setBegin(map);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	// ENRICHMENT_PERIOD_END("enrichment_period_end", "ts_edm_end", true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// Map<String, List<String>> map = timespan.getEnd() != null ? timespan
	// .getEnd() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// timespan.setEnd(map);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_CONCEPT_TERM("enrichment_concept_term", "skos_concept", false)
	// {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<ConceptImpl> concepts = (List<ConceptImpl>) fetchEntity(
	// ConceptImpl.class, fullBean);
	// ConceptImpl concept = concepts.size() > 0 ? concepts.get(0)
	// : new ConceptImpl();
	// concept.setAbout(value);
	// if (concepts.size() > 0) {
	// concepts.set(0, concept);
	// } else {
	// concepts.add(concept);
	// }
	// fullBean.setConcepts(concepts);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_CONCEPT_LABEL("enrichment_concept_label", "cc_skos_prefLabel",
	// true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<ConceptImpl> concepts = (List<ConceptImpl>) fetchEntity(
	// ConceptImpl.class, fullBean);
	// ConceptImpl concept = concepts.size() > 0 ? concepts.get(0)
	// : new ConceptImpl();
	// Map<String, List<String>> map = concept.getPrefLabel() != null ? concept
	// .getPrefLabel() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// concept.setPrefLabel(map);
	// if (concepts.size() > 0) {
	// concepts.set(0, concept);
	// } else {
	// concepts.add(concept);
	// }
	// fullBean.setConcepts(concepts);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_AGENT_TERM("enrichment_agent_term", "edm_agent", false) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<AgentImpl> agents = (List<AgentImpl>) fetchEntity(
	// AgentImpl.class, fullBean);
	// AgentImpl agent = agents.size() > 0 ? agents.get(0)
	// : new AgentImpl();
	// agent.setAbout(value);
	// if (agents.size() > 0) {
	// agents.set(0, agent);
	// } else {
	// agents.add(agent);
	// }
	// fullBean.setAgents(agents);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_AGENT_LABEL("enrichment_agent_label", "ag_skos_prefLabel",
	// true) {
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<AgentImpl> agents = (List<AgentImpl>) fetchEntity(
	// AgentImpl.class, fullBean);
	// AgentImpl agent = agents.size() > 0 ? agents.get(0)
	// : new AgentImpl();
	// Map<String, List<String>> map = agent.getPrefLabel() != null ? agent
	// .getPrefLabel() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// agent.setPrefLabel(map);
	// if (agents.size() > 0) {
	// agents.set(0, agent);
	// } else {
	// agents.add(agent);
	// }
	// fullBean.setAgents(agents);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	// },
	//
	// ENRICHMENT_PLACE_BROADER_TERM("enrichment_place_broader_term",
	// "pl_dcterms_isPartOf", true) {
	//
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// Map<String, List<String>> map = place.getIsPartOf() != null ? place
	// .getIsPartOf() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// place.setIsPartOf(map);
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	//
	// },
	//
	// ENRICHMENT_PLACE_BROADER_LABEL("enrichment_place_broader_label",
	// "pl_dcterms_isPartOf_label", true) {
	//
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<PlaceImpl> places = (List<PlaceImpl>) fetchEntity(
	// PlaceImpl.class, fullBean);
	// PlaceImpl place = places.size() > 0 ? places.get(0)
	// : new PlaceImpl();
	// Map<String, List<String>> map = place.getIsPartOf() != null ? place
	// .getIsPartOf() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// place.setIsPartOf(map);
	// if (places.size() > 0) {
	// places.set(0, place);
	// } else {
	// places.add(place);
	// }
	// fullBean.setPlaces(places);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	//
	// },
	//
	// ENRICHMENT_PERIOD_BROADER_TERM("enrichment_period_broader_term",
	// "ts_dcterms_isPartOf", true) {
	//
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// Map<String, List<String>> map = timespan.getIsPartOf() != null ? timespan
	// .getIsPartOf() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// timespan.setIsPartOf(map);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	//
	// },

	// ENRICHMENT_PERIOD_BROADER_LABEL("enrichment_period_broader_label",
	// "ts_dcterms_isPartOf_label", true) {
	//
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<TimespanImpl> timespans = (List<TimespanImpl>) fetchEntity(
	// TimespanImpl.class, fullBean);
	// TimespanImpl timespan = timespans.size() > 0 ? timespans.get(0)
	// : new TimespanImpl();
	// Map<String, List<String>> map = timespan.getIsPartOf() != null ? timespan
	// .getIsPartOf() : new HashMap<String, List<String>>();
	// List<String> val = map.get("def") != null ? map.get("def")
	// : new ArrayList<String>();
	// val.add(value);
	// map.put("def", val);
	// timespan.setIsPartOf(map);
	// if (timespans.size() > 0) {
	// timespans.set(0, timespan);
	// } else {
	// timespans.add(timespan);
	// }
	// fullBean.setTimespans(timespans);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	//
	// },

	// ENRICHMENT_CONCEPT_BROADER_TERM("enrichment_concept_broader_term",
	// "cc_skos_broader", false) {
	//
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<ConceptImpl> concepts = (List<ConceptImpl>) fetchEntity(
	// ConceptImpl.class, fullBean);
	// ConceptImpl concept = concepts.size() > 0 ? concepts.get(0)
	// : new ConceptImpl();
	//
	// List<String> broader = concept.getBroader() != null ? new
	// ArrayList<String>(
	// Arrays.asList(concept.getBroader()))
	// : new ArrayList<String>();
	// broader.add(value);
	//
	// concept.setBroader(StringArrayUtils.toArray(broader));
	// if (concepts.size() > 0) {
	// concepts.set(0, concept);
	// } else {
	// concepts.add(concept);
	// }
	// fullBean.setConcepts(concepts);
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	//
	// },
	//
	// ENRICHMENT_CONCEPT_BROADER_LABEL("enrichment_concept_broader_label",
	// "cc_skos_broaderLabel", true) {
	//
	// @Override
	// public void setField(FullBean fullBean, String value)
	// throws EntityNotFoundException, SecurityException,
	// IllegalArgumentException, NoSuchMethodException,
	// IllegalAccessException, InvocationTargetException {
	// List<ConceptImpl> concepts = (List<ConceptImpl>) fetchEntity(
	// ConceptImpl.class, fullBean);
	// ConceptImpl concept = concepts.size() > 0 ? concepts.get(0)
	// : new ConceptImpl();
	// List<String> broader = concept.getBroader() != null ? new
	// ArrayList<String>(
	// Arrays.asList(concept.getBroader()))
	// : new ArrayList<String>();
	// broader.add(value);
	// concept.setBroader(StringArrayUtils.toArray(broader));
	// if (concepts.size() > 0) {
	// concepts.set(0, concept);
	// } else {
	// concepts.add(concept);
	// }
	// fullBean.setConcepts(concepts);
	//
	// List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
	// ProxyImpl.class, fullBean);
	// ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
	// europeanaProxy = addField(europeanaProxy,
	// europeanaProxy.getEdmHasMet(), "EdmHasMet", value);
	//
	// proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
	// }
	//
	// },
	EUROPEANA_YEAR("europeana_year", "proxy_edm_year", true, true) {

		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException, SecurityException,
				IllegalArgumentException, NoSuchMethodException,
				IllegalAccessException, InvocationTargetException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl europeanaProxy = getEuropeanaProxy(proxies);
			Map<String, List<String>> map = europeanaProxy.getYear() != null ? europeanaProxy
					.getYear() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			europeanaProxy.setYear(map);
			proxies.set(getEuropeanaProxyIndex(proxies), europeanaProxy);
			fullBean.setYear(val.toArray(new String[val.size()]));
		}

	},
	EUROPEANA_LANGUAGE("europeana_language",
			"europeana_aggregation_edm_language", true, false) {

		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException, SecurityException,
				IllegalArgumentException, NoSuchMethodException,
				IllegalAccessException, InvocationTargetException {
			EuropeanaAggregation europeanaAggregation = fullBean
					.getEuropeanaAggregation();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			List<String> val = europeanaAggregation.getEdmLanguage() != null ? europeanaAggregation
					.getEdmLanguage().get("def") : new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			europeanaAggregation.setEdmLanguage(map);
			fullBean.setEuropeanaAggregation(europeanaAggregation);
		}

	},
	EUROPEANA_COUNTRY("europeana_country", "europeana_aggregation_edm_country",
			true, false) {

		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException, SecurityException,
				IllegalArgumentException, NoSuchMethodException,
				IllegalAccessException, InvocationTargetException {
			EuropeanaAggregation europeanaAggregation = fullBean
					.getEuropeanaAggregation();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			List<String> val = europeanaAggregation.getEdmCountry() != null ? europeanaAggregation
					.getEdmCountry().get("def") : new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			europeanaAggregation.setEdmCountry(map);
			fullBean.setEuropeanaAggregation(europeanaAggregation);
		}

	},
	DCTERMS_HASPART("dcterms_hasPart", "proxy_dcterms_hasPart", true, false) {
		@Override
		public void setField(FullBean fullBean, String value)
				throws EntityNotFoundException {
			List<ProxyImpl> proxies = (List<ProxyImpl>) fetchEntity(
					ProxyImpl.class, fullBean);
			ProxyImpl proxy = getProxy(proxies);
			Map<String, List<String>> map = proxy.getDctermsHasPart() != null ? proxy
					.getDctermsHasPart() : new HashMap<String, List<String>>();
			List<String> val = map.get("def") != null ? map.get("def")
					: new ArrayList<String>();
			val.add(value);
			map.put("def", val);
			proxy.setDctermsHasPart(map);
			proxies.set(getProxyIndex(proxies), proxy);
			fullBean.setProxies(proxies);

		}
	};

	private String eseField;
	private String edmField;
	private boolean hasDynamicField;
	private boolean isEnrichmentField;
	
	FieldMapping(String eseField, String edmField, boolean hasDynamicField, boolean isEnrichmentField) {
		this.eseField = eseField;
		this.edmField = edmField;
		this.hasDynamicField = hasDynamicField;
		this.isEnrichmentField = isEnrichmentField;
	}

	public  FieldMapping isContainedIn(String eseField){
		for (FieldMapping fm: FieldMapping.values()){
			if (StringUtils.contains(eseField, fm.getEseField())){
				return fm;
			}
		}
		return null;
	}
	/**
	 * Add a String array to the EuropeanaProxy
	 * 
	 * @param europeanaProxy
	 * @param arr
	 * @param method
	 * @param value
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected ProxyImpl addField(ProxyImpl europeanaProxy, String[] arr,
			String method, String value) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (europeanaProxy == null) {
			europeanaProxy = new ProxyImpl();
			europeanaProxy.setEuropeanaProxy(true);
		}
		Class<String[]>[] str = new Class[1];
		str[0] = String[].class;
		Method method1 = europeanaProxy.getClass().getMethod("set" + method,
				str);
		method1.invoke(europeanaProxy, new Object[] { StringArrayUtils
				.addToArray(arr != null ? arr : new String[] {}, value) });
		return europeanaProxy;
	}

	/**
	 * Add a Map to the EuropeanaProxy
	 * 
	 * @param europeanaProxy
	 * @param map
	 * @param method
	 * @param value
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected ProxyImpl addField(ProxyImpl europeanaProxy,
			Map<String, List<String>> map, String method, String value)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if (europeanaProxy == null) {
			europeanaProxy = new ProxyImpl();
			europeanaProxy.setEuropeanaProxy(true);
		}
		@SuppressWarnings("rawtypes")
		Class<Map>[] str = new Class[1];
		str[0] = Map.class;
		Method method1 = europeanaProxy.getClass().getMethod("set" + method,
				str);
		Map<String, List<String>> tempMap = map != null ? map
				: new HashMap<String, List<String>>();

		method1.invoke(europeanaProxy, tempMap);
		return europeanaProxy;
	}

	/**
	 * Return the EuropeanaProxy
	 * 
	 * @param proxies
	 * @return
	 */
	protected ProxyImpl getEuropeanaProxy(List<ProxyImpl> proxies) {
		for (ProxyImpl proxy : proxies) {
			if (proxy.isEuropeanaProxy()) {
				return proxy;
			}
		}
		return null;
	}

	/**
	 * Return the location of the EuropeanaProxy in the List of Proxies
	 * 
	 * @param proxies
	 * @return
	 * @throws EntityNotFoundException
	 */
	protected int getEuropeanaProxyIndex(List<ProxyImpl> proxies)
			throws EntityNotFoundException {
		int i = 0;
		for (ProxyImpl proxy : proxies) {
			if (proxy.isEuropeanaProxy()) {
				return i;
			}
			i++;
		}
		throw new EntityNotFoundException("Europeana Proxy does not exist");
	}

	/**
	 * Return the first non-Europeana Proxy
	 * 
	 * @param proxies
	 * @return
	 */
	protected ProxyImpl getProxy(List<ProxyImpl> proxies) {
		for (ProxyImpl proxy : proxies) {
			if (!proxy.isEuropeanaProxy()) {
				return proxy;
			}
		}
		return null;
	}

	/**
	 * Return the index of the first non-Europeana Proxy in the List of Proxies
	 * 
	 * @param proxies
	 * @return
	 * @throws EntityNotFoundException
	 */
	protected int getProxyIndex(List<ProxyImpl> proxies)
			throws EntityNotFoundException {
		int i = 0;
		for (ProxyImpl proxy : proxies) {
			if (!proxy.isEuropeanaProxy()) {
				return i;
			}
			i++;
		}
		throw new EntityNotFoundException("Europeana Proxy does not exist");
	}

	/**
	 * Get the ESE field
	 * 
	 * @return
	 */
	public String getEseField() {
		return eseField;
	}

	/**
	 * Get the EDM field
	 * 
	 * @return
	 */
	public String getEdmField() {
		return edmField;
	}

	/**
	 * Check if that field is a Dynamic Field
	 * 
	 * @return
	 */
	public boolean getHasDynamicField() {
		return hasDynamicField;
	}

	public boolean isEnrichmentField(){
		return isEnrichmentField;
	}
	/**
	 * Set the value of the field to the FullBean
	 * 
	 * @param fullBean
	 * @param value
	 * @throws EntityNotFoundException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public abstract void setField(FullBean fullBean, String value)
			throws EntityNotFoundException, SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException;

	private static List<? extends AbstractEdmEntity> fetchEntity(
			Class<? extends AbstractEdmEntity> clazz, FullBean fullBean)
			throws EntityNotFoundException {
		if (StringUtils.equals(clazz.getCanonicalName(),
				ProxyImpl.class.getCanonicalName())) {
			return fullBean.getProxies() != null ? fullBean.getProxies()
					: new ArrayList<ProxyImpl>();
		} else if (StringUtils.equals(clazz.getCanonicalName(),
				AggregationImpl.class.getCanonicalName())) {
			return fullBean.getAggregations() != null ? fullBean
					.getAggregations() : new ArrayList<AggregationImpl>();
		} else if (StringUtils.equals(clazz.getCanonicalName(),
				ConceptImpl.class.getCanonicalName())) {
			return fullBean.getConcepts() != null ? fullBean.getConcepts()
					: new ArrayList<ConceptImpl>();
		} else if (StringUtils.equals(clazz.getCanonicalName(),
				TimespanImpl.class.getCanonicalName())) {
			return fullBean.getTimespans() != null ? fullBean.getTimespans()
					: new ArrayList<TimespanImpl>();
		} else if (StringUtils.equals(clazz.getCanonicalName(),
				PlaceImpl.class.getCanonicalName())) {
			return fullBean.getPlaces() != null ? fullBean.getPlaces()
					: new ArrayList<PlaceImpl>();
		} else if (StringUtils.equals(clazz.getCanonicalName(),
				AgentImpl.class.getCanonicalName())) {
			return fullBean.getAgents() != null ? fullBean.getAgents()
					: new ArrayList<AgentImpl>();
		} else {
			throw new EntityNotFoundException("Entity "
					+ clazz.getCanonicalName() + " not usable in this method.");
		}
	}

	/**
	 * Get the appropriate FieldMapping according to its field anme
	 * 
	 * @param fieldName
	 *            The field name to use
	 * @return
	 */
	public static FieldMapping getFieldMapping(String fieldName) {
		for (FieldMapping fieldMapping : FieldMapping.values()) {
			if (StringUtils.equals(fieldName, fieldMapping.getEseField())) {
				return fieldMapping;
			}
		}
		return null;
	}

	private static boolean webResourceExists(
			List<WebResourceImpl> webResources, String value) {
		for (WebResourceImpl wr : webResources) {
			if (StringUtils.equals(wr.getAbout(), value)) {
				return true;
			}
		}
		return false;
	}
	
	public static synchronized List<FieldMapping> getFieldMappings(){
		return new EnrichmentFieldMappings().getFieldMappings();
	}
	
	
	private static final class EnrichmentFieldMappings{
		static List<FieldMapping> listMappings;
		public EnrichmentFieldMappings(){
			if(listMappings==null){
				listMappings = new ArrayList<FieldMapping>();
				listMappings.add(DC_DATE);
				listMappings.add(DC_COVERAGE);
				listMappings.add(DC_CREATOR);
				listMappings.add(DC_TYPE);
				listMappings.add(DC_SUBJECT);
				listMappings.add(DC_CONTRIBUTOR);
				listMappings.add(EUROPEANA_YEAR);
				listMappings.add(DCTERMS_TEMPORAL);
				listMappings.add(DCTERMS_SPATIAL);
			}
		}
		
		public List<FieldMapping> getFieldMappings(){
			return listMappings;
		}
		
		
		
	}
}

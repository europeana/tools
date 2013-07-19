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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.definitions.solr.entity.Proxy;

/**
 * Enumeration to set the original fields from which enrichments were created.
 * This should correspond to the field rule pairs of Annocultor
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public enum OriginalField {

	PROXY_DC_DATE("proxy_dc_date") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDcDate(appendValue(proxy.getDcDate(), uri));
			return proxy;
		}
	},
	PROXY_DC_COVERAGE("proxy_dc_coverage") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDcCoverage(appendValue(proxy.getDcCoverage(), uri));
			return proxy;
		}
	},
	PROXY_DCTERMS_TEMPORAL("proxy_dcterms_temporal") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDctermsTemporal(appendValue(proxy.getDctermsTemporal(),
					uri));
			return proxy;
		}
	},
	PROXY_EDM_YEAR("proxy_edm_year") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setYear(appendValue(proxy.getYear(), uri));
			return proxy;
		}
	},
	PROXY_DCTERMS_SPATIAL("proxy_dcterms_spatial") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDctermsSpatial(appendValue(proxy.getDctermsSpatial(), uri));
			return proxy;
		}
	},
	PROXY_DC_TYPE("proxy_dc_type") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDcType(appendValue(proxy.getDcType(), uri));
			return proxy;
		}
	},
	PROXY_DC_SUBJECT("proxy_dc_subject") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDcSubject(appendValue(proxy.getDcSubject(), uri));
			return proxy;

		}
	},
	PROXY_DC_CREATOR("proxy_dc_creator") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDcCreator(appendValue(proxy.getDcCreator(), uri));
			return proxy;

		}
	},
	PROXY_DC_CONTRIBUTOR("proxy_dc_contributor") {
		@Override
		public Proxy appendField(Proxy proxy, String uri) {
			proxy.setDcContributor(appendValue(proxy.getDcContributor(), uri));
			return proxy;
		}
	};

	String originalField;

	private OriginalField(String originalField) {
		this.originalField = originalField;
	}

	public static OriginalField getOriginalField(String originalField) {
		if (!StringUtils.isEmpty(originalField)) {
			for (OriginalField orField : OriginalField.values()) {
				if (orField.originalField.equalsIgnoreCase(originalField)) {
					return orField;
				}
			}

			throw new IllegalArgumentException(originalField + " not found");
		}
		return null;
	}

	/**
	 * Append the appropriate URI of the contextual entity to the field that
	 * generated it
	 * 
	 * @param proxy
	 *            The proxy to append the object to
	 * @param uri
	 *            The URI to append
	 * @return The modified proxy
	 */
	public abstract Proxy appendField(Proxy proxy, String uri);

	private static Map<String, List<String>> appendValue(
			Map<String, List<String>> map, String value) {
		if (map == null) {
			map = new HashMap<String, List<String>>();
		}
		List<String> values = map.get("def");
		if (values == null) {
			values = new ArrayList<String>();
		}
		if (!values.contains(value)) {
			values.add(value);
		}
		map.put("def", values);
		return map;
	}
}

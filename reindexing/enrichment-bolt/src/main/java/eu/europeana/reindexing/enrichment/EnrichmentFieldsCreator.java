/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.enrichment;

import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.InputValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author ymamakis
 */
public class EnrichmentFieldsCreator {

    private final static Map<String, EntityClass> REFERENCEMAP = new HashMap<String, EntityClass>() {
        {
            put("proxy_dc_date",EntityClass.TIMESPAN);
            put("proxy_dc_coverage",EntityClass.PLACE);
            put("proxy_dcterms_temporal",EntityClass.TIMESPAN);
            put("proxy_edm_year",EntityClass.TIMESPAN);
            put("proxy_dcterms_spatial",EntityClass.PLACE);
            put("proxy_dc_type",EntityClass.CONCEPT);
            put("proxy_dc_subject",EntityClass.CONCEPT);
            put("proxy_dc_creator",EntityClass.AGENT);
            put("proxy_dc_contributor",EntityClass.AGENT);
        }
    ;

    };
    public static List<InputValue> extractEnrichmentFieldsFromProxy(ProxyImpl proxy) {
        List<InputValue> toBeEnriched = new ArrayList<>();
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dc_date", proxy.getDcDate()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dc_coverage", proxy.getDcCoverage()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dcterms_temporal", proxy.getDctermsTemporal()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_edm_year", proxy.getYear()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dcterms_spatial", proxy.getDctermsSpatial()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dc_type", proxy.getDcType()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dc_subject", proxy.getDcSubject()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dc_creator", proxy.getDcCreator()));
        toBeEnriched.addAll(extractFieldsFromMap("proxy_dc_contributor", proxy.getDcContributor()));
        return toBeEnriched;
    }

    private static List<InputValue> extractFieldsFromMap(String originalField, Map<String, List<String>> map) {
        if (map != null) {
            List<InputValue> valuesToEnrich= new ArrayList<>();
            for(Entry<String,List<String>> entry:map.entrySet()){
                if(entry.getValue()!=null){
                    for(String str:entry.getValue()){
                        InputValue val = new InputValue();
                        val.setOriginalField(originalField);
                        List<EntityClass> entityVoc = new ArrayList<>();
                        entityVoc.add(REFERENCEMAP.get(originalField));
                        val.setVocabularies(entityVoc);
                        val.setValue(str);
                        valuesToEnrich.add(val);
                    }
                }
            }
            return valuesToEnrich;
        }
        return new ArrayList<>();
    }
}

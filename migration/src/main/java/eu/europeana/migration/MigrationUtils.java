package eu.europeana.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.*;
import eu.europeana.enrichment.api.external.EntityWrapper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by ymamakis on 4/5/16.
 */
public class MigrationUtils {

  private ObjectMapper mapper = new ObjectMapper();

  public ProxyImpl cleanEuropeanaProxy(ProxyImpl proxy) {
    proxy.setDcDate(new HashMap<>());
    proxy.setDctermsIssued(new HashMap<>());
    proxy.setDctermsCreated(new HashMap<>());
    proxy.setDctermsTemporal(new HashMap<>());
    proxy.setDcCoverage(new HashMap<>());
    proxy.setDctermsSpatial(new HashMap<>());
    proxy.setDcType(new HashMap<>());
    proxy.setDcSubject(new HashMap<>());
    proxy.setDcCreator(new HashMap<>());
    proxy.setDcContributor(new HashMap<>());
    if (proxy.getYear() != null) {
      Map<String, List<String>> newYears = new HashMap<>();
      Map<String, List<String>> year = proxy.getYear();
      for (Map.Entry<String, List<String>> years : year.entrySet()) {
        if (years.getValue() != null) {
          List<String> newYearList = new ArrayList<>();
          for (String y : years.getValue()) {
            if (!StringUtils.startsWith(y, "http:")) {
              newYearList.add(y);
            }
          }
          newYears.put(years.getKey(), newYearList);
        }
      }
      proxy.setYear(newYears);
    }

    return proxy;
  }


  public Set<String> extractAllUris(ProxyImpl proxy) {
    Set<String> uris = new HashSet<>();
    uris.addAll(extractStringsFromMap(proxy.getDcContributor()));
    uris.addAll(extractStringsFromMap(proxy.getDcType()));
    uris.addAll(extractStringsFromMap(proxy.getDcCoverage()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsConformsTo()));
    uris.addAll(extractStringsFromMap(proxy.getDcDate()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIsReferencedBy()));
    uris.addAll(extractStringsFromMap(proxy.getDcCreator()));
    uris.addAll(extractStringsFromMap(proxy.getDcDescription()));
    uris.addAll(extractStringsFromMap(proxy.getDcFormat()));
    uris.addAll(extractStringsFromMap(proxy.getDcIdentifier()));
    uris.addAll(extractStringsFromMap(proxy.getDcLanguage()));
    uris.addAll(extractStringsFromMap(proxy.getDcPublisher()));
    uris.addAll(extractStringsFromMap(proxy.getDcRelation()));
    uris.addAll(extractStringsFromMap(proxy.getDcRights()));
    uris.addAll(extractStringsFromMap(proxy.getDcSource()));
    uris.addAll(extractStringsFromMap(proxy.getDcSubject()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsAlternative()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsCreated()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsExtent()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsHasFormat()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsHasPart()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsHasVersion()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIsFormatOf()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIsPartOf()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIsReplacedBy()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsReferences()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIsReferencedBy()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIsRequiredBy()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIssued()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsIsVersionOf()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsMedium()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsProvenance()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsReplaces()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsRequires()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsSpatial()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsTemporal()));
    uris.addAll(extractStringsFromMap(proxy.getDctermsTOC()));
    uris.addAll(extractStringsFromMap(proxy.getDcTitle()));
    uris.addAll(extractStringsFromMap(proxy.getEdmHasMet()));
    uris.addAll(extractStringsFromMap(proxy.getEdmIsRelatedTo()));
    uris.add(proxy.getEdmIsRepresentationOf());
    if (proxy.getEdmIsSimilarTo() != null && proxy.getEdmIsSimilarTo().length > 0) {
      uris.addAll(Arrays.asList(proxy.getEdmIsSimilarTo()));
    }
    if (proxy.getEdmIsSuccessorOf() != null && proxy.getEdmIsSuccessorOf().length > 0) {
      uris.addAll(Arrays.asList(proxy.getEdmIsSuccessorOf()));
    }
    if (proxy.getEdmIncorporates() != null && proxy.getEdmIncorporates().length > 0) {
      uris.addAll(Arrays.asList(proxy.getEdmIncorporates()));
    }
    if (proxy.getEdmIsDerivativeOf() != null && proxy.getEdmIsDerivativeOf().length > 0) {
      uris.addAll(Arrays.asList(proxy.getEdmIsDerivativeOf()));
    }
    if (proxy.getEdmRealizes() != null && proxy.getEdmRealizes().length > 0) {
      uris.addAll(Arrays.asList(proxy.getEdmRealizes()));
    }
    if (proxy.getEdmWasPresentAt() != null && proxy.getEdmWasPresentAt().length > 0) {
      uris.addAll(Arrays.asList(proxy.getEdmWasPresentAt()));
    }
    return uris;
  }

  private Set<String> extractStringsFromMap(Map<String, List<String>> map) {
    Set<String> uris = new HashSet<>();
    if (map != null) {
      for (List<String> candidates : map.values()) {
        for (String str : candidates) {
          if (str.startsWith("http://")) {
            uris.add(str);
          }
        }
      }
    }
    return uris;
  }

  public List<LangValue> extractValuesFromProxy(ProxyImpl proxy) {
    List<LangValue> values = new ArrayList<>();
    if (proxy.getDcCoverage() != null) {
      values.addAll(addFromMap(proxy.getDcCoverage(), "PLACE", "proxy_dc_coverage"));
    }
    if (proxy.getDctermsSpatial() != null) {
      values.addAll(addFromMap(proxy.getDctermsSpatial(), "PLACE", "proxy_dcterms_spatial"));
    }
    if (proxy.getDcType() != null) {
      values.addAll(addFromMap(proxy.getDcType(), "CONCEPT", "proxy_dc_type"));
    }
    if (proxy.getDcSubject() != null) {
      values.addAll(addFromMap(proxy.getDcSubject(), "CONCEPT", "proxy_dc_subject"));
    }
    if (proxy.getDcCreator() != null) {
      values.addAll(addFromMap(proxy.getDcCreator(), "AGENT", "proxy_dc_creator"));
    }
    if (proxy.getDcContributor() != null) {
      values.addAll(addFromMap(proxy.getDcContributor(), "AGENT", "proxy_dc_contributor"));
    }
    //        if (proxy.getDcDate() != null) {
//            values.addAll(addFromMap(proxy.getDcDate(), "TIMESPAN", "proxy_dc_date"));
//        }
//        if (proxy.getDctermsIssued() != null) {
//            values.addAll(addFromMap(proxy.getDctermsIssued(), "TIMESPAN", "proxy_dcterms_issued"));
//        }
//        if (proxy.getDctermsCreated() != null) {
//            values.addAll(addFromMap(proxy.getDctermsCreated(), "TIMESPAN", "proxy_dcterms_created"));
//        }
//        if (proxy.getDctermsTemporal() != null) {
//            values.addAll(addFromMap(proxy.getDctermsTemporal(), "TIMESPAN", "proxy_dcterms_temporal"));
//        }
    return values;
  }

  public ProxyImpl addValueToProxy(EntityWrapper wrapper, ProxyImpl europeanaProxy) {
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dc_date")) {
      europeanaProxy.setDcDate(addToMap(europeanaProxy.getDcDate(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dcterms_issued")) {
      europeanaProxy
          .setDctermsIssued(addToMap(europeanaProxy.getDctermsIssued(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dcterms_created")) {
      europeanaProxy
          .setDctermsCreated(addToMap(europeanaProxy.getDctermsCreated(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dcterms_temporal")) {
      europeanaProxy
          .setDctermsTemporal(addToMap(europeanaProxy.getDctermsTemporal(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dc_coverage")) {
      europeanaProxy.setDcCoverage(addToMap(europeanaProxy.getDcCoverage(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dcterms_spatial")) {
      europeanaProxy
          .setDctermsSpatial(addToMap(europeanaProxy.getDctermsSpatial(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dc_type")) {
      europeanaProxy.setDcType(addToMap(europeanaProxy.getDcType(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dc_subject")) {
      europeanaProxy.setDcSubject(addToMap(europeanaProxy.getDcSubject(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dc_creator")) {
      europeanaProxy.setDcCreator(addToMap(europeanaProxy.getDcCreator(), wrapper.getUrl()));
    }
    if (StringUtils.equals(wrapper.getOriginalField(), "proxy_dc_contributor")) {
      europeanaProxy
          .setDcContributor(addToMap(europeanaProxy.getDcContributor(), wrapper.getUrl()));
    }
    return europeanaProxy;
  }

  public void addContextualClassToBean(FullBeanImpl fBean, EntityWrapper entityWrapper)
      throws IOException {
    switch (entityWrapper.getClassName()) {
      case "eu.europeana.corelib.solr.entity.ConceptImpl":
        List<ConceptImpl> concepts = fBean.getConcepts();
        if (concepts == null) {
          concepts = new ArrayList<>();
        }
        if (!entityExists(concepts, entityWrapper.getUrl())) {
          concepts.add(mapper.readValue(entityWrapper.getContextualEntity(), ConceptImpl.class));
        }
        if (concepts.size() > 0) {
          fBean.setConcepts(concepts);
        }
        break;
      case "eu.europeana.corelib.solr.entity.AgentImpl":
        List<AgentImpl> agents = fBean.getAgents();
        if (agents == null) {
          agents = new ArrayList<>();
        }
        if (!entityExists(agents, entityWrapper.getUrl())) {
          agents.add(mapper.readValue(entityWrapper.getContextualEntity(), AgentImpl.class));
        }
        if (agents.size() > 0) {
          fBean.setAgents(agents);
        }
        break;
      case "eu.europeana.corelib.solr.entity.PlaceImpl":
        List<PlaceImpl> places = fBean.getPlaces();
        if (places == null) {
          places = new ArrayList<>();
        }
        if (!entityExists(places, entityWrapper.getUrl())) {
          places.add(mapper.readValue(entityWrapper.getContextualEntity(), PlaceImpl.class));
        }
        if (places.size() > 0) {
          fBean.setPlaces(places);
        }
        break;
//      case "eu.europeana.corelib.solr.entity.TimespanImpl":
//        List<TimespanImpl> ts = fBean.getTimespans();
//        if (ts == null) {
//          ts = new ArrayList<>();
//        }
//        if (!entityExists(ts, entityWrapper.getUrl())) {
//          ts.add(mapper.readValue(entityWrapper.getContextualEntity(), TimespanImpl.class));
//        }
//        if (ts.size() > 0) {
//          fBean.setTimespans(ts);
//        }
//        break;
    }
  }

  private boolean entityExists(List<? extends ContextualClassImpl> list, String url) {
    if (list.size() > 0) {
      for (ContextualClassImpl cl : list) {
        if (StringUtils.equals(url, cl.getAbout())) {
          return true;
        }
      }
    }
    return false;
  }

  private Map<String, List<String>> addToMap(Map<String, List<String>> map, String val) {
    if (map == null) {
      map = new HashMap<>();
    }
    List<String> getDef = map.get("def");
    if (getDef == null) {
      getDef = new ArrayList<>();
    }
    if (!getDef.contains(val)) {
      getDef.add(val);
    }
    map.put("def", getDef);
    return map;
  }

  private List<LangValue> addFromMap(Map<String, List<String>> map, String vocabulary, String
      originalField) {
    List<LangValue> values = new ArrayList<>();
    if (map != null) {
      for (Map.Entry<String, List<String>> entry : map.entrySet()) {
        LangValue value = new LangValue();
        value.setValues(entry.getValue());
        value.setLanguage(entry.getKey());
        value.setVocabulary(vocabulary);
        value.setOriginalField(originalField);
        values.add(value);
      }
    }
    return values;
  }
}

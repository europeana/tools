package eu.europeana.metis.dao;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.harvester.database.DataManager;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author Roman Graf 
 * @since 2018-02-16
 */
@Configuration
@PropertySource("classpath:authentication.properties")
public class EnrichmentZohoAccessClientDao extends ZohoAccessClientDao {

	  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentZohoAccessClientDao.class);
	
	  private static final String LEADS_MODULE_STRING = "Leads";
	  private static final String GET_RECORDS_STRING = "getRecords";
	  private static final String AUTHENTICATION_TOKEN_STRING = "authtoken";
	  private static final String RESPONSE_STRING = "response";
	  private static final String RESULT_STRING = "result";
	  private static final String ROW_STRING = "row";
	  private static final String JSON_STRING = "json";
	  private static final String CRMAPI_STRING = "crmapi";
	  private static final String ORGANIZATION_NAME_FIELD = "Account Name";
	  private static final String VALUE_LABEL = "val";
	  private static final String CONTENT_LABEL = "content";
	  private static final String FIELDS_LABEL = "FL";
	  private static final String SCOPE_STRING = "scope";
	  private static final String ACCOUNTS_MODULE_STRING = "Accounts";
	  private static final String FROM_INDEX_STRING = "fromIndex";
	  private static final String TO_INDEX_STRING = "toIndex";
	
	  @Value("${zoho.base.url}")
	  private String zohoBaseUrl;
	  @Value("${zoho.authentication.token}")
	  private String zohoAuthenticationToken;	
	
      private final String ZOHO_BASE_URL = "https://crm.zoho.com/crm/private";
	  private final String ZOHO_AUTHENTICATION_TOKEN = "a0fa7bf7a12c292d209773f29d02e656";
	  
	  /**
	   * Constructor with required fields that will be used to access the Zoho service.
	   *
	   * @param zohoBaseUrl the remote url endpoint
	   * @param zohoAuthenticationToken the remote authentication token required to access its REST API
	   */
	  public EnrichmentZohoAccessClientDao(String zohoBaseUrl, String zohoAuthenticationToken) {
		  super(zohoBaseUrl, zohoAuthenticationToken);
	  }
	
	//  @Bean
	//  public EnrichmentZohoAccessClientDao() {
	//	  super(zohoBaseUrl, zohoAuthenticationToken);
	//  }
	
	  @Bean
	  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
	    return new PropertySourcesPlaceholderConfigurer();
	  }	
	    
	  /**
	   * Retrieve organizations using start and end index. <p>It will try to fetch the
	   * organizations from the external CRM. This method returns a list of organizations in json
	   * format.</p>
	   * 
	   * Example query:
	   * https://crm.zoho.com/crm/private/json/Accounts/getRecords?authtoken=<token>&scope=crmapi&fromIndex=1&toIndex=10
	   *
	   * @param start to start search from this index
	   * @param end to end search by this index
	   * @return the list of the organizations
	   * @throws GenericMetisException which can be one of:
	   * <ul>
	   * <li>{@link BadContentException} if any other problem occurred while constructing the user, like an
	   * organization did not have a role defined or the response cannot be converted to {@link JsonNode}</li>
	   * </ul>
	 * @throws IOException 
	   */
	  public JsonNode getOrganizations(String start, String end)
	      throws GenericMetisException, IOException {

		zohoBaseUrl = ZOHO_BASE_URL;
		zohoAuthenticationToken = ZOHO_AUTHENTICATION_TOKEN;
		  
	    String contactsSearchUrl = String
	        .format("%s/%s/%s/%s", zohoBaseUrl, JSON_STRING, ACCOUNTS_MODULE_STRING,
	        		GET_RECORDS_STRING);
	    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
	        .queryParam(AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
	        .queryParam(SCOPE_STRING, CRMAPI_STRING)
	        .queryParam(FROM_INDEX_STRING, start)
	    	.queryParam(TO_INDEX_STRING, end);
	
	    RestTemplate restTemplate = new RestTemplate();
	    String organisationsResponse = restTemplate
	        .getForObject(builder.build().encode().toUri(), String.class);
	    LOGGER.info(organisationsResponse);
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonRecordsResponse = mapper.readTree(organisationsResponse);
	    return findRecordsByType(jsonRecordsResponse, ACCOUNTS_MODULE_STRING);
	  }
	
	  /**
	   * This method retrieves a list of organisations in {@link JsonNode} format.
	   * @param jsonLeadsResponse
	   * @param type of records
	   * @return {@link JsonNode} representation of the leads
	   */
	  private JsonNode findRecordsByType(JsonNode jsonLeadsResponse, String type) {
		    if (jsonLeadsResponse.get(RESPONSE_STRING).get(RESULT_STRING) == null) {
		      return null;
		    }
		    return jsonLeadsResponse.get(RESPONSE_STRING).get(RESULT_STRING)
			        .get(type).get(ROW_STRING);
//	        .get(type).get(ROW_STRING).get(FIELDS_LABEL);
	  }
  
	  /**
	   * This method will try to fetch all the leads records in {@link JsonNode} format 
	   * from the external CRM.</p>
	   * Example: https://crm.zoho.com/crm/private/json/Leads/getRecords?authtoken=<token>&scope=crmapi
	   * 
	   * @return {@link JsonNode} representation of the leads
	   * @throws IOException if the response cannot be converted to {@link JsonNode}
	   * @throws BadContentException if the organization did not have a role defined
	   */
	  @Bean
	  public JsonNode getRecords()
	      throws IOException, BadContentException {
	    String leadsSearchUrl = String
	        .format("%s/%s/%s/%s", zohoBaseUrl, JSON_STRING, LEADS_MODULE_STRING,
	            GET_RECORDS_STRING);
	    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(leadsSearchUrl)
	        .queryParam(AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
	        .queryParam(SCOPE_STRING, CRMAPI_STRING);
	
	    RestTemplate restTemplate = new RestTemplate();
	    URI uri = builder.build().encode().toUri();
		String recordsResponse = restTemplate
	        .getForObject(uri, String.class);
	    LOGGER.info(recordsResponse);
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonRecordsResponse = mapper.readTree(recordsResponse);
	    return findRecordsByType(jsonRecordsResponse, LEADS_MODULE_STRING);
	  }

      /**
       * @param jsonOrgizationsResponse
       * @param organizationName
       * @return
       */
      protected JsonNode findOrganizationFromListOfJsonNodes(JsonNode jsonOrgizationsResponse,
	      String organizationName) {
	    Iterator<JsonNode> organizationJsonNodes = jsonOrgizationsResponse.get(RESPONSE_STRING)
	        .get(RESULT_STRING).get(ACCOUNTS_MODULE_STRING).get(ROW_STRING).elements();
	    if (organizationJsonNodes == null || !organizationJsonNodes.hasNext()) {
	      return null;
	    }
	    while (organizationJsonNodes.hasNext()) {
	      JsonNode nextOrganizationJsonNode = organizationJsonNodes.next().get(FIELDS_LABEL);
	      Iterator<JsonNode> organizationFields = nextOrganizationJsonNode.elements();
	      while (organizationFields.hasNext()) {
	        JsonNode organizationField = organizationFields.next();
	        JsonNode val = organizationField.get(VALUE_LABEL);
	        JsonNode content = organizationField.get(CONTENT_LABEL);
	        if (val.textValue().equals(ORGANIZATION_NAME_FIELD) && content.textValue()
	            .equals(organizationName)) {
	          return nextOrganizationJsonNode;
	        }
	      }
	    }
	    return null;
	  }

      /**
       * This method retrieves map of records.
       * @param jsonRecordsResponse
       * @return map representation of the records
       */
      protected Map<String,String> getRecordsMapFromListOfJsonNodes(JsonNode jsonRecordsResponse) {
    		Map<String,String> res = new HashMap<String,String>();
  	    Iterator<JsonNode> recordsJsonNodes = jsonRecordsResponse.elements();
  	    if (recordsJsonNodes == null || !recordsJsonNodes.hasNext()) {
  	      return null;
  	    }
  	    while (recordsJsonNodes.hasNext()) {
  	      JsonNode nextRecordsJsonNode = recordsJsonNodes.next();
            JsonNode val = nextRecordsJsonNode.get(VALUE_LABEL);
            JsonNode content = nextRecordsJsonNode.get(CONTENT_LABEL);
            res.put(val.textValue(), content.textValue());
  	    }
  	    return res;
  	  }

  	  /**
  	   * This method retrieves map of records.
  	   * @param jsonRecordsResponse
       * @return map representation of the records
  	   */
  	  protected Map<String,String> getOrganizationsMapFromListOfJsonNodes(JsonNode jsonRecordsResponse) {
  	        DataManager dm = new DataManager();
  		  
	  		Map<String,String> res = new HashMap<String,String>();
		    Iterator<JsonNode> recordsJsonNodes = jsonRecordsResponse.elements();
		    if (recordsJsonNodes == null || !recordsJsonNodes.hasNext()) {
		      return null;
		    }
		    while (recordsJsonNodes.hasNext()) {
			      JsonNode nextOrganizationJsonNode = recordsJsonNodes.next().get(FIELDS_LABEL);
			      Iterator<JsonNode> organizationFields = nextOrganizationJsonNode.elements();
			      OrganizationImpl organization = new OrganizationImpl();
			      while (organizationFields.hasNext()) {
				      JsonNode nextRecordsJsonNode = organizationFields.next();
			          JsonNode val = nextRecordsJsonNode.get(VALUE_LABEL);
			          JsonNode content = nextRecordsJsonNode.get(CONTENT_LABEL);
			          res.put(val.textValue(), content.textValue());
				  }
			      dm.insertOrganization(organization);
		    }
		    return res;
	  }

}
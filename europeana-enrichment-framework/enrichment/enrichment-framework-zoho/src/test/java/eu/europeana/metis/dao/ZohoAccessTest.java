package eu.europeana.metis.dao;

//import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.fasterxml.jackson.databind.JsonNode;

import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.harvester.database.DataManager;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import junit.framework.Assert;

/**
 * Unit tests for Zoho access 
 * @author roman.graf@ait.ac.at
 *
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis"})
@PropertySource("classpath:authentication.properties")
public class ZohoAccessTest { 

	private EnrichmentZohoAccessClientDao zohoClient = null;
	
	@Value("${zoho.base.url}")
	private String zohoBaseUrl;
	@Value("${zoho.authentication.token}")
	private String zohoAuthenticationToken;	
	private final String TEST_ORGANIZATION_NAME = "Memini"; //"Media Deals"; 
	private final String TEST_ORGANIZATION_ID = "1482250000005593027";
	private final String COMPANY_FIELD = "Company"; 
	private final String ZOHO_BASE_URL = "https://crm.zoho.com/crm/private";
	private final String ZOHO_AUTHENTICATION_TOKEN = "a0fa7bf7a12c292d209773f29d02e656";
	private final String START_INDEX = "1"; 
	private final String END_INDEX = "10"; 
	
	/** Test fields from Zoho */
//	Data provider/false
//	MODIFIEDBY/1482250000001209007
//	Account Owner/Tamara van Hulst
//	Image service Opt-in/false
//	DEA Sent/false
//	SMOWNERID/1482250000001209007
//	Modified Time/2018-01-30 10:19:50
//	Created Time/2018-01-30 10:19:50
//	Modified By/Tamara van Hulst
//	Account Name/Restnova, Art Solutions
//	ACCOUNTID/1482250000005386671
//	Country/Spain
//	Last Activity Time/2018-01-30 10:19:50
//	Data Partner/false
//	Domain/Creative industries
//	DEA Signed/false
	
//    private final DataManager dm = new DataManager();
	
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
	    return new PropertySourcesPlaceholderConfigurer();
	}	
	
	/**
	 * Initialization of the Zoho client
	 */
	@Before
	@Bean
	public void mockUp() {
//		zohoClient = new EnrichmentZohoAccessClientDao(zohoBaseUrl, zohoAuthenticationToken);
		zohoClient = new EnrichmentZohoAccessClientDao(ZOHO_BASE_URL, ZOHO_AUTHENTICATION_TOKEN);
	}

	/**
	 * Retrieval of organizations by start and end indexes test
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws GenericMetisException 
	 */
	@Test
	public void testStoreZohoOrganizationInDB() throws IOException, ParseException, GenericMetisException {
		
		DataManager dm = new DataManager();
		
		JsonNode organizationsJson = zohoClient.getOrganizations(START_INDEX, END_INDEX);
		List<OrganizationImpl> organizationsList = zohoClient.getOrganizationsListFromListOfJsonNodes(organizationsJson);
		OrganizationImpl organization = organizationsList.get(0);
		dm.insertOrganization(organization);
		Assert.assertTrue(organizationsList.size() > 0);
	}
	
	/**
	 * Retrieval of organizations by start and end indexes test
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws GenericMetisException 
	 */
	@Test
	@Ignore
	public void testGetOrganizationsFromZohoForIndexes() throws IOException, ParseException, GenericMetisException {
		JsonNode organizationsJson = zohoClient.getOrganizations(START_INDEX, END_INDEX);
		Map<String,String> organizationsMap = zohoClient.getOrganizationsMapFromListOfJsonNodes(organizationsJson);
		
		for (Map.Entry<String, String> entry : organizationsMap.entrySet()) {
		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}		
		Assert.assertTrue(organizationsMap.size() > 0);
	}
	
	/**
	 * Get organization from Zoho by name test
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws GenericMetisException 
	 */
	@Test
	@Ignore
	public void testGetOrganizationFromZohoByName() throws IOException, ParseException, GenericMetisException {
		String organizationId = zohoClient.getOrganizationIdByOrganizationName(TEST_ORGANIZATION_NAME);
		Assert.assertEquals(organizationId, TEST_ORGANIZATION_ID);
	}
	
	/**
	 * All leads retrieval test
	 * @throws BadContentException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@Test
//	@Bean
	@Ignore
	public void testGetAllLeadsFromZoho() throws IOException, BadContentException, ParseException {
//		zohoClient = new EnrichmentZohoAccessClientDao(zohoBaseUrl, zohoAuthenticationToken);
		JsonNode leadsJson = zohoClient.getRecords();
		Map<String,String> leadsMap = zohoClient.getRecordsMapFromListOfJsonNodes(leadsJson);
		
		for (Map.Entry<String, String> entry : leadsMap.entrySet()) {
		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}		
		Assert.assertTrue(leadsMap.size() > 0);
	}
	
	/**
	 * Organization retrieval test
	 * @throws BadContentException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@Test
	@Ignore
	public void testGetOrganizationFromZoho() throws IOException, BadContentException, ParseException {
		JsonNode leadsJson = zohoClient.getRecords();
		Map<String,String> leadsMap = zohoClient.getRecordsMapFromListOfJsonNodes(leadsJson);
		String organizationName = leadsMap.get(COMPANY_FIELD);
		Assert.assertEquals(TEST_ORGANIZATION_NAME, organizationName);
	}
}

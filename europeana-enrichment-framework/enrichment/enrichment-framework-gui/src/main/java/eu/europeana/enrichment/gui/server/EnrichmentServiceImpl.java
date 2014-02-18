package eu.europeana.enrichment.gui.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.gui.client.EnrichmentService;
import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;

public class EnrichmentServiceImpl extends RemoteServiceServlet implements EnrichmentService {
	Logger log = Logger.getLogger(this.getClass());
	EnrichmentDriver driver = new EnrichmentDriver();

	@Override
	public List<EntityWrapperDTO> enrich(List<InputValueDTO> values, boolean toEdm) {
		List<InputValue> inputValues = new ArrayList<InputValue>();
		for (InputValueDTO value : values) {
			InputValue inputValue = new InputValue();
			if (value.getOriginalField() != null) {
				inputValue.setOriginalField(value.getOriginalField());
			}
			inputValue.setValue(value.getValue());
			List<EntityClass> classes = new ArrayList<EntityClass>();
			classes.add(EntityClass.valueOf(value.getVocabulary()));
			inputValue.setVocabularies(classes);
			inputValues.add(inputValue);
		}

		try {
			List<EntityWrapper> reply = driver
					.enrich("http://localhost:8282/enrichment-framework-rest-0.1-SNAPSHOT/enrich/",
							inputValues, toEdm);
			
			List<EntityWrapperDTO> replyDTO = new ArrayList<EntityWrapperDTO>();
			for (EntityWrapper entity : reply) {
				EntityWrapperDTO entityDTO = new EntityWrapperDTO();
				entityDTO.setClassName(entity.getClassName());
				if(!toEdm){
				entityDTO.setContextualEntity(entity.getContextualEntity());
				} else {
					entityDTO.setContextualEntity(StringEscapeUtils.unescapeXml(entity.getContextualEntity()));
				}
				if(entity.getOriginalField()!=null){
					entityDTO.setOriginalField(entity.getOriginalField());
				} else {
					entityDTO.setOriginalField("");
				}
				replyDTO.add(entityDTO);
			}
			
				return replyDTO;
			
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}


}

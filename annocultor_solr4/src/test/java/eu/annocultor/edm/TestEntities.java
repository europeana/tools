package eu.annocultor.edm;

import java.util.List;
import java.util.Map.Entry;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import eu.annocultor.converters.europeana.Entity;
import eu.annocultor.converters.europeana.Field;
import eu.annocultor.converters.solr.BuiltinSolrDocumentTagger;

public class TestEntities {

	@Test
	public void testEntities(){
		SolrInputDocument solrDocument = new SolrInputDocument();
		solrDocument.addField("proxy_dc_creator", "Raphael");
		solrDocument.addField("proxy_dc_coverage","paris");
		solrDocument.addField("proxy_dc_date", "1880");
		solrDocument.addField("proxy_dc_subject", "paper");
		BuiltinSolrDocumentTagger tagger = new BuiltinSolrDocumentTagger() {
			
			@Override
			public boolean shouldReplicateThisField(String fieldName) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void preProcess(SolrInputDocument document, String id) {
				// TODO Auto-generated method stub
				
			}
		};
		
		try {
			tagger.init("Europeana");
			
			List<Entity> entities = tagger.tagDocument(solrDocument);
			for(Entity entity:entities){
				System.out.println("Class name: "+entity.getClassName());
				System.out.println("Original field: "+entity.getOriginalField());
				System.out.println("URI: " + entity.getUri());
				for(Field field: entity.getFields()){
					System.out.println("Field name: "+field.getName());
					for(Entry<String,List<String>> fieldValues: field.getValues().entrySet()){
						for(String value:fieldValues.getValue()){
							System.out.println("Field values: "+  fieldValues.getKey() + "-"+ value);
						}
					}
				}
				
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

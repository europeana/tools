package eu.europeana.enrichment.debug;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import eu.europeana.enrichment.converters.solr.BuiltinSolrDocumentTagger;
import eu.europeana.enrichment.model.external.EntityWrapper;

public class DebuggingUtils {

	public static void main(String... args){
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
			
			List<String> dcSubject = new ArrayList<String>();
			dcSubject.add("music");
			dcSubject.add("ivory");
			dcSubject.add("steel");
		
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("proxy_dc_subject", dcSubject);
			doc.addField("proxy_dcterms_spatial","Paris");
			doc.addField("proxy_dc_date","1918");
			doc.addField("proxy_dc_creator","Rembrandt");
			List<EntityWrapper> entity = tagger.tagDocument(doc);
			System.out.println(entity.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

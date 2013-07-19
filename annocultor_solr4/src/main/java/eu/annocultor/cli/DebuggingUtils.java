package eu.annocultor.cli;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.converters.europeana.Entity;
import eu.annocultor.converters.solr.BuiltinSolrDocumentTagger;

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
			List<Entity> entity = tagger.tagDocument(doc);
			System.out.println(entity.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

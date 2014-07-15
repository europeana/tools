package eu.europeana.datamigration.ese2edm;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import eu.annocultor.converters.europeana.Entity;
import eu.europeana.datamigration.ese2edm.enrichment.EuropeanaTagger;

public class EnrichmentTest {

	public static void main(String[] args) {

		EuropeanaTagger tagger = new EuropeanaTagger();
		try {
			tagger.init("Europeana");
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("proxy_dc_subject", "Photography");
			doc.addField("proxy_dc_subject", "World War I");
			List<Entity> entities = tagger.tagDocument(doc);
			for (Entity entity : entities) {
				System.out.println(entity.getFields().get(0).getValues().get("def").get(0));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

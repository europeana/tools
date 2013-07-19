package eu.europeana.datamigration.ese2edm.converters.generic;

import java.util.Date;
import java.util.logging.Level;

import org.apache.solr.common.SolrInputDocument;

import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.tools.utils.EuropeanaUriUtils;
import eu.europeana.datamigration.ese2edm.enums.FieldMapping;

public class EuropeanaIdGenerator {



	public String saveNewEuropeanaId(
				EuropeanaIdMongoServer europeanaIdMongoServer,
				SolrInputDocument inputDocument, String uri, String hash,
				String newCollectionId) {
			try {
				EuropeanaId europeanaId = new EuropeanaId();
				europeanaId.setOldId(uri);
				europeanaId.setNewId(EuropeanaUriUtils.createEuropeanaId(
						newCollectionId, hash));
				europeanaId.setTimestamp(new Date().getTime());
				if (!europeanaIdMongoServer.oldIdExists(europeanaId.getNewId())) {
					europeanaIdMongoServer.saveEuropeanaId(europeanaId);
				}
				inputDocument.addField(FieldMapping.EUROPEANA_URI.getEdmField(),
						"/" + newCollectionId + "/" + hash);
				return europeanaId.getNewId();
			} catch (Exception e) {
				//log.log(Level.SEVERE, "Record " + uri + " was encountered twice.\n");
				return uri;
			}
		}
	
	
}

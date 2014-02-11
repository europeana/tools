package eu.europeana.enrichment.model.internal;

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.Id;

public class MongoTerm {

	@Id
	public String id;
	public String codeUri;
	public String label;
	public String originalLabel;
	public String lang;
	public DBRef<? extends MongoTerm, String> parent;
	
}

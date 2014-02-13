package eu.europeana.enrichment.model.internal;

import java.util.List;

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.ObjectId;

import org.codehaus.jackson.annotate.JsonProperty;

import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;

public abstract class MongoTermList<T extends AbstractEdmEntityImpl> {

	private String parent;
	private String codeUri;
	private List<DBRef<? extends MongoTerm,String>> terms;
	private String id;
	protected T representation;
	private String entityType;
	@ObjectId
	  @JsonProperty("_id")
	public String getId() {
		return id;
	}
	@ObjectId
	  @JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}
	public String getCodeUri() {
		return codeUri;
	}
	public void setCodeUri(String codeUri) {
		this.codeUri = codeUri;
	}
	public List<DBRef<? extends MongoTerm, String>> getTerms() {
		return terms;
	}
	public void setTerms(List<DBRef<? extends MongoTerm, String>> terms) {
		this.terms = terms;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public abstract T getRepresentation();

	public abstract void setRepresentation(T representation);
	
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	
	
}

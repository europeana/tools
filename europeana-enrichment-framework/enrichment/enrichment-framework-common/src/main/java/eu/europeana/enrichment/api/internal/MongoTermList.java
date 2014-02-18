package eu.europeana.enrichment.api.internal;

import java.util.List;

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.ObjectId;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;

/**
 * Basic Class linking a number of MonoTerms. This class enables searching by
 * CodeUri for fetching all the relevant MongoTerms while it includes the parent
 * term (skos:broader, dcterms:isPartOf), the className of the entityType for
 * deserialization and a JSON representation of the contextual class
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 * @param <T> AgentImpl, PlaceImpl, ConceptImpl, TimespanImpl
 */

public abstract class MongoTermList<T extends AbstractEdmEntityImpl> {

	private String parent;
	private String codeUri;
	private List<DBRef<? extends MongoTerm, String>> terms;
	private String id;
	protected T representation;
	private String entityType;

	@ObjectId
	@JsonProperty("_id")
	@JsonSerialize(using = ObjectIdSerializer.class)
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

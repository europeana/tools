package eu.europeana.enrichment.model.internal;

import eu.europeana.corelib.solr.entity.ConceptImpl;

public class ConceptTermList extends MongoTermList<ConceptImpl> {

	@Override
	public ConceptImpl getRepresentation() {
		return representation;
	}

	@Override
	public void setRepresentation(ConceptImpl representation) {
		this.representation = representation;
	}

}

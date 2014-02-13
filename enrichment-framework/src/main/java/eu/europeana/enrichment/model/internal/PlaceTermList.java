package eu.europeana.enrichment.model.internal;

import eu.europeana.corelib.solr.entity.PlaceImpl;

public class PlaceTermList extends MongoTermList<PlaceImpl> {

	@Override
	public PlaceImpl getRepresentation() {
		return representation;
	}

	@Override
	public void setRepresentation(PlaceImpl representation) {
		this.representation = representation;
	}
	
}

package eu.europeana.enrichment.api.internal;

import eu.europeana.corelib.solr.entity.TimespanImpl;

public class TimespanTermList extends MongoTermList<TimespanImpl> {

	@Override
	public TimespanImpl getRepresentation() {
		return representation;
	}

	@Override
	public void setRepresentation(TimespanImpl representation) {
		this.representation = representation;
	}

}

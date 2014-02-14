package eu.europeana.enrichment.api.internal;

import eu.europeana.corelib.solr.entity.AgentImpl;

public class AgentTermList extends MongoTermList<AgentImpl> {

	@Override
	public AgentImpl getRepresentation() {
		return representation;
	}

	@Override
	public void setRepresentation(AgentImpl representation) {
		this.representation = representation;
	}

}

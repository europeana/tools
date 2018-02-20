package eu.europeana.enrichment.api.internal;

import eu.europeana.corelib.solr.entity.OrganizationImpl;

/**
 * OrganizationImpl specific MongoTermList
 * @author Roman Graf
 *
 */
public class OrganizationTermList extends MongoTermList<OrganizationImpl> {

	@Override
	public OrganizationImpl getRepresentation() {
		return representation;
	}

	@Override
	public void setRepresentation(OrganizationImpl representation) {
		this.representation = representation;
	}

}

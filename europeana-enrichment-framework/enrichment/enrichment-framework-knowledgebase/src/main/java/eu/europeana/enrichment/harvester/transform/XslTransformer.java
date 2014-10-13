package eu.europeana.enrichment.harvester.transform;

import javax.xml.transform.Source;

import eu.europeana.corelib.definitions.solr.entity.ContextualClass;

public interface XslTransformer<T extends ContextualClass> {
	
	public abstract T transform(String xsltPath, String resourceUri, Source doc);
}

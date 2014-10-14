package eu.europeana.enrichment.harvester.transform.edm.agent;

import java.io.File;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


import eu.europeana.corelib.definitions.solr.entity.Agent;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.harvester.transform.XslTransformer;

public class AgentTransformer implements XslTransformer<Agent> {
	static final Logger log = Logger.getLogger(XslTransformer.class.getCanonicalName());
	@Override
	public AgentImpl transform(String xsltPath, String resourceUri, Source doc) {
		StreamSource transformDoc = new StreamSource(new File(xsltPath));
		
		try {
			Transformer transformer = TransformerFactory
					.newInstance().newTransformer(transformDoc);
			StreamResult out = new StreamResult(new StringWriter());
			transformer.transform(doc, out);
			return AgentTemplate.getInstance().transform(out.getWriter().toString(),resourceUri);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			log.log(Level.SEVERE, e.getMessage());
			
		} 

		return null;
	}

}

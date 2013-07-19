package eu.europeana.normalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.thoughtworks.xstream.XStream;

import eu.europeana.normalizer.Profile.FieldMapping;
import eu.europeana.normalizer.Profile.MapTo;
import eu.europeana.normalizer.Profile.RecordAddition;
import eu.europeana.normalizer.Profile.Source;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 * 
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AnnoCultorProfileExporter {

	Document doc ;
	String ns = "";
	String xsi = "";

	private Element addLeaveElement(Element parent, String name) {
		return addLeaveElement(parent, name, null);
	}

	private Element addLeaveElement(Element parent, String name, String value) {
		Element element = doc.createElementNS(ns, "ann:file");
		element.appendChild(element);
		if (value != null) 
			element.setTextContent(value);
		return element;
	}

	private Element addTypedLeaveElement(Element parent, String name, String value, String type) {
		Element element = addLeaveElement(parent, name, value);
		element.setAttributeNS(xsi, "xsi:type", type);
		return element;
	}
	
	public void exportToAnnoCultor(File inputFile, File outputFile) 
	throws Exception {
		XStream stream = new XStream();
		stream.processAnnotations(Profile.class);
		Profile p = (Profile) stream.fromXML(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();

		Element profile = doc.createElementNS(ns, "ann:profile");
		Element namespaces = doc.createElementNS(ns, "ann:namespaces");
		Element environment = doc.createElementNS(ns, "ann:environment");
		Element vocabularies = doc.createElementNS(ns, "ann:vocabularies");
		Element destinations = doc.createElementNS(ns, "ann:destinations");
		Element sources = doc.createElementNS(ns, "ann:sources");
		profile.appendChild(namespaces);
		profile.appendChild(environment);
		profile.appendChild(vocabularies);
		profile.appendChild(destinations);
		profile.appendChild(sources);

		// profile
		profile.setAttributeNS(ns, "ann:id", p.name);
		profile.setAttributeNS(ns, "ann:directory", "UNSPECIFIED");
		profile.setAttributeNS(ns, "ann:institution", "UNSPECIFIED");
		profile.setAttributeNS(ns, "ann:publisherId", "UNSPECIFIED");

		// environment

		// destinations
		String defaultGraph = "all";
		// sources
		for (Source pSource : p.sources) {
			Element source = addLeaveElement(sources, "ann:source");
			source.setAttributeNS(ns, "ann:id", pSource.collectionId);

			// datasource
			Element dataSource = addLeaveElement(source, "ann:datasource");
			Element xml = addLeaveElement(dataSource, "ann:xml");
			Element file = addLeaveElement(xml, "ann:file", pSource.file);

			// object rule
			Element objectRule = addLeaveElement(source, "ann:objectRule");
			Element selector = addLeaveElement(objectRule, "ann:selector");
			addLeaveElement(selector, "ann:recordSeparator", pSource.recordSeparator);
			addLeaveElement(selector, "ann:recordNamespace", "XXX");
			addLeaveElement(selector, "ann:recordIdentifier", "XXX");
			addLeaveElement(selector, "ann:recordInformalIdentifier", "XXX");

			// additions
			for (RecordAddition pAdd : pSource.additions)	{
				Element propertyRule = addTypedLeaveElement(objectRule, "ann:propertyRule", null, "eu.annocultor.rules.CreateResourcePropertyRule");
				Element sourceXmlPath = addTypedLeaveElement(propertyRule, "ann:sourceXmlPath", "XXX", "XmlPath");
				Element targetRdfProperty = addTypedLeaveElement(propertyRule, "ann:targetRdfProperty", pAdd.key.toString(), "RdfProperty");
				Element targetRdfResourceValue = addTypedLeaveElement(propertyRule, "ann:targetRdfResourceValue", pAdd.value, "String");
				Element graph = addTypedLeaveElement(propertyRule, "ann:graph", defaultGraph, "Graph");
			}

			// property rules
			for (FieldMapping pFieldMapping : pSource.fieldMappings) {
				for (MapTo to : pFieldMapping.mapTo) {
					Element propertyRule = addTypedLeaveElement(objectRule, "ann:propertyRule", null, "eu.annocultor.rules.RenameLiteralPropertyRule");
					Element sourceXmlPath = addTypedLeaveElement(propertyRule, "ann:sourceXmlPath", pFieldMapping.from, "XmlPath");
					Element targetRdfProperty = addTypedLeaveElement(propertyRule, "ann:targetRdfProperty", to.toString(), "RdfProperty");
					Element languageCodeToEnforce = addTypedLeaveElement(propertyRule, "ann:languageCodeToEnforce", "XXX", "String");
					Element graph = addTypedLeaveElement(propertyRule, "ann:graph", defaultGraph, "Graph");					
				}
			}

		}

		XMLSerializer serializer = new XMLSerializer();
		serializer.setOutputCharStream( new FileWriter(outputFile));
		serializer.serialize(doc);
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: <source-file> <destination-file>");
			return;
		}
	}
}

/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.xconverter.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.europeana.enrichment.common.Utils;
import eu.europeana.enrichment.context.Namespaces;


public class XConverter2Java {

	private final class ProfileEntityResolver implements EntityResolver {
		private File profileDir;
		
		public ProfileEntityResolver(File profileDir) {
			super();
			this.profileDir = profileDir;
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId)
		throws SAXException, IOException 
		{
			if (systemId != null && systemId.endsWith(".xml"))
			{
				/*
				 * Feature: resolve against profile directory.
				 * Else VM tries to resolve it itself, making
				 * a nice mess of current dir, profile dir, etc.
				 */
				systemId = profileDir.getCanonicalPath() + systemId.substring(systemId.indexOf(":") + 1);
				//+ systemId.substring(systemId.lastIndexOf("/"));
				//System.out.println("Entity resolver: " + new URL(systemId));
				return new InputSource(
						new StringReader(
								Utils.loadFileToString(systemId, "\n")));
			}
			return null;
		}
	}

	public static final String GENERATED_CONVERTER_CLASS_NAME = "GeneratedConverter";

	/*	public static Document parseXmlFile(String filename, boolean validating) {
    try {
        // Create a builder factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validating);

        // Create the builder and parse the file
        Document doc = factory.newDocumentBuilder().parse(new File(filename));
        return doc;
    } catch (SAXException e) {
        // A parsing error occurred; the xml input is not valid
    } catch (ParserConfigurationException e) {
    } catch (IOException e) {
    }
    return null;
}
	 */
	public VelocityContext run(
			File profileDir,
			InputStream sourceXml, 
			String templateFileNameOnClasspath, 
			OutputStream targetJava,
			File workDir)
	throws Exception
	{
		if (sourceXml == null)
		{
			throw new NullPointerException("Null source stream");
		}
		if (templateFileNameOnClasspath == null)
		{
			throw new NullPointerException("Null template file");
		}
		Properties props = new Properties();
		props.setProperty("resource.loader", "class");
		props.setProperty("class.resource.loader.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		props.setProperty("runtime.log", new File(workDir, "velocity.log").getCanonicalPath());

		Velocity.init(props);

		DOMParser p = new DOMParser();
		p.setEntityResolver(new ProfileEntityResolver(profileDir));
		p.parse(new InputSource(sourceXml));
		Document document = p.getDocument();


		// TODO: very ugly : rely on transformer to resolve entities ipv XmlElementForVelocity
		// where we should treat entity_reference separately and return children
		StringWriter sw = new StringWriter();
		Source source = new DOMSource(document);
		Result result = new StreamResult(sw);
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);

		// generated document with entities
		File file = new File(workDir, "profileBefore2Java.xml");
		FileWriter fw = new FileWriter(file);
		fw.write(sw.toString());
		fw.close();

		p = new DOMParser();
		p.parse(new InputSource(new StringReader(sw.toString())));
		document = p.getDocument();

		/*
		 * Validating
		 */
		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		String schemaFileName = document.getDocumentElement().getAttribute("xsi:schemaLocation");
		schemaFileName = schemaFileName.substring(schemaFileName.indexOf(" ") + 1);
		//System.out.println("XSI " + new File(".").getCanonicalPath()  );

		// load a WXS schema, represented by a Schema instance
		Source schemaFile = new StreamSource(new File(schemaFileName));
		/*  Schema schema = factory.newSchema(schemaFile);


    Validator validator = schema.newValidator();
    validator.validate(new DOMSource(document));
		 */
		/*
		 *  getting namespace declarations
		 */
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("ac", Namespaces.ANNOCULTOR_CONVERTER.getUri());

		for (int i = 0; i < document.getDocumentElement().getAttributes().getLength(); i++)
		{
			Node att = document.getDocumentElement().getAttributes().item(i);
			if ("xmlns".equals(att.getPrefix()))
			{
				String existing = namespaces.get(att.getLocalName());
				if (existing != null && !existing.equals(att.getNodeValue()))
					throw new Exception("Attempt to redefine namespace " + att.getLocalName() + " = " + existing + ", with new uri " + att.getNodeValue());
				namespaces.put(att.getLocalName(), att.getNodeValue());
			}
		}

		VelocityContext context = new VelocityContext();
		context.put("xml", new XmlElementForVelocity(document.getDocumentElement(), namespaces).getFirstChild("ac:Profile"));
		context.put("namespaces", namespaces);

		Template template = Velocity.getTemplate(templateFileNameOnClasspath);

		/*
		 * Applying template
		 */
		PrintWriter writer = new PrintWriter(targetJava, true);
		template.merge(context, writer);
		writer.flush();
		return context;
	}

	public static void main(String args[]) 
	throws Exception
	{
		XConverter2Java converter = new XConverter2Java();
		converter.run(
				new File("."),
				XConverter2Java.class.getResourceAsStream("/xconverter/europeana.xml"), 
				"xconverter/XConverterGenerator.vm", 
				System.out,
				new File("."));

	}
}

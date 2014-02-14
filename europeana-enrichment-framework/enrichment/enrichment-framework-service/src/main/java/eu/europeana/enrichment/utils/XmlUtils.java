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
package eu.europeana.enrichment.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import eu.europeana.enrichment.common.Utils;
import eu.europeana.enrichment.converter.ConverterHandler;

/**
 * XML utils.
 * 
 * @author Borys Omelayenko
 * 
 */
public class XmlUtils {
	private static final String OPT_FN = "fn";

	public static void main(String... args) throws Exception {
		// Handling command line parameters with Apache Commons CLI
		Options options = new Options();

		options.addOption(OptionBuilder.withArgName(OPT_FN).hasArg()
				.isRequired()
				.withDescription("XML file name to be pretty-printed")
				.create(OPT_FN));

		// now lets parse the input
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options,
					Utils.getCommandLineFromANNOCULTOR_ARGS(args));
		} catch (ParseException pe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pretty", options);
			return;
		}

		List<File> files = Utils.expandFileTemplateFrom(new File("."),
				cmd.getOptionValue(OPT_FN));
		for (File file : files) {
			// XML pretty print
			System.out.println("Pretty-print for file " + file);
			if (file.exists())
				prettyPrintXmlFileSAX(file.getCanonicalPath());
			else
				throw new Exception("File not found: "
						+ file.getCanonicalPath());
		}
	}

	/**
	 * 
	 * @param fileStream
	 * @param handler
	 * @param validating
	 * @return <code>true</code> on success.
	 * @throws Exception
	 */
	public static int parseXmlFileSAX(File sourceFile,
			ConverterHandler handler, boolean validating) throws Exception {
		// Create a builder factory
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(validating);
		factory.setNamespaceAware(true);

		InputStream fileStream = new BufferedInputStream(new FileInputStream(
				sourceFile), 1024 * 1024);

		if (fileStream == null) {
			throw new Exception("Null input XML file stream");
		}
		if (handler == null) {
			throw new Exception("Null XML handler");
		}
		try {
			// Create the builder and parse the file
			SAXParser newSAXParser = factory.newSAXParser();
			if (newSAXParser == null) {
				throw new Exception("null SAX parser");
			}
			newSAXParser.parse(fileStream, handler);
		} catch (Exception e) {
			System.err.println("\n"
					+ "*****************************************************\n"
					+ "EXCEPTION OCCURRED in file "
					+ sourceFile.getCanonicalPath() + "\n" + "at line "
					+ handler.getDocumentLocator().getLineNumber()
					+ ", column "
					+ handler.getDocumentLocator().getColumnNumber());
			e.printStackTrace();
			System.err
					.println("\n"
							+ "TRYING TO CLOSE FILES GRACEFULLY \n"
							+ "*****************************************************\n");
			return -1;
		}
		return 0;
	}

	public static void prettyPrintXmlFileSAX(String filename) throws Exception {
		FileOutputStream fos = new FileOutputStream(filename + ".pretty");
		// XERCES 1 or 2 additional classes.
		OutputFormat of = new OutputFormat("XML", "UTF-8", true);
		of.setIndenting(true);
		of.setIndent(1);

		XMLSerializer serializer = new XMLSerializer(fos, of);
		// SAX2.0 ContentHandler.
		ContentHandler hd = serializer.asContentHandler();
		parseXmlFileSAX(new File(filename), new PrettyPrintHandler(hd), false);
		fos.close();
	}

	private static class PrettyPrintHandler extends ConverterHandler {
		ContentHandler hd;

		public PrettyPrintHandler(ContentHandler hd) {
			super(null);
			this.hd = hd;
		}

		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			hd.characters(arg0, arg1, arg2);
		}

		@Override
		public void endDocument() throws SAXException {
			hd.endDocument();
		}

		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException {
			hd.endElement(arg0, arg1, arg2);
		}

		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
			hd.endPrefixMapping(arg0);
		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
			hd.ignorableWhitespace(arg0, arg1, arg2);
		}

		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
			hd.processingInstruction(arg0, arg1);
		}

		@Override
		public void setDocumentLocator(Locator arg0) {
			hd.setDocumentLocator(arg0);
		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
			hd.skippedEntity(arg0);
		}

		@Override
		public void startDocument() throws SAXException {
			hd.startDocument();
		}

		@Override
		public void startElement(String arg0, String arg1, String arg2,
				Attributes arg3) throws SAXException {
			hd.startElement(arg0, arg1, arg2, arg3);
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
			hd.startPrefixMapping(arg0, arg1);
		}

	}

}

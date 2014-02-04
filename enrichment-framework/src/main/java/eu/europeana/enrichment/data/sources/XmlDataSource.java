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
package eu.europeana.enrichment.data.sources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import eu.europeana.enrichment.api.DataSource;
import eu.europeana.enrichment.common.Utils;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.converter.ConverterHandler;
import eu.europeana.enrichment.converter.ConverterHandler.ConversionResult;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.utils.XmlUtils;
import eu.europeana.enrichment.xconverter.impl.XConverterFactory;

/**
 * Source dataset consisting of a set of XML files.
 * 
 * @author Borys Omelayenko
 * 
 */
public class XmlDataSource implements DataSource {
	Logger log = LoggerFactory.getLogger(getClass().getName());

	private static class AttributesProxy implements Attributes {

		private String name;

		AttributesProxy(File src) throws IOException {
			name = src.getCanonicalPath();
		}

		public int getIndex(String uri, String localName) {
			return "name".equals(uri + localName) ? 0 : -1;
		}

		public int getIndex(String name) {
			return getIndex("", name);
		}

		public int getLength() {
			return 1;
		}

		public String getLocalName(int index) {
			return (index == 0) ? "name" : null;
		}

		public String getQName(int index) {
			return (index == 0) ? "name" : null;
		}

		public String getType(int index) {
			return (index == 0) ? "attribute" : null;
		}

		public String getType(String uri, String localName) {
			return null;
		}

		public String getType(String name) {
			return null;
		}

		public String getURI(int index) {
			return (index == 0) ? "" : null;
		}

		public String getValue(int index) {
			return (index == 0) ? name : null;
		}

		public String getValue(String uri, String localName) {
			return "name".equals(uri + localName) ? name : null;
		}

		public String getValue(String name) {
			return getValue("", name);
		}

	}

	private List<File> srcFiles = new ArrayList<File>();

	public XmlDataSource(Environment environment, String... file)
			throws IOException {
		System.out.println(environment
				.getParameter(Environment.PARAMETERS.ANNOCULTOR_INPUT_DIR));
		File inputDir = new File(
				"/home/gmamakis/workspace3/annocultor/converters/europeana/input_source");
		addSourceFile(inputDir, file);
		setMergeSourceFiles(true);
	}

	public void addSourceFile(File dir, String... pattern) throws IOException {
		if (dir == null) {
			throw new IOException("Null dir in source XML files ");
		}

		List<File> files = Utils.expandFileTemplateFrom(dir, pattern);
		if (files.size() == 0) {
			throw new IOException("No single file found with pattern "
					+ StringUtils.join(pattern, ",") + " in dir "
					+ dir.getCanonicalPath());
		}

		srcFiles.addAll(files);
	}

	@Override
	public void feedData(ConverterHandler handler, Path recordSeparatingPath,
			Path recordIdentifyingPath) throws Exception {

		int result = 0;
		if (isMergeSourceFiles()) {
			handler.multiFileStartDocument();
			handler.startElement("",
					XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILESET,
					XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILESET, null);
		}

		int current = 1;
		for (File src : srcFiles) {
			if (isMergeSourceFiles()) {
				handler.startElement("",
						XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILE,
						XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILE,
						new AttributesProxy(src));
			}

			if (result == 0) {
				log.info("File " + (current++) + "/" + srcFiles.size() + " "
						+ src.getName() + " of "
						+ (src.length() / FileUtils.ONE_MB) + " Mb");
				result = parseSourceFile(handler, src, recordSeparatingPath);
			}
			if (isMergeSourceFiles()) {
				handler.endElement("",
						XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILE,
						XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILE);
			}
		}
		if (isMergeSourceFiles()) {
			handler.endElement("",
					XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILESET,
					XConverterFactory.MERGED_SOURCES_OUTER_TAG_FILESET);
			handler.multiFileEndDocument();
		}
		handler.setConversionResult(result == 0 ? ConversionResult.success
				: ConversionResult.failure);
	}

	protected int parseSourceFile(ConverterHandler handler, File src,
			Path recordSeparatingPath) throws Exception {
		return XmlUtils.parseXmlFileSAX(src, handler, true);
	}

	boolean mergeSourceFiles = false;

	/**
	 * Creates a virtual source XML file with root element <code>fileset</code>
	 * and nested elements <code>file</code> wrapping each file in the source
	 * files. This allows multiple files to be processed in a single converter
	 * run.
	 */
	public void setMergeSourceFiles(boolean mergeSourceFiles) {
		this.mergeSourceFiles = mergeSourceFiles;
	}

	public boolean isMergeSourceFiles() {
		return mergeSourceFiles;
	}

}

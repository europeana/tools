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
package eu.europeana.enrichment.converter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.api.ConverterKernel;
import eu.europeana.enrichment.api.ConverterTester;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.converter.ConverterHandler.ConversionResult;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * The real converter XML/SQL -> RDF that does the actual conversion job.
 * 
 * @author Borys Omelayenko
 */
class Converter implements ConverterKernel {

	private static final long serialVersionUID = Common
			.getCommonSerialVersionUID();

	Logger log = LoggerFactory.getLogger(getClass().getName());

	private Task task;

	private ConverterTester tester;

	public void setTester(ConverterTester tester) {
		this.tester = tester;
	}

	protected Environment env;

	protected boolean generateN3 = false;

	private ConverterHandler handler;

	public void setMaximalRecordsToPass(int maximalRecordsToPass) {
		handler.setMaximalRecordsToPass(maximalRecordsToPass);
	}

	/**
	 * Creates a converter.
	 * 
	 * @param task
	 *            conversion task
	 * @param env
	 *            conversion environment
	 */
	Converter(Task task, ConverterHandler handler, ConverterTester tester) {
		this.task = task;
		this.env = task.getEnvironment();
		this.handler = handler;
		if (handler == null)
			this.handler = new ConverterHandler(task);
		this.tester = tester;
	}

	/*
	 * Converter
	 */
	StopWatch timeElapsed = new StopWatch();

	public int convert() throws Exception {
		int result = 0;
		startConversion();
		try {
			handler.setConversionResult(ConversionResult.failure);
			task.getDataSource().feedData(handler,
					task.getObjectRules().get(0).getRecordSeparatingPath(),
					task.getObjectRules().get(0).getPrimaryRecordIdPath());
			result = handler.getConversionResult() == ConversionResult.success ? 0
					: -1;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log.error(sw.toString());
			result = -1;
		} finally {
			finishConversion(result);
		}
		return result;
	}

	public BufferedInputStream makeInputStream(File src)
			throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(src), 1024 * 1024);
	}

	protected void startConversion() throws Exception {
		timeElapsed.start();
		// creating a tmp location
		File tmpDir = new File(env.getTmpDir() + "/");
		if (!tmpDir.exists())
			tmpDir.mkdir();
	}

	protected void finishConversion(int result) throws Exception {
		for (Graph target : task.getGraphs()) {
			// ignore virtual graphs
			if (target.getRealGraph() != target)
				continue;

			// finish writing this target
			target.endRdf();
			if (target.writingHappened()) {
				for (int i = 1; i <= target.getVolume(); i++) {
					log.info("Saved "
							+ target.getFinalFile(i).getCanonicalPath());
				}
			} else {
				log.warn("Writing never started for "
						+ target.getFinalFile(1).getCanonicalPath());
			}
		}

		timeElapsed.stop();
		log.info("Finished conversion in " + timeElapsed + " ms.");
		log.info(result == 0 ? "\nSUCCESS."
				: "\nFAILURE. Please, examine the error messages above (they may have already scrolled up). Please refer to directory ./doc for a conversion report.");
	}

}
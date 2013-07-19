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
package eu.annocultor.tests;

import junit.framework.TestCase;
import eu.annocultor.TestEnvironment;
import eu.annocultor.api.Factory;
import eu.annocultor.api.ObjectRule;
import eu.annocultor.api.Reporter;
import eu.annocultor.api.Task;
import eu.annocultor.context.Concepts;
import eu.annocultor.context.Namespaces;
import eu.annocultor.context.Concepts.RDF;
import eu.annocultor.context.Concepts.SKOS;
import eu.annocultor.path.Path;
import eu.annocultor.rules.CreateResourcePropertyRule;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.rules.RenameLiteralPropertyRule;
import eu.annocultor.rules.SequenceRule;
import eu.annocultor.xconverter.api.Graph;

public class ReporterTest extends TestCase
{

	public Task initializeTask() throws Exception
	{
		Task task = Factory.makeTask("bibliopolis", "terms", "Bibliopolis, KB", Namespaces.DC, new TestEnvironment());

		Graph trgTerms = Factory.makeTermsGraph(task);
		Graph trgTermsMap = Factory.makeGraph(task, "map", "aat", "mapping to AAT");

		ObjectRule termsMap =
				ObjectRuleImpl.makeObjectRule(task,
						new Path("dc:Results/dc:Recordset/dc:Record", new Namespaces()),
						new Path("dc:Results/dc:Recordset/dc:Record/dc:TWOND", new Namespaces()),
						new Path("dc:Results/dc:Recordset/dc:Record/dc:NUM", new Namespaces()),
						null,
						true);
		termsMap.addRelRule(new Path("NUM"),
				new SequenceRule(new RenameLiteralPropertyRule(Concepts.DC.IDENTIFIER, null, trgTerms),
						new CreateResourcePropertyRule(RDF.TYPE, SKOS.CONCEPT, trgTerms)));
		termsMap.addRelRule(new Path("DEF"), new RenameLiteralPropertyRule(SKOS.DEFINITION, "nl", trgTerms));
		termsMap.addRelRule(new Path("DEF_EN"), new RenameLiteralPropertyRule(Concepts.SKOS.DEFINITION,
				"en",
				trgTerms));
		return task;
	}

	public void testConsole() throws Exception
	{
		Task task = initializeTask();
		Reporter reporter = task.getReporter();

		reporter.addTask(task);

		reporter.log("30-jul-2008 13:18:44 eu.annocultor.Converter startConversion");
		reporter.log("INFO: Annocultor version 1.3 build 3");
		reporter.log("30-jul-2008 13:18:44 eu.annocultor.Converter convertFilesToRDF");
		reporter.log("INFO: Parsing source file xml/termenlijst.xml");
		reporter.log("30-jul-2008 13:18:53 info.aduna.lang.service.ServiceRegistry <init>");
		reporter.log("INFO: Registered service class org.openrdf.rio.n3.N3ParserFactory");
		reporter.log("30-jul-2008 13:19:01 org.openrdf.query.parser.serql.NullProcessor$NullVisitor visit");
		reporter.log("WARNING: Use of NULL values in SeRQL queries has been deprecated");
		reporter.log("Exception in thread \"main\" java.lang.Exception: No terms loaded to vocabulary bibliopolis");
		reporter.log("at eu.annocultor.tagger.vocabularies.Vocabulary.loadTerms(Vocabulary.java:263)");
		reporter.report();
	}

	public void testNull() throws Exception
	{
		Path.formatPath(null, new Namespaces());
		//
		//    ReporterImpl.formatPath(new Path("annaTordai.mil"), null, new HashMap<String, String>());
		//
		//    ReporterImpl.formatPath(new Path("htuysp"), new HashMap<String, String>(), null);
	}
}
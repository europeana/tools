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
package eu.annocultor.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.annocultor.common.Utils;
import eu.annocultor.reports.parts.ReportCounter.ObjectCountPair;
import eu.annocultor.tagger.rules.VocabularyMatchResult;

/**
 * Reporter: a store for named counters and messages.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ReportPresenter extends AbstractReporter {

    public static void main(String... args) throws Exception {
        generateReport(new File("."));
    }

    public static void generateReport(File annoCultorHome) throws Exception {
        Logger log = LoggerFactory.getLogger("ReportPresenter");
        File dir = new File(annoCultorHome, "doc");
        if (dir.isDirectory() && dir.exists() && dir.list().length > 0) {

            for (File fn : dir.listFiles()) {
                if (fn.isDirectory() && !fn.isHidden()) {
                    String datasetId = fn.getName();
                    log.info("Loading conversion statistics for " + datasetId);
                    ReportPresenter reportPresenter = new ReportPresenter(dir, datasetId);
                    log.info("Generating conversion report for " + datasetId);
                    reportPresenter.makeHtmlReport();
                }
            }
        }
    }

    public ReportPresenter(File reportDir, String datasetId) throws Exception {
        super(reportDir, datasetId);
        init();
        load();
    }

    public void makeHtmlReport() throws Exception
    {
        File jsReportDataFile = new File(getReportDir(), "ReportData.js");
        jsReportDataFile.delete();
        PrintWriter jsonReportWriter = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(jsReportDataFile),"UTF-8")
        );

        // Header
        jsonReportWriter.println("YAHOO.namespace (\"AnnoCultor\");");
        jsonReportWriter.println("YAHOO.AnnoCultor.Data = ");
        JSONWriter json = new JSONWriter(jsonReportWriter).object();

        // environment
        json = json.key("environment").array();
        for (KeyValuePair kvp : environment) {	
            json = json.object().key("id").value(kvp.getKey()).key("value").value(kvp.getValue()).endObject();
        }
        json = json.endArray();

        // graphs
        json = json.key("graphs").array();
        for (Graphs graph : graphs)
        {
            json =
                json
                .object()
                .key("id")
                .value(graph.getId())
                .key("subjects")
                .value(graph.getSubjects())
                .key("properties")
                .value("0")
                .key("triples")
                .value(graph.getTriples())
                .key("diff")
                .value("")
                //task.getEnvironment().getPreviousDir() == null ? "" : ("file://" + new File(task
                //.getEnvironment()
                //.getDiffDir(), graph.getId() + ".html").getCanonicalPath()))
                .endObject();
        }
        json = json.endArray();

        // rules
        json = json.key("rules").array();
        for (ObjectCountPair<RuleInvocation> ocp : invokedRules.asSorted()) {
            json =
                json
                .object()
                .key("id")
                .value(ocp.getObject().getId())
                .key("rule")
                .value(ocp.getObject().getRule())
                .key("tag")
                .value(ocp.getObject().getPath())
                .key("firings")
                .value(ocp.getCount())
                .endObject();		
        }
        json = json.endArray();

        // unusedtags
        json = json.key("unusedtags").array();
        for (ObjectCountPair<Id> ocp : forgottenPaths.asSorted()) {
            json =
                json
                .object()
                .key("id")
                .value(ocp.getObject().getId())
                .key("occurrences")
                .value(ocp.getCount())
                .endObject();
        }
        json = json.endArray();

        // lookup counters
        for (VocabularyMatchResult result : VocabularyMatchResult.values()) {

            int count = 0;				
            List<ObjectCountPair<Lookup>> asSorted = lookupCounters.asSorted();
            messages.add("Total " + result + ": " + asSorted.size());

            json = json.key(result.getName()).array();
            for (ObjectCountPair<Lookup> counter : asSorted) {

                Lookup object = counter.getObject();

                if (object.getResult().equals(result.getName())) {
                    boolean showCode = ! result.getName().equals(VocabularyMatchResult.missed.getName());
                    json =
                        json.object().key("term").value(
                                ""
                                //								+ object.getRule()
                                //								+ ":"
                                + object.getPath()
                                //								+ ":"
                                //								+ object.getResult()
                                + ":"
                                + "<b>"
                                + object.getLabel() 
                                + (showCode ? ("(" + object.getCode() + ")") : "")
                                + "</b>").key("count").value(counter.getCount()).endObject();
                    count++;

                    if (count > 500)
                    {
                        break;
                    }
                }
            }
            json = json.endArray();
        }

        // messages
        json = json.key("console").array();
        for (String line : messages)
        {
            json = json.object().key("line").value(line).endObject();
        }
        json = json.endArray();


        json.endObject();
        jsonReportWriter.println(";");
        jsonReportWriter.flush();
        jsonReportWriter.close();

        // ------------

        String[] reportFiles = new String[]  {
                "/yui/datatable/assets/skins/sam/datatable.css",
                "/yui/datatable/datatable-debug.js",
                "/yui/logger/assets/skins/sam/logger.css",
                "/yui/tabview/assets/skins/sam/tabview.css",
                "/yui/logger/logger-debug.js",
                "/yui/yahoo-dom-event/yahoo-dom-event.js",
                "/yui/datasource/datasource-debug.js",
                "/yui/element/element-debug.js",
                "/yui/yuiloader/yuiloader-min.js",
                "/yui/tabview/tabview-min.js",
                "/ReportCode.js",
                "/Report.css"
        };

        // copy report files
        for (String fn : reportFiles) 
        {
            new File(getReportDir(), FilenameUtils.getPath(fn)).mkdirs();
            File f = new File(getReportDir(), fn);
            Utils.copy(getClass().getResourceAsStream(fn), f);			
        }

        // copy report template
        Utils.copy(
                getClass().getResourceAsStream("/ReportTemplate.html"), 
                new File(getReportDir(),"index.html"));
        Utils.copy(
                getClass().getResourceAsStream("/ReportCode.js"), 
                new File(getReportDir(),"ReportCode.js"));
        Utils.copy(
                getClass().getResourceAsStream("/Report.css"),
                new File(getReportDir(), "Report.css"));
    }


}

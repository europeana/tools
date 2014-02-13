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
package eu.annocultor.tagger.server.controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.annocultor.context.Environment;
import eu.annocultor.context.EnvironmentImpl;
import eu.annocultor.converters.europeana.EuropeanaSolrDocumentTagger;
import eu.annocultor.reports.ReportPresenter;



/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 *
 */
@Controller
public class SolrTaggerController  
{

    private static final String ENRICHMENT_LOG = "enrichment.log";

    @RequestMapping("/start.html")
    public void start(
            @RequestParam(value = "solrSourceUrl", required = true) String solrSourceUrl,
            @RequestParam(value = "solrDestinationUrl", required = true) String solrDestinationUrl,
            @RequestParam(value = "query", required = true) String solrQuery,
            @RequestParam(value = "start", required = false, defaultValue = "0") String startStr,
            @RequestParam(value = "fileUrl", required = false) String fileUrl,
            HttpServletResponse response) throws Exception {

        File home = fetchAnnoCultorHome();
        PrintWriter log = new PrintWriter(new FileWriter(new File(home, ENRICHMENT_LOG)));
        EuropeanaSolrDocumentTagger tagger = null;
        try {
            int start = Integer.parseInt(startStr);
            if(StringUtils.isBlank(fileUrl)){
            	tagger = tag(solrQuery, solrSourceUrl, solrDestinationUrl,start, log);
            }
            else {
            	List<String[]> collections = readFile(home.getAbsolutePath() +"/"+ fileUrl);
            	for(String collection : collections.get(0)){
            		tagger = tag("europeana_collectionName:"+collection+"*", solrSourceUrl, solrDestinationUrl,start, log);
            	}
            	for(String collection:collections.get(1)){
            		tagger = delete("europeana_collectionName:"+collection+"*", solrSourceUrl, solrDestinationUrl,start, log);
            	}
            }
            tagger.optimize();
        }
        catch (Throwable e) {
            log.println(new Date());
            e.printStackTrace(new PrintWriter(log));
        } finally {
            log.write("SEMANTIC ENRICHMENT COMPLETED");
            log.flush();
            log.close();
        }
        
        PrintWriter out = response.getWriter();
        out.println("Semantic tagging started and will continue for a while");
        out.println("Ongoing log is available at /log.html");
        out.flush();
        out.close();
    }

    private EuropeanaSolrDocumentTagger delete(String query, String solrSourceUrl,
			String solrDestinationUrl, int start, PrintWriter log) throws SolrServerException, IOException {
    	EuropeanaSolrDocumentTagger tagger = new EuropeanaSolrDocumentTagger(query, solrSourceUrl, solrDestinationUrl, start, log);
		tagger.delete(query, solrSourceUrl, solrDestinationUrl, start, log);
		return tagger;
	}

	private List<String[]> readFile(String url) throws IOException{
    	FileInputStream fStream = new FileInputStream(new File(url));
    	StringBuffer sb = new StringBuffer();
    	BufferedInputStream bin = new BufferedInputStream(fStream);
    	byte[] contents = new byte[1024];
    	int bytesRead = 0;
    	while((bytesRead = bin.read(contents))!=-1){
    		sb.append(new String(contents,0,bytesRead));
    	}
    	String toImport = StringUtils.substringBetween(sb.toString(), "Import\n", "Delete\n");
    	List<String[]> collections = new ArrayList<String[]> ();
    	String[] collectionsToImport = StringUtils.split(toImport,"\n");
    	collections.add(collectionsToImport);
    	String toDelete = StringUtils.substringAfter(sb.toString(), "Delete\n");
    	String[] collectionsToDelete = StringUtils.split(toDelete,"\n");
    	collections.add(collectionsToDelete);
    	return collections;
    }
    
    private EuropeanaSolrDocumentTagger tag(String query, String solrSourceUrl,String solrDestinationUrl,int start, PrintWriter log) throws Exception{
    	EuropeanaSolrDocumentTagger tagger = new EuropeanaSolrDocumentTagger(query, solrSourceUrl, solrDestinationUrl, start, log);
        tagger.init("Europeana");
        if (start == 0) {
            tagger.clearDestination(query);
        }
        try {
            log.println(new Date() + "***** Start tagging ******");
            tagger.tag();
        } finally {
            log.println(new Date() + "***** Making report data ******");
            tagger.report();
            log.println(new Date() + "***** Generating html report ******");
            File home = fetchAnnoCultorHome();
            ReportPresenter.generateReport(home);
        }
        return tagger;
    }
    
    File fetchAnnoCultorHome() {
        Environment environment = new EnvironmentImpl();
        return environment.getAnnoCultorHome();
    }

    File fetchDoc() {
        Environment environment = new EnvironmentImpl();
        return environment.getDocDir();
    }

    @RequestMapping("/log.html")
    public void log(HttpServletResponse response) throws Exception {
        IOUtils.copy(
                new FileInputStream(new File(fetchAnnoCultorHome(), ENRICHMENT_LOG)),
                response.getOutputStream()
        );
    }
    @RequestMapping("/doc/**/*.*")
    public void report(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {

        String path = merge(request.getServletPath(), request.getPathInfo());
        path = path.substring("/doc".length());
        response.setCharacterEncoding("UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            IOUtils.copy(new FileInputStream(new File(fetchDoc(), path)), outputStream);
        } finally {
            outputStream.close();
        }
    }

    private String merge(String pageNamePrefix, String pageName) {
        return 
        (pageNamePrefix == null ? "" : pageNamePrefix)  
        + (pageName == null ? "" : pageName);
    }


}

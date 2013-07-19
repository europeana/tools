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
package eu.annocultor.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import eu.annocultor.common.Helper;
import eu.annocultor.context.Concepts;
import eu.annocultor.context.Namespaces;

/**
 * Starts with top concepts listed in a file and traces down the skos:narrower
 * relation to get a list of concepts, (indirect) children of the top concepts,
 * and save it to a file.
 * 
 * @author Borys Omelayenko
 *
 */
public class HierarchyTracingFilter {

    /**
     * Starts with top X concepts and selects narrower
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        String fileWithTopConcepts = args[0];
        String fileWithRdf = args[1];
        String fileWithSelection = args[2];

        HierarchyTracingFilter filter = new HierarchyTracingFilter();
        filter.doTracing(fileWithTopConcepts, fileWithSelection, fileWithRdf);
    }

    public void visitTerm(RepositoryConnection connection, ValueFactory factory, StringInStack url, StringInStack previous) 
    throws Exception {

    }

    public void doTracing(String fileWithTopConcepts, String fileWithSelection,  String... fileWithRdf)
    throws Exception {
        Repository rdf = Helper.createLocalRepository();
        File[] filesToLoad = new File[fileWithRdf.length];
        for (int i = 0; i < fileWithRdf.length; i++) {
            filesToLoad[i] = new File(fileWithRdf[i]);
        }
        Helper.importRDFXMLFile(rdf, Namespaces.ANNOCULTOR_CONVERTER.getUri(), filesToLoad);
        RepositoryConnection connection = rdf.getConnection();
        ValueFactory factory = rdf.getValueFactory();

        System.out.println("Loaded " + connection.size() + " statements");
        List topConceptsStr = FileUtils.readLines(new File(fileWithTopConcepts), "UTF-8");
        List<StringInStack> top = new ArrayList<StringInStack>();
        for (Object object : topConceptsStr) {
            top.add(new StringInStack(object.toString(), 0));
        }
        
        Set<String> passedUrls = traceBroaderDepthFirst(connection, factory, top);

        // save
        List<String> passedSorted = new LinkedList<String>();
        passedSorted.addAll(passedUrls);
        Collections.sort(passedSorted);
        System.out.println("Saving " + passedSorted.size() + " terms");
        saveListOfUrls(fileWithSelection, passedUrls, passedSorted);
    }

    public void saveListOfUrls(String fileWithSelection,  Collection<String> passedUnSorted, List<String> passedSorted) throws IOException {
        if (fileWithSelection != null) {
            FileUtils.writeLines(new File(fileWithSelection), passedSorted, "\n");
        }
    }

    public static class StringInStack implements Comparable<StringInStack> {
        String string;
        int level;
        
        public StringInStack(String string, int level) {
            super();
            this.string = string;
            this.level = level;
        }

        public String getString() {
            return string;
        }

        public int getLevel() {
            return level;
        }

        @Override
        public int compareTo(StringInStack o) {
             return string.compareTo(o.string);
        }
            
    }

    private Set<String> traceBroaderDepthFirst(RepositoryConnection connection, ValueFactory factory, List<StringInStack> topConcepts)
    throws Exception {
        // trace skos:broader tree top-down
        Stack<StringInStack> urlsToCheck = new Stack<StringInStack>();
        urlsToCheck.addAll(topConcepts);
        Set<String> passedUrls = new HashSet<String>();

        while (!urlsToCheck.isEmpty()) {

            StringInStack url = urlsToCheck.pop();
            if (!StringUtils.isEmpty(url.getString()) && passedUrls.add(url.getString())) {
                List<StringInStack> children = fetchNarrower(connection, factory, url, urlsToCheck.isEmpty() ? null : urlsToCheck.peek());
                Collections.sort(children);
                urlsToCheck.addAll(children);
            }
        }
        return passedUrls;
    }

    public List<StringInStack> fetchNarrower(RepositoryConnection connection, ValueFactory factory, StringInStack url, StringInStack previous) 
    throws Exception {
        visitTerm(connection, factory, url, previous);
        final URI broader = factory.createURI(Concepts.SKOS.BROADER.getUri());
        RepositoryResult<Statement> children = connection.getStatements(
                null, 
                broader,
                factory.createURI(url.getString()),
                false
        );

        List<StringInStack> result = new ArrayList<StringInStack>();
        while (children.hasNext()) {
            result.add(new StringInStack(children.next().getSubject().stringValue(), url.getLevel() + 1));
        }
        children.close();
        return result;
    }
}

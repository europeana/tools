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
package eu.europeana.enrichment.converters.time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import eu.europeana.enrichment.context.Concepts;
import eu.europeana.enrichment.utils.HierarchyTracingFilter;

/**
 * Generates a bunch of html files to make each ontology term resolveable.
 * 
 * @author Borys Omelayenko
 *
 */
public class OntologyToHtmlGenerator extends HierarchyTracingFilter {

    /**
     * Starts with top X concepts and selects narrower
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        System.out.println("Starting");
        String dirWithSelection = "pages";
        OntologyToHtmlGenerator filter = new OntologyToHtmlGenerator();
        filter.outputDir = new File(dirWithSelection);
        filter.outputDir.mkdirs();

        String fileWithTopConcepts = args[0];
        String[] f = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            f[i-1] = args[i];
        }
        filter.doTracing(fileWithTopConcepts, null, f);            
    }

    File outputDir;

    List<String> urls = new ArrayList<String>();
    {
        urls.add("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        urls.add("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        urls.add("<head>");
        urls.add("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        urls.add("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.8.2r1/build/fonts/fonts-min.css\" />");
        urls.add("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.8.2r1/build/treeview/assets/skins/sam/treeview.css\" />");
        urls.add("<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.8.2r1/build/yahoo-dom-event/yahoo-dom-event.js\"></script>");
        urls.add("<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.8.2r1/build/treeview/treeview-min.js\"></script>");
        urls.add("<title>AnnoCultor Time Ontology Term</title>");
        urls.add("<style>");
        for (int i = 0; i < 10; i++) {
            urls.add("p.level_" + i + " { margin-left: " + i + "0px; }");                    
        }
        urls.add("</style>");        
        urls.add("<link rel=\"stylesheet\" href=\"terms.css\" type=\"text/css\" />");
        urls.add("</head>");        
        urls.add("<body class=\"yui-skin-sam\">");
        urls.add("<div id=\"markup\"  class=\"whitebg\">");
        urls.add("<ul>");
    }

    public static class Node {
        LinkedList<Node> children = new LinkedList<Node>();
        String text;
        Node parent;
        int level;
        String url;

        public Node(String text, String url, int level, Node parent) {
            this.text = text;
            this.parent = parent;
            this.level = level;
            this.url = url;
            if (parent != null) {
                parent.addChild(this);
            }
        }
        public LinkedList<Node> getChildren() {
            return children;
        }        
        public Node getParent() {
            return parent;
        }
        public String getText() {
            return text;
        }
        private void addChild(Node node) {
            children.add(node);
        }
    }

    Node tree = new Node("Time", "", 0, null);

    Node last = tree;

    int lastLevel = -1;

    @Override
    public void visitTerm(RepositoryConnection connection, ValueFactory factory, StringInStack url, StringInStack previous) throws Exception {

        String prefLabel = makeTermDefinitionFile(connection, factory, url);

        addToIndexFile(url, prefLabel);
    }

    String makeTermDefinitionFile(RepositoryConnection connection,
            ValueFactory factory, StringInStack url)
    throws RepositoryException, FileNotFoundException, IOException {
        List<String> term = new ArrayList<String>();
        term.add("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        term.add("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        term.add("<head>");
        term.add("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        term.add("<link rel=\"stylesheet\" href=\"terms.css\" type=\"text/css\" />");


        List<String> pref = collectLabels(connection, factory, Concepts.SKOS.LABEL_PREFERRED.getUri(), url);
        List<String> alt = collectLabels(connection, factory, Concepts.SKOS.LABEL_ALT.getUri(), url);
        List<String> beginDate = collectLabels(connection, factory, Concepts.ANNOCULTOR.DATE_BEGIN.getUri(), url);
        List<String> endDate = collectLabels(connection, factory, Concepts.ANNOCULTOR.DATE_END.getUri(), url);

        String prefLabel = null;
        if (!pref.isEmpty()) {
            prefLabel = StringUtils.join(pref.toArray(), ", ");
        }
        if (prefLabel == null && !alt.isEmpty()) {
            prefLabel = alt.get(0);
        }
        if (prefLabel == null) {
            prefLabel = "Time";
        }
        term.add("<title>" + prefLabel + "</title>");
        term.add("</head>");        
        term.add("<body>");
        term.add("<p><a href=\"http://annocultor.eu/\">Back to AnnoCultor</a></p>");

        formatLabels(term, pref, alt);
        formatDates(term, beginDate, endDate);

        term.add("</body>");
        term.add("</html>");

        FileOutputStream os = new FileOutputStream(new File(outputDir, StringUtils.substringAfterLast(url.getString(), "/")));
        IOUtils.writeLines(term, "\n", os, "UTF-8");
        os.close();
        return prefLabel;
    }

    void addToIndexFile(StringInStack url, String prefLabel) {
        //        if (previous == null) {
        //            
        //        } else {
        //            if (url.getLevel() > previous.getLevel()) {
        //                urls.add("</div>");               
        //                urls.add("<div class=\"div_" + url.getLevel() + "\">");               
        //            } else {
        //                urls.add("<div class=\"div_" + url.getLevel() + "\">");               
        //                urls.add("<div class=\"div_" + url.getLevel() + "\">");                               
        //            }
        //        }


        String prefix = StringUtils.repeat(" ", url.getLevel());
        String prefLabelToDisplay = StringUtils.substringBefore(prefLabel, "<sub>");
        String text = "<a href=\"" + url.getString() + "\">" + prefLabelToDisplay + "</a>";
        if (url.getLevel() > lastLevel) {
            last = new Node(text, url.getString(), url.getLevel(), last);
            //            urls.add(prefix + "<li><ul>");
        }
        if (url.getLevel() < lastLevel) {
            for (int i = 0; i <= lastLevel - url.getLevel(); i++) {
                last = last.getParent();
            }
            last = new Node(text, url.getString(), url.getLevel(), last);
            //            urls.add(prefix + "</ul></li>");            
        }
        if (url.getLevel() == lastLevel) {
            last = new Node(text, url.getString(), url.getLevel(), last.getParent());
            //            urls.add("</li><li>");
        }

        //        urls.add("<li><p class=\"level_" + url.getLevel() + "\"><a href=\"" + url.getString() + "\">" 
        //                + StringUtils.substringBefore(prefLabel, "<sub>") + "</a></p>");
        lastLevel = url.getLevel();
    }

    private void formatDates(List<String> term, List<String> beginDate, List<String> endDate) {
        String time = "";
        if (!beginDate.isEmpty()) {
            time = beginDate.get(0);
        }
        if (!endDate.isEmpty()) {
            time += " - " + endDate.get(0);
        }        
        if (!time.isEmpty()) {
            term.add("<h1>Period</h1>");
            term.add(time);
        }
    }

    private void formatLabels(List<String> term, List<String> pref, List<String> alt) {
        term.add("<h1>Preferred label</h1>");
        for (String label : pref) {
            term.add("<p>" + label + "</p>");
        }
        term.add("<h1>Alternative labels</h1>");
        for (String label : alt) {
            term.add("<p>" + label + "</p>");
        }
    }

    private List<String> collectLabels(RepositoryConnection connection, ValueFactory factory, String property, StringInStack url) 
    throws RepositoryException {
        List<String> alt = new ArrayList<String>();
        RepositoryResult<Statement> alts = connection.getStatements(
                factory.createURI(url.getString()),
                factory.createURI(property),
                null, 
                false
        );
        while (alts.hasNext()) {
            Value lbl = alts.next().getObject();
            String lang = "";
            if (lbl instanceof Literal) {
                String language = ((Literal)lbl).getLanguage();
                if (language != null && !language.isEmpty()) {
                    lang = " <sub>" + language + "</sub>";
                }
            }
            alt.add(lbl.stringValue() + lang);
        }
        alts.close();
        return alt;
    }

    @Override
    public void saveListOfUrls(String fileWithSelection, Collection<String> passedUnSorted, List<String> passedSorted)
    throws IOException {

        for (Node child : tree.children) {
            printBranch(child, urls);            
        }
        urls.add("</ul>");
        urls.add("</div>");

        urls.add("<script type=\"text/javascript\">");
        urls.add("var tree1;");

        urls.add("(function() {");
        urls.add(" var treeInit = function() {");
        urls.add("  tree1 = new YAHOO.widget.TreeView(\"markup\");");
        urls.add("  tree1.render();    ");
        urls.add("  tree1.subscribe('dblClickEvent',tree1.onEventEditNode);");
        urls.add("  };");
        urls.add(" YAHOO.util.Event.onDOMReady(treeInit);");
        urls.add("})();");
        urls.add("</script>");

        urls.add("<div id=\"treeDiv2\" class=\"whitebg\"></div>");
        urls.add("</body>");
        urls.add("</html>");
        IOUtils.writeLines(urls, "\n", new FileOutputStream(new File(outputDir, "index.html")), "UTF-8");
    }

    Set<String> expanded = new HashSet<String>();

    {
        expanded.add("http://annocultor.eu/time/Time");
        expanded.add("http://annocultor.eu/time/HistoricalPeriod");
        expanded.add("http://annocultor.eu/time/ChronologicalPeriod");
        expanded.add("http://annocultor.eu/time/AD2xxx");
    }

    public void printBranch(Node node, List<String> os) {

        if (!StringUtils.isBlank(node.text)) {
            String prefix = StringUtils.repeat(" ", node.level);
            String classExpanded = expanded.contains(node.url) ? " class=\"expanded\"" : "";
            os.add(prefix + "<li" + classExpanded + ">" + node.text);
            if (!node.children.isEmpty()) {
                if (node.children.size() > 1) {
                    os.add(prefix + "<ul>");
                }
                for (Node child : node.children) {
                    printBranch(child, os);
                }
                if (node.children.size() > 1) {
                    os.add(prefix + "</ul>");
                }
            }
            os.add(prefix + "</li>");
        }
    }


}

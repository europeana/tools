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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import eu.annocultor.common.Helper;
import eu.annocultor.common.Utils;
import eu.annocultor.context.Environment;
import eu.annocultor.context.EnvironmentImpl;
import eu.annocultor.context.Namespaces;
import eu.annocultor.context.Concepts.DC;
import eu.annocultor.context.Concepts.RDF;
import eu.annocultor.context.Concepts.SKOS;
import eu.annocultor.context.Concepts.VRA;
import eu.annocultor.tagger.terms.CodeURI;
import eu.annocultor.tagger.vocabularies.DisambiguationContext;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;

/**
 * Generates an approval form for sameAs statements, given a file with the
 * statements and a source of images.
 * 
 * @author Borys Omelayenko
 * @author Anna Tordai
 * 
 */
public class ApprovalFormClusters
{
	public static final String EVALUATION = Namespaces.ANNOCULTOR_CONVERTER + "evaluation";

	/**
	 * Assumes the file with statements to define clusters with instances of
	 * <code>skos:Collection</code>, property <code>skos:member</code> and
	 * <code>skos:prefLabel</code>.
	 * 
	 * @param args
	 *          fileWithStatementsToApprove.rdf, images.rdf...
	 */
	public static void main(String[] args) throws Exception
	{
		for (String string : args)
		{
			System.out.println(">" + string);
		}
		File fileToApprove = new File(args[0]);
		if (!fileToApprove.exists())
			throw new Exception("File " + fileToApprove.getCanonicalPath() + " does not exist.");
		File fileToApprove2 = new File(args[1]);
		if (!fileToApprove2.exists())
			throw new Exception("File " + fileToApprove2.getCanonicalPath() + " does not exist.");
		String taskSignature = args[2];
		String description = args[3];
		String proxy = args[4];
		if (proxy.equals("none"))
			proxy = null;
		if (description.contains("\'"))
			throw new Exception("Description should not contain character ' as it will conflict with the js");
		// loading clusters
		VocabularyOfTerms map = new VocabularyOfTerms("clustesToApprove", null);
		Environment environment = new EnvironmentImpl();
		map.loadTermsSeRQL("SELECT X, T FROM {X} <"
			+ RDF.TYPE.getUri()
			+ "> {<"
			+ SKOS.COLLECTION
			+ ">}; <"
			+ SKOS.LABEL_PREFERRED
			+ "> {T}", 
			environment.getTmpDir(), 
			new File("."),
			fileToApprove.getPath());

		// loading images
		Map<String, String> images = new HashMap<String, String>();
		String[] imageFiles = args[5].contains(" ") ? args[5].split(" ") : args[5].split(",");
		VocabularyOfTerms imagesThisFile = new VocabularyOfTerms("img", null);
		imagesThisFile.loadTermsSeRQL("SELECT I, W FROM {I} <" + VRA.DEPICTS.getUri() + "> {W}",
				environment.getTmpDir(),
				new File("."),				
				imageFiles);
		// reverse to work -> image
		for (CodeURI key : imagesThisFile.codeSet())
		{
			images.put(imagesThisFile.findLabel(key), key.toString());
		}

		// loading additional info
		VocabularyOfTerms workTitles = new VocabularyOfTerms("titles", null);
		workTitles.loadTermsSeRQL(
				"SELECT I, W FROM {I} <" + DC.TITLE.getUri() + "> {W}", 
				environment.getTmpDir(), 
				new File("."),
				imageFiles);
		VocabularyOfTerms workDates = new VocabularyOfTerms("dates", null);
		workDates.loadTermsSeRQL(
				"SELECT I, W FROM {I} <" + DC.DATE.getUri() + "> {W}", 
				environment.getTmpDir(), 			
				new File("."),
				imageFiles);
		VocabularyOfTerms workCreators = new VocabularyOfTerms("creators", null);
		workCreators.loadTermsSeRQL(
				"SELECT I, W FROM {I} <" + DC.CREATOR.getUri() + "> {W}", 
				environment.getTmpDir(), 
				new File("."),
				imageFiles);

		String[] scripts =
				new String[]
				{
						"/www/script/login.js",
						"/www/script/misc.js",
						"/www/yui/2.5.1/build/utilities/utilities-debug.js",
						"/www/yui/2.5.1/build/treeview/treeview.js",
						"/www/yui/2.5.1/build/json/json.js",
						"/www/script/parameters.js",
						"/www/script/resultview.js",
						"/www/yui/2.5.1/build/datasource/datasource-beta.js",
						"/www/yui/2.5.1/build/datatable/datatable-beta.js",
						"/www/yui/2.5.1/build/container/container.js",
						"/www/yui/2.5.1/build/menu/menu.js",
						"/www/script/localview.js",
						"/www/script/carousel.js",
						"/www/yui/2.5.1/build/button/button.js",
						"/www/script/module.js",
						"/www/yui/2.5.1/build/autocomplete/autocomplete.js",
						"/www/script/autocomplete.js" };

		String base = "http://e-culture.multimedian.nl/backup/";

		// generating html
		PrintWriter html = new PrintWriter("approveForm_" + taskSignature + ".html");
		html.println("<html><head>");
		// for (String script : scripts)
		// {
		// html.println("<script type='text/javascript' src='" + base + script +
		// "'></script>");
		// }
		html.println(Utils
				.readResourceFileFromSamePackageAsString(ApprovalFormClusters.class, "/headers.html")
				.replaceAll("/backup/", base));
		// html.println("<link rel='stylesheet' type='text/css' href='http://www.cs.vu.nl/~borys/styles/mainstyle.css'/>");
		// html.println("<link rel='stylesheet' type='text/css' href='http://annocultor.sourceforge.net/style.css'/>");
		html.println("<style type='text/css'>");
		html.println(Utils.readResourceFileFromSamePackageAsString(ApprovalFormClusters.class, "/style.css"));
		html.println("</style>");
		html.println("</head><body  class='yui-skin-sam'>");

		// locaview panel
		html.println("<div id='localview'></div>");
		html.println("<script type='text/javascript'>");
		html
				.println("var localview = new YAHOO.mazzle.LocalView('localview', '"
					+ base
					+ "api/localview', '', {\"cellFormatting\":\"row\", \"edit\":true, \"languageFilter\":false, \"overlay\":true, \"source\":\"none\"});");
		html.println("</script>");

		html.println("<p id='title'>AnnoCultor</p>");
		html.println("<p>Task: " + taskSignature + "</p>");
		html.println("<p>" + description + "</p>");
		html.println("<p>User signature: <input name='userSignature'/></p>");

		Repository rdf = Helper.createLocalRepository();
		ValueFactory f = rdf.getValueFactory();
		Helper.importRDFXMLFile(rdf, "http://localhost/", fileToApprove);
		Helper.importRDFXMLFile(rdf, "http://localhost/", fileToApprove2);

		int i = 0;
		RepositoryConnection conn = rdf.getConnection();
		for (CodeURI cluster : map.codeSet())
		{
			// cluster title
			String clusterTitle = map.findLabel(cluster);
			clusterTitle = cleanLiteral(deQuote(clusterTitle));

			List<Statement> stmts =
					conn
							.getStatements(f.createURI(cluster.toString()), f.createURI(SKOS.MEMBER.getUri()), null, true)
							.asList();
			if (stmts.size() > 1)
			{
				html.println();
				html.println("<div class=\"cluster\"><div class=\"desc\">");
				html.println(" <i>Cluster title:</i> " + clusterTitle + "<br/></div>");

				// get cluster members
				html.println("<div class=\"imgs\">");
				String membersFormData = "";
				int membersShown = 0;
				for (Statement statement : stmts)
				{
					if (membersShown > 10)
					{
						html.println("<div class=\"img\"> <p class=\"more\">And more...</p> </div>");
						break;
					}
					CodeURI member = new CodeURI(statement.getObject().stringValue());

					// details
					String title = workTitles.findLabel(member);
					if (title != null)
						title = cleanLiteral(title);
					String date = workDates.findLabel(member);
					if (date != null)
						date = cleanLiteral(date);
					String creator = workCreators.findLabel(member);
					if (creator != null)
					{
						int lastSlash = creator.lastIndexOf("/");
						int lastHeck = creator.lastIndexOf("#");
						int last = lastHeck > lastSlash ? lastHeck : lastSlash;
						if (last >= 0)
							creator = creator.substring(last);
					}

					membersFormData += "<input type=\"hidden\" name=\"member\" value=\"" + member + "\"/>";

					html.println("<div class=\"img\" onclick='showLocalView(\"" + member + "\")'>" // <a
						// href=\""
						// +
						// images.get(member)
						// +
						// "\">"
						+ "<img title=\""
						+ member
						+ "\" src=\""
						+ ApprovalFormPairs.makeThumbUri(images.get(member.toString()), proxy)
						+ "\"/>"
						+ /* </a> */"</br>");
					html.println("<div class=\"title\">"
						+ (title == null ? "" : ("<i>t:</i> " + title + "</br>"))
						+ (creator == null ? "" : ("<i>c:</i> " + creator + "</br>"))
						+ (date == null ? "" : ("<i>d:</i> " + date + "</br>"))
						+ "</div></div>");
					membersShown++;
				}
				// stmts.close();
				html.println(" </div>"); // 
				html.println("<br class=\"clearboth\">");
				html.println(" <div class=\"desc\">"); // 

				// get existing relations
				RepositoryResult<Statement> stm =
						conn.getStatements(f.createURI(cluster.toString()), f.createURI(EVALUATION), null, true);
				String existingEvaluation = null;
				if (stm.hasNext())
					existingEvaluation = stm.next().getObject().stringValue();
				if (existingEvaluation == null)
					existingEvaluation = "inconsistent";
				stm.close();

				html.println(" <p class='noindent'>"
					+ "<form name =\"f_"
					+ i
					+ "\">"
					+ " <input type=\"hidden\" name=\"cluster\" value=\""
					+ cluster
					+ "\"/>"
					+ membersFormData
					+ "<i>Cluster is: </i><input type=\"radio\" name=\"relation\" value=\"consistent\" "
					+ ("consistent".equals(existingEvaluation) ? "checked=\"true\"" : "")
					+ "/>consistent  "
					+ "<input type=\"radio\" name=\"relation\" value=\"inconsistent\" "
					+ ("inconsistent".equals(existingEvaluation) ? "checked=\"true\"" : "")
					+ "/>inconsistent<br/>"
					+ "</form></p>");
				html.println(" </div>"); // 

				html.println(" </div>"); // cluster
				i++;
				// if (i>1000) break;
			}
		}

		html.println("<script type=\"text/javascript\">");
		html.println("function getCheckedValue(radioObj) { ");
		html.println("  if(!radioObj) ");
		html.println("  return ''; ");
		html.println("  var radioLength = radioObj.length; ");
		html.println("  if(radioLength == undefined) ");
		html.println("    if(radioObj.checked) ");
		html.println("      return radioObj.value; ");
		html.println("    else ");
		html.println("      return ''; ");
		html.println("  for(var i = 0; i < radioLength; i++) { ");
		html.println("    if(radioObj[i].checked) { ");
		html.println("      return radioObj[i].value; ");
		html.println("    } ");
		html.println("  } ");
		html.println("  return ''; ");
		html.println("} ");
		html.println("function saveAsRdf() {");
		html.println("var rdfHead = ");
		html.println("'<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\n' + ");
		html.println("'<rdf:RDF \\n' + ");
		html.println("'  xmlns:anno=\"" + Namespaces.ANNOCULTOR_CONVERTER + "\"\\n' + ");
		html.println("'  xmlns:owl=\"" + Namespaces.OWL + "\"\\n' + ");
		html.println("'  xmlns:rdf=\"" + Namespaces.RDF + "\"\\n' + ");
		html.println("'  xmlns:skos=\"" + Namespaces.SKOSCORE + "\"\\n' + ");
		html.println("'>\\n' + ");
		html.println("'<!-- \\n' + ");
		html.println("'USER: ' + document.getElementsByTagName('input')[0].value + '\\n' + ");
		html.println("'TASK: " + taskSignature + "\\n' + ");
		html.println("'DESCRIPTION: " + description + "\\n' + ");
		html.println("'DATE: ' + Date() + '\\n' + ");
		html.println("' -->\\n\\n';  ");
		// html.println("alert(rdfHead);");

		html.println("var body = '';");
		html.println("var forms = document.getElementsByTagName('form'); ");
		html.println("for(var i=0; i < forms.length; i++) ");
		html.println("{\n ");
		html.println("var form = forms[i]; ");
		html.println("var relation = getCheckedValue(form.elements['relation']);  ");
		html.println("body = body + '<skos:Collection rdf:about=\"' + form.elements[0].value + '\"> \\n';");
		html.println("  for(var j=1; j < form.elements.length - 2; j++) ");
		html.println("  {\n ");
		html
				.println("     body = body + '  <skos:member rdf:resource=\"' + form.elements[j].value + '\"/>\\n'; \n");
		html.println("  }\n ");
		html.println("  body = body + '  <anno:evaluation>' + relation + '</anno:evaluation>\\n' + ");
		html.println("                '</skos:Collection>\\n' ");
		html.println("}");
		// html.println("alert(body);");
		html.println("document.getElementById('rdf').value = rdfHead + body + '\\n </rdf:RDF> \\n';");
		html.println("document.getElementById('rdf').rows = forms.length + 30;");
		html.println("}");
		html.println("</script>");
		html.println("<div>");
		html
				.println("<input type=\"submit\" value=\"Generate RDF from opinions\" onClick=\"saveAsRdf()\"/> <br/> ");
		html
				.println("<textarea id=\"rdf\" rows=\"20\" cols=\"120\" value=\"Click the above button to see the RDF file here.\"></textarea>");
		html.println("</div>");
		html.println("</body></html>");
		html.close();
	}

	private static final Pattern sesameResultPattern = Pattern.compile("\"([^;]+)\"@(\\w\\w);");

	private static String cleanLiteral(String clusterTitle) throws Exception
	{

		Scanner sc = new Scanner(clusterTitle + ";");
		String s = null;
		String title = null;
		do
		{
			s = sc.findInLine(sesameResultPattern);
			if (s != null)
			{
				Matcher matcher = sesameResultPattern.matcher(s);
				if (title == null)
					title = matcher.replaceAll("$1<sub>$2</sub>");
				else
					title += matcher.replaceAll("; $1<sub>$2</sub>");
			}
		}
		while (s != null);
		sc.close();
		return (title == null ? clusterTitle : title);
	}

	private static String deQuote(String title)
	{
		if (title.startsWith("\"") && title.endsWith("\""))
			title = title.substring(1, title.length() - 1);
		return title;
	}

}

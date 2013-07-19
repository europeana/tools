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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import eu.annocultor.context.Environment;
import eu.annocultor.context.EnvironmentImpl;
import eu.annocultor.context.Namespaces;
import eu.annocultor.context.Concepts.RDF;
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
public class ApprovalFormPairs
{
	static String makeThumbUri(String imageUri, String proxy) throws Exception
	{
		if (imageUri == null)
			return "null";
		if (proxy == null)
			return imageUri;
		return proxy + URLEncoder.encode(imageUri, "UTF-8");
	}

	/**
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
		String taskSignature = args[1];
		String description = args[2];
		String proxy = args[3];
		if (proxy.equals("none"))
			proxy = null;
		if (description.contains("\'"))
			throw new Exception("Description should not contain character ' as it will conflict with the js");
		// loading file to approve
		VocabularyOfTerms map = new VocabularyOfTerms("toApprove", null);
		Environment environment = new EnvironmentImpl();
		map.loadTermsSeRQL(
				"SELECT X, Y FROM {X} <" + RDF.SAMEAS.getUri() + "> {Y}", 
				environment.getTmpDir(), 
				new File("."),
				fileToApprove.getCanonicalPath());

		// loading images
		Map<String, String> images = new HashMap<String, String>();
		String[] imageFiles = args[4].split(" ");
		for (String imageFile : imageFiles)
		{
			VocabularyOfTerms imagesThisFile = new VocabularyOfTerms("img", null);
			imagesThisFile.loadTermsSeRQL(
					"SELECT I, W FROM {I} <" + VRA.DEPICTS.getUri() + "> {W}",
					environment.getTmpDir(),
					new File("."),
					imageFile);
			// reverse to work -> image
			for (CodeURI key : imagesThisFile.codeSet())
			{
				images.put(imagesThisFile.findLabel(key), key.toString());
			}
		}

		// generating html
		PrintWriter html = new PrintWriter("approveForm_" + taskSignature + ".html");
		html.println("<html><head>");
		html
				.println("<link rel='stylesheet' type='text/css' href='http://www.cs.vu.nl/~borys/styles/mainstyle.css'/>");
		html
				.println("<link rel='stylesheet' type='text/css' href='http://annocultor.sourceforge.net/style.css'/>");
		html.println("</head><body>");
		html.println("<p id='title'>AnnoCultor</p>");
		html.println("<p>Task: " + taskSignature + "</p>");
		html.println("<p>" + description + "</p>");
		html.println("<p>User signature: <input name='userSignature'/></p>");
		html.println("<table border='0'><tr><th>X" + "</th><th>Relation</th><th>Y" + "</th></tr>");

		int i = 0;
		for (CodeURI X : map.codeSet())
		{
			String Y = map.findLabel(X);
			html.println("<tr><form name =\"f_" + i + "\">");
			html.println("<input type=\"hidden\" name=\"X\" value=\"" + X + "\"/>");
			html.println("<input type=\"hidden\" name=\"Y\" value=\"" + Y + "\"/>");
			html.println("<td><a href=\""
				+ images.get(X)
				+ "\">"
				+ "<img title=\""
				+ X
				+ "\" src=\""
				+ makeThumbUri(images.get(X), proxy)
				+ "\"/></a><br/>");
			html.println(// "<p class='noindent'>" + X + "</p>" +
					"</td>");
			html.println("<td><p class='noindent'>"
				+ "<input type=\"radio\" name=\"relation\" value=\"owl:sameAs\" checked=\"true\"/>sameAs<br/>"
				+ "<input type=\"radio\" name=\"relation\" value=\"ec:partOf\"/>isPartOf<br/>"
				+ "<input type=\"radio\" name=\"relation\" value=\"ec:hasPart\"/>hasPart<br/>"
				+ "<input type=\"radio\" name=\"relation\" value=\"related\"/>related<br/>"
				+ "<input type=\"radio\" name=\"relation\" value=\"unrelated\"/>unrelated"
				+ "</p></td>");
			html.println("<td><a href=\""
				+ images.get(Y)
				+ "\">"
				+ "<img title=\""
				+ Y
				+ "\" src=\""
				+ makeThumbUri(images.get(Y), proxy)
				+ "\"/></a><br/>");
			html.println(// "<p class='noindent'>" + Y + "</p>" +
					"</td>");
			html.println("</form></tr>");
			i++;
			// if (i>10) break;
		}
		html.println("</table>");

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
		html.println("'  xmlns:rdf=\"" + Namespaces.RDF + "\"\\n' + ");
		html.println("'  xmlns:owl=\"" + Namespaces.OWL + "\"\\n' + ");
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
		html.println("for(var i=0; i<forms.length; i++) ");
		html.println("{\n ");
		html.println("var relation = getCheckedValue(forms[i].elements['relation']);  ");
		html.println("body = body + '<rdf:Description rdf:about=\"' + ");
		html.println("            forms[i].elements[0].value + '\"> \\n' + ");
		html.println("            '  <' + relation + ");
		html.println("            ' rdf:resource=\"' + forms[i].elements[1].value + '\"/>\\n' + ");
		html.println("            '</rdf:Description>\\n' ");
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
}

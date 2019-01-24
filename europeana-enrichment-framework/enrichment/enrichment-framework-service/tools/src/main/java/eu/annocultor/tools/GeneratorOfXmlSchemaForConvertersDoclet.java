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
package eu.annocultor.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

import eu.annocultor.annotations.AnnoCultor;
import eu.annocultor.api.Rule;
import eu.annocultor.common.Utils;
import eu.annocultor.common.Language.Lang;
import eu.annocultor.context.Namespace;
import eu.annocultor.path.Path;
import eu.annocultor.tagger.vocabularies.VocabularyOfPeople;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.tagger.vocabularies.VocabularyOfTime;
import eu.annocultor.triple.Property;
import eu.annocultor.xconverter.api.Graph;
import eu.annocultor.xconverter.api.Resource;
import eu.annocultor.xconverter.impl.XConverterFactory;

public class GeneratorOfXmlSchemaForConvertersDoclet extends Doclet 
{

	public static final String XSD_TEXT_TO_REPLACED_WITH_GENERATED_XML_SIGNATURES =
		"AnnoCultor XML API add-on: This comment will be replaced with the generated signatures, DO NOT ALTER THIS LINE.";


	public static Set<String> getSuperClasses(ClassDoc cd)
	{
		Set<String> superClasses = new HashSet<String>();
		ClassDoc cls = cd;
		while (cls != null && cls.isClass())
		{
			ClassDoc superClass = cls.superclass();
			if (superClass != null)
			{
				if (superClasses.contains(superClass.qualifiedName()))
				{
					throw new RuntimeException("Cycle in superclass relation: " + Utils.show(superClasses, ", "));
				}
				superClasses.add(superClass.qualifiedName());
			}
			cls = superClass;
		}
		return superClasses;
	}

	public static boolean isMeantForXMLAccess(ConstructorDoc cd)
	{
		for (AnnotationDesc annotation : cd.annotations())
		{
			if ((AnnoCultor.class.getSimpleName() + "." + AnnoCultor.XConverter.class.getSimpleName())
					.equals(annotation.annotationType().name()))
			{
				for (ElementValuePair evp : annotation.elementValues())
				{
					if ("include".equals(evp.element().name()) && "true".equals(evp.value().toString()))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public static File destination = null;
	private static String ruleListenerDef;
	public static boolean start(RootDoc root) 
	{
		Logger log = Logger.getLogger("DocletGenerator");

		if (destination == null) 
		{			
			try
			{
				// loaded from RuleListenersFragment
				ruleListenerDef = IOUtils.toString(
						GeneratorOfXmlSchemaForConvertersDoclet.class.getResourceAsStream("/RuleListenersFragment.xsd"), 
						"UTF-8"
				);				

				String fn = System.getenv("annocultor.xconverter.destination.file.name");
				fn = (fn == null) ? "./../../../src/site/resources/schema/XConverterInclude.xsd" : fn;

				destination = new File(fn);
				if (destination.exists()) 
				{
					destination.delete();
				}

				// exceptions copy of the template and include
				FileOutputStream os;
				os = new FileOutputStream(new File(destination.getParentFile(), "XConverter.xsd"));
				IOUtils.copy(
						new AutoCloseInputStream(GeneratorOfXmlSchemaForConvertersDoclet.class.getResourceAsStream("/XConverterTemplate.xsd")), 
						os);
				os.close();
				os = new FileOutputStream(destination);
				IOUtils.copy(
						new AutoCloseInputStream(GeneratorOfXmlSchemaForConvertersDoclet.class.getResourceAsStream("/XConverterInclude.xsd")), 
						os);
				os.close();
			}
			catch (Exception e) 
			{
				try {
					throw new RuntimeException("On destination " + destination.getCanonicalPath(), e);
				} catch (IOException e1) {
					// too bad
					throw new RuntimeException(e1);
				}
			}
		}

		try
		{
			String s = Utils.loadFileToString(destination.getCanonicalPath(), "\n");
			int breakPoint = s.indexOf(XSD_TEXT_TO_REPLACED_WITH_GENERATED_XML_SIGNATURES);
			if (breakPoint < 0)
			{
				throw new Exception("Signature not found in XSD: " + XSD_TEXT_TO_REPLACED_WITH_GENERATED_XML_SIGNATURES);
			}
			String preambula = s.substring(0, breakPoint);
			String appendix = s.substring(breakPoint);

			destination.delete();

			PrintWriter schemaWriter = new PrintWriter(destination);
			schemaWriter.print(preambula);

			ClassDoc[] classes = root.classes();
			for (int i = 0; i < classes.length; ++i) 
			{
				ClassDoc cd = classes[i];
				PrintWriter documentationWriter = null;
				if (getSuperClasses(cd).contains(Rule.class.getName()))
				{					
					for (ConstructorDoc constructorDoc : cd.constructors())
					{
						if (constructorDoc.isPublic()) 
						{
							if (isMeantForXMLAccess(constructorDoc))
							{
								// dump APT doc
								if (documentationWriter == null)
								{
									// Create APT file and write the rule description
									File file = new File("./../../../src/site/xdoc/rules." + cd.name() + ".xml");
									documentationWriter = new PrintWriter(file);
									log.info("Generating doc for rule " + file.getCanonicalPath());
									printRuleDocStart(cd, documentationWriter);								
								}

								// create XSD 

								// check for the init() method
								boolean initFound = false;
								for (MethodDoc methodDoc : cd.methods())
								{
									if ("init".equals(methodDoc.name())) 
									{
										if (methodDoc.parameters().length == 0)
										{
											initFound = true;
											break;
										}
									}
								} 
								if (!initFound)
								{
									//									throw new Exception("Method init() is required. Please make sure NOW that it is called in constructor " + cd.name());
								}

								printConstructorSchema(constructorDoc, schemaWriter);

								if (documentationWriter != null) 
								{
									printConstructorDoc(constructorDoc, documentationWriter);
								}
							}
						}
					}
				}

				if (documentationWriter !=  null)
				{
					printRuleDocEnd(documentationWriter);
				}
			}

			schemaWriter.print(appendix);
			schemaWriter.close();
			log.info("Saved to " + destination.getCanonicalPath());
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		return true;
	}

	private abstract static class Formatter 
	{
		PrintWriter writer;

		public Formatter(PrintWriter writer) {
			super();
			this.writer = writer;
		}

		public abstract void formatElementStart(String name, String type, boolean array);
		public abstract void formatElementEnd(String name);
		public abstract void formatDocumentation(String doc);
	}

	private static void findType(
			ConstructorDoc constr, 
			Formatter formatter) 
	throws Exception
	{
		Map<String, String> paramTags = new HashMap<String, String>();

		for (ParamTag params : constr.paramTags())
		{
			paramTags.put(params.parameterName(), params.parameterComment());
		}

		boolean first = true;
		boolean array = false; 
		for (Parameter p : constr.parameters())
		{
			if (array)
			{
				throw new Exception("Error: something after a dimensioned parameter: " + constr.name() + "." + p.name());
			}
			if (p.type().dimension().length() > 0)
			{
				array = true;					
				//					throw new Exception("Error: Dimensioned parameter " + constr.name() + "." + p.name());
			}

			if (first)
			{
				/*				if (!"BatchRule".equals(constr.name()))
				{
					if (!Path.class.getName().equals(p.type().qualifiedTypeName()) || !p.name().equals("srcPath")) 
					{
						throw new Exception("To be published in XML, a rule should have the first parameter (Path srcPath, while found (" 
								+ p.type().qualifiedTypeName() + " " + p.name() + " in " + constr.name());
					}
				}
				 */		}

			boolean foundType = false;
			if (String.class.getName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:String", array);
				foundType = true;
			}

			if (Property.class.getName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:Property", array);
				foundType = true;
			}

			if (Resource.class.getName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:Resource", array);
				foundType = true;
			}

			if (Graph.class.getName().equals(p.type().qualifiedTypeName()))
			{					
				if (first && !p.name().startsWith("dstGraph"))
				{
					throw new Exception("First parameter of type Graph is expected to start with dstGraph, while found (" 
							+ p.type().qualifiedTypeName() + " " + p.name() + " in " + constr.name());
				}
				formatter.formatElementStart(p.name(), "ac:Graph", array);
				foundType = true;
			}

			if (Path.class.getName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:Path", array);
				foundType = true;
			}

			if (Lang.class.getCanonicalName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:Lang", array);
				foundType = true;
			}

			if (Namespace.class.getCanonicalName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:Namespace", array);
				foundType = true;
			}

			if (VocabularyOfTerms.class.getCanonicalName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:VocabularyOfTerms", array);
				foundType = true;
			}

			if (VocabularyOfPlaces.class.getCanonicalName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:VocabularyOfPlaces", array);
				foundType = true;
			}

			if (VocabularyOfPeople.class.getCanonicalName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:VocabularyOfPeople", array);
				foundType = true;
			}

			if (VocabularyOfTime.class.getCanonicalName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:VocabularyOfTime", array);
				foundType = true;
			}

			if (p.name().equals("rule"))
			{
				Class ruleClass = Class.forName(p.type().qualifiedTypeName());
				List superClasses = ClassUtils.getAllSuperclasses(ruleClass);
				if (!superClasses.contains(Rule.class))
				{
					throw new Exception("Error: Parameter 'rule' should be of class Rule o its subclasses, while found " + p.type().qualifiedTypeName());
				}
				formatter.formatElementStart(p.name(), "ac:Rule", array);
				foundType = true;
			}

			if (XConverterFactory.MapObjectToObject.class.getCanonicalName().equals(p.type().qualifiedTypeName()))
			{					
				formatter.formatElementStart(p.name(), "ac:Map", array);
				foundType = true;
			}
			if (paramTags.containsKey(p.name()))
			{
				formatter.formatDocumentation(paramTags.get(p.name()));
			}

			formatter.formatElementEnd(p.name());

			if (!foundType) 
			{
				throw new Exception("Error: parameter " + constr.name() + "." + p.name() + " of type " + p.typeName() + " that is not allowed in XML signatures");
			}
			first = false;
		}
	}

	static void printConstructorSchema(ConstructorDoc constr, PrintWriter out) 
	throws Exception
	{ 
		if(constr.modifierSpecifier() == Modifier.PUBLIC)
		{
			String affix = constr.annotations()[0].elementValues()[1].value().toString();
			affix = StringUtils.stripEnd(StringUtils.stripStart(affix, "\""), "\"");

			out.println("<!-- " + constr.qualifiedName()+constr.signature() + " -->");
			out.println("       <xs:element name=\"" + constr.qualifiedName() + "-" + affix + "\" substitutionGroup=\"ABSTRACT-RULE\" >");
			out.println("        <xs:complexType> ");
			out.println("         <xs:sequence>");

			findType(
					constr, 
					new Formatter(out) {

						@Override
						public void formatElementStart(String name, String type, boolean array) {
							writer.print( "           <xs:element name=\"" + name + "\" type=\"" + type + "\"" +
									(array ? " minOccurs=\"0\" maxOccurs=\"unbounded\" " : "")  + ">");
						}

						@Override
						public void formatDocumentation(String doc) {
							writer.print( "\n         <xs:annotation><xs:documentation xml:lang=\"en\">" 
									+ StringEscapeUtils.escapeXml(doc) + "</xs:documentation></xs:annotation>");
						}

						@Override
						public void formatElementEnd(String name) {
							writer.print(" \n            </xs:element>");
						}


					});
		}
		out.println(ruleListenerDef);
		out.println("      </xs:sequence>");		
		out.println("     </xs:complexType> ");
		out.println("    </xs:element>");		
	}

	static void printRuleDocStart(ClassDoc constr, PrintWriter out) 
	throws Exception
	{ 
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<document xmlns=\"http://maven.apache.org/XDOC/2.0\"");
		out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		out.println("  xsi:schemaLocation=\"http://maven.apache.org/XDOC/2.0 http://maven.apache.org/xsd/xdoc-2.0.xsd\">");
		out.println();
		out.println("  <properties>");
		out.println("    <title>Rule " + constr.name() + "</title>");
		out.println("  </properties>");
		out.println("  <body>");
		out.println("  <h1 id=\"Rule " + constr.name() + "\">Rule <code>" + constr.name() + "</code></h1>");
		out.println();
		out.println("  <p>" + constr.commentText() + "</p>");
		out.println();
		out.flush();


	}

	static void printRuleDocEnd(PrintWriter out) 
	throws Exception
	{ 
		out.println(" </body>");
		out.println("</document>");
		out.println();
		out.flush();
		out.close();
	}

	static void printConstructorDoc(ConstructorDoc constr, PrintWriter out) 
	throws Exception
	{ 
		if(constr.modifierSpecifier() == Modifier.PUBLIC)
		{
			String affix = constr.annotations()[0].elementValues()[1].value().toString();
			affix = StringUtils.stripEnd(StringUtils.stripStart(affix, "\""), "\"");
			out.println("  <h2 id=\"Constructor " + constr.name() + "-" + affix +
					"\">Constructor <code>" + affix + "</code></h2>");
			out.println();
			out.println(" <p>" + constr.commentText() + "</p>");
			out.println();
			out.println(" <p>Sample use:</p>");
			out.println("<div class=\"source\"><pre>");


			out.println(StringEscapeUtils.escapeHtml("<ac:" + constr.qualifiedName() + "-" + affix + ">"));
			findType(
					constr,  
					new Formatter(out) {

						@Override
						public void formatElementStart(String name, String type, boolean array) {
							writer.print(StringEscapeUtils.escapeHtml( "  <ac:" + name + " rdf:datatype=\"" + type + "\">"));
						}

						@Override
						public void formatDocumentation(String doc) {
							writer.print( " <i style=\"font-family: Times\">" +  StringEscapeUtils.escapeHtml(doc)  + "</i> ");
						}

						@Override
						public void formatElementEnd(String name) {
							writer.println(StringEscapeUtils.escapeHtml(  "</ac:" + name +">"));
						}


					}
			);

			out.println(StringEscapeUtils.escapeHtml("</ac:" + constr.qualifiedName() + "-" + affix + ">"));
			out.println("</pre></div>");
		}
		out.println();
		out.flush();
	}
}

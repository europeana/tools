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
package eu.annocultor.xconverter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlElementForVelocity
{

	private static final String NAMESPACE_PREFIX_SEPARATOR = ":";

	public XmlElementForVelocity(Element element, Map<String, String> namespaces)
	{
		super();
		this.element = element;
		this.namespaces = namespaces;
	}

	Map<String, String> namespaces;
	Element element;

	public XmlElementForVelocity getFirstChild(String tagName)
	{
		XmlElementForVelocity[] children = getChildren(tagName);
		if (children.length == 0)
			return null;
		return 
		(		children[0].getChildren("*").length == 0
				&& (children[0].getValue() == null || children[0].getValue().isEmpty()))
				? null : children[0];
	}

	public XmlElementForVelocity[] getChildren(String tagName)
	{
		List<XmlElementForVelocity> result = new ArrayList<XmlElementForVelocity>();
		NodeList children = element.getChildNodes();
		for (int i = 0;  i < children.getLength(); i++)
		{
			Node node = children.item(i);
			// expanded entity references
//			if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE)
//			{
//				Node entityNode = node.getFirstChild();
//				if (entityNode != null) 
//				{
//					result.addAll(new XmlElementForVelocity(entityNode).getChildren(tagName));
//				}
//			}
			if (node.getNodeType() == Node.ELEMENT_NODE 
					&& 
					(tagName.equals("*") || tagName.equals(node.getNodeName())))
			{
				result.add(new XmlElementForVelocity((Element)node, namespaces));
			}
		}
		return result.toArray(new XmlElementForVelocity[]{});
	}

	public String getXsiType()
	{
		return element.getAttribute("xsi:type");
	}

	public String getValue()
	{
		//		if (element.getFirstChild() != null && element.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE)
		//		{
		//			System.out.println("hi");
		//		}
		if (element.getFirstChild() != null && 
				(element.getFirstChild().getNodeType() == Node.TEXT_NODE 
						|| element.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE)) 
		{
			String value = element.getFirstChild().getNodeValue();
			if (value != null)
				value = value.trim();
			return value;
		}		
		return null;
	}


	public String getName()
	{
		return element.getNodeName();
	}

	public String getAttribute(String attName)
	throws Exception
	{
		NST nst = new NST(attName);
		return element.getAttributeNS(nst.nsURI, nst.localName).isEmpty() ? null : element.getAttribute(attName);
	}

	private class NST 
	{

		public NST(String tag)
		throws Exception
		{
			this.nsURI = "";
			this.localName = tag;

			String[] p = tag.split(":");
			if (p.length > 2)
			{
				throw new Exception("Error: Multiple ':' at " + tag);
			}
			if (p.length == 2)
			{
				// ns:tag
				if (namespaces.containsKey(p[0]))
				{
					nsURI = namespaces.get(p[0]);
					localName = p[1];
				}
				else
				{
					throw new Exception("Nick " + p[0] + " not found");
				}
			}
		}

		String nsURI;
		String localName;

	}

	@Override
	public String toString()
	{
		if (element == null)
			return "NULL";
		return "<< " + element.getTextContent() + " >>";
	}

	public static String removeNamespacePrefix(String value)
	throws Exception
	{
		return StringUtils.substringAfter(value, NAMESPACE_PREFIX_SEPARATOR);
	}

	public static String removeAffix(String value, String affixChar)
	throws Exception
	{
		return StringUtils.substringBefore(value, affixChar);
	}

	/**
	 * Java-enquoting of multi-line String values.
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String enquote(String value)
	throws Exception
	{
		if (value == null)
			return null;
		return "\"" + value.replaceAll("\\n", "\"\n + \"") + "\"";
	}

	/**
	 * Wrap-up on Apache Commons Lang  StringEscapeUtils escapeJava().
	 * @param text
	 * @return
	 * 
	 * @see StringEscapeUtils#escapeJava(String)
	 */
	public static String escapeJava(String text)
	{
		// with a fix on escapeJava in Lang 2.4 mistakenly escapes / 
		return StringEscapeUtils.escapeJava(text).replace("\\/", "/");
	}
}

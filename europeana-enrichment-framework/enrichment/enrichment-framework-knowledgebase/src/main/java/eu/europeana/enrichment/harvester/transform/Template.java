package eu.europeana.enrichment.harvester.transform;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.solr.entity.ContextualClassImpl;

public abstract class Template<S extends ContextualClassImpl> {

	private static final Logger log = Logger.getLogger(Template.class
			.getCanonicalName());
	private static final String RDF_RESOURCE = "rdf:resource";
	private static final String XML_LANG = "xml:lang";
	private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
	private static final String RDF_NAMESPACE ="http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	private void appendValue(S obj, String methodName, Object value) {
		if (value.getClass().isAssignableFrom(String.class)) {
			obj.setAbout((String) value);
		} else {
			Class type = null;
			Method found = null;

			Method getter;
			try {
				getter = obj.getClass().getMethod(
						StringUtils.replace(methodName, "set", "get"));

				for (Method method : obj.getClass().getMethods()) {
					if (StringUtils.equals(method.getName(), methodName)) {
						type = method.getParameterTypes()[0];
						found = method;
					}
				}
				if (type.isAssignableFrom(String[].class)) {
					String[] vals = (String[]) getter.invoke(obj);
					if (vals == null) {
						vals = new String[1];
						vals[0] = ((AttributeHolder) value).getAttributeValue();
						found.invoke(obj, (Object)vals);

					} else {
						List<String> strs = new ArrayList<>(Arrays.asList(vals));
						strs.add(((AttributeHolder) value).getAttributeValue());
						String[] finalArray = strs.toArray(new String[strs.size()]);
						found.invoke(obj, (Object) finalArray);
					}
				}
				if (type.isAssignableFrom(Map.class)) {
					Map<String, List<String>> vals = (Map<String, List<String>>) getter
							.invoke(obj);
					AttributeHolder attr = (AttributeHolder) value;
					if (attr.getAttributeName() != null
							&& attr.getAttributeName().equals(RDF_RESOURCE)) {
						if (vals == null) {
							vals = new HashMap<>();
							List<String> str = new ArrayList<>();
							str.add(attr.attributeValue);
							vals.put("def", str);
						} else {
							List<String> str = new ArrayList<>();
							if (vals.containsKey("def")) {
								str = vals.get("def");
							}
							str.add(attr.attributeValue);
							vals.put("def", str);
						}
					} else {
						if (attr.getAttributeName() == null) {
							if (vals == null) {
								vals = new HashMap<>();
								List<String> str = new ArrayList<>();
								str.add(attr.elementValue);
								vals.put("def", str);
							} else {
								List<String> str = new ArrayList<>();
								if (vals.containsKey("def")) {
									str = vals.get("def");
								}
								str.add(attr.elementValue);
								vals.put("def", str);
							}
						} else {
							if (vals == null) {
								vals = new HashMap<>();
								List<String> str = new ArrayList<>();
								str.add(attr.elementValue);
								vals.put(attr.attributeValue, str);
							} else {
								List<String> str = new ArrayList<>();
								if (vals.containsKey(attr.attributeValue)) {
									str = vals.get(attr.attributeValue);
								}
								str.add(attr.elementValue);
								vals.put(attr.attributeValue, str);
							}
						}
					}
					found.invoke(obj, vals);
				}
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				log.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	protected S parse(S obj, String resourceUri, String xml,
			Map<String, String> methodMapping) {
		InputStream in = new ByteArrayInputStream(xml.getBytes());
		try {
			XMLEventReader eventReader = XMLInputFactory.newInstance()
					.createXMLEventReader(in);
			boolean isRoot = true;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					String qElem = startElement.getName().getPrefix() + ":"
							+ startElement.getName().getLocalPart();
					if (methodMapping.containsKey(qElem)) {
						if (isRoot) {
							appendValue(obj, methodMapping.get(qElem),
									resourceUri);
							isRoot = !isRoot;
							continue;
						} else {
							
							Iterator attributes = startElement.getAttributes();
							
							
							Attribute attr = startElement
									.getAttributeByName(new QName(RDF_NAMESPACE,
											"resource"));
							Attribute langAttr = startElement
									.getAttributeByName(new QName(XML_NAMESPACE,"lang"));
							Attribute about = startElement
									.getAttributeByName(new QName(RDF_NAMESPACE,
											"about"));
							if (attr != null) {
								AttributeHolder attribute = new AttributeHolder();
								attribute.setAttributeName(RDF_RESOURCE);
								attribute.setAttributeValue(attr.getValue());
								appendValue(obj, methodMapping.get(qElem),
										attribute);
								continue;
							} else {
								if (about == null) {
									AttributeHolder attribute = new AttributeHolder();
									if (langAttr != null) {
										attribute.setAttributeName(XML_LANG);
										attribute.setAttributeValue(langAttr
												.getValue());
									}
									event = eventReader.nextEvent();
									if (!event.isEndElement()) {
										attribute.setElementValue(event
												.asCharacters().getData());
										appendValue(obj, methodMapping.get(qElem),
												attribute);
									}
								}
								continue;
							}
						}
					}

				}
			}
			return obj;
		} catch (XMLStreamException | FactoryConfigurationError e) {
			log.log(Level.SEVERE, e.getMessage());
		}
		return null;
	}

	class AttributeHolder {

		private String attributeName;
		private String attributeValue;
		private String elementValue;

		public String getAttributeName() {
			return attributeName;
		}

		public void setAttributeName(String attributeName) {
			this.attributeName = attributeName;
		}

		public String getAttributeValue() {
			return attributeValue;
		}

		public void setAttributeValue(String attributeValue) {
			this.attributeValue = attributeValue;
		}

		public String getElementValue() {
			return elementValue;
		}

		public void setElementValue(String elementValue) {
			this.elementValue = elementValue;
		}

	}
}

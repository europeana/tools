/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.mapper;

import eu.europeana.neo4j.model.Hierarchy;
import java.util.Iterator;
import java.util.List;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.neo4j.graphdb.Node;

/**
 *
 * @author gmamakis
 */
public class ObjectMapper {

    public String toJson(Hierarchy hierarchy) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        ArrayNode parents = json.arrayNode();
        for (Node parentNode : hierarchy.getParents()) {
            ObjectNode parent = JsonNodeFactory.instance.objectNode();

            Iterator<String> parentProperties = parentNode.getPropertyKeys().iterator();
            while (parentProperties.hasNext()) {
                String key = parentProperties.next();
                Object obj = parentNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    parent.put(key, (String) parentNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    parent.put(key, (Boolean) parentNode.getProperty(key));
                } 
                else if (obj.getClass().isAssignableFrom(Long.class)) {
                    parent.put(key, (long) parentNode.getProperty(key));
                }else {
                    ArrayNode parentArray = parent.arrayNode();
                    String[] props = (String[]) parentNode.getProperty(key);
                    for (String str : props) {
                        parentArray.add(str);
                    }
                    parent.put(key, parentArray);
                }
            }
            parents.add(parent);
        }
        json.put("parents", parents);
        synchronized(this){
        ArrayNode siblings = json.arrayNode();
        for (Node siblingNode : hierarchy.getSiblings()) {
            ObjectNode sibling = JsonNodeFactory.instance.objectNode();

            Iterator<String> siblingProperties = siblingNode.getPropertyKeys().iterator();
            while (siblingProperties.hasNext()) {
                String key = siblingProperties.next();
                Object obj = siblingNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    sibling.put(key, (String) siblingNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    sibling.put(key, (Boolean) siblingNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    sibling.put(key, (long) siblingNode.getProperty(key));
                }else {
                    ArrayNode parentArray = sibling.arrayNode();
                    String[] props = (String[]) siblingNode.getProperty(key);
                    for (String str : props) {
                        parentArray.add(str);
                    }
                    sibling.put(key, parentArray);
                }
            }
            siblings.add(sibling);
        }
        json.put("followingSiblings", siblings);
        }
        ArrayNode siblingsBefore = json.arrayNode();
        for (Node siblingNode : hierarchy.getPreviousSiblings()) {
            ObjectNode siblingBefore = JsonNodeFactory.instance.objectNode();

            Iterator<String> siblingProperties = siblingNode.getPropertyKeys().iterator();
            while (siblingProperties.hasNext()) {
                String key = siblingProperties.next();
                Object obj = siblingNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    siblingBefore.put(key, (String) siblingNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    siblingBefore.put(key, (Boolean) siblingNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    siblingBefore.put(key, (long) siblingNode.getProperty(key));
                }else {
                    ArrayNode parentArray = siblingBefore.arrayNode();
                    String[] props = (String[]) siblingNode.getProperty(key);
                    for (String str : props) {
                        parentArray.add(str);
                    }
                    siblingBefore.put(key, parentArray);
                }
            }
            siblingsBefore.add(siblingBefore);
        }
        json.put("precedingSiblings", siblingsBefore);
        return json.toString();
    }
    
    
    public String siblingsToJson(List<Node> siblingsList, String title) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        ArrayNode siblings = json.arrayNode();
        for (Node siblingNode : siblingsList) {
            ObjectNode sibling = JsonNodeFactory.instance.objectNode();

            Iterator<String> siblingProperties = siblingNode.getPropertyKeys().iterator();
            while (siblingProperties.hasNext()) {
                String key = siblingProperties.next();
                Object obj = siblingNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    sibling.put(key, (String) siblingNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    sibling.put(key, (Boolean) siblingNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    sibling.put(key, (long) siblingNode.getProperty(key));
                } else {
                    ArrayNode multiPropArray = sibling.arrayNode();
                    String[] props = (String[]) siblingNode.getProperty(key);
                    for (String str : props) {
                        multiPropArray.add(str);
                    }
                    sibling.put(key, multiPropArray);
                }
            }
            siblings.add(sibling);
        }
        json.put(title, siblings);
        return json.toString();
    }
    
}

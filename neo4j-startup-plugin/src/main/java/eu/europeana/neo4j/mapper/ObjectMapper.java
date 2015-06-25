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
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    parent.put(key, (long) parentNode.getProperty(key));
                } else {
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
        ArrayNode fSiblings = json.arrayNode();
        for (Node fsNode : hierarchy.getFollowingSiblings()) {
            ObjectNode fSibling = JsonNodeFactory.instance.objectNode();

            Iterator<String> fsProperties = fsNode.getPropertyKeys().iterator();
            while (fsProperties.hasNext()) {
                String key = fsProperties.next();
                Object obj = fsNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    fSibling.put(key, (String) fsNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    fSibling.put(key, (Boolean) fsNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    fSibling.put(key, (long) fsNode.getProperty(key));
                } else {
                    ArrayNode fsArray = fSibling.arrayNode();
                    String[] props = (String[]) fsNode.getProperty(key);
                    for (String str : props) {
                        fsArray.add(str);
                    }
                    fSibling.put(key, fsArray);
                }
            }
            fSiblings.add(fSibling);
        }
        json.put("followingSiblings", fSiblings);

        ArrayNode pSiblings = json.arrayNode();
        for (Node psNode : hierarchy.getPreviousSiblings()) {
            ObjectNode pSibling = JsonNodeFactory.instance.objectNode();

            Iterator<String> psProperties = psNode.getPropertyKeys().iterator();
            while (psProperties.hasNext()) {
                String key = psProperties.next();
                Object obj = psNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    pSibling.put(key, (String) psNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    pSibling.put(key, (Boolean) psNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    pSibling.put(key, (long) psNode.getProperty(key));
                } else {
                    ArrayNode psArray = pSibling.arrayNode();
                    String[] props = (String[]) psNode.getProperty(key);
                    for (String str : props) {
                        psArray.add(str);
                    }
                    pSibling.put(key, psArray);
                }
            }
            pSiblings.add(pSibling);
        }
        json.put("precedingSiblings", pSiblings);

        ArrayNode fsChildren = json.arrayNode();
        for (Node fsChildrenNode : hierarchy.getFollowingSiblingChildren()) {
            ObjectNode fsChild = JsonNodeFactory.instance.objectNode();

            Iterator<String> fsChildProperties = fsChildrenNode.getPropertyKeys().iterator();
            while (fsChildProperties.hasNext()) {
                String key = fsChildProperties.next();
                Object obj = fsChildrenNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    fsChild.put(key, (String) fsChildrenNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    fsChild.put(key, (Boolean) fsChildrenNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    fsChild.put(key, (long) fsChildrenNode.getProperty(key));
                } else {
                    ArrayNode fsChildArray = fsChild.arrayNode();
                    String[] props = (String[]) fsChildrenNode.getProperty(key);
                    for (String str : props) {
                        fsChildArray.add(str);
                    }
                    fsChild.put(key, fsChildArray);
                }
            }
            fsChildren.add(fsChild);
        }
        json.put("followingSiblingChildren", fsChildren);

        ArrayNode psChildren = json.arrayNode();
        for (Node psChildrenNode : hierarchy.getPreviousSiblingChildren()) {
            ObjectNode psChild = JsonNodeFactory.instance.objectNode();

            Iterator<String> psChildProperties = psChildrenNode.getPropertyKeys().iterator();
            while (psChildProperties.hasNext()) {
                String key = psChildProperties.next();
                Object obj = psChildrenNode.getProperty(key);
                if (obj.getClass().isAssignableFrom(String.class)) {
                    psChild.put(key, (String) psChildrenNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Boolean.class)) {
                    psChild.put(key, (Boolean) psChildrenNode.getProperty(key));
                } else if (obj.getClass().isAssignableFrom(Long.class)) {
                    psChild.put(key, (long) psChildrenNode.getProperty(key));
                } else {
                    ArrayNode psChildArray = psChild.arrayNode();
                    String[] props = (String[]) psChildrenNode.getProperty(key);
                    for (String str : props) {
                        psChildArray.add(str);
                    }
                    psChild.put(key, psChildArray);
                }
            }
            psChildren.add(psChild);
        }
        json.put("precedingSiblingChildren", psChildren);

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

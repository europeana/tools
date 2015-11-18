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
 * @author gmamakis, luthien
 */
public class ObjectMapper {

    public String toJson(Hierarchy hierarchy) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("parents", assimilate(hierarchy.getParents(), json.arrayNode()));
        json.put("followingSiblings", assimilate(hierarchy.getFollowingSiblings(), json.arrayNode()));
        json.put("precedingSiblings", assimilate(hierarchy.getPreviousSiblings(), json.arrayNode()));
        json.put("followingSiblingChildren", assimilate(hierarchy.getFollowingSiblingChildren(), json.arrayNode()));
        json.put("precedingSiblingChildren", assimilate(hierarchy.getPreviousSiblingChildren(), json.arrayNode()));
        return json.toString();
    }

    public String siblingsToJson(List<Node> siblingsList, String title) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put(title, assimilate(siblingsList, json.arrayNode()));
        return json.toString();
    }

    private ArrayNode assimilate(List<Node> brats, ArrayNode group) {
        for (Node brat : brats) {
            ObjectNode       individual = JsonNodeFactory.instance.objectNode();
            Iterator<String> character  = brat.getPropertyKeys().iterator();
            while (character.hasNext()) {
                String trait  = character.next();
                Object nature = brat.getProperty(trait);
                if (nature.getClass().isAssignableFrom(String.class)) {
                    individual.put(trait, (String) brat.getProperty(trait));
                } else if (nature.getClass().isAssignableFrom(Boolean.class)) {
                    individual.put(trait, (Boolean) brat.getProperty(trait));
                } else if (nature.getClass().isAssignableFrom(Long.class)) {
                    individual.put(trait, (long) brat.getProperty(trait));
                } else {
                    ArrayNode spunk  = individual.arrayNode();
                    String[]  spiwit = (String[]) brat.getProperty(trait);
                    for (String pluck : spiwit) {
                        spunk.add(pluck);
                    }
                    individual.put(trait, spunk);
                }
            }
            group.add(individual);
        }
        return group;
    }
}

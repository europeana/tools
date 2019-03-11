/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.utils;

import eu.europeana.neo4j.model.Family;

import java.util.Iterator;
import java.util.List;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.neo4j.graphdb.Node;

/**
 *
 * @author gmamakis, luthien
 */
public class FamilyTherapist {

    public String toJson(Family family) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.set("parents", assimilate(family.getParents(), json.arrayNode()));
        json.set("followingSiblings", assimilate(family.getFollowingSiblings(), json.arrayNode()));
        json.set("precedingSiblings", assimilate(family.getPrecedingSiblings(), json.arrayNode()));
        json.set("followingSiblingChildren", assimilate(family.getFollowingSiblingChildren(), json.arrayNode()));
        json.set("precedingSiblingChildren", assimilate(family.getPrecedingSiblingChildren(), json.arrayNode()));
        return json.toString();
    }

    public String siblingsToJson(List<Node> siblingsList, String title) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.set(title, assimilate(siblingsList, json.arrayNode()));
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

    public static String fixSlashes(String rdfAbout){
        rdfAbout.replace("%2F", "/");
        if (rdfAbout.contains("/") && !rdfAbout.startsWith("/")){
            rdfAbout = "/" + rdfAbout;
        }
        return rdfAbout;
    }

    public static String error2Json(String errMessage){
        return JsonNodeFactory.instance.textNode(errMessage).toString();
    }
}

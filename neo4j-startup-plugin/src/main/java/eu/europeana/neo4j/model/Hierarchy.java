/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.neo4j.model;

import java.util.ArrayList;
import java.util.List;
import org.neo4j.graphdb.Node;

/**
 *
 * @author gmamakis, luthien
 */

public class Hierarchy {
    
    List<Node> parents = new ArrayList<>();
    List<Node> followingSiblings = new ArrayList<>();
    List<Node> precedingSiblings = new ArrayList<>();
    List<Node> followingSiblingChildren = new ArrayList<>();
    List<Node> precedingSiblingChildren = new ArrayList<>();

    public List<Node> getParents() {
        return parents;
    }

    public void setParents(List<Node> parents) {
        this.parents = parents;
    }

    public List<Node> getFollowingSiblings() {
        return followingSiblings;
    }

    public void setFollowingSiblings(List<Node> followingSiblings) {
        this.followingSiblings = followingSiblings;
    }

    public List<Node> getPrecedingSiblings() {
        return precedingSiblings;
    }

    public void setPrecedingSiblings(List<Node> precedingSiblings) {
        this.precedingSiblings = precedingSiblings;
    }

    public List<Node> getFollowingSiblingChildren() {
        return followingSiblingChildren;
    }

    public void setFollowingSiblingChildren(List<Node> followingSiblingChildren) {
        this.followingSiblingChildren = followingSiblingChildren;
    }

    public List<Node> getPrecedingSiblingChildren() {
        return precedingSiblingChildren;
    }

    public void setPrecedingSiblingChildren(List<Node> precedingSiblingChildren) {
        this.precedingSiblingChildren = precedingSiblingChildren;
    }
    
    
}

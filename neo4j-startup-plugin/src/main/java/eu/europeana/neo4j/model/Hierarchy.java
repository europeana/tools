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
 * @author gmamakis
 */

public class Hierarchy {
    
    List<Node> parents = new ArrayList<>();
    List<Node> followingSiblings = new ArrayList<>();
    List<Node> previousSiblings = new ArrayList<>();
    List<Node> followingSiblingChildren = new ArrayList<>();
    List<Node> previousSiblingChildren = new ArrayList<>();

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

    public List<Node> getPreviousSiblings() {
        return previousSiblings;
    }

    public void setPreviousSiblings(List<Node> previousSiblings) {
        this.previousSiblings = previousSiblings;
    }

    public List<Node> getFollowingSiblingChildren() {
        return followingSiblingChildren;
    }

    public void setFollowingSiblingChildren(List<Node> followingSiblingChildren) {
        this.followingSiblingChildren = followingSiblingChildren;
    }

    public List<Node> getPreviousSiblingChildren() {
        return previousSiblingChildren;
    }

    public void setPreviousSiblingChildren(List<Node> previousSiblingChildren) {
        this.previousSiblingChildren = previousSiblingChildren;
    }
    
    
}

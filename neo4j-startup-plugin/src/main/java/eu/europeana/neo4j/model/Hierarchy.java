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
    List<Node> siblings = new ArrayList<>();
    List<Node> previousSiblings = new ArrayList<>();

    public List<Node> getParents() {
        return parents;
    }

    public void setParents(List<Node> parents) {
        this.parents = parents;
    }

    public List<Node> getSiblings() {
        return siblings;
    }

    public void setSiblings(List<Node> siblings) {
        this.siblings = siblings;
    }

    public List<Node> getPreviousSiblings() {
        return previousSiblings;
    }

    public void setPreviousSiblings(List<Node> previousSiblings) {
        this.previousSiblings = previousSiblings;
    }
    
    
}

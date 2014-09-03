/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.neo4j.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.neo4j.graphdb.Node;

/**
 *
 * @author gmamakis
 */
@XmlRootElement
public class Hierarchy {
    
    List<Node> parents = new ArrayList<>();
    List<Node> siblings = new ArrayList<>();

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
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.initial;

import eu.europeana.neo4j.mapper.ObjectMapper;
import eu.europeana.neo4j.model.Hierarchy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

/**
 *
 * @author gmamakis, luthien
 */
@javax.ws.rs.Path("/startup")
public class Startup {

    final DynamicRelationshipType ISNEXTINSEQUENCE = DynamicRelationshipType.withName("edm:isNextInSequence");
    final DynamicRelationshipType ISFAKEORDER = DynamicRelationshipType.withName("isFakeOrder");

    private GraphDatabaseService db;

    public Startup(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response hierarchy(@PathParam("nodeId") String nodeId,
                              @QueryParam("length") @DefaultValue("32") int length,
                              @QueryParam("lengthBefore") @DefaultValue("8") int lengthBefore) {
        Hierarchy hierarchy = new Hierarchy();
        List<Node> parents = new ArrayList<>();
        Transaction tx = db.beginTx();
        try {
            Node node = db.index().forNodes("edmsearch2").get("rdf_about", nodeId).getSingle();
            if (node.hasProperty("hasChildren")) {
                long childrenCount = getChildrenCount(node.getProperty("rdf:about").toString());
                node.setProperty("childrenCount", childrenCount);
            }
            if (node.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                node.setProperty("relBefore", false);
            } else if (node.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                node.setProperty("relBefore", true);
            }

            parents.add(node);
            Node testNode = node;
            while (testNode.hasProperty("hasParent")) {
                Node newNode = db.index().forNodes("edmsearch2").get("rdf_about", testNode.getProperty("hasParent")).
                        getSingle();
                long childrenCount = getChildrenCount(newNode.getProperty("rdf:about").toString());
                newNode.setProperty("childrenCount", childrenCount);
                if (newNode.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                    newNode.setProperty("relBefore", false);
                } else if (newNode.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                    newNode.setProperty("relBefore", true);
                }
                parents.add(newNode);
                testNode = newNode;
            }
            hierarchy.setParents(parents);
            
            List<Node> precedingSiblings = new ArrayList<>();
            List<Node> precedingSiblingChildren = new ArrayList<>();
            TraversalDescription traversal = db.traversalDescription();
            Traverser traverse = traversal
                    .depthFirst()
                    .relationships(ISNEXTINSEQUENCE, Direction.OUTGOING)
                    .relationships(ISFAKEORDER, Direction.OUTGOING)
                    .evaluator(Evaluators.toDepth(length))
                    .evaluator(Evaluators.excludeStartPosition())
                    .traverse(node);
            for (Path path : traverse) {
                Node endNode = path.endNode();
                if (endNode.hasProperty("hasChildren")) {
                    long childrenCount = getChildrenCount(endNode.getProperty("rdf:about").toString());
                    endNode.setProperty("childrenCount", childrenCount);
                    precedingSiblingChildren.add(getFirstChild(endNode.getProperty("rdf:about").toString()));
                }
                
                if (endNode.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                    endNode.setProperty("relBefore", false);
                } else if (endNode.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                    endNode.setProperty("relBefore", true);
                }
                precedingSiblings.add(path.endNode());
            }
            hierarchy.setPrecedingSiblings(precedingSiblings);
            hierarchy.setPrecedingSiblingChildren(precedingSiblingChildren);
            
            List<Node> followingSiblings = new ArrayList<>();
            List<Node> followingSiblingChildren = new ArrayList<>();
            TraversalDescription traversalBefore = db.traversalDescription();
            Traverser traverseBefore = traversalBefore
                    .depthFirst()
                    .relationships(ISNEXTINSEQUENCE, Direction.INCOMING)
                    .relationships(ISFAKEORDER, Direction.INCOMING)
                    .evaluator(Evaluators.toDepth(lengthBefore))
                    .evaluator(Evaluators.excludeStartPosition())
                    .traverse(node);

            for (Path path : traverseBefore) {
                Node endNode = path.endNode();
                if (endNode.hasProperty("hasChildren")) {
                    long childrenCount = getChildrenCount(endNode.getProperty("rdf:about").toString());
                    endNode.setProperty("childrenCount", childrenCount);
                    followingSiblingChildren.add(getFirstChild(endNode.getProperty("rdf:about").toString()));
                }
                followingSiblings.add(endNode);
                if (endNode.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                    endNode.setProperty("relBefore", false);
                } else if (endNode.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                    endNode.setProperty("relBefore", true);
                }
            }
            hierarchy.setFollowingSiblings(followingSiblings);
            hierarchy.setFollowingSiblingChildren(followingSiblingChildren);

        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }
        String obj = new ObjectMapper().toJson(hierarchy);
        tx.success();
        return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
                "application/json").build();
    }
    
    private synchronized long getIndex(long nodeId) {
        long maxLength = 0;
        try {
            Node startNode = db.getNodeById(nodeId);
            TraversalDescription traversal = db.traversalDescription();
            Traverser traverse = traversal
                    .depthFirst()
                    .relationships(ISNEXTINSEQUENCE, Direction.OUTGOING)
                    .relationships(ISFAKEORDER, Direction.OUTGOING)
                    .traverse(startNode);

            for (Path path : traverse) {
                if (path.length() > maxLength) {
                    maxLength = path.length();
                }
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }
        return maxLength;
    }

    private long getChildrenCount(String rdfAbout) {
        Transaction tx = db.beginTx();
        Result result = db.execute(
                "start n = node:edmsearch2(rdf_about=\"" + rdfAbout
                + "\") MATCH (n)-[:`dcterms:hasPart`]->(part) RETURN COUNT(part) as children");
        Iterator<Long> columns = result.columnAs("children");
        tx.success();
        return columns.next();
    }
    
    private Node getFirstChild(String rdfAbout) {
        Transaction tx = db.beginTx();
        Result result = db.execute(
                "start parent = node:edmsearch2(rdf_about=\"" + rdfAbout 
                + "\") MATCH (parent)-[:`dcterms:hasPart`]->(firstchild) "
                + "WHERE NOT ()-[:isFakeOrder]->(firstchild) "
                + "AND NOT ()-[:`edm:isNextInSequence`]->(firstchild) "
                + "RETURN firstchild LIMIT 1;");
        Iterator<Node> firstChild = result.columnAs("firstchild");
        tx.success();
        return firstChild.next();
    }
}

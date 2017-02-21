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
import java.util.logging.LogRecord;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.impl.util.StringLogger;

/**
 *
 * @author gmamakis, luthien
 */
@javax.ws.rs.Path("/startup")
public class Startup {

    private static final RelationshipType ISNEXTINSEQUENCE = DynamicRelationshipType.withName("edm:isNextInSequence");
    private static final RelationshipType ISFAKEORDER = DynamicRelationshipType.withName("isFakeOrder");
    private static final RelationshipType HAS_PART = DynamicRelationshipType.withName("dcterms:hasPart");

    private GraphDatabaseService db;
    private ExecutionEngine engine;

    public Startup(@Context GraphDatabaseService db) {
        this.db = db;
        this.engine = new ExecutionEngine(db, StringLogger.SYSTEM);
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response hierarchy(@PathParam("nodeId") String nodeId,
                              @QueryParam("length") @DefaultValue("32") int length,
                              @QueryParam("lengthBefore") @DefaultValue("8") int lengthBefore) {
        String rdfAbout = ObjectMapper.fixSlashes(nodeId);
        Hierarchy hierarchy = new Hierarchy();
        List<Node> parents = new ArrayList<>();
        String obj;
        try ( Transaction tx = db.beginTx() ) {
            Node node = db.index().forNodes("edmsearch2").get("rdf_about", rdfAbout).getSingle();

            if (node.hasProperty("hasChildren")) {
                long childrenCount = getChildrenCount(node.getProperty("rdf:about").toString());
                if (childrenCount > 0) {
                    node.setProperty("childrenCount", childrenCount);
                } else {
                    throw new Neo4jDataConsistencyException("Inconsistency found between node's hasChildren property and actual unique children", node.getProperty("rdf:about").toString());
                }
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

            obj = new ObjectMapper().toJson(hierarchy);
            tx.success();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();

        } catch (Neo4jDataConsistencyException ne) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE,
                    ne.getRdfAbout() + "\n" + ne.getCause() + "\n" + ne.getMessage());
            obj = error2Json("INCONSISTENT_DATA");
            return Response.status(502).entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE,
                    e.getCause() + "\n" + e.getMessage());
            obj = error2Json("ERROR");
            return Response.status(500).entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();
        }

    }

    private String error2Json(String errMessage){
        return JsonNodeFactory.instance.textNode(errMessage).toString();
    }

    private long getChildrenCount(String rdfAbout) {
        rdfAbout = ObjectMapper.fixSlashes(rdfAbout);
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index      = db.index();
            Index<Node>     edmsearch2 = index.forNodes("edmsearch2");
            IndexHits<Node> hits       = edmsearch2.get("rdf_about", rdfAbout);
            Node            parent     = hits.getSingle();
            if (parent == null) throw new IllegalArgumentException("no node found in index for rdf_about = " + rdfAbout);
            tx.success();
            return (long) IteratorUtil.count(parent.getRelationships(Direction.OUTGOING, HAS_PART));
        }
    }
    
    private Node getFirstChild(String rdfAbout) {
        rdfAbout = ObjectMapper.fixSlashes(rdfAbout);
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index       = db.index();
            Index<Node>     edmsearch2  = index.forNodes("edmsearch2");
            IndexHits<Node> hits        = edmsearch2.get("rdf_about", rdfAbout);
            Node            first       = null;
            Node            parent      = hits.getSingle();
            if (parent == null) throw new IllegalArgumentException("no node found in index for rdf_about = " + rdfAbout);

            // the child node which has no fakeOrder or NextInSequence relationships pointing to it, is the first
            for (Relationship r1 : parent.getRelationships(Direction.OUTGOING, HAS_PART)) {
                Node child = r1.getEndNode();
                if ((child.getDegree(ISFAKEORDER, Direction.OUTGOING) == 0) &&
                    (child.getDegree(ISNEXTINSEQUENCE, Direction.OUTGOING) == 0)) {
                    first = child;
                    break;
                }
            }
            if (first == null) throw new IllegalArgumentException("no first child for node " + parent);
            tx.success();
            return first;
        }
    }
}

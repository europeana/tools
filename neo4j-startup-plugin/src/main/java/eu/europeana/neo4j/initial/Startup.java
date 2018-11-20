/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.initial;

import eu.europeana.neo4j.mapper.ObjectMapper;
import eu.europeana.neo4j.model.Hierarchy;
import java.util.ArrayList;
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

import org.codehaus.jackson.node.JsonNodeFactory;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

/**
 *
 * @author gmamakis, luthien
 */
@javax.ws.rs.Path("/startup")
public class Startup {

    private static final RelationshipType HAS_PART          = RelationshipType.withName("dcterms:hasPart");
    private static final RelationshipType ISFAKEORDER       = RelationshipType.withName("isFakeOrder");
    private static final RelationshipType ISNEXTINSEQUENCE  = RelationshipType.withName("edm:isNextInSequence");
    private static final String RDFABOUT                    = "rdf_about";
    private static final String EDMSEARCH2                  = "edmsearch2";
    private static final String HAS_PARENT                  = "hasParent";
    private static final String HAS_CHILDREN                = "hasChildren";
    private static final String CHILDRENCOUNT               = "childrenCount";
    private static final String RELBEFORE                   = "relBefore";

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
        String rdfAbout = ObjectMapper.fixSlashes(nodeId);
        Hierarchy hierarchy = new Hierarchy();
        List<Node> parents = new ArrayList<>();
        String obj;
        try ( Transaction tx = db.beginTx() ) {
            Node node = db.index().forNodes(EDMSEARCH2).get(RDFABOUT, rdfAbout).getSingle();

            if (node.hasProperty(HAS_CHILDREN)) {
                long childrenCount = getChildrenCount(node.getProperty(RDFABOUT).toString());
                if (childrenCount > 0) {
                    node.setProperty(CHILDRENCOUNT, childrenCount);
                } else {
                    throw new Neo4jDataConsistencyException("Inconsistency found between node's hasChildren property and actual unique children", node.getProperty("rdf:about").toString());
                }
            }
            if (node.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                node.setProperty(RELBEFORE, false);
            } else if (node.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                node.setProperty(RELBEFORE, true);
            }

            parents.add(node);
            Node testNode = node;
            while (testNode.hasProperty(HAS_PARENT)) {
                Node newNode = db.index().forNodes(EDMSEARCH2).get(RDFABOUT, testNode.getProperty(HAS_PARENT)).
                        getSingle();
                long childrenCount = getChildrenCount(newNode.getProperty(RDFABOUT).toString());
                newNode.setProperty(CHILDRENCOUNT, childrenCount);
                if (newNode.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                    newNode.setProperty(RELBEFORE, false);
                } else if (newNode.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                    newNode.setProperty(RELBEFORE, true);
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
                if (endNode.hasProperty(HAS_CHILDREN)) {
                    long childrenCount = getChildrenCount(endNode.getProperty(RDFABOUT).toString());
                    endNode.setProperty(CHILDRENCOUNT, childrenCount);
                    precedingSiblingChildren.add(getFirstChild(endNode.getProperty(RDFABOUT).toString()));
                }
                
                if (endNode.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                    endNode.setProperty(RELBEFORE, false);
                } else if (endNode.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                    endNode.setProperty(RELBEFORE, true);
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
                if (endNode.hasProperty(HAS_CHILDREN)) {
                    long childrenCount = getChildrenCount(endNode.getProperty(RDFABOUT).toString());
                    endNode.setProperty(CHILDRENCOUNT, childrenCount);
                    followingSiblingChildren.add(getFirstChild(endNode.getProperty(RDFABOUT).toString()));
                }
                followingSiblings.add(endNode);
                if (endNode.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
                    endNode.setProperty(RELBEFORE, false);
                } else if (endNode.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
                    endNode.setProperty(RELBEFORE, true);
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
            Index<Node>     edmsearch2 = index.forNodes(EDMSEARCH2);
            IndexHits<Node> hits       = edmsearch2.get(RDFABOUT, rdfAbout);
            Node            parent     = hits.getSingle();
            if (parent == null) throw new IllegalArgumentException("no node found in index for rdf_about = " + rdfAbout);
            tx.success();
            return parent.getDegree(HAS_PART, Direction.OUTGOING);
//            return (long) IteratorUtil.count(parent.getRelationships(Direction.OUTGOING, HAS_PART));
        }
    }
    
    private Node getFirstChild(String rdfAbout) {
        rdfAbout = ObjectMapper.fixSlashes(rdfAbout);
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index       = db.index();
            Index<Node>     edmsearch2  = index.forNodes(EDMSEARCH2);
            IndexHits<Node> hits        = edmsearch2.get(RDFABOUT, rdfAbout);
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

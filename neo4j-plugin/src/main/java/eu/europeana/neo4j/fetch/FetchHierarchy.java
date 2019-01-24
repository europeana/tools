/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.fetch;

import eu.europeana.neo4j.exceptions.Neo4jDataConsistencyException;
import eu.europeana.neo4j.utils.FamilyTherapist;
import eu.europeana.neo4j.model.Family;

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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
@javax.ws.rs.Path("/hierarchy")
public class FetchHierarchy {

    private static final RelationshipType HAS_PART          = RelationshipType.withName("dcterms:hasPart");
    private static final RelationshipType ISFAKEORDER       = RelationshipType.withName("isFakeOrder");
    private static final RelationshipType ISNEXTINSEQUENCE  = RelationshipType.withName("edm:isNextInSequence");
    private static final String RDFABOUT                    = "rdf:about";
    private static final String RDF_ABOUT                   = "rdf_about";
    private static final String EDMSEARCH2                  = "edmsearch2";
    private static final String HAS_PARENT                  = "hasParent";
    private static final String HAS_CHILDREN                = "hasChildren";
    private static final String CHILDRENCOUNT               = "childrenCount";
    private static final String RELBEFORE                   = "relBefore";

    private GraphDatabaseService db;

    public FetchHierarchy(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getHierarchy(@PathParam("nodeId") String nodeId,
                                 @QueryParam("length") @DefaultValue("32") int length,
                                 @QueryParam("lengthBefore") @DefaultValue("8") int lengthBefore) {
        String     rdfAbout = FamilyTherapist.fixSlashes(nodeId);
        Family     family   = new Family();
        List<Node> parents  = new ArrayList<>();
        String     obj;

        try ( Transaction tx = db.beginTx() ) {
            Node node = db.index().forNodes(EDMSEARCH2)
                          .get(RDF_ABOUT, rdfAbout).getSingle();

            setChildCountAndRelBefore(node);
            parents.add(node);

            Node testNode = node;
            while (testNode.hasProperty(HAS_PARENT)) {
                // fetch the parent
                Node newNode = db.index().forNodes(EDMSEARCH2)
                                 .get(RDF_ABOUT, testNode.getProperty(HAS_PARENT)).getSingle();
                setChildCountAndRelBefore(newNode);
                parents.add(newNode);
                testNode = newNode;
            }

            family.setParents(parents);
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
                    setChildCountAndRelBefore(endNode);
                    precedingSiblingChildren.add(getFirstChild(endNode.getProperty(RDFABOUT).toString()));
                }
                precedingSiblings.add(path.endNode());
            }

            family.setPrecedingSiblings(precedingSiblings);
            family.setPrecedingSiblingChildren(precedingSiblingChildren);
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
                    setChildCountAndRelBefore(endNode);
                    followingSiblingChildren.add(getFirstChild(endNode.getProperty(RDFABOUT).toString()));
                }
                followingSiblings.add(endNode);
            }

            family.setFollowingSiblings(followingSiblings);
            family.setFollowingSiblingChildren(followingSiblingChildren);

            obj = new FamilyTherapist().toJson(family);

            tx.success();

            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE, "application/json").build();

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

    private void setChildCountAndRelBefore(Node node) throws Neo4jDataConsistencyException {
        if (!node.hasProperty(CHILDRENCOUNT) && node.hasProperty(HAS_CHILDREN)) {
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
    }

    private String error2Json(String errMessage){
        return JsonNodeFactory.instance.textNode(errMessage).toString();
    }

    private long getChildrenCount(String rdfAbout) {
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
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
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
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

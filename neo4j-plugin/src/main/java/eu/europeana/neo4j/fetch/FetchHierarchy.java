/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.fetch;

import eu.europeana.neo4j.exceptions.Neo4jDataConsistencyException;
import eu.europeana.neo4j.exceptions.Neo4jNodeNotFoundException;
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
@javax.ws.rs.Path("/")
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
    private static final String INDEX                       = "index";
    private static final String JSONMIMETYPE                = "application/json";

    private GraphDatabaseService db;
    private FamilyTherapist familyTherapist;

    public FetchHierarchy(@Context GraphDatabaseService db) {
        familyTherapist = new FamilyTherapist();
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("self/rdfAbout/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSelf(@PathParam("rdfAbout") String rdfAbout){
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
        List<Node> selfList  = new ArrayList<>();
        String     obj;
        Node       self;

        try ( Transaction tx = db.beginTx() ) {
            self = db.index().forNodes(EDMSEARCH2).get(RDF_ABOUT, rdfAbout).getSingle();
            if (null == self){
                throw new Neo4jNodeNotFoundException("Couldn't find node with rdfAbout '" + rdfAbout + "'", rdfAbout);
            }
            setChildCountAndRelBefore(self);
            consolidateIndex(db, self);
            selfList.add(self);
            obj = new FamilyTherapist().siblingsToJson(selfList, "self");
            tx.success();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE, JSONMIMETYPE).build();

        } catch (Neo4jDataConsistencyException nce) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(
                    Level.SEVERE, nce.getRdfAbout() + "\n" + nce.getMessage());
            obj = FamilyTherapist.error2Json("INCONSISTENT_DATA");
            return Response.status(502).entity(obj).header(
                    HttpHeaders.CONTENT_TYPE, JSONMIMETYPE).build();
        } catch (Neo4jNodeNotFoundException nfe) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(
                    Level.INFO, nfe.getRdfAbout() + "\n" + nfe.getMessage());
            obj = FamilyTherapist.error2Json("NODE_NOT_FOUND");
            return Response.status(404).entity(obj).header(
                    HttpHeaders.CONTENT_TYPE, JSONMIMETYPE).build();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE,
                    e.getCause() + "\n" + e.getMessage());
            obj = FamilyTherapist.error2Json("ERROR");
            return Response.status(500).entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    JSONMIMETYPE).build();
        }
    }


    @GET
    @javax.ws.rs.Path("hierarchy/rdfAbout/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getHierarchy(@PathParam("rdfAbout") String rdfAbout,
                                 @QueryParam("length") @DefaultValue("32") int length,
                                 @QueryParam("lengthBefore") @DefaultValue("8") int lengthBefore) {
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
        Family     family   = new Family();
        List<Node> parents  = new ArrayList<>();
        String     obj;

        try ( Transaction tx = db.beginTx() ) {
            Node node = db.index().forNodes(EDMSEARCH2).get(RDF_ABOUT, rdfAbout).getSingle();
            if (null == node){
                throw new Neo4jNodeNotFoundException("Couldn't find node with rdfAbout '" + rdfAbout + "'", rdfAbout);
            }
            setChildCountAndRelBefore(node);
            parents.add(node);

            Node testNode = node;
            while (testNode.hasProperty(HAS_PARENT)) {
                // fetch the parent
                Node newNode = db.index().forNodes(EDMSEARCH2)
                                 .get(RDF_ABOUT, testNode.getProperty(HAS_PARENT)).getSingle();
                setChildCountAndRelBefore(newNode);
                consolidateIndex(db, node);
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
                    precedingSiblingChildren.add(getFirstChild(endNode));
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
                    followingSiblingChildren.add(getFirstChild(endNode));
                }
                followingSiblings.add(endNode);
            }

            family.setFollowingSiblings(followingSiblings);
            family.setFollowingSiblingChildren(followingSiblingChildren);

            obj = familyTherapist.toJson(family);

            tx.success();

            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE, JSONMIMETYPE).build();

        } catch (Neo4jDataConsistencyException nce) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(
                    Level.SEVERE, nce.getRdfAbout() + "\n" + nce.getMessage());
            obj = FamilyTherapist.error2Json("INCONSISTENT_DATA");
            return Response.status(502).entity(obj).header(
                    HttpHeaders.CONTENT_TYPE, JSONMIMETYPE).build();
        } catch (Neo4jNodeNotFoundException nfe) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(
                    Level.INFO, nfe.getRdfAbout() + "\n" + nfe.getMessage());
            obj = FamilyTherapist.error2Json("NODE_NOT_FOUND");
            return Response.status(404).entity(obj).header(
                    HttpHeaders.CONTENT_TYPE, JSONMIMETYPE).build();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE,
                    e.getCause() + "\n" + e.getMessage());
            obj = FamilyTherapist.error2Json("ERROR");
            return Response.status(500).entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    JSONMIMETYPE).build();
        }

    }

    private void consolidateIndex(GraphDatabaseService db, Node node){
        if (node.hasProperty(INDEX) && node.getProperty(INDEX) instanceof String){
            node.removeProperty(INDEX);
        }
        if (!node.hasProperty(INDEX)){
            long traverseLength = 0L;
            TraversalDescription traversal = db.traversalDescription();
            Traverser traverse = traversal
                    .depthFirst()
                    .relationships(ISNEXTINSEQUENCE, Direction.OUTGOING)
                    .relationships(ISFAKEORDER, Direction.OUTGOING)
                    .traverse(node);

            for (Path path : traverse) {
                if (path.length() > traverseLength) {
                    traverseLength = path.length();
                }
            }
            node.setProperty(INDEX, traverseLength + 1L);
        }

    }

    private void setChildCountAndRelBefore(Node node) throws Neo4jDataConsistencyException {
        if (!node.hasProperty(CHILDRENCOUNT) && node.hasProperty(HAS_CHILDREN)) {
            long childrenCount = node.getDegree(HAS_PART, Direction.OUTGOING);
            if (childrenCount > 0) {
                node.setProperty(CHILDRENCOUNT, childrenCount);
            } else {
                throw new Neo4jDataConsistencyException(
                        "Inconsistency found between node's hasChildren property and actual unique children",
                        node.getProperty("rdf:about").toString());
            }
        }
        if (node.hasRelationship(ISFAKEORDER, Direction.INCOMING)) {
            node.setProperty(RELBEFORE, false);
        } else if (node.hasRelationship(ISNEXTINSEQUENCE, Direction.INCOMING)) {
            node.setProperty(RELBEFORE, true);
        }
    }

    // the child node which has no fakeOrder or NextInSequence relationships pointing to it, is the first
    private Node getFirstChild(Node parent){
        Node first = null;
        for (Relationship r1 : parent.getRelationships(Direction.OUTGOING, HAS_PART)) {
            Node child = r1.getEndNode();
            if ((child.getDegree(ISFAKEORDER, Direction.OUTGOING) == 0) &&
                (child.getDegree(ISNEXTINSEQUENCE, Direction.OUTGOING) == 0)) {
                first = child;
                break;
            }
        }
        if (first == null) throw new IllegalArgumentException("no first child for node " + parent);
        return first;
    }
}

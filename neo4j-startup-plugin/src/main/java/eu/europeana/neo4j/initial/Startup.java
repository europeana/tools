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
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

/**
 *
 * @author gmamakis
 */
@javax.ws.rs.Path("/startup")
public class Startup {

    final DynamicRelationshipType ISNEXTINSEQUENCE = DynamicRelationshipType.withName("edm:isNextInSequence");

    private GraphDatabaseService db;

    public Startup(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response hierarchy(@PathParam("nodeId") String nodeId, @QueryParam("length") @DefaultValue("32") int length,
            @QueryParam("lengthBefore") @DefaultValue("8") int lengthBefore) {
        Hierarchy hierarchy = new Hierarchy();
        List<Node> parents = new ArrayList<>();
        Transaction tx = db.beginTx();
        try {
            Node node = db.index().forNodes("edmsearch2").get("rdf_about", nodeId).getSingle();
            long nodeIndex = getIndex(node.getId());
            node.setProperty("index", nodeIndex);
            parents.add(node);
            Node testNode = node;
            while (testNode.hasProperty("hasParent")) {
                Node newNode = db.index().forNodes("edmsearch2").get("rdf_about", testNode.getProperty("hasParent")).
                        getSingle();
                long parentIndex = getIndex(newNode.getId());
                newNode.setProperty("index", parentIndex);
                parents.add(newNode);
                testNode = newNode;
            }

            hierarchy.setParents(parents);
            List<Node> children = new ArrayList<>();
            TraversalDescription traversal = db.traversalDescription();
            Traverser traverse = traversal
                    .depthFirst()
                    .relationships(ISNEXTINSEQUENCE, Direction.OUTGOING)
                    .evaluator(Evaluators.toDepth(length))
                    .evaluator(Evaluators.excludeStartPosition())
                    .traverse(node);
            long followingIndex = nodeIndex;
            for (Path path : traverse) {
                followingIndex++;
                Node endNode = path.endNode();
                endNode.setProperty("index", followingIndex);
                children.add(path.endNode());
            }
            
            hierarchy.setSiblings(children);
            List<Node> childrenBefore = new ArrayList<>();
            TraversalDescription traversalBefore = db.traversalDescription();
            Traverser traverseBefore = traversalBefore
                    .depthFirst()
                    .relationships(ISNEXTINSEQUENCE, Direction.INCOMING)
                    .evaluator(Evaluators.toDepth(lengthBefore))
                    .evaluator(Evaluators.excludeStartPosition())
                    .traverse(node);
            long previousIndex = nodeIndex;

            for (Path path : traverseBefore) {
                previousIndex--;
                Node endNode = path.endNode();
                endNode.setProperty("index", previousIndex);
                childrenBefore.add(endNode);
            }
            hierarchy.setPreviousSiblings(childrenBefore);
            
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }
        String obj = new ObjectMapper().toJson(hierarchy);
        tx.success();
        tx.finish();
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
}

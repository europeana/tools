/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.initial;

import eu.europeana.neo4j.model.Hierarchy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    @javax.ws.rs.Path("/nodeId/{nodeId}/{length}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hierarchy(@PathParam("nodeId") String nodeId, @PathParam("length") long length) {
        Hierarchy hierarchy = new Hierarchy();
        List<Node> parents = new ArrayList<>();
        Transaction tx = db.beginTx();
        try {
            Node node = db.index().forNodes("edmsearch2").get("rdf_about", nodeId).getSingle();
            parents.add(node);
            Node testNode = node;
            while (testNode.hasProperty("hasParent")) {
                testNode = db.index().forNodes("edmsearch2").get("rdf_about", testNode.getProperty("rdf:about")).
                        getSingle();
                parents.add(testNode);
            }

            parents.add(testNode);

            hierarchy.setParents(parents);
            List<Node> children = new ArrayList<>();
            int maxLength = 0;
            TraversalDescription traversal = db.traversalDescription();
            Traverser traverse = traversal
                    .depthFirst()
                    .relationships(ISNEXTINSEQUENCE, Direction.OUTGOING)
                    .traverse(node);
            if (maxLength == length) {
                for (Path path : traverse) {
                    children.add(path.endNode());
                }
                maxLength++;
            }
            hierarchy.setSiblings(children);
            tx.success();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }

        return Response.ok().entity(hierarchy).header(HttpHeaders.CONTENT_TYPE, "application/json").build();
    }
}

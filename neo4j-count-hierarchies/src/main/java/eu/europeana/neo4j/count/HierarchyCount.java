/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.count;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Traverser;

import org.neo4j.graphdb.traversal.TraversalDescription;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@javax.ws.rs.Path("/hierarchycount")
public class HierarchyCount {

    final DynamicRelationshipType ISNEXTINSEQUENCE = DynamicRelationshipType.withName("edm:isNextInSequence");
    private GraphDatabaseService db;

    public HierarchyCount(@Context GraphDatabaseService db) {

        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response distance(@PathParam("nodeId") int nodeId) throws IOException {
        int maxLength = 0;
        long start = System.currentTimeMillis();
        Transaction tx = db.beginTx();
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
            tx.success();
            tx.finish();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("length", String.valueOf(maxLength));
        json.put("time", String.valueOf((System.currentTimeMillis() - start)));

        String output = new ObjectMapper().writeValueAsString(json);

        return Response.ok().entity(output).header(HttpHeaders.CONTENT_TYPE, "application/json").build();
    }
}

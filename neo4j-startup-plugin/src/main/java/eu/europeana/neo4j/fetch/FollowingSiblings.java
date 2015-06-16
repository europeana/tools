/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.fetch;

import eu.europeana.neo4j.mapper.ObjectMapper;
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
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.util.StringLogger;

/**
 *
 * @author luthien
 */
@javax.ws.rs.Path("/following")
public class FollowingSiblings {

    

    private GraphDatabaseService db;
    private ExecutionEngine engine;

    public FollowingSiblings(@Context GraphDatabaseService db) {
        this.db = db;
        this.engine = new ExecutionEngine(db, StringLogger.SYSTEM);
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getfollowing(@PathParam("nodeId") String nodeId,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        List<Node> followingSiblings = new ArrayList<>();
        Transaction tx = db.beginTx();
        
        try {
            ExecutionResult result = engine.execute(
                    "start self = node:edmsearch2(rdf_about=\"" + nodeId + "\") "
                    + " MATCH (self)-[:isFakeOrder|`edm:isNextInSequence`*]->(following) "
                    + "RETURN following LIMIT " + limit);
            Iterator<Node> followingIterator = result.columnAs("following");
            int i = 0;
            while (followingIterator.hasNext()) {
                followingSiblings.add(followingIterator.next());
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        } finally {

            String obj = new ObjectMapper().siblingsToJson(followingSiblings, "siblings");
            tx.success();
            tx.finish();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();
        }
    }
}
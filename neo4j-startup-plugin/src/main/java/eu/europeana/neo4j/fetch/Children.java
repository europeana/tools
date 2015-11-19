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
@javax.ws.rs.Path("/children")
public class Children {

    

    private GraphDatabaseService db;
    private ExecutionEngine engine;

    public Children(@Context GraphDatabaseService db) {
        this.db = db;
        this.engine = new ExecutionEngine(db, StringLogger.SYSTEM);
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getchildren(@PathParam("nodeId") String nodeId, @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        List<Node> children = new ArrayList<>();
        Transaction tx = db.beginTx();
        try {
            ExecutionResult result = engine.execute(
                    "start parent = node:edmsearch2(rdf_about=\"" + nodeId + "\") "
                    + "MATCH (parent)-[:`dcterms:hasPart`]->(child) "
                    + "WHERE NOT ()-[:isFakeOrder]->(child) "
                    + "AND NOT ()-[:`edm:isNextInSequence`]->(child) "
                    + "WITH child AS first "
                    + "MATCH (first)-[:isFakeOrder|`edm:isNextInSequence`*]->(next) "
                    + "WITH DISTINCT first + COLLECT(next) AS spool "
                    + "UNWIND spool as children RETURN children "
                    + "SKIP " + offset + " LIMIT " + limit);
            Iterator<Node> childIterator = result.columnAs("children");
            while (childIterator.hasNext()) {
                children.add(childIterator.next());
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        } finally {

            String obj = new ObjectMapper().siblingsToJson(children, "siblings");
            tx.success();
            tx.finish();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();
        }
    }
}
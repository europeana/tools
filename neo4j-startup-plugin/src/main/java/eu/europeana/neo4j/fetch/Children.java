/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.fetch;

import eu.europeana.neo4j.mapper.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.tooling.GlobalGraphOperations;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author luthien
 */
@javax.ws.rs.Path("/children")
public class Children {

    private static final RelationshipType HAS_PART = DynamicRelationshipType.withName("dcterms:hasPart");
    private static final RelationshipType IS_FAKE  = DynamicRelationshipType.withName("isFakeOrder");
    private static final RelationshipType IS_NEXT  = DynamicRelationshipType.withName("edm:isNextInSequence");

//    private GraphDatabaseService db;
//    private ExecutionEngine engine;

//    public Children(@Context GraphDatabaseService db) {
//        this.db = db;
//        this.engine = new ExecutionEngine(db, StringLogger.SYSTEM);
//    }


    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChildren(@PathParam("nodeId") String nodeId,
                                @QueryParam("offset") @DefaultValue("0") int offset,
                                @QueryParam("limit") @DefaultValue("10") int limit,
                                @Context GraphDatabaseService db) throws IOException {
        List<Node> children = new ArrayList<>();
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index      = db.index();
            Index<Node>     edmsearch2 = index.forNodes("edmsearch2");
            IndexHits<Node> hits       = edmsearch2.get("rdf_about", nodeId);
            Node            parent     = hits.getSingle();
            if (parent==null) {
                throw new IllegalArgumentException("no node found in index for rdf_about = " + nodeId);
            }
            Node first = null;

            // Get all children
            for (Relationship r1 : parent.getRelationships(Direction.OUTGOING, HAS_PART)) {
                Node child = r1.getEndNode();
                if ((child.getDegree(IS_FAKE, Direction.OUTGOING) == 0) &&
                        (child.getDegree(IS_NEXT, Direction.OUTGOING) == 0)) {
                    first = child;
                }
            }

            if (first==null) {
                throw new IllegalArgumentException("no first child for node " + parent);
            }

            // Go up to limit hops away
            TraversalDescription td = db.traversalDescription()
                    .depthFirst()
                    .relationships(IS_FAKE, Direction.INCOMING)
                    .relationships(IS_NEXT, Direction.INCOMING)
                    .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
                    .evaluator(Evaluators.fromDepth(offset))
                    .evaluator(Evaluators.toDepth((offset + limit) - 1));

            // Add to the results
            for (org.neo4j.graphdb.Path path : td.traverse(first)) {
                Node child = path.endNode();
                children.add(child);
            }
            String obj = new ObjectMapper().siblingsToJson(children, "siblings");
            tx.success();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();
        }
    }

    @GET
    @javax.ws.rs.Path("degree")
    public String getDegreeHistogram(@Context GraphDatabaseService gds) throws IOException {
        SortedMap<Integer, Integer> histogram = new TreeMap<>();
        try (Transaction tx = gds.beginTx()) {
            for (Node n: GlobalGraphOperations.at(gds).getAllNodes()) {
                int degree = n.getDegree();

                Integer val = histogram.get(degree);
                if (val==null) {
                    histogram.put(degree, 1);
                } else {
                    histogram.put(degree, val+1);
                }

            }
            tx.success();
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry: histogram.entrySet()) {
            sb.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }




//    @GET
//    @javax.ws.rs.Path("/nodeId/{nodeId}")
//    @Produces(MediaType.APPLICATION_JSON)
//
//    public Response getchildren(@PathParam("nodeId") String nodeId,
//                                @QueryParam("offset") @DefaultValue("0") int offset,
//                                @QueryParam("limit") @DefaultValue("10") int limit) {
//        List<Node> children = new ArrayList<>();
//        Transaction tx = db.beginTx();
//        try {
//            ExecutionResult result = engine.execute(
//                    "start parent = node:edmsearch2(rdf_about=\"" + nodeId + "\") "
//                    + "MATCH (parent)-[:`dcterms:hasPart`]->(child) "
//                    + "WHERE NOT ()-[:isFakeOrder]->(child) "
//                    + "AND NOT ()-[:`edm:isNextInSequence`]->(child) "
//                    + "WITH child AS first "
//                    + "MATCH (first)-[:isFakeOrder|`edm:isNextInSequence`*]->(next) "
//                    + "WITH DISTINCT first + COLLECT(next) AS spool "
//                    + "UNWIND spool as children RETURN children "
//                    + "SKIP " + offset + " LIMIT " + limit);
//            Iterator<Node> childIterator = result.columnAs("children");
//            while (childIterator.hasNext()) {
//                children.add(childIterator.next());
//            }
//        } catch (Exception e) {
//            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
//        } finally {
//
//            String obj = new ObjectMapper().siblingsToJson(children, "siblings");
//            tx.success();
//            tx.finish();
//            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
//                    "application/json").build();
//        }
//    }
}
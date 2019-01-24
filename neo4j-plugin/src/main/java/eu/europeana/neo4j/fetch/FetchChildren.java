/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.neo4j.fetch;

import eu.europeana.neo4j.utils.FamilyTherapist;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.tooling.GlobalGraphOperations;

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
public class FetchChildren {

    private static final RelationshipType HAS_PART = RelationshipType.withName("dcterms:hasPart");
    private static final RelationshipType ISFAKEORDER  = RelationshipType.withName("isFakeOrder");
    private static final RelationshipType ISNEXTINSEQUENCE  = RelationshipType.withName("edm:isNextInSequence");


    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChildren(@PathParam("nodeId") String nodeId,
                                @QueryParam("offset") @DefaultValue("0") int offset,
                                @QueryParam("limit") @DefaultValue("10") int limit,
                                @Context GraphDatabaseService db) throws IOException {
        List<Node> children = new ArrayList<>();
        String rdfAbout = FamilyTherapist.fixSlashes(nodeId);
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index      = db.index();
            Index<Node>     edmsearch2 = index.forNodes("edmsearch2");
            IndexHits<Node> hits       = edmsearch2.get("rdf_about", rdfAbout);
            Node            parent     = hits.getSingle();
            if (parent==null) {
                throw new IllegalArgumentException("no node found in index for rdf_about = " + rdfAbout);
            }
            Node first = null;

            // Get all children
            for (Relationship r1 : parent.getRelationships(Direction.OUTGOING, HAS_PART)) {
                Node child = r1.getEndNode();
                if ((child.getDegree(ISFAKEORDER, Direction.OUTGOING) == 0) &&
                    (child.getDegree(ISNEXTINSEQUENCE, Direction.OUTGOING) == 0)) {
                    first = child;
                    break;
                }
            }

            if (first == null) {
                throw new IllegalArgumentException("no first child for node " + parent);
            }

            // Go up to limit hops away
            TraversalDescription td = db.traversalDescription()
                    .depthFirst()
                    .relationships(ISFAKEORDER, Direction.INCOMING)
                    .relationships(ISNEXTINSEQUENCE, Direction.INCOMING)
                    .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
                    .evaluator(Evaluators.fromDepth(offset))
                    .evaluator(Evaluators.toDepth((offset + limit) - 1));

            // Add to the results
            for (org.neo4j.graphdb.Path path : td.traverse(first)) {
                Node child = path.endNode();
                children.add(child);
            }
            String obj = new FamilyTherapist().siblingsToJson(children, "siblings");
            tx.success();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();
        }
    }

    @GET
    @javax.ws.rs.Path("degree")
    public String getDegreeHistogram(@Context GraphDatabaseService gds) {
        SortedMap<Integer, Integer> histogram = new TreeMap<>();
        try (Transaction tx = gds.beginTx()) {
            for (Node n: gds.getAllNodes()) {
                int degree = n.getDegree();
                histogram.merge(degree, 1, (a, b) -> a + b);
            }
            tx.success();
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry: histogram.entrySet()) {
            sb.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

}
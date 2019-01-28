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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author luthien
 */
@javax.ws.rs.Path("/following")
public class FetchFollowing {


    private static final RelationshipType ISNEXTINSEQUENCE = RelationshipType.withName("edm:isNextInSequence");
    private static final RelationshipType ISFAKEORDER      = RelationshipType.withName("isFakeOrder");
    private static final String           RDF_ABOUT        = "rdf_about";
    private static final String           EDMSEARCH2       = "edmsearch2";

    private GraphDatabaseService db;

    public FetchFollowing(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getfollowing(@PathParam("nodeId") String nodeId,
                                 @QueryParam("offset") @DefaultValue("0") int offset,
                                 @QueryParam("limit") @DefaultValue("10") int limit) {
        String rdfAbout = FamilyTherapist.fixSlashes(nodeId);
        List<Node> followingSiblings = new ArrayList<>();
        try ( Transaction tx = db.beginTx() ) {
            Node sibling = db.index().forNodes(EDMSEARCH2).get(RDF_ABOUT, rdfAbout).getSingle();
            if (sibling == null) {
                throw new IllegalArgumentException("no node found in index for rdf_about = " + rdfAbout);
            }

            // Gather all ye following brothers and sisters but take heed! No more than in 'limit' number shall ye come!
            TraversalDescription td = db.traversalDescription()
                    .breadthFirst()
                    .relationships(ISFAKEORDER, Direction.INCOMING)
                    .relationships(ISNEXTINSEQUENCE, Direction.INCOMING)
                    .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
                    .evaluator(Evaluators.excludeStartPosition())
                    .evaluator(Evaluators.fromDepth(offset + 1))
                    .evaluator(Evaluators.toDepth(offset + limit));

            // Add to the results
            for (org.neo4j.graphdb.Path path : td.traverse(sibling)) {
                Node child = path.endNode();
                followingSiblings.add(child);
            }
            String obj = new FamilyTherapist().siblingsToJson(followingSiblings, "siblings");
            tx.success();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE, "application/json").build();
        }
    }
}
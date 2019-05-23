package eu.europeana.neo4j.fetch;

import eu.europeana.neo4j.exceptions.Neo4jNodeNotFoundException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author luthien
 */
@javax.ws.rs.Path("/following")
public class FetchFollowing {


    private static final RelationshipType ISNEXTINSEQUENCE = RelationshipType.withName("edm:isNextInSequence");
    private static final RelationshipType ISFAKEORDER      = RelationshipType.withName("isFakeOrder");
    private static final String           JSONMIMETYPE     = "application/json";

    private GraphDatabaseService db;

    public FetchFollowing(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/rdfAbout/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getfollowing(@PathParam("rdfAbout") String rdfAbout,
                                 @QueryParam("offset") @DefaultValue("0") int offset,
                                 @QueryParam("limit") @DefaultValue("10") int limit) {
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
        List<Node> followingSiblings = new ArrayList<>();
        String     obj;
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index      = db.index();
            Index<Node>     edmsearch2 = index.forNodes("edmsearch2");
            IndexHits<Node> hits       = edmsearch2.get("rdf_about", rdfAbout);
            Node            sibling    = hits.getSingle();
            if (null == sibling) {
                throw new Neo4jNodeNotFoundException("Couldn't find node with rdfAbout '" + rdfAbout + "'", rdfAbout);
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
            obj = new FamilyTherapist().siblingsToJson(followingSiblings, "siblings");
            tx.success();
            return Response.ok().entity(obj).header(HttpHeaders.CONTENT_TYPE,
                    "application/json").build();
        } catch (Neo4jNodeNotFoundException nfe) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(
                    Level.INFO, nfe.getRdfAbout() + "\n" + nfe.getMessage());
            obj = FamilyTherapist.error2Json("NODE_NOT_FOUND");
            return Response.status(404).entity(obj).header(
                    HttpHeaders.CONTENT_TYPE, JSONMIMETYPE).build();
        }
    }
}
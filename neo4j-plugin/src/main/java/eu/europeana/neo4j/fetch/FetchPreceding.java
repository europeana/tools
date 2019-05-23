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
@javax.ws.rs.Path("/preceding")
public class FetchPreceding {


    private static final RelationshipType ISFAKEORDER      = RelationshipType.withName("isFakeOrder");
    private static final RelationshipType ISNEXTINSEQUENCE = RelationshipType.withName("edm:isNextInSequence");
    private static final String           JSONMIMETYPE     = "application/json";

    private GraphDatabaseService db;

    public FetchPreceding(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/rdfAbout/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getpreceding(@PathParam("rdfAbout") String rdfAbout,
                                 @QueryParam("offset") @DefaultValue("0") int offset,
                                 @QueryParam("limit") @DefaultValue("10") int limit) {
        List<Node> precedingSiblings = new ArrayList<>();
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
        String     obj;
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index      = db.index();
            Index<Node>     edmsearch2 = index.forNodes("edmsearch2");
            IndexHits<Node> hits       = edmsearch2.get("rdf_about", rdfAbout);
            Node            sibling    = hits.getSingle();
            if (null == sibling) {
                throw new Neo4jNodeNotFoundException("Couldn't find node with rdfAbout '" + rdfAbout + "'", rdfAbout);
            }

            // Gather all ye preceding brothers and sisters but take heed! No more than in 'limit' number shall ye come!
            TraversalDescription td = db.traversalDescription()
                    .breadthFirst()
                    .relationships(ISFAKEORDER, Direction.OUTGOING)
                    .relationships(ISNEXTINSEQUENCE, Direction.OUTGOING)
                    .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
                    .evaluator(Evaluators.excludeStartPosition())
                    .evaluator(Evaluators.fromDepth(offset + 1))
                    .evaluator(Evaluators.toDepth(offset + limit));

            // Add to the results
            for (org.neo4j.graphdb.Path path : td.traverse(sibling)) {
                Node child = path.endNode();
                precedingSiblings.add(child);
            }

            obj = new FamilyTherapist().siblingsToJson(precedingSiblings, "siblings");
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
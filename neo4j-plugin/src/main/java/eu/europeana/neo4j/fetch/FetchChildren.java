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
import org.neo4j.graphdb.GraphDatabaseService;


import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author luthien
 */
@javax.ws.rs.Path("/children")
public class FetchChildren {

    private static final RelationshipType HAS_PART         = RelationshipType.withName("dcterms:hasPart");
    private static final RelationshipType ISFAKEORDER      = RelationshipType.withName("isFakeOrder");
    private static final RelationshipType ISNEXTINSEQUENCE = RelationshipType.withName("edm:isNextInSequence");
    private static final String           JSONMIMETYPE     = "application/json";


    @GET
    @javax.ws.rs.Path("/rdfAbout/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChildren(@PathParam("rdfAbout") String rdfAbout,
                                @QueryParam("offset") @DefaultValue("0") int offset,
                                @QueryParam("limit") @DefaultValue("10") int limit,
                                @Context GraphDatabaseService db) throws IOException {
        List<Node> children = new ArrayList<>();
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
        String     obj;
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index      = db.index();
            Index<Node>     edmsearch2 = index.forNodes("edmsearch2");
            IndexHits<Node> hits       = edmsearch2.get("rdf_about", rdfAbout);
            Node            parent     = hits.getSingle();
            if (null == parent) {
                throw new Neo4jNodeNotFoundException("Couldn't find node with rdfAbout '" + rdfAbout + "'", rdfAbout);
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
                throw new Neo4jNodeNotFoundException(
                        "Couldn't find first child of node with rdfAbout '" + rdfAbout + "'", rdfAbout);
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
            obj = new FamilyTherapist().siblingsToJson(children, "siblings");
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
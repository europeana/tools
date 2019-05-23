package eu.europeana.neo4j.count;

import java.io.IOException;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import eu.europeana.neo4j.utils.FamilyTherapist;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Traverser;

import org.neo4j.graphdb.traversal.TraversalDescription;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 * @author Maike.Dulk@ europeana.eu
 */
@javax.ws.rs.Path("/hierarchy")
public class CountHierarchy {

    final   RelationshipType    ISNEXTINSEQUENCE = RelationshipType.withName("edm:isNextInSequence");
    final   RelationshipType    ISFAKEORDER      = RelationshipType.withName("isFakeOrder");
    private static final String RDF_ABOUT        = "rdf_about";
    private static final String EDMSEARCH2       = "edmsearch2";

    private GraphDatabaseService db;
    private ObjectMapper objectMapper;

    public CountHierarchy(@Context GraphDatabaseService db) {
        this.db = db;
        this.objectMapper = new ObjectMapper();
    }

    @GET
    @javax.ws.rs.Path("/rdfAbout/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response distance(@PathParam("rdfAbout") String rdfAbout) throws IOException {
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
        int maxLength = 0;
        long start = System.currentTimeMillis();
        try ( Transaction tx = db.beginTx() ) {
            Node startNode = db.index().forNodes(EDMSEARCH2).get(RDF_ABOUT, rdfAbout).getSingle();
            maxLength = traverse(db, startNode);
            tx.success();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }
        return Response.ok().entity(jsonify(maxLength, start)).header(HttpHeaders.CONTENT_TYPE, "application/json").build();
    }

    @GET
    @javax.ws.rs.Path("/nodeId/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response distance(@PathParam("nodeId") int nodeId) throws IOException {
        int maxLength = 0;
        long start = System.currentTimeMillis();
        try ( Transaction tx = db.beginTx() ) {
            Node startNode = db.getNodeById(nodeId);
            maxLength = traverse(db, startNode);
            tx.success();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }
        return Response.ok().entity(jsonify(maxLength, start)).header(HttpHeaders.CONTENT_TYPE, "application/json").build();
    }

    private int traverse(GraphDatabaseService db, Node startNode){
        int maxLength = 0;
        TraversalDescription traversal = db.traversalDescription();
        Traverser traverse = traversal
                .depthFirst()
                .relationships(ISNEXTINSEQUENCE, Direction.OUTGOING)
                .relationships(ISFAKEORDER, Direction.OUTGOING)
                .traverse(startNode);

        for (Path path : traverse) {
            if (path.length() > maxLength) {
                maxLength = path.length();
            }
        }
        return maxLength;
    }

    private String jsonify(int maxLength, long start) throws JsonProcessingException {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        // add 1 to number of hops required to arrive at the start of the traverse
        json.put("length", String.valueOf(maxLength + 1));
        json.put("time", String.valueOf((System.currentTimeMillis() - start)));
        return objectMapper.writeValueAsString(json);
    }

}

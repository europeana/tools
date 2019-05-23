package eu.europeana.neo4j.count;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import eu.europeana.neo4j.utils.FamilyTherapist;
import org.neo4j.graphdb.*;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lúthien
 */
@javax.ws.rs.Path("/children")
public class CountChildren {

    private static final String RDF_ABOUT          = "rdf_about";
    private static final String EDMSEARCH2         = "edmsearch2";
    private static final RelationshipType HAS_PART = RelationshipType.withName("dcterms:hasPart");

    private GraphDatabaseService db;

    public CountChildren(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @javax.ws.rs.Path("/rdfAbout/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countChildren(@PathParam("rdfAbout") String rdfAbout) throws IOException {
        rdfAbout = FamilyTherapist.fixSlashes(rdfAbout);
        long childrenCount = 0L;
        try ( Transaction tx = db.beginTx() ) {
            Node node = db.index().forNodes(EDMSEARCH2).get(RDF_ABOUT, rdfAbout).getSingle();
            if (node == null) throw new IllegalArgumentException("no node found in index for rdf:about = " + rdfAbout);
            tx.success();
            childrenCount = node.getDegree(HAS_PART, Direction.OUTGOING);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, e.getMessage());
        }
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("childrencount", String.valueOf(childrenCount));
        String output = new ObjectMapper().writeValueAsString(json);
        return Response.ok().entity(output).header(HttpHeaders.CONTENT_TYPE, "application/json").build();
    }

}

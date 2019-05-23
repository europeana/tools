package eu.europeana.neo4j.utils;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by luthien on 22/05/2017.
 */
@javax.ws.rs.Path("/exists")
public class Exists {


    @GET
    @javax.ws.rs.Path("/nodeId/{rdfAbout}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("rdfAbout") String rdfAbout,
                                @Context GraphDatabaseService db) throws IOException {
        try ( Transaction tx = db.beginTx() ) {
            IndexManager    index       = db.index();
            Index<Node>     edmsearch2  = index.forNodes("edmsearch2");
            IndexHits<Node> hits        = edmsearch2.get("rdf_about", rdfAbout);
            Node            node        = hits.getSingle();
            if (node == null) {
                tx.success();
                return Response.status(404).entity("false").header(HttpHeaders.CONTENT_TYPE,
                        "application/json").build();
            } else {
                tx.success();
                return Response.status(200).entity("true").header(HttpHeaders.CONTENT_TYPE,
                        "application/json").build();
            }
        }
    }

}
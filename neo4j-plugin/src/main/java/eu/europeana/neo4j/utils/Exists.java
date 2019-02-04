/*
 * Copyright 2007-2017 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

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
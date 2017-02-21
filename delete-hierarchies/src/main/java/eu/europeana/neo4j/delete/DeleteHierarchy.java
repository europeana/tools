package eu.europeana.neo4j.delete;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@javax.ws.rs.Path("/")
public class DeleteHierarchy {
	private GraphDatabaseService db;
	private static String findNumber = "MATCH (n) where n.`rdf:about` =~ \"/%s/.*\" RETURN count(n)";
	private static String findIds = "MATCH (n) where n.`rdf:about` =~ \"/%s/.*\" RETURN n LIMIT 1000";
	private final static Logger logger = Logger.getLogger(DeleteHierarchy.class.getName());
	public DeleteHierarchy(@Context GraphDatabaseService db) {
		this.db = db;
	}

	@GET
	@javax.ws.rs.Path("/collection/{collectionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("collectionId") String collectionId) throws IOException {
		ExecutionEngine engine = new ExecutionEngine(db);
		long countTime = System.currentTimeMillis();
		ExecutionResult result = engine.execute(String.format(findNumber, collectionId));
		ResourceIterator<?> res = result.columnAs("count(n)");
		long numberOfResults = Long.parseLong(res.next().toString());
		String message = "Found " + String.valueOf(numberOfResults) +  "objects\nExecuted query: "
				+ String.format(findNumber, collectionId) + " in "
				+ String.valueOf(System.currentTimeMillis() - countTime) + " ms\n";

		logger.log(Level.INFO, "Number of results: "+ numberOfResults);

		int maxIterations = (int) (numberOfResults / 1000l) + 1;
		int i = 0;
		long deleteTime = System.currentTimeMillis();

		while (i < maxIterations) {
			ExecutionResult findRecordIds = engine.execute(String.format(findIds, collectionId));
			message += String.format(findIds, collectionId) + "\n";
			logger.log(Level.INFO, "Executing: "+ String.format(findIds, collectionId));

			ResourceIterator<Node> resIterator = findRecordIds.columnAs("n");
			try ( Transaction tx = db.beginTx() ) {
				while (resIterator.hasNext()) {
					Node resObject = resIterator.next();
					try ( Transaction tx2 = db.beginTx() ) {
						if (resObject.getRelationships() != null) {
							Iterator<Relationship> rels = resObject.getRelationships().iterator();
							while (rels.hasNext()){
								rels.next().delete();
							}
						}
					resObject.delete();
					tx2.success();
					}
				}
				tx.success();
				i++;
			}
		}
		message += "Executed query for deletion with relationships of collection " + collectionId + " in "
				+  String.valueOf(System.currentTimeMillis() - deleteTime) + " ms\n";
		return Response.ok(message).build();
	}
}

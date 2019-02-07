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

import org.neo4j.graphdb.*;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@javax.ws.rs.Path("/collection")
public class DeleteCollection {
	public DeleteCollection(@Context GraphDatabaseService db) {
		this.db = db;
	}
	private GraphDatabaseService db;
	private final static Logger logger = Logger.getLogger(DeleteCollection.class.getName());

	@GET
	@javax.ws.rs.Path("/{collectionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("collectionId") String collectionId) throws IOException {
		String findNumber = "MATCH (n) where n.`rdf:about` =~ \"/%s/.*\" RETURN count(n)";
		String findIds = "MATCH (n) where n.`rdf:about` =~ \"/%s/.*\" RETURN n LIMIT 1000";
		StringBuilder sb = new StringBuilder();

		long countTime = System.currentTimeMillis();

		Result result = db.execute(String.format(findNumber, collectionId));

		ResourceIterator<?> res = result.columnAs("count(n)");
		long numberOfResults = Long.parseLong(res.next().toString());
		sb.append("Found ");
		sb.append(String.valueOf(numberOfResults));
		sb.append(" objects\nExecuted query: ");
		sb.append(String.format(findNumber, collectionId));
		sb.append(" in ");
		sb.append(String.valueOf(System.currentTimeMillis() - countTime));
		sb.append(" ms\n\n");

		logger.log(Level.INFO, "Number of results: {0}", numberOfResults);

		int maxIterations = (int) (numberOfResults / 1000l) + 1;
		int i = 0;
		long deleteTime = System.currentTimeMillis();

		while (i < maxIterations) {
			Result findRecordIds = db.execute(String.format(findIds, collectionId));
			sb.append(String.format(findIds, collectionId));
			sb.append("\n");
			logger.log(Level.INFO, "Executing: {0}", String.format(findIds, collectionId));

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
		sb.append("Executed query for deletion with relationships of collection ");
		sb.append(collectionId);
		sb.append(" in ");
		sb.append(String.valueOf(System.currentTimeMillis() - deleteTime));
		sb.append(" ms\n");
		return Response.ok(sb.toString()).build();
	}
}

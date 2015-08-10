package eu.europeana.corelib.ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.impl.util.StringLogger;

import eu.europeana.corelib.ordering.model.NaturalOrderNode;

@javax.ws.rs.Path("/fakeorder")
public class FakeOrder {
	final DynamicRelationshipType ISFAKEORDER = DynamicRelationshipType
			.withName("isFakeOrder");
	final DynamicRelationshipType ISLASTINSEQUENCE = DynamicRelationshipType
			.withName("isLastInSequence");
	final DynamicRelationshipType ISFIRSTINSEQUENCE = DynamicRelationshipType
			.withName("isFirstInSequence");
	final DynamicRelationshipType EDMISNEXTINSEQUENCE = DynamicRelationshipType
			.withName("edm:isNextInSequence");
	private GraphDatabaseService db;
	private ExecutionEngine engine;
	public FakeOrder(@Context GraphDatabaseService db) {
		this.db = db;
		  this.engine = new ExecutionEngine(db, StringLogger.SYSTEM);
	}
	@GET
	@javax.ws.rs.Path("/nodeId/{nodeId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generate(@PathParam("nodeId") String parentId) {

		Transaction tx = db.beginTx();
		Node node = db.index().forNodes("edmsearch2")
				.get("rdf_about", parentId).getSingle();
		if (node != null) {
			if (node.hasProperty("hasChildren")) {
				Iterator<Node> children = getChildren(parentId);
				
				List<NaturalOrderNode> ordered = getNaturalOrder(children);
				generateFakeRelationships(ordered);
				
				tx.success();
				tx.finish();

				List<NaturalOrderNode> getSeqData = getSequentialData(getSequenceStartChildren(parentId));
				if (ordered.size() > 0) {
					getSeqData.add(ordered.get(0));
				}
				tx = db.beginTx();
				generateFakeRelationships(getSeqData);
				tx.success();
				tx.finish();
			}
		}

		return null;
	}

	private List<NaturalOrderNode> getSequentialData(Iterator<Node> children) {
		List<NaturalOrderNode> firstChildren = getNaturalOrder(children);
		List<NaturalOrderNode> lastChildren = new ArrayList<NaturalOrderNode>();
		List<NaturalOrderNode> finalChildren = new ArrayList<NaturalOrderNode>();
		for (NaturalOrderNode node : firstChildren) {
			Transaction tx = db.beginTx();
			Node startNode = db.getNodeById(node.getNodeId());
			TraversalDescription traversal = db.traversalDescription();
			Traverser traverse = traversal.depthFirst()
					.relationships(EDMISNEXTINSEQUENCE, Direction.OUTGOING)
					.traverse(startNode);
			for (Node nodeRet : traverse.nodes()) {
				if (nodeRet.hasRelationship(ISLASTINSEQUENCE,
						Direction.INCOMING)) {
					NaturalOrderNode last = new NaturalOrderNode();
					last.setId(nodeRet.getProperty("rdf:about").toString());
					last.setNodeId(nodeRet.getId());
					if (nodeRet.hasProperty("dc:title_xml:lang_def")) {
						last.setTitle(nodeRet.getProperty(
								"dc:title_xml:lang_def").toString());
					}
					if (nodeRet.hasProperty("dc:description_xml:lang_def")) {
						last.setDescription(nodeRet.getProperty(
								"dc:description_xml:lang_def").toString());
					}
					if (nodeRet.hasProperty("dc:date_xml:lang_def")) {
						node.setDate(nodeRet.getProperty(
								"dc:date_xml:lang_def").toString());
					}
					if (nodeRet.hasProperty("dcterms:created_xml:lang_def")) {
						node.setCreated(nodeRet.getProperty(
								"dcterms:created_xml:lang_def").toString());
					}
					if (nodeRet.hasProperty("dcterms:issued_xml:lang_def")) {
						node.setIssued(nodeRet.getProperty(
								"dcterms:issued_xml:lang_def").toString());
					}
					lastChildren.add(last);
				}
			}
			tx.success();
			tx.finish();
		}
		for (int i = 0; i < firstChildren.size() - 1; i++) {
			finalChildren.add(firstChildren.get(i + 1));
			finalChildren.add(lastChildren.get(i));
		}
		if(lastChildren.size()>1){
			finalChildren.add(lastChildren.get(lastChildren.size() - 1));
		}
		return finalChildren;
	}

	private void generateFakeRelationships(List<NaturalOrderNode> ordered) {
		for (int i = 0; i < ordered.size() - 1; i++) {
			Transaction tx = db.beginTx();
			Node startNode = db.getNodeById(ordered.get(i).getNodeId());
			Node endNode = db.getNodeById(ordered.get(i + 1).getNodeId());
			startNode.createRelationshipTo(endNode, ISFAKEORDER);
			tx.success();
			tx.finish();
		}

	}

	private Iterator<Node> getChildren(String parentId) {
		Transaction tx = db.beginTx();
		ExecutionResult result = engine
				.execute("start n = node:edmsearch2(rdf_about=\""
						+ parentId
						+ "\") match (n)-[:`dcterms:hasPart`]->(part) RETURN part");
		Iterator<Node> columns = result.columnAs("part");
		tx.success();
		tx.finish();
		return columns;
	}

	private Iterator<Node> getSequenceStartChildren(String parentId) {
		Transaction tx = db.beginTx();
		ExecutionResult result = engine
				.execute("start n = node:edmsearch2(rdf_about=\""
						+ parentId
						+ "\") match (n)-[:`isFirstInSequence`]->(part) RETURN part");
		Iterator<Node> columns = result.columnAs("part");
		tx.success();
		tx.finish();
		return columns;
	}

	private List<NaturalOrderNode> getNaturalOrder(Iterator<Node> children) {
		List<Node> unordered = getUnordered(children);
		return order(unordered);
	}

	private List<Node> getUnordered(Iterator<Node> children) {
		Transaction tx = db.beginTx();
		List<Node> unordered = new ArrayList<Node>();
		while (children.hasNext()) {
			Node child = children.next();
			if (!child.hasRelationship(EDMISNEXTINSEQUENCE)) {
				unordered.add(child);
			}
		}
		tx.success();
		tx.finish();
		return unordered;
	}

	private List<NaturalOrderNode> order(List<Node> unordered) {
		List<NaturalOrderNode> unorderedNodes = new ArrayList<NaturalOrderNode>();
		for (Node unorderedNode : unordered) {
                    Transaction tx = db.beginTx();
			NaturalOrderNode node = new NaturalOrderNode();
			node.setId(unorderedNode.getProperty("rdf:about").toString());
			node.setNodeId(unorderedNode.getId());
			if (unorderedNode.hasProperty("dc:title_xml:lang_def")) {
				node.setTitle(unorderedNode
						.getProperty("dc:title_xml:lang_def").toString());
			}
			if (unorderedNode.hasProperty("dc:description_xml:lang_def")) {
				node.setDescription(unorderedNode.getProperty(
						"dc:description_xml:lang_def").toString());
			}
			if (unorderedNode.hasProperty("dc:date_xml:lang_def")) {
				node.setDate(unorderedNode.getProperty(
						"dc:date_xml:lang_def").toString());
			}
			if (unorderedNode.hasProperty("dcterms:created_xml:lang_def")) {
				node.setCreated(unorderedNode.getProperty(
						"dcterms:created_xml:lang_def").toString());
			}
			if (unorderedNode.hasProperty("dcterms:issued_xml:lang_def")) {
				node.setIssued(unorderedNode.getProperty(
						"dcterms:issued_xml:lang_def").toString());
			}
                        tx.success();
                        tx.finish();
			unorderedNodes.add(node);
		}
		Collections.sort(unorderedNodes);

		return unorderedNodes;
	}

}

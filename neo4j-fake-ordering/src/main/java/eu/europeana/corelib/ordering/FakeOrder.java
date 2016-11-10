package eu.europeana.corelib.ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

/**
 * @author gmamakis, luthien
 */

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

	/**
	 * - accepts a node Id get parameter
	 *   Step 1) isolated single orphan nodes:
	 * - Retrieve the parent node and its children;
	 * - apply a natural ordering to isolated orphan nodes among those children and
	 * - create a fakeRelationship between those isolated (but now ordered) orphan nodes.
	 *   Step 2) disjointed sequences:
	 * - Retrieve all children that have an incoming isFirstInSequence relation to the parent;
	 * - 
	 */
	@GET
	@javax.ws.rs.Path("/nodeId/{nodeId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generate(@PathParam("nodeId") String parentId) {

		Transaction tx = db.beginTx();
		Node node = db.index().forNodes("edmsearch2").get("rdf_about", parentId).getSingle();
		if (node != null) {
			if (node.hasProperty("hasChildren")) {
				Iterator<Node> childNodeIterator = getChildren(parentId);
				
				List<NaturalOrderNode> orderedOrphanNodeList = getOrderedOrphanNodes(childNodeIterator, true);
				generateFakeRelations(orderedOrphanNodeList);
				
				tx.success();
				tx.finish();

				List<NaturalOrderNode> firstInSequenceNodeList = getFirstInSequenceNodes(getSequenceStartNodes(parentId));
				if (orderedOrphanNodeList.size() > 0) {
					firstInSequenceNodeList.add(orderedOrphanNodeList.get(0));
				}
				tx = db.beginTx();
				generateFakeRelations(firstInSequenceNodeList);
				tx.success();
				tx.finish();
			}
		}
		return null;
	}

	// retrieve the children for a parent with given rdf_about = parentId
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

	// iterate over child nodes, cherry-pick those who aren't related to their siblings via either a fakeOrder or,
	// if ignoreEdmNext is false, an edmIsNextInSequence relation.
	// Send this list to the orderOrphanNodeList ...

	private List<NaturalOrderNode> getOrderedOrphanNodes(Iterator<Node> childNodeIterator, boolean considerEdmNext) {
		Transaction tx = db.beginTx();
		List<Node> unorderedOrphanNodeList = new ArrayList<Node>();
		while (childNodeIterator.hasNext()) {
			Node childNode = childNodeIterator.next();
			if (!((considerEdmNext && childNode.hasRelationship(EDMISNEXTINSEQUENCE)) || childNode.hasRelationship(ISFAKEORDER))) {
				unorderedOrphanNodeList.add(childNode);
			}
		}
		tx.success();
		tx.finish();
		return orderOrphanNodeList(unorderedOrphanNodeList);
	}

	// takes the list of disjointed child nodes, transforms it to a list of NaturalOrderedNodes and apply the natural
	// ordering to that list
	private List<NaturalOrderNode> orderOrphanNodeList(List<Node> unorderedOrphanNodeList) {
		List<NaturalOrderNode> orderedOrphanNodeList = new ArrayList<NaturalOrderNode>();
		for (Node unorderedOrphanNode : unorderedOrphanNodeList) {
			Transaction tx = db.beginTx();
			NaturalOrderNode orderedOrphanNode = new NaturalOrderNode();
			orderedOrphanNode.setId(unorderedOrphanNode.getProperty("rdf:about").toString());
			orderedOrphanNode.setNodeId(unorderedOrphanNode.getId());
			if (unorderedOrphanNode.hasProperty("dc:title_xml:lang_def")) {
				orderedOrphanNode.setTitle(((String[]) unorderedOrphanNode.getProperty("dc:title_xml:lang_def"))[0]);
			}
			if (unorderedOrphanNode.hasProperty("dc:description_xml:lang_def")) {
				orderedOrphanNode.setDescription(((String[]) unorderedOrphanNode.getProperty("dc:description_xml:lang_def"))[0]);
			}
			if (unorderedOrphanNode.hasProperty("dc:date_xml:lang_def")) {
				orderedOrphanNode.setDate(unorderedOrphanNode.getProperty("dc:date_xml:lang_def").toString());
			}
			if (unorderedOrphanNode.hasProperty("dcterms:created_xml:lang_def")) {
				orderedOrphanNode.setCreated(unorderedOrphanNode.getProperty("dcterms:created_xml:lang_def").toString());
			}
			if (unorderedOrphanNode.hasProperty("dcterms:issued_xml:lang_def")) {
				orderedOrphanNode.setIssued(((String[]) unorderedOrphanNode.getProperty("dcterms:issued_xml:lang_def"))[0]);
			}
			tx.success();
			tx.finish();
			orderedOrphanNodeList.add(orderedOrphanNode);
		}
		Collections.sort(orderedOrphanNodeList);
		return orderedOrphanNodeList;
	}

	private void generateFakeRelations(List<NaturalOrderNode> naturalOrderNodeList) {
		for (int i = 0; i < naturalOrderNodeList.size() - 1; i++) {
			Transaction tx = db.beginTx();
			Node startNode = db.getNodeById(naturalOrderNodeList.get(i).getNodeId());
			Node endNode = db.getNodeById(naturalOrderNodeList.get(i + 1).getNodeId());
			startNode.createRelationshipTo(endNode, ISFAKEORDER);
			tx.success();
			tx.finish();
		}

	}

	private Iterator<Node> getSequenceStartNodes(String parentId) {
		Transaction tx = db.beginTx();
		ExecutionResult result = engine
				.execute("start n = node:edmsearch2(rdf_about=\""
						+ parentId
						+ "\") match (n)-[:`isFirstInSequence`]->(part) RETURN part");
		Iterator<Node> startNodeIterator = result.columnAs("part");
		tx.success();
		tx.finish();
		return startNodeIterator;
	}

	private List<NaturalOrderNode> getFirstInSequenceNodes(Iterator<Node> startNodeIterator) {
		List<NaturalOrderNode> firstChildren = getOrderedOrphanNodes(startNodeIterator, false);
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
                                            last.setTitle(((String[]) nodeRet.getProperty("dc:title_xml:lang_def"))[0]);
					}
					if (nodeRet.hasProperty("dc:description_xml:lang_def")) {
                                            last.setDescription(((String[]) nodeRet.getProperty("dc:description_xml:lang_def"))[0]);
					}
					if (nodeRet.hasProperty("dc:date_xml:lang_def")) {
                                            last.setDate(nodeRet.getProperty("dc:date_xml:lang_def").toString());
					}
					if (nodeRet.hasProperty("dcterms:created_xml:lang_def")) {
                                            last.setCreated(nodeRet.getProperty("dcterms:created_xml:lang_def").toString());
					}
					if (nodeRet.hasProperty("dcterms:issued_xml:lang_def")) {
                                            last.setIssued(((String[]) nodeRet.getProperty("dcterms:issued_xml:lang_def"))[0]);
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

}

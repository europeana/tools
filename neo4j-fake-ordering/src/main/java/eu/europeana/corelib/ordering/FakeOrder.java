package eu.europeana.corelib.ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.impl.util.StringLogger;

import eu.europeana.corelib.ordering.model.NaturalOrderNode;

/**
 * @author gmamakis, luthien
 */

@javax.ws.rs.Path("/fakeorder")
public class FakeOrder {
	private static final RelationshipType ISFAKEORDER = DynamicRelationshipType.withName("isFakeOrder");
	private static final RelationshipType ISLASTINSEQUENCE = DynamicRelationshipType.withName("isLastInSequence");
	private static final RelationshipType ISFIRSTINSEQUENCE = DynamicRelationshipType.withName("isFirstInSequence");
	private static final RelationshipType EDMISNEXTINSEQUENCE = DynamicRelationshipType.withName("edm:isNextInSequence");
	private static final RelationshipType HAS_PART = DynamicRelationshipType.withName("dcterms:hasPart");

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
		Node node;
		List<NaturalOrderNode> orderedOrphanNodeList = null;
		List<NaturalOrderNode> firstInSequenceNodeList;
		try ( Transaction tx = db.beginTx() ) {
			node = db.index().forNodes("edmsearch2").get("rdf_about", parentId).getSingle();
			if (node != null && node.hasProperty("hasChildren")) {
				orderedOrphanNodeList = getOrderedOrphanNodes(getChildren(parentId, false), true);
				generateFakeRelations(orderedOrphanNodeList);
			}
			tx.success();
		}
		try ( Transaction tx = db.beginTx() ) {
			if (node != null && node.hasProperty("hasChildren")) {
				firstInSequenceNodeList = getFirstInSequenceNodes(getChildren(parentId, true));
				if (orderedOrphanNodeList.size() > 0) {
					firstInSequenceNodeList.add(orderedOrphanNodeList.get(0));
				}
				generateFakeRelations(firstInSequenceNodeList);
			}
			tx.success();
		}
		return null;
	}

	// retrieve the children for a parent with given rdf_about = parentId
	private Iterator<Node> getChildren(String parentId, boolean seqStartOnly) {
		String rdfAbout = fixSlashes(parentId);
		List<Node> children = new ArrayList<>();
		try ( Transaction tx = db.beginTx() ) {
			IndexManager    index      = db.index();
			Index<Node>     edmsearch2 = index.forNodes("edmsearch2");
			IndexHits<Node> hits       = edmsearch2.get("rdf_about", rdfAbout);
			Node            parent     = hits.getSingle();
			if (parent == null) {
				throw new IllegalArgumentException("no node found in index for rdf_about = " + rdfAbout);
			}
			// if seqStartOnly, then get sequence start nodes; else, get all children
			for (Relationship r1 : parent.getRelationships(Direction.OUTGOING, seqStartOnly? ISFIRSTINSEQUENCE : HAS_PART)) {
				children.add(r1.getEndNode());
			}
			tx.success();
			return children.iterator();
		}
	}

	// iterate over child nodes, cherry-pick those who aren't related to their siblings via either a fakeOrder or,
	// if ignoreEdmNext is false, an edmIsNextInSequence relation.
	// Send this list to the orderOrphanNodeList ...

	private List<NaturalOrderNode> getOrderedOrphanNodes(Iterator<Node> childNodeIterator, boolean considerEdmNext) {
		try ( Transaction tx = db.beginTx() ) {
			List<Node> unorderedOrphanNodeList = new ArrayList<Node>();
			while (childNodeIterator.hasNext()) {
				Node childNode = childNodeIterator.next();
				if (!((considerEdmNext && childNode.hasRelationship(EDMISNEXTINSEQUENCE))
					|| childNode.hasRelationship(ISFAKEORDER))) {
					unorderedOrphanNodeList.add(childNode);
				}
			}
			tx.success();
			return orderOrphanNodeList(unorderedOrphanNodeList);
		}
	}

	// takes the list of disjointed child nodes, transforms it to a list of NaturalOrderedNodes and apply the natural
	// ordering to that list
	private List<NaturalOrderNode> orderOrphanNodeList(List<Node> unorderedOrphanNodeList) {
		List<NaturalOrderNode> orderedOrphanNodeList = new ArrayList<NaturalOrderNode>();
		for (Node unorderedOrphanNode : unorderedOrphanNodeList) {
			try ( Transaction tx = db.beginTx() ) {
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
				orderedOrphanNodeList.add(orderedOrphanNode);
			}
		}
		Collections.sort(orderedOrphanNodeList);
		return orderedOrphanNodeList;
	}

	private void generateFakeRelations(List<NaturalOrderNode> naturalOrderNodeList) {
		for (int i = 0; i < naturalOrderNodeList.size() - 1; i++) {
			try ( Transaction tx = db.beginTx() ) {
				Node startNode = db.getNodeById(naturalOrderNodeList.get(i).getNodeId());
				Node endNode = db.getNodeById(naturalOrderNodeList.get(i + 1).getNodeId());
				startNode.createRelationshipTo(endNode, ISFAKEORDER);
				tx.success();
			}
		}
	}

	private List<NaturalOrderNode> getFirstInSequenceNodes(Iterator<Node> startNodeIterator) {
		List<NaturalOrderNode> firstChildren = getOrderedOrphanNodes(startNodeIterator, false);
		List<NaturalOrderNode> lastChildren = new ArrayList<NaturalOrderNode>();
		List<NaturalOrderNode> finalChildren = new ArrayList<NaturalOrderNode>();
		for (NaturalOrderNode node : firstChildren) {
			try ( Transaction tx = db.beginTx() ) {
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
			}
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

	private String fixSlashes(String rdfAbout){
		rdfAbout.replace("%2F", "/");
		if (rdfAbout.contains("/") && !rdfAbout.startsWith("/")){
			rdfAbout = "/" + rdfAbout;
		}
		return rdfAbout;
	}

}

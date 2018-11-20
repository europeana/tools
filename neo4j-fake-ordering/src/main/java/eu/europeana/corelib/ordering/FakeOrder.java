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

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import eu.europeana.corelib.ordering.model.NaturalOrderNode;

/**
 * @author gmamakis, luthien
 */

@javax.ws.rs.Path("/fakeorder")
public class FakeOrder {
	private static final RelationshipType ISFAKEORDER          = RelationshipType.withName("isFakeOrder");
	private static final RelationshipType ISLASTINSEQUENCE     = RelationshipType.withName("isLastInSequence");
	private static final RelationshipType ISFIRSTINSEQUENCE    = RelationshipType.withName("isFirstInSequence");
	private static final RelationshipType EDMISNEXTINSEQUENCE  = RelationshipType.withName("edm:isNextInSequence");
	private static final RelationshipType HAS_PART             = RelationshipType.withName("dcterms:hasPart");

	private static final String DCTITLE 		= "dc:title_xml:lang_def";
	private static final String DCDESCRIPTION 	= "dc:description_xml:lang_def";
	private static final String DCDATE 			= "dc:date_xml:lang_def";
	private static final String DCTERMS_CREATED = "dcterms:created_xml:lang_def";
	private static final String DCTERMS_ISSUED 	= "dcterms:issued_xml:lang_def";
	private static final String HAS_CHILDREN 	= "hasChildren";
	private static final String RDF_ABOUT 		= "rdf:about";
	private static final String EDMSEARCH2 		= "edmsearch2";

	private GraphDatabaseService db;

	public FakeOrder(@Context GraphDatabaseService db) {
		this.db = db;
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
			node = db.index().forNodes(EDMSEARCH2).get(RDF_ABOUT, parentId).getSingle();
			if (node != null && node.hasProperty(HAS_CHILDREN)) {
				orderedOrphanNodeList = getOrderedOrphanNodes(getChildren(parentId, false), true);
				generateFakeRelations(orderedOrphanNodeList);
			}
			tx.success();
		}
		try ( Transaction tx = db.beginTx() ) {
			if (node != null && node.hasProperty(HAS_CHILDREN)) {
				firstInSequenceNodeList = getFirstInSequenceNodes(getChildren(parentId, true));
				if (null != orderedOrphanNodeList && !orderedOrphanNodeList.isEmpty()) {
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
			Index<Node>     edmsearch2 = index.forNodes(EDMSEARCH2);
			IndexHits<Node> hits       = edmsearch2.get(RDF_ABOUT, rdfAbout);
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
			List<Node> unorderedOrphanNodeList = new ArrayList<>();
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
		List<NaturalOrderNode> orderedOrphanNodeList = new ArrayList<>();
		for (Node unorderedOrphanNode : unorderedOrphanNodeList) {
			try ( Transaction tx = db.beginTx() ) {
				NaturalOrderNode orderedOrphanNode = new NaturalOrderNode();
				orderedOrphanNode.setId(unorderedOrphanNode.getProperty(RDF_ABOUT).toString());
				orderedOrphanNode.setNodeId(unorderedOrphanNode.getId());
				if (unorderedOrphanNode.hasProperty(DCTITLE)) {
					orderedOrphanNode.setTitle(((String[]) unorderedOrphanNode.getProperty(DCTITLE))[0]);
				}
				if (unorderedOrphanNode.hasProperty(DCDESCRIPTION)) {
					orderedOrphanNode.setDescription(((String[]) unorderedOrphanNode.getProperty(DCDESCRIPTION))[0]);
				}
				if (unorderedOrphanNode.hasProperty(DCDATE)) {
					orderedOrphanNode.setDate(unorderedOrphanNode.getProperty(DCDATE).toString());
				}
				if (unorderedOrphanNode.hasProperty(DCTERMS_CREATED)) {
					orderedOrphanNode.setCreated(unorderedOrphanNode.getProperty(DCTERMS_CREATED).toString());
				}
				if (unorderedOrphanNode.hasProperty(DCTERMS_ISSUED)) {
					orderedOrphanNode.setIssued(((String[]) unorderedOrphanNode.getProperty(DCTERMS_ISSUED))[0]);
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
		List<NaturalOrderNode> lastChildren  = new ArrayList<>();
		List<NaturalOrderNode> finalChildren = new ArrayList<>();
		for (NaturalOrderNode node : firstChildren) {
			try ( Transaction tx = db.beginTx() ) {
				Node startNode = db.getNodeById(node.getNodeId());
				TraversalDescription traversal = db.traversalDescription();
				Traverser traverse = traversal.depthFirst()
						.relationships(EDMISNEXTINSEQUENCE, Direction.OUTGOING)
						.traverse(startNode);
				for (Node nodeRet : traverse.nodes()) {
					if (nodeRet.hasRelationship(ISLASTINSEQUENCE, Direction.INCOMING)) {
						NaturalOrderNode last = new NaturalOrderNode();
						last.setId(nodeRet.getProperty(RDF_ABOUT).toString());
						last.setNodeId(nodeRet.getId());
						if (nodeRet.hasProperty(DCTITLE)) {
							last.setTitle(((String[]) nodeRet.getProperty(DCTITLE))[0]);
						}
						if (nodeRet.hasProperty(DCDESCRIPTION)) {
							last.setDescription(((String[]) nodeRet.getProperty(DCDESCRIPTION))[0]);
						}
						if (nodeRet.hasProperty(DCDATE)) {
							last.setDate(nodeRet.getProperty(DCDATE).toString());
						}
						if (nodeRet.hasProperty(DCTERMS_CREATED)) {
							last.setCreated(nodeRet.getProperty(DCTERMS_CREATED).toString());
						}
						if (nodeRet.hasProperty(DCTERMS_ISSUED)) {
							last.setIssued(((String[]) nodeRet.getProperty(DCTERMS_ISSUED))[0]);
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
		StringUtils.replace(rdfAbout, "%2F", "/");
		if (rdfAbout.contains("/") && !rdfAbout.startsWith("/")){
			rdfAbout = "/" + rdfAbout;
		}
		return rdfAbout;
	}

}

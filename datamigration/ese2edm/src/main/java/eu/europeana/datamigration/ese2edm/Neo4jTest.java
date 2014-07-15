/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.datamigration.ese2edm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.traversal.RestTraversal;

import eu.europeana.corelib.neo4j.entity.RelType;
import eu.europeana.corelib.neo4j.entity.Relation;
import eu.europeana.corelib.solr.server.Neo4jServer;
import eu.europeana.corelib.solr.server.impl.Neo4jServerImpl;

/**
 *
 * @author gmamakis
 */
public class Neo4jTest {
	static RestGraphDatabase graphDb = new RestGraphDatabase(
			"http://localhost:7474/db/data/");
	static Neo4jServer neoServer;
	public static void main(String[] args) {

		RestIndex<RestNode> nodeIndex = graphDb.getRestAPI().getIndex(
				"edmsearch2");
		IndexHits<RestNode> nodes = nodeIndex.get("rdf_about",
				"/9200300/BibliographicResource_3000051785565");

		if (nodes.getSingle() != null) {
			
			getNextSiblings(nodes.getSingle(), 10);
			getPreviousSiblings(nodes.getSingle(), 10);
			getParent(nodes.getSingle());
			getChildren(nodes.getSingle(), 0, 10);
		}
		
		neoServer = new Neo4jServerImpl("http://sandbox05.isti.cnr.it:7474/db/data/", "edmsearch2", "http://sandbox05.isti.cnr.it:7474/europeana/hierarchycount/nodeId");
		List<Node> children = neoServer.getChildren(neoServer.getNode("/9200300/BibliographicResource_3000052917524"), 0, 10);
		System.out.println("\n");
		System.out.println("\n");
		System.out.println("\n");
		System.out.println("\n");
		System.out.println("\n");
		for (Node child : children) {
			System.out.println("Children are "+ child.getProperty("rdf:about"));
		}
		getIndex(nodes.getSingle());
		getChildrenCount(neoServer.getNode("/9200300/BibliographicResource_3000052917524"));

	}

	private static void getChildrenCount(Node single) {
		System.out.println(neoServer.getChildrenCount(single));
		
	}

	private static List<Node> getNextSiblings(Node node, int limit) {
		Date start = new Date();

		List<Node> children = new ArrayList<Node>();
		RestTraversal traversal = (RestTraversal) graphDb
				.traversalDescription();

		traversal.evaluator(Evaluators.excludeStartPosition());

		traversal.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
		traversal.breadthFirst();
		traversal.maxDepth(limit);
		// traversal.relationships(RelType.DCTERMS_HASPART,
		// Direction.OUTGOING);
		traversal.relationships(
				new Relation(RelType.EDM_ISNEXTINSEQUENCE.getRelType()),
				Direction.INCOMING);
		Traverser tr = traversal.traverse(node);
		Iterator<Node> resIter = tr.nodes().iterator();

		while (resIter.hasNext()) {
			Node path = resIter.next();
			// path.lastRelationship();
			// while(children.size()<10){
			children.add(path);
			// }

		}
		System.out.println(children.size());
		System.out.println(new Date().getTime() - start.getTime());
		for (Node child : children) {
			System.out.println(child.getProperty("rdf:about"));
		}
		return (children);
	}

	private static void getPreviousSiblings(Node node, int limit) {
		Date start = new Date();
		List<Node> children = new ArrayList<Node>();
		RestTraversal traversal = (RestTraversal) graphDb
				.traversalDescription();

		traversal.evaluator(Evaluators.excludeStartPosition());

		traversal.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
		traversal.breadthFirst();
		traversal.maxDepth(limit);
		// traversal.relationships(RelType.DCTERMS_HASPART,
		// Direction.OUTGOING);
		traversal.relationships(
				new Relation(RelType.EDM_ISNEXTINSEQUENCE.getRelType()),
				Direction.OUTGOING);
		Traverser tr = traversal.traverse(node);
		Iterator<Node> resIter = tr.nodes().iterator();

		while (resIter.hasNext()) {
			Node path = resIter.next();
			// path.lastRelationship();
			// while(children.size()<10){
			children.add(path);
			// }

		}
		System.out.println(children.size());
		System.out.println(new Date().getTime() - start.getTime());
		for (Node child : children) {
			System.out.println(child.getProperty("rdf:about"));
		}
	}

	private static void getParent(Node node) {
		Date start = new Date();
		List<Node> children = new ArrayList<Node>();
		RestTraversal traversal = (RestTraversal) graphDb
				.traversalDescription();

		traversal.evaluator(Evaluators.excludeStartPosition());

		traversal.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
		traversal.breadthFirst();
		traversal.maxDepth(1);
		// traversal.relationships(RelType.DCTERMS_HASPART,
		// Direction.OUTGOING);
		traversal.relationships(
				new Relation(RelType.DCTERMS_ISPARTOF.getRelType()),
				Direction.OUTGOING);
		Traverser tr = traversal.traverse(node);
		Iterator<Node> resIter = tr.nodes().iterator();

		while (resIter.hasNext()) {
			Node path = resIter.next();
			// while(children.size()<10){
			children.add(path);
			// }

		}
		System.out.println(children.size());
		System.out.println(new Date().getTime() - start.getTime());
		for (Node child : children) {
			System.out.println(child.getProperty("rdf:about"));
		}
	}

	private static void getChildren(Node node, int offset, int limit) {
		Date start = new Date();
		List<Node> children = new ArrayList<Node>();
		RestTraversal traversal = (RestTraversal) graphDb
				.traversalDescription();
		traversal.evaluator(Evaluators.excludeStartPosition());

		traversal.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
		traversal.breadthFirst();
		traversal.maxDepth(1);

		traversal.relationships(
				new Relation(RelType.ISFIRSTINSEQUENCE.getRelType()),Direction.OUTGOING);

		Traverser tr = traversal.traverse(node);
		Iterator<Node> resIter = tr.nodes().iterator();
		while (resIter.hasNext()) {

			Node child = resIter.next();

			if(offset==0){
                children.add(child);
                children.addAll(getNextSiblings(child, limit-1));
                } else {
                    children.addAll(getNextSiblings(child,offset+limit));
                }
		}
		List<Node> subChildren = children.subList(offset, children.size()>limit? limit : children.size());
		System.out.println(subChildren.size());
		System.out.println(new Date().getTime() - start.getTime());
		for (Node child : subChildren) {
			System.out.println(child.getProperty("rdf:about"));
		}
	}
	
	private static void getIndex(Node node){
		System.out.println(neoServer.getNodeIndex(node));
	}

}

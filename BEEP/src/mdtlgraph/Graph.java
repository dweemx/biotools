package mdtlgraph;
import java.util.ArrayList;

public class Graph {
			
	public Graph() {
		
	}
	
	public void addEdge(Edge edge) {
//		if(!this.edges.contains(edge))
			this.edges.add(edge);
	}
	
	public void addNode(Node node) {
		if(!this.nodes.contains(node))
			this.nodes.add(node);
	}
	
	public void setRoot(Node rootNode) {
		this.root = rootNode;
	}
	
	Node root;
	
	public ArrayList<Edge> getEdgeListBySpeciesId2(String speciesId) {
		ArrayList<Edge> tmp = new ArrayList<Edge>();
		for(Edge e: edges) {
			if(e.getSourceNode().getSpeciesId().equals(speciesId) || 
					e.getTargetNode().getSpeciesId().equals(speciesId))
				tmp.add(e);
		}
		return tmp;
	}
	
	public ArrayList<ArrayList<Edge>> getEdgeListBySpeciesId(String speciesId) {
		ArrayList<ArrayList<Edge>> tmp = new ArrayList<ArrayList<Edge>>();
		ArrayList<Edge> source = new ArrayList<Edge>();
		ArrayList<Edge> target = new ArrayList<Edge>();
		for(Edge e: edges) {
			if(e.getSourceNode().getSpeciesId() == speciesId)
				source.add(e);
			if(e.getTargetNode().getSpeciesId() == speciesId)
				target.add(e);
		}
		tmp.add(source);
		tmp.add(target);
		return tmp;
	}
	
	public ArrayList<Edge> getEdgeListByEventType(EventType eventType) {
		ArrayList<Edge> tmp = new ArrayList<Edge>();
		for(Edge e: edges) {
			if(e.getEventType() == eventType) {
				tmp.add(e);
			}
		}
		return tmp;
	}
	
	/**
	 * Store all the edges for this graph
	 */
	ArrayList<Edge> edges = new ArrayList<Edge>();
	
	/**
	 * If the node does exist in the graph return the node given
	 * the clade id and the species id otherwise create a new new
	 * node with the given clade id and the given species id and return
	 * this newly created node
	 * 
	 * @param cladeId
	 * @param speciesId
	 * @return
	 */
	public Node getNode(String cladeId, String speciesId) {
//		System.out.println(cladeId+"####"+speciesId);
		Node n = getNodeByCladeAndSpeciesId(cladeId, speciesId);
		if(n == null)
			return new Node(cladeId,speciesId);
		return n;
	}
	
	public Node getNodeByCladeAndSpeciesId(String cladeId, String speciesId) {
		for(Node n: nodes) {
			/*
			 * It can happen that for some lines in the reconciliation 
			 * file there is neither final cladeId_l nor cladeId_r
			 * (When 1 LeftEventSplit, 1 or multiple NoSplitEvent
			 * and 0 RightEventSplit)
			 */
			if(n.getCladeId() == null)
				continue;
			if(n.getCladeId().equals(cladeId) && n.getSpeciesId().equals(speciesId))
				return n;
		}
		return null;
	}
	
	public Node getNodeBySpeciesId(int speciesId) {
		for(Node n: nodes)
			if(n.getSpeciesId().equals(speciesId))
				return n;
		return null;
	}
	
	public Node getNodeByCladeId(String cladeId) {
		for(Node n: nodes)
			if(n.getCladeId().equals(cladeId))
				return n;
		return null;
	}
	
	/**
	 * Store all the nodes for this graph
	 */
	ArrayList<Node> nodes = new ArrayList<Node>();
	
	public void print() {
		System.out.println("Nodes: "+nodes.size());
		System.out.println("############ NODES ###########");
		for(Node n: nodes) {
			System.out.println("Node: "+n);
		}
		System.out.println("Edges: "+edges.size());
		System.out.println("############ EDGES ###########");
		for(Edge e: edges) {
			System.out.println("Edge: "+e);
		}
	}
}

package mdtlgraph;

public class Node {
	
	public Node(String cladeId, String speciesId) {
		this.setCladeId(cladeId);
		this.setSeciesId(speciesId);
	}
	
	/**
	 * Get the species Id for this Node
	 * 
	 * @return Species Id number for this node
	 */
	public String getSpeciesId() {
		return seciesId;
	}

	void setSeciesId(String seciesId) {
		this.seciesId = seciesId;
	}
	
	/**
	 * Store the species Id for this Node
	 */
	private String seciesId;

	/**
	 * Get the clade Id for this Node
	 * 
	 * @return Clade Id number for this node
	 */
	public String getCladeId() {
		return cladeId;
	}

	void setCladeId(String cladeId) {
		this.cladeId = cladeId;
	}
	
	/**
	 * Store the clade Id for this Node
	 */
	private String cladeId;

	public Node getParent() {
		return this.parent;
	}
	
	public void setParent(Node parentNode) {
		this.parent = parentNode;
	}
	
	Node parent;
	
	@Override
	public String toString() {
		return this.getCladeId()+":"+this.getSpeciesId();
	}
	
}

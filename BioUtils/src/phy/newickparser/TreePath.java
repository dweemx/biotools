package phy.newickparser;

import java.util.ArrayList;

public class TreePath {
	
	public TreePath(TreeNode b) {
		this.setB(b);
	}
		
	public TreePath(TreeNode a, TreeNode b) {
		this.setA(a);
		this.setB(b);
	}
	
	public TreePath() {
	}
	
	public void init() {
		nodes.removeAll(nodes);
		this.setA(null);
		this.setB(null);
	}

	public double getTotalBranchLength() {
		double length = 0;
		if(this.getA() != null)
			length += this.getA().getBranchLength();
		for(TreeNode node: this.internalNodes) {
			length += node.getBranchLength();
		}
		length += this.getB().getBranchLength();
		// Branch length is stored in child nodes
		return length;
	}
	
	public void removeLast() {
		nodes.remove(nodes.size()-1);
	}
	
	public void addNode(TreeNode node) {
		nodes.add(node);
	}
	
	public ArrayList<TreeNode> getNodes() {
		return nodes;
	}
	
	private ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
	
	public int[] getLCAIndexes(TreePath treePath) {
		int treePathNodesLCAIndex = -1, thisTreePathNodesLCAIndex = -1;
		ArrayList<TreeNode> treePathNodes = treePath.getNodes();
		for(int i=treePathNodes.size()-1; i>=0; i--) {
			ArrayList<TreeNode> thisTreePathNodes = this.getNodes();
			for(int j=thisTreePathNodes.size()-1; j>=0; j--) {
				treePathNodesLCAIndex = treePathNodes.get(i).getId();
				thisTreePathNodesLCAIndex = thisTreePathNodes.get(j).getId();
				if(treePathNodesLCAIndex == thisTreePathNodesLCAIndex) {
					return new int[]{treePathNodesLCAIndex,thisTreePathNodesLCAIndex};
				}
			}
		}
		throw new IllegalArgumentException("No LCA found for "+
				((TreeLeafNode)treePath.getB()).getName()+" and "+
				((TreeLeafNode)this.getB()).getName()+" !");
	}
	
	/**
	 * Get the non overlapping segment of this TreePath with the
	 * given treePath without the LCA
	 * 
	 * @param treePath
	 * @return
	 */
	public TreePath getNonOverlappingTreePath(TreePath treePath) {
		TreePath nonOverlappingTreePath = new TreePath(treePath.getB(),this.getB());
		ArrayList<TreeNode> treePathNodes = treePath.getNodes();
		ArrayList<TreeNode> thisTreePathNodes = this.getNodes();
		// Add the nodes from the given treePath
		int[] lCAIndexes = this.getLCAIndexes(treePath);
		// -2: Don't consider the Leaf node (already added)
		for(int i=treePathNodes.size()-2; i>=0; i--) {
			// Strictly greater than because don't take LCA
			TreeNode tn = treePathNodes.get(i);
			if(tn.getId() != lCAIndexes[0]) {
				nonOverlappingTreePath.addInternalNode(tn);
			} else {
				this.setLCA(tn);
				break;
			}
		}
		// Add the nodes from this TreePath
		// -2: Don't consider the Leaf node (already added)
		for(int i=thisTreePathNodes.size()-2; i>=0; i--) {
			// Strictly greater than because don't take LCA
			TreeNode tn = thisTreePathNodes.get(i);
			if(tn.getId() != lCAIndexes[1]) {
				nonOverlappingTreePath.addInternalNode(tn);
			} else {
				break;
			}
		}
		return nonOverlappingTreePath;
	}
	
	/**
	 * Get the total distance of the parts of the paths that
	 * are not overlapping
	 * 
	 * @return
	 */
	public double getNonOverlappingDistance(TreePath treePath) {
		return this.getNonOverlappingTreePath(treePath).getTotalBranchLength();
	}
	
	public TreeNode getLCA() {
		return lca;
	}

	public void setLCA(TreeNode lca) {
		this.lca = lca;
	}
	
	/**
	 * TreeNode corresponding to the Least Common Ancestor
	 */
	private TreeNode lca;

	public TreeNode getB() {
		return b;
	}

	/**
	 * Set the leaf of this TreePath
	 * 
	 * @param b
	 */
	public void setB(TreeNode b) {
		this.nodes.add(b);
		this.b = b;
	}
	
	private TreeNode b;

	public TreeNode getA() {
		return a;
	}

	public void setA(TreeNode a) {
		this.nodes.add(a);
		this.a = a;
	}
	
	private TreeNode a;
	
	public boolean hasTreeNode(TreeNode node) {
		if(this.getA().equals(node))
			return true;
		if(this.getB().equals(node))
			return true;
		if(this.getLCA() != null)
			if(this.getLCA().equals(node))
				return true;
		return this.internalNodes.contains(node);
	}
	
	public void addInternalNode(int index, TreeNode node) {
		this.nodes.add(index,node);
		this.internalNodes.add(index,node);
	}
	
	public void addInternalNode(TreeNode node) {
		this.nodes.add(node);
		this.internalNodes.add(node);
	}

	ArrayList<TreeNode> internalNodes = new ArrayList<TreeNode>();
}

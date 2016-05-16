package phy.newickparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

public class TreeInternalNode extends TreeNode {

	public TreeInternalNode(int id) {
		super(id);
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	private String label;
	
	public double getBranchSupport() {
		if(this.getLabel() == null)
			return 0.0;
		try {
			branchSupport = Double.parseDouble(this.getLabel());
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("No branch support in the tree!");
		}
		return branchSupport;
	}

//	public void setBranchSupport(double branchSupport) {
//		this.branchSupport = branchSupport;
//	}
	
	private double branchSupport;

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	private boolean isRoot;
	
	public List<Integer> getLeafSet() {
		List<Integer> leafSet = new ArrayList<Integer>();
		for(TreePath tp: cladePaths.values()) {
			leafSet.add(Integer.parseInt(((TreeLeafNode)tp.getB()).getName()));
		}
		return leafSet;
	}
	
	public boolean hasEndingLeaf(TreeNode node) {
		return cladePaths.containsKey(node.getId());
	}
	
	public void addEndLeaf(int id, TreePath path) {
		cladePaths.put(id, path);
	}

	public HashMap<Integer, TreePath> getEndLeaves() {
		return cladePaths;
	}
	
	/**
	 * <Id of the TreeNode, Distance to ending leaf>
	 */
	private HashMap<Integer,TreePath> cladePaths = new HashMap<Integer,TreePath>();
	
	public boolean hasPolytomy() {
		return childrenNodes.size() > 2;
	}
	
	public TreeNode getChildrenNodeByEndingLeaf(TreeNode node) {
		for(TreeNode child: childrenNodes) {
			if(child instanceof TreeLeafNode) {
				if(child.equals(node)) 
					return child;
			} else {
				if(((TreeInternalNode) child).hasEndingLeaf(node))
					return child;
			}
		}
		return null;
	}
	
	public void addChildrenNode(TreeNode node) {
		childrenNodes.add(node);
	}
	
	public ArrayList<TreeNode> getChildrenNodes() {
		return childrenNodes;
	}
	
	private ArrayList<TreeNode> childrenNodes = new ArrayList<TreeNode>();
	
	public void resolve() {
		// Save the current childrenNodes
		ArrayList<TreeNode> tmpChildrenNodes = new ArrayList<TreeNode>(childrenNodes);
		// Delete all childrenNodes
		this.childrenNodes.clear();
		/* Regrafting procedure
		 * 1) Create a new TreeInternalNode
		 * 2) Set its branch length to 0 
		 * 3) Set its parent to this TreeNode
		 * 4) Add this new TreeInternalNode as a child to this TreeNode
		 * 5) Pick a random child from this TreeNode
		 * 6) Remove it from this TreeNode's children
		 * 7) Set is parent to this TreeNode
		 * 8) Add this randomly picked child to this TreeNode
		 */
		// 1) -> 4)
		TreeInternalNode tin = new TreeInternalNode(999);
		tin.setParent(this);
		tin.setBranchLength(4.0);
		this.addChildrenNode(tin);
		
		// 5) -> 8)
		Random random = new Random();
		int min = 0, max = tmpChildrenNodes.size()-1;
		// Generate random number in range : http://stackoverflow.com/questions/20389890/generating-a-random-number-between-1-and-10-java
		int randomIndex = random.nextInt(max - min + 1) + min;
		TreeNode randomChild = tmpChildrenNodes.get(randomIndex);
		tmpChildrenNodes.remove(randomIndex);
		randomChild.setParent(this);
		this.addChildrenNode(randomChild);
		/*
		 * Add to the newly created node all the childrenNodes of this TreeInternalNode
		 * except the one that was randomly picked
		 */
		tin.childrenNodes.addAll(tmpChildrenNodes);
		/*
		 * Check if the newly created TreeInternalNode has a polytomy,
		 * if it's the case continue to resolve
		 */
		if(tin.hasPolytomy()) {
			tin.resolve();
		}
	}
	
	static int count = 0;
	@Override
	public String toString() {
		ArrayList<String> newickList = new ArrayList<String>();
		for(TreeNode tn: childrenNodes) {
			String text = tn+"";
//			if(!text.equals("null")) 
				newickList.add(text);
		}
		/*
		 * If the size is 1 then collapse it. Not mandatory 
		 * but ecceTERA considers a leaf between brackets as
		 * a non-binary node. So we have to remove the brackets
		 * for the internal nodes having only 1 node
		 */
		String isEnd = isRoot ? ";" : "";
//		if(newickList.size() > 1) {
			if(this.getLabel() != null) {
				// ecceTERA cannot deal with special characters in their labels
				String label = this.getLabel()
						.replace("(", "").replace(")", "")
						.replace(":", "").replace(";", "");
				return "("+StringUtils.join(newickList,",")+")"+":"+this.getDecimalBranchLength()+isEnd;
//				return "("+StringUtils.join(newickList,",")+")"+label+":"+this.getBranchLength()+(isRoot ? ";" : "");
			}
			if(this.getBranchSupport() == 0.0) 
				return "("+StringUtils.join(newickList,",")+")"+":"+this.getDecimalBranchLength()+isEnd;
			return "("+StringUtils.join(newickList,",")+")"+this.getBranchSupport()+":"+this.getDecimalBranchLength()+isEnd;
//		} 
//		else {
//			/*
//			 * If we remove a leaf from the tree then we need to collapse
//			 * its parent TreeInternalNode
//			 */
//			if(this.getLabel() != null)
//				return StringUtils.join(newickList,",")+isEnd;
////				return "("+StringUtils.join(newickFormat,",")+")"+this.getLabel()+":"+this.getBranchLength()+(isRoot ? ";" : "");
//			if(this.getBranchSupport() == 0.0) 
//				return StringUtils.join(newickList,",")+isEnd;
//			return StringUtils.join(newickList,",")+isEnd;
//		}
	}
	
}

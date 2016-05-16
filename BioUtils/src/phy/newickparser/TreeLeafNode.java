package phy.newickparser;

import java.util.Random;

public class TreeLeafNode extends TreeNode {

	public TreeLeafNode(int id, String name) {
		super(id);
		this.setName(name);
		this.setComplete(true);
	}
	
	public String getBioProjectID() {
		int indexUid = this.getName().lastIndexOf("_uid");
		if(indexUid < 0)
			return null;
		return "PRJNA"+this.getOrganism().substring(indexUid+4);
	}
	
	public String getOrganism() {
		int indexUnd = this.getName().lastIndexOf("_");
		return this.getName().substring(0, indexUnd);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	private String name;
	
	@Override
	public String toString() {
		/*
		 * Better that generating a random number for those leaves
		 * having no labels
		 */
		if(name == null) {
//			return null;
			Random r = new Random();
			int min = 10000000, max = 20000000;
			int rr = r.nextInt(max - min + 1) + min;
			return rr+":"+this.getBranchLength();
		}
		return this.name+":"+this.getDecimalBranchLength();
	}

}

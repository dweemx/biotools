package phy.newickparser;

import java.text.DecimalFormat;

public class TreeNode {
	
	public TreeNode(int id) {
		this.setId(id);
		this.setComplete(false);
	}
	
	public TreeNode getParent() {
		return this.parent;
	}
	
	void setParent(TreeNode parent) {
		this.parent = parent;
	}
	
	private TreeNode parent;
	
	public String getDecimalBranchLength() {
		String bL = Double.toString(branchLength);
		if(bL.indexOf('E') > -1) {
			String[] bLs = bL.split("E");
			String bLs0_bd = "", bLs0_ad = "";
			if(bLs[0].indexOf('.') > -1) {
				bLs0_bd = bLs[0].split("\\.")[0];
				bLs0_ad = bLs[0].split("\\.")[1];
			} else {
				bLs0_bd = bLs[0];
				bLs0_ad = bLs[1];
			}
			int exp = Math.abs(Integer.parseInt(bLs[1]));
			int nb0 = exp-bLs0_bd.length();
			String zeros = new String(new char[nb0]).replace("\0", "0");
			String decimalBranchLength = "0."+zeros+bLs0_bd+(bLs0_ad.equals("0") ? "": bLs0_ad);
			return decimalBranchLength;
		} else {
			return Double.toString(branchLength);
		}
	}
	
	public double getBranchLength() {
		return branchLength;
	}

	public void setBranchLength(double branchLength) {
		this.branchLength = branchLength;
	}
	
	private double branchLength;
	
	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	private boolean isComplete;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int id;
	
	public void setNuid(int i) {
		this.nuid = i;
	}
	
	public int getNuid() {
		return nuid;
	}
	
	public void addNuid(int i) {
		nuid += i;
	}
	
	private int nuid;
}

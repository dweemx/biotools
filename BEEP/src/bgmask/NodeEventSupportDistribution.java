package bgmask;

import java.util.ArrayList;
import java.util.List;

public class NodeEventSupportDistribution {

	public NodeEventSupportDistribution(int nodeId) {
		this.setNodeId(nodeId);
	}
	
	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	private int nodeId;
	
	public void addS(double e) {
		dS.add(e);
	}
	
	List<Double> dS = new ArrayList<Double>();
	
	public void addD(double e) {
		getdD().add(e);
	}
	
	private List<Double> dD = new ArrayList<Double>();	
	
	public void addT(double e) {
		getdT().add(e);
	}
	
	private List<Double> dT = new ArrayList<Double>();
	
	public void addTTD(double e) {
		getdTTD().add(e);
	}
	
	private List<Double> dTTD = new ArrayList<Double>();
	
	public void addTFD(double e) {
		getdTFD().add(e);
	}
	
	private List<Double> dTFD = new ArrayList<Double>();
	
	public void addSL(double e) {
		getdSL().add(e);
	}
	
	private List<Double> dSL = new ArrayList<Double>();
	
	public void addTL(double e) {
		getdTL().add(e);
	}
	
	private List<Double> dTL = new ArrayList<Double>();	
	
	public void addTLTD(double e) {
		getdTLTD().add(e);
	}
	
	private List<Double> dTLTD = new ArrayList<Double>();
	
	public void addTLFD(double e) {
		getdTLFD().add(e);
	}

	private List<Double> dTLFD = new ArrayList<Double>();
	
	@Override
	public String toString() {
		return getdD().size()+","+getdT().size()+","+getdTTD().size()+","+getdTFD().size()+","+
				getdSL().size()+","+getdTL().size()+","+getdTLTD().size()+","+getdTLFD().size();
	}
	
	public double[] getdDAsArray() {
		return toArray(dD);
	}

	public List<Double> getdD() {
		return dD;
	}

	public void setdD(List<Double> dD) {
		this.dD = dD;
	}
	
	// Convert List to arrray : http://stackoverflow.com/questions/9572795/convert-list-to-array-in-java
	public static double[] toArray(List<Double> list) {
		double[] array = new double[list.size()];
		for(int i = 0; i < list.size(); i++) array[i] = list.get(i);
		return array;
	}
	
	public double[] getdTAsArray() {
		return toArray(dT);
	}

	public List<Double> getdT() {
		return dT;
	}

	public void setdT(List<Double> dT) {
		this.dT = dT;
	}
	
	public double[] getdTTDAsArray() {
		return toArray(dTTD);
	}

	public List<Double> getdTTD() {
		return dTTD;
	}

	public void setdTTD(List<Double> dTTD) {
		this.dTTD = dTTD;
	}
	
	public double[] getdTFDAsArray() {
		return toArray(dTFD);
	}

	public List<Double> getdTFD() {
		return dTFD;
	}

	public void setdTFD(List<Double> dTFD) {
		this.dTFD = dTFD;
	}
	
	public double[] getdSLAsArray() {
		return toArray(dSL);
	}

	public List<Double> getdSL() {
		return dSL;
	}

	public void setdSL(List<Double> dSL) {
		this.dSL = dSL;
	}
	
	public double[] getdTLAsArray() {
		return toArray(dTL);
	}

	public List<Double> getdTL() {
		return dTL;
	}

	public void setdTL(List<Double> dTL) {
		this.dTL = dTL;
	}
	
	public double[] getdTLTDAsArray() {
		return toArray(dTLTD);
	}

	public List<Double> getdTLTD() {
		return dTLTD;
	}

	public void setdTLTD(List<Double> dTLTD) {
		this.dTLTD = dTLTD;
	}
	
	public double[] getdTLFDAsArray() {
		return toArray(dTLFD);
	}

	public List<Double> getdTLFD() {
		return dTLFD;
	}

	public void setdTLFD(List<Double> dTLFD) {
		this.dTLFD = dTLFD;
	}
}

package math;

import java.util.Comparator;
import java.util.HashMap;

public class DistanceMatrix extends HashMap<String,HashMap<String,Double>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2677608159723353448L;

	public DistanceMatrix() {
		dv = new DistanceVector();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void addDistance(String rowIndex, String columnIndex, double distance) {
		if(!this.containsKey(rowIndex)) {
			HashMap<String, Double> cols = new HashMap<String,Double>();
			cols.put(columnIndex, distance);
			this.put(rowIndex, cols);
		} else {
			this.get(rowIndex).put(columnIndex, distance);
		}
		dv.addDistance(rowIndex, columnIndex, distance);
	}
	
	public DistanceVector toVector() {
		return dv;
	}
	
	DistanceVector dv;
	
}

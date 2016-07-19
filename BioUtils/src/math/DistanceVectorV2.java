package math;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DistanceVector extends TreeMap<String,Double> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -480513045090465096L;

	public DistanceVector() {
		
	}
	
	public DistanceVector(Comparator<String> comparator) {
		super(comparator);
	}
	
	public void addDistance(String rowIndex, String columnIndex, double distance) {
		this.put(rowIndex+"_"+columnIndex, distance);
	}
	
	public double getDistance(String rowIndex, String columnIndex) {
		String index = rowIndex+"_"+columnIndex;
		return this.get(index);
	}
	
	// Sort HashMap : http://www.programcreek.com/2013/03/java-sort-map-by-value/
	public DistanceVector sortByValue(){
		Comparator<String> comparator = new ValueComparator(this);
		//TreeMap is a map sorted by its keys. 
		//The comparator is used to sort the TreeMap by keys. 
		DistanceVector result = new DistanceVector(comparator);
		result.putAll(this);
		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}

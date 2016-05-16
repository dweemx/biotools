package math;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ValueComparator implements Comparator<String>{
	 
	HashMap<String, Double> map = new HashMap<String, Double>();
 
	public ValueComparator(Map<String,Double> map){
		this.map.putAll(map);
	}
 
	@Override
	public int compare(String d1, String d2) {
		if(map.get(d1) >= map.get(d2)){
			return -1;
		}else{
			return 1;
		}	
	}
}
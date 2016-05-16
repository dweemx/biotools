package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Maths {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	// Get union : http://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    // Get intersection : http://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

}

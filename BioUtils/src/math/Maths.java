package math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Maths {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	// Rounding to X decimals : http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

}

package csv;

public class NodeMEEP3 {
	
	private double S = 0.0, D = 0.0, TD = 0.0, TR = 0.0, L = 0.0;
	private int nTD = 1, nTR = 1, nL = 1;

	public NodeMEEP3() {
	}

	public double getS() {
		return S;
	}

	public void setS(double s) {
		S = s;
	}

	public double getD() {
		return D;
	}

	public void setD(double d) {
		D = d;
	}

	public double getTD() {
		return TD/nTD;
	}

	public void addTD(double tD) {
		nTD++;
		TD += tD;
	}

	public double getTR() {
		return TR/nTR;
	}

	public void addTR(double tR) {
		nTR++;
		TR += tR;
	}

	public double getL() {
		return L/nL;
	}

	public void addL(double l) {
		nL++;
		L += l;
	}
	
}

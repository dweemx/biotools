package csv;

public class NodeEEP2 {
	
	private double S,D,T,TTD,TFD,SL,TL,TLTD,TLFD;
	private String nid;

	public NodeEEP2() {
	}
	
	public NodeEEP2(
			double S,
			double D,
			double T,
			double TTD,
			double TFD,
			double SL,
			double TL,
			double TLTD,
			double TLFD) {
		this.setS(S);
		this.setD(D);
		this.setT(T);
		this.setTTD(TTD);
		this.setTFD(TFD);
		this.setSL(SL);
		this.setTL(TL);
		this.setTLTD(TLTD);
		this.setTLFD(TLFD);
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

	public double getT() {
		return T;
	}

	public void setT(double t) {
		T = t;
	}

	public double getTTD() {
		return TTD;
	}

	public void setTTD(double tTD) {
		TTD = tTD;
	}

	public double getTFD() {
		return TFD;
	}

	public void setTFD(double tFD) {
		TFD = tFD;
	}

	public double getSL() {
		return SL;
	}

	public void setSL(double sL) {
		SL = sL;
	}

	public double getTL() {
		return TL;
	}

	public void setTL(double tL) {
		TL = tL;
	}

	public double getTLTD() {
		return TLTD;
	}

	public void setTLTD(double tLTD) {
		TLTD = tLTD;
	}

	public double getTLFD() {
		return TLFD;
	}

	public void setTLFD(double tLFD) {
		TLFD = tLFD;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}
	
}

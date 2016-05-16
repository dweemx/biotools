package csv;

public class NodeMEEP {
	
	private double S_s,S_t,D_s,D_t,T_s,T_t,TTD,TFD,SL_s,SL_l,SL_k,TL_s,TL_t,TLTD,TLFD;

	public NodeMEEP() {
	}
	
	public NodeMEEP(
			double S_s,
			double S_t,
			double D_s,
			double D_t,
			double T_s,
			double T_t,
			double TTD,
			double TFD,
			double SL_s,
			double SL_l,
			double SL_k,
			double TL_s,
			double TL_t,
			double TLTD,
			double TLFD) {
		this.setS_s(S_s);
		this.setS_t(S_t);
		this.setD_s(D_s);
		this.setD_t(D_t);
		this.setT_s(T_s);
		this.setT_t(T_t);
		this.setTTD(TTD);
		this.setTFD(TFD);
		this.setSL_s(SL_s);
		this.setSL_l(SL_l);
		this.setSL_k(SL_k);
		this.setTL_s(TL_s);
		this.setTL_t(TL_t);
		this.setTLTD(TLTD);
		this.setTLFD(TLFD);
	}

	public double getS_s() {
		return S_s;
	}

	public void setS_s(double s_s) {
		S_s = s_s;
	}

	public double getS_t() {
		return S_t;
	}

	public void setS_t(double s_t) {
		S_t = s_t;
	}

	public double getD_s() {
		return D_s;
	}

	public void setD_s(double d_s) {
		D_s = d_s;
	}

	public double getD_t() {
		return D_t;
	}

	public void setD_t(double d_t) {
		D_t = d_t;
	}
	
	public double getT_s() {
		return T_s;
	}

	public void setT_s(double t_s) {
		T_s = t_s;
	}

	public double getT_t() {
		return T_t;
	}

	public void setT_t(double t_t) {
		T_t = t_t;
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

	public double getSL_s() {
		return SL_s;
	}

	public void setSL_s(double sL_s) {
		SL_s = sL_s;
	}

	public double getSL_l() {
		return SL_l;
	}

	public void setSL_l(double sL_l) {
		SL_l = sL_l;
	}

	public double getSL_k() {
		return SL_k;
	}

	public void setSL_k(double sL_k) {
		SL_k = sL_k;
	}

	public double getTL_s() {
		return TL_s;
	}

	public void setTL_s(double tL_s) {
		TL_s = tL_s;
	}

	public double getTL_t() {
		return TL_t;
	}

	public void setTL_t(double tL_t) {
		TL_t = tL_t;
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
	
}

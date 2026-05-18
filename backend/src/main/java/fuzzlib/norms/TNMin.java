package fuzzlib.norms;

public class TNMin extends TNorm {

	public TNMin() {
		this.type = TNorm.TN_MINIMUM;
	}

	public double calc(double a, double b) {
		if (a > b)
			return b;
		else
			return a;
	}

	public double reverseCalc(double membership, double a) {
		return membership;
	}

}

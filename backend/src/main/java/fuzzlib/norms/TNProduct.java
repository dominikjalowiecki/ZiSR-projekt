package fuzzlib.norms;

public class TNProduct extends TNorm {

	public TNProduct() {
		this.type = TNorm.TN_PRODUCT;
	}

	public double calc(double a, double b) {
		return a * b;
	}

	public double reverseCalc(double membership, double a) {
		if (a == 0.0) {
			return 0.0;
		}
		return membership / a;
	}
}

package fuzzlib.norms;

//Drastic t-norm
public class TNDrastic extends TNorm {

	public TNDrastic() {
		this.type = TNorm.TN_DRASTIC;
	}

	public double calc(double a, double b) {
		if (a == 1.0 || b == 1.0)
			return 1.0;
		else
			return 0.0;
	}

	public double reverseCalc(double membership, double a) {
		return 0.0;
	}
}
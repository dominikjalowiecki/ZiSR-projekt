package fuzzlib.norms;

//Lukasiewicz t-norm
public class TNLukas extends TNorm {

	public TNLukas() {
		this.type = TNorm.TN_LUKASIEWICZ;
	}

	public double calc(double a, double b) {
		double tmp = a + b - 1.0;
		if (tmp > 0.0)
			return tmp;
		else
			return 0.0;
	}

	public double reverseCalc(double membership, double a) {
		double tmp = membership + a - 1.0;
		if (tmp > 0.0)
			return tmp;
		else
			return 0.0;
	}
}

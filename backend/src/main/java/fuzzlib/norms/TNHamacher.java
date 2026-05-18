package fuzzlib.norms;

///Hamacher product t-norm
public class TNHamacher extends TNorm {

	public TNHamacher() {
		this.type = TNorm.TN_HAMACHER;
	}

	public double calc(double a, double b) {
		double tmp = a * b;
		if (tmp == 0.0)
			return 0.0;
		else
			return tmp / (a + b - tmp);
	}

	public double reverseCalc(double membership, double a) {
		// a*b/(a+b-ab) = membership => b = m*a / (a+m*a-m)
		double tmp = membership * a;
		if (tmp == 0.0)
			return 0.0;
		else
			return tmp / (a + membership * a - membership);
	}
}

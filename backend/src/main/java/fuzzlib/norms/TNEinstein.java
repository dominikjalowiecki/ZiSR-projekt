package fuzzlib.norms;

//Einstein product t-norm
public class TNEinstein extends TNorm {

	public TNEinstein() {
		this.type = TNorm.TN_EINSTEIN;
	}

	public double calc(double a, double b) {
		double tmp = a * b;
		if (tmp == 0.0)
			return 0.0;
		else
			return tmp / (2.0 - (a + b - tmp));
	}

	/* calc odwrucony, zwraca waroďż˝ďż˝ b */
	public double reverseCalc(double membership, double a) {
		// a*b / ( 2-(a+b-a*b) ) = membership => b = m(2 - a) / (a + m - m*a)
		double tmp = membership * (2.0 - a);
		if (tmp == 0.0)
			return 0.0;
		else
			return tmp / (a + membership - membership * a);
	}
}

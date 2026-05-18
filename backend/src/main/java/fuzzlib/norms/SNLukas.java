package fuzzlib.norms;

//Lukasiewicz s-norm
public class SNLukas extends SNorm {

	public SNLukas() {
		this.type = SNorm.SN_LUKASIEWICZ;
	}

	public double calc(double a, double b) {
		double tmp = a + b;
		if (tmp > 1.0)
			return 1.0;
		else
			return tmp;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return membership - a;
	}
}

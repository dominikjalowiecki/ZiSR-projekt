package fuzzlib.norms;

//Probabilistic sum s-norm
public class SNProbSum extends SNorm {

	public SNProbSum() {
		this.type = SNorm.SN_PROBABSUM;
	}

	public double calc(double a, double b) {
		return a + b - a * b;
	}

	public double reverseCalc(double membership, double a) {
		// b = (membership - a)/(1-a)
		if (a == 0.0 || a == 1.0)
			return membership;
		else
			return (membership - a) / (1.0 - a);
	}
}

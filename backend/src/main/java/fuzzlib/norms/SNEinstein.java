package fuzzlib.norms;

//Einstein s-norm
public class SNEinstein extends SNorm {

	public SNEinstein() {
		this.type = SNorm.SN_EINSTEIN;
	}

	public double calc(double a, double b) {
		return (a + b) / (1.0 + a * b);
	}

	public double reverseCalc(double membership, double a) {
		// (a+b)/(1+a*b) = membership => b = (membership - a)/(1-membership*a)
		return (membership - a) / (1.0 - membership * a);
	}
}

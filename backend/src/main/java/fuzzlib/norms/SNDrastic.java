package fuzzlib.norms;

//Drastic s-norm
public class SNDrastic extends SNorm {

	public SNDrastic() {
		this.type = SNorm.SN_DRASTIC;
	}

	public double calc(double a, double b) {
		if (a == 0.0 || b == 0.0)
			return 0.0;
		else
			return 1.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return 1.0;
	}
}

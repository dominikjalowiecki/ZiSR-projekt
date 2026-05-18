package fuzzlib.norms;

//Nilpotent minimum t-norm
public class TNNilpot extends TNorm {

	public TNNilpot() {
		this.type = TNorm.TN_NILPOTENT;
	}

	public double calc(double a, double b) {
		if ((a + b) > 1.0) { // min(a,b)
			if (a > b)
				return b;
			else
				return a;
		} else
			return 0.0;
	}

	public double reverseCalc(double membership, double a) {
		if ((membership + a) > 1.0) {
			if (membership > a)
				return a;
			else
				return membership;
		} else
			return 0.0;
	}

}

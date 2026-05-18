package fuzzlib.negs;

public class NegZadeh extends Negation {

	public NegZadeh() {
		this.type = Negation.NEG_ZADEH;
	}

	public double calc(double a) {
		return 1.0 - a;
	}

}


package fuzzlib.impl;
//Lukasiewicz logical implication
public class LILukasiewicz extends LImp{
	public LILukasiewicz(){ 
		this.type=LImp.LI_LUKASIEWICZ; 
	}

	public double calc(double a, double b) {
		double tmp = 1.0 - a + b;
	    if (tmp < 1.0) return tmp;
	    else           return 1.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		double tmp = 1.0 - membership + a;
		if (tmp < 1.0)	return 1.0;
		else 			return tmp;
	}
}

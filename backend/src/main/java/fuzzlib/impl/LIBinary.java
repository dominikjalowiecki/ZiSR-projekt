package fuzzlib.impl;
//Binary (Kleene-Dienes) logical implication
public class LIBinary extends LImp{
	public LIBinary(){ 
		this.type=LImp.LI_BINARY; 
	}

	public double calc(double a, double b) {
		double tmp = 1.0 - a;
	    if (tmp > b) return tmp;
	    else return b;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		double tmp = 1.0 - membership;
		if (tmp > a)	return tmp;
		else return a;
	}
}

package fuzzlib.impl;
//Godel logical implication
public class LIGodel extends LImp{
	public LIGodel(){ 
		this.type=LImp.LI_GODEL; 
	}

	public double calc(double a, double b) {
		if (a>b) return b;
	    else     return 1.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		if (membership > a)	return a;
		else 				return 1.0;
	}
}

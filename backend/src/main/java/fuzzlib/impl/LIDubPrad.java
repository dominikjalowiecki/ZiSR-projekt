package fuzzlib.impl;
//Dubois-Prade logical implication
public class LIDubPrad extends LImp{
	public LIDubPrad(){ 
		this.type=LImp.LI_DUBOISPRADE; 
	}

	public double calc(double a, double b) {
		double tmp = 1.0 - a;
	    if (b==0.0)   return tmp;
	    if (a==1.0) return b;
	    return 1.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		double tmp = 1.0 - membership;
		if (a == 0.0) 			return a;
		if (membership == 1.0) 	return tmp;
		return 0.0;
	}
}

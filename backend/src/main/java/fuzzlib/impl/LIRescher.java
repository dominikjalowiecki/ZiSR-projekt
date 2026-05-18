package fuzzlib.impl;
//Resher logical implication
public class LIRescher extends LImp{
	public LIRescher(){ 
		this.type=LImp.LI_RESCHER; 
	}

	public double calc(double a, double b) {
		if (a>b) return 0.0;
	    else     return 1.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		if (membership > a) return 1.0;
		else 				return 0.0;
	}

}

package fuzzlib.impl;
//Yager logical implication
public class LIYager extends LImp{
	public LIYager(){ 
		this.type=LImp.LI_YAGER; 
	}

	public double calc(double a, double b) {
		if (a==0.0) return 1.0;
	    else return Math.pow(b,a);
	}

	@Override
	public double reverseCalc(double membership, double a) {
		if (a == 1.0) return 0.0; 
		else return Math.pow(membership,a);
	}
	
}

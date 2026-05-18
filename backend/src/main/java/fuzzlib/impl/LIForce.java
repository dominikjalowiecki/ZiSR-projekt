package fuzzlib.impl;

//Force logical implication
public class LIForce extends LImp{
	public LIForce(){ 
		this.type=LImp.LI_FORCE; 
	}

	public double calc(double a, double b) {
		double tmp = a - b;
	    if (tmp<0.0) tmp = 1 + tmp;
	    else         tmp = 1 - tmp;
	    return a*tmp;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		double tmp = membership - a;
		if (tmp < 0.0) tmp = 1- tmp;
		else 		   tmp = 1+ tmp;
		return membership*tmp;
	}

}

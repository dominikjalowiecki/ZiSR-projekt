package fuzzlib.impl;
//Reichenbach logical implication
public class LIReichenbach extends LImp{
	public LIReichenbach(){ 
		this.type=LImp.LI_REICHENBACH; 
	}

	public double calc(double a, double b) {
		return 1.0-a+a*b;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return 1.0-membership + a * membership;
	}
}

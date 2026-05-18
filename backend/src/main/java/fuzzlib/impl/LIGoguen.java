package fuzzlib.impl;
//Goguen logical implication
public class LIGoguen extends LImp{
	public LIGoguen(){ 
		this.type=LImp.LI_GOGUEN; 
	}

	public double calc(double a, double b) {
		if (a==0.0) return 1.0;
	    else {
	        double tmp = b/a;
	        if (tmp>1.0) return 1.0;
	        else         return tmp;
	    }
	}

	@Override
	public double reverseCalc(double membership, double a) {
		if (a == 0.0) return 0.0;
		else {
			double tmp = membership/a;
			if (tmp > 1.0) return 1.0;
			else 		   return tmp;
		}
	}
}

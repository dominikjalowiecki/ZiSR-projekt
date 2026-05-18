package fuzzlib.impl;
//Fodor logical implication
public class LIFodor extends LImp{
	public LIFodor(){ 
		this.type=LImp.LI_FODOR; 
	}

	public double calc(double a, double b) {
		if (a<=b) return 1.0;
	    else {
	      double tmp = 1.0 - a;
	      if (tmp > b) return tmp;
	      else         return b;
	    }
	}

	@Override
	public double reverseCalc(double membership, double a) {
		if (membership >= a) return 1.0;
		else{ 
			double tmp = 1.0 - membership;
			if (tmp > a)	return tmp;
			else 			return a;
			
		}
	}
}

package fuzzlib.norms;
//Nilpotent s-norm
public class SNNilpot extends SNorm{

	public SNNilpot(){ 
		this.type=SNorm.SN_NILPOTENT;
	}
	
	public double calc(double a, double b) {
		if ( (a+b)<1.0 ) { // max(a,b)
	        if (a > b) return a;
	        else       return b;
	    } else return 1.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return membership;
	}
}

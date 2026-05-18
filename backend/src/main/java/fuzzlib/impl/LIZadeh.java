package fuzzlib.impl;
//Zadeh logical implication
public class LIZadeh extends LImp{
	public LIZadeh(){ 
		this.type=LImp.LI_ZADEH; 
	}

	public double calc(double a, double b) {
		double tmp1 = 1.0 - a;
	    double tmp2;
	      //min(a,b)
	    if (a>b) tmp2 = b;
	    else     tmp2 = a;
	      //max( min(a,b), 1-a )
	    if (tmp1>tmp2) return tmp1;
	    else           return tmp2;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		double tmp1 = 1.0 - membership;
		double tmp2;
		if(membership>a) tmp2 = membership;
		else			 tmp2 = a;
		
		if(tmp1>tmp2) return tmp1;
		else		  return tmp2;
	}
}

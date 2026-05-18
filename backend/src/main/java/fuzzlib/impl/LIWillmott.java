package fuzzlib.impl;
//Willmott logical implication
public class LIWillmott extends LImp{
	public LIWillmott(){ 
		this.type=LImp.LI_WILLMOTT; 
	}

	public double calc(double a, double b) {
		double one_a = 1.0 - a;
	    double one_b = 1.0 - b;
	    double max1_a_b;
	    double min1_a_b;
	    double tmp;
	      //max(1-a,b) and min(1-a,b)
	    if (one_a > b) { max1_a_b = one_a; min1_a_b = b; }
	    else           { max1_a_b = b; min1_a_b = one_a; }
	      //max( a, 1-b, min(1-a,b) )
	    if (a>one_b) tmp = a;
	    else         tmp = one_b;
	    if (tmp<min1_a_b) tmp = min1_a_b;
	      //final result = min(tmp,max1_a_b)
	    if (tmp > max1_a_b) return max1_a_b;
	    else                return tmp;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		//uzupełnić w przyszłości 
		return 0;
	}
}

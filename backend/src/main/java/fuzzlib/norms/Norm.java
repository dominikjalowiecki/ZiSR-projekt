package fuzzlib.norms;

import fuzzlib.SPoint;

///Abstract class of T-Norm and T-Conorm
public abstract class Norm {
	protected short type;

	public abstract double calc(double a, double b);

	/* returns b, result represents the result of calc() */
	public abstract double reverseCalc(double result, double a);
	
	public short getType() {
		return type;
	};
		
	public boolean isTnorm(short type){
		if(type >= 0 && type < 100) return true;
		else return false;
	}
	
	public double findMaxIntersection(SPoint p1, SPoint p2, double constant,
			TNorm intersection, double max, double mindy, double mindx) {

		return max;
	}

	@Override
	public String toString() {
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
	}
}

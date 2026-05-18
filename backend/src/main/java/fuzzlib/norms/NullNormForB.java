package fuzzlib.norms;

public class NullNormForB extends Norm{

	public NullNormForB(){ 
		this.type= -2;
	}
	public double calc(double a, double b) {
		return b;
	}
	@Override
	public double reverseCalc(double membership, double a) {
		return membership;
	}
}

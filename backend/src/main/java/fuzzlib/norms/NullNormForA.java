package fuzzlib.norms;

public class NullNormForA extends Norm{

	public NullNormForA(){ 
		this.type= -1;
	}
	public double calc(double a, double b) {
		return a;
	}
	@Override
	public double reverseCalc(double membership, double a) {
		return a;
	}
}

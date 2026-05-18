package fuzzlib.avers;
//Sum
public class Sum extends Average{
	
	private double value;
	public Sum(){ 
		Reset();
		this.type=Average.SUM; 
	}
	
	void Reset() {
		value = 0.0; 
		val_num = 0;
	}

	void addValue(double value) {
		this.value += value;
	}

	double getResult(){
		return value;
	}

	public double calc(double a, double b) {
		return a+b;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return membership + a;
	}
}


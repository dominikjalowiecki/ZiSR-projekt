package fuzzlib.avers;
//Geometric average (Geometric mean)
public class AVGeometric extends Average{
	
	private double value;
	public AVGeometric(){ 
		Reset();
		this.type=Average.AV_GEOMETRIC; 
	}
	
	void Reset() {
		value = 0.0; 
		val_num = 0;
	}

	void addValue(double value) {
		this.value *= value;
		val_num++;
	}

	double getResult(){
		return Math.pow(value,1.0/val_num);
	}

	public double calc(double a, double b) {
		return Math.pow(a*b,0.5);
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return Math.pow(membership*a,0.5);
	}
}


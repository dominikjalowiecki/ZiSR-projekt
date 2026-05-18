package fuzzlib.avers;
//Arithmetic average (Arithmetic mean)
public class AVArithmetic extends Average{
	
	private double value;
	public AVArithmetic(){ 
		Reset();
		this.type=Average.AV_ARITHMETIC; 
	}
	
	void Reset() {
		value = 0.0; 
		val_num = 0;
	}

	void addValue(double value) {
		this.value += value;
		val_num++;
	}

	double getResult(){
		return value/val_num;
	}

	public double calc(double a, double b) {
		return (a+b)/2.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return (membership +a)/2.0;
	}
}

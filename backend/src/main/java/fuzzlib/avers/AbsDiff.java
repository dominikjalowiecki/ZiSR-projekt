package fuzzlib.avers;
//Absolute difference
public class AbsDiff extends Average{
	
	private double value;
	public AbsDiff(){ 
		Reset();
		this.type=Average.ABS_DIFF; 
	}
	
	void Reset() {
		value = 0.0; 
		val_num = 0;
	}

	void addValue(double value) {
		this.value -= value;
	}

	double getResult(){
		if (value<0.0) return -value;
	    else return value;
	}

	public double calc(double a, double b) {
		double tmp = a-b;
	    if (tmp<0.0) tmp = -tmp;
	    return tmp;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		double tmp = membership-a;
		if (tmp > 0.0) tmp = -tmp;
		return tmp;
	}
}


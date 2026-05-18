package fuzzlib.avers;
//Harmonic average (Harmonic mean)
public class AVHarmonic extends Average{
	
	private double value1;
	private double value2;
	public AVHarmonic(){ 
		Reset();
		this.type=Average.AV_HARMONIC; 
	}
	
	void Reset() {
		value1 = value2 = 0.0; 
		val_num = 0;
	}

	void addValue(double value) {
		this.value1 *= value;
	    this.value2 += value;
	    val_num++;
	}

	double getResult(){
		if (value2!=0.0) return (val_num*value1)/(value2);
	    else return 0.0;
	}

	public double calc(double a, double b) {
		double tmp = a+b;
	    if (tmp != 0.0) return (2*a*b)/(a+b);
	    else return 0.0;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		double tmp = membership + a;
		if (tmp != 1.0) return (2*membership*a)/(tmp);
		else return 0.0;
	}
}


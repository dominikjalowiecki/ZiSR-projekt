package fuzzlib.norms;
//maximum s-norm (Zadeh)
public class SNMax extends SNorm{

	public SNMax(){ 
		this.type=SNorm.SN_MAXIMUM;
	}
	
	public double calc(double a, double b) {
		if (a > b) return a;
	    else return b;
	}

	@Override
	public double reverseCalc(double membership, double a) {
		return membership;
	}

}

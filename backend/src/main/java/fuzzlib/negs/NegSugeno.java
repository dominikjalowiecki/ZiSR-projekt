package fuzzlib.negs;

public class NegSugeno extends Negation{
	private double p;
	public NegSugeno(){ 
		p=0.0;
		this.type=Negation.NEG_SUGENO; 
	}
	public NegSugeno(double ip){ 
		this.type=Negation.NEG_SUGENO; 
		setP(ip); 
	};
	void setP(double ip){ if (ip > -1.0) p=ip; };
	public double getP(){ 
		return p; 
	};
		  		   
	@Override
	public double calc(double a) {
		return (1.0 - a)/(1.0 + p*a);
	}
	
}

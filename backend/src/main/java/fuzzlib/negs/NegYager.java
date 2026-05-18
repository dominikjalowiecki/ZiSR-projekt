package fuzzlib.negs;

public class NegYager extends Negation{
	private double p;
	public NegYager(){
		p=2.0;
		this.type = Negation.NEG_YAGER;
	}
	public NegYager(double ip){ 
		this.type = Negation.NEG_YAGER; 
		setP(ip); 
	}
    public void setP(double ip){ 
    	if (ip>0.0) p=ip; 
    };
    public double getP(){ 
    	return p; 
    };
	
		public double calc(double a) {
		return Math.pow(1.0-Math.pow(a,p),1.0/a);
	}

	

	    
	 
	
	
}

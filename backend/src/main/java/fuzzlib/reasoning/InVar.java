package fuzzlib.reasoning;
import fuzzlib.*;

public class InVar {
	public double inval;
//ZMIANA
	public FuzzySet invalF;
	public FuzzySet fuzz;
//KONIEC
	public String id;
	public String des;
	
	public InVar() {
		id="";
		des="";
		fuzz = null;
		invalF = null;
		inval=0.0;
	}
	public InVar(InVar in){
		id=in.id;
		des=in.des;
//ZMIANA
		if (in.fuzz != null) fuzz = new FuzzySet(in.fuzz);
//ZMIANA
		inval=in.inval;
		if (in.invalF!=null) invalF = new FuzzySet(); 
	}

	
//ZMIANA 
	public InVar assign(InVar in){
	        id = in.id; 
	        des = in.des; 
			if (in.fuzz != null) fuzz = new FuzzySet(in.fuzz);
	        inval = in.inval;
	        if (in.invalF!=null) invalF = new FuzzySet(); 
//KONIEC
	        return this; 
	} 
}

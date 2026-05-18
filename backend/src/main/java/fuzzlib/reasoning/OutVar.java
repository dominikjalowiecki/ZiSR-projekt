package fuzzlib.reasoning;
import fuzzlib.FuzzySet;

public class OutVar {
	
	public FuzzySet outset = new FuzzySet();   //output variable's fuzzy set
	public double outval;     //automatically defuzzyfied value
	public int cnum;          //number of rules containing this variable in conclusion - needed for average agregation
	                       // - (i.e. for arithmetic average 'cnum' will be a divider of all conclusions' sum)
	public String id;    //varaible's identifier
	public String des;   //varaible's description

	public OutVar(){
		id="";
		des="";
		outval=0.0;
		cnum=0;
	}
	
	public OutVar(OutVar in){
		id=in.id;
		des=in.des;
		outval=in.outval;
		outset=in.outset;
		cnum=in.cnum;
	}

//ZMIANA	
	public OutVar assign(OutVar in) {
	        id = in.id; 
	        des = in.des; 
	        if (in.outset != null) outset = new FuzzySet(in.outset); 
//KONIEC
	        outval = in.outval; 
	        cnum = in.cnum; 
	        return this; 
	}
}

package fuzzlib.reasoning;

import fuzzlib.FuzzySet;

//structure describing consequent element
public class ConsItem {
	
	public int iVar; // index of an output variable
	public int iSet; // index of a consequent
	public FuzzySet con = new FuzzySet(); // rule conclusion
	
	public ConsItem(){
		this(false);
	}
	
	public ConsItem(boolean storeConclusion){
		iVar=-1;
		iSet=-1;
		con=null;
		if (storeConclusion) con = new FuzzySet();
	}
	
	public ConsItem(ConsItem in){
		iVar=in.iVar;
		iSet=in.iSet;
		con=null;
		if (in.con!=null) con = new FuzzySet(); 
	}
//ZMIANA
	public ConsItem assign(ConsItem in){
		iVar=in.iVar; 
		iSet=in.iSet;
		if (in.con!=null) con = new FuzzySet(in.con);
//KONIEC
		return this; 
	}
	public void CreateConclusionSet(){
		if (con==null) con = new FuzzySet(); };
	public void DeleteConclusionSet(){
		if (con!=null) con=null; 
	}
}

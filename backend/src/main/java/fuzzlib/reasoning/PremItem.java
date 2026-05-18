package fuzzlib.reasoning;

import fuzzlib.norms.Norm;

//structure describing one logical operation between two antecedents in fuzzy rule
public class PremItem {
	
	public int iLVar,iRVar; // indexes of two input variables (-2 means no variable - in case of only one premise)
	                     //                                (-1 means to take result from a stack)
	public int iLSet,iRSet; // indexes of two antecedents (-2,-1 - look above)
	public short op_type;
	public Norm op;       // norm as a logical operation (or,and) between membership levels of two antecedents
	                     // NULL means no operation (in case of only one premise)

	public PremItem(){
	    	iLVar=-2;
	    	iRVar=-2;
	    	iLSet=-2;
	    	iRSet=-2;
	    	op_type=OpType.NO_OPERATION;
	    	op=null;
	}
	public PremItem(PremItem in){
	        iLVar = in.iLVar; 
	        iRVar = in.iRVar; 
	        iLSet = in.iLSet; 
	        iRSet = in.iRSet;
	        op_type = in.op_type; 
	        op = in.op;
	}
//ZMIANA	
	public PremItem assign(PremItem in){
	        iLVar = in.iLVar; 
	        iRVar = in.iRVar; 
	        iLSet = in.iLSet; 
	        iRSet = in.iRSet;
	        op_type = in.op_type; 
	        op = in.op; 
	        return this; 
	}
}

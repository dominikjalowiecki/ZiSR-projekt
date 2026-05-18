package fuzzlib.reasoning;

import fuzzlib.norms.Norm;

//structure storing one rule
public class Rule {
	
	public static final int DEFAULT_RULE_SIZE  = 8;
	public static final int DEFAULT_RULE_EXTENSION  = 4;
	public PremItem [] pits; //array of premise items - premise
	public int psize;
	public int maxpsize;
	public ConsItem [] cits; //array of consequent items - consequent
	public int csize;
	public int maxcsize;

	public Rule(int premiseSize, int conclusionSize){
		psize = 0;
	    csize = 0;
	    maxpsize = premiseSize;
	    maxcsize = conclusionSize;
	    if (maxpsize < 1) maxpsize = DEFAULT_RULE_SIZE;
	    if (maxcsize < 1) maxcsize = DEFAULT_RULE_SIZE;
//ZMIANA
	    pits = new PremItem[maxpsize];
	    for (int i=0; i<maxpsize; i++) pits[i] = new PremItem();
	    cits = new ConsItem[maxcsize];
	    for (int i=0; i<maxcsize; i++) cits[i] = new ConsItem();
//KONIEC
	    }
	public Rule(){
		this(DEFAULT_RULE_SIZE, DEFAULT_RULE_SIZE);
	}
	public Rule(Rule irule){
		psize = irule.psize;
	    maxpsize = irule.maxpsize;
	    csize = irule.csize;
	    maxcsize = irule.maxcsize;

//ZMIANA
	    pits = new PremItem[maxpsize];
	    for (int i=0; i<maxpsize; i++) pits[i] = new PremItem();
	    cits = new ConsItem[maxcsize];
	    for (int i=0; i<maxcsize; i++) cits[i] = new ConsItem();

	    for (int i=0; i<psize; i++) pits[i].assign(irule.pits[i]);
	    for (int i=0; i<csize; i++) cits[i].assign(irule.cits[i]);
//KONIEC
	}

//ZMIANA	
	public Rule assign(Rule irule){
		if (irule == this) return this;

	    if (pits != null) pits =null;
	    if (cits != null) cits=null;

	    psize = irule.psize;
	    maxpsize = irule.maxpsize;
	    csize = irule.csize;
	    maxcsize = irule.maxcsize;

	    pits = new PremItem[maxpsize];
	    for (int i=0; i<maxpsize; i++) pits[i] = new PremItem();
	    cits = new ConsItem[maxcsize];
	    for (int i=0; i<maxcsize; i++) cits[i] = new ConsItem();

	    for (int i=0; i<psize; i++) pits[i].assign(irule.pits[i]);
	    for (int i=0; i<csize; i++) cits[i].assign(irule.cits[i]);
//KONIEC

	    return this;
	}

	public void AddPreItem(int varA, int setA, int varB, int setB, short op_type, Norm op){
		//if array size is not enough
	    if (psize+1 > maxpsize) {
	        maxpsize += DEFAULT_RULE_EXTENSION;
	        PremItem [] tmp = new PremItem[maxpsize]; //new memory
//ZMIANA
		    for (int i=0; i<maxpsize; i++) tmp[i] = new PremItem();
	        for( int i=0; i < psize; i++ ) {
	            tmp[i].assign(pits[i]); 
	        }          //copy old content
//KONIEC
	        pits = tmp; 					 //set new content
	    }

	    pits[psize].iLVar = varA;
	    pits[psize].iLSet = setA;
	    pits[psize].iRVar = varB;
	    pits[psize].iRSet = setB;
	    pits[psize].op_type = op_type;
	    pits[psize].op = op;
	    psize++;
	
	}
	public void AddConItem(int varC, int setC, boolean extended){
		  //if array size is too small
	    if (csize+1 > maxcsize) {
	        maxcsize += DEFAULT_RULE_EXTENSION;
	        ConsItem [] tmp = new ConsItem[maxcsize]; //new memory
//ZMIANA
		    for (int i=0; i<maxpsize; i++) tmp[i] = new ConsItem();
	        for( int i=0; i < csize; i++ ) {
	            tmp[i].assign(cits[i]);		 //copy old content
	        }        
//KONIEC
	        cits = tmp; 					 //set new content
	    }

	    cits[csize].iVar = varC;
	    cits[csize].iSet = setC;
	    if (extended==true)               //if extended system than create additional local set
	        cits[csize].CreateConclusionSet();
	    csize++;
	}
}

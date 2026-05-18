package fuzzlib.negs;

///Abstract class of negation (complement)
public abstract class Negation {
	protected short type;
	private static final short MIN = 200;
	private static final short MAX = 299;
	public static final short NEG_ZADEH = 200;
	public static final short NEG_YAGER = 201;
	public static final short NEG_SUGENO = 202;
	public abstract double calc(double a);
	public short getType(){ return type; }
	public static int isNegation(short negation_type){
        if ((negation_type>=MIN)&&(negation_type<=MAX)) return 1;
        else return 0;
    };
    
}


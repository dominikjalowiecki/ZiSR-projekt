package fuzzlib.impl;
import fuzzlib.norms.Norm;
//Abstract class for logical implication
public abstract class LImp extends Norm{
	private static final short MIN = 300;
	private static final short MAX = 399;
	public static final short LI_BINARY      = 300;
	public static final short LI_LUKASIEWICZ = 301;
	public static final short LI_REICHENBACH = 302;
	public static final short LI_FODOR       = 303;
	public static final short LI_RESCHER     = 304;
	public static final short LI_GOGUEN      = 305;
	public static final short LI_GODEL       = 306;
	public static final short LI_YAGER       = 307;
	public static final short LI_ZADEH       = 308;
	public static final short LI_WILLMOTT    = 309;
	public static final short LI_DUBOISPRADE = 310;
	public static final short LI_FORCE       = 311;
	public static int isLogicalImplication(short implication_type){
	        if ((implication_type>=MIN)&&(implication_type<=MAX)) return 1;
	        else return 0;
	    }
}

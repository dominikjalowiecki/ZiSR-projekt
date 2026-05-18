package fuzzlib.avers;
import fuzzlib.norms.Norm;
//Abstract class for logical implication
public abstract class Average extends Norm{
	
	protected int val_num; //number of added values for average calculation
	private static final short MIN = 400;
	private static final short MAX = 499;
	public static final short AV_ARITHMETIC = 400;
	public static final short AV_GEOMETRIC  = 401;
	public static final short AV_HARMONIC   = 402;
	public static final short ABS_DIFF      = 498;
	public static final short SUM           = 499;
	public static int isAverage(short average_type){
	        if ((average_type>=MIN)&&(average_type<=MAX)) return 1;
	        else return 0;
	    }
	    abstract void Reset();
	    abstract void addValue(double value);
	    abstract double getResult();

}

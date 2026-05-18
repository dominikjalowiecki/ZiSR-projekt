package fuzzlib.reasoning;

import fuzzlib.DefuzMethod;
import fuzzlib.impl.LImp;
import fuzzlib.norms.SNorm;
import fuzzlib.norms.TNorm;

public class SystemConfig {
	public static final int DEFAULT_RULE_SIZE  = 8;
	public static final int DEFAULT_RULE_EXTENSION  = 4;
	public static final short DEFAULT_SETARRAY_SIZE = 10;
	public static final short DEF_MAMDANI = 0;
	public static final short DEF_LOGICAL = 1;
	boolean Extended;

    int inputWidth;
    int outputWidth;
    int preSetsNr;
    int conSetsNr;
    short IsType;
    short OrType;
    short AndType;
    short ImplType;
    short AgrType;
    short truthCompType;
    short DefuzzType;
    boolean AutoDefuzz;
    double Alpha;
    boolean AutoAlpha;

    double truthYPrecision;
    double truthXPrecision;
    
    public SystemConfig(){
    	inputWidth=0;
    	outputWidth=0;
    	preSetsNr=DEFAULT_SETARRAY_SIZE;
        conSetsNr=DEFAULT_SETARRAY_SIZE;
        OrType=SNorm.SN_MAXIMUM;
        IsType=TNorm.TN_MINIMUM;
        truthCompType = IsType;
        AndType=TNorm.TN_MINIMUM;
        ImplType=TNorm.TN_MINIMUM;
        AgrType=SNorm.SN_MAXIMUM;
        DefuzzType=DefuzMethod.DF_COG;
        AutoDefuzz=true;
        Alpha=0.0;
        AutoAlpha=false;
        Extended=false;
        truthYPrecision=0.01;
        truthXPrecision=0.001;
    }
	public SystemConfig(short system_type){
		Extended = false;
		inputWidth = 0; outputWidth = 0;
		preSetsNr = DEFAULT_SETARRAY_SIZE;
		conSetsNr = DEFAULT_SETARRAY_SIZE;
		DefuzzType = DefuzMethod.DF_COG;
		Alpha = 0.0;
		AutoDefuzz = true;
		AutoAlpha = false;
		truthYPrecision = 0.01;
		truthXPrecision = 0.001;
        truthCompType = TNorm.TN_MINIMUM;

		switch(system_type){
		        case SystemConfig.DEF_MAMDANI :      IsType  = TNorm.TN_MINIMUM;
		                OrType   = SNorm.SN_MAXIMUM; AndType = TNorm.TN_MINIMUM;
		                ImplType = TNorm.TN_MINIMUM; AgrType = SNorm.SN_MAXIMUM;
		            break;
		        case SystemConfig.DEF_LOGICAL :      IsType  = TNorm.TN_MINIMUM;
		                OrType   = SNorm.SN_MAXIMUM; AndType = TNorm.TN_MINIMUM;
		                ImplType = LImp.LI_BINARY ;  AgrType = TNorm.TN_MINIMUM;
		            break;
		        default :                             IsType  = TNorm.TN_MINIMUM;
		                OrType   = SNorm.SN_MAXIMUM; AndType = TNorm.TN_MINIMUM;
		                ImplType = TNorm.TN_MINIMUM; AgrType = SNorm.SN_MAXIMUM;
		    }
	}
	public void setExtended(boolean state){ Extended = state; };
	public void setInputWidth(int width) { inputWidth = width; };
	public void setOutputWidth(int width){ outputWidth = width; };
	public void setNumberOfPremiseSets(int nr){ preSetsNr = nr; };
	public void setNumberOfConclusionSets(int nr){ conSetsNr = nr; };
	public void setIsOperationType(short type){ IsType = type; };
	public void setOrOperationType(short type){ OrType = type; };
	public void setAndOperationType(short type){ AndType = type; };
	public void setImplicationType(short type){ ImplType = type; };
	public void setConclusionAgregationType(short type){ AgrType = type; };
	public void setTruthCompositionType(short type){ truthCompType = type; };
	public void setDefuzzyfication(short type){ setDefuzzyfication(type, 0.0);}
	public void setDefuzzyfication(short type, double alpha){
            DefuzzType = type;
            if (alpha>=0.0) Alpha = alpha;
            else Alpha = 0.0; }
	public void setAutoDefuzzyfication(boolean state){ AutoDefuzz = state; };
	public void setAutoAlpha(boolean state){ AutoAlpha = state; };
	public void setTruthPrecision(double mindy){setTruthPrecision(mindy,0.001); }
	public void setTruthPrecision(double mindy, double mindx){
             if (mindy<=0.0 || mindx <=0.0) return;
             truthYPrecision = mindy; truthXPrecision = mindx; }

	public boolean getExtended(){ return Extended; };
	public int getInputWidth(){ return inputWidth; };
	public int getOutputWidth(){ return outputWidth; };
	public int getNumberOfPremiseSets(){ return preSetsNr; };
	public int getNumberOfConclusionSets(){ return conSetsNr; };
	public short getIsOperationType(){ return IsType; };
	public short getOrOperationType(){ return OrType; };
	public short getAndOperationType(){ return AndType; };
	public short getImplicationType(){ return ImplType; };
	public short getConclusionAgregationType(){ return AgrType; };
	public short getTruthCompositionType(){ return truthCompType; };
	public short getDefuzzyficationType(){ return DefuzzType; };
	public double getDefuzzyficationAlpha(){ return Alpha; };
	public boolean getAutoDefuzzyfication(){ return AutoDefuzz; };
	public boolean getAutoAlpha(){ return AutoAlpha; };
	public double getTruthYPrecision(){return truthYPrecision;}
	public double getTruthXPrecision(){return truthXPrecision;}
}

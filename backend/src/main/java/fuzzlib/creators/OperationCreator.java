package fuzzlib.creators;
import fuzzlib.avers.*;
import fuzzlib.impl.*;
import fuzzlib.negs.*;
import fuzzlib.norms.*;
public class OperationCreator {
	
		public static TNorm newTNorm(short type){
			switch(type){
	        case TNorm.TN_MINIMUM:     return new TNMin();
	        case TNorm.TN_PRODUCT:     return new TNProduct();
	        case TNorm.TN_LUKASIEWICZ: return new TNLukas();
	        case TNorm.TN_DRASTIC:     return new TNDrastic();
	        case TNorm.TN_NILPOTENT:   return new TNNilpot();
	        case TNorm.TN_HAMACHER:    return new TNHamacher();
	        case TNorm.TN_EINSTEIN:    return new TNEinstein();
	        default :                   return new TNMin();
	    }
		}
		public static SNorm newSNorm(short type){
			switch(type){
	        case SNorm.SN_MAXIMUM:     return new SNMax();
	        case SNorm.SN_PROBABSUM:   return new SNProbSum();
	        case SNorm.SN_LUKASIEWICZ: return new SNLukas();
	        case SNorm.SN_DRASTIC:     return new SNDrastic();
	        case SNorm.SN_NILPOTENT:   return new SNNilpot();
	        case SNorm.SN_EINSTEIN:    return new SNEinstein();
	        default :                   return new SNMax();
	    }
		}
		public static Negation newNegation(short type){
			switch(type){
	        case Negation.NEG_ZADEH:  return new NegZadeh();
	        case Negation.NEG_YAGER:  return new NegYager();
	        case Negation.NEG_SUGENO: return new NegSugeno();
	        default :                  return new NegZadeh();
	    }
		}
		public static Norm newImplication(short type){
			switch(type){
	        case LImp.LI_BINARY:       return new LIBinary();
	        case LImp.LI_LUKASIEWICZ:  return new LILukasiewicz();
	        case LImp.LI_REICHENBACH:  return new LIReichenbach();
	        case LImp.LI_FODOR:        return new LIFodor();
	        case LImp.LI_RESCHER:      return new LIRescher();
	        case LImp.LI_GOGUEN:       return new LIGoguen();
	        case LImp.LI_GODEL:        return new LIGodel();
	        case LImp.LI_YAGER:        return new LIYager();
	        case LImp.LI_ZADEH:        return new LIZadeh();
	        case LImp.LI_WILLMOTT:     return new LIWillmott();
	        case LImp.LI_DUBOISPRADE:  return new LIDubPrad();
	        case LImp.LI_FORCE:        return new LIForce();
	        case TNorm.TN_MINIMUM:     return new TNMin();
	        case TNorm.TN_PRODUCT:     return new TNProduct();
	        case TNorm.TN_LUKASIEWICZ: return new TNLukas();
	        case TNorm.TN_DRASTIC:     return new TNDrastic();
	        case TNorm.TN_NILPOTENT:   return new TNNilpot();
	        case TNorm.TN_HAMACHER:    return new TNHamacher();
	        case TNorm.TN_EINSTEIN:    return new TNEinstein();
	        default :                   return new TNMin();
	    }
		}
		public static Norm newAverage(short type){
			switch(type){
	        case Average.AV_ARITHMETIC : return new AVArithmetic();
	        case Average.AV_GEOMETRIC  : return new AVGeometric();
	        case Average.AV_HARMONIC   : return new AVHarmonic();
	        case Average.SUM           : return new Sum();
	        case Average.ABS_DIFF      : return new AbsDiff();
	        default :                     return new AVArithmetic();
	    }
		}

		public static Norm newNorm(short type){
			switch(type){
	        case TNorm.TN_MINIMUM:     return new TNMin();
	        case TNorm.TN_PRODUCT:     return new TNProduct();
	        case TNorm.TN_LUKASIEWICZ: return new TNLukas();
	        case TNorm.TN_DRASTIC:     return new TNDrastic();
	        case TNorm.TN_NILPOTENT:   return new TNNilpot();
	        case TNorm.TN_HAMACHER:    return new TNHamacher();
	        case TNorm.TN_EINSTEIN:    return new TNEinstein();
	        case SNorm.SN_MAXIMUM:     return new SNMax();
	        case SNorm.SN_PROBABSUM:   return new SNProbSum();
	        case SNorm.SN_LUKASIEWICZ: return new SNLukas();
	        case SNorm.SN_DRASTIC:     return new SNDrastic();
	        case SNorm.SN_NILPOTENT:   return new SNNilpot();
	        case SNorm.SN_EINSTEIN:    return new SNEinstein();
	        case LImp.LI_BINARY:       return new LIBinary();
	        case LImp.LI_LUKASIEWICZ:  return new LILukasiewicz();
	        case LImp.LI_REICHENBACH:  return new LIReichenbach();
	        case LImp.LI_FODOR:        return new LIFodor();
	        case LImp.LI_RESCHER:      return new LIRescher();
	        case LImp.LI_GOGUEN:       return new LIGoguen();
	        case LImp.LI_GODEL:        return new LIGodel();
	        case LImp.LI_YAGER:        return new LIYager();
	        case LImp.LI_ZADEH:        return new LIZadeh();
	        case LImp.LI_WILLMOTT:     return new LIWillmott();
	        case LImp.LI_DUBOISPRADE:  return new LIDubPrad();
	        case LImp.LI_FORCE:        return new LIForce();
	        case Average.AV_ARITHMETIC:return new AVArithmetic();
	        case Average.AV_GEOMETRIC: return new AVGeometric();
	        case Average.AV_HARMONIC:  return new AVHarmonic();
	        case Average.SUM:          return new Sum();
	        default :                   return new TNMin();
	    }
		}
		
}

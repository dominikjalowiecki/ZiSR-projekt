package fuzzlib.reasoning;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import fuzzlib.DefuzMethod;
import fuzzlib.FuzzySet;
import fuzzlib.creators.OperationCreator;
import fuzzlib.norms.Norm;
import fuzzlib.norms.SNorm;
import fuzzlib.norms.TNorm;

import java.util.Stack;

///Class representing a fuzzy reasoning system
///Defines input,output variables and antecedents,consequents of fuzzy rules
public class ReasoningSystem {
	public static final int DEFAULT_RULE_SIZE = 8;
	public static final short DEFAULT_SETARRAY_SIZE = 10;
	public static final short DEFAULT_SETARRAY_EXTENSION = 5;

	boolean extended;

	InVar[] ins; // input variables - values, description and fuzzyfiers
	HashMap<String, Integer> InIndex = null;
	int isize;
	OutVar[] outs; // output variables - output sets, defuzzyfied values and description
	HashMap<String, Integer> OutIndex = null;
	int osize;
	FuzzySet[] PreS; // Fuzzy sets describing premises
	HashMap<String, Integer> PreIndex = null;
	int psize;
	int maxpsize;
	FuzzySet[] ConS; // Fuzzy sets describing conclusions
	HashMap<String, Integer> ConIndex = null;
	int csize;
	int maxcsize;

	List<Rule> Rules = new LinkedList<Rule>(); // Fuzzy system's rules

	TNorm opIS; // IS operation - TNorm joining input sets with premise set (Variable is Set)
	SNorm opOR; // OR operation (OR agregation of premises)
	TNorm opAND; // AND operation (AND agregation of premises)
	Norm impl; // implication for calculating conclusion
	Norm ruleAgregator; // rule agregation method

	TNorm truthComp; // TNorm operation needed for truth function composition

	boolean autoDefuzz;
	short defuzz;
	boolean autoAlpha;
	double alpha;

	double truthDy, truthDx;

	Rule lastRule;

	FuzzySet _tmpOne; // temporary sets needed for calculations
	FuzzySet _tmpTwo;

//ZMIANA    
	void _delete_properties() {
		ins = null;
		outs = null;
		PreS = null;
		ConS = null;
		opIS = null;
		opOR = null;
		opAND = null;
		impl = null;
		InIndex = null;
		OutIndex = null;
		PreIndex = null;
		ConIndex = null;
		ruleAgregator = null;
		truthComp = null;

		_tmpOne = null;
		_tmpTwo = null;
	}

	public ReasoningSystem() {
		extended = false;
		ins = null;
		outs = null;
		isize = osize = 0;
		psize = 0;
		maxpsize = DEFAULT_SETARRAY_SIZE;
		PreS = new FuzzySet[maxpsize];
		csize = 0;
		maxcsize = DEFAULT_SETARRAY_SIZE;
		ConS = new FuzzySet[maxcsize];
		InIndex = new HashMap<String, Integer>();
		OutIndex = new HashMap<String, Integer>();
		PreIndex = new HashMap<String, Integer>();
		ConIndex = new HashMap<String, Integer>();

		opIS = OperationCreator.newTNorm(TNorm.TN_MINIMUM);
		opOR = OperationCreator.newSNorm(SNorm.SN_MAXIMUM);
		opAND = OperationCreator.newTNorm(TNorm.TN_MINIMUM);
		impl = OperationCreator.newImplication(TNorm.TN_MINIMUM);
		ruleAgregator = OperationCreator.newSNorm(SNorm.SN_MAXIMUM);
		truthComp = OperationCreator.newTNorm(TNorm.TN_MINIMUM);

		defuzz = DefuzMethod.DF_COG;
		alpha = 0.0;
		autoDefuzz = true;
		autoAlpha = false;

		truthDy = 0.01;
		truthDx = 0.001;

		_tmpOne = new FuzzySet(100);
		_tmpTwo = new FuzzySet(100);

		lastRule = null;
	}

	public ReasoningSystem(SystemConfig config) {
		extended = config.getExtended();
		ins = null;
		outs = null;

		isize = osize = 0;
		setInputWidth(config.getInputWidth());
		setOutputWidth(config.getOutputWidth());

		psize = 0;
		maxpsize = config.getNumberOfPremiseSets();
		PreS = new FuzzySet[maxpsize];
		csize = 0;
		maxcsize = config.getNumberOfConclusionSets();
		ConS = new FuzzySet[maxcsize];
		InIndex = new HashMap<String, Integer>();
		OutIndex = new HashMap<String, Integer>();
		PreIndex = new HashMap<String, Integer>();
		ConIndex = new HashMap<String, Integer>();

		opIS = OperationCreator.newTNorm(config.getIsOperationType());
		opOR = OperationCreator.newSNorm(config.getOrOperationType());
		opAND = OperationCreator.newTNorm(config.getAndOperationType());
		impl = OperationCreator.newImplication(config.getImplicationType());
		ruleAgregator = OperationCreator.newNorm(config.getConclusionAgregationType());
		truthComp = OperationCreator.newTNorm(config.getTruthCompositionType());
		defuzz = config.getDefuzzyficationType();
		alpha = config.getDefuzzyficationAlpha();
		autoDefuzz = config.getAutoDefuzzyfication();
		autoAlpha = config.getAutoAlpha();

		truthDy = config.getTruthYPrecision();
		truthDx = config.getTruthXPrecision();

		_tmpOne = new FuzzySet(100);
		_tmpTwo = new FuzzySet(100);

		lastRule = null;
	}

	/// Reasoning system reset
	/// Old configuration and rules are deleted and completely new system is created
	/// with default configuration
	public void Reset() {
		// delete old system data
		_delete_properties();
		DeleteRules();
		// configure system as default constructor
		extended = false;
		ins = null;
		outs = null;
		isize = osize = 0;
		psize = 0;
		maxpsize = DEFAULT_SETARRAY_SIZE;
		PreS = new FuzzySet[maxpsize];
		csize = 0;
		maxcsize = DEFAULT_SETARRAY_SIZE;
		ConS = new FuzzySet[maxcsize];
		InIndex = new HashMap<String, Integer>();
		OutIndex = new HashMap<String, Integer>();
		PreIndex = new HashMap<String, Integer>();
		ConIndex = new HashMap<String, Integer>();

		opIS = OperationCreator.newTNorm(TNorm.TN_MINIMUM);
		opOR = OperationCreator.newSNorm(SNorm.SN_MAXIMUM);
		opAND = OperationCreator.newTNorm(TNorm.TN_MINIMUM);
		impl = OperationCreator.newImplication(TNorm.TN_MINIMUM);
		ruleAgregator = OperationCreator.newSNorm(SNorm.SN_MAXIMUM);
		truthComp = OperationCreator.newTNorm(TNorm.TN_MINIMUM);
		defuzz = DefuzMethod.DF_COG;
		alpha = 0.0;
		autoDefuzz = true;
		autoAlpha = false;

		truthDy = 0.01;
		truthDx = 0.001;

		_tmpOne = new FuzzySet(100);
		_tmpTwo = new FuzzySet(100);

		lastRule = null;
	}

	/// Reasoning system reset
	/// Old configuration and rules are deleted and completely new system is created
	/// according to the configuration
	public void Reset(SystemConfig config) {
		// delete old system data
		_delete_properties();
		DeleteRules();
		// configure system according to config
		extended = config.getExtended();
		ins = null;
		outs = null;

		isize = osize = 0;
		setInputWidth(config.getInputWidth());
		setOutputWidth(config.getOutputWidth());

		psize = 0;
		maxpsize = config.getNumberOfPremiseSets();
		PreS = new FuzzySet[maxpsize];
		csize = 0;
		maxcsize = config.getNumberOfConclusionSets();
		ConS = new FuzzySet[maxcsize];
		InIndex = new HashMap<String, Integer>();
		OutIndex = new HashMap<String, Integer>();
		PreIndex = new HashMap<String, Integer>();
		ConIndex = new HashMap<String, Integer>();

		opIS = OperationCreator.newTNorm(config.getIsOperationType());
		opOR = OperationCreator.newSNorm(config.getOrOperationType());
		opAND = OperationCreator.newTNorm(config.getAndOperationType());
		impl = OperationCreator.newImplication(config.getImplicationType());
		ruleAgregator = OperationCreator.newNorm(config.getConclusionAgregationType());
		truthComp = OperationCreator.newTNorm(config.getTruthCompositionType());
		defuzz = config.getDefuzzyficationType();
		alpha = config.getDefuzzyficationAlpha();
		autoDefuzz = config.getAutoDefuzzyfication();
		autoAlpha = config.getAutoAlpha();

		truthDy = config.getTruthYPrecision();
		truthDx = config.getTruthXPrecision();

		_tmpOne = new FuzzySet(100);
		_tmpTwo = new FuzzySet(100);

		lastRule = null;
	}

	/// Reconfigures reasoning system
	/// Reconfigure method allows to change system operations like:
	/// IS,OR,AND,implication,agregation and defuzzyfication type and options.
	/// Other items configured in config parameter are ignored.
	public void Reconfigure(SystemConfig config) {
		// delete old configuration data
		opIS = null;
		opOR = null;
		opAND = null;
		impl = null;
		ruleAgregator = null;

		// apply new configuration according to the config
		opIS = OperationCreator.newTNorm(config.getIsOperationType());
		opOR = OperationCreator.newSNorm(config.getOrOperationType());
		opAND = OperationCreator.newTNorm(config.getAndOperationType());
		impl = OperationCreator.newImplication(config.getImplicationType());
		ruleAgregator = OperationCreator.newNorm(config.getConclusionAgregationType());
		truthComp = OperationCreator.newTNorm(config.getTruthCompositionType());
		defuzz = config.getDefuzzyficationType();
		alpha = config.getDefuzzyficationAlpha();
		autoDefuzz = config.getAutoDefuzzyfication();
		autoAlpha = config.getAutoAlpha();

		truthDy = config.getTruthYPrecision();
		truthDx = config.getTruthXPrecision();

		// apply pointers of new operations to rules
		ListIterator<Rule> it = Rules.listIterator();
		int j;
		while (it.hasNext()) {
			Rule el = it.next();
			for (j = 0; j < el.psize; j++) { // for all premise items
				if (el.pits[j].op_type == OpType.AND)
					el.pits[j].op = opAND;
				else if (el.pits[j].op_type == OpType.OR)
					el.pits[j].op = opOR;
			}
		}
	}

	public void DeleteRules() {
		ListIterator<Rule> it = Rules.listIterator();
		while (it.hasNext())
			it = null;
		Rules.clear();
	}

	public void setInputWidth(int _isize) {
		if (_isize <= 0)
			return;
		InVar[] tmp = new InVar[_isize];
//ZMIANA
		for (int i = 0; i < _isize; i++)
			tmp[i] = new InVar();

		if (ins != null) { // copy old content if resizing
			// choose smaller size for copying
			int size;
			if (isize > _isize)
				size = _isize;
			else
				size = isize;

			for (int i = 0; i < size; i++)
				tmp[i].assign(ins[i]);
//KONIEC
			ins = null;
		}

		ins = tmp;
		isize = _isize;
	}

	public void setOutputWidth(int _osize) {
		if (_osize <= 0)
			return;
		OutVar[] tmp = new OutVar[_osize];
//ZMIANA
		for (int i = 0; i < _osize; i++)
			tmp[i] = new OutVar();

		if (outs != null) { // copy old content if resizing
			// choose smaller size for copying
			int size;
			if (osize > _osize)
				size = _osize;
			else
				size = osize;

			for (int i = 0; i < size; i++)
				tmp[i].assign(outs[i]);
//KONIEC
			outs = null;
		}

		outs = tmp;
		osize = _osize;
	}

	public void describeInputVar(int nr, String id, String description) {
		ins[nr].id = id;
		ins[nr].des = description;
		InIndex.put(id, nr); // add search index
	}

	public void describeOutputVar(int nr, String id, String description) {
		outs[nr].id = id;
		outs[nr].des = description;
		OutIndex.put(id, nr); // add search index
	}

	public void addPremiseSet(FuzzySet fs) {
		// if array size is not enough
		if (psize + 1 > maxpsize) {
			FuzzySet[] tmp;
			maxpsize += DEFAULT_SETARRAY_EXTENSION;
			tmp = new FuzzySet[maxpsize]; // new memory
			for (int i = 0; i < psize; i++) {
				tmp[i] = PreS[i];
			} // copy old content
//ZMIANA 
			PreS = tmp; // free old memory and set new
//KONIEC
		}

		PreS[psize] = fs;
		PreIndex.put(fs.getId(), psize); // add search index
		psize++;
	}

	public FuzzySet getPremiseSet(String id) throws Exception {
		FuzzySet fset = null;
		if (PreIndex.containsKey(id)) {
			return PreS[PreIndex.get(id)];
		}
		// linear search
//		for (int i = 0; i < psize; i++) {
//			if (PreS[i].getId().equals(id)) {
//				return PreS[i];
//			}
//		}
		throw new Exception("Premise set '" + id + "'not found");
	}

	public void addConclusionSet(FuzzySet fs) {
		// if array size is not enough
		if (csize + 1 > maxcsize) {
			FuzzySet[] tmp;
			maxcsize += DEFAULT_SETARRAY_EXTENSION;
			tmp = new FuzzySet[maxcsize]; // new memory
			for (int i = 0; i < csize; i++) {
				tmp[i] = ConS[i];
			} // copy old content
				// ZMIANA
			ConS = tmp; // free old memory and set new
			// KONIEC
		}

		ConS[csize] = fs;
		ConIndex.put(fs.getId(), csize); // add search index
		csize++;
	}

	public FuzzySet getConclusionSet(String id) throws Exception {
		FuzzySet fset = null;
		if (ConIndex.containsKey(id)) {
			return ConS[ConIndex.get(id)];
		}
		// linear serch
//		for (int i = 0; i < csize; i++) {
//			if (ConS[i].getId().equals(id)) {
//				return ConS[i];
//			}
//		}
		throw new Exception("Conclusion set '" + id + "'not found");
	}

	public void addRule() {
		addRule(DEFAULT_RULE_SIZE, DEFAULT_RULE_SIZE);
	}

	public void addRule(int premise_size) {
		addRule(premise_size, DEFAULT_RULE_SIZE);
	}

	public void addRule(int premise_size, int conclusion_size) {
		lastRule = new Rule(premise_size, conclusion_size);
		Rules.add(lastRule);
	}

//    void addRule(string rule); // "if iID is pID and iID is pID then oID is cID,oID is cID"
	public void addRuleItem(int varA, int setA, short operation, int varB, int setB) {
		if (lastRule == null)
			return;
		Norm tmp = opAND;
		if (operation == OpType.OR)
			tmp = opOR;
		lastRule.AddPreItem(varA, setA, varB, setB, operation, tmp);

	}

	public void addRuleItem(int varA, int setA) {
		if (lastRule == null)
			return;
		lastRule.AddPreItem(varA, setA, -1, -2, OpType.NO_OPERATION, null);
	}

	// *****************************************************************************************************

	public void addRuleItem(String varA, String setA, String operation, String varB, String setB) throws Exception {
		if (lastRule == null)
			return;

		int idxVarA = -2;
		int idxSetA = -2;
		int idxVarB = -2;
		int idxSetB = -2;
		short op = OpType.AND;
		Norm tmp_op = opAND;
		int i;

		if (varA.equals("STACK")) {
			idxVarA = -1;
			idxSetA = -1;
		}
		if (varB.equals("STACK")) {
			idxVarB = -1;
			idxSetB = -1;
		}

		// get variable A and set A indices
		if (idxVarA == -2) {
			if (InIndex.containsKey(varA)) {
				idxVarA = InIndex.get(varA);
			} else { // old search if not in index
				// !!! TO BE DELETED after making getInputVar private !!!
				for (i = 0; i < isize; i++) {
					if (ins[i].id.equals(varA)) {
						idxVarA = i;
						i = isize;
					}
				}
			}
			if (PreIndex.containsKey(setA)) {
				idxSetA = PreIndex.get(setA);
			}
//			for (i = 0; i < psize; i++) {
//				if (PreS[i].getId().equals(setA)) {
//					idxSetA = i;
//					i = psize;
//				}
//			}
		}

		// variable not found
		if (idxVarA == -2) {
			throw new Exception("Variable " + varA + " not found");
		}
		// set not found
		if (idxSetA == -2) {
			throw new Exception("Set " + setA + " not found");
		}

		// get variable B and set B indices
		if (idxVarB == -2) {
			if (InIndex.containsKey(varB)) {
				idxVarB = InIndex.get(varB);
			} else { // old search if not in index
				// !!! TO BE DELETED after making getInputVar private !!!
				for (i = 0; i < isize; i++) {
					if (ins[i].id.equals(varB)) {
						idxVarB = i;
						i = isize;
					}
				}
			}
			if (PreIndex.containsKey(setB)) {
				idxSetB = PreIndex.get(setB);
			}
//			for (i = 0; i < psize; i++) {
//				if (PreS[i].getId().equals(setB)) {
//					idxSetB = i;
//					i = psize;
//				}
//			}
		}
		// variable not found
		if (idxVarB == -2) {
			throw new Exception("Variable " + varB + " not found");
		}
		// set not found
		if (idxSetB == -2) {
			throw new Exception("Set " + setB + " not found");
		}

		if (operation.equals("OR")) {
			op = OpType.OR;
			tmp_op = opOR;
		}

		lastRule.AddPreItem(idxVarA, idxSetA, idxVarB, idxSetB, op, tmp_op);
	}

	// *****************************************************************************************************
	public void addRuleItem(String varA, String setA) throws Exception {
		if (lastRule == null)
			return;

		// get variable index
		int idxVar = -2;
		if (InIndex.containsKey(varA)) {
			idxVar = InIndex.get(varA);
		} else { // old search if not in index
			// !!! TO BE DELETED after making getInputVar private !!!
			for (int i = 0; i < isize; i++) {
				if (ins[i].id.equals(varA)) {
					idxVar = i;
					i = isize; // to break the loop nicely
				}
			}
		}
		// variable not found
		if (idxVar == -2) {
			throw new Exception("Variable " + varA + " not found");
		}

		// get index of the set
		int idxSet = -2;
		if (PreIndex.containsKey(setA)) {
			idxSet = PreIndex.get(setA);
		}
//		for (int i = 0; i < psize; i++) {
//			if (PreS[i].getId().equals(setA)) {
//				idxSet = i;
//				i = psize; // to break the loop nicely
//			}
//		}
		// set not found
		if (idxSet == -2) {
			throw new Exception("Set " + setA + " not found");
		}

		lastRule.AddPreItem(idxVar, idxSet, -2, -2, OpType.NO_OPERATION, null);
	}

	public void addRuleConclusion(int varC, int setC) {
		if (lastRule == null)
			return;
		lastRule.AddConItem(varC, setC, extended);
		outs[varC].cnum++; // update number of occurrence in rules (needed to calculate an average)
	}

	// ***************************************************************
	public void addRuleConclusion(String varC, String setC) throws Exception {
		if (lastRule == null)
			return;

		// get variable index
		int idxVar = -2;
		if (InIndex.containsKey(varC)) {
			idxVar = InIndex.get(varC);
		} else { // old search if not in index
			// !!! TO BE DELETED after making getInputVar private !!!
			for (int i = 0; i < osize; i++) {
				if (outs[i].id.equals(varC)) {
					idxVar = i;
					i = osize; // to break the loop nicely
				}
			}
		}
		// variable not found
		if (idxVar == -2) {
			throw new Exception("Variable " + varC + " not found");
		}

		// get index of the set
		int idxSet = -2;
		if (ConIndex.containsKey(setC)) {
			idxSet = ConIndex.get(setC);
		}
//		for (int i = 0; i < csize; i++) {
//			if (ConS[i].getId().equals(setC)) {
//				idxSet = i;
//				i = csize; // to break the loop nicely
//			}
//		}
		// set not found
		if (idxSet == -2) {
			throw new Exception("Set " + setC + " not found");
		}

		lastRule.AddConItem(idxVar, idxSet, extended);
		outs[idxVar].cnum++;

	}

	// ***************************************************************
	public void setInput(int nr, double value) {
		ins[nr].inval = value;
	}

	public void setInput(double[] array) {
		for (int i = 0; i < isize; i++)
			ins[i].inval = array[i];
	}

	public InVar getInputVar(int nr) {
		return ins[nr];
	}

	public double getOutput(int nr) {
		return outs[nr].outval;
	}

	public void getOutput(double[] array) {
		for (int i = 0; i < isize; i++)
			array[i] = outs[i].outval;
	}

	public OutVar getOutputVar(int nr) {
		return outs[nr];
	}

	public double getAlpha() {
		return alpha;
	};

	public void Process() {
		// 1. Prepare input variables (i.e. fuzzyfication)
		int i, j;
		// fuzzyfy input values if fuzzyfier is configured
		for (i = 0; i < isize; i++) {
//ZMIANA
			if (ins[i].fuzz != null) {
				if (ins[i].invalF == null)
					ins[i].invalF = new FuzzySet();
				ins[i].invalF.assign(ins[i].fuzz);
//KONIEC ZMIANY
				ins[i].invalF.fuzzyfy(ins[i].inval);
			}

		}

		// 2. Prepare output sets - set begin and end value of output variable
		// - (Mamdani reasoning) for SNorm agregation clear set (lowx/0.0,highx/0.0)
		// - (logical reasoning) for TNorm agregation fill set (lowx/1.0,highx/1.0)
		double fill = 0.0;

//ZMIANA typu zwracanego przez metodę isTNorm

		if (TNorm.isTNorm(ruleAgregator) == true)
			fill = 1.0;

		for (i = 0; i < osize; i++) {
			outs[i].outset.ClearSet();
			outs[i].outset.addPoint(0.0, fill);
//            outs[i].outset.AddPoint(outs[i].outset.begin,fill);
//            outs[i].outset.AddPoint(outs[i].outset.end,fill);
		}

		if (autoAlpha)
			alpha = 0.0; // reset alpha before automatic calculation

		// 3. For all rules
		Stack<Double> st = new Stack<Double>();
		ListIterator<Rule> it = Rules.listIterator();
		double tmpLeft, tmpRight, result;

		while (it.hasNext()) {
			Rule el = it.next();
			// 3.1 Solve premise part (result on stack)
			for (j = 0; j < el.psize; j++) { // for all premise items

				// 3.1.1 Calculate defined operation for two membership levels taken from
				// - result of IS operation for input variables and premise sets
				// - or taken from a stack (result saved earlier)

				// Left parameter of an operation:
				tmpLeft = 0.0;
				if (el.pits[j].iLVar == -1) { // if the left parameter is on a stack
					tmpLeft = st.peek();
					st.pop();
				} else if (el.pits[j].iLVar > -1) { // to avoid wrong indexing
					// left parameter needs to be calculated
					if (ins[(el.pits[j].iLVar)].invalF != null) { // if left variable is fuzzyfied
						FuzzySet.processSetsWithNorm(_tmpOne, (ins[el.pits[j].iLVar].invalF), PreS[el.pits[j].iLSet],
								opIS);

						tmpLeft = _tmpOne.getMaximumMembership();
					} else { // - left variable is a singleton
						tmpLeft = PreS[el.pits[j].iLSet].getMembership(ins[el.pits[j].iLVar].inval);
					}
				}

				// Calculate operation if available and put membership of the result (premise
				// item) on a stack
				if (el.pits[j].op != null) { // if operation is defined => there is a right parameter
					tmpRight = 0.0;
					if (el.pits[j].iRVar == -1) { // if the right parameter is on a stack
						tmpRight = st.peek();
						st.pop();
					} else if (el.pits[j].iRVar > -1) { // to avoid wrong indexing
						// calculate right variable's membership level
						if (ins[el.pits[j].iRVar].invalF != null) { // Right variable fuzzyfied
							// *_tmpOne = *(ins[(*it)->pits[j].iLVar].invalF);
							FuzzySet.processSetsWithNorm(_tmpOne, (ins[el.pits[j].iRVar].invalF),
									PreS[el.pits[j].iRSet], opIS);

							tmpRight = _tmpOne.getMaximumMembership();
						} else { // Right variable is a singleton
							tmpRight = PreS[el.pits[j].iRSet].getMembership(ins[el.pits[j].iRVar].inval);
						}
					}
					// calculate output membership level according to defined operation
					tmpLeft = el.pits[j].op.calc(tmpLeft, tmpRight);
				}
				// push result on the stack
				st.push(tmpLeft);

			} // loop through all premise items

			// For all conclusion items:
			// 3.2 Calculate conclusion using defined implication according to premise
			// result
			// 3.3 Apply conclusion to output variables using defined agregation method

			result = st.peek();
			st.pop();

			for (j = 0; j < el.csize; j++) { // for all conclusion items
				// copy conclusion set
				// *_tmpOne = *ConS[(*it)->cits[j].iSet];
				// process conclusion set with membership level (reasoning,implication)
				// _tmpOne->processSetAndMembershipWithNorm(result,*impl);

				// process conclusion set with membership level (reasoning,implication)
				// - alternative approach
				_tmpTwo.ClearSet();
				_tmpTwo.addPoint(0.0, result);
				FuzzySet.processSetsWithNorm(_tmpOne, _tmpTwo, ConS[el.cits[j].iSet], impl);

				// update alpha value - one step
				if (autoAlpha) {
					alpha += impl.calc(result, 0.0);
				}

				// agregate result to a set of output variable
				FuzzySet.processSetsWithNorm(_tmpTwo, _tmpOne, outs[el.cits[j].iVar].outset, ruleAgregator);
				(_tmpTwo).PackFlatSections();
//ZMIANA
				outs[el.cits[j].iVar].outset.assign(_tmpTwo);
//KONIEC
			}
		} // loop through all rules

		if (autoAlpha)
			alpha = alpha / Rules.size(); // finish automatic alpha calculation

		// 4. Defuzzyfy output variables
		if (autoDefuzz) {
			for (i = 0; i < osize; i++) {
				outs[i].outval = outs[i].outset.DeFuzzyfyEx(defuzz, alpha);
			}
		}
	}

	// public void ProcessAndReport(std::ostream & os, int precision = 0);
	// //reasoning process with percise reasoning report
	public void ProcessBaldwin() {
		// 1. Prepare input variables (i.e. fuzzyfication)
		int i, j;
		// fuzzyfy input values if fuzzyfier is configured
		for (i = 0; i < isize; i++) {
			if (ins[i].fuzz != null) {
				if (ins[i].invalF == null)
					ins[i].invalF = new FuzzySet();
				ins[i].invalF.assign(ins[i].fuzz);
				ins[i].invalF.fuzzyfy(ins[i].inval);
			}

		}

		// 2. Prepare output sets - set begin and end value of output variable
		// - (Mamdani reasoning) for SNorm agregation clear set (lowx/0.0,highx/0.0)
		// - (logical reasoning) for TNorm agregation fill set (lowx/1.0,highx/1.0)
		double fill = 0.0;
		if (TNorm.isTNorm(ruleAgregator.getType()) == true)
			fill = 1.0;

		for (i = 0; i < osize; i++) {
			outs[i].outset.ClearSet();
			outs[i].outset.addPoint(0.0, fill);
//            outs[i].outset.AddPoint(outs[i].outset.begin,fill);
//            outs[i].outset.AddPoint(outs[i].outset.end,fill);
		}

		if (autoAlpha)
			alpha = 0.0; // reset alpha before automatic calculation

		// 3. For all rules
		Stack<Double> st = new Stack<Double>();
		ListIterator<Rule> it = Rules.listIterator();
		double tmpLeft, tmpRight, result;

		while (it.hasNext()) {
			Rule el = it.next();
			// 3.1 Solve premise part (result on stack)
			for (j = 0; j < el.psize; j++) { // for all premise items

				// 3.1.1 Calculate defined operation for two membership levels taken from
				// - result of IS operation for input variables and premise sets
				// - or taken from a stack (result saved earlier)

				// Left parameter of an operation:
				tmpLeft = 0.0;
				if (el.pits[j].iLVar == -1) { // if the left parameter is on a stack
					tmpLeft = st.peek();
					st.pop();
				} else if (el.pits[j].iLVar > -1) { // to avoid wrong indexing
					// left parameter needs to be calculated
					if (ins[el.pits[j].iLVar].invalF != null) { // if left variable is fuzzyfied
						// *_tmpOne = *(ins[(*it)->pits[j].iLVar].invalF);
						FuzzySet.processSetsWithNorm(_tmpOne, (ins[el.pits[j].iLVar].invalF), PreS[el.pits[j].iLSet],
								opIS);

						tmpLeft = _tmpOne.getMaximumMembership();
					} else { // - left variable is a singleton
						tmpLeft = PreS[el.pits[j].iLSet].getMembership(ins[el.pits[j].iLVar].inval);
					}
				}

				// Calculate operation if available and put membership of the result (premise
				// item) on a stack
				if (el.pits[j].op != null) { // if operation is defined => there is a right parameter
					tmpRight = 0.0;
					if (el.pits[j].iRVar == -1) { // if the right parameter is on a stack
						tmpRight = st.peek();
						st.pop();
					} else if (el.pits[j].iRVar > -1) { // to avoid wrong indexing
						// calculate right variable's membership level
						if (ins[el.pits[j].iRVar].invalF != null) { // Right variable fuzzyfied
							// *_tmpOne = *(ins[(*it)->pits[j].iLVar].invalF);
							FuzzySet.processSetsWithNorm(_tmpOne, (ins[el.pits[j].iRVar].invalF),
									PreS[el.pits[j].iRSet], opIS);

							tmpRight = _tmpOne.getMaximumMembership();
						} else { // Right variable is a singleton
							tmpRight = PreS[el.pits[j].iRSet].getMembership(ins[el.pits[j].iRVar].inval);
						}
					}
					// calculate output membership level according to defined operation
					tmpLeft = el.pits[j].op.calc(tmpLeft, tmpRight);
				}
				// push result on a stack
				st.push(tmpLeft);

			} // loop through all premise items

			// For all conclusion items:
			// 3.2 Calculate conclusion using defined implication according to premise
			// result
			// 3.3 Apply conclusion to output variables using defined agregation method

			result = st.peek();
			st.pop();

			for (j = 0; j < el.csize; j++) { // for all conclusion items
				// calculate truth function for conclusion
				FuzzySet.calcConcTruthFunction(_tmpTwo, result, impl, truthDy, truthDx);
				// copy conclusion set
				// *_tmpOne = *ConS[(*it)->cits[j].iSet];
				// process conclusion set with truth function
				_tmpOne.ProcessSetWithTruthFunction(_tmpTwo, ConS[el.cits[j].iSet]);

				// update alpha value - one step
				if (autoAlpha) {
					alpha += _tmpTwo.getMembership(0.0);
				}

				// agregate result to a set of output variable
				FuzzySet.processSetsWithNorm(_tmpTwo, _tmpOne, outs[el.cits[j].iVar].outset, ruleAgregator);
				(_tmpTwo).PackFlatSections();
//ZMIANA
				outs[el.cits[j].iVar].outset.assign(_tmpTwo);
//KONIEC
			}
		} // loop through all rules

		if (autoAlpha)
			alpha = alpha / Rules.size(); // finish automatic alpha calculation

		// 4. Defuzzyfy output varaibles
		if (autoDefuzz) {
			for (i = 0; i < osize; i++) {
				outs[i].outval = outs[i].outset.DeFuzzyfyEx(defuzz, alpha);
			}
		}
	} // Baldwin reasoning process

	public void ProcessBaldwinAndReport(Writer os, int precision) { // Baldwin reasoning process with report
		// if (precision > 0) os.precision(precision);

		// ----START report
		try {
			os.write("<============================ BALDWIN REASONING PROCESS START\n");

			// ----END report

			// 1. Prepare input variables (i.e. fuzzyfication)
			int i, j;
			// fuzzyfy input values if fuzzyfier is configured
			for (i = 0; i < isize; i++) {
				// ----START report
				os.write("Input Var " + i + ": " + ins[i].inval + "\n");
				// ----END report
				if (ins[i].fuzz != null) {
					if (ins[i].invalF == null)
						ins[i].invalF = new FuzzySet();
					ins[i].invalF.assign(ins[i].fuzz);
					ins[i].invalF.fuzzyfy(ins[i].inval);
					// ----START report
					os.write("Fuzzyfied: " + ins[i].invalF + "\n");
					// ----END report
				}

			}

			// 2. Prepare output sets - set begin and end value of output variable
			// - (logical reasoning) for TNorm agregation fill set (lowx/1.0,highx/1.0)
			// - (Mamdani reasoning) for other agregation (SNorm,average ...) clear set
			// (lowx/0.0,highx/0.0)
			double fill = 0.0;
			if (TNorm.isTNorm(ruleAgregator.getType()) == true)
				fill = 1.0;

			for (i = 0; i < osize; i++) {
				outs[i].outset.ClearSet();
				outs[i].outset.addPoint(0.0, fill);
//            outs[i].outset.AddPoint(outs[i].outset.begin,fill);
//            outs[i].outset.AddPoint(outs[i].outset.end,fill);
				// ----START report
				os.write("Output Var " + i + " (conclusion set before processing): " + outs[i].outset + "\n");
				// ----END report
			}

			if (autoAlpha)
				alpha = 0.0; // reset alpha before automatic calculation

			// 3. For all rules
			Stack<Double> st = new Stack<Double>();
			ListIterator<Rule> it = Rules.listIterator();
			;
			double tmpLeft, tmpRight, result;

			// ----START report
			os.write("\nNumber of rules :" + Rules.size() + "\n");
			int rule_counter = 1;
			// ----END report

			while (it.hasNext()) {
				Rule el = it.next();
				// 3.1 Solve premise part (result on stack)
				// ----START report
				os.write("<- RULE " + rule_counter + " BEGINS\n");
				os.write("Rule premise items :" + el.psize + "\n");
				// ----END report
				for (j = 0; j < el.psize; j++) { // for all premise items

					// 3.1.1 Calculate defined operation for two membership levels taken from
					// - result of IS operation for input variables and premise sets
					// - or taken from a stack (result saved earlier)

					// ----START report
					os.write("__premise item nr " + j + "\n");
					// ----END report

					// Left parameter of an operation:
					tmpLeft = 0.0;
					if (el.pits[j].iLVar == -1) { // if the left parameter is on a stack
						tmpLeft = st.peek();
						st.pop();
						// ----START report
						os.write("left parameter taken from stack\n");
						// ----END report
					} else if (el.pits[j].iLVar > -1) { // to avoid wrong indexing
						// left parameter needs to be calculated
						if (ins[el.pits[j].iLVar].invalF != null) { // if left variable is fuzzyfied
							// *_tmpOne = *(ins[(*it)->pits[j].iLVar].invalF);
							FuzzySet.processSetsWithNorm(_tmpOne, (ins[el.pits[j].iLVar].invalF),
									PreS[el.pits[j].iLSet], opIS);

							tmpLeft = _tmpOne.getMaximumMembership();
							// ----START report
							os.write("left variable '" + ins[el.pits[j].iLVar].id + "\n");
							os.write("' fuzzyfied (set): " + (ins[el.pits[j].iLVar].invalF) + "\n");
							os.write("left premise set '" + PreS[el.pits[j].iLSet].getId());
							os.write("' : " + PreS[el.pits[j].iLSet] + "\n");
							os.write("result set: " + _tmpOne + "\n");
							os.write("left truth function-singleton (for max membership level): " + tmpLeft + "\n");
							// ----END report
						} else { // - left variable is a singleton
							tmpLeft = PreS[el.pits[j].iLSet].getMembership(ins[el.pits[j].iLVar].inval);
							// ----START report
							os.write("left variable '" + ins[el.pits[j].iLVar].id);
							os.write("' is a singleton: " + ins[el.pits[j].iLVar].inval + "\n");
							os.write("left premise set '" + PreS[el.pits[j].iLSet].getId());
							os.write("': " + PreS[el.pits[j].iLSet] + "\n");
							os.write("left truth function-singleton (for max membership level): " + tmpLeft + "\n");
							// ----END report
						}
					}

					// Calculate operation if available and put membership of the result (premise
					// item) on a stack
					if (el.pits[j].op != null) { // if operation is defined => there is a right parameter
						tmpRight = 0.0;
						if ((el.pits[j].iRVar) == -1) { // if the right parameter is on a stack
							tmpRight = st.peek();
							st.pop();
							// ----START report
							os.write("right parameter taken from stack" + "\n");
							// ----END report
						} else if (el.pits[j].iRVar > -1) { // to avoid wrong indexing
							// calculate right variable's membership level
							if (ins[el.pits[j].iRVar].invalF != null) { // Right variable fuzzyfied
								// *_tmpOne = *(ins[(*it)->pits[j].iLVar].invalF);
								FuzzySet.processSetsWithNorm(_tmpOne, (ins[el.pits[j].iRVar].invalF),
										PreS[el.pits[j].iRSet], opIS);

								tmpRight = _tmpOne.getMaximumMembership();
								// ----START report
								os.write("right variable '" + ins[el.pits[j].iRVar].id);
								os.write("' fuzzyfied (set): " + (ins[el.pits[j].iRVar].invalF) + "\n");
								os.write("right premise set '" + PreS[el.pits[j].iRSet].getId());
								os.write("': " + PreS[el.pits[j].iRSet] + "\n");
								os.write("result set: " + _tmpOne + "\n");
								os.write("right truth function-singleton (for max membership level): " + tmpRight
										+ "\n");
								// ----END report
							} else { // Right variable is a singleton
								tmpRight = PreS[el.pits[j].iRSet].getMembership(ins[el.pits[j].iRVar].inval);
								// ----START report
								os.write("right variable '" + ins[el.pits[j].iRVar].id);
								os.write("' is a singleton: " + ins[el.pits[j].iRVar].inval + "\n");
								os.write("right premise set '" + PreS[el.pits[j].iRSet].getId());
								os.write("': " + PreS[el.pits[j].iRSet] + "\n");
								os.write("right truth function-singleton (for max membership level): " + tmpRight
										+ "\n");
								// ----END report
							}
						}
						// calculate output membership level according to defined operation
						tmpLeft = el.pits[j].op.calc(tmpLeft, tmpRight);
						// ----START report
						if (TNorm.isTNorm(el.pits[j].op.getType()) == true)
							os.write("result of AND between left and right truth functions (singletons): " + tmpLeft
									+ "\n");
						else
							os.write("result of OR between left and right truth functions (singletons): " + tmpLeft
									+ "\n");
						// ----END report
					}
					// push result on a stack
					st.push(tmpLeft);

				} // loop through all premise items

				// For all conclusion items:
				// 3.2 Calculate conclusion using defined implication according to premise
				// result
				// 3.3 Apply conclusion to output variables using defined agregation method

				result = st.peek();
				st.pop();

				// ----START report
				os.write("Premise truth function (singleton) : " + result + "\n");
				os.write("Rule conclusion items : " + el.csize + "\n");
				// ----END report

				for (j = 0; j < el.csize; j++) { // for all conclusion items

					// calculate truth function for implication
					FuzzySet.calcConcTruthFunction(_tmpTwo, result, impl, truthDy, truthDx);
					// ----START report
					os.write("Implication truth function : " + _tmpTwo + "\n");
					// ----END report

					// update alpha value - one step
					if (autoAlpha) {
						alpha += _tmpTwo.getMembership(0.0);
					}

					// process conclusion set with truth function
					_tmpOne.ProcessSetWithTruthFunction(_tmpTwo, ConS[el.cits[j].iSet]);

					// agregate result to a set of output variable
					FuzzySet.processSetsWithNorm(_tmpTwo, _tmpOne, outs[el.cits[j].iVar].outset, ruleAgregator);
					(_tmpTwo).PackFlatSections();
					// ----START report
					os.write("Conclusion set '" + ConS[el.cits[j].iSet].getId());
					os.write("': " + ConS[el.cits[j].iSet] + "\n");
					os.write("conclusion after truth functional modification : " + _tmpOne + "\n");
					os.write("output variable '" + outs[el.cits[j].iVar].id);
					os.write("' (set) : " + outs[el.cits[j].iVar].outset + "\n");
					os.write("agregated conclusion (set) : " + _tmpTwo + "\n");
					// ----END report
//ZMIANA
					outs[el.cits[j].iVar].outset.assign(_tmpTwo);
//KONIEC
				}
				// ----START report
				os.write("-> RULE " + rule_counter++ + " ENDS" + "\n\n");
				// ----END report
			} // loop through all rules

			if (autoAlpha) {
				alpha = alpha / Rules.size(); // finish automatic alpha calculation
				// ----START report
				os.write("Alpha (defuzzyfication parameter) : " + alpha + "\n");
				// ----END report
			}

			// 4. Defuzzyfy output varaibles
			if (autoDefuzz) {
				for (i = 0; i < osize; i++) {
					outs[i].outval = outs[i].outset.DeFuzzyfyEx(defuzz, alpha);
					// ----START report
					os.write("Defuzzyfied output variable " + i + " '" + outs[i].id);
					os.write("': " + outs[i].outval + "\n");
					// ----END report
				}
			}

			// ----START report
			os.write("<============================ REASONING PROCESS END" + "\n");
			// ----END report

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void ProcessBaldwinFull() {
		// 1. Prepare input variables (i.e. fuzzyfication)
		int i, j;
		// fuzzyfy input values if fuzzyfier is configured
		for (i = 0; i < isize; i++) {
			if (ins[i].fuzz != null) {
				if (ins[i].invalF == null)
					ins[i].invalF = new FuzzySet();
				ins[i].invalF.assign(ins[i].fuzz);
				ins[i].invalF.fuzzyfy(ins[i].inval);
			}

		}

		// 2. Prepare output sets - set begin and end value of output variable
		// - (Mamdani reasoning) for SNorm agregation clear set (lowx/0.0,highx/0.0)
		// - (logical reasoning) for TNorm agregation fill set (lowx/1.0,highx/1.0)
		double fill = 0.0;
		if (TNorm.isTNorm(ruleAgregator.getType()) == true)
			fill = 1.0;

		for (i = 0; i < osize; i++) {
			outs[i].outset.ClearSet();
			outs[i].outset.addPoint(0.0, fill);
//            outs[i].outset.AddPoint(outs[i].outset.begin,fill);
//            outs[i].outset.AddPoint(outs[i].outset.end,fill);
		}

		if (autoAlpha)
			alpha = 0.0; // reset alpha before automatic calculation

		// 3. For all rules
		Stack<FuzzySet> st = new Stack<FuzzySet>();
		ListIterator<Rule> it = Rules.listIterator();
		FuzzySet tmpLeft = new FuzzySet();
		FuzzySet tmpRight = new FuzzySet();
		FuzzySet result = new FuzzySet();

		while (it.hasNext()) {
			Rule el = it.next();
			// 3.1 Solve premise part (result on stack)
			for (j = 0; j < el.psize; j++) { // for all premise items

				// 3.1.1 Calculate defined operation for two membership levels taken from
				// - result of IS operation for input variables and premise sets
				// - or taken from a stack (result saved earlier)

				// Left parameter of an operation:
				tmpLeft.ClearSet();
				if (el.pits[j].iLVar == -1) { // if the left parameter is on a stack
					tmpLeft.assign(st.peek());
					st.pop();
				} else if (el.pits[j].iLVar > -1) { // to avoid wrong indexing
					// left parameter needs to be calculated
					if (ins[el.pits[j].iLVar].invalF != null) { // if left variable is fuzzyfied
						// calculate fuzzy truth function for fuzzyfied input
						FuzzySet.calcIsTruthFunction(tmpLeft, ins[el.pits[j].iLVar].invalF, PreS[el.pits[j].iLSet],
								truthDy, truthDx);
					} else { // - left variable is a singleton
						double tmp = PreS[el.pits[j].iLSet].getMembership(ins[el.pits[j].iLVar].inval);
						tmpLeft.addPoint(0, 0);
						tmpLeft.addPoint(1, 0);
						if (tmp > 0.01)
							tmpLeft.addPoint(tmp - 0.01, 0);
						tmpLeft.addPoint(tmp, 1);
						if (tmp < 0.99)
							tmpLeft.addPoint(tmp + 0.01, 0);
						// tmpLeft.IncreaseYPrecision(0.1, 0.001);
					}
				}

				// Calculate operation if available and put membership of the result (premise
				// item) on a stack
				if (el.pits[j].op != null) { // if operation is defined => there is a right parameter
					tmpRight.ClearSet();
					if (el.pits[j].iRVar == -1) { // if the right parameter is on a stack
						tmpRight.assign(st.peek());
						st.pop();
					} else if (el.pits[j].iRVar > -1) { // to avoid wrong indexing
						// calculate right variable's membership level
						if (ins[el.pits[j].iRVar].invalF != null) { // Right variable fuzzyfied
							// calculate fuzzy truth function for fuzzyfied input
							FuzzySet.calcIsTruthFunction(tmpRight, ins[el.pits[j].iRVar].invalF, PreS[el.pits[j].iRSet],
									truthDy, truthDx);
						} else { // Right variable is a singleton
							double tmp = PreS[el.pits[j].iRSet].getMembership(ins[el.pits[j].iRVar].inval);
							tmpRight.addPoint(0, 0);
							tmpRight.addPoint(1, 0);
							if (tmp > 0.01)
								tmpRight.addPoint(tmp - 0.01, 0);
							tmpRight.addPoint(tmp, 1);
							if (tmp < 0.99)
								tmpRight.addPoint(tmp + 0.01, 0);
							// tmpRight.IncreaseYPrecision(0.1, 0.001);
						}
					}
					// calculate output truth function according to defined operation
					_tmpOne.ClearSet();
					FuzzySet.calcCompoundTruthFunction(_tmpOne, tmpLeft, el.pits[j].op.getType(), tmpRight, opAND,
							truthDy, truthDx);
					// push result on a stack
					st.push(new FuzzySet(_tmpOne));
				} else {
					// push result on a stack - in case of only one premise
					st.push(new FuzzySet(tmpLeft));
				}
			} // loop through all premise items

			// For all conclusion items:
			// 3.2 Calculate conclusion using defined implication according to premise
			// result
			// 3.3 Apply conclusion to output variables using defined agregation method

			result = st.peek();
			st.pop();

			for (j = 0; j < el.csize; j++) { // for all conclusion items
				// calculate truth function for conclusion
				FuzzySet.calcConcTruthFunction(_tmpTwo, result, impl, truthComp, truthDy, truthDx);
				// copy conclusion set
				// *_tmpOne = *ConS[(*it)->cits[j].iSet];
				// process conclusion set with truth function
				_tmpOne.ProcessSetWithTruthFunction(_tmpTwo, ConS[el.cits[j].iSet]);

				// update alpha value - one step
				if (autoAlpha) {
					alpha += _tmpTwo.getMembership(0.0);
				}

				// agregate result to a set of output variable
				FuzzySet.processSetsWithNorm(_tmpTwo, _tmpOne, outs[el.cits[j].iVar].outset, ruleAgregator);
				(_tmpTwo).PackFlatSections();
//ZMIANA
				outs[el.cits[j].iVar].outset.assign(_tmpTwo);
//KONIEC
			}
		} // loop through all rules

		if (autoAlpha)
			alpha = alpha / Rules.size(); // finish automatic alpha calculation

		// 4. Defuzzyfy output varaibles
		if (autoDefuzz) {
			for (i = 0; i < osize; i++) {
				outs[i].outval = outs[i].outset.DeFuzzyfyEx(defuzz, alpha);
			}
		}
	} // Baldwin reasoning process

	public void ProcessBaldwinFullAndReport(Writer os) {

		// ----START report
		try {
			os.write("<============================ BALDWIN REASONING PROCESS: START\n");

			// ----END report

			// 1. Prepare input variables (i.e. fuzzyfication)
			int i, j;
			// fuzzyfy input values if fuzzyfier is configured
			for (i = 0; i < isize; i++) {
				// ----START report
				os.write("Input Var " + i + ": " + ins[i].inval + "\n");
				// ----END report
				if (ins[i].fuzz != null) {
					if (ins[i].invalF == null)
						ins[i].invalF = new FuzzySet();
					ins[i].invalF.assign(ins[i].fuzz);
					ins[i].invalF.fuzzyfy(ins[i].inval);
				}

			}

			// 2. Prepare output sets - set begin and end value of output variable
			// - (Mamdani reasoning) for SNorm agregation clear set (lowx/0.0,highx/0.0)
			// - (logical reasoning) for TNorm agregation fill set (lowx/1.0,highx/1.0)
			double fill = 0.0;
			if (TNorm.isTNorm(ruleAgregator.getType()) == true)
				fill = 1.0;

			for (i = 0; i < osize; i++) {
				outs[i].outset.ClearSet();
				outs[i].outset.addPoint(0.0, fill);
//            outs[i].outset.AddPoint(outs[i].outset.begin,fill);
//            outs[i].outset.AddPoint(outs[i].outset.end,fill);
				// ----START report
				os.write("Output Var " + i + " (conclusion set before processing): " + outs[i].outset + "\n");
				// ----END report
			}

			if (autoAlpha)
				alpha = 0.0; // reset alpha before automatic calculation

			// 3. For all rules
			Stack<FuzzySet> st = new Stack<FuzzySet>();
			ListIterator<Rule> it = Rules.listIterator();
			FuzzySet tmpLeft = new FuzzySet();
			FuzzySet tmpRight = new FuzzySet();
			FuzzySet result;

			// ----START report
			os.write("\nNumber of rules :" + Rules.size() + "\n");
			int rule_counter = 1;
			// ----END report

			while (it.hasNext()) {
				Rule el = it.next();
				// 3.1 Solve premise part (result on stack)
				// ----START report
				os.write("<- RULE " + rule_counter + " BEGINS\n");
				os.write("Rule premise items :" + el.psize + "\n");
				// ----END report
				for (j = 0; j < el.psize; j++) { // for all premise items

					// 3.1.1 Calculate defined operation for two membership levels taken from
					// - result of IS operation for input variables and premise sets
					// - or taken from a stack (result saved earlier)

					// ----START report
					os.write("__premise item nr " + j + "\n");
					// ----END report

					// Left parameter of an operation:
					tmpLeft.ClearSet();
					if (el.pits[j].iLVar == -1) { // if the left parameter is on a stack
						tmpLeft.assign(st.peek());
						st.pop();
						// ----START report
						os.write("left parameter taken from stack\n");
						// ----END report
					} else if (el.pits[j].iLVar > -1) { // to avoid wrong indexing
						// left parameter needs to be calculated
						if (ins[el.pits[j].iLVar].invalF != null) { // if left variable is fuzzyfied
							// calculate fuzzy truth function for fuzzyfied input
							FuzzySet.calcIsTruthFunction(tmpLeft, ins[el.pits[j].iLVar].invalF, PreS[el.pits[j].iLSet],
									truthDy, truthDx);
							// ----START report
							os.write("left variable '" + ins[el.pits[j].iLVar].id + "\n");
							os.write("' fuzzyfied (set): " + (ins[el.pits[j].iLVar].invalF) + "\n");
							os.write("left premise set '" + PreS[el.pits[j].iLSet].getId());
							os.write("' : " + PreS[el.pits[j].iLSet] + "\n");
							os.write("left truth function: " + tmpLeft + "\n");
							// ----END report
						} else { // - left variable is a singleton
							double tmp = PreS[el.pits[j].iLSet].getMembership(ins[el.pits[j].iLVar].inval);
							tmpLeft.addPoint(0, 0);
							tmpLeft.addPoint(1, 0);
							if (tmp > 0.01)
								tmpLeft.addPoint(tmp - 0.01, 0);
							tmpLeft.addPoint(tmp, 1);
							if (tmp < 0.99)
								tmpLeft.addPoint(tmp + 0.01, 0);
							// ----START report
							os.write("left variable '" + ins[el.pits[j].iLVar].id);
							os.write("' is a singleton: " + ins[el.pits[j].iLVar].inval + "\n");
							os.write("left premise set '" + PreS[el.pits[j].iLSet].getId());
							os.write("': " + PreS[el.pits[j].iLSet] + "\n");
							os.write("left truth function-singleton (for max membership level): " + tmpLeft + "\n");
							// ----END report
						}
					}

					// Calculate operation if available and put membership of the result (premise
					// item) on a stack
					if (el.pits[j].op != null) { // if operation is defined => there is a right parameter
						tmpRight.ClearSet();
						if (el.pits[j].iRVar == -1) { // if the right parameter is on a stack
							tmpRight.assign(st.peek());
							st.pop();
							// ----START report
							os.write("right parameter taken from stack" + "\n");
							// ----END report
						} else if (el.pits[j].iRVar > -1) { // to avoid wrong indexing
							// calculate right variable's membership level
							if (ins[el.pits[j].iRVar].invalF != null) { // Right variable fuzzyfied
								// calculate fuzzy truth function for fuzzyfied input
								FuzzySet.calcIsTruthFunction(tmpRight, ins[el.pits[j].iRVar].invalF,
										PreS[el.pits[j].iRSet], truthDy, truthDx);
								// ----START report
								os.write("right variable '" + ins[el.pits[j].iRVar].id + "\n");
								os.write("' fuzzyfied (set): " + (ins[el.pits[j].iRVar].invalF) + "\n");
								os.write("right premise set '" + PreS[el.pits[j].iRSet].getId());
								os.write("' : " + PreS[el.pits[j].iRSet] + "\n");
								os.write("right truth function: " + tmpRight + "\n");
								// ----END report
							} else { // Right variable is a singleton
								double tmp = PreS[el.pits[j].iRSet].getMembership(ins[el.pits[j].iRVar].inval);
								tmpRight.addPoint(0, 0);
								tmpRight.addPoint(1, 0);
								if (tmp > 0.01)
									tmpRight.addPoint(tmp - 0.01, 0);
								tmpRight.addPoint(tmp, 1);
								if (tmp < 0.99)
									tmpRight.addPoint(tmp + 0.01, 0);
								// ----START report
								os.write("right variable '" + ins[el.pits[j].iRVar].id);
								os.write("' is a singleton: " + ins[el.pits[j].iRVar].inval + "\n");
								os.write("right premise set '" + PreS[el.pits[j].iRSet].getId());
								os.write("': " + PreS[el.pits[j].iRSet] + "\n");
								os.write("right truth function-singleton (for max membership level): " + tmpRight
										+ "\n");
								// ----END report
							}
						}
						// calculate output truth function according to defined operation
						_tmpOne.ClearSet();
						FuzzySet.calcCompoundTruthFunction(_tmpOne, tmpLeft, el.pits[j].op.getType(), tmpRight, opAND,
								truthDy, truthDx);
						// push result on a stack
						st.push(new FuzzySet(_tmpOne));
						// ----START report
						if (TNorm.isTNorm(el.pits[j].op.getType()) == true)
							os.write("result of AND between truth functions: " + tmpLeft + "\n");
						else
							os.write("result of OR between truth functions: " + tmpLeft + "\n");
						// ----END report
					} else {
						// push result on a stack - in case of only one premise
						st.push(new FuzzySet(tmpLeft));
					}

				} // loop through all premise items

				// For all conclusion items:
				// 3.2 Calculate conclusion using defined implication according to premise
				// result
				// 3.3 Apply conclusion to output variables using defined agregation method

				result = st.peek();
				st.pop();

				// ----START report
				os.write("Premise truth function: " + result + "\n");
				os.write("Rule conclusion items : " + el.csize + "\n");
				// ----END report

				for (j = 0; j < el.csize; j++) { // for all conclusion items
					// calculate truth function for conclusion
					FuzzySet.calcConcTruthFunction(_tmpTwo, result, impl, truthComp, truthDy, truthDx);
					// ----START report
					os.write("Implication truth function : " + _tmpTwo + "\n");
					// ----END report
					// process conclusion set with truth function
					_tmpOne.ProcessSetWithTruthFunction(_tmpTwo, ConS[el.cits[j].iSet]);

					// update alpha value - one step
					if (autoAlpha) {
						alpha += _tmpTwo.getMembership(0.0);
					}

					// agregate result to a set of output variable
					FuzzySet.processSetsWithNorm(_tmpTwo, _tmpOne, outs[el.cits[j].iVar].outset, ruleAgregator);
					(_tmpTwo).PackFlatSections();
					// ----START report
					os.write("Conclusion set '" + ConS[el.cits[j].iSet].getId());
					os.write("': " + ConS[el.cits[j].iSet] + "\n");
					os.write("conclusion after truth functional modification : " + _tmpOne + "\n");
					os.write("output variable '" + outs[el.cits[j].iVar].id);
					os.write("' (set) : " + outs[el.cits[j].iVar].outset + "\n");
					os.write("agregated conclusion (set) : " + _tmpTwo + "\n");
					// ----END report
//ZMIANA
					outs[el.cits[j].iVar].outset.assign(_tmpTwo);
//KONIEC
				}
				// ----START report
				os.write("-> RULE " + rule_counter++ + " ENDS" + "\n\n");
				// ----END report
			} // loop through all rules

			if (autoAlpha) {
				alpha = alpha / Rules.size(); // finish automatic alpha calculation
				// ----START report
				os.write("Alpha (defuzzyfication parameter) : " + alpha + "\n");
				// ----END report
			}

			// 4. Defuzzyfy output varaibles
			if (autoDefuzz) {
				for (i = 0; i < osize; i++) {
					outs[i].outval = outs[i].outset.DeFuzzyfyEx(defuzz, alpha);
					// ----START report
					os.write("Defuzzyfied output variable " + i + " '" + outs[i].id);
					os.write("': " + outs[i].outval + "\n");
					// ----END report
				}
			}
			// ----START report
			os.write("<============================ REASONING PROCESS: END" + "\n");
			// ----END report

		} catch (IOException e) {
			e.printStackTrace();
		}
	} // Baldwin reasoning process
}

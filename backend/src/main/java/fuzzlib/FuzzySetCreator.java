package fuzzlib;

import java.util.ArrayList;
import java.util.List;

import fuzzlib.norms.SNorm;

public class FuzzySetCreator {
	List<Double> values = new ArrayList<Double>();
	FuzzySet _fuzzyfier;

	public static final int TRIANGLE = 0;
	public static final int TRAPEZIUM = 1;
	public static final int GAUSS = 2;

	public static final int FROM_AVERAGE = 0;
	public static final int FROM_FUZZYFIED_INPUT = 1;
	
	int _type = GAUSS; 

	public void setType(int membership_function_type) {
		this._type = membership_function_type;
	}

	public void clear() {
		values.clear();
	}

	public void addValue(double v) {
		values.add(v);
	}

	public void removeWorst(double ratio){
		if (values.size() == 0) return;
		double average = 0.0;
		double min = 0.0, tmp;

		//1. calculate an average and min
		min = values.get(0);
		for (Double d : values) {
			if (d < min) min = d;
			average += d;
		}
		average /= values.size();

		//3. wyznacz min odlegĹ‚oĹ›Ä‡ od Ĺ›redniej
		boolean first_run = true;
		for (Double d : values) {
			tmp = average - d;
			if (tmp < 0.0) tmp *= -1;
			if (first_run) min = tmp;
			else if (tmp < min) min = tmp;
		}
		
		if (ratio < min) ratio = min;
		
		List<Double> newList = new ArrayList<Double>();
		
		//3. wyznaczaj odlegĹ‚oĹ›ci od Ĺ›redniej i usuwaj
		for (Double d : values) {
			tmp = average - d;
			if (tmp < 0.0) tmp *= -1;
			if (tmp <= ratio) newList.add(d);
		}
		
//		if (values.size() != newList.size())
//			System.out.println("wywalono: " + (values.size() - newList.size()));
		
		values = newList;
	}
	
	public FuzzySet createFromAverage(double width_factor, double min_width) {
		if (values.isEmpty()) {
			FuzzySet fs = new FuzzySet();
			switch(_type){
			case GAUSS:     fs.newGaussian(0.0, min_width*width_factor); break;
			case TRIANGLE:  fs.newTriangle(0.0, min_width*width_factor); break;
			case TRAPEZIUM: fs.newTrapezium(0.0, min_width*width_factor, (min_width*width_factor)/2.0); break;
			default:        fs.newGaussian(0.0, min_width*width_factor);
			}
			return fs;
		}
		double width = 0.0;
		double placement = 0.0;
		boolean first_run = true;
		double min = 0, max = 0;

		for (Double d : values) {
			if (first_run) {
				min = max = d;
			} else {
				if (d > max) max = d;
				else if (d < min) min = d;
			}
			placement += d;
			first_run = false;
		}
		placement /= values.size();
		width = ((max - min) / 2.0);
		if (width < min_width) width = min_width;
		width *= width_factor;

		FuzzySet fs = new FuzzySet();

		switch(_type){
		case GAUSS:     fs.newGaussian(placement, width); break;
		case TRIANGLE:  fs.newTriangle(placement, width); break;
		case TRAPEZIUM: fs.newTrapezium(placement, width, width/2.0); break;
		default:        fs.newGaussian(placement, width);
		}
		
		return fs;
	}
	
	public FuzzySet createFromFuzzyfiedValues(FuzzySet fuzzyfier, SNorm snorm){
		if (values.isEmpty()) {
			return fuzzyfier;
		}
		boolean first_run = true;

		FuzzySet result=null,tmp;
		
		for (Double d : values) {
			FuzzySet fs = new FuzzySet().assign(fuzzyfier).fuzzyfy(d);
			if (first_run) {
				result = fs;
			} else {
//				tmp = new FuzzySet(fs.size + result.size + 1);
				tmp = new FuzzySet();
				FuzzySet.processSetsWithNorm(tmp, result, fs, snorm);
				result = tmp;
			}
			first_run = false;
		}
		return result;
	}
}

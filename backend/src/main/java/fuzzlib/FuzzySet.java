package fuzzlib;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import fuzzlib.creators.OperationCreator;
import fuzzlib.negs.NegZadeh;
import fuzzlib.negs.Negation;
import fuzzlib.norms.Norm;
import fuzzlib.norms.NullNormForA;
import fuzzlib.norms.SNMax;
import fuzzlib.norms.SNorm;
import fuzzlib.norms.TNMin;
import fuzzlib.norms.TNorm;

/**
 * Class represents a fuzzy set. Based on piecewise-linear description of
 * membership function. Allows the user to create fuzzy sets, perform operations
 * like intersection, negation, process with T-Norms and S-Norms and other.
 * Contains methods for fast defining of gaussian, triangular and trapezoidal
 * membership functions. Piecewise-linear approach gives the possibility to
 * define membership functions of any shape with defined precision.
 * 
 * @author PrzemysĹ‚aw KudĹ‚acik
 */
public class FuzzySet {

	// default values
	public static final int DEFAULT_ARRAY_MAX_SIZE = 32;
	public static final int DEFAULT_ARRAY_EXTENSION = 64;

	// array of points - piecewise-linear function
	protected SPoint[] pts;

	// number of description points
	protected int size;

	// t-norm operation
	protected TNorm tnorm;
	// s-norm operation
	protected SNorm snorm;
	// negation operation
	protected Negation neg;

	// minimum x value of all description points
	protected double min_val;
	// maximum x value of all description points
	protected double max_val;

	// point with maximum membership level
	protected SPoint max_membership = new SPoint();

	// fuzzy set identifier
	protected String id;
	// fuzzy set description
	protected String des;

	// type of default defuzzyfication method
	protected short def_defuz;
	// default alpha parameter
	protected double def_alpha;
	// default minimum dx parameter
	protected double def_mindx;
	// format of floating point values in printing
	private DecimalFormat format;

	/**
	 * @return print format
	 */
	public DecimalFormat getPrintFormat() {
		return format;
	}

	/**
	 * Sets print format.
	 * 
	 * @param format format
	 */
	public void setPrintFormat(DecimalFormat format) {
		this.format = format;
	}

	/**
	 * Creates array of SPoint objects
	 * 
	 * @param size size of array
	 */
	private void _constructArray(int size) {
		pts = new SPoint[size];
		for (int i = 0; i < pts.length; i++) {
			pts[i] = new SPoint();
		}
	}

	/**
	 * Extends static array of SPoint objects by a given value
	 * 
	 * @param byValue
	 */
	private void extendSize(int byValue) {

		if (byValue <= 0)
			byValue = DEFAULT_ARRAY_EXTENSION;

		SPoint[] new_pts = new SPoint[pts.length + byValue];

		// move data to new array
		for (int i = 0; i < size; i++) {
			new_pts[i] = pts[i];
		}
		// create new objects in remaining positions
		for (int i = size; i < new_pts.length; i++) {
			new_pts[i] = new SPoint();
		}
		// set new array as actual
		pts = new_pts;

	}

	/**
	 * Constructor setting default values.
	 * 
	 * @param _id  fuzzy set identifier
	 * @param _des fuzzy set description
	 */
	public FuzzySet(String _id, String _des) {
		id = _id;
		des = _des;
		max_val = 0.0;
		max_membership = new SPoint(0, -1);
		// TODO: Co z tym min i max ? nie powinny byÄ‡ standardowo na 0 ??
		min_val = 1000000;
		max_val = -1000000;
		tnorm = new TNMin();
		snorm = new SNMax();
		neg = new NegZadeh();
		_constructArray(DEFAULT_ARRAY_MAX_SIZE);
		size = 0;
		def_defuz = DefuzMethod.DF_COG;
		def_alpha = 0.0;
		def_mindx = 0.000001;
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		format = new DecimalFormat("#.##", otherSymbols);
	}

	/**
	 * Default constructor setting default values.
	 */
	public FuzzySet() {
		this("", "");
	}

	/**
	 * Constructor creating an empty set with given maximum size of array of points.
	 * Other parameters default.
	 * 
	 * @param max_size initial size for array of points
	 * @param _id      fuzzy set identifier
	 * @param _des     fuzzy set description
	 */
	public FuzzySet(int max_size, String _id, String _des) {
		id = _id;
		des = _des;
		max_val = 0.0;
		max_membership.x = 0.0;
		max_membership.y = -1.0;
		// TODO: Co z tym min i max ? nie powinny byÄ‡ standardowo na 0 ??
		min_val = 1000000;
		max_val = -1000000;
		tnorm = new TNMin();
		snorm = new SNMax();
		neg = new NegZadeh();
		_constructArray(max_size);
		size = 0;
		def_defuz = DefuzMethod.DF_COG;
		def_alpha = 0.0;
		def_mindx = 0.000001;
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		format = new DecimalFormat("#.##", otherSymbols);
	}

	/**
	 * Constructor creating an empty set with given maximum size of array of points.
	 * Other parameters default.
	 * 
	 * @param max_size initial size for array of points
	 */
	public FuzzySet(int max_size) {
		this(max_size, "", "");
	}

	/**
	 * Copy constructor.
	 * 
	 * @param fs source fuzzy set
	 */
	public FuzzySet(FuzzySet fs) {
		id = new String(fs.id);
		des = new String(fs.des);

		size = fs.size;

		min_val = fs.min_val;
		max_val = fs.max_val;
		max_membership.assign(fs.max_membership);
		def_defuz = fs.def_defuz;
		def_alpha = fs.def_alpha;
		def_mindx = fs.def_mindx;

		_constructArray(fs.pts.length);

		for (int i = 0; i < size; i++) {
			pts[i].x = fs.pts[i].x;
			pts[i].y = fs.pts[i].y;
		}

		tnorm = null;
		snorm = null;
		neg = null;

		setTNorm(fs.tnorm.getType());
		setSNorm(fs.snorm.getType());
		setNegation(fs.neg.getType());
		setPrintFormat(fs.getPrintFormat());
	}

	/**
	 * Copies all fuzzy set data given by fs parameter (except name and
	 * description).
	 * 
	 * @param fs input FuzzySet
	 */
	public FuzzySet assign(FuzzySet fs) {
		size = fs.size;
		if (pts.length < fs.size)
			_constructArray(fs.size + DEFAULT_ARRAY_EXTENSION);
		min_val = fs.min_val;
		max_val = fs.max_val;
		max_membership.assign(fs.max_membership);
		def_defuz = fs.def_defuz;
		def_alpha = fs.def_alpha;
		def_mindx = fs.def_mindx;

		for (int i = 0; i < size; i++) {
			pts[i].x = fs.pts[i].x;
			pts[i].y = fs.pts[i].y;
		}

		tnorm = null;
		snorm = null;
		neg = null;

		setTNorm(fs.tnorm.getType());
		setSNorm(fs.snorm.getType());
		setNegation(fs.neg.getType());
		setPrintFormat(fs.getPrintFormat());

		return this;
	}

	/**
	 * Calculates intersection of this instance of FuzzySet and fs input parameter.
	 * 
	 * @param fs input fuzzy set
	 * @return result of intersection
	 */
	public FuzzySet intersection(FuzzySet fs) {
		if (size == 0)
			return this;
		if (fs.size == 0)
			return fs;

		int new_max_size = DEFAULT_ARRAY_MAX_SIZE;
		if (new_max_size <= size + fs.size)
			new_max_size = size + fs.size + DEFAULT_ARRAY_EXTENSION;

		FuzzySet tmp = new FuzzySet(new_max_size);

		tmp.setTNorm(tnorm.getType());
		tmp.setSNorm(snorm.getType());
		tmp.setNegation(neg.getType());

		_processSetsWithNorm(tmp, this, fs, tmp.tnorm);

		return tmp;
	}

	/**
	 * @return number of description points
	 */
	public final int getSize() {
		return size;
	};

	/**
	 * Retrieves n-th node of membership function description.
	 * 
	 * @param n index of point (starting with 0)
	 * @return n-th SPoint object
	 */
	public final SPoint getPoint(int n) {
		return pts[n];
	}

	/**
	 * Retrieves first node of membership function description.
	 * 
	 * @return first SPoint object
	 */
	public final SPoint getFirstPoint() {
		return pts[0];
	}

	/**
	 * Retrieves first node of membership function description.
	 * 
	 * @return first SPoint object
	 */
	public final SPoint getLastPoint() {
		return pts[size - 1];
	}

	/**
	 * Retrieves x value of n-th point of membership function description.
	 * 
	 * @param n index of point (starting with 0)
	 * @return x value of n-th SPoint object
	 */
	public final double getPointX(int n) {
		return pts[n].x;
	}

	/**
	 * Retrieves y value of n-th point of membership function description.
	 * 
	 * @param n index of point (starting with 0)
	 * @return y value of n-th SPoint object
	 */
	public final double getPointY(int n) {
		return pts[n].y;
	}

	/**
	 * Sets y value of n-th point of membership function description.
	 * 
	 * @param n     index of point (starting with 0)
	 * @param value new y value of n-th SPoint object
	 */
	public void setPointY(int n, double value) {
		pts[n].y = value;
	}

	/**
	 * Sets fuzzy set's identifier
	 * 
	 * @param _id identifier
	 */
	public void setId(String _id) {
		id = _id;
	};

	/**
	 * Sets fuzzy set's description
	 * 
	 * @param _des description
	 */
	public void setDescription(String _des) {
		des = _des;
	};

	/**
	 * @return fuzzy set's identifeir
	 */
	public String getId() {
		return id;
	};

	/**
	 * @return fuzzy set's description
	 */
	public String getDescription() {
		return des;
	};

	/**
	 * Creates a new default T-Norm operation of a given type for this fuzzy set.
	 * 
	 * @param type type of T-Norm
	 */
	public void setTNorm(short type) {
		tnorm = OperationCreator.newTNorm(type);
	}

	/**
	 * Sets a new default T-Norm operation for this fuzzy set.
	 * 
	 * @param tnorm new TNorm object
	 */
	public void setTNorm(TNorm tnorm) {
		this.tnorm = tnorm;
	}

	/**
	 * Creates a new default S-Norm operation of a given type for this fuzzy set.
	 * 
	 * @param type type of S-Norm
	 */
	public void setSNorm(short type) {
		snorm = OperationCreator.newSNorm(type);
	}

	/**
	 * Sets a new default S-Norm operation for this fuzzy set.
	 * 
	 * @param snorm new SNorm object
	 */
	public void setSNorm(SNorm snorm) {
		this.snorm = snorm;
	}

	/**
	 * Creates a new default negation operation of a given type for this fuzzy set.
	 * 
	 * @param type type of negation
	 */
	public void setNegation(short type) {
		neg = OperationCreator.newNegation(type);
	}

	/**
	 * Sets a new negation operation for this fuzzy set.
	 * 
	 * @param neg ne Negation object
	 */
	public void setNegation(Negation neg) {
		this.neg = neg;
	}

	/**
	 * Adds one point (x,y) to description of membership function. Points are
	 * appropriately sorted by x (ascending), so there's no need to add them in
	 * order. However, adding points in x order works faster.
	 * 
	 * @param x x value
	 * @param y y value
	 */
	public void addPoint(double x, double y) {
		int i = 0;

		// if array size is too small
		if (size + 1 > pts.length) {
			SPoint[] tmp = new SPoint[pts.length + DEFAULT_ARRAY_EXTENSION];
			for (int j = 0; j < tmp.length; j++) {
				tmp[j] = new SPoint();
			}

			while (i < size && x > pts[i].x) {
				tmp[i].assign(pts[i]);
				i++;
			}
			tmp[i].x = x;
			tmp[i].y = y;
			// if index behind last position (no point to compare to)
			// or new element does not exist in the set
			if ((i == size) || (x != pts[i].x)) { // then add point
				while (i < size) { // copy the rest of it's content after new
									// point
					tmp[i + 1].assign(pts[i]);
					i++;
				}
				size++;
				// apply new data
				pts = tmp;
			} else { // if new element already exists in the set then
				if (pts[i].y < y) { // change y to new value if membership is
									// grater
					pts[i].y = y;
				}
			}

		} else { // when array size is big enough
			while (i < size && x > pts[i].x)
				i++;
			int position = i;
			// if index behind last position (no point to compare to)
			// or new element does not exist in the set
			if ((i == size) || (x != pts[i].x)) { // then add point
				i = size - 1; // go to the last element of an array
				while (i >= position) { // and move the rest of it's content to
										// right by one place
					pts[i + 1].x = pts[i].x;
					pts[i + 1].y = pts[i].y;
					i--;
				}
				size++;
			}
			pts[position].x = x;
			pts[position].y = y;
		}
		// apply maximum membership level and left,right margins
		if (max_membership.y < y) {
			max_membership.x = x;
			max_membership.y = y;
		}
		if (size == 1) {
			max_membership.x = x;
			max_membership.y = y;
			min_val = x;
			max_val = x;
		} else {
			if (x < min_val)
				min_val = x;
			else if (x > max_val)
				max_val = x;
		}
	}

	/**
	 * Adds one SPoint to description of membership function. Points are
	 * appropriately sorted by x (ascending), so there's no need to add them in
	 * order. However, adding points in x order works faster.
	 * 
	 * @param p point of description to be added
	 */
	public void addPoint(SPoint p) {
		addPoint(p.x, p.y);
	}

	/**
	 * Method creates for an object a new description of membership function in
	 * Gaussian shape. Object remains the same - description changes.
	 * 
	 * @param center center of gaussian peek
	 * @param width  width of function's shape
	 * @param step   step between subsequent points of description
	 * @return reference to this object
	 */
	public FuzzySet newGaussianConstStep(double center, double width, double step) {
		ClearSet();

		if (step <= 0.0)
			step = 0.1;
		if (width <= 0.0)
			width = 1.0;

		double tmp, x = center - 4.0 * width;

		addPoint(x, 0.0);
		x += step;

		while (x < (center + 4.0 * width)) {
			tmp = x - center;
			tmp = tmp * tmp / (2.0 * width * width);
			tmp = Math.exp(-tmp);
			addPoint(x, tmp);
			x += step;
		}

		if (getMembershipD(center) < 0.995)
			addPoint(center, 1.0);
		addPoint(center + 4.0 * width, 0.0);

		return this;
	}

	/**
	 * Method creates for an object a new description of membership function in
	 * Gaussian shape. Object remains the same - description changes.
	 * 
	 * @param center center of gaussian peek
	 * @param width  width of function's shape. If the parameter value is <= 0 then
	 *               it is set to 0.01.
	 * @return reference to this object
	 */
	public FuzzySet newGaussian(double center, double width) {
		ClearSet();
		if (width <= 0.0)
			width = 0.01;

		double tmp, x = center - 4.0 * width;

		// 1 Point
		addPoint(x, 0.0);

		// 2,3,4,5,6 Point
		x += width;
		for (int i = 0; i < 5; i++) {
			tmp = x - center;
			tmp = tmp * tmp / (2.0 * width * width);
			tmp = Math.exp(-tmp);
			addPoint(x, tmp);
			x += width / 3.6;
		}
				
		// 7,8,9,10,11 Point
		x = center - width;
		for (int i = 0; i < 4; i++) {
			tmp = x - center;
			tmp = tmp * tmp / (2.0 * width * width);
			tmp = Math.exp(-tmp);
			addPoint(x, tmp);
			x += width / 4.0;
		}

		// 12 Point
		addPoint(center, 1.0);

		// Second half
		for (int i = 10; i >= 0; i--) {
			x = center + (center - pts[i].x);
			addPoint(x, pts[i].y);
		}

		return this;
	}

	/**
	 * Method creates for an object a new description of membership function based
	 * on Gaussian shape. In contrast to newGaussian, each "side of membership
	 * function" can have different width. Object remains the same - description
	 * changes.
	 * 
	 * @param center     center of gaussian peek
	 * @param leftWidth  left width of function's shape. If the parameter value is
	 *                   <= 0 then it is set to 0.01.
	 * @param rightWidth right width of function's shape. If the parameter value is
	 *                   <= 0 then it is set to 0.01.
	 * @return reference to this object
	 */
	public FuzzySet newGaussian(double center, double leftWidth, double rightWidth) {
		ClearSet();
		if (leftWidth <= 0.0)
			leftWidth = 0.01;
		if (rightWidth <= 0.0)
			rightWidth = 0.01;

		double tmp, x = center - 4.0 * leftWidth;

		// 1st Point
		addPoint(x, 0.0);

		// 2nd,3rd,4,5,6th Point
		x += leftWidth;
		for (int i = 0; i < 5; i++) {
			tmp = x - center;
			tmp = tmp * tmp / (2.0 * leftWidth * leftWidth);
			tmp = Math.exp(-tmp);
			addPoint(x, tmp);
			x += leftWidth / 3.6;
		}

		// 7,8,9,10,11th Point
		x = center - leftWidth;
		for (int i = 0; i < 4; i++) {
			tmp = x - center;
			tmp = tmp * tmp / (2.0 * leftWidth * leftWidth);
			tmp = Math.exp(-tmp);
			addPoint(x, tmp);
			x += leftWidth / 4.0;
		}

		// 12th Point
		addPoint(center, 1.0);

		// Second half

		// 13,14,15,16,17th Point
		x = center;
		for (int i = 0; i < 4; i++) {
			x += rightWidth / 4.0;
			tmp = x - center;
			tmp = tmp * tmp / (2.0 * rightWidth * rightWidth);
			tmp = Math.exp(-tmp);
			addPoint(x, tmp);
		}

		// 18,19,20th,21st,22nd Point
		x = center + 3.0 * rightWidth - 5.0 * rightWidth / 3.6;
		for (int i = 0; i < 5; i++) {
			tmp = x - center;
			tmp = tmp * tmp / (2.0 * rightWidth * rightWidth);
			tmp = Math.exp(-tmp);
			addPoint(x, tmp);
			x += rightWidth / 3.6;
		}

		// 23rd Point
		addPoint(center + 4.0 * rightWidth, 0.0);

		return this;
	}

	/**
	 * Method creates for an object a new description of membership function in
	 * triangular shape. Object remains the same - description changes.
	 * 
	 * @param center center of peek
	 * @param width  width of function's shape. If the parameter value is <= 0 then
	 *               it is set to 0.01.
	 * @return reference to this object
	 */
	public FuzzySet newTriangle(double center, double width) {
		ClearSet();
		if (width <= 0.0)
			width = 0.01;
		addPoint(center - width, 0.0);
		addPoint(center, 1.0);
		addPoint(center + width, 0.0);
		return this;
	}

	/**
	 * Method creates for an object a new description of membership function in
	 * triangular shape. In contrast to newTriangle, each "side of membership
	 * function" can have different width. Object remains the same - description
	 * changes.
	 * 
	 * @param center     center of peek
	 * @param leftWidth  left width of function's shape. If the parameter value is
	 *                   <= 0 then it is set to 0.01.
	 * @param rightWidth right width of function's shape. If the parameter value is
	 *                   <= 0 then it is set to 0.01.
	 * @return reference to this object
	 */
	public FuzzySet newTriangle(double center, double leftWidth, double rightWidth) {
		ClearSet();
		leftWidth *= 2;
		rightWidth *= 2;
		if (leftWidth <= 0.0)
			leftWidth = 0.01;
		if (rightWidth <= 0.0)
			rightWidth = 0.01;
		addPoint(center - leftWidth, 0.0);
		addPoint(center, 1.0);
		addPoint(center + rightWidth, 0.0);
		return this;
	}

	/**
	 * Method creates for an object a new description of membership function in
	 * trapezoidal shape. Object remains the same - description changes.
	 * 
	 * @param center     center of trapezium's core
	 * @param width      width of function's shape. If the parameter value is <= 0
	 *                   then it is set to 0.01.
	 * @param core_width width of trapezium's core. It has to be grater then width
	 *                   parameter. If this condition is not fulfilled then
	 *                   core_width is set to width/2.
	 * @return reference to this object
	 */
	public FuzzySet newTrapezium(double center, double width, double core_width) {
		ClearSet();
		if (width <= 0.0)
			width = 0.01;
		if (core_width >= width)
			core_width = width / 2.0;
		double dist = width - core_width;
		core_width /= 2.0;
		addPoint(center - (core_width + dist), 0.0);
		addPoint(center - core_width, 1.0);
		if (core_width > 0) {
			addPoint(center + core_width, 1.0);
		}
		addPoint(center + (core_width + dist), 0.0);
		return this;
	}

	/**
	 * Normalizes the fuzzy set. All membership (y) values of a set are divided by
	 * the maximum. If minimum < 0 then the values are properly scaled so minimum =
	 * 0. In other cases minimum values remain the same. After the process the
	 * membership values are in range [0,1].
	 */
	public void normalize() {
		double min, max;
		double range, offset;
		int i;
		if (size != 0) {
			min = pts[0].y;
			max = pts[0].y;
		} else
			return;

		// find minimum and maximum membership
		for (i = 0; i < size; i++) {
			if (pts[i].y < min)
				min = pts[i].y;
			else if (pts[i].y > max)
				max = pts[i].y;
		}

		// calculate parameters of normalization
		if (min < 0.0)
			offset = -min;
		else
			offset = min = 0.0;

		range = max - min;

		// normalize membership values
		for (i = 0; i < size; i++) {
			pts[i].y = (pts[i].y + offset) / range;
			if (max_membership.y < pts[i].y)
				max_membership.assign(pts[i]);
		}
	}

	/**
	 * Transforms data according to the average in defined region (softens the
	 * function). All membership (y) values of a fuzzy set are transformed according
	 * to the average.
	 * 
	 * @param numOfPoints points number to calculate average (greater value =>
	 *                    softer result). Minimum value = 1 (1 to the left and one
	 *                    to the right = 3 points for calculating average).
	 */
	public void softenByAverage(int numOfPoints) {
		if (size < 2)
			return;
		if (numOfPoints < 1) {
			numOfPoints = 1;
		}
		SPoint new_pts[] = new SPoint[pts.length];
		for (int j = 0; j < new_pts.length; j++) {
			new_pts[j] = new SPoint();
		}
		for (int i = 0; i < size; i++) {
			int start = i - numOfPoints;
			int stop = i + numOfPoints;
			if (start < 0)
				start = 0;
			if (stop >= size)
				stop = size - 1;
			double aver = 0;
			int num = 0;
			for (int j = start; j <= stop; j++) {
				aver += pts[j].y;
				num++;
			}
			new_pts[i].x = pts[i].x;
			new_pts[i].y = aver / num;
		}
		pts = new_pts;
	}

	/**
	 * Transforms data according to the average in defined region (softens the
	 * function). All membership (y) values of fuzzy set points are transformed
	 * according to the average values within the given range.
	 * 
	 * @param range range value rescribing citizenship at the left and right of each
	 *              point to calculate average (greater value => softer result). If
	 *              range <= 0 then range = 0.01.
	 */
	public void softenByAverage(double range) {
		if (size < 2)
			return;
		if (range <= 0) {
			range = 0.01;
		}
		SPoint new_pts[] = new SPoint[pts.length];
		for (int j = 0; j < new_pts.length; j++) {
			new_pts[j] = new SPoint();
		}
		for (int i = 0; i < size; i++) {
			double dbl_srt = pts[i].x - range;
			double dbl_stp = pts[i].x + range;
			IntWrapper idx = new IntWrapper();
			double aver = 0;
			int num = 2;
			aver = getMembership(dbl_srt, idx);
			int start = idx.value;
			aver += getMembership(dbl_stp, idx);
			int stop = idx.value;
			if (start < 0)
				start = 0;
			if (pts[start].x < dbl_srt)
				start++;
			for (int j = start; j <= stop; j++) {
				aver += pts[j].y;
				num++;
			}
			new_pts[i].x = pts[i].x;
			new_pts[i].y = aver / num;
		}
		pts = new_pts;
	}

	/**
	 * Divides fuzzy set's membership values. All membership (y) values of a fuzzy
	 * set are divided by given parameter.
	 * 
	 * @param divisor divisor of membership values
	 */
	public void divideMemberships(double divisor) {
		// divide membership values
		for (int i = 0; i < size; i++) {
			pts[i].y = pts[i].y / divisor;
		}
		max_membership.y = divisor;
	}

	/**
	 * Scales fuzzy set's membership values. All membership (y) values of a fuzzy
	 * set are multiplied by given parameter.
	 * 
	 * @param scale_factor scale factor of membership values
	 */
	public void scaleMembership(double scale_factor) {
		// scale membership values

		for (int i = 0; i < size; i++) {
			pts[i].y = pts[i].y * scale_factor;
		}
		max_membership.y *= scale_factor;
	}

	/**
	 * Scales fuzzy set's domain (x). All x values of points' description are
	 * multiplied by given parameter.
	 * 
	 * @param scale_factor scale factor of domain
	 */
	public void scaleDomain(double scale_factor) {
		for (int i = 0; i < size; i++) {
			pts[i].x = pts[i].x * scale_factor;
		}
		min_val *= scale_factor;
		max_val *= scale_factor;
	}

	/**
	 * Subtracts minimum value (the lowest y) from all points.
	 */
	public void SubtractMinimum() {
		if (size <= 0)
			return;
		double min;
		// initialize minimum
		min = pts[0].y;
		// find minimum
		for (int i = 0; i < size; i++) {
			if (pts[i].y < min)
				min = pts[i].y;
		}
		// subtract minimum
		for (int i = 0; i < size; i++) {
			pts[i].y -= min;
		}
	}

	/**
	 * "Cuts" to low values (all y values lower than given level are set to 0.0)
	 * 
	 * @param level
	 */
	public void cutMembership(double level) {
		// subtract value
		for (int i = 0; i < size; i++) {
			if (pts[i].y < level) {
				pts[i].y = 0.0;
			}
		}
	}

	/**
	 * Returns first point having y grater then given value
	 * 
	 * @param value value of y to search
	 * @return SPoint object fulfilling the search criteria or null in not found
	 */
	public SPoint findFirstHigherThan(double value) {
		if (size <= 0)
			return null;
		for (int i = 0; i < size; i++) {
			if (pts[i].y > value) {
				return pts[i];
			}
		}
		return null;
	}

	/**
	 * Returns last point having y grater then given value
	 * 
	 * @param value value of y to search
	 * @return SPoint object fulfilling the search criteria or null in not found
	 */
	public SPoint findLastHigherThan(double value) {
		if (size <= 0)
			return null;
		for (int i = size - 1; i >= 0; i--) {
			if (pts[i].y > value) {
				return pts[i];
			}
		}
		return null;
	}

	/**
	 * Deletes the left slope (all nodes going from left to first maximum-value node
	 * are set to maximum value)
	 */
	public void openLeftSlope() {
		if (size <= 1) // more than one node is needed for a slope
			return;
		for (int i = 0; i < size; i++) {
			if (pts[i].y < max_membership.y) {
				pts[i].y = max_membership.y;
			} else {
				return; // end if max achieved
			}
		}
	}

	/**
	 * Deletes the right slope (all nodes going from right to first maximum-value
	 * node are set to maximum value)
	 */
	public void openRightSlope() {
		if (size <= 1) // more than one node is needed for a slope
			return;
		for (int i = size - 1; i >= 0; i--) {
			if (pts[i].y < max_membership.y) {
				pts[i].y = max_membership.y;
			} else {
				return; // end if max achieved
			}
		}

	}

	/**
	 * Modify the set with a given truth function (truth functional modification)
	 * 
	 * @param truthfun
	 */
	public void ProcessSetWithTruthFunction(FuzzySet truthfun) {
		max_membership.x = 0.0;
		max_membership.y = -1.0;
		// scale membership values with truth function
		for (int i = 0; i < size; i++) {
			pts[i].y = truthfun.getMembership(pts[i].y);
			if (pts[i].y > max_membership.y) {
				max_membership.assign(pts[i]);
			}
		}
	}

	/**
	 * Modify the set with a given truth function (truth functional modification).
	 * This fuzzy set remains unchanged. The modified result is stored in conclusion
	 * set.
	 */
	public void ProcessSetWithTruthFunction(FuzzySet truthfun, FuzzySet conclusion) {
		FuzzySet con = conclusion;
		FuzzySet tf = truthfun;
		// allocate new memory if not enough
		if (pts.length < con.size) {
			pts = null;
			// ZMIANA
			// pts = new SPoint[con.pts.length];
			_constructArray(con.pts.length);
			// KONIEC ZMIANY
		}

		size = con.size;
		min_val = con.min_val;
		max_val = con.max_val;
		max_membership.x = 0.0;
		max_membership.y = -1.0;

		// scale conclusion membership values with truth function
		for (int i = 0; i < size; i++) {
			pts[i].x = con.pts[i].x;
			pts[i].y = tf.getMembership(con.pts[i].y);
			if (pts[i].y > max_membership.y) {
				max_membership.assign(pts[i]);
			}
		}
	}

	/**
	 * Increase set precision (add more points) Method increases precision of a
	 * fuzzy set description by adding new elements into each interval described
	 * with two points of original set. Method does not increase precision of flat
	 * intervals.
	 * 
	 * @param times             number of parts each interval is going to be divided
	 *                          into
	 * @param smallest_interval a restriction for new created intervals (won't be
	 *                          less then this param)
	 */
	public void IncreasePrecision(int times, double smallestXInterval) {
		if (size < 2)
			return;

		if (times < 2)
			return;
		else if (times > 100)
			times = 100;

		double width, step, x;
		int i, j, tmp_times, values_inserted = 0;

		FuzzySet tmp_set = new FuzzySet((times * size + DEFAULT_ARRAY_EXTENSION));
		// tmp_set = *this;

		for (i = 0; i < (size - 1); i++) {
			if (pts[i + 1].y != pts[i].y) { // leave flat intervals
				width = pts[i + 1].x - pts[i].x;
				tmp_times = times;
				step = width / tmp_times;
				while ((tmp_times > 0) && (step < smallestXInterval)) {
					tmp_times--;
				}
				for (j = 1; j < tmp_times; j++) {
					x = pts[i].x + step * j;
					tmp_set.addPoint(x, getMembership(x));
					values_inserted++;
				}
			}
		}

		// if (values_put > 0) (*this) = tmp_set;
	}

	/**
	 * Increase set precision (add more points) Method increases precision of a
	 * fuzzy set description by adding new nodes if the difference between y values
	 * of two nodes is greater than given maxdy parameter. The number of inserted
	 * nodes are calculated according to maxdy. The mindx parameter defines the
	 * minimum difference of x values between two subsequent nodes. This parameter
	 * is added as a constraint. No additional points are added if the dx difference
	 * would be smaller.
	 * 
	 * @param maxdy maximum difference of y values between nodes
	 * @param mindx constraint - the smallest difference between nodes in x domain
	 */
	public void IncreaseYPrecision(double maxdy, double mindx) {
		if (size < 2)
			return;
		if (maxdy <= 0.0)
			return;
		if (mindx <= 0.0)
			return;

		List<SPoint> tmpdes = new ArrayList<SPoint>();
		double dy, dx, divider, x;
		int i;

		// add first point
		tmpdes.add(new SPoint(pts[0].x, pts[0].y));

		// 1.Divide each segment to adequate number of parts (to make dy <=
		// mindy)
		for (i = 0; i < (size - 1); i++) {
			// calculate how many times segment should be divided
			dy = Math.abs(pts[i + 1].y - pts[i].y);
			if (dy > maxdy) {
				divider = Math.ceil(dy / maxdy);
				dx = (pts[i + 1].x - pts[i].x) / divider;
				if (dx < mindx)
					dx = mindx;
				x = pts[i].x;
				while ((x += dx) < pts[i + 1].x) {

					tmpdes.add(new SPoint(x, getMembership(x)));
				}
			}
			tmpdes.add(new SPoint(pts[i + 1].x, pts[i + 1].y));
		}

		// 2.Allocate memory if needed
		size = tmpdes.size();
		if (size > pts.length) {
			if (pts.length > 0)
				pts = null;
			pts = new SPoint[size + DEFAULT_ARRAY_EXTENSION];
		}

		// 3.Copy points
		ListIterator<SPoint> it = tmpdes.listIterator();
		i = 0;
		while (it.hasNext()) {
			pts[i] = it.next();
			i++;
		}

	}

	public void IncreaseXPrecision(double mindx) {
		if (size < 2)
			return;
		if (mindx <= 0.0)
			return;

		List<SPoint> tmpdes = new ArrayList<SPoint>();
		double dx, divider, x;
		int i;

		// add first point
		tmpdes.add(new SPoint(pts[0].x, pts[0].y));

		// 1.Divide each segment to adequate number of parts (to make dx <=
		// mindx)
		for (i = 0; i < (size - 1); i++) {
			// calculate how many times segment should be divided
			dx = Math.abs(pts[i + 1].x - pts[i].x);
			if (dx > mindx) {
				divider = Math.ceil(dx / mindx);
				dx /= divider;
				x = pts[i].x;
				while ((x += dx) < pts[i + 1].x) {
					tmpdes.add(new SPoint(x, getMembership(x)));
				}
			}
			tmpdes.add(new SPoint(pts[i + 1].x, pts[i + 1].y));
		}

		// 2.Allocate memory if needed
		size = tmpdes.size();
		if (size > pts.length) {
			if (pts.length > 0)
				pts = null;
			pts = new SPoint[size + DEFAULT_ARRAY_EXTENSION];
		}

		// 3.Copy points
		ListIterator<SPoint> it = tmpdes.listIterator();
		i = 0;
		while (it.hasNext()) {
			pts[i] = it.next();
			i++;
		}
	}

	/*
	 * Delete redundant nodes (when too many points are defined in flat areas). Such
	 * situations can occur in results of processing more fuzzy sets with TNorm or
	 * SNorm.
	 */
	public void PackFlatSections() {
		if (size < 3)
			return;
		int i, valid_pos = 1;

		double last_y = pts[0].y;

		int new_size = size;

		for (i = 1; i < size - 1; i++) { // czy tu nie sprawdzi poza zakresem
			if (last_y == pts[i].y && last_y == pts[i + 1].y) {
				new_size--;
			} else {
				valid_pos++;
			}
			last_y = pts[i].y;
			pts[valid_pos].assign(pts[i + 1]);
			// pts[valid_pos].x = pts[i+1].x;
			// pts[valid_pos].y = pts[i+1].y;
		}

		// copy the last point
		pts[valid_pos].assign(pts[i]);
		// pts[valid_pos].x = pts[i].x;
		// pts[valid_pos].y = pts[i].y;

		size = new_size;
	}

	public void toNegation() {
		if (size == 0)
			return;
		int i;

		// calculate negation of each point describing member function of a set
		for (i = 0; i < size; i++) {
			pts[i].y = neg.calc(pts[i].y);
		}
	}

	public FuzzySet fuzzyfy(double x) {
		if (size == 0)
			return this;
		int i;

		// move each point of a set by given x value
		for (i = 0; i < size; i++) {
			pts[i].x += x;
		}
		// DODANO 22.06
		max_membership.x += x;
		// KONIEC
		return this;

	}

	// Defuzzyfication of a set with configured method - COG is default
	public double DeFuzzyfy() {
		return DeFuzzyfyEx(def_defuz, def_alpha, def_mindx);
	}

	public double DeFuzzyfyEx(short method, double alpha, double min_dx) {
		switch (method) {
		case DefuzMethod.DF_COG:
			return _defuzzCenterOfGravity(min_dx);
		case DefuzMethod.DF_ICOG:
			return _defuzzIndexedCenterOfGravity(alpha, min_dx);
		case DefuzMethod.DF_MICOG:
			return _defuzzModifiedIndexedCenterOfGravity(alpha, min_dx);
		case DefuzMethod.DF_MAX:
			return _defuzzCenterOfMax();
		default:
			return _defuzzCenterOfGravity(min_dx);
		}
	}

	public double DeFuzzyfyEx() {
		return DeFuzzyfyEx(DefuzMethod.DF_COG, 0.0, 0.000001);
	}

	public double DeFuzzyfyEx(short method) {
		return DeFuzzyfyEx(method, 0.0, 0.000001);
	}

	public double DeFuzzyfyEx(short method, double alpha) {
		return DeFuzzyfyEx(method, alpha, 0.000001);
	}

	public void DeFuzzConf(short method, double alpha, double min_dx) {
		switch (method) {
		case DefuzMethod.DF_COG:
			def_defuz = DefuzMethod.DF_COG;
			break;
		case DefuzMethod.DF_ICOG:
			def_defuz = DefuzMethod.DF_ICOG;
			break;
		case DefuzMethod.DF_MICOG:
			def_defuz = DefuzMethod.DF_MICOG;
			break;
		case DefuzMethod.DF_MAX:
			def_defuz = DefuzMethod.DF_MAX;
			break;
		default:
			def_defuz = DefuzMethod.DF_COG;
			break;
		}
		if (alpha >= 0.0)
			def_alpha = alpha;
		if (min_dx >= 0.0)
			def_mindx = min_dx;
	}

	public void DeFuzzConf(short method) {
		DeFuzzConf(method, 0.0, 0.000001);
	}

	public void DeFuzzConf(short method, double alpha) {
		DeFuzzConf(method, alpha, 0.000001);
	}

	public void ClearSet() {
		size = 0;
		max_membership.x = 0.0;
		max_membership.y = -1.0;
		min_val = 10000;
		max_val = -10000;
	}

	// Calculates membership of given x value of a set
	public double getMembership(double x, IntWrapper left_idx) {
		if (size == 0)
			return 0.0;
		// if x is below the first set point then return it's membership
		if (x <= pts[0].x) {
			if (left_idx != null)
				left_idx.setValue(-1);
			return pts[0].y;
		}
		// if x is above the last set point then return it's membership
		if (x >= pts[size - 1].x) {
			if (left_idx != null)
				left_idx.setValue(size - 1);
			return pts[size - 1].y;
		}

		// otherwise calculate result according to set points

		// temporary variables
		int left; // position of a left point of set's description
		int right; // position of a left point of set's description
		int width; // number of points between left and right index
		int act; // actual index
		double result; // result to return

		// initialize
		left = 0;
		right = size - 1;

		// while indices do not point to one range do:
		while ((width = right - left) > 1) {
			act = left + width / 2; // calculate index of the middle
			if (x < pts[act].x)
				right = act; // choose left half
			else
				left = act; // choose right half
		}

		// calculate value of a linear function
		result = (x - pts[left].x) * (pts[right].y - pts[left].y);
		result = result / (pts[right].x - pts[left].x) + pts[left].y;
		if (left_idx != null)
			left_idx.setValue(left);
		return result;
	}

	public double getMembership(double x) {
		return getMembership(x, null);
	}

	// Calculates membership of given x value of a set
	// Method dedicated for constant division of description.
	public double getMembershipD(double x, Integer left_idx) {
		if (size == 0)
			return 0.0;
		// if x is below the first set point then return it's membership
		if (x <= pts[0].x) {
			if (left_idx != null)
				left_idx = -1;
			return pts[0].y;
		}
		// if x is above the last set point then return it's membership
		if (x >= pts[size - 1].x) {
			if (left_idx != null)
				left_idx = size - 1;
			return pts[size - 1].y;
		}

		// otherwise calculate result according to set points

		int act, last;

		// prediction of a nearest point position
		act = (int) ((size - 1) * (x - pts[0].x) / (pts[size - 1].x - pts[0].x));

		if (x > pts[act].x) { // go right to calculate result
			last = act;
			act++;
			while (x > pts[act].x) {
				last = act;
				act++;
			}
			// ((x-x1)*(y2-y1)/(x2-x1)) + y1
			if (left_idx != null)
				left_idx = last;
			return ((x - pts[last].x) * (pts[act].y - pts[last].y) / (pts[act].x - pts[last].x)) + pts[last].y;
		} else { // go left to calculate result
			last = act;
			act--;
			while (x < pts[act].x) {
				last = act;
				act--;
			}
			// ((x-x1)*(y2-y1)/(x2-x1)) + y1
			if (left_idx != null)
				left_idx = act;
			return ((x - pts[act].x) * (pts[last].y - pts[act].y) / (pts[last].x - pts[act].x)) + pts[act].y;
		}
	}

	public double getMembershipD(double x) {
		return getMembershipD(x, 0);
	}

	public double getMaximumMembership() {
		return max_membership.y;
	}

	public double getAverageMembership() {
		if (size == 0)
			return 0.0;
		double tmp = 0.0;

		for (int i = 0; i < size; i++) {
			tmp += pts[i].y;
		}

		return tmp / size;
	}

	// Returns point with maximal membership
	public final SPoint getMaxPoint() {
		return max_membership;
	}

	public void processSetAndMembershipWithNorm(double level, Norm norm) {
		if (size <= 0)
			return;

		double last_y, last_diff;
		SPoint cross = new SPoint();

		last_y = pts[0].y;
		last_diff = level - pts[0].y;
		max_membership.x = 0.0;
		max_membership.y = -1.0;

		for (int i = 0; i < size; i++) {
			// if level crossed the function add new point at cross position
			if ((last_diff * (level - pts[i].y)) < 0.0) {
				cross.x = ((level - last_y) * (pts[i].x - pts[i - 1].x));
				cross.x = (cross.x / (pts[i].y - last_y)) + pts[i - 1].x;
				cross.y = norm.calc(level, level);
				addPoint(cross);
				if (max_membership.y < cross.y)
					max_membership.assign(cross);
				i++; // ommit added point in further analysis
			}
			last_diff = level - pts[i].y;
			last_y = pts[i].y;
			pts[i].y = norm.calc(level, pts[i].y);
			if (max_membership.y < pts[i].y)
				max_membership.assign(pts[i]);
		}
	}

	// Public version of processing function
	// - asures size adjusting (if needed) of destination set
	public static void processSetsWithNorm(FuzzySet dest, FuzzySet aSet, FuzzySet bSet, Norm norm) {

		if ((aSet.size == 0) || (bSet.size == 0))
			return;

		// clear destination set
		dest.ClearSet();

		// allocate new memory if needed
		if (dest.pts.length <= aSet.size + bSet.size) {
			dest._constructArray(aSet.size + bSet.size + DEFAULT_ARRAY_EXTENSION);
		}

		// now process sets
		_processSetsWithNorm(dest, aSet, bSet, norm);
	}

	// Calculates the result of a norm operation between two fuzzy sets
	// Method assumes that the destination set has enough memory to store the
	// result
	private static void _processSetsWithNorm(FuzzySet dest, FuzzySet aSet, FuzzySet bSet, Norm norm) {
		SPoint Abeg = new SPoint();
		SPoint Aend = new SPoint();
		SPoint Bbeg = new SPoint();
		SPoint Bend = new SPoint();
		SPoint cross = new SPoint();
		double last_dif, act_dif;
		int posA, posB, out_counter;
		boolean A_NotDone, B_NotDone;

		A_NotDone = B_NotDone = true;

		// 0 - calculate the first point - beginning
		posA = posB = 0;
		out_counter = 0;
		Abeg.assign(aSet.pts[posA]);
		Bbeg.assign(bSet.pts[posB]);
		if (Abeg.x < Bbeg.x) {
			Bbeg.x = Abeg.x;
			if (posA < (aSet.size - 1))
				posA++; // increment pointer if possible
			else
				A_NotDone = false;
		} else {
			if (Abeg.x > Bbeg.x) {
				Abeg.x = Bbeg.x;
				if (posB < (bSet.size - 1))
					posB++; // increment pointer if possible
				else
					B_NotDone = false;
			} else { // if Abeg.x == Bbeg.x
				// incrementation needed to avoid input of two points with te
				// same values
				if (posA < (aSet.size - 1))
					posA++; // increment pointer if possible
				else
					A_NotDone = false;
				if (posB < (bSet.size - 1))
					posB++; // increment pointer if possible
				else
					B_NotDone = false;
			}
		}
		last_dif = act_dif = Abeg.y - Bbeg.y;
		// add first point to result set
		dest.pts[out_counter].x = Abeg.x;
		dest.pts[out_counter].y = norm.calc(Abeg.y, Bbeg.y);
		dest.max_membership.assign(dest.pts[out_counter]);
		out_counter++;

		// loop through all points in both sets
		while (A_NotDone || B_NotDone) {
			Aend.assign(aSet.pts[posA]);
			Bend.assign(bSet.pts[posB]);
			if (A_NotDone == false)
				Aend.x = Bend.x;
			if (B_NotDone == false)
				Bend.x = Aend.x + 1.0;
			if (Aend.x < Bend.x) {
				// wyznaczenie punktu dla drugiego zbioru
				Bend.y = ((Aend.x - Bbeg.x) * (Bend.y - Bbeg.y) / (Bend.x - Bbeg.x)) + Bbeg.y;
				Bend.x = Aend.x;
				if (posA < (aSet.size - 1))
					posA++; // increment pointer if possible
				else
					A_NotDone = false;
			} else {
				if (Aend.x > Bend.x) {
					// wyznaczenie punktu dla pierwszego zbioru
					Aend.y = ((Bend.x - Abeg.x) * (Aend.y - Abeg.y) / (Aend.x - Abeg.x)) + Abeg.y;
					Aend.x = Bend.x;
					if (posB < (bSet.size - 1))
						posB++; // increment pointer if possible
					else
						B_NotDone = false;
				} else { // if Aend.x == Bend.x
					// incrementation needed to avoid input of two points with
					// te same values
					if (posA < (aSet.size - 1))
						posA++; // increment pointer if possible
					else
						A_NotDone = false;
					if (posB < (bSet.size - 1))
						posB++; // increment pointer if possible
					else
						B_NotDone = false;
				}
			}
			act_dif = Aend.y - Bend.y;
			// jesli wczesniej nastapilo przeciecie funkcji przynaleznosci
			// zbiorow to
			// wylicz punkt przeciecia funkcji i dodaj go do zbioru wynikowego
			if ((act_dif * last_dif) < 0.0) {
				double o1, o2;
				o1 = Abeg.y - Bbeg.y;
				o2 = Bend.y - Aend.y;
				cross.x = ((Aend.x * o1) + (Abeg.x * o2)) / (o1 + o2);
				cross.y = ((Aend.y * Bbeg.y) - (Abeg.y * Bend.y)) / (-1.0 * (o1 + o2));
				dest.pts[out_counter].x = cross.x;
				dest.pts[out_counter].y = norm.calc(cross.y, cross.y);
				if (dest.max_membership.y < dest.pts[out_counter].y)
					dest.max_membership.assign(dest.pts[out_counter]);
				out_counter++;
				if (out_counter >= dest.pts.length) {
					dest.size = out_counter;
					dest.extendSize(DEFAULT_ARRAY_EXTENSION);
				}
			}
			// add next point to result set
			dest.pts[out_counter].x = Aend.x;
			dest.pts[out_counter].y = norm.calc(Aend.y, Bend.y);
			if (dest.max_membership.y < dest.pts[out_counter].y)
				dest.max_membership.assign(dest.pts[out_counter]);
			out_counter++;
			if (out_counter >= dest.pts.length) {
				dest.size = out_counter;
				dest.extendSize(DEFAULT_ARRAY_EXTENSION);
			}
			// last values become first for next step
			Abeg.assign(Aend);
			Bbeg.assign(Bend);
			last_dif = act_dif;
		}

		dest.size = out_counter;
		dest.min_val = aSet.min_val < bSet.min_val ? aSet.min_val : bSet.min_val;
		dest.max_val = aSet.max_val > bSet.max_val ? aSet.max_val : bSet.max_val;
	}

	// Defuzzyfication of a set with center of maximum values method (max)
	private double _defuzzCenterOfMax() {
		// description is not efficient to calculate center of max
		if (size < 2)
			return 0.0;
		int i, idxL, idxR;
		idxL = idxR = 0;
		for (i = 1; i < size; i++) {
			if (pts[idxL].y < pts[i].y) {
				idxL = idxR = i;
			} else if (pts[idxL].y == pts[i].y)
				idxR = i;
		}

		return pts[idxL].x + (pts[idxR].x - pts[idxL].x) / 2.0;
	}

	// Defuzzyfication of a set with center of gravity method
	private double _defuzzCenterOfGravity(double min_dx) {
		// description is not efficient to calculate center of graviy
		if (size < 2)
			return 0.0;
		// if first or last point describing a set don't have 0.0 membership
		// then center of weight doesn't exist => return center value of
		// availabe description
		// if ( (pts[0].y != 0.0) || (pts[size-1].y != 0.0) ) return
		// ((pts[size-1].x - pts[0].x)/2) + pts[0].x;

		int i;
		double dx, dx2, dx3, dy, dd;
		double x12, x13, x22, x23;
		double Sdydx3, Sdddx2, Sdydx2, Sdddx;

		Sdydx3 = Sdddx2 = Sdydx2 = Sdddx = 0.0;

		// loop for all points calculating y=integral(x*f(x))/integral(f(x))
		for (i = 0; i < (size - 1); i++) {
			dx = pts[i + 1].x - pts[i].x; // x2-x1
			if (dx > min_dx) { // to avoid division by 0 or really small values
				dy = pts[i + 1].y - pts[i].y; // y2-y1
				x12 = pts[i].x * pts[i].x; // x1^2
				x13 = x12 * pts[i].x; // x1^3
				x22 = pts[i + 1].x * pts[i + 1].x; // x2^2
				x23 = x22 * pts[i + 1].x; // x2^3
				dx2 = x22 - x12; // x2^2 - x1^2
				dx3 = x23 - x13; // x2^3 - x1^3
				dd = pts[i + 1].x * pts[i].y - pts[i].x * pts[i + 1].y; // x2*y1
																		// -
																		// x1*y2

				Sdydx3 += dy * dx3 / dx;
				Sdddx2 += dd * dx2 / dx;
				Sdydx2 += dy * dx2 / dx;
				Sdddx += dd * dx / dx;
			}
		}

		if ((Sdydx2 / 2.0 + Sdddx) != 0.0) // if division possible
			return (Sdydx3 / 3.0 + Sdddx2 / 2.0) / (Sdydx2 / 2.0 + Sdddx);
		else
			return ((pts[size - 1].x - pts[0].x) / 2.0 + pts[0].x); // if not
																	// return
																	// middle
																	// point
	}

	// Defuzzyfication of a set with indexed center of gravity method
	private double _defuzzIndexedCenterOfGravity(double alpha, double min_dx) {
		// description is not efficient to calculate center of gravity
		if (size < 2)
			return 0.0;

		int i;
		double dx, dx2, dx3, dy, dd;
		double x12, x13, x22, x23;
		double Sdydx3, Sdddx2, Sdydx2, Sdddx;
		double tmpx1, tmpx2, tmpy1, tmpy2;

		Sdydx3 = Sdddx2 = Sdydx2 = Sdddx = 0.0;

		// loop for all points calculating y=integral(x*f(x))/integral(f(x))
		for (i = 0; i < (size - 1); i++) {
			// prepare data according to alpha
			tmpx1 = pts[i].x;
			tmpx2 = pts[i + 1].x;
			tmpy1 = pts[i].y;
			tmpy2 = pts[i + 1].y;
			// if both start and end point are < alpha then no calculations
			if (tmpy1 >= alpha || tmpy2 >= alpha) {
				if (tmpy1 < alpha) { // => calcualte new start point
					tmpx1 = tmpx1 + (tmpx2 - tmpx1) * (alpha - tmpy1) / (tmpy2 - tmpy1);
					tmpy1 = alpha;
				}
				if (tmpy2 < alpha) { // => calculate new end point
					tmpx2 = tmpx2 - (tmpx2 - tmpx1) * (alpha - tmpy2) / (tmpy1 - tmpy2);
					tmpy2 = alpha;
				}
				// if both start and end point are >= alpha then no changes

				// calculate one step of regular COG method
				dx = tmpx2 - tmpx1; // x2-x1
				if (dx > min_dx) { // to avoid division by 0 or really small
									// values
					dy = tmpy2 - tmpy1; // y2-y1
					x12 = tmpx1 * tmpx1; // x1^2
					x13 = x12 * tmpx1; // x1^3
					x22 = tmpx2 * tmpx2; // x2^2
					x23 = x22 * tmpx2; // x2^3
					dx2 = x22 - x12; // x2^2 - x1^2
					dx3 = x23 - x13; // x2^3 - x1^3
					dd = tmpx2 * tmpy1 - tmpx1 * tmpy2; // x2*y1 - x1*y2

					Sdydx3 += dy * dx3 / dx;
					Sdddx2 += dd * dx2 / dx;
					Sdydx2 += dy * dx2 / dx;
					Sdddx += dd * dx / dx;
				}
			} // tmpy1 >= alpha || tmpy2 >= alpha
		}

		if ((Sdydx2 / 2.0 + Sdddx) != 0.0) // if division possible
			return (Sdydx3 / 3.0 + Sdddx2 / 2.0) / (Sdydx2 / 2.0 + Sdddx);
		else
			return ((pts[size - 1].x - pts[0].x) / 2.0 + pts[0].x); // if not
																	// return
																	// middle
																	// point
	}

	private double _defuzzModifiedIndexedCenterOfGravity(double alpha, double min_dx) {
		// description is not efficient to calculate center of graviy
		if (size < 2)
			return 0.0;

		int i;
		double dx, dx2, dx3, dy, dd;
		double x12, x13, x22, x23;
		double Sdydx3, Sdddx2, Sdydx2, Sdddx;
		double tmpx1, tmpx2, tmpy1, tmpy2;

		Sdydx3 = Sdddx2 = Sdydx2 = Sdddx = 0.0;

		// loop for all points calculating y=integral(x*f(x))/integral(f(x))
		for (i = 0; i < (size - 1); i++) {
			// prepare data according to alpha
			tmpx1 = pts[i].x;
			tmpx2 = pts[i + 1].x;
			tmpy1 = pts[i].y - alpha;
			tmpy2 = pts[i + 1].y - alpha;
			// if both start and end point are < 0.0 then no calculations
			if (tmpy1 >= 0.0 || tmpy2 >= 0.0) {
				if (tmpy1 < 0.0) { // => calcualte new start point
					tmpx1 = tmpx1 - (tmpx2 - tmpx1) * tmpy1 / (tmpy2 - tmpy1);
					tmpy1 = 0.0;
				}
				if (tmpy2 < 0.0) { // => calculate new end point
					tmpx2 = tmpx2 + (tmpx2 - tmpx1) * tmpy2 / (tmpy1 - tmpy2);
					tmpy2 = 0.0;
				}
				// if both start and end point are >= alpha then no changes

				// calculate one step of regular COG method
				dx = tmpx2 - tmpx1; // x2-x1
				if (dx > min_dx) { // to avoid division by 0 or really small
									// values
					dy = tmpy2 - tmpy1; // y2-y1
					x12 = tmpx1 * tmpx1; // x1^2
					x13 = x12 * tmpx1; // x1^3
					x22 = tmpx2 * tmpx2; // x2^2
					x23 = x22 * tmpx2; // x2^3
					dx2 = x22 - x12; // x2^2 - x1^2
					dx3 = x23 - x13; // x2^3 - x1^3
					dd = tmpx2 * tmpy1 - tmpx1 * tmpy2; // x2*y1 - x1*y2

					Sdydx3 += dy * dx3 / dx;
					Sdddx2 += dd * dx2 / dx;
					Sdydx2 += dy * dx2 / dx;
					Sdddx += dd * dx / dx;
				}
			} // tmpy1 >= alpha || tmpy2 >= alpha
		}

		if ((Sdydx2 / 2.0 + Sdddx) != 0.0) // if division possible
			return (Sdydx3 / 3.0 + Sdddx2 / 2.0) / (Sdydx2 / 2.0 + Sdddx);
		else
			return ((pts[size - 1].x - pts[0].x) / 2.0 + pts[0].x); // if not
																	// return
																	// middle
																	// point
	}

	public String toString() {
		String out = "";
		boolean first = true;

		for (int i = 0; i < size; i++) {
			if (!first) {
				out += ", ";
			}
			out += "[" + format.format(pts[i].x) + "]" + format.format(pts[i].y);
			first = false;
		}
		return out;
	}

	// ///for Zadeh reasoning system:

	// TODO: wyrzuciÄ‡ ponizsze metody z klasy FuzzySet (nie dotyczÄ…ce stricte zbioru
	// rozmytego)

	public static void inferenceZadeh(FuzzySet result, FuzzySet input, FuzzySet premise, FuzzySet conclusion, Norm impl,
			TNorm tnorm) {
		// ZMIANA
		result.assign(conclusion);
		// KONIEC
		FuzzySet tmpi = new FuzzySet();
		FuzzySet tmpc = new FuzzySet();
		FuzzySet level = new FuzzySet();
		level.ClearSet();
		level.addPoint(0.0, 0.0);

		result.max_membership.x = 0.0;
		result.max_membership.y = -1.0;

		for (int i = 0; i < result.size; i++) {
			// calculate implication
			level.pts[0].y = result.pts[i].y;
			processSetsWithNorm(tmpi, premise, level, impl);
			// cut implication with variable
			processSetsWithNorm(tmpc, tmpi, input, tnorm);
			// apply supremum
			result.pts[i].y = tmpc.getMaximumMembership();
			if (result.pts[i].y > result.max_membership.y) {
				result.max_membership.assign(result.pts[i]);
			}
		}
	}

	public static void inferenceZadehForSingleton(FuzzySet result, double input, FuzzySet premise, FuzzySet conclusion,
			Norm impl) {
		result = conclusion;

		FuzzySet tmpi = new FuzzySet();
		FuzzySet level = new FuzzySet();
		level.ClearSet();
		level.addPoint(0.0, 0.0);

		result.max_membership.x = 0.0;
		result.max_membership.y = -1.0;

		for (int i = 0; i < result.size; i++) {
			// calculate implication
			level.pts[0].y = result.pts[i].y;
			processSetsWithNorm(tmpi, premise, level, impl);
			// apply value for singleton
			result.pts[i].y = tmpi.getMembership(input);
			if (result.pts[i].y > result.max_membership.y) {
				result.max_membership.assign(result.pts[i]);
			}
		}
	}

	// /Inference for N input variables
	public static void inferenceZadeh(FuzzySet result, FuzzySet[] Fact, FuzzySet[] Premise, int number_of_premises,
			FuzzySet conclusion, Norm comp, Norm impl, TNorm tnorm) {
		if (number_of_premises <= 0)
			return;

		FullZadehData data = new FullZadehData(result, Fact, Premise, number_of_premises, conclusion, comp, impl,
				tnorm);

		result.max_membership.x = 0.0;
		result.max_membership.y = -1.0;

		for (int i = 0; i < result.getSize(); i++) {
			data.max = 0.0;
			data.idxCZ = i;
			_inferenceZadeh(data, number_of_premises - 1);
			// apply supremum
			result.setPointY(i, data.max);
			if (result.pts[i].y > result.max_membership.y) {
				result.max_membership.assign(result.pts[i]);

			}
		}
	}

	private static void _inferenceZadeh(FullZadehData data, int level) {
		if (level >= 0) {
			for (data.idx[level] = data.start[level]; data.idx[level] <= data.stop[level]; data.idx[level]++)
				_inferenceZadeh(data, level - 1);
		} else {
			// 1. calculate implication
			data.tmp = data.P[0].pts[data.idx[0]].y;
			// compound premise
			for (data.tmpi = 1; data.tmpi < data.numP; data.tmpi++) {
				data.tmp = data.comp.calc(data.tmp, data.P[data.tmpi].pts[data.idx[data.tmpi]].y);
			}
			// implication
			data.tmp = data.impl.calc(data.tmp, data.CZ.pts[data.idxCZ].y);

			// 2. cut implication with compound fact
			data.tmpcut = data.F[0].pts[data.idx[0]].y;
			for (data.tmpi = 1; data.tmpi < data.numP; data.tmpi++) {
				data.tmpcut = data.tnorm.calc(data.tmpcut, data.F[data.tmpi].pts[data.idx[data.tmpi]].y);
			}
			data.tmp = data.tnorm.calc(data.tmpcut, data.tmp);

			// 3. apply supremum
			if (data.tmp > data.max)
				data.max = data.tmp;
		}
	}

	// ///for Baldwin reasoning system:

	public static void calcIsTruthFunction(FuzzySet outFun, FuzzySet PSet, FuzzySet QSet, double minDY, double minDX) {
		_calcIsTruthFunction(outFun, PSet, QSet, minDY, minDX);
	}

	public static void calcIsTruthFunction(FuzzySet outFun, FuzzySet PSet, FuzzySet QSet) {
		calcIsTruthFunction(outFun, PSet, QSet, 0.01, 0.001);
	}

	private static void _calcIsTruthFunction(FuzzySet outFun, FuzzySet PSet, FuzzySet QSet, double minDY,
			double minDX) {
		outFun.ClearSet();

		SPoint Pp = new SPoint();
		SPoint Pk = new SPoint();
		SPoint Ps = new SPoint();
		SPoint tPs = new SPoint();
		double dx, dy, AprY, tdx, tAprY;
		Stack<SPoint> st = new Stack<SPoint>();
		// wstaw miejsca opisu punktow Q na P - aby nie pominac
		// charakterystycznych wartosci Q
		// NIEOPTYMALNIE - najlepiej zrobic to samemu i przy okazji pozbierac
		// rozne poziomy
		// przynaleznosci
		FuzzySet P = new FuzzySet();
		NullNormForA nn = new NullNormForA();
		processSetsWithNorm(P, PSet, QSet, nn);

		// 0. Wyznaczyc poczatkowe Pp i Pk
		// Pp.x = 0.0; Pk.x = 1.0;
		// obliczyc maks wartosc funkcji prawdy: Pp.y=_findMaxQforP(0.0,P,Q);
		// obliczyc maks wartosc funkcji prawdy: Pk.y =_findMaxQforP(1.0,P,Q);
		Pp.x = 0.0;
		Pp.y = _findMaxQforP(0.0, P, QSet);
		Pk.x = 1.0;
		Pk.y = _findMaxQforP(1.0, P, QSet);

		/*
		 * outFun.AddPoint(Pp.x,Pp.y); outFun.AddPoint(Pk.x,Pk.y);
		 */

		Ps.x = P.getMembership(QSet.getMaxPoint().x);
		Ps.y = QSet.getMaxPoint().y;

		outFun.addPoint(Pp.x, Pp.y);
		// dodaj punkt srodkowy tylko wtedy, gdy nie jest on poczatkiem lub
		// koncem
		if ((Ps.x > Pp.x) && (Ps.x < Pk.x)) {
			// dodaj punkt srodkowy i koncowy
			outFun.addPoint(Ps.x, Ps.y);
			outFun.addPoint(Pk.x, Pk.y);
			// prawa strona podzialu do przetworzenia pďż˝niej
			// ZMIANA 22.06
			st.push(new SPoint(Ps));
			st.push(new SPoint(Pk));
			// lewa strona podzialu do przetworzania teraz
			Pk.assign(Ps);
			// KONIEC
		} else {
			// dodaj tylko punkt koncowy
			outFun.addPoint(Pk.x, Pk.y);
		}
		/*
		 * //0a. wyznacz wszystkie rozne poziomy przynaleznosci opisane w P
		 * //NIEOPTYMALNE - mozna zbierac w szybszy sposob i potem posortowac //AddPoint
		 * juz jest wiec dlatego uzyty, ale kopiuje pamiec przy wstawianiu w srodek
		 * tablicy FuzzySet levels(P.size); int i; for(i=0; i<P.size; i++ ){ if (
		 * (P.pts[i].y!=0.0) && (P.pts[i].y!=1.0) ) //dla 1 i 0 juz dodane wiec tylko
		 * dla roznych levels.AddPoint( P.pts[i].y , 0.0 ); }
		 * 
		 * //0b. dla zebranych poziomow oblicz punkty funkcji prawdy for(i=0;
		 * i<levels.size; i++ ){ Ps.x = levels.pts[i].x; Ps.y = _findMaxQforP( Ps.x
		 * ,P,Q); outFun.AddPoint(Ps.x,Ps.y); }
		 * 
		 * //sproboj wpisac wiecej punktow zgodnie ze starym algorytmem //jesli funkcja
		 * zbyt malo dokladna
		 */
		boolean STOP = false;

		while (!STOP) {

			// 1. Wyznaczyc srodek Ps dla funkcji aproksymowanej oraz prostej
			// opisanej przez Pp i Pk
			// dx = (Pk.x - Pp.x)/2;
			dx = (Pk.x - Pp.x) / 2;
			// Ps.x = Pp.x + dx;
			Ps.x = Pp.x + dx;
			// aproksymowana: Ps.y = _findmaxQforP(Ps.x,P,Q);
			Ps.y = _findMaxQforP(Ps.x, P, QSet);
			// prosta aproksymujaca: AprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
			AprY = (Ps.x - Pp.x) * (Pk.y - Pp.y) / (Pk.x - Pp.x) + Pp.y;
			// dy = abs(AprY - Ps.y);
			dy = AprY - Ps.y;
			if (dy < 0.0)
				dy *= -1.0;

			// jeszcze sprawdzamy dla pewnosci lewo i prawo punktu srodkowego
			// - zdarza sie ze srodek jest zgodny aproksymacjďż˝ na dwoch
			// punktach,
			// ale funkcja jest np. schodkowa (i schodek przypada wlasnie w
			// srodku)

			if (dy < minDY && dx >= minDX) { // jesli dy dobre i dx w porzďż˝dku
												// to oblicz dy dla lewej
				tdx = (Ps.x - Pp.x) / 2;
				tPs.x = Pp.x + tdx;
				tPs.y = _findMaxQforP(tPs.x, P, QSet);
				// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
				tAprY = (tPs.x - Pp.x) * (Ps.y - Pp.y) / (Ps.x - Pp.x) + Pp.y;
				dy = tAprY - tPs.y;
				if (dy < 0.0)
					dy *= -1.0;
				if (dy < minDY) { // jesli lewa dobra to oblicz dy dla prawej -
									// nie tzreba juz sprawdzac dx
					tdx = (Pk.x - Ps.x) / 2;
					tPs.x = Ps.x + tdx;
					tPs.y = _findMaxQforP(tPs.x, P, QSet);
					// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
					tAprY = (tPs.x - Ps.x) * (Pk.y - Ps.y) / (Pk.x - Ps.x) + Ps.y;
					dy = tAprY - tPs.y;
					if (dy < 0.0)
						dy *= -1.0;
				}
			}

			// 2. Jesli dy < minDY lub dx < minDX to nie trzeba dodawac punktu
			if (dy < minDY || dx < minDX) {
				// jesli STOS pusty to STOP
				if (st.empty())
					STOP = true;
				else {
					// jesli nie to odczytaj Pp,Pk ze STOSu i GOTO (1.)
					Pk = st.pop();
					Pp = st.pop();
				}
			} else { // jesli trzeba dodac punkt
				// 3. Dodaj punkt Ps do opisu aproksymacji
				// (najlepiej zrealizowac dodawanie na liďż˝cie sortowanej - NA
				// RAZIE AddPoint bo juďż˝ jest)
				outFun.addPoint(Ps.x, Ps.y);
				// wrzuc Ps, Pk na STOS; - prawa strona podzialu do
				// przetworzenia pďż˝niej
				// ZMIANA 22.06
				st.push(new SPoint(Ps));
				st.push(new SPoint(Pk));
				// Pk = Ps i GOTO (1.) - lewa strona podzialu do przetworzania
				// teraz
				Pk.assign(Ps);
				// KONIEC
			}
		}

		// STOP: utworz wynikowy zbior rozmyty na podstawie obliczonych punktow
		// NA RAZIE WYKORZYSTUJďż˝ FUNKCJďż˝ AddPoint klasy FuzzySet - nieefektywne

		// Spakuj plaskie sekcje
		outFun.PackFlatSections();
	}

	private static double _findMaxQforP(double membership, FuzzySet PSet, FuzzySet QSet) {
		double last_diff = membership - PSet.pts[0].y;
		double diff, max, tmp;
		max = 0.0;
		// Przetworz wszystkie punkty zbioru P
		for (int i = 0; i < PSet.size; i++) {
			diff = membership - PSet.pts[i].y;
			if (diff == 0.0) { // jesli punkt P rowny membership sprawdz czy
								// maks
				tmp = QSet.getMembership(PSet.pts[i].x);
				// sprawdďż˝ czy maks
				if (tmp > max) {
					max = tmp;
					if (max == 1.0)
						return max;
				}
			} else {
				if ((diff * last_diff) < 0.0) { // jesli P przecina membership
												// to wyznacz x - w jakim
												// punkcie
					// x = (y-y1)(x2-x1)/(y2-y1) + x1
					tmp = (membership - PSet.pts[i - 1].y) * (PSet.pts[i].x - PSet.pts[i - 1].x)
							/ (PSet.pts[i].y - PSet.pts[i - 1].y) + PSet.pts[i - 1].x;
					tmp = QSet.getMembership(tmp);
					// sprawdďż˝ czy maks
					if (tmp > max) {
						max = tmp;
						if (max == 1.0)
							return max;
					}
				}
			}
			last_diff = diff;
			// jesli nic to kontynuuj przegladanie zbioru
		}

		// rozwiazanie zoptymalizowane : - do zaimplementowania kiedys (nie
		// wymaga kopiowania punktow z Q na P)

		// Przetworz wszystkie punkty zbioru P
		// (szukaj przeciecia z "membership" lub = membership - Punkt przeciecia
		// (Pprz))
		// - sprawdzaj dopoki punkt P = membership aby wyznaczyc caly plaski
		// obszar
		// - jesli plaski obszar P to wyznacz maks punkt Q w tym obszarze
		// - jesli obszar nie plaski to sprawdz q tylko w tym punkcie
		// - jesli przecieto to wyznacz przynaleznosc dla x przecietego punktu w
		// zbiorze Q:
		// - jesli wynik wiekszy niz aktualny maks to zapisz nowy maks

		return max;

	}

	public static void calcCompoundTruthFunction(FuzzySet outFun, FuzzySet tfFunA, short compound_type, FuzzySet trFunB,
			TNorm tnorm) {
		calcCompoundTruthFunction(outFun, tfFunA, compound_type, trFunB, tnorm, 0.01, 0.001);
	}

	public static void calcCompoundTruthFunction(FuzzySet outFun, FuzzySet trFunA, short composition_type,
			FuzzySet trFunB, TNorm tnorm, double minDY, double minDX) {

		if (trFunA.size < 2 || trFunB.size < 2) {
			return;
		}
		if (trFunA.pts[0].x != 0 || trFunB.pts[0].x != 0) {
			return;
		}
		if (trFunA.pts[trFunA.size - 1].x != 1 || trFunB.pts[trFunB.size - 1].x != 1) {
			return;
		}

		SPoint Pp = new SPoint();
		SPoint Pk = new SPoint();
		SPoint Ps = new SPoint();
		SPoint tPs = new SPoint();
		double dx, dy, AprY, tdx, tAprY;
		Stack<SPoint> st = new Stack<SPoint>();

		// double (findMaxAandBforMembership)(double, const FuzzySet &, const
		// FuzzySet &,
		// TNorm &, double , double );
		FuzzySet trA;
		FuzzySet trB;
		FindMaxMembership find;

		// !!! po poprawieniu drobnego bledu w funkcji
		// _findMaxAandBforMembershipPR
		// nizej opisany problem przestal sie pojawiac
		// Algorytm np dla zlaczenia iloczynem daje "poszarpanďż˝" funkcjďż˝
		// wyjsciowďż˝ gdy
		// zbior A osiaga maksimum dla wartosci bliskim zeru (falsz-falsz
		// absolutny)
		// oraz gdy zbior B "podchodzi" w stronďż˝ prawdy.
		// Jest to najprawdopodobniej spowodowane bardzo niewielkimi
		// szerokosciami przedzialow
		// wyznaczanych przy przejsciu ze zbioru A na B (np algorytm dla
		// zlaczenia iloczynem).
		// Zmieniajac w tej sytuacji zbiory miejscami problem ten nie powstaje
		// (przynajmniej
		// wizualnie). Dlatego aby uzyskac lepsze wyniki zamienia sie miejscami
		// zbiory A i B gdy zbior A osiaga maksimum w poblizu 0 (gdy < od 0.3)
		trA = trFunA;
		trB = trFunB;

		find = null;
		switch (composition_type) {
		case TNorm.TN_MINIMUM:
			find = new FindMaxAandBforMembershipMIN();
			break;
		case SNorm.SN_MAXIMUM:
			find = new FindMaxAandBforMembershipMAX();
			break;
		default:
			find = new FindMaxAandBforMembership(composition_type);
			// kiedyĹ› przy wykonywaniu zĹ‚ozenia dla PRoduct TNorm
			// funkcja byĹ‚a poszarpadna.
			// Problem przestal sie pojawiac gdy zamieniono funkcje
			// if ( itrA.getMaxPoint().x > 0.3)
			// trA = &itrB; trB = &itrA;
			// ALE to byĹ‚o w dedykowanej metodzie findMaxAandBforMembership obiektu dla
			// TNormy Product
			// - teraz jest jedna wspĂłlna metoda. Jakby problem wystÄ…piĹ‚ to nalezy znow
			// funkcje zamienic..

		}

		outFun.ClearSet();

		// 0. Wyznaczyc poczatkowe Pp, Pk oraz Ps, ktore wynika z przeciecia
		// singletonow (maks wartosci funkcji prawd)
		// Pp.x = 0.0; Pk.x = 1.0;
		// obliczyc maks wartosc funkcji prawdy:
		// Pp.y=_findMaxAandBforMembership(0.0,trA,trB,tnorm);
		// obliczyc maks wartosc funkcji prawdy: Pk.y
		// =_findMaxAandBforMembership(1.0,trA,trB,tnorm);
		Pp.x = 0.0;
		Pp.y = find.findMaxAandBforMembership(0.0, trA, trB, tnorm, minDY, minDX);
		Pk.x = 1.0;
		Pk.y = find.findMaxAandBforMembership(1.0, trA, trB, tnorm, minDY, minDX);

		Norm comp_fun = OperationCreator.newNorm(composition_type);
		Ps.x = comp_fun.calc(trA.getMaxPoint().x, trB.getMaxPoint().x);
		comp_fun = null;
		Ps.y = tnorm.calc(trA.getMaxPoint().y, trB.getMaxPoint().y);

		outFun.addPoint(Pp.x, Pp.y);
		// dodaj punkt srodkowy tylko wtedy, gdy nie jest on poczatkiem lub
		// koncem
		if ((Ps.x > Pp.x) && (Ps.x < Pk.x)) {
			// dodaj punkt srodkowy i koncowy
			outFun.addPoint(Ps.x, Ps.y);
			outFun.addPoint(Pk.x, Pk.y);
			// ZMIANA 22.06
			// prawa strona podzialu do przetworzenia pďż˝niej
			st.push(new SPoint(Ps));
			st.push(new SPoint(Pk));
			// lewa strona podzialu do przetworzania teraz
			Pk.assign(Ps);
			// KONIEC
		} else {
			// dodaj tylko punkt koncowy
			outFun.addPoint(Pk.x, Pk.y);
		}
		// Info: Punktu koncowego Pk nie mozna dodac tutaj, poniewaz w wypadku
		// dodania punktu srodkowego Ps, punkt Pk zostaje zmieniony

		boolean STOP = false;

		while (!STOP) {

			// 1. Wyznaczyc srodek Ps dla funkcji aproksymowanej oraz prostej
			// opisanej przez Pp i Pk
			// dx = (Pk.x - Pp.x)/2;
			dx = (Pk.x - Pp.x) / 2;
			// Ps.x = Pp.x + dx;
			Ps.x = Pp.x + dx;
			// aproksymowana: Ps.y =
			// _findMaxAandBforMembership(Ps.x,trA,trB,tnorm);
			Ps.y = find.findMaxAandBforMembership(Ps.x, trA, trB, tnorm, minDY, minDX);
			// aproksymujaca: AprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
			AprY = (Ps.x - Pp.x) * (Pk.y - Pp.y) / (Pk.x - Pp.x) + Pp.y;
			// dy = abs(AprY - Ps.y);
			dy = AprY - Ps.y;
			if (dy < 0.0)
				dy *= -1.0;

			// jeszcze sprawdzamy dla pewnosci lewo i prawo punktu srodkowego
			// - zdarza sie ze srodek jest zgodny aproksymacjďż˝ na dwoch
			// punktach,
			// ale funkcja jest np. schodkowa (i schodek przypada wlasnie w
			// srodku)

			if (dy < minDY && dx >= minDX) { // jesli dy dobre i dx w porzďż˝dku
												// to oblicz dy dla lewej
				tdx = (Ps.x - Pp.x) / 2;
				tPs.x = Pp.x + tdx;
				tPs.y = find.findMaxAandBforMembership(tPs.x, trA, trB, tnorm, minDY, minDX);
				// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
				tAprY = (tPs.x - Pp.x) * (Ps.y - Pp.y) / (Ps.x - Pp.x) + Pp.y;
				dy = tAprY - tPs.y;
				if (dy < 0.0)
					dy *= -1.0;
				if (dy < minDY) { // jesli lewa dobra to oblicz dy dla prawej -
									// nie tzreba juz sprawdzac dx
					tdx = (Pk.x - Ps.x) / 2;
					tPs.x = Ps.x + tdx;
					tPs.y = find.findMaxAandBforMembership(tPs.x, trA, trB, tnorm, minDY, minDX);
					// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
					tAprY = (tPs.x - Ps.x) * (Pk.y - Ps.y) / (Pk.x - Ps.x) + Ps.y;
					dy = tAprY - tPs.y;
					if (dy < 0.0)
						dy *= -1.0;
				}
			}

			// 2. Jesli dy < minDY lub dx < minDX to nie trzeba dodawac punktu
			if (dy < minDY || dx < minDX) {
				// jesli dla tych sprawdzen dalej ok to jedziemy dalej nie
				// dodajac punktow
				// jesli STOS pusty to STOP
				if (st.empty())
					STOP = true;
				else {
					// jesli nie to odczytaj Pp,Pk ze STOSu i GOTO (1.)
					Pk = st.peek();
					st.pop();
					Pp = st.peek();
					st.pop();
				}
			} else { // jesli trzeba dodac punkt
				// 3. Dodaj punkt Ps do opisu aproksymacji
				// (najlepiej zrealizowac dodawanie na liďż˝cie sortowanej - NA
				// RAZIE AddPoint bo juďż˝ jest)
				outFun.addPoint(Ps.x, Ps.y);
				// ZMIANA 22.06
				// wrzuc Ps, Pk na STOS; - prawa strona podzialu do
				// przetworzenia pďż˝niej
				st.push(new SPoint(Ps));
				st.push(new SPoint(Pk));
				// Pk = Ps i GOTO (1.) - lewa strona podzialu do przetworzania
				// teraz
				Pk.assign(Ps);
				// KONIEC
			}
		}

		// STOP: utworz wynikowy zbior rozmyty na podstawie obliczonych punktow
		// NA RAZIE WYKORZYSTUJďż˝ FUNKCJďż˝ AddPoint klasy FuzzySet - nieefektywne

		// Spakuj plaskie sekcje
		outFun.PackFlatSections();
	}

	public static void calcConcTruthFunction(FuzzySet outFun, FuzzySet trFun, Norm impl, TNorm tnorm, double minDY,
			double minDX) {
		_calcConcTruthFunction(outFun, trFun, impl, tnorm, minDY, minDX);
	}

	public static void calcConcTruthFunction(FuzzySet outFun, FuzzySet trFun, Norm impl, TNorm tnorm) {
		calcConcTruthFunction(outFun, trFun, impl, tnorm, 0.01, 0.001);
	}

	public static void calcConcTruthFunction(FuzzySet outFun, double singleton, Norm impl, double minDY, double minDX) {
		_calcConcTruthFunction(outFun, singleton, impl, minDY, minDX);
	}

	public static void calcConcTruthFunction(FuzzySet outFun, double singleton, Norm impl) {
		calcConcTruthFunction(outFun, singleton, impl, 0.01, 0.001);
	}

	public static void calcConcTruthFunction(FuzzySet outFun, double singleton, Norm impl, double minDY) {
		calcConcTruthFunction(outFun, singleton, impl, minDY, 0.001);
	}

	private static void _calcConcTruthFunction(FuzzySet outFun, FuzzySet trFun, Norm impl, TNorm tnorm, double minDY,
			double minDX) {
		outFun.ClearSet();

		SPoint Pp = new SPoint();
		SPoint Pk = new SPoint();
		SPoint Ps = new SPoint();
		SPoint tPs = new SPoint();
		double dx, dy, AprY, tdx, tAprY;
		Stack<SPoint> st = new Stack<SPoint>();
		// 0. Wyznaczyc poczatkowe Pp i Pk
		// Pp.x = 0.0; Pk.x = 1.0;
		// obliczyc maks wartosc funkcji prawdy:
		// Pp.y=_findMaxQforImpl(0.0,trFun,impl,tnorm);
		// obliczyc maks wartosc funkcji prawdy: Pk.y
		// =_findMaxQforImpl(1.0,trFun,impl,tnorm);
		Pp.x = 0.0;
		Pp.y = _findMaxQforImpl(0.0, trFun, impl, tnorm);
		Pk.x = 1.0;
		Pk.y = _findMaxQforImpl(1.0, trFun, impl, tnorm);
		outFun.addPoint(Pp.x, Pp.y);
		outFun.addPoint(Pk.x, Pk.y);

		boolean STOP = false;

		while (!STOP) {

			// 1. Wyznaczyc srodek Ps dla funkcji aproksymowanej oraz prostej
			// opisanej przez Pp i Pk
			// dx = (Pk.x - Pp.x)/2;
			dx = (Pk.x - Pp.x) / 2;
			// Ps.x = Pp.x + dx;
			Ps.x = Pp.x + dx;
			// aproksymowana: Ps.y = _findMaxQforImpl(Ps.x,trFun,impl,tnorm);
			Ps.y = _findMaxQforImpl(Ps.x, trFun, impl, tnorm);
			// aproksymujaca: AprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
			AprY = (Ps.x - Pp.x) * (Pk.y - Pp.y) / (Pk.x - Pp.x) + Pp.y;
			// dy = abs(AprY - Ps.y);
			dy = AprY - Ps.y;
			if (dy < 0.0)
				dy *= -1.0;

			// jeszcze sprawdzamy dla pewnosci lewo i prawo punktu srodkowego
			// - zdarza sie ze srodek jest zgodny aproksymacjďż˝ na dwoch
			// punktach,
			// ale funkcja jest np. schodkowa (i schodek przypada wlasnie w
			// srodku)

			if (dy < minDY && dx >= minDX) { // jesli dy dobre i dx w porzďż˝dku
												// to oblicz dy dla lewej
				tdx = (Ps.x - Pp.x) / 2;
				tPs.x = Pp.x + tdx;
				tPs.y = _findMaxQforImpl(tPs.x, trFun, impl, tnorm);
				// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
				tAprY = (tPs.x - Pp.x) * (Ps.y - Pp.y) / (Ps.x - Pp.x) + Pp.y;
				dy = tAprY - tPs.y;
				if (dy < 0.0)
					dy *= -1.0;
				if (dy < minDY) { // jesli lewa dobra to oblicz dy dla prawej -
									// nie tzreba juz sprawdzac dx
					tdx = (Pk.x - Ps.x) / 2;
					tPs.x = Ps.x + tdx;
					tPs.y = _findMaxQforImpl(tPs.x, trFun, impl, tnorm);
					// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
					tAprY = (tPs.x - Ps.x) * (Pk.y - Ps.y) / (Pk.x - Ps.x) + Ps.y;
					dy = tAprY - tPs.y;
					if (dy < 0.0)
						dy *= -1.0;
				}
			}

			// 2. Jesli dy < minDY lub dx < minDX to nie trzeba dodawac punktu
			if (dy < minDY || dx < minDX) {
				// jesli dla tych sprawdzen dalej ok to jedziemy dalej nie
				// dodajac punktow
				// jesli STOS pusty to STOP
				if (st.empty())
					STOP = true;
				else {
					// jesli nie to odczytaj Pp,Pk ze STOSu i GOTO (1.)
					Pk = st.peek();
					st.pop();
					Pp = st.peek();
					st.pop();
				}
			} else { // jesli trzeba dodac punkt
				// 3. Dodaj punkt Ps do opisu aproksymacji
				// (najlepiej zrealizowac dodawanie na liďż˝cie sortowanej - NA
				// RAZIE AddPoint bo juďż˝ jest)
				outFun.addPoint(Ps.x, Ps.y);
				// ZMIANA 22.06
				// wrzuc Ps, Pk na STOS; - prawa strona podzialu do
				// przetworzenia pďż˝niej
				st.push(new SPoint(Ps));
				st.push(new SPoint(Pk));
				// Pk = Ps i GOTO (1.) - lewa strona podzialu do przetworzania
				// teraz
				Pk.assign(Ps);
				// KONIEC
			}
		}

		// STOP: utworz wynikowy zbior rozmyty na podstawie obliczonych punktow
		// NA RAZIE WYKORZYSTUJďż˝ FUNKCJďż˝ AddPoint klasy FuzzySet - nieefektywne

		// Spakuj plaskie sekcje
		outFun.PackFlatSections();
	}

	private static void _calcConcTruthFunction(FuzzySet outFun, double singleton, Norm impl, double minDY,
			double minDX) {
		outFun.ClearSet();

		SPoint Pp = new SPoint();
		SPoint Pk = new SPoint();
		SPoint Ps = new SPoint();
		SPoint tPs = new SPoint();
		double dx, dy, AprY, tdx, tAprY;
		Stack<SPoint> st = new Stack<SPoint>();

		// 0. Wyznaczyc poczatkowe Pp i Pk
		// Pp.x = 0.0; Pk.x = 1.0;
		// obliczyc maks wartosc funkcji prawdy:
		// Pp.y=_findMaxQforImpl(0.0,trFun,impl,tnorm);
		// obliczyc maks wartosc funkcji prawdy: Pk.y
		// =_findMaxQforImpl(1.0,trFun,impl,tnorm);
		Pp.x = 0.0;
		Pp.y = impl.calc(singleton, 0.0);
		Pk.x = 1.0;
		Pk.y = impl.calc(singleton, 1.0);
		outFun.addPoint(Pp.x, Pp.y);
		outFun.addPoint(Pk.x, Pk.y);

		boolean STOP = false;

		while (!STOP) {

			// 1. Wyznaczyc srodek Ps dla funkcji aproksymowanej oraz prostej
			// opisanej przez Pp i Pk
			// dx = (Pk.x - Pp.x)/2;
			dx = (Pk.x - Pp.x) / 2;
			// Ps.x = Pp.x + dx;
			Ps.x = Pp.x + dx;
			// aproksymowana: Ps.y = _findMaxQforImpl(Ps.x,trFun,impl,tnorm);
			Ps.y = impl.calc(singleton, Ps.x);
			// aproksymujaca: AprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
			AprY = (Ps.x - Pp.x) * (Pk.y - Pp.y) / (Pk.x - Pp.x) + Pp.y;
			// dy = abs(AprY - Ps.y);
			dy = AprY - Ps.y;
			if (dy < 0.0)
				dy *= -1.0;

			// jeszcze sprawdzamy dla pewnosci lewo i prawo punktu srodkowego
			// - zdarza sie ze srodek jest zgodny aproksymacjďż˝ na dwoch
			// punktach,
			// ale funkcja jest np. schodkowa (i schodek przypada wlasnie w
			// srodku)

			if (dy < minDY && dx >= minDX) { // jesli dy dobre i dx w porzďż˝dku
												// to oblicz dy dla lewej
				tdx = (Ps.x - Pp.x) / 2;
				tPs.x = Pp.x + tdx;
				tPs.y = impl.calc(singleton, tPs.x);
				// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
				tAprY = (tPs.x - Pp.x) * (Ps.y - Pp.y) / (Ps.x - Pp.x) + Pp.y;
				dy = tAprY - tPs.y;
				if (dy < 0.0)
					dy *= -1.0;
				if (dy < minDY) { // jesli lewa dobra to oblicz dy dla prawej -
									// nie tzreba juz sprawdzac dx
					tdx = (Pk.x - Ps.x) / 2;
					tPs.x = Ps.x + tdx;
					tPs.y = impl.calc(singleton, tPs.x);
					// tAprY = ((x-x1)*(y2-y1)/(x2-x1)) + y1;
					tAprY = (tPs.x - Ps.x) * (Pk.y - Ps.y) / (Pk.x - Ps.x) + Ps.y;
					dy = tAprY - tPs.y;
					if (dy < 0.0)
						dy *= -1.0;
				}
			}

			// 2. Jesli dy < minDY lub dx < minDX to nie trzeba dodawac punktu
			if (dy < minDY || dx < minDX) {
				// jesli dla tych sprawdzen dalej ok to jedziemy dalej nie
				// dodajac punktow
				// jesli STOS pusty to STOP
				if (st.empty())
					STOP = true;
				else {
					// jesli nie to odczytaj Pp,Pk ze STOSu i GOTO (1.)
					Pk = st.peek();
					st.pop();
					Pp = st.peek();
					st.pop();
				}
			} else { // jesli trzeba dodac punkt
				// 3. Dodaj punkt Ps do opisu aproksymacji
				// (najlepiej zrealizowac dodawanie na liďż˝cie sortowanej - NA
				// RAZIE AddPoint bo juďż˝ jest)
				outFun.addPoint(Ps.x, Ps.y);
				// ZMIANA 22.06
				// wrzuc Ps, Pk na STOS; - prawa strona podzialu do
				// przetworzenia pďż˝niej
				st.push(new SPoint(Ps));
				st.push(new SPoint(Pk));
				// Pk = Ps i GOTO (1.) - lewa strona podzialu do przetworzania
				// teraz
				Pk.assign(Ps);
				// KONIEC
			}
		}

		// STOP: utworz wynikowy zbior rozmyty na podstawie obliczonych punktow
		// NA RAZIE WYKORZYSTUJďż˝ FUNKCJďż˝ AddPoint klasy FuzzySet - nieefektywne

		// Spakuj plaskie sekcje
		outFun.PackFlatSections();
	}

	// public double _findMaxQforImpl
	private double _findMaxIntersectionOfImplicationAndTruthFunction(double constant, FuzzySet tr_fun, Norm impl,
			TNorm intersection, double mindy, double mindx) {
		double tmp, last_y, y, max;
		int size = tr_fun.getSize();

		if (size < 1)
			return 0.0;

		max = intersection.calc(tr_fun.getPointY(0), impl.calc(tr_fun.getPointX(0), constant));
		last_y = impl.calc(tr_fun.getPointX(0), constant);
		// Przetworz wszystkie punkty zbioru tr_fun
		for (int i = 1; i < size; i++) {
			// wylicz wartosc implikacji dla x przetwarzanego punktu
			y = impl.calc(tr_fun.getPointX(size), constant);

			if ((last_y - y) * (tr_fun.getPointY(size - 1) - tr_fun.getPointY(size)) < 0) {
				tmp = impl.findMaxIntersection(tr_fun.getPoint(size - 1), tr_fun.getPoint(size), constant, intersection,
						max, mindy, mindx);
			} else {
				tmp = intersection.calc(tr_fun.getPointY(i), y);
			}
			if (tmp > max) {
				max = tmp;
				if (max == 1.0)
					return max;
			}

			last_y = y;
		}
		return max;
	}

	private static double _findMaxQforImpl(double n, FuzzySet QSet, Norm impl, TNorm tnorm) {
		double tmp, max = 0.0;

		// Operacja sup(TNorm) - projekcja na 'n' przeciďż˝cia funkcji prawdy z
		// implikacjďż˝

		// Przetworz wszystkie punkty zbioru Q
		for (int i = 0; i < QSet.size; i++) {
			// wylicz wartosc implikacji dla x przetwarzanego punktu
			tmp = impl.calc(QSet.pts[i].x, n);
			// wylicz przeciecie implikacji i funkcji prawdy w tym punkcie
			// (tnorma)
			tmp = tnorm.calc(tmp, QSet.pts[i].y);
			// sprawdz czy wartosc maksymalna (krok supremum)
			if (tmp > max) {
				max = tmp;
				if (max == 1.0)
					return max;
			}
		}

		return max;
	}

	public double getMax_val() {
		return max_val;
	}

	public SPoint getMax_membership() {
		return max_membership;
	}

	public double getMin_val() {
		return min_val;
	}

}

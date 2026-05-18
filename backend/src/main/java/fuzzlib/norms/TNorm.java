package fuzzlib.norms;

public abstract class TNorm extends Norm {
	private static final short MIN = 0;
	private static final short MAX = 99;

	public static final short TN_MINIMUM = 0;
	public static final short TN_PRODUCT = 1;
	public static final short TN_LUKASIEWICZ = 2;
	public static final short TN_DRASTIC = 3;
	public static final short TN_NILPOTENT = 4;
	public static final short TN_HAMACHER = 5;
	public static final short TN_EINSTEIN = 6;

	public static boolean isTNorm(short norm_type) {
		if ((norm_type >= MIN) && (norm_type <= MAX))
			return true;
		else
			return false;
	};

	public static boolean isTNorm(Norm n) {
		if ((n.getType() >= MIN) && (n.getType() <= MAX))
			return true;
		else
			return false;
	};
}

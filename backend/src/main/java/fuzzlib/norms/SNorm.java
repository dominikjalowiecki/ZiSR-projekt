package fuzzlib.norms;

//Abstract class of S-Norm (T-Conorm)
public abstract class SNorm extends Norm {

	private static final short MIN = 100;
	private static final short MAX = 199;
	public static final short SN_MAXIMUM = 100;
	public static final short SN_PROBABSUM = 101;
	public static final short SN_LUKASIEWICZ = 102;
	public static final short SN_DRASTIC = 103;
	public static final short SN_NILPOTENT = 104;
	public static final short SN_EINSTEIN = 105;

	public static boolean isSNorm(short norm_type) {
		if ((norm_type >= MIN) && (norm_type <= MAX))
			return true;
		else
			return false;
	}

	public static boolean isSNorm(Norm n) {
		if ((n.getType() >= MIN) && (n.getType() <= MAX))
			return true;
		else
			return false;
	}
}

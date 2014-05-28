package bloodandmithril.util;

/**
 * Utility class for using comparisons
 *
 * @author Matt
 */
public class ComparisonUtil<T> {

	/** Instance being compared */
	private T t;

	/**
	 * Private constructor
	 */
	private ComparisonUtil(T t) {
		this.t = t;
	}

	/**
	 * Constructor
	 */
	public static <T> ComparisonUtil<T> obj(T t) {
		return new ComparisonUtil<T>(t);
	}

	@SafeVarargs
	public final boolean oneOf(T... ts) {
		for (T t : ts) {
			if (t.equals(this.t)) {
				return true;
			}
		}
		return false;
	}
}
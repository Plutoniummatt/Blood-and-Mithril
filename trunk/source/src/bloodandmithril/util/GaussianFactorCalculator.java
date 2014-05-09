package bloodandmithril.util;

/**
 * Calculates gaussian weights
 *
 * @author Matt
 */
public class GaussianFactorCalculator {

	/**
	 * Main calculation routine
	 */
	public static void main(String[] args) {
		double steps = 24d;

		for (double d = 0.0d; d <= 2.0d; d += 2.0d / steps) {
			System.out.println(Math.exp(-Math.pow(d, 2)));
		}
	}
}
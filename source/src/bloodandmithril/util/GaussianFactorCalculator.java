package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * Calculates gaussian weights
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GaussianFactorCalculator {

	/**
	 * Main calculation routine
	 */
	public static void main(String[] args) {
		double steps = 8d;

		for (double d = 0.0d; d <= 2.0d; d += 2.0d / steps) {
			System.out.println(Math.exp(-Math.pow(d, 2)));
		}
	}
}
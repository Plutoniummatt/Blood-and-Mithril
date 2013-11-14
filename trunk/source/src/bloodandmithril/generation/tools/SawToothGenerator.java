package bloodandmithril.generation.tools;

import java.io.Serializable;
import java.util.HashMap;

import bloodandmithril.util.Util;



/**
 * Generates sawtooth like 1D function
 *
 * @author Sam
 */
public class SawToothGenerator implements Serializable {
	private static final long serialVersionUID = 8666913840394916203L;
	
	private final int period;
	private int counter;

	private int minSurface;
	private int maxSurface;
	private int gradientDampening;
	private final int panicGradientDampening;

	private int slopeGradient = 0;
	private int slopeGradientAccelerationDirection = 1;

	/**
	 * @param minSurface - the maximum height the surface can be, takes world tile coord.
	 * @param maxSurface - the minimum height the surface can be, takes world tile coord.
	 * @param gradientDampening - bigger numbers mean less change of increasing or decreasing height with each tile.
	 * @param panicGradientDampening - the gradientDampening to use when the surface is out of bounds.
	 * @param period - the maximum number of tiles before hills must switch direction (eg. start going down instead of up)
	 */
	public SawToothGenerator(int minSurface, int maxSurface, int gradientDampening, int panicGradientDampening, int period) {
		this.minSurface = minSurface;
		this.maxSurface = maxSurface;
		this.gradientDampening = gradientDampening;
		this.panicGradientDampening = panicGradientDampening;
		this.period = period;

		counter = Util.getRandom().nextInt(period);
	}


	/**
	 * @param gradientDampening - bigger numbers mean less change of increasing or decreasing height with each tile.
	 */
	public void setGradientDampening(int gradientDampening) {
		this.gradientDampening = gradientDampening;
	}


	/**
	 * @param maxSurface - the maximum height the surface can be, takes world tile coord.
	 */
	public void setMaxSurface(int maxSurface) {
		this.maxSurface = maxSurface;
	}


	/**
	 * @param minSurface - the minimum height the surface can be, takes world tile coord.
	 */
	public void setMinSurface(int minSurface) {
		this.minSurface = minSurface;
	}


	/**
	 * @param topography - the topography we're generating on
	 * @param x - the collumn you need the surface height for
	 * @param generatingToRight - true if generating to the right, false if generating to the left
	 */
	public void generateSurfaceHeight(int x, boolean generatingToRight, HashMap<Integer, Integer> surfaceHeight) {

		if (surfaceHeight.get(x) != null) {
			throw new RuntimeException("Overwriting existing surface heights is not allowed.");
		}

		if (counter == 0) {
			slopeGradientAccelerationDirection = -slopeGradientAccelerationDirection;
			counter = Util.getRandom().nextInt(period);
		} else {
			counter--;
		}
		int gradientDampeningToUse;
		if (generatingToRight) {
			if (surfaceHeight.get(x - 1) != null) {
				gradientDampeningToUse = surfaceHeight.get(x - 1) > maxSurface || surfaceHeight.get(x - 1) < minSurface ? panicGradientDampening : gradientDampening;
			} else {
				gradientDampeningToUse = gradientDampening;
			}
		} else {
			if (surfaceHeight.get(x + 1) != null) {
				gradientDampeningToUse = surfaceHeight.get(x + 1) > maxSurface || surfaceHeight.get(x + 1) < minSurface ? panicGradientDampening : gradientDampening;
			} else {
				gradientDampeningToUse = gradientDampening;
			}
		}

		int dampening = Util.getRandom().nextInt(2);
		for (int i = 0; i < gradientDampeningToUse; i++) {
			dampening = dampening * Util.getRandom().nextInt(2);
		}

		slopeGradient = slopeGradientAccelerationDirection * dampening;

		if (generatingToRight && surfaceHeight.get(x - 1) != null) {
			if (surfaceHeight.get(x - 1) + slopeGradient > maxSurface)
				slopeGradientAccelerationDirection = -1;
			if (surfaceHeight.get(x - 1) + slopeGradient < minSurface)
				slopeGradientAccelerationDirection = 1;
			surfaceHeight.put(x, surfaceHeight.get(x - 1) + slopeGradient);
		} else if (!generatingToRight && surfaceHeight.get(x + 1) != null) {
			if (surfaceHeight.get(x + 1) + slopeGradient > maxSurface)
				slopeGradientAccelerationDirection = -1;
			if (surfaceHeight.get(x + 1) + slopeGradient < minSurface)
				slopeGradientAccelerationDirection = 1;
			surfaceHeight.put(x, surfaceHeight.get(x + 1) + slopeGradient);
		} else {
			surfaceHeight.put(x, minSurface + Util.getRandom().nextInt(maxSurface - minSurface));
		}
	}
}

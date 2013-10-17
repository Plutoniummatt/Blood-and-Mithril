package spritestar.world.generation.tools;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.pow;

import java.io.Serializable;
import java.util.Random;

/**
 * One dimensional perlin noise generator
 *
 * @author Sam
 */
public class PerlinNoiseGenerator1D implements Serializable {
	private static final long serialVersionUID = 4830716048266970103L;
	
	/**
	 * How much the perlin noise is stretched by, bigger numbers mean tiles
	 * change less sharply.
	 */
	private final int stretch;
	private final int seed;


	/**
	 * Constructor
	 */
	public PerlinNoiseGenerator1D(int stretch, int seed) {
		this.stretch = stretch;
		this.seed = seed;
	}


	/**
	 * Generates random noise at x
	 *
	 * @param x
	 * @return - a random float between 0 and 1
	 */
	private float noise(float x) {
		Random random = new Random();
		random.setSeed((long)(654321 * x + seed));
		return random.nextFloat();
	}


	/**
	 * Smoothes the noise
	 *
	 * @return - the smoothed noise
	 */
	private float smoothNoise(float x, int factor) {
		float smoothNoise = 0;
		for (int offset = -factor; offset <= factor; offset++) {
			smoothNoise =+ (factor - abs(offset) + 1) * noise(x + offset) / (float) pow(factor + 1, 2);
		}
		return smoothNoise;
	}


	/**
	 * Cubic Interpolation
	 *
	 * @return - the interpolated value
	 */
	private float cubicInterpolate(float v0, float v1, float v2, float v3, float x) {
		float P = v3 - v2 - (v0 - v1);
		float Q = v0 - v1 - P;
		float R = v2 - v0;
		float S = v1;

		return P * (float) pow(x, 3) + Q * (float) pow(x, 2) + R * x + S;
	}


	/**
	 * Smooth and interpolate the noise
	 *
	 * @param x - world tile x-coord
	 * @return the smooth, interpolated noise
	 */
	private float interpolatedNoise(float x, int factor) {
		int tempx = (int) floor(x);
		float remX = x - tempx;

		float v0 = smoothNoise(tempx - 1, factor);
		float v1 = smoothNoise(tempx, factor);
		float v2 = smoothNoise(tempx + 1, factor);
		float v3 = smoothNoise(tempx + 2, factor);

		return cubicInterpolate(v0, v1, v2, v3, remX);
	}


	/**
	 * Generates Perlin noise
	 *
	 * @param x - world tile x-coord
	 * @return The generated value
	 */
	public float generate(int tileX, int factor) {
		float x = (float) tileX / stretch;

		return interpolatedNoise(x, factor);
	}
}
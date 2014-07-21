package bloodandmithril.generation.tools;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.pow;

import java.io.Serializable;
import java.util.Random;

import bloodandmithril.core.Copyright;

/**
 * One dimensional Perlin noise generator
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class PerlinNoiseGenerator1D implements Serializable {
	private static final long serialVersionUID = 4830716048266970103L;

	/**
	 * How much the perlin noise is stretched by, bigger numbers mean tiles
	 * change less sharply.
	 */
	private final int stretch;
	private final int seed;

	/** The Number of times Perlin noise is iterated, it adds smaller noise to bigger noise */
	private final int numberOfOctaves;

	/**
	 * Persistence determines how much the strength fades by through consecutive iterations
	 * Smaller numbers means smoother curves
	 */
	private final float perlinIterationPersistence;

	/**
	 * @param stretch - how much the noise is stretched by. More stretch means more values are interpolated.
	 * @param seed - the seed of the noise.
	 * @param perlinIterationPersistence - How much effect subsequent iterations of noise have.
	 * @param numberOfOctaves - the number of iterations of perlin noise put on top of itself.
	 */
	public PerlinNoiseGenerator1D(int stretch, int seed, int octaves, float perlinIterationPersistence) {
		this.stretch = stretch;
		this.seed = seed;
		this.numberOfOctaves = octaves;
		this.perlinIterationPersistence = perlinIterationPersistence;
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
	 * Smoothes the noise by taking a weighted average with adjacent points.
	 * The larger the factor, the more adjacent points it looks at to get the average of.
	 *
	 * @return - the smoothed noise
	 */
	private float smoothNoise(float x, int smoothingFactor) {
		float smoothNoise = 0;
		for (int offset = -smoothingFactor; offset <= smoothingFactor; offset++) {
			smoothNoise += (smoothingFactor - abs(offset) + 1) * noise(x + offset) / (float) pow(smoothingFactor + 1, 2);
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
	private float interpolatedNoise(float x, int smoothingFactor) {
		int tempx = (int) floor(x);
		float remX = x - tempx;

		float v0 = smoothNoise(tempx - 1, smoothingFactor);
		float v1 = smoothNoise(tempx, smoothingFactor);
		float v2 = smoothNoise(tempx + 1, smoothingFactor);
		float v3 = smoothNoise(tempx + 2, smoothingFactor);

		return cubicInterpolate(v0, v1, v2, v3, remX);
	}


	/**
	 * Generates Perlin noise by:
	 * <ol>
	 * <li>Generating a value between 0 and 1 depending on the position given.</li>
	 * <li>Smoothing the noise by taking a weighted average between this point and adjacent points.</li>
	 * <li>Interpolating the noise using a cubic equation to join the two generated points.</li>
	 * <li>Iterating the noise depending on the perlinIterationPersistence and numberOfOctaves.</li>
	 * </ol>
	 * @param x - world tile x-coord
	 * @param smoothingFactor - smoothing applied to this area of the noise. See {@link #smoothNoise(float, int) smoothNoise}
	 * @return The generated value
	 */
	public float generate(int tileX, int smoothingFactor) {
		float x = (float) tileX / stretch;

	    float total = 0;
	    float maxTotal = 0;
	    for (int i = 0; i < numberOfOctaves; i++) {
	    	int frequency = (int)pow(2, i);
	    	float amplitude = (float)pow(perlinIterationPersistence, i);
	    	maxTotal = maxTotal + amplitude;
	    	total = total + interpolatedNoise(x * frequency, smoothingFactor) * amplitude;
	    }
		return total/maxTotal;
	}
}
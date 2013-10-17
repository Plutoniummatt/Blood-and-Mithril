package spritestar.world.generation.tools;

import static java.lang.Math.floor;
import static java.lang.Math.pow;

import java.util.Random;

import spritestar.persistence.ParameterPersistenceService;

/**
 * Two dimensional Perlin noise generator
 * 
 * @author Sam, Matt
 */
public class PerlinNoiseGenerator2D {

	/**
	 * How much the perlin noise is stretched by, bigger numbers mean tiles
	 * change less sharply.
	 */
	private int stretch;

	/**
	 * Constructor
	 */
	public PerlinNoiseGenerator2D(int stretch) {
		this.stretch = stretch;
	}

	
	/**
	 * Generates random noise at x, y
	 * 
	 * @param x
	 * @param y
	 * @return - a random float between 0 and 1
	 */
	private float noise(float x, float y) {
		Random random = new Random();
		random.setSeed((long) (x * 123456 - y * 654321 + ParameterPersistenceService.getParameters().getSeed()));
		return random.nextFloat();
	}
	

	/**
	 * Smoothes the noise
	 * 
	 * @return - the smoothed noise
	 */
	private float smoothNoise(float x, float y) {
		// averages the float against the surrounding floats
		float corners = (
			noise(x - 1, y - 1) + 
			noise(x + 1, y - 1)	+ 
			noise(x - 1, y + 1) + 
			noise(x + 1, y + 1)) 
			/ 16;

		float sides = (
			noise(x - 1, y) + 
			noise(x + 1, y) + 
			noise(x, y - 1) + 
			noise(x, y + 1)) 
			/ 8;

		float center = noise(x, y) / 4;
		return corners + sides + center;
	}

	
	/**
	 * Cubic Interpolation
	 * 
	 * @return - the interpolated value
	 */
	private float interpolate(float v0, float v1, float v2, float v3, float x) {
		float P = (v3 - v2) - (v0 - v1);
		float Q = (v0 - v1) - P;
		float R = v2 - v0;
		float S = v1;

		return P * ((float) pow(x, 3)) + Q * ((float) pow(x, 2)) + R * x + S;
	}
	

	/**
	 * Smooth and interpolate the noise
	 * 
	 * @param x - world tile x-coord
	 * @param y - world tile y-coord
	 * @return the smooth, interpolated noise
	 */
	private float interpolatedNoise(float x, float y) {

		int tempx = (int) floor(x);
		int tempy = (int) floor(y);
		float remX = x - tempx;
		float remY = y - tempy;

		float v1 = smoothNoise(tempx - 1, tempy - 1);
		float v2 = smoothNoise(tempx, tempy - 1);
		float v3 = smoothNoise(tempx + 1, tempy - 1);
		float v4 = smoothNoise(tempx + 2, tempy - 1);
		float v5 = smoothNoise(tempx - 1, tempy);
		float v6 = smoothNoise(tempx, tempy);
		float v7 = smoothNoise(tempx + 1, tempy);
		float v8 = smoothNoise(tempx + 2, tempy);
		float v9 = smoothNoise(tempx - 1, tempy + 1);
		float v10 = smoothNoise(tempx, tempy + 1);
		float v11 = smoothNoise(tempx + 1, tempy + 1);
		float v12 = smoothNoise(tempx + 2, tempy + 1);
		float v13 = smoothNoise(tempx - 1, tempy + 2);
		float v14 = smoothNoise(tempx, tempy + 2);
		float v15 = smoothNoise(tempx + 1, tempy + 2);
		float v16 = smoothNoise(tempx + 2, tempy + 2);

		float i1 = interpolate(v1, v2, v3, v4, remX);
		float i2 = interpolate(v5, v6, v7, v8, remX);
		float i3 = interpolate(v9, v10, v11, v12, remX);
		float i4 = interpolate(v13, v14, v15, v16, remX);

		return interpolate(i1, i2, i3, i4, remY);
	}
	

	/**
	 * Generates Perlin noise
	 * 
	 * @param x - world tile x-coord
	 * @param y - world tile y-coord
	 * @return The generated value
	 */
	public float generate(int tileX, int tileY) {
		float x = (float) tileX / stretch;
		float y = (float) tileY / stretch;

		return interpolatedNoise(x, y);
	}
}

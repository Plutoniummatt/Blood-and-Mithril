package bloodandmithril.world.weather;

import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.World;

/**
 * Contains weather related logic
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class WeatherService {

	
	public final Color getSunColor(final World world) {
		final float time = world.getEpoch().getTime();
		final Color filter = new Color();

		if (time < 9) {
			filter.r = (float) (0.5D + 0.5D * exp(-0.050*pow(time-9, 2)));
			filter.g = (float) (0.4D + 0.6D * exp(-0.150*pow(time-9, 2)));
			filter.b = (float) (0.1D + 0.9D * exp(-0.200*pow(time-9, 2)));
		} else if (time >= 9 && time < 15) {
			filter.r = 1.0f;
			filter.g = 1.0f;
			filter.b = 1.0f;
		} else {
			filter.r = (float) (0.5D + 0.5D * exp(-0.050*pow(time-15, 2)));
			filter.g = (float) (0.4D + 0.6D * exp(-0.150*pow(time-15, 2)));
			filter.b = (float) (0.1D + 0.9D * exp(-0.200*pow(time-15, 2)));
		}
		filter.a = 1f;

		return filter;
	}
	
	
	public final Color getDaylightColor(final World world) {
		final float time = world.getEpoch().getTime();
		final Color filter = new Color();

		if (time < 10) {
			filter.r = (float) (0.06D + 1.1D * exp(-0.100*pow(time-10, 2)));
			filter.g = (float) (0.06D + 0.9D * exp(-0.150*pow(time-10, 2)));
			filter.b = (float) (0.06D + 0.7D * exp(-0.200*pow(time-10, 2)));
		} else if (time >= 10 && time < 14) {
			filter.r = 1.16f;
			filter.g = 0.96f;
			filter.b = 0.76f;
		} else {
			filter.r = (float) (0.06D + 1.1D * exp(-0.100*pow(time-14, 2)));
			filter.g = (float) (0.06D + 0.9D * exp(-0.150*pow(time-14, 2)));
			filter.b = (float) (0.06D + 0.7D * exp(-0.200*pow(time-14, 2)));
		}
		filter.a = 1f;

		return filter;
	}
	
	
	public final float glareAlpha(final float time) {
		if (time <= 4f || time >= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time - 4f) * (Math.PI / 16f)), 2f);
	}


	public final float nightSuppression(final float time) {
		if (time >= 4f || time <= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time + 4f) * (Math.PI / 8f)), 2f);
	}


	public final float volumetricAlphaMultiplier(final float time) {
		if (time >= 8f && time <= 16f) {
			return 1f;
		} else if (time >= 16f && time <= 18f) {
			return 1f + 0.25f * (18f - time);
		} else if (time >= 6f && time <= 8f) {
			return 1f + 0.25f * (8f - time);
		}

		return 1.5f;
	}
}

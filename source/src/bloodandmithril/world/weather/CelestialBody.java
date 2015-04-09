package bloodandmithril.world.weather;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.Map;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * Class representing a celestial body
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class CelestialBody {

	public static Map<Integer, TextureRegion> starTextures = Maps.newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			// Populate texture regions
		}
	}

	/** Texture ID of this star */
	public final int textureId;

	/** Sky coordinates of the star, relative to the orbital pivot, angle is measured as at midnight, in degrees, positive clockwise */
	public final float orbitalRadius, angle;

	/**
	 * Constructor
	 */
	public CelestialBody(int textureId, float orbitalRadius, float angle) {
		this.textureId = textureId;
		this.orbitalRadius = orbitalRadius;
		this.angle = angle;
	}


	/**
	 * Renders this star
	 */
	public void render() {
		float time = WorldState.getCurrentEpoch().getTime();
		float theta = angle + time / 24f * 360f;

		BloodAndMithrilClient.spriteBatch.draw(
			starTextures.get(textureId),
			Weather.orbitalPivot.x + orbitalRadius * (float) sin(theta),
			Weather.orbitalPivot.y + orbitalRadius * (float) cos(theta)
		);
	}
}
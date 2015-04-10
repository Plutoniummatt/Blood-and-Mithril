package bloodandmithril.world.weather;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.graphics.Color;
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
			starTextures.put(0, new TextureRegion(Domain.gameWorldTexture, 1, 422, 100, 100)); // Moon
			starTextures.put(1, new TextureRegion(Domain.gameWorldTexture, 1, 400, 21, 21));
			starTextures.put(2, new TextureRegion(Domain.gameWorldTexture, 23, 400, 15, 15));
			starTextures.put(3, new TextureRegion(Domain.gameWorldTexture, 39, 400, 13, 13));
			starTextures.put(4, new TextureRegion(Domain.gameWorldTexture, 53, 400, 11, 11));
		}
	}

	/** Texture ID of this star */
	public final int textureId;

	/** Sky coordinates of the star, relative to the orbital pivot, angle is measured as at midnight, in degrees, positive clockwise */
	public final float orbitalRadius, angle;

	/** Color of this {@link CelestialBody} */
	public final Color filter;

	/**
	 * Constructor
	 */
	public CelestialBody(int textureId, float orbitalRadius, float angle, Color filter) {
		this.textureId = textureId;
		this.orbitalRadius = orbitalRadius;
		this.angle = angle;
		this.filter = filter;
	}


	/**
	 * Renders this star
	 */
	public void render() {
		float time = WorldState.getCurrentEpoch().getTime();
		float theta = angle + time / 24f * 360f;

		TextureRegion region = starTextures.get(textureId);

		Shaders.filter.begin();
		Shaders.filter.setUniformf("color", max(0.9f, filter.r), max(0.9f, filter.g), max(0.9f, filter.b), 1.2f - Weather.getDaylightColor().r);
		spriteBatch.draw(
			region,
			Weather.orbitalPivot.x + orbitalRadius * (float) sin(toRadians(theta)) - region.getRegionWidth() / 2,
			Weather.orbitalPivot.y + orbitalRadius * (float) cos(toRadians(theta)) + region.getRegionHeight() / 2
		);
		spriteBatch.flush();
	}
}
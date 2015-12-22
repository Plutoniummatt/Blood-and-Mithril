package bloodandmithril.world.weather;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.World;

/**
 * Class representing a celestial body
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public final class CelestialBody {

	public static final Map<Integer, TextureRegion> starTextures = Maps.newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			starTextures.put(0, new TextureRegion(WorldRenderer.gameWorldTexture, 1, 422, 100, 100)); // Moon
			starTextures.put(1, new TextureRegion(WorldRenderer.gameWorldTexture, 1, 400, 21, 21));
			starTextures.put(2, new TextureRegion(WorldRenderer.gameWorldTexture, 23, 400, 15, 15));
			starTextures.put(3, new TextureRegion(WorldRenderer.gameWorldTexture, 39, 400, 13, 13));
			starTextures.put(4, new TextureRegion(WorldRenderer.gameWorldTexture, 53, 400, 11, 11));
		}
	}

	/** Texture ID of this star */
	public final int textureId;

	/** Sky coordinates of the star, relative to the orbital pivot, angle is measured as at midnight, in degrees, positive clockwise */
	public final float orbitalRadius, angle;

	/** Color of this {@link CelestialBody} */
	public final Color filter;

	/** Whether this {@link CelestialBody} rotates */
	public final boolean rotates;

	/**
	 * Constructor
	 */
	public CelestialBody(int textureId, float orbitalRadius, float angle, Color filter, boolean rotates) {
		this.textureId = textureId;
		this.orbitalRadius = orbitalRadius;
		this.angle = angle;
		this.filter = filter;
		this.rotates = rotates;
	}


	/**
	 * Renders this star
	 */
	public final void render(World world) {
		float time = world.getEpoch().getTime();
		float theta = angle + time / 24f * 360f;

		TextureRegion region = starTextures.get(textureId);

		Shaders.filter.begin();
		Shaders.filter.setUniformf("color", max(0.9f, filter.r), max(0.9f, filter.g), max(0.9f, filter.b), (float) Math.pow(1.0f - WeatherRenderer.getDaylightColor(world).r, 2));
		if (rotates) {
			getGraphics().getSpriteBatch().draw(
				region,
				WeatherRenderer.orbitalPivot.x + orbitalRadius * (float) sin(toRadians(theta)) - region.getRegionWidth() / 2,
				WeatherRenderer.orbitalPivot.y + orbitalRadius * (float) cos(toRadians(theta)) + region.getRegionHeight() / 2,
				region.getRegionWidth() / 2,
				region.getRegionHeight() / 2,
				region.getRegionWidth(),
				region.getRegionHeight(),
				1f,
				1f,
				- theta + 90f
			);
		} else {
			getGraphics().getSpriteBatch().draw(
				region,
				WeatherRenderer.orbitalPivot.x + orbitalRadius * (float) sin(toRadians(theta)) - region.getRegionWidth() / 2,
				WeatherRenderer.orbitalPivot.y + orbitalRadius * (float) cos(toRadians(theta)) + region.getRegionHeight() / 2
			);
		}
	}
}
package bloodandmithril.world.weather;

import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static bloodandmithril.graphics.Textures.celestialBodyTextures;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.World;

/**
 * Renders {@link CelestialBody}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CelestialBodyRenderer {
	
	static {
		if (ClientServerInterface.isClient()) {
			celestialBodyTextures.put(0, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 422, 100, 100)); // Moon
			celestialBodyTextures.put(1, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 400, 21, 21));
			celestialBodyTextures.put(2, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 23, 400, 15, 15));
			celestialBodyTextures.put(3, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 39, 400, 13, 13));
			celestialBodyTextures.put(4, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 53, 400, 11, 11));
		}
	}
	
	final Vector2 orbitalPivot = new Vector2(getGdxWidth()/2, getGdxHeight()/4);
	
	@Inject private WeatherService weatherService;
	@Inject private Graphics graphics;

	public void render(CelestialBody body, World world) {
		float time = world.getEpoch().getTime();
		float theta = body.angle + time / 24f * 360f;

		TextureRegion region = celestialBodyTextures.get(body.textureId);

		Shaders.filter.begin();
		Shaders.filter.setUniformf("color", max(0.9f, body.filter.r), max(0.9f, body.filter.g), max(0.9f, body.filter.b), (float) Math.pow(1.0f - weatherService.getDaylightColor(world).r, 2));
		if (body.rotates) {
			graphics.getSpriteBatch().draw(
				region,
				orbitalPivot.x + body.orbitalRadius * (float) sin(toRadians(theta)) - region.getRegionWidth() / 2,
				orbitalPivot.y + body.orbitalRadius * (float) cos(toRadians(theta)) + region.getRegionHeight() / 2,
				region.getRegionWidth() / 2,
				region.getRegionHeight() / 2,
				region.getRegionWidth(),
				region.getRegionHeight(),
				1f,
				1f,
				- theta + 90f
			);
		} else {
			graphics.getSpriteBatch().draw(
				region,
				orbitalPivot.x + body.orbitalRadius * (float) sin(toRadians(theta)) - region.getRegionWidth() / 2,
				orbitalPivot.y + body.orbitalRadius * (float) cos(toRadians(theta)) + region.getRegionHeight() / 2
			);
		}
	}
	
	
	public void resize() {
		orbitalPivot.x = getGdxWidth()/2;
		orbitalPivot.y = getGdxHeight()/4;
	}
}
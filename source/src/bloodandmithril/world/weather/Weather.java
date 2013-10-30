package bloodandmithril.world.weather;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;

import bloodandmithril.Fortress;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

/**
 * Weather class, renderable, changes with {@link Epoch}
 *
 * @author Matt
 */
public class Weather {

	private static ShapeRenderer shapeRenderer = new ShapeRenderer();

	private static Color dayTopColor = new Color(33f/150f, 169f/255f, 255f/255f, 1f);
	private static Color dayBottomColor = new Color(0f, 144f/255f, 1f, 1f);
	private static Color nightTopColor = new Color(87f/255f, 0f, 218f/255f, 1f);
	private static Color nightBottomColor = new Color(33f/255f, 0f, 232f/255f, 1f);

	/** Texture regions for various phases of the moon */
	private static List<TextureRegion> moonPhases = new ArrayList<TextureRegion>();

	private static TextureRegion sunTexture = new TextureRegion(GameWorld.gameWorldTexture, 0, 1536, 512, 512);
	private static TextureRegion sunGlowTexture = new TextureRegion(GameWorld.gameWorldTexture, 512, 1536, 512, 512);

	/** Orbital radius of the moon around the {@link #celestialPivot} */
	private static float celestialOrbitalRadius = Fortress.WIDTH * 3 / 4;

	/** The pivot around which the moon resolves */
	private static Vector2 celestialPivot = new Vector2(Fortress.WIDTH/2, -Fortress.HEIGHT/2);

	/** The index to access {@link #moonPhases} with, updated between 11:00 and 13:00 */
	private static int moonPhaseIndex = (int)(12f / 30f * WorldState.currentEpoch.dayOfMonth);

	/** Load resources */
	public static void setup() {
		populateMoonTextureRegions();
	}


	/**
	 * Renders the {@link Weather}
	 */
	public static void render() {
		renderSky();
		renderMoon();
		renderSun();
	}


	/** Renders the sun */
	private static void renderSun() {
		float time = WorldState.currentEpoch.getTime();

		double angle = time < 12f ? Math.PI/2f + Math.PI * (time- 2f)/20f : Math.PI + Math.PI * (time - 12f)/20f;

		float x = celestialPivot.x - celestialOrbitalRadius * (float)Math.sin((float)angle) - sunTexture.getRegionWidth()/2;
		float y = celestialPivot.y - celestialOrbitalRadius * (float)Math.cos((float)angle) - sunTexture.getRegionHeight()/2;

		Fortress.spriteBatch.begin();
		Shaders.sun.begin();
		Shaders.sun.setUniformf("time", time);
		Shaders.sun.end();
		Fortress.spriteBatch.setShader(Shaders.sun);
		Fortress.spriteBatch.draw(sunGlowTexture, x - 768, y - 768, 2048, 2048);
		Fortress.spriteBatch.setShader(Shaders.pass);
		Fortress.spriteBatch.draw(sunTexture, x, y);
		Fortress.spriteBatch.end();
	}


	/** Renders the moon */
	private static void renderMoon() {
		float time = WorldState.currentEpoch.getTime();

		if (time > 11f && time < 13f) {
			moonPhaseIndex = (int)(12f / 30f * WorldState.currentEpoch.dayOfMonth);
		}

		double angle = Math.PI + time < 12f ? Math.PI + Math.PI * time/24f : Math.PI * time/24f;

		float x = celestialPivot.x - celestialOrbitalRadius * (float)Math.sin((float)angle) - moonPhases.get(moonPhaseIndex).getRegionWidth()/2;
		float y = celestialPivot.y - celestialOrbitalRadius * (float)Math.cos((float)angle) - moonPhases.get(moonPhaseIndex).getRegionHeight()/2;

		Fortress.spriteBatch.begin();
		Fortress.spriteBatch.setShader(Shaders.moon);
		Fortress.spriteBatch.draw(moonPhases.get(moonPhaseIndex), x, y);
		Fortress.spriteBatch.end();
	}


	/** Renders the sky */
	private static void renderSky() {
		shapeRenderer.begin(ShapeType.FilledRectangle);

		float time = WorldState.currentEpoch.getTime();
		Color filter = new Color();

		if (time < 10) {
			filter.r = (float) (0.1D + 1.2D * exp(-0.100*pow(time-10, 2)));
			filter.g = (float) (0.1D + 1.2D * exp(-0.150*pow(time-10, 2)));
			filter.b = (float) (0.1D + 1.2D * exp(-0.200*pow(time-10, 2)));
		} else if (time >= 10 && time < 14) {
			filter.r = 1.3f;
			filter.g = 1.3f;
			filter.b = 1.3f;
		} else {
			filter.r = (float) (0.1D + 1.2D * exp(-0.100*pow(time-14, 2)));
			filter.g = (float) (0.1D + 1.2D * exp(-0.150*pow(time-14, 2)));
			filter.b = (float) (0.1D + 1.2D * exp(-0.200*pow(time-14, 2)));
		}

		Color topColor = dayTopColor.cpy().mul(WorldState.currentEpoch.dayLight()).add(nightTopColor.cpy().mul(1f - WorldState.currentEpoch.dayLight())).mul(filter);
		Color bottomColor = dayBottomColor.cpy().mul(WorldState.currentEpoch.dayLight()).add(nightBottomColor.cpy().mul(1f - WorldState.currentEpoch.dayLight())).mul(filter);

		shapeRenderer.filledRect(0, 0, Fortress.WIDTH, Fortress.HEIGHT, bottomColor, bottomColor, topColor, topColor);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.FilledCircle);
		shapeRenderer.end();
	}


	/** Populates {@link #moonPhases} with the required {@link TextureRegion}s from {@link GameWorld#gameWorldTexture} */
	private static void populateMoonTextureRegions() {
		int sideLength = 175;
		for (int i = 0; i < 11; i++) {
			moonPhases.add(new TextureRegion(GameWorld.gameWorldTexture, i * sideLength, 0, sideLength, sideLength));
		}
		for (int i = 0; i < 2; i++) {
			moonPhases.add(new TextureRegion(GameWorld.gameWorldTexture, i * sideLength, 175, sideLength, sideLength));
		}
	}
}

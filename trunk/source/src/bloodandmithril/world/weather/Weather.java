package bloodandmithril.world.weather;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.world.Domain.gameWorldTexture;
import static bloodandmithril.world.WorldState.getCurrentEpoch;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.FilledCircle;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.FilledRectangle;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.List;

import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Weather class, renderable, changes with {@link Epoch}
 *
 * @author Matt
 */
public class Weather {

	private static ShapeRenderer shapeRenderer 			= new ShapeRenderer();

	private static Color dayTopColor 					= new Color(33f/150f, 169f/255f, 255f/255f, 1f);
	private static Color dayBottomColor 				= new Color(0f, 144f/255f, 1f, 1f);
	private static Color nightTopColor 					= new Color(87f/255f, 0f, 218f/255f, 1f);
	private static Color nightBottomColor 				= new Color(33f/255f, 0f, 232f/255f, 1f);

	/** Texture regions for various phases of the moon */
	private static List<TextureRegion> moonPhases 		= newArrayList();

	private static TextureRegion sunTexture 			= new TextureRegion(gameWorldTexture, 0, 1536, 512, 512);
	private static TextureRegion sunGlowTexture 		= new TextureRegion(gameWorldTexture, 512, 1536, 512, 512);

	/** Orbital radius of the moon around the {@link #celestialPivot} */
	private static float celestialOrbitalRadius 		= WIDTH * 3 / 4;

	/** The pivot around which the moon resolves */
	private static Vector2 celestialPivot 				= new Vector2(WIDTH/2, -HEIGHT/2);

	/** The index to access {@link #moonPhases} with, updated between 11:00 and 13:00 */
	private static int moonPhaseIndex 					= (int)(12f / 30f * getCurrentEpoch().dayOfMonth);

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


	public static Color getDaylightColor() {
		float time = getCurrentEpoch().getTime();
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

		return filter;
	}


	/** Renders the sun */
	private static void renderSun() {
		float time = WorldState.getCurrentEpoch().getTime();

		double angle = time < 12f ? PI/2f + PI * (time- 2f)/20f : PI + PI * (time - 12f)/20f;

		float x = celestialPivot.x - celestialOrbitalRadius * (float)sin((float)angle) - sunTexture.getRegionWidth()/2;
		float y = celestialPivot.y - celestialOrbitalRadius * (float)cos((float)angle) - sunTexture.getRegionHeight()/2;

		spriteBatch.begin();
		Shaders.sun.begin();
		Shaders.sun.setUniformf("time", time);
		Shaders.sun.end();
		spriteBatch.setShader(Shaders.sun);
		spriteBatch.draw(sunGlowTexture, x - 768, y - 768, 2048, 2048);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(sunTexture, x, y);
		spriteBatch.end();
	}


	/** Renders the moon */
	private static void renderMoon() {
		float time = getCurrentEpoch().getTime();

		if (time > 11f && time < 13f) {
			moonPhaseIndex = (int)(12f / 30f * getCurrentEpoch().dayOfMonth);
		}

		double angle = PI + time < 12f ? PI + PI * time/24f : PI * time/24f;

		float x = celestialPivot.x - celestialOrbitalRadius * (float)sin((float)angle) - moonPhases.get(moonPhaseIndex).getRegionWidth()/2;
		float y = celestialPivot.y - celestialOrbitalRadius * (float)cos((float)angle) - moonPhases.get(moonPhaseIndex).getRegionHeight()/2;

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.moon);
		spriteBatch.draw(moonPhases.get(moonPhaseIndex), x, y);
		spriteBatch.end();
	}


	/** Renders the sky */
	private static void renderSky() {
		shapeRenderer.begin(FilledRectangle);
		Color filter = getDaylightColor();

		Color topColor = dayTopColor.cpy().mul(getCurrentEpoch().dayLight()).add(nightTopColor.cpy().mul(1f - getCurrentEpoch().dayLight())).mul(filter);
		Color bottomColor = dayBottomColor.cpy().mul(getCurrentEpoch().dayLight()).add(nightBottomColor.cpy().mul(1f - getCurrentEpoch().dayLight())).mul(filter);

		shapeRenderer.filledRect(0, 0, WIDTH, HEIGHT, bottomColor, bottomColor, topColor, topColor);
		shapeRenderer.end();

		shapeRenderer.begin(FilledCircle);
		shapeRenderer.end();
	}


	/** Populates {@link #moonPhases} with the required {@link TextureRegion}s from {@link Domain#gameWorldTexture} */
	private static void populateMoonTextureRegions() {
		int sideLength = 175;
		for (int i = 0; i < 11; i++) {
			moonPhases.add(new TextureRegion(gameWorldTexture, i * sideLength, 0, sideLength, sideLength));
		}
		for (int i = 0; i < 2; i++) {
			moonPhases.add(new TextureRegion(gameWorldTexture, i * sideLength, 175, sideLength, sideLength));
		}
	}
}

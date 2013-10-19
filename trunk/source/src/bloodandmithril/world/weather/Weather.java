package bloodandmithril.world.weather;

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
	private static Color nightTopColor = new Color(3.5f/255f, 0f, 9.5f/255f, 1f);
	private static Color nightBottomColor = new Color(14f/255f, 0f, 38f/255f, 1f);

	/** Texture regions for various phases of the moon */
	private static List<TextureRegion> moonPhases = new ArrayList<TextureRegion>();

	/** Orbital radius of the moon around the {@link #moonPivot} */
	private static float moonOrbitalRadius = 1400f;

	/** The pivot around which the moon resolves */
	private static Vector2 moonPivot = new Vector2(Fortress.WIDTH/2, -moonOrbitalRadius/2);

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
	}


	/** Renders the moon */
	private static void renderMoon() {
		float time = WorldState.currentEpoch.getTime();

		if (time > 11f && time < 13f) {
			moonPhaseIndex = (int)(12f / 30f * WorldState.currentEpoch.dayOfMonth);
		}

		double angle = Math.PI + time < 12f ? Math.PI + Math.PI * time/24f : Math.PI * time/24f;

		float x = moonPivot.x - moonOrbitalRadius * (float)Math.sin((float)angle) - moonPhases.get(moonPhaseIndex).getRegionWidth()/2;
		float y = moonPivot.y - moonOrbitalRadius * (float)Math.cos((float)angle) - moonPhases.get(moonPhaseIndex).getRegionHeight()/2;

		Fortress.spriteBatch.begin();
		Fortress.spriteBatch.setShader(Shaders.moon);
		Fortress.spriteBatch.draw(moonPhases.get(moonPhaseIndex), x, y);
		Fortress.spriteBatch.end();
	}


	/** Renders the sky */
	private static void renderSky() {
		shapeRenderer.begin(ShapeType.FilledRectangle);

		Color topColor = dayTopColor.cpy().mul(WorldState.currentEpoch.dayLight()).add(nightTopColor.cpy().mul(1f - WorldState.currentEpoch.dayLight()));
		Color bottomColor = dayBottomColor.cpy().mul(WorldState.currentEpoch.dayLight()).add(nightBottomColor.cpy().mul(1f - WorldState.currentEpoch.dayLight()));

		shapeRenderer.filledRect(0, 0, Fortress.WIDTH, Fortress.HEIGHT, bottomColor, bottomColor, topColor, topColor);
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

package bloodandmithril.world.weather;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.exp;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.graphics.background.Layer;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.World;

/**
 * Weather class, renderable, changes with {@link Epoch}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class Weather {

	private static final Color dayTopColor 								= new Color(33f/150f, 169f/255f, 255f/255f, 1f);
	private static final Color dayBottomColor 							= new Color(0f, 144f/255f, 1f, 1f);
	private static final Color nightTopColor 							= new Color(33f/255f, 0f, 150f/255f, 1f);
	private static final Color nightBottomColor 						= new Color(60f/255f, 0f, 152f/255f, 1f);

	private static FrameBuffer skyBuffer								= new FrameBuffer(RGBA8888, getGraphics().getWidth(), getGraphics().getHeight(), false);
	private static FrameBuffer working									= new FrameBuffer(RGBA8888, 1, 1, false);

	private static Vector2 sunPosition									= new Vector2();
	public static Vector2 orbitalPivot 									= new Vector2(getGraphics().getWidth()/2, 0);

	private static final LinkedList<CelestialBody> celestialBodies		= Lists.newLinkedList();


	/**
	 * Renders the {@link Weather}
	 */
	public static final void render(FrameBuffer toDrawTo, World world) {
		renderSky(toDrawTo, world);
		renderStars(toDrawTo, world);
		updateSun(world);
	}


	private static final void updateSun(World world) {
		float time = world.getEpoch().getTime();
		float radius = getGraphics().getWidth()/2.5f;
		Vector2 position = orbitalPivot.cpy().add(new Vector2(0f, radius).rotate(-((time - 12f) / 12f) * 180f));

		sunPosition.x = position.x;
		sunPosition.y = position.y;
	}


	public static final Color getDaylightColor(World world) {
		float time = world.getEpoch().getTime();
		Color filter = new Color();

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


	public static final Vector2 getSunPosition() {
		return sunPosition;
	}


	public static final Color getSunColor(World world) {
		float time = world.getEpoch().getTime();
		Color filter = new Color();

		if (time < 10) {
			filter.r = (float) (0.5D + 0.5D * exp(-0.050*pow(time-10, 2)));
			filter.g = (float) (0.2D + 0.8D * exp(-0.150*pow(time-10, 2)));
			filter.b = (float) (0.1D + 0.9D * exp(-0.200*pow(time-10, 2)));
		} else if (time >= 10 && time < 14) {
			filter.r = 1.1f;
			filter.g = 1.1f;
			filter.b = 1.1f;
		} else {
			filter.r = (float) (0.5D + 0.5D * exp(-0.050*pow(time-14, 2)));
			filter.g = (float) (0.2D + 0.8D * exp(-0.150*pow(time-14, 2)));
			filter.b = (float) (0.1D + 0.9D * exp(-0.200*pow(time-14, 2)));
		}
		filter.a = 1f;

		return filter;
	}


	/** Renders the sky */
	private static final void renderSky(FrameBuffer toDrawTo, World world) {
		skyBuffer.begin();
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.sky);
		Color filter = getDaylightColor(world);

		Color topColor = dayTopColor.cpy().mul(world.getEpoch().dayLight()).add(nightTopColor.cpy().mul(1f - world.getEpoch().dayLight())).mul(filter);
		Color bottomColor = dayBottomColor.cpy().mul(world.getEpoch().dayLight()).add(nightBottomColor.cpy().mul(1f - world.getEpoch().dayLight())).mul(filter);

		Shaders.sky.setUniformf("top", topColor);
		Shaders.sky.setUniformf("bottom", bottomColor);
		Shaders.sky.setUniformf("sun", sunPosition);
		Shaders.sky.setUniformf("horizon", (float) Layer.getScreenHorizonY() / getGraphics().getHeight());
		Shaders.sky.setUniformf("resolution", getGraphics().getWidth(), getGraphics().getHeight());

		getGraphics().getSpriteBatch().draw(working.getColorBufferTexture(), 0, 0, getGraphics().getWidth(), getGraphics().getHeight());
		getGraphics().getSpriteBatch().end();
		skyBuffer.end();

		float time = world.getEpoch().getTime();

		toDrawTo.begin();
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.sun);
		Shaders.sun.setUniformf("resolution", getGraphics().getWidth(), getGraphics().getHeight());
		Shaders.sun.setUniformf("sunPosition", sunPosition);
		Shaders.sun.setUniformf("filter", Colors.modulateAlpha(getSunColor(world), glareAlpha(time)));
		Shaders.sun.setUniformf("epoch", world.getEpoch().getTime());
		Shaders.sun.setUniformf("nightSuppression", nightSuppression(time));
		getGraphics().getSpriteBatch().draw(skyBuffer.getColorBufferTexture(), 0, 0);
		getGraphics().getSpriteBatch().end();
		toDrawTo.end();
	}


	private static final void renderStars(FrameBuffer toDrawTo, World world) {
		toDrawTo.begin();
		getGraphics().getSpriteBatch().begin();
		WorldRenderer.gameWorldTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		for (CelestialBody celestialBody : celestialBodies) {
			celestialBody.render(world);
		}
		getGraphics().getSpriteBatch().end();
		toDrawTo.end();
		WorldRenderer.gameWorldTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	private static final float glareAlpha(float time) {
		if (time <= 4f || time >= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time - 4f) * (Math.PI / 16f)), 2f);
	}


	private static final float nightSuppression(float time) {
		if (time >= 4f || time <= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time + 4f) * (Math.PI / 8f)), 2f);
	}


	public static final float volumetricAlphaMultiplier(float time) {
		if (time >= 8f && time <= 16f) {
			return 1f;
		} else if (time >= 16f && time <= 18f) {
			return 1f + 0.25f * (18f - time);
		} else if (time >= 6f && time <= 8f) {
			return 1f + 0.25f * (8f - time);
		}

		return 1.5f;
	}


	public static final void dispose() {
		skyBuffer.dispose();
		working.dispose();
	}


	/** Load resources */
	public static final void setup() {
		skyBuffer = new FrameBuffer(RGBA8888, getGraphics().getWidth(), getGraphics().getHeight(), false);
		working = new FrameBuffer(RGBA8888, 1, 1, false);
		orbitalPivot = new Vector2(getGraphics().getWidth()/2, 0);

		celestialBodies.clear();
		celestialBodies.add(new CelestialBody(0, getGraphics().getWidth()/2.5f, 0f, Color.WHITE, true));

		for (int i = 0; i < 500; i++) {
			Vector2 cartesian = new Vector2(
				(Util.getRandom().nextFloat() - 0.5f) * max(getGraphics().getWidth(), getGraphics().getHeight()) * 2.0f,
				(Util.getRandom().nextFloat() - 0.5f) * max(getGraphics().getWidth(), getGraphics().getHeight()) * 2.0f
			);

			celestialBodies.add(
				new CelestialBody(
					Util.randomOneOf(2, 3, 4),
					cartesian.len(),
					cartesian.angle(),
					Util.randomOneOf(Color.RED, Color.BLUE, Color.WHITE, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.GREEN),
					false
				)
			);
		}

		for (int i = 0; i < 15; i++) {
			Vector2 cartesian = new Vector2(
				(Util.getRandom().nextFloat() - 0.5f) * max(getGraphics().getWidth(), getGraphics().getHeight()) * 2.0f,
				(Util.getRandom().nextFloat() - 0.5f) * max(getGraphics().getWidth(), getGraphics().getHeight()) * 2.0f
			);

			celestialBodies.add(
				new CelestialBody(
					Util.randomOneOf(1),
					cartesian.len(),
					cartesian.angle(),
					Util.randomOneOf(Color.RED, Color.BLUE, Color.WHITE, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.GREEN),
					false
				)
			);
		}
	}
}

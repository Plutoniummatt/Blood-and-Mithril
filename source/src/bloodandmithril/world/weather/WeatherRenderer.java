package bloodandmithril.world.weather;

import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.exp;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
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
public final class WeatherRenderer {

	private static final Color dayTopColor 								= new Color(33f/150f, 169f/255f, 255f/255f, 1f);
	private static final Color dayBottomColor 							= new Color(0f, 144f/255f, 1f, 1f);
	private static final Color nightTopColor 							= new Color(33f/255f, 0f, 150f/255f, 1f);
	private static final Color nightBottomColor 						= new Color(60f/255f, 0f, 152f/255f, 1f);

	private static FrameBuffer skyBuffer								= new FrameBuffer(RGBA8888, getGdxWidth(), getGdxHeight(), false);
	private static FrameBuffer working									= new FrameBuffer(RGBA8888, 1, 1, false);

	private static Vector2 sunPosition									= new Vector2();
	public static Vector2 orbitalPivot 									= new Vector2(getGdxWidth()/2, getGdxHeight()/4);

	private static final LinkedList<CelestialBody> celestialBodies		= Lists.newLinkedList();

	/**
	 * Renders the {@link WeatherRenderer}
	 */
	public static final void render(final FrameBuffer toDrawTo, final World world, final Graphics graphics) {
		renderSky(toDrawTo, world, graphics);
		renderStars(toDrawTo, world, graphics);
		updateSun(world, graphics);
	}


	private static final void updateSun(final World world, final Graphics graphics) {
		final float time = world.getEpoch().getTime();
		final float radius = graphics.getWidth()/2.5f;
		final Vector2 position = orbitalPivot.cpy().add(new Vector2(0f, radius).rotate(-((time - 12f) / 12f) * 180f));

		sunPosition.x = position.x;
		sunPosition.y = position.y;
	}


	public static final Color getDaylightColor(final World world) {
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


	public static final Vector2 getSunPosition() {
		return sunPosition;
	}


	public static final Color getSunColor(final World world) {
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


	/** Renders the sky */
	private static final void renderSky(final FrameBuffer toDrawTo, final World world, final Graphics graphics) {
		skyBuffer.begin();
		graphics.getSpriteBatch().begin();
		graphics.getSpriteBatch().setShader(Shaders.sky);
		final Color filter = getDaylightColor(world);
		
		final Color topColor = dayTopColor.cpy().mul(world.getEpoch().dayLight()).add(nightTopColor.cpy().mul(1f - world.getEpoch().dayLight())).mul(filter);
		final Color bottomColor = dayBottomColor.cpy().mul(world.getEpoch().dayLight()).add(nightBottomColor.cpy().mul(1f - world.getEpoch().dayLight())).mul(filter);

		Shaders.sky.setUniformf("top", topColor);
		Shaders.sky.setUniformf("bottom", bottomColor);
		Shaders.sky.setUniformf("sun", sunPosition);
		Shaders.sky.setUniformf("horizon", (float) Layer.getScreenHorizonY(graphics) / graphics.getHeight());
		Shaders.sky.setUniformf("resolution", graphics.getWidth(), graphics.getHeight());

		graphics.getSpriteBatch().draw(working.getColorBufferTexture(), 0, 0, graphics.getWidth(), graphics.getHeight());
		graphics.getSpriteBatch().flush();
		graphics.getSpriteBatch().end();
		skyBuffer.end();

		final float time = world.getEpoch().getTime();

		toDrawTo.begin();
		graphics.getSpriteBatch().begin();
		graphics.getSpriteBatch().setShader(Shaders.sun);
		Shaders.sun.setUniformf("resolution", graphics.getWidth(), graphics.getHeight());
		Shaders.sun.setUniformf("sunPosition", sunPosition);
		Shaders.sun.setUniformf("filter", Colors.modulateAlpha(getSunColor(world), glareAlpha(time)));
		Shaders.sun.setUniformf("epoch", world.getEpoch().getTime());
		Shaders.sun.setUniformf("nightSuppression", nightSuppression(time));
		graphics.getSpriteBatch().draw(skyBuffer.getColorBufferTexture(), 0, 0);
		graphics.getSpriteBatch().end();
		toDrawTo.end();
	}


	private static final void renderStars(final FrameBuffer toDrawTo, final World world, final Graphics graphics) {
		toDrawTo.begin();
		graphics.getSpriteBatch().begin();
		final int source = graphics.getSpriteBatch().getBlendSrcFunc();
		final int destination = graphics.getSpriteBatch().getBlendDstFunc();
		graphics.getSpriteBatch().setBlendFunction(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		Textures.GAME_WORLD_TEXTURE.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		for (final CelestialBody celestialBody : celestialBodies) {
			celestialBody.render(world, graphics);
		}
		graphics.getSpriteBatch().end();
		graphics.getSpriteBatch().setBlendFunction(source, destination);
		toDrawTo.end();
		Textures.GAME_WORLD_TEXTURE.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	private static final float glareAlpha(final float time) {
		if (time <= 4f || time >= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time - 4f) * (Math.PI / 16f)), 2f);
	}


	private static final float nightSuppression(final float time) {
		if (time >= 4f || time <= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time + 4f) * (Math.PI / 8f)), 2f);
	}


	public static final float volumetricAlphaMultiplier(final float time) {
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
		skyBuffer = new FrameBuffer(RGBA8888, getGdxWidth(), getGdxHeight(), false);
		working = new FrameBuffer(RGBA8888, 1, 1, false);
		orbitalPivot = new Vector2(getGdxWidth()/2, getGdxHeight()/4);

		celestialBodies.clear();
		celestialBodies.add(new CelestialBody(0, getGdxWidth()/2.5f, 0f, Color.WHITE, true));

		for (int i = 0; i < 500; i++) {
			final Vector2 cartesian = new Vector2(
				(Util.getRandom().nextFloat() - 0.5f) * max(getGdxWidth(), getGdxHeight()) * 2.0f,
				(Util.getRandom().nextFloat() - 0.5f) * max(getGdxWidth(), getGdxHeight()) * 2.0f
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
			final Vector2 cartesian = new Vector2(
				(Util.getRandom().nextFloat() - 0.5f) * max(getGdxWidth(), getGdxHeight()) * 2.0f,
				(Util.getRandom().nextFloat() - 0.5f) * max(getGdxWidth(), getGdxHeight()) * 2.0f
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

package bloodandmithril.world.weather;

import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.max;

import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.graphics.background.BackgroundRenderingService;
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
@Singleton
@Copyright("Matthew Peck 2014")
public final class WeatherRenderer {
	
	@Inject private WeatherService weatherService;
	@Inject private CelestialBodyRenderer celestialBodyRenderer;
	@Inject private BackgroundRenderingService backgroundRenderingService;

	private static final Color dayTopColor 								= new Color(33f/150f, 169f/255f, 255f/255f, 1f);
	private static final Color dayBottomColor 							= new Color(0f, 144f/255f, 1f, 1f);
	private static final Color nightTopColor 							= new Color(33f/255f, 0f, 150f/255f, 1f);
	private static final Color nightBottomColor 						= new Color(60f/255f, 0f, 152f/255f, 1f);

	private FrameBuffer skyBuffer								= new FrameBuffer(RGBA8888, getGdxWidth(), getGdxHeight(), false);
	private FrameBuffer working									= new FrameBuffer(RGBA8888, 1, 1, false);

	private final Vector2 sunPosition									= new Vector2();
	private final LinkedList<CelestialBody> celestialBodies				= Lists.newLinkedList();

	/**
	 * Renders the {@link WeatherRenderer}
	 */
	public final void render(final FrameBuffer toDrawTo, final World world, final Graphics graphics) {
		renderSky(toDrawTo, world, graphics);
		renderStars(toDrawTo, world, graphics);
		updateSun(world, graphics);
	}


	private final void updateSun(final World world, final Graphics graphics) {
		final float time = world.getEpoch().getTime();
		final float radius = graphics.getWidth()/2.5f;
		final Vector2 position = celestialBodyRenderer.orbitalPivot.cpy().add(new Vector2(0f, radius).rotate(-((time - 12f) / 12f) * 180f));

		sunPosition.x = position.x;
		sunPosition.y = position.y;
	}


	public final Vector2 getSunPosition() {
		return sunPosition;
	}


	/** Renders the sky */
	private final void renderSky(final FrameBuffer toDrawTo, final World world, final Graphics graphics) {
		skyBuffer.begin();
		graphics.getSpriteBatch().begin();
		graphics.getSpriteBatch().setShader(Shaders.sky);
		final Color filter = weatherService.getDaylightColor(world);
		
		final Color topColor = dayTopColor.cpy().mul(world.getEpoch().dayLight()).add(nightTopColor.cpy().mul(1f - world.getEpoch().dayLight())).mul(filter);
		final Color bottomColor = dayBottomColor.cpy().mul(world.getEpoch().dayLight()).add(nightBottomColor.cpy().mul(1f - world.getEpoch().dayLight())).mul(filter);

		Shaders.sky.setUniformf("top", topColor);
		Shaders.sky.setUniformf("bottom", bottomColor);
		Shaders.sky.setUniformf("sun", sunPosition);
		Shaders.sky.setUniformf("horizon", backgroundRenderingService.getHorizonScreenY() / graphics.getHeight());
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
		Shaders.sun.setUniformf("filter", Colors.modulateAlpha(weatherService.getSunColor(world), weatherService.glareAlpha(time)));
		Shaders.sun.setUniformf("epoch", world.getEpoch().getTime());
		Shaders.sun.setUniformf("nightSuppression", weatherService.nightSuppression(time));
		graphics.getSpriteBatch().draw(skyBuffer.getColorBufferTexture(), 0, 0);
		graphics.getSpriteBatch().end();
		toDrawTo.end();
	}


	private final void renderStars(final FrameBuffer toDrawTo, final World world, final Graphics graphics) {
		toDrawTo.begin();
		graphics.getSpriteBatch().begin();
		final int source = graphics.getSpriteBatch().getBlendSrcFunc();
		final int destination = graphics.getSpriteBatch().getBlendDstFunc();
		graphics.getSpriteBatch().setBlendFunction(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		Textures.GAME_WORLD_TEXTURE.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		for (final CelestialBody celestialBody : celestialBodies) {
			celestialBodyRenderer.render(celestialBody, world);
		}
		graphics.getSpriteBatch().end();
		graphics.getSpriteBatch().setBlendFunction(source, destination);
		toDrawTo.end();
		Textures.GAME_WORLD_TEXTURE.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	public final void dispose() {
		skyBuffer.dispose();
		working.dispose();
	}


	/** Load resources */
	public final void setup() {
		skyBuffer = new FrameBuffer(RGBA8888, getGdxWidth(), getGdxHeight(), false);
		working = new FrameBuffer(RGBA8888, 1, 1, false);

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

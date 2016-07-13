package bloodandmithril.graphics;

import static bloodandmithril.graphics.Graphics.isOnScreen;
import static bloodandmithril.graphics.WorldRenderer.Depth.BACKGROUND;
import static bloodandmithril.graphics.WorldRenderer.Depth.FOREGROUND;
import static bloodandmithril.graphics.WorldRenderer.Depth.MIDDLEGROUND;
import static bloodandmithril.util.Logger.generalDebug;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;
import static com.google.common.collect.Collections2.filter;
import static java.lang.Math.round;

import java.io.Serializable;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.google.common.base.Predicate;
import com.google.inject.Singleton;

import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Class for rendering {@link World}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class WorldRenderer {

	/** {@link WorldRenderer}-specific {@link ShapeRenderer} */
	public static ShapeRenderer shapeRenderer;

	/** Textures */
	public static Texture gameWorldTexture;
	public static Texture individualTexture;

	/** The frame buffer used for tiles */
	public static FrameBuffer fBuffer;
	public static FrameBuffer mBuffer;
	public static FrameBuffer bBuffer;
	public static FrameBuffer workingQuantized;
	public static FrameBuffer bBufferQuantized;
	public static FrameBuffer fBufferQuantized;
	public static FrameBuffer combinedBufferQuantized;

	private static TextureRegion circle;
	private static Graphics graphics;
	private static FactionControlService factionControlService;
	private static GameClientStateTracker gameClientStateTracker;

	static {
		if (ClientServerInterface.isClient()) {
			graphics = Wiring.injector().getInstance(Graphics.class);
			factionControlService = Wiring.injector().getInstance(FactionControlService.class);
			gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);
			gameWorldTexture = new Texture(files.internal("data/image/gameWorld.png"));
			individualTexture = new Texture(files.internal("data/image/character/individual.png"));
			gameWorldTexture.setFilter(Linear, Linear);
			individualTexture.setFilter(Nearest, Nearest);
			circle = new TextureRegion(WorldRenderer.gameWorldTexture, 102, 422, 100, 100);
		}
	}

	public static void dispose() {
		fBuffer.dispose();
		mBuffer.dispose();
		bBuffer.dispose();
		workingQuantized.dispose();
		bBufferQuantized.dispose();
		fBufferQuantized.dispose();
		combinedBufferQuantized.dispose();
	}

	public static void setup() {
		fBuffer 							= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		mBuffer 							= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		bBuffer 							= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		workingQuantized 					= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		bBufferQuantized 					= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		fBufferQuantized 					= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		combinedBufferQuantized 			= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);

		bBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		shapeRenderer = new ShapeRenderer();
	}


	public static void render(final World world, final int camX, final int camY) {
		final SpriteBatch batch = graphics.getSpriteBatch();

		gameClientStateTracker.setRendering(true);
		bBuffer.begin();
		Shaders.invertAlphaSolidColor.begin();
		world.getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {}, graphics);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.pass.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		for (final Prop prop : world.props().getProps()) {
			if (prop.depth == BACKGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.render(graphics);
				batch.flush();
			}
		}
		renderParticles(Depth.BACKGROUND, world);
		batch.end();
		bBuffer.end();

		final int xOffset = round(graphics.getCam().position.x) % TILE_SIZE;
		final int yOffset = round(graphics.getCam().position.y) % TILE_SIZE;

		workingQuantized.begin();
		graphics.getCam().position.x = graphics.getCam().position.x - xOffset;
		graphics.getCam().position.y = graphics.getCam().position.y - yOffset;
		graphics.getCam().update();
		world.getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {}, graphics);
		graphics.getCam().position.x = graphics.getCam().position.x + xOffset;
		graphics.getCam().position.y = graphics.getCam().position.y + yOffset;
		graphics.getCam().update();
		workingQuantized.end();

		bBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 1.0f, 0.0f, 0.0f, 1.0f);
		batch.draw(workingQuantized.getColorBufferTexture(), 0, 0, Graphics.getGdxWidth(), Graphics.getGdxHeight());
		batch.end();
		bBufferQuantized.end();

		workingQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		graphics.getCam().position.x = graphics.getCam().position.x - xOffset;
		graphics.getCam().position.y = graphics.getCam().position.y - yOffset;
		graphics.getCam().update();
		world.getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {}, graphics);
		graphics.getCam().position.x = graphics.getCam().position.x + xOffset;
		graphics.getCam().position.y = graphics.getCam().position.y + yOffset;
		graphics.getCam().update();
		workingQuantized.end();

		fBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 0.0f, 1.0f, 0.0f, 1.0f);
		batch.draw(workingQuantized.getColorBufferTexture(), 0, 0, Graphics.getGdxWidth(), Graphics.getGdxHeight());
		batch.end();
		fBufferQuantized.end();

		combinedBufferQuantized.begin();
		batch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setShader(Shaders.invertAlphaSolidColorBlend);
		bBufferQuantized.getColorBufferTexture().bind(1);
		Shaders.invertAlphaSolidColorBlend.setUniformi("u_texture_2", 1);
		gl.glActiveTexture(GL_TEXTURE0);
		batch.draw(
			fBufferQuantized.getColorBufferTexture(),
			0, 0, Graphics.getGdxWidth(), Graphics.getGdxHeight()
		);
		batch.end();
		combinedBufferQuantized.end();

		mBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		for (final Prop prop : world.props().getProps()) {
			if (prop.depth == MIDDLEGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render(graphics);
				batch.flush();
			}
		}
		renderParticles(MIDDLEGROUND, world);
		batch.end();
		mBuffer.end();

		fBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		individualTexture.setFilter(Linear, Linear);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		for (final Prop prop : world.props().getProps()) {
			if (prop.depth == FOREGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render(graphics);
				batch.flush();
			}
		}
		for (final Item item : world.items().getItems()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			item.render(graphics);
			batch.flush();
		}
		batch.end();
		individualTexture.setFilter(Nearest, Nearest);
		IndividualPlatformFilteringRenderer.renderIndividuals(world.getWorldId());
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		for (final Projectile projectile : world.projectiles().getProjectiles()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			projectile.render(batch);
			batch.flush();
		}
		renderParticles(Depth.FOREGROUND, world);
		batch.end();
		world.getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {}, graphics);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		for (final Prop prop : world.props().getProps()) {
			if (prop.depth == Depth.FRONT) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render(graphics);
				batch.flush();
			}
		}
		batch.end();
		fBuffer.end();

		GaussianLightingRenderer.render(camX, camY, world);
		gameClientStateTracker.setRendering(false);
	}


	private static void renderParticles(final Depth depth, final World world) {
		final SpriteBatch batch = graphics.getSpriteBatch();

		gl20.glEnable(GL20.GL_BLEND);
		gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		shapeRenderer.begin(Line);
		shapeRenderer.setProjectionMatrix(graphics.getCam().projection);
		shapeRenderer.setTransformMatrix(graphics.getCam().view);
		gl20.glEnable(GL11.GL_LINE_SMOOTH);
		if (world.getClientParticles() != null) {
			world.getClientParticles().stream().filter(p -> p.depth == depth).forEach(p -> {
				p.renderLine(Gdx.graphics.getDeltaTime());
			});
		}
		if (world.getServerParticles() != null) {
			world.getServerParticles().values().stream().filter(p -> p.depth == depth).forEach(p -> {
				p.renderLine(Gdx.graphics.getDeltaTime());
			});
		}
		gl20.glDisable(GL11.GL_LINE_SMOOTH);
		shapeRenderer.end();

		batch.setShader(Shaders.particleTexture);
		final int source = batch.getBlendSrcFunc();
		final int destination = batch.getBlendDstFunc();
		batch.setBlendFunction(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		Shaders.particleTexture.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		Shaders.particleTexture.setUniformf("feather", 0.3f);
		Shaders.particleTexture.setUniformf("topLeft", circle.getU(), circle.getV());
		Shaders.particleTexture.setUniformf("bottomRight", circle.getU2(), circle.getV2());
		if (world.getClientParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			world.getClientParticles().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime(), circle, graphics);
				batch.flush();
				counter.t++;
			});
		}
		if (world.getServerParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			world.getServerParticles().values().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime(), circle, graphics);
				batch.flush();
				counter.t++;
			});
		}
		batch.flush();
		batch.setBlendFunction(source, destination);

		gl20.glDisable(GL20.GL_BLEND);
	}


	/**
	 * Class to encapsulate the rendering of {@link Individual}s
	 *
	 * @author Matt
	 */
	private static class IndividualPlatformFilteringRenderer {


		/** {@link Predicate} for filtering out those that are NOT on platforms */
		private static Predicate<Individual> onPlatform = new Predicate<Individual>() {
			@Override
			public boolean apply(final Individual individual) {
				try {
					if (Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
						Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
						return true;
					} else {
						return false;
					}
				} catch (final NoTileFoundException e) {
					return false;
				}

			};
		};

		/** {@link Predicate} for filtering out those that ARE on platforms */
		private static Predicate<Individual> offPlatform = new Predicate<Individual>() {
			@Override
			public boolean apply(final Individual individual) {
				try {
					if (Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
						Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
						return false;
					} else {
						return true;
					}
				} catch (final NoTileFoundException e) {
					return false;
				}
			};
		};

		private static Comparator<Individual> renderPrioritySorter = (i1, i2) -> {
			return Integer.compare(getIndividualRenderPriority(i1), getIndividualRenderPriority(i2));
		};

		/** Renders all individuals, ones that are on platforms are rendered first */
		private static void renderIndividuals(final int worldId) {
			try {
				for (final Individual indi : filter(Domain.getSortedIndividualsForWorld(renderPrioritySorter, worldId), offPlatform)) {
					Renderer.render(indi, graphics);
				}

				for (final Individual indi : filter(Domain.getSortedIndividualsForWorld(renderPrioritySorter, worldId), onPlatform)) {
					Renderer.render(indi, graphics);
				}
			} catch (final NullPointerException e) {
				generalDebug("Nullpointer whilst rendering individual", LogLevel.INFO, e);
			}
		}
	}


	public enum Depth implements Serializable {
		BACKGROUND, FOREGROUND, MIDDLEGROUND, FRONT
	}


	private static int getIndividualRenderPriority(final Individual individual) {
		for (final Item equipped : individual.getEquipped().keySet()) {
			if (equipped instanceof Torch) {
				return 2;
			}
		}

		return factionControlService.isControllable(individual) ? 1 : 0;
	}
}
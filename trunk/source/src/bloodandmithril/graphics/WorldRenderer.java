package bloodandmithril.graphics;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginX;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginY;
import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
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

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.base.Predicate;

/**
 * Class for rendering {@link World}s
 *
 * @author Matt
 */
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


	public static void setup() {
		gameWorldTexture 					= new Texture(files.internal("data/image/gameWorld.png"));
		individualTexture 					= new Texture(files.internal("data/image/character/individual.png"));

		gameWorldTexture.setFilter(Linear, Nearest);
		individualTexture.setFilter(Nearest, Nearest);

		fBuffer 							= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		mBuffer 							= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		bBuffer 							= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		workingQuantized 					= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		bBufferQuantized 					= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		fBufferQuantized 					= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		combinedBufferQuantized 			= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);

		bBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	public static void render(World world, int camX, int camY) {
		bBuffer.begin();
		Shaders.invertAlphaSolidColor.begin();
		world.getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {});
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.pass.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == BACKGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.render();
				spriteBatch.flush();
			}
		}
		renderParticles(Depth.BACKGROUND, world);
		spriteBatch.end();
		bBuffer.end();

		int xOffset = round(cam.position.x) % TILE_SIZE;
		int yOffset = round(cam.position.y) % TILE_SIZE;

		workingQuantized.begin();
		cam.position.x = cam.position.x - xOffset;
		cam.position.y = cam.position.y - yOffset;
		cam.update();
		world.getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {});
		cam.position.x = cam.position.x + xOffset;
		cam.position.y = cam.position.y + yOffset;
		cam.update();
		workingQuantized.end();

		bBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 1.0f, 0.0f, 0.0f, 1.0f);
		spriteBatch.draw(workingQuantized.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
		spriteBatch.end();
		bBufferQuantized.end();

		workingQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		cam.position.x = cam.position.x - xOffset;
		cam.position.y = cam.position.y - yOffset;
		cam.update();
		world.getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {});
		cam.position.x = cam.position.x + xOffset;
		cam.position.y = cam.position.y + yOffset;
		cam.update();
		workingQuantized.end();

		fBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 0.0f, 1.0f, 0.0f, 1.0f);
		spriteBatch.draw(workingQuantized.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
		spriteBatch.end();
		fBufferQuantized.end();

		combinedBufferQuantized.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.invertAlphaSolidColorBlend);
		bBufferQuantized.getColorBufferTexture().bind(1);
		Shaders.invertAlphaSolidColorBlend.setUniformi("u_texture_2", 1);
		gl.glActiveTexture(GL_TEXTURE0);
		spriteBatch.draw(
			fBufferQuantized.getColorBufferTexture(),
			0, 0, WIDTH, HEIGHT
		);
		spriteBatch.end();
		combinedBufferQuantized.end();

		mBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == MIDDLEGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				spriteBatch.flush();
			}
		}
		renderParticles(MIDDLEGROUND, world);
		spriteBatch.end();
		mBuffer.end();

		fBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		individualTexture.setFilter(Linear, Linear);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == FOREGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				spriteBatch.flush();
			}
		}
		for (Item item : world.items().getItems()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			item.render();
			spriteBatch.flush();
		}
		spriteBatch.end();
		individualTexture.setFilter(Nearest, Nearest);
		IndividualPlatformFilteringRenderer.renderIndividuals();
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
		for (Projectile projectile : world.projectiles().getProjectiles()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			projectile.render();
			spriteBatch.flush();
		}
		renderParticles(Depth.FOREGROUND, world);
		spriteBatch.end();
		world.getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {});
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == Depth.FRONT) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				spriteBatch.flush();
			}
		}
		spriteBatch.end();
		fBuffer.end();

		GaussianLightingRenderer.render(camX, camY, world);
	}


	private static void renderParticles(Depth depth, World world) {
		gl20.glEnable(GL20.GL_BLEND);
		gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		shapeRenderer.begin(Line);
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
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
		shapeRenderer.end();
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
		if (world.getClientParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			world.getClientParticles().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime());
				counter.t++;
			});
		}
		if (world.getServerParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			world.getServerParticles().values().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime());
				counter.t++;
			});
		}
		shapeRenderer.end();
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
			public boolean apply(Individual individual) {
				try {
					if (Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
						Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
						return true;
					} else {
						return false;
					}
				} catch (NoTileFoundException e) {
					return false;
				}

			};
		};

		/** {@link Predicate} for filtering out those that ARE on platforms */
		private static Predicate<Individual> offPlatform = new Predicate<Individual>() {
			@Override
			public boolean apply(Individual individual) {
				try {
					if (Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
						Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
						return false;
					} else {
						return true;
					}
				} catch (NoTileFoundException e) {
					return false;
				}
			};
		};

		private static Comparator<Individual> renderPrioritySorter = (i1, i2) -> {
			return Integer.compare(i1.getRenderPriority(), i2.getRenderPriority());
		};

		/** Renders all individuals, ones that are on platforms are rendered first */
		private static void renderIndividuals() {
			try {
				for (Individual indi : filter(Domain.getSortedIndividuals(renderPrioritySorter), offPlatform)) {
					indi.render();
				}

				for (Individual indi : filter(Domain.getSortedIndividuals(renderPrioritySorter), onPlatform)) {
					indi.render();
				}
			} catch (NullPointerException e) {
				generalDebug("Nullpointer whilst rendering individual", LogLevel.INFO, e);
			}
		}
	}


	public enum Depth implements Serializable {
		BACKGROUND, FOREGROUND, MIDDLEGROUND, FRONT
	}
}
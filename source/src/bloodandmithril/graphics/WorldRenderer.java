package bloodandmithril.graphics;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.google.common.base.Predicate;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
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

	static {
		if (ClientServerInterface.isClient()) {
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
		fBuffer 							= new FrameBuffer(RGBA8888, getGraphics().getWidth() + getGraphics().getCamMarginX(), getGraphics().getHeight() + getGraphics().getCamMarginY(), false);
		mBuffer 							= new FrameBuffer(RGBA8888, getGraphics().getWidth() + getGraphics().getCamMarginX(), getGraphics().getHeight() + getGraphics().getCamMarginY(), false);
		bBuffer 							= new FrameBuffer(RGBA8888, getGraphics().getWidth() + getGraphics().getCamMarginX(), getGraphics().getHeight() + getGraphics().getCamMarginY(), false);
		workingQuantized 					= new FrameBuffer(RGBA8888, getGraphics().getWidth() + getGraphics().getCamMarginX(), getGraphics().getHeight() + getGraphics().getCamMarginY(), false);
		bBufferQuantized 					= new FrameBuffer(RGBA8888, getGraphics().getWidth() + getGraphics().getCamMarginX(), getGraphics().getHeight() + getGraphics().getCamMarginY(), false);
		fBufferQuantized 					= new FrameBuffer(RGBA8888, getGraphics().getWidth() + getGraphics().getCamMarginX(), getGraphics().getHeight() + getGraphics().getCamMarginY(), false);
		combinedBufferQuantized 			= new FrameBuffer(RGBA8888, getGraphics().getWidth() + getGraphics().getCamMarginX(), getGraphics().getHeight() + getGraphics().getCamMarginY(), false);

		bBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	public static void render(World world, int camX, int camY) {
		BloodAndMithrilClient.rendering.set(true);
		bBuffer.begin();
		Shaders.invertAlphaSolidColor.begin();
		world.getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {});
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.filter);
		Shaders.pass.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == BACKGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.render();
				getGraphics().getSpriteBatch().flush();
			}
		}
		renderParticles(Depth.BACKGROUND, world);
		getGraphics().getSpriteBatch().end();
		bBuffer.end();

		int xOffset = round(getGraphics().getCam().position.x) % TILE_SIZE;
		int yOffset = round(getGraphics().getCam().position.y) % TILE_SIZE;

		workingQuantized.begin();
		getGraphics().getCam().position.x = getGraphics().getCam().position.x - xOffset;
		getGraphics().getCam().position.y = getGraphics().getCam().position.y - yOffset;
		getGraphics().getCam().update();
		world.getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {});
		getGraphics().getCam().position.x = getGraphics().getCam().position.x + xOffset;
		getGraphics().getCam().position.y = getGraphics().getCam().position.y + yOffset;
		getGraphics().getCam().update();
		workingQuantized.end();

		bBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 1.0f, 0.0f, 0.0f, 1.0f);
		getGraphics().getSpriteBatch().draw(workingQuantized.getColorBufferTexture(), 0, 0, getGraphics().getWidth(), getGraphics().getHeight());
		getGraphics().getSpriteBatch().end();
		bBufferQuantized.end();

		workingQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getCam().position.x = getGraphics().getCam().position.x - xOffset;
		getGraphics().getCam().position.y = getGraphics().getCam().position.y - yOffset;
		getGraphics().getCam().update();
		world.getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {});
		getGraphics().getCam().position.x = getGraphics().getCam().position.x + xOffset;
		getGraphics().getCam().position.y = getGraphics().getCam().position.y + yOffset;
		getGraphics().getCam().update();
		workingQuantized.end();

		fBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 0.0f, 1.0f, 0.0f, 1.0f);
		getGraphics().getSpriteBatch().draw(workingQuantized.getColorBufferTexture(), 0, 0, getGraphics().getWidth(), getGraphics().getHeight());
		getGraphics().getSpriteBatch().end();
		fBufferQuantized.end();

		combinedBufferQuantized.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.invertAlphaSolidColorBlend);
		bBufferQuantized.getColorBufferTexture().bind(1);
		Shaders.invertAlphaSolidColorBlend.setUniformi("u_texture_2", 1);
		gl.glActiveTexture(GL_TEXTURE0);
		getGraphics().getSpriteBatch().draw(
			fBufferQuantized.getColorBufferTexture(),
			0, 0, getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		combinedBufferQuantized.end();

		mBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == MIDDLEGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				getGraphics().getSpriteBatch().flush();
			}
		}
		renderParticles(MIDDLEGROUND, world);
		getGraphics().getSpriteBatch().end();
		mBuffer.end();

		fBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		individualTexture.setFilter(Linear, Linear);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == FOREGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				getGraphics().getSpriteBatch().flush();
			}
		}
		for (Item item : world.items().getItems()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			item.render();
			getGraphics().getSpriteBatch().flush();
		}
		getGraphics().getSpriteBatch().end();
		individualTexture.setFilter(Nearest, Nearest);
		IndividualPlatformFilteringRenderer.renderIndividuals(world.getWorldId());
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
		for (Projectile projectile : world.projectiles().getProjectiles()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			projectile.render();
			getGraphics().getSpriteBatch().flush();
		}
		renderParticles(Depth.FOREGROUND, world);
		getGraphics().getSpriteBatch().end();
		world.getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {});
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
		for (Prop prop : world.props().getProps()) {
			if (prop.depth == Depth.FRONT) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				getGraphics().getSpriteBatch().flush();
			}
		}
		getGraphics().getSpriteBatch().end();
		fBuffer.end();

		GaussianLightingRenderer.render(camX, camY, world);
		BloodAndMithrilClient.rendering.set(false);
	}


	private static void renderParticles(Depth depth, World world) {
		gl20.glEnable(GL20.GL_BLEND);
		gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		shapeRenderer.begin(Line);
		shapeRenderer.setProjectionMatrix(getGraphics().getCam().projection);
		shapeRenderer.setTransformMatrix(getGraphics().getCam().view);
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

		getGraphics().getSpriteBatch().setShader(Shaders.particleTexture);
		int source = getGraphics().getSpriteBatch().getBlendSrcFunc();
		int destination = getGraphics().getSpriteBatch().getBlendDstFunc();
		getGraphics().getSpriteBatch().setBlendFunction(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		Shaders.particleTexture.setUniformMatrix("u_projTrans", getGraphics().getCam().combined);
		Shaders.particleTexture.setUniformf("feather", 0.3f);
		Shaders.particleTexture.setUniformf("topLeft", circle.getU(), circle.getV());
		Shaders.particleTexture.setUniformf("bottomRight", circle.getU2(), circle.getV2());
		if (world.getClientParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			world.getClientParticles().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime(), circle);
				getGraphics().getSpriteBatch().flush();
				counter.t++;
			});
		}
		if (world.getServerParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			world.getServerParticles().values().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime(), circle);
				getGraphics().getSpriteBatch().flush();
				counter.t++;
			});
		}
		getGraphics().getSpriteBatch().flush();
		getGraphics().getSpriteBatch().setBlendFunction(source, destination);

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
		private static void renderIndividuals(int worldId) {
			try {
				for (Individual indi : filter(Domain.getSortedIndividualsForWorld(renderPrioritySorter, worldId), offPlatform)) {
					Renderer.render(indi);
				}

				for (Individual indi : filter(Domain.getSortedIndividualsForWorld(renderPrioritySorter, worldId), onPlatform)) {
					Renderer.render(indi);
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
package bloodandmithril.graphics;

import static bloodandmithril.graphics.Graphics.isOnScreen;
import static bloodandmithril.graphics.WorldRenderer.Depth.BACKGROUND;
import static bloodandmithril.graphics.WorldRenderer.Depth.FOREGROUND;
import static bloodandmithril.graphics.WorldRenderer.Depth.MIDDLEGROUND;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;
import static java.lang.Math.round;

import java.io.Serializable;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.PropRenderer;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.TopographyRenderer;

/**
 * Class for rendering {@link World}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class WorldRenderer {

	/** {@link WorldRenderer}-specific {@link ShapeRenderer} */
	private static ShapeRenderer shapeRenderer;

	/** {@link TextureRegion} to use to render a circle */
	private static TextureRegion circle;

	/** The frame buffer used for tiles */
	public static FrameBuffer fBuffer;
	public static FrameBuffer mBuffer;
	public static FrameBuffer bBuffer;
	public static FrameBuffer workingQuantized;
	public static FrameBuffer bBufferQuantized;
	public static FrameBuffer fBufferQuantized;
	public static FrameBuffer combinedBufferQuantized;

	@Inject private Graphics graphics;
	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private PropRenderer propRenderer;
	@Inject private IndividualPlatformFilteringRenderer individualPlatformFilteringRenderer;
	@Inject private TopographyRenderer topographyRenderer;
	@Inject private GaussianLightingRenderer gaussianLightingRenderer;

	public void dispose() {
		fBuffer.dispose();
		mBuffer.dispose();
		bBuffer.dispose();
		workingQuantized.dispose();
		bBufferQuantized.dispose();
		fBufferQuantized.dispose();
		combinedBufferQuantized.dispose();
	}


	public void setup() {
		if (shapeRenderer == null) {
			shapeRenderer 						= new ShapeRenderer();
		}

		circle 								= new TextureRegion(Textures.GAME_WORLD_TEXTURE, 102, 422, 100, 100);
		fBuffer 							= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		mBuffer 							= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		bBuffer 							= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		workingQuantized 					= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		bBufferQuantized 					= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		fBufferQuantized 					= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);
		combinedBufferQuantized 			= new FrameBuffer(RGBA8888, Graphics.getGdxWidth() + graphics.getCamMarginX(), Graphics.getGdxHeight() + graphics.getCamMarginY(), false);

		bBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	public void render(final World world, final int camX, final int camY) {
		final SpriteBatch batch = graphics.getSpriteBatch();

		gameClientStateTracker.setRendering(true);
		renderBackgroundBuffer(world, camX, camY, batch);
		renderQuantizedBuffersForTileLighting(world, camX, camY, batch);
		renderMiddleGroundBuffer(world, batch);
		renderForeGroundBuffer(world, camX, camY, batch);

		// Renders all buffers using the final renderer
		gaussianLightingRenderer.render(camX, camY, world);
		gameClientStateTracker.setRendering(false);
	}


	private void renderForeGroundBuffer(final World world, final int camX, final int camY, final SpriteBatch batch) {
		fBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		Textures.INDIVIDUAL_TEXTURE.setFilter(Linear, Linear);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		renderProps(world, batch, FOREGROUND);

		Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
		world.getPositionalIndexMap().getOnScreenEntities(Item.class, graphics).stream().forEach(itemId -> {
			final Item item = world.items().getItem(itemId);
			if (item == null) {
				world.getPositionalIndexMap().getOnScreenNodes(graphics).forEach(node -> {
					node.removeItem(itemId);
				});
				return;
			}

			item.render(graphics);
			batch.flush();
		});

		batch.end();
		Textures.INDIVIDUAL_TEXTURE.setFilter(Nearest, Nearest);
		individualPlatformFilteringRenderer.renderIndividuals(world.getWorldId());
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
		for (final Projectile projectile : world.projectiles().getProjectiles()) {
			projectile.render(batch);
			batch.flush();
		}
		renderParticles(Depth.FOREGROUND, world);
		batch.end();
		topographyRenderer.renderForeGround(world.getTopography(), camX, camY, Shaders.pass, shader -> {}, graphics);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		renderProps(world, batch, Depth.FRONT);
		batch.end();
		fBuffer.end();
	}

	private void renderMiddleGroundBuffer(final World world, final SpriteBatch batch) {
		mBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		renderProps(world, batch, MIDDLEGROUND);
		renderParticles(MIDDLEGROUND, world);
		batch.end();
		mBuffer.end();
	}


	private void renderProps(final World world, final SpriteBatch batch, final Depth depth) {
		Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
		world.getPositionalIndexMap().getOnScreenEntities(Prop.class, graphics).stream().forEach(propId -> {
			final Prop prop = world.props().getProp(propId);

			if (prop == null) {
				world.getPositionalIndexMap().getOnScreenNodes(graphics).forEach(node -> {
					node.removeProp(propId);
				});
				return;
			}

			if (prop.depth == depth) {
				propRenderer.render(prop);
				batch.flush();
			}
		});
	}

	private void renderQuantizedBuffersForTileLighting(final World world, final int camX, final int camY, final SpriteBatch batch) {
		final int xOffset = round(graphics.getCam().position.x) % TILE_SIZE;
		final int yOffset = round(graphics.getCam().position.y) % TILE_SIZE;

		workingQuantized.begin();
		graphics.getCam().position.x = graphics.getCam().position.x - xOffset;
		graphics.getCam().position.y = graphics.getCam().position.y - yOffset;
		graphics.getCam().update();
		topographyRenderer.renderBackGround(world.getTopography(), camX, camY, Shaders.pass, shader -> {}, graphics);
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
		topographyRenderer.renderForeGround(world.getTopography(), camX, camY, Shaders.ignoreSlightTransparency, shader -> {}, graphics);
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
	}

	private void renderBackgroundBuffer(final World world, final int camX, final int camY, final SpriteBatch batch) {
		bBuffer.begin();
		Shaders.invertAlphaSolidColor.begin();
		topographyRenderer.renderBackGround(world.getTopography(), camX, camY, Shaders.pass, shader -> {}, graphics);
		batch.begin();
		batch.setShader(Shaders.filter);
		Shaders.pass.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		renderProps(world, batch, BACKGROUND);
		renderParticles(Depth.BACKGROUND, world);
		batch.end();
		bBuffer.end();
	}


	private void renderParticles(final Depth depth, final World world) {
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
			final Wrapper<Integer> counter = new Wrapper<>(0);
			world.getClientParticles().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime(), circle, graphics);
				batch.flush();
				counter.t++;
			});
		}
		if (world.getServerParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<>(0);
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
	 * @return the {@link WorldRenderer} specific {@link ShapeRenderer}
	 */
	public ShapeRenderer getShapeRenderer() {
		return shapeRenderer;
	}


	public enum Depth implements Serializable {
		BACKGROUND, FOREGROUND, MIDDLEGROUND, FRONT
	}
}
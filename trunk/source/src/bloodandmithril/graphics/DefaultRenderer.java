package bloodandmithril.graphics;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.camMargin;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL10.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.round;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class DefaultRenderer {

	public static FrameBuffer bBufferDownSampled;
	public static FrameBuffer bBufferDownSampledXBlurColorBuffer;
	public static FrameBuffer bBufferDownSampledYBlurColorBuffer;
	public static FrameBuffer backgroundFBO;
	public static FrameBuffer backgroundOcclusionFBO;

	/**
	 * Master render method.
	 */
	public static void render(float camX, float camY) {
		weather();
		background();
//		middleground();
//		foreground();
	}


	/**
	 * Loads framebuffers etc.
	 */
	public static void setup() {
		bBufferDownSampled = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		bBufferDownSampledXBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		bBufferDownSampledYBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		backgroundFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		backgroundOcclusionFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		bBufferDownSampled.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		bBufferDownSampledXBlurColorBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		bBufferDownSampledYBlurColorBuffer.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}


	private static void weather() {
		Weather.render();
	}


	private static void background() {
		backgroundLighting();

		backgroundFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(
			Domain.bBuffer.getColorBufferTexture(),
			-camMargin / 2,
			-camMargin / 2
		);
		spriteBatch.end();
		backgroundFBO.end();

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertYBlendWithOcclusion);
		backgroundOcclusionFBO.getColorBufferTexture().bind(1);
		Shaders.invertYBlendWithOcclusion.setUniformi("occlusion", 1);
		gl.glActiveTexture(GL_TEXTURE0);

		spriteBatch.draw(
			backgroundFBO.getColorBufferTexture(),
			0, 0
		);

//		spriteBatch.draw(
//			backgroundOcclusionFBO.getColorBufferTexture(),
//			0, 0
//		);

		spriteBatch.end();
	}


	/**
	 * Renders the background lighting control occlusion FBO
	 */
	private static void backgroundLighting() {
		// Step 1
		// Render the quantized background buffer to the 16x downsampled FBO
		bBufferDownSampled.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			Domain.bBufferQuantized.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		bBufferDownSampled.end();

		// Step 2
		// Apply x-blur to bBufferDownSampled
		bBufferDownSampledXBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmear);
		Shaders.colorSmear.setUniformf("res", bBufferDownSampled.getWidth(), bBufferDownSampled.getHeight());
		Shaders.colorSmear.setUniformf("dir", 1f, 0f);
		spriteBatch.draw(
			bBufferDownSampled.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		bBufferDownSampledXBlurColorBuffer.end();

		// Step 3
		// Apply y-blur to x-blurred bBufferDownSampledXBlur
		bBufferDownSampledYBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmear);
		Shaders.colorSmear.setUniformf("res", bBufferDownSampledXBlurColorBuffer.getWidth(), bBufferDownSampledXBlurColorBuffer.getHeight());
		Shaders.colorSmear.setUniformf("dir", 0f, 1f);
		spriteBatch.draw(
			bBufferDownSampledXBlurColorBuffer.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		bBufferDownSampledYBlurColorBuffer.end();

		backgroundOcclusionFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
//		spriteBatch.draw(
//			bBufferDownSampledYBlurColorBuffer.getColorBufferTexture(),
//			0,
//			0,
//			bBufferDownSampledYBlurColorBuffer.getWidth() * 4,
//			bBufferDownSampledYBlurColorBuffer.getHeight() * 4
//		);
		spriteBatch.draw(
			bBufferDownSampledYBlurColorBuffer.getColorBufferTexture(),
			-camMargin / 2 - round(cam.position.x) % TILE_SIZE,
			-camMargin / 2 - round(cam.position.y) % TILE_SIZE,
			bBufferDownSampledYBlurColorBuffer.getWidth() * TILE_SIZE,
			bBufferDownSampledYBlurColorBuffer.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		backgroundOcclusionFBO.end();
	}


	private static void middleground() {
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(Domain.mBuffer.getColorBufferTexture(), -camMargin / 2, -camMargin / 2);
		spriteBatch.end();
	}


	private static void foreground() {
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(Domain.fBuffer.getColorBufferTexture(), -camMargin / 2, -camMargin / 2);
		spriteBatch.end();
	}
}
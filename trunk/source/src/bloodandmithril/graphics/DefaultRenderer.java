package bloodandmithril.graphics;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.camMargin;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.round;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class DefaultRenderer {

	public static FrameBuffer bBufferDownSampled;
	public static FrameBuffer bBufferDownSampledXBlur;
	public static FrameBuffer bBufferDownSampledYBlur;
	public static FrameBuffer backgroundOcclusionFBO;

	/**
	 * Master render method.
	 */
	public static void render(float camX, float camY) {
		weather();
		background();
		middleground();
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

		bBufferDownSampledXBlur = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		bBufferDownSampledYBlur = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		backgroundOcclusionFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		bBufferDownSampled.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		bBufferDownSampledXBlur.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		bBufferDownSampledYBlur.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	private static void weather() {
//		Weather.render();
	}


	private static void background() {
		backgroundLighting();

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);

//		spriteBatch.draw(
//			Domain.bBuffer.getColorBufferTexture(),
//			-camMargin / 2,
//			-camMargin / 2
//		);

		spriteBatch.draw(
			backgroundOcclusionFBO.getColorBufferTexture(),
			0, 0
		);

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
		spriteBatch.setShader(Shaders.invertAlphaOcclusion);
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
		bBufferDownSampledXBlur.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.blur);
		Shaders.blur.setUniformf("res", bBufferDownSampled.getWidth(), bBufferDownSampled.getHeight());
		Shaders.blur.setUniformf("dir", 1f, 0f);
		spriteBatch.draw(
			bBufferDownSampled.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.flush();
		spriteBatch.end();
		bBufferDownSampledXBlur.end();

		// Step 3
		// Apply y-blur to x-blurred bBufferDownSampledXBlur
		bBufferDownSampledYBlur.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.blur);
		Shaders.blur.setUniformf("res", bBufferDownSampledXBlur.getWidth(), bBufferDownSampledXBlur.getHeight());
		Shaders.blur.setUniformf("dir", 0f, 1f);
		spriteBatch.draw(
			bBufferDownSampledXBlur.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		bBufferDownSampledYBlur.end();

		backgroundOcclusionFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(1f, 1f, 1f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			bBufferDownSampledYBlur.getColorBufferTexture(),
			-camMargin / 2 - (round(cam.position.x) % 16),
			-camMargin / 2 - (round(cam.position.y) % 16),
			bBufferDownSampledYBlur.getWidth() * 16,
			bBufferDownSampledYBlur.getHeight() * 16
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
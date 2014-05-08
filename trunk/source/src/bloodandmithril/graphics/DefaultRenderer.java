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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 * Class that encapsulates rendering things to the screen
 *
 * @author Matt
 */
public class DefaultRenderer {
	public static boolean SEE_ALL = false;

	public static FrameBuffer workingDownSampled;
	public static FrameBuffer workingDownSampledXBlurColorBuffer;
	public static FrameBuffer workingDownSampledYBlurColorBuffer;
	public static FrameBuffer backgroundOcclusionFBO;
	public static FrameBuffer foregroundOcclusionFBO;
	public static FrameBuffer workingFBO;

	/**
	 * Master render method.
	 */
	public static void render(float camX, float camY) {
		weather();
		backgroundLighting();
		foregroundLighting();
		background();
		middleground();
		foreground();
	}


	/**
	 * Loads framebuffers etc.
	 */
	public static void setup() {
		workingDownSampled = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		workingDownSampledXBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		workingDownSampledYBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMargin) / 16,
			(HEIGHT + camMargin) / 16,
			false
		);

		workingFBO = new FrameBuffer(
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

		foregroundOcclusionFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		workingDownSampledXBlurColorBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		workingDownSampledYBlurColorBuffer.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}


	private static void weather() {
		Weather.render();
	}


	/**
	 * Renders the background lighting control occlusion FBO
	 */
	private static void backgroundLighting() {
		// Step 1
		// Render the quantized background buffer to the 16x downsampled FBO
		workingDownSampled.begin();
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
		workingDownSampled.end();

		// Step 2
		// Apply x-blur to workingDownSampled
		workingDownSampledXBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampled.getWidth(), workingDownSampled.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 1f, 0f);
		spriteBatch.draw(
			workingDownSampled.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampledXBlurColorBuffer.end();

		// Step 3
		// Apply y-blur to x-blurred workingDownSampledXBlur
		workingDownSampledYBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampledXBlurColorBuffer.getWidth(), workingDownSampledXBlurColorBuffer.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 0f, 1f);
		spriteBatch.draw(
			workingDownSampledXBlurColorBuffer.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampledYBlurColorBuffer.end();

		backgroundOcclusionFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			workingDownSampledYBlurColorBuffer.getColorBufferTexture(),
			-camMargin / 2 - round(cam.position.x) % TILE_SIZE,
			-camMargin / 2 - round(cam.position.y) % TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getWidth() * TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		backgroundOcclusionFBO.end();
	}



	private static void foregroundLighting() {
		// Step 1
		// Render the quantized foreground buffer to the 16x downsampled FBO
		workingDownSampled.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			Domain.fBufferQuantized.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampled.end();

		// Step 2
		// Apply x-blur to workingDownSampled
		workingDownSampledXBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmearSmallRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampled.getWidth(), workingDownSampled.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 1f, 0f);
		spriteBatch.draw(
			workingDownSampled.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampledXBlurColorBuffer.end();

		// Step 3
		// Apply y-blur to x-blurred workingDownSampledXBlur
		workingDownSampledYBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmearSmallRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampledXBlurColorBuffer.getWidth(), workingDownSampledXBlurColorBuffer.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 0f, 1f);
		spriteBatch.draw(
			workingDownSampledXBlurColorBuffer.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampledYBlurColorBuffer.end();

		foregroundOcclusionFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			workingDownSampledYBlurColorBuffer.getColorBufferTexture(),
			-camMargin / 2 - round(cam.position.x) % TILE_SIZE,
			-camMargin / 2 - round(cam.position.y) % TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getWidth() * TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		foregroundOcclusionFBO.end();
	}


	private static void background() {
		workingFBO.begin();
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
		workingFBO.end();

		spriteBatch.begin();
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.invertY);
			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		} else {
			spriteBatch.setShader(Shaders.invertYBlendWithOcclusion);
			backgroundOcclusionFBO.getColorBufferTexture().bind(1);
			Color daylight = Weather.getDaylightColor();
			Shaders.invertYBlendWithOcclusion.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			Shaders.invertYBlendWithOcclusion.setUniformi("occlusion", 1);
			gl.glActiveTexture(GL_TEXTURE0);

			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}
		spriteBatch.end();
	}


	private static void middleground() {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(
			Domain.mBuffer.getColorBufferTexture(),
			-camMargin / 2,
			-camMargin / 2
		);
		spriteBatch.end();
		workingFBO.end();

		spriteBatch.begin();
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.invertY);
			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		} else {
			spriteBatch.setShader(Shaders.invertYDoubleBlendWithTwoOcclusions);
			Color daylight = Weather.getDaylightColor();
			Shaders.invertYBlendWithOcclusion.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			foregroundOcclusionFBO.getColorBufferTexture().bind(1);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformi("occlusion", 1);
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformi("occlusion2", 2);
			gl.glActiveTexture(GL_TEXTURE0);

			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}
		spriteBatch.end();
	}


	private static void foreground() {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(
			Domain.fBuffer.getColorBufferTexture(),
			-camMargin / 2,
			-camMargin / 2
		);
		spriteBatch.end();
		workingFBO.end();

		spriteBatch.begin();
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.invertY);
			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		} else {
			spriteBatch.setShader(Shaders.invertYDoubleBlendWithTwoOcclusions);
			foregroundOcclusionFBO.getColorBufferTexture().bind(1);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformi("occlusion", 1);
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformi("occlusion2", 2);
			gl.glActiveTexture(GL_TEXTURE0);

			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}
		spriteBatch.end();
	}
}
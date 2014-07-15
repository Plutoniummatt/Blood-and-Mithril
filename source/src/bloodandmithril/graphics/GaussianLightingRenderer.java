package bloodandmithril.graphics;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginX;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreen;
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
 * Class that encapsulates rendering things to the screen, lighting model is based on efficient large radius Gaussian blurred occlusion mapping
 *
 * @author Matt
 */
public class GaussianLightingRenderer {
	public static boolean SEE_ALL = false;

	public static FrameBuffer lightingFBO;
	public static FrameBuffer workingDownSampled;
	public static FrameBuffer workingDownSampledXBlurColorBuffer;
	public static FrameBuffer workingDownSampledYBlurColorBuffer;
	public static FrameBuffer workingDownSampledXBlurColorBuffer2;
	public static FrameBuffer workingDownSampledYBlurColorBuffer2;
	public static FrameBuffer backgroundOcclusionFBO;
	public static FrameBuffer backgroundOcclusionFBONearest;
	public static FrameBuffer backgroundShadowFBO;
	public static FrameBuffer foregroundOcclusionFBO;
	public static FrameBuffer foregroundShadowFBO;
	public static FrameBuffer workingFBO;

	/**
	 * Master render method.
	 */
	public static void render(float camX, float camY) {
		weather();
		lighting();
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
			(WIDTH + camMarginX) / 16,
			(HEIGHT + camMarginY) / 16,
			false
		);

		workingDownSampledXBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMarginX) / 16,
			(HEIGHT + camMarginY) / 16,
			false
		);

		workingDownSampledYBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMarginX) / 16,
			(HEIGHT + camMarginY) / 16,
			false
		);

		workingDownSampledXBlurColorBuffer2 = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMarginX) / 16,
			(HEIGHT + camMarginY) / 16,
			false
		);

		workingDownSampledYBlurColorBuffer2 = new FrameBuffer(
			RGBA8888,
			(WIDTH + camMarginX) / 16,
			(HEIGHT + camMarginY) / 16,
			false
		);

		lightingFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
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

		backgroundOcclusionFBONearest = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		backgroundShadowFBO = new FrameBuffer(
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

		foregroundShadowFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		workingDownSampledXBlurColorBuffer2.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		workingDownSampledXBlurColorBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		workingDownSampledYBlurColorBuffer2.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		workingDownSampledYBlurColorBuffer.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}


	private static void weather() {
		Weather.render();
	}


	/**
	 * Handles rendering to the lighting FBO.
	 */
	private static void lighting() {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		workingFBO.end();

		lightingFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.lightingFBO);
		Shaders.lightingFBO.begin();

		Domain.getActiveWorld().getParticles().stream().filter(p -> {
			return p.glowIntensity != 0f;
		}).forEach(p -> {
			Shaders.lightingFBO.setUniformf("intensity", p.glowIntensity);
			Shaders.lightingFBO.setUniformf("color", p.color);
			Shaders.lightingFBO.setUniformf("position", worldToScreen(p.position));
			Shaders.lightingFBO.setUniformf("resolution", WIDTH, HEIGHT);
			spriteBatch.draw(workingFBO.getColorBufferTexture(), 0, 0);
			spriteBatch.flush();
		});

		spriteBatch.end();
		lightingFBO.end();
	}


	/**
	 * Renders the background lighting control occlusion FBO
	 */
	private static void backgroundLighting() {
		// Step 1
		// Render the quantized background buffer to 16x downsampled FBO
		workingDownSampled.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			Domain.combinedBufferQuantized.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampled.end();

		// Step 2
		// Apply y-blur to workingDownSampled, attenuated by foreground
		workingDownSampledXBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampled.getWidth(), workingDownSampled.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 0f, 1f);
		spriteBatch.draw(
			workingDownSampled.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampledXBlurColorBuffer.end();

		// Step 3
		// Apply x-blur to y-blurred workingDownSampledXBlur, attenuated by foreground
		workingDownSampledYBlurColorBuffer.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampledXBlurColorBuffer.getWidth(), workingDownSampledXBlurColorBuffer.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 1f, 0f);
		spriteBatch.draw(
			workingDownSampledXBlurColorBuffer.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampledYBlurColorBuffer.end();

		// Step 4
		// Apply x-blur to another workingDownSampled, attenuated by foreground
		workingDownSampledXBlurColorBuffer2.begin();
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
		workingDownSampledXBlurColorBuffer2.end();

		// Step 5
		// Apply y-blur to x-blurred workingDownSampledXBlur, attenuated by foreground
		workingDownSampledYBlurColorBuffer2.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampledXBlurColorBuffer2.getWidth(), workingDownSampledXBlurColorBuffer2.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 0f, 1f);
		spriteBatch.draw(
			workingDownSampledXBlurColorBuffer2.getColorBufferTexture(),
			0,
			0,
			WIDTH, HEIGHT
		);
		spriteBatch.end();
		workingDownSampledYBlurColorBuffer2.end();

		backgroundOcclusionFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.blendXYandYXSmears);
		workingDownSampledYBlurColorBuffer2.getColorBufferTexture().bind(1);
		Shaders.blendXYandYXSmears.setUniformi("u_texture2", 1);
		gl.glActiveTexture(GL_TEXTURE0);
		spriteBatch.draw(
			workingDownSampledYBlurColorBuffer.getColorBufferTexture(),
			-camMarginX / 2 - round(cam.position.x) % TILE_SIZE,
			-camMarginY / 2 - round(cam.position.y) % TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getWidth() * TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		backgroundOcclusionFBO.end();

		// Process the shadow FBO - Upsample the downsampled background
		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		backgroundShadowFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			workingDownSampled.getColorBufferTexture(),
			-camMarginX / 2 - round(cam.position.x) % TILE_SIZE,
			-camMarginY / 2 - round(cam.position.y) % TILE_SIZE,
			workingDownSampled.getWidth() * TILE_SIZE,
			workingDownSampled.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		backgroundShadowFBO.end();
		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		backgroundOcclusionFBONearest.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			workingDownSampled.getColorBufferTexture(),
			-camMarginX / 2 - round(cam.position.x) % TILE_SIZE,
			-camMarginY / 2 - round(cam.position.y) % TILE_SIZE,
			workingDownSampled.getWidth() * TILE_SIZE,
			workingDownSampled.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		backgroundOcclusionFBONearest.end();
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
			Domain.combinedBufferQuantized.getColorBufferTexture(),
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
		Shaders.colorSmearSmallRadius.setUniformf("res", workingDownSampled.getWidth(), workingDownSampled.getHeight());
		Shaders.colorSmearSmallRadius.setUniformf("dir", 1f, 0f);
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
		Shaders.colorSmearSmallRadius.setUniformf("res", workingDownSampledXBlurColorBuffer.getWidth(), workingDownSampledXBlurColorBuffer.getHeight());
		Shaders.colorSmearSmallRadius.setUniformf("dir", 0f, 1f);
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
			-camMarginX / 2 - round(cam.position.x) % TILE_SIZE,
			-camMarginY / 2 - round(cam.position.y) % TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getWidth() * TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		foregroundOcclusionFBO.end();

		// Process the foreground drop shadow
		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		foregroundShadowFBO.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(
			workingDownSampled.getColorBufferTexture(),
			-camMarginX / 2 - round(cam.position.x) % TILE_SIZE,
			-camMarginY / 2 - round(cam.position.y) % TILE_SIZE,
			workingDownSampled.getWidth() * TILE_SIZE,
			workingDownSampled.getHeight() * TILE_SIZE
		);
		spriteBatch.end();
		foregroundShadowFBO.end();
		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	private static void background() {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(
			Domain.bBuffer.getColorBufferTexture(),
			-camMarginX / 2,
			-camMarginY / 2
		);
		spriteBatch.end();
		workingFBO.end();

		spriteBatch.begin();
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.invertY);
			spriteBatch.draw(
				backgroundShadowFBO.getColorBufferTexture(),
				0, 0
			);
		} else {
			spriteBatch.setShader(Shaders.invertYBlendWithOcclusionBackground);
			backgroundOcclusionFBO.getColorBufferTexture().bind(1);
			backgroundShadowFBO.getColorBufferTexture().bind(2);
			backgroundOcclusionFBO.getColorBufferTexture().bind(3);
			backgroundOcclusionFBONearest.getColorBufferTexture().bind(4);
			Color daylight = Weather.getDaylightColor();
			Shaders.invertYBlendWithOcclusionBackground.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			Shaders.invertYBlendWithOcclusionBackground.setUniformi("occlusion", 1);
			Shaders.invertYBlendWithOcclusionBackground.setUniformi("occlusion2", 2);
			Shaders.invertYBlendWithOcclusionBackground.setUniformi("occlusion3", 3);
			Shaders.invertYBlendWithOcclusionBackground.setUniformi("occlusion4", 4);
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
			-camMarginX / 2,
			-camMarginY / 2
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
			-camMarginX / 2,
			-camMarginY / 2
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
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			foregroundOcclusionFBO.getColorBufferTexture().bind(1);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			lightingFBO.getColorBufferTexture().bind(3);
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformi("occlusion", 1);
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformi("occlusion2", 2);
			Shaders.invertYDoubleBlendWithTwoOcclusions.setUniformi("occlusion3", 3);
			gl.glActiveTexture(GL_TEXTURE0);

			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}
		spriteBatch.end();
	}
}
package bloodandmithril.graphics;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginX;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginY;
import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.round;

import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.background.Layer;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.graphics.particles.TracerParticle;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.World;
import bloodandmithril.world.WorldState;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Class that encapsulates rendering things to the screen, lighting model is based on efficient large radius Gaussian blurred occlusion mapping
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GaussianLightingRenderer {
	public static boolean SEE_ALL = false;

	public static FrameBuffer foregroundLightingFBOSmall, middleGroundLightingFBOSmall, smallWorking;
	public static FrameBuffer foregroundLightingFBO, middleGroundLightingFBO;
	public static FrameBuffer workingDownSampled;
	public static FrameBuffer workingDownSampledXBlurColorBuffer;
	public static FrameBuffer workingDownSampledYBlurColorBuffer;
	public static FrameBuffer workingDownSampledXBlurColorBuffer2;
	public static FrameBuffer workingDownSampledYBlurColorBuffer2;
	public static FrameBuffer backgroundOcclusionFBO;
	public static FrameBuffer backgroundOcclusionFBONearest;
	public static FrameBuffer foregroundOcclusionFBO;
	public static FrameBuffer foregroundShadowFBO;
	public static FrameBuffer workingFBO, workingFBO2;

	public static final int MAX_PARTICLES = 100;
	private static final int LIGHTING_FBO_DOWNSIZE_SAMPLER = 6;

	/**
	 * Master render method.
	 */
	public static void render(float camX, float camY, World world) {
		weather();
		backgroundSprites(world);
		backgroundLighting();
		foregroundLighting();
		lighting(foregroundLightingFBOSmall, foregroundLightingFBO, Depth.FOREGROUND, world);
		lighting(middleGroundLightingFBOSmall, middleGroundLightingFBO, Depth.MIDDLEGROUND, world);
		background();
		middleground();
		foreground();
		volumetricLighting(world);
	}


	private static void backgroundSprites(World world) {
		workingFBO2.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		world.getBackgroundImages().renderBackground();
		workingFBO2.end();

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertYReflective);
		workingFBO.getColorBufferTexture().bind(14);
		Color daylightColor = Weather.getDaylightColor();
		Shaders.invertYReflective.setUniformf("filter", Weather.getSunColor().mul(new Color(daylightColor.r, daylightColor.r, daylightColor.r, 1f)));
		Shaders.invertYReflective.setUniformi("u_texture2", 14);
		Shaders.invertYReflective.setUniformf("time", WorldState.getCurrentEpoch().getTime() * 360f);
		Shaders.invertYReflective.setUniformf("horizon", (HEIGHT - (float) Layer.getScreenHorizonY()) / HEIGHT);
		Shaders.invertYReflective.setUniformf("resolution", WIDTH, HEIGHT);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		spriteBatch.draw(workingFBO2.getColorBufferTexture(), 0, 0);
		spriteBatch.end();
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

		foregroundLightingFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		workingFBO2 = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		middleGroundLightingFBO = new FrameBuffer(
			RGBA8888,
			WIDTH,
			HEIGHT,
			false
		);

		foregroundLightingFBOSmall = new FrameBuffer(
			RGBA8888,
			WIDTH/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			HEIGHT/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			false
		);

		middleGroundLightingFBOSmall = new FrameBuffer(
			RGBA8888,
			WIDTH/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			HEIGHT/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			false
		);

		smallWorking = new FrameBuffer(
			RGBA8888,
			WIDTH/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			HEIGHT/LIGHTING_FBO_DOWNSIZE_SAMPLER,
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
		middleGroundLightingFBOSmall.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		foregroundLightingFBOSmall.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}


	private static void weather() {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		workingFBO.end();
		Weather.render(workingFBO);

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(workingFBO.getColorBufferTexture(), 0, 0);
		spriteBatch.end();
	}


	/**
	 * Handles rendering to the lighting FBO.
	 */
	private static void lighting(FrameBuffer lightingFboSmall, FrameBuffer lightingFbo, Depth depth, World world) {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		workingFBO.end();

		lightingFboSmall.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		lightingFboSmall.end();

		int positionIndex = 0;
		int colorIndex = 0;
		int index = 0;
		float[] intensities = new float[MAX_PARTICLES];
		float[] currentPositions = new float[MAX_PARTICLES * 2];
		float[] previousPositions = new float[MAX_PARTICLES * 2];
		float[] colors = new float[MAX_PARTICLES * 4];

		List<Particle> clientSideGlowingTracerParticles = Lists.newLinkedList(Iterables.filter(world.getClientParticles(), p -> {
			return p.depth == depth && isOnScreen(p.position, 50f);
		}));

		List<Particle> serverSideGlowingTracerParticles = Lists.newLinkedList(Iterables.filter(world.getServerParticles().values(), p -> {
			return p.depth == depth && isOnScreen(p.position, 50f);
		}));

		List<List<Particle>> particleCollections = Lists.newLinkedList();
		for (int i = clientSideGlowingTracerParticles.size(); i > 0; i -= 100) {
			particleCollections.add(clientSideGlowingTracerParticles.subList(i - 100 < 0 ? 0 : i - 100, i));
		}

		for (int i = serverSideGlowingTracerParticles.size(); i > 0; i -= 100) {
			particleCollections.add(serverSideGlowingTracerParticles.subList(i - 100 < 0 ? 0 : i - 100, i));
		}

		for (List<Particle> collection : particleCollections) {
			smallWorking.begin();
			spriteBatch.begin();
			spriteBatch.setShader(Shaders.tracerParticlesFBO);
			Shaders.tracerParticlesFBO.begin();
			Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			for (Particle p : collection) {
				if (index == MAX_PARTICLES) {
					break;
				}
				if (p instanceof TracerParticle && ((TracerParticle) p).glowIntensity != 0f) {
					currentPositions[positionIndex] = worldToScreenX(p.position.x);
					currentPositions[positionIndex + 1] = worldToScreenY(p.position.y);

					previousPositions[positionIndex] = worldToScreenX(((TracerParticle) p).prevPosition.x);
					previousPositions[positionIndex + 1] = worldToScreenY(((TracerParticle) p).prevPosition.y);

					colors[colorIndex] = ((TracerParticle) p).glowColow.r;
					colors[colorIndex + 1] = ((TracerParticle) p).glowColow.g;
					colors[colorIndex + 2] = ((TracerParticle) p).glowColow.b;
					colors[colorIndex + 3] = ((TracerParticle) p).glowColow.a;

					intensities[index] = ((TracerParticle) p).glowIntensity;

					index ++;
					positionIndex += 2;
					colorIndex += 4;
				}
			}

			Shaders.tracerParticlesFBO.setUniformf("resolution", WIDTH, HEIGHT);
			Shaders.tracerParticlesFBO.setUniform1fv("intensity[0]", intensities, 0, MAX_PARTICLES);
			Shaders.tracerParticlesFBO.setUniform2fv("currentPosition[0]", currentPositions, 0, MAX_PARTICLES * 2);
			Shaders.tracerParticlesFBO.setUniform2fv("previousPosition[0]", previousPositions, 0, MAX_PARTICLES * 2);
			Shaders.tracerParticlesFBO.setUniform4fv("color[0]", colors, 0, MAX_PARTICLES * 4);
			spriteBatch.draw(lightingFboSmall.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
			spriteBatch.end();
			smallWorking.end();

			lightingFboSmall.begin();
			spriteBatch.begin();
			spriteBatch.setShader(Shaders.invertY);
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			spriteBatch.draw(smallWorking.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
			spriteBatch.end();
			lightingFboSmall.end();

			index = 0;
			positionIndex = 0;
			colorIndex = 0;
		}



		lightingFbo.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(lightingFboSmall.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
		spriteBatch.end();
		lightingFbo.end();
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
			WorldRenderer.combinedBufferQuantized.getColorBufferTexture(),
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
			WorldRenderer.combinedBufferQuantized.getColorBufferTexture(),
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
			WorldRenderer.bBuffer.getColorBufferTexture(),
			-camMarginX / 2,
			-camMarginY / 2
		);
		spriteBatch.end();
		workingFBO.end();

		spriteBatch.begin();
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.invertY);
		} else {
			spriteBatch.setShader(Shaders.backgroundShader);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			backgroundOcclusionFBONearest.getColorBufferTexture().bind(3);
			foregroundLightingFBO.getColorBufferTexture().bind(4);
			middleGroundLightingFBO.getColorBufferTexture().bind(8);
			Color daylight = Weather.getDaylightColor();
			Shaders.backgroundShader.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			Shaders.backgroundShader.setUniformi("occlusion3", 2);
			Shaders.backgroundShader.setUniformi("occlusion4", 3);
			Shaders.backgroundShader.setUniformi("occlusion5", 4);
			Shaders.backgroundShader.setUniformi("mgLighting", 8);
			Shaders.backgroundShader.setUniformf("height", HEIGHT);
			gl.glActiveTexture(GL_TEXTURE0);

			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}
		spriteBatch.end();
	}


	private static void middleground() {
		Gdx.gl20.glEnable(GL20.GL_DITHER);
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(
			WorldRenderer.mBuffer.getColorBufferTexture(),
			-camMarginX / 2,
			-camMarginY / 2
		);
		spriteBatch.end();
		workingFBO.end();

		spriteBatch.begin();
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.invertY);
		} else {
			spriteBatch.setShader(Shaders.foregroundShader);
			Color daylight = Weather.getDaylightColor();
			Shaders.invertYBlendWithOcclusion.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			foregroundOcclusionFBO.getColorBufferTexture().bind(1);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			middleGroundLightingFBO.getColorBufferTexture().bind(8);
			Shaders.foregroundShader.setUniformi("occlusion", 1);
			Shaders.foregroundShader.setUniformi("occlusion2", 2);
			Shaders.foregroundShader.setUniformi("occlusion3", 8);
			gl.glActiveTexture(GL_TEXTURE0);

			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}

		spriteBatch.setShader(Shaders.lightingFBOBlend);
		Shaders.lightingFBOBlend.setUniformf("color", 1f, 1f, 1f, 0.45f);
		spriteBatch.draw(
			middleGroundLightingFBO.getColorBufferTexture(),
			0, 0
		);

		spriteBatch.end();
	}


	private static void foreground() {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(
			WorldRenderer.fBuffer.getColorBufferTexture(),
			-camMarginX / 2,
			-camMarginY / 2
		);
		spriteBatch.end();
		workingFBO.end();

		spriteBatch.begin();
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.invertY);
		} else {
			spriteBatch.setShader(Shaders.foregroundShader);
			Color daylight = Weather.getDaylightColor();
			Shaders.foregroundShader.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			foregroundOcclusionFBO.getColorBufferTexture().bind(1);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			foregroundLightingFBO.getColorBufferTexture().bind(7);
			middleGroundLightingFBO.getColorBufferTexture().bind(8);
			Shaders.foregroundShader.setUniformi("occlusion", 1);
			Shaders.foregroundShader.setUniformi("occlusion2", 2);
			Shaders.foregroundShader.setUniformi("occlusion3", 7);
			Shaders.foregroundShader.setUniformi("mgLighting", 8);
			gl.glActiveTexture(GL_TEXTURE0);

			spriteBatch.draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}

		spriteBatch.setShader(Shaders.lightingFBOBlend);
		Shaders.lightingFBOBlend.setUniformf("color", 1f, 1f, 1f, 0.35f);
		spriteBatch.draw(
			foregroundLightingFBO.getColorBufferTexture(),
			0, 0
		);
		spriteBatch.end();
	}


	private static void volumetricLighting(World world) {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		world.getBackgroundImages().renderBackground();
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertY);
		spriteBatch.draw(
			WorldRenderer.fBuffer.getColorBufferTexture(),
			-camMarginX / 2,
			-camMarginY / 2
		);
		spriteBatch.draw(
			WorldRenderer.mBuffer.getColorBufferTexture(),
			-camMarginX / 2,
			-camMarginY / 2
		);
		spriteBatch.draw(
			WorldRenderer.bBuffer.getColorBufferTexture(),
			-camMarginX / 2,
			-camMarginY / 2
		);
		spriteBatch.end();
		workingFBO.end();

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.volumetricLighting);
		Color daylightColor = Weather.getDaylightColor();
		Vector3 rgb = new Vector3(daylightColor.r, daylightColor.g, daylightColor.b);
		rgb.nor().scl(1.7f);

		Shaders.volumetricLighting.setUniformf("color", rgb.x, rgb.y, rgb.z, daylightColor.r);
		Shaders.volumetricLighting.setUniformf("resolution", WIDTH, HEIGHT);
		Shaders.volumetricLighting.setUniformf("time", WorldState.getCurrentEpoch().getTime() * 300f);
		Shaders.volumetricLighting.setUniformf("sourceLocation", Weather.getSunPosition().cpy().x, Weather.getSunPosition().cpy().y);
		spriteBatch.draw(
			workingFBO.getColorBufferTexture(),
			0, 0
		);
		spriteBatch.end();
	}
}
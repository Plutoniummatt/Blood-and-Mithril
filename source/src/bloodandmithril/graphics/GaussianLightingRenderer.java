package bloodandmithril.graphics;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.round;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.background.Layer;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.graphics.particles.TracerParticle;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.World;
import bloodandmithril.world.weather.WeatherRenderer;

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
		weather(world);
		backgroundSprites(world);
		backgroundLighting();
		foregroundLighting();
		lighting(foregroundLightingFBOSmall, foregroundLightingFBO, Depth.FOREGROUND, world);
		lighting(middleGroundLightingFBOSmall, middleGroundLightingFBO, Depth.MIDDLEGROUND, world);
		background(world);
		middleground(world);
		foreground(world);
		volumetricLighting(world);
	}


	private static void backgroundSprites(World world) {
		workingFBO2.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		world.getBackgroundImages().renderBackground();
		workingFBO2.end();

		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertYReflective);
		workingFBO.getColorBufferTexture().bind(14);
		Color daylightColor = WeatherRenderer.getDaylightColor(world);
		Shaders.invertYReflective.setUniformf("filter", WeatherRenderer.getSunColor(world).mul(new Color(daylightColor.r, daylightColor.r, daylightColor.r, 1f)));
		Shaders.invertYReflective.setUniformi("u_texture2", 14);
		Shaders.invertYReflective.setUniformf("time", world.getEpoch().getTime() * 360f);
		Shaders.invertYReflective.setUniformf("horizon", (getGraphics().getHeight() - (float) Layer.getScreenHorizonY()) / getGraphics().getHeight());
		Shaders.invertYReflective.setUniformf("resolution", getGraphics().getWidth(), getGraphics().getHeight());
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		getGraphics().getSpriteBatch().draw(workingFBO2.getColorBufferTexture(), 0, 0);
		getGraphics().getSpriteBatch().end();
	}


	public static void dispose() {
		foregroundLightingFBOSmall.dispose();
		middleGroundLightingFBOSmall.dispose();
		smallWorking.dispose();
		foregroundLightingFBO.dispose();
		middleGroundLightingFBO.dispose();
		workingDownSampled.dispose();
		workingDownSampledXBlurColorBuffer.dispose();
		workingDownSampledYBlurColorBuffer.dispose();
		workingDownSampledXBlurColorBuffer2.dispose();
		workingDownSampledYBlurColorBuffer2.dispose();
		backgroundOcclusionFBO.dispose();
		backgroundOcclusionFBONearest.dispose();
		foregroundOcclusionFBO.dispose();
		foregroundShadowFBO.dispose();
		workingFBO.dispose();
		workingFBO2.dispose();
	}


	/**
	 * Loads framebuffers etc.
	 */
	public static void setup() {
		workingDownSampled = new FrameBuffer(
			RGBA8888,
			(getGraphics().getWidth() + getGraphics().getCamMarginX()) / 16,
			(getGraphics().getHeight() + getGraphics().getCamMarginY()) / 16,
			false
		);

		workingDownSampledXBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(getGraphics().getWidth() + getGraphics().getCamMarginX()) / 16,
			(getGraphics().getHeight() + getGraphics().getCamMarginY()) / 16,
			false
		);

		workingDownSampledYBlurColorBuffer = new FrameBuffer(
			RGBA8888,
			(getGraphics().getWidth() + getGraphics().getCamMarginX()) / 16,
			(getGraphics().getHeight() + getGraphics().getCamMarginY()) / 16,
			false
		);

		workingDownSampledXBlurColorBuffer2 = new FrameBuffer(
			RGBA8888,
			(getGraphics().getWidth() + getGraphics().getCamMarginX()) / 16,
			(getGraphics().getHeight() + getGraphics().getCamMarginY()) / 16,
			false
		);

		workingDownSampledYBlurColorBuffer2 = new FrameBuffer(
			RGBA8888,
			(getGraphics().getWidth() + getGraphics().getCamMarginX()) / 16,
			(getGraphics().getHeight() + getGraphics().getCamMarginY()) / 16,
			false
		);

		foregroundLightingFBO = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
			false
		);

		workingFBO2 = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
			false
		);

		middleGroundLightingFBO = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
			false
		);

		foregroundLightingFBOSmall = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth()/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			getGraphics().getHeight()/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			false
		);

		middleGroundLightingFBOSmall = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth()/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			getGraphics().getHeight()/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			false
		);

		smallWorking = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth()/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			getGraphics().getHeight()/LIGHTING_FBO_DOWNSIZE_SAMPLER,
			false
		);

		workingFBO = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
			false
		);

		backgroundOcclusionFBO = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
			false
		);

		backgroundOcclusionFBONearest = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
			false
		);

		foregroundOcclusionFBO = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
			false
		);

		foregroundShadowFBO = new FrameBuffer(
			RGBA8888,
			getGraphics().getWidth(),
			getGraphics().getHeight(),
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


	private static void weather(World world) {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		workingFBO.end();
		WeatherRenderer.render(workingFBO, world);


		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		getGraphics().getSpriteBatch().draw(workingFBO.getColorBufferTexture(), 0, 0);
		getGraphics().getSpriteBatch().flush();
		getGraphics().getSpriteBatch().setShader(Shaders.invertYFilter);
		Shaders.invertYFilter.setUniformf("color", WeatherRenderer.getSunColor(world));
		getGraphics().getSpriteBatch().draw(WorldRenderer.cloudBuffer.getColorBufferTexture(), 0, 0);
		getGraphics().getSpriteBatch().end();
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
			if (p instanceof TracerParticle) {
				return p.depth == depth && isOnScreen(p.position, ((TracerParticle) p).glowIntensity * 10f);
			} else {
				return p.depth == depth && isOnScreen(p.position, 50f);
			}
		}));

		List<Particle> serverSideGlowingTracerParticles = Lists.newLinkedList(Iterables.filter(world.getServerParticles().values(), p -> {
			if (p instanceof TracerParticle) {
				return p.depth == depth && isOnScreen(p.position, ((TracerParticle) p).glowIntensity * 10f);
			} else {
				return p.depth == depth && isOnScreen(p.position, 50f);
			}
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
			getGraphics().getSpriteBatch().begin();
			getGraphics().getSpriteBatch().setShader(Shaders.tracerParticlesFBO);
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

			Shaders.tracerParticlesFBO.setUniformf("resolution", getGraphics().getWidth(), getGraphics().getHeight());
			Shaders.tracerParticlesFBO.setUniform1fv("intensity[0]", intensities, 0, MAX_PARTICLES);
			Shaders.tracerParticlesFBO.setUniform2fv("currentPosition[0]", currentPositions, 0, MAX_PARTICLES * 2);
			Shaders.tracerParticlesFBO.setUniform2fv("previousPosition[0]", previousPositions, 0, MAX_PARTICLES * 2);
			Shaders.tracerParticlesFBO.setUniform4fv("color[0]", colors, 0, MAX_PARTICLES * 4);
			getGraphics().getSpriteBatch().draw(lightingFboSmall.getColorBufferTexture(), 0, 0, getGraphics().getWidth(), getGraphics().getHeight());
			getGraphics().getSpriteBatch().end();
			smallWorking.end();

			lightingFboSmall.begin();
			getGraphics().getSpriteBatch().begin();
			getGraphics().getSpriteBatch().setShader(Shaders.invertY);
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			getGraphics().getSpriteBatch().draw(smallWorking.getColorBufferTexture(), 0, 0, getGraphics().getWidth(), getGraphics().getHeight());
			getGraphics().getSpriteBatch().end();
			lightingFboSmall.end();

			index = 0;
			positionIndex = 0;
			colorIndex = 0;
		}



		lightingFbo.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		getGraphics().getSpriteBatch().draw(lightingFboSmall.getColorBufferTexture(), 0, 0, getGraphics().getWidth(), getGraphics().getHeight());
		getGraphics().getSpriteBatch().end();
		lightingFbo.end();
	}


	/**
	 * Renders the background lighting control occlusion FBO
	 */
	private static void backgroundLighting() {
		// Step 1
		// Render the quantized background buffer to 16x downsampled FBO
		workingDownSampled.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.pass);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.combinedBufferQuantized.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampled.end();

		// Step 2
		// Apply y-blur to workingDownSampled, attenuated by foreground
		workingDownSampledXBlurColorBuffer.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampled.getWidth(), workingDownSampled.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 0f, 1f);
		getGraphics().getSpriteBatch().draw(
			workingDownSampled.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampledXBlurColorBuffer.end();

		// Step 3
		// Apply x-blur to y-blurred workingDownSampledXBlur, attenuated by foreground
		workingDownSampledYBlurColorBuffer.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampledXBlurColorBuffer.getWidth(), workingDownSampledXBlurColorBuffer.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 1f, 0f);
		getGraphics().getSpriteBatch().draw(
			workingDownSampledXBlurColorBuffer.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampledYBlurColorBuffer.end();

		// Step 4
		// Apply x-blur to another workingDownSampled, attenuated by foreground
		workingDownSampledXBlurColorBuffer2.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampled.getWidth(), workingDownSampled.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 1f, 0f);
		getGraphics().getSpriteBatch().draw(
			workingDownSampled.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampledXBlurColorBuffer2.end();

		// Step 5
		// Apply y-blur to x-blurred workingDownSampledXBlur, attenuated by foreground
		workingDownSampledYBlurColorBuffer2.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.colorSmearLargeRadius);
		Shaders.colorSmearLargeRadius.setUniformf("res", workingDownSampledXBlurColorBuffer2.getWidth(), workingDownSampledXBlurColorBuffer2.getHeight());
		Shaders.colorSmearLargeRadius.setUniformf("dir", 0f, 1f);
		getGraphics().getSpriteBatch().draw(
			workingDownSampledXBlurColorBuffer2.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampledYBlurColorBuffer2.end();

		backgroundOcclusionFBO.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.blendXYandYXSmears);
		workingDownSampledYBlurColorBuffer2.getColorBufferTexture().bind(1);
		Shaders.blendXYandYXSmears.setUniformi("u_texture2", 1);
		gl.glActiveTexture(GL_TEXTURE0);
		getGraphics().getSpriteBatch().draw(
			workingDownSampledYBlurColorBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2 - round(getGraphics().getCam().position.x) % TILE_SIZE,
			-getGraphics().getCamMarginY() / 2 - round(getGraphics().getCam().position.y) % TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getWidth() * TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getHeight() * TILE_SIZE
		);
		getGraphics().getSpriteBatch().end();
		backgroundOcclusionFBO.end();

		backgroundOcclusionFBONearest.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.pass);
		getGraphics().getSpriteBatch().draw(
			workingDownSampled.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2 - round(getGraphics().getCam().position.x) % TILE_SIZE,
			-getGraphics().getCamMarginY() / 2 - round(getGraphics().getCam().position.y) % TILE_SIZE,
			workingDownSampled.getWidth() * TILE_SIZE,
			workingDownSampled.getHeight() * TILE_SIZE
		);
		getGraphics().getSpriteBatch().end();
		backgroundOcclusionFBONearest.end();
	}



	private static void foregroundLighting() {
		// Step 1
		// Render the quantized foreground buffer to the 16x downsampled FBO
		workingDownSampled.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.pass);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.combinedBufferQuantized.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampled.end();

		// Step 2
		// Apply x-blur to workingDownSampled
		workingDownSampledXBlurColorBuffer.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.colorSmearSmallRadius);
		Shaders.colorSmearSmallRadius.setUniformf("res", workingDownSampled.getWidth(), workingDownSampled.getHeight());
		Shaders.colorSmearSmallRadius.setUniformf("dir", 1f, 0f);
		getGraphics().getSpriteBatch().draw(
			workingDownSampled.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampledXBlurColorBuffer.end();

		// Step 3
		// Apply y-blur to x-blurred workingDownSampledXBlur
		workingDownSampledYBlurColorBuffer.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.colorSmearSmallRadius);
		Shaders.colorSmearSmallRadius.setUniformf("res", workingDownSampledXBlurColorBuffer.getWidth(), workingDownSampledXBlurColorBuffer.getHeight());
		Shaders.colorSmearSmallRadius.setUniformf("dir", 0f, 1f);
		getGraphics().getSpriteBatch().draw(
			workingDownSampledXBlurColorBuffer.getColorBufferTexture(),
			0,
			0,
			getGraphics().getWidth(), getGraphics().getHeight()
		);
		getGraphics().getSpriteBatch().end();
		workingDownSampledYBlurColorBuffer.end();

		foregroundOcclusionFBO.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.pass);
		getGraphics().getSpriteBatch().draw(
			workingDownSampledYBlurColorBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2 - round(getGraphics().getCam().position.x) % TILE_SIZE,
			-getGraphics().getCamMarginY() / 2 - round(getGraphics().getCam().position.y) % TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getWidth() * TILE_SIZE,
			workingDownSampledYBlurColorBuffer.getHeight() * TILE_SIZE
		);
		getGraphics().getSpriteBatch().end();
		foregroundOcclusionFBO.end();

		// Process the foreground drop shadow
		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		foregroundShadowFBO.begin();
		getGraphics().getSpriteBatch().begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().setShader(Shaders.pass);
		getGraphics().getSpriteBatch().draw(
			workingDownSampled.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2 - round(getGraphics().getCam().position.x) % TILE_SIZE,
			-getGraphics().getCamMarginY() / 2 - round(getGraphics().getCam().position.y) % TILE_SIZE,
			workingDownSampled.getWidth() * TILE_SIZE,
			workingDownSampled.getHeight() * TILE_SIZE
		);
		getGraphics().getSpriteBatch().end();
		foregroundShadowFBO.end();
		workingDownSampled.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	private static void background(World world) {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.bBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2,
			-getGraphics().getCamMarginY() / 2
		);
		getGraphics().getSpriteBatch().end();
		workingFBO.end();

		getGraphics().getSpriteBatch().begin();
		if (SEE_ALL) {
			getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		} else {
			getGraphics().getSpriteBatch().setShader(Shaders.backgroundShader);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			backgroundOcclusionFBONearest.getColorBufferTexture().bind(3);
			foregroundLightingFBO.getColorBufferTexture().bind(4);
			middleGroundLightingFBO.getColorBufferTexture().bind(8);
			Color daylight = WeatherRenderer.getDaylightColor(world);
			Shaders.backgroundShader.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			Shaders.backgroundShader.setUniformi("occlusion3", 2);
			Shaders.backgroundShader.setUniformi("occlusion4", 3);
			Shaders.backgroundShader.setUniformi("occlusion5", 4);
			Shaders.backgroundShader.setUniformi("mgLighting", 8);
			Shaders.backgroundShader.setUniformf("height", getGraphics().getHeight());
			gl.glActiveTexture(GL_TEXTURE0);

			getGraphics().getSpriteBatch().draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}
		getGraphics().getSpriteBatch().end();
	}


	private static void middleground(World world) {
		Gdx.gl20.glEnable(GL20.GL_DITHER);
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.mBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2,
			-getGraphics().getCamMarginY() / 2
		);
		getGraphics().getSpriteBatch().end();
		workingFBO.end();

		getGraphics().getSpriteBatch().begin();
		if (SEE_ALL) {
			getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		} else {
			getGraphics().getSpriteBatch().setShader(Shaders.foregroundShader);
			Color daylight = WeatherRenderer.getDaylightColor(world);
			Shaders.invertYBlendWithOcclusion.setUniformf("dayLightColor", daylight.r, daylight.g, daylight.b, 1.0f);
			foregroundOcclusionFBO.getColorBufferTexture().bind(1);
			backgroundOcclusionFBO.getColorBufferTexture().bind(2);
			middleGroundLightingFBO.getColorBufferTexture().bind(8);
			Shaders.foregroundShader.setUniformi("occlusion", 1);
			Shaders.foregroundShader.setUniformi("occlusion2", 2);
			Shaders.foregroundShader.setUniformi("occlusion3", 8);
			gl.glActiveTexture(GL_TEXTURE0);

			getGraphics().getSpriteBatch().draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}

		getGraphics().getSpriteBatch().setShader(Shaders.lightingFBOBlend);
		Shaders.lightingFBOBlend.setUniformf("color", 1f, 1f, 1f, 0.45f);
		getGraphics().getSpriteBatch().draw(
			middleGroundLightingFBO.getColorBufferTexture(),
			0, 0
		);

		getGraphics().getSpriteBatch().end();
	}


	private static void foreground(World world) {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.fBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2,
			-getGraphics().getCamMarginY() / 2
		);
		getGraphics().getSpriteBatch().end();
		workingFBO.end();

		getGraphics().getSpriteBatch().begin();
		if (SEE_ALL) {
			getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		} else {
			getGraphics().getSpriteBatch().setShader(Shaders.foregroundShader);
			Color daylight = WeatherRenderer.getDaylightColor(world);
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

			getGraphics().getSpriteBatch().draw(
				workingFBO.getColorBufferTexture(),
				0, 0
			);
		}

		getGraphics().getSpriteBatch().setShader(Shaders.lightingFBOBlend);
		Shaders.lightingFBOBlend.setUniformf("color", 1f, 1f, 1f, 0.35f);
		getGraphics().getSpriteBatch().draw(
			foregroundLightingFBO.getColorBufferTexture(),
			0, 0
		);
		getGraphics().getSpriteBatch().end();
	}


	private static void volumetricLighting(World world) {
		workingFBO.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		world.getBackgroundImages().renderBackground();
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.invertY);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.fBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2,
			-getGraphics().getCamMarginY() / 2
		);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.mBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2,
			-getGraphics().getCamMarginY() / 2
		);
		getGraphics().getSpriteBatch().draw(
			WorldRenderer.bBuffer.getColorBufferTexture(),
			-getGraphics().getCamMarginX() / 2,
			-getGraphics().getCamMarginY() / 2
		);
		getGraphics().getSpriteBatch().end();
		workingFBO.end();

		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.volumetricLighting);
		Color daylightColor = WeatherRenderer.getDaylightColor(world);
		Vector3 rgb = new Vector3(daylightColor.r, daylightColor.g, daylightColor.b);
		rgb.nor().scl(1.7f);

		Shaders.volumetricLighting.setUniformf("color", rgb.x, rgb.y, rgb.z, daylightColor.r);
		Shaders.volumetricLighting.setUniformf("resolution", getGraphics().getWidth(), getGraphics().getHeight());
		Shaders.volumetricLighting.setUniformf("time", world.getEpoch().getTime() * 300f);
		Shaders.volumetricLighting.setUniformf("sourceLocation", WeatherRenderer.getSunPosition().cpy().x, WeatherRenderer.getSunPosition().cpy().y);
		getGraphics().getSpriteBatch().draw(
			workingFBO.getColorBufferTexture(),
			0, 0
		);
		getGraphics().getSpriteBatch().end();
	}
}
package bloodandmithril.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Class representing the game world.
 *
 * @author Matt
 */
public class GameWorld {

	/** Gravity */
	public static float GRAVITY = 1200;

	/** Topography of the game world */
	public static Topography topography;

	/** Lights */
	public static ConcurrentHashMap<Integer, Light> lights = new ConcurrentHashMap<>();

	/** {@link Individual} that are selected for manual control */
	public static HashSet<Individual> selectedIndividuals = new HashSet<Individual>();

	/** Every {@link Individual} that exists */
	public static ConcurrentHashMap<Integer, Individual> individuals = new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	public static ConcurrentHashMap<Integer, Prop> props = new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	public static ConcurrentHashMap<Integer, Faction> factions = new ConcurrentHashMap<>();

	/** Textures */
	public static Texture gameWorldTexture;
	public static Texture individualTexture;

	/** The frame buffer used for tiles */
	private static FrameBuffer fBuffer;
	private static FrameBuffer mBuffer;
	private static FrameBuffer mBufferLit;
	private static FrameBuffer bBuffer;
	private static FrameBuffer bBufferLit;


	/**
	 * Constructor
	 */
	public GameWorld(boolean client) {
		topography = new Topography(client);
	}


	public static void setup() {
		gameWorldTexture = new Texture(Gdx.files.internal("data/image/gameWorld.png"));
		individualTexture = new Texture(Gdx.files.internal("data/image/character/individual.png"));
		fBuffer = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		mBuffer = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		mBufferLit = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		bBuffer = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		bBufferLit = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		gameWorldTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		individualTexture.setFilter(TextureFilter.Linear, TextureFilter.Nearest);
	}


	/**
	 * Renders the game world
	 */
	public void render(int camX, int camY) {
		bBuffer.begin();
		topography.renderBackGround(camX, camY);
		BloodAndMithrilClient.spriteBatch.begin();
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Prop prop : props.values()) {
			if (prop.depth == Depth.BACKGROUND) {
				prop.render();
			}
		}
		BloodAndMithrilClient.spriteBatch.end();
		bBuffer.end();

		mBuffer.begin();
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		topography.renderForeGround(camX, camY);
		BloodAndMithrilClient.spriteBatch.begin();
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Prop prop : props.values()) {
			if (prop.depth == Depth.MIDDLEGROUND) {
				prop.render();
			}
		}
		BloodAndMithrilClient.spriteBatch.end();
		mBuffer.end();

		fBuffer.begin();
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		topography.renderForeGround(camX, camY);
		BloodAndMithrilClient.spriteBatch.begin();
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Prop prop : props.values()) {
			if (prop.depth == Depth.FOREGOUND) {
				prop.render();
			}
		}
		BloodAndMithrilClient.spriteBatch.end();
		IndividualRenderer.renderIndividuals();
		fBuffer.end();

		DynamicLightingPostRenderer.render(camX, camY);
	}


	/**
	 * Updates the game world
	 */
	public void update(int camX, int camY) {
		topography.loadOrGenerateNullChunksAccordingToCam(camX, camY);
		float d = 1f/60f;
		WorldState.currentEpoch.incrementTime(d);

		for (Individual indi : individuals.values()) {
			indi.update(d);
		}
		
		for (Prop prop : props.values()) {
			if (ClientServerInterface.isServer()) {
				prop.update(d);
			}
		}

		// Topography.saveAndFlushUnneededChunks((int) BloodAndMithrilClient.cam.position.x, (int) BloodAndMithrilClient.cam.position.y);
		Topography.executeBackLog();
	}


	/**
	 * @return returns the topography of the GameWorld.
	 */
	public Topography getTopography() {
		return topography;
	}


	/**
	 * A light, holding data of the occlusion map and 1d shadow map
	 *
	 * @author Matt
	 */
	public static class Light {

		/** World coords and size of this {@link Light} */
		public float x, y;
		public int size;
		public Color color;
		public float intensity;

		/** Various {@link FrameBuffer}s */
		public FrameBuffer fOcclusion, mOcclusion, fShadowMap, mShadowMap;

		/**
		 * Constructor
		 */
		public Light(int size, float x, float y, Color color, float intensity) {
			this.size = size;
			this.x = x;
			this.y = y;
			this.color = color;
			this.intensity = intensity;
		}
		
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof Light) {
				Light otherLight = (Light) other;
				return this.x == otherLight.x && this.y == otherLight.y && this.color.equals(otherLight.color) && this.intensity == otherLight.intensity;
			}
			
			return false;
		}
	}


	/**
	 * Class to encapsulate the rendering of {@link Individual}s
	 *
	 * @author Matt
	 */
	private static class IndividualRenderer {

		/** {@link Predicate} for filtering out those that are NOT on platforms */
		private static Predicate<Individual> onPlatform = new Predicate<Individual>() {
			@Override
			public boolean apply(Individual input) {
				if (Topography.getTile(input.getState().position.x, input.getState().position.y - Topography.TILE_SIZE/2, true).isPlatformTile ||
					Topography.getTile(input.getState().position.x, input.getState().position.y - 3 * Topography.TILE_SIZE/2, true).isPlatformTile) {
					return true;
				} else {
					return false;
				}
			};
		};

		/** {@link Predicate} for filtering out those that ARE on platforms */
		private static Predicate<Individual> offPlatform = new Predicate<Individual>() {
			@Override
			public boolean apply(Individual input) {
				if (Topography.getTile(input.getState().position.x, input.getState().position.y - Topography.TILE_SIZE/2, true).isPlatformTile ||
						Topography.getTile(input.getState().position.x, input.getState().position.y - 3 * Topography.TILE_SIZE/2, true).isPlatformTile) {
					return false;
				} else {
					return true;
				}
			};
		};

		/** Renders all individuals, ones that are on platforms are rendered first */
		private static void renderIndividuals() {
			try {
				for (Individual indi : Collections2.filter(Lists.newArrayList(individuals.values()), onPlatform)) {
					indi.render();
				}

				for (Individual indi : Collections2.filter(Lists.newArrayList(individuals.values()), offPlatform)) {
					indi.render();
				}
			} catch (NullPointerException e) {
				Logger.generalDebug("Nullpointer whilst rendering individual", LogLevel.WARN, e);
			}
		}
	}


	/**
	 * Class to encapsulate post-rendering with dynamic lighting shaders.
	 *
	 * @author Matt
	 */
	private static class DynamicLightingPostRenderer {
		private static void render(float camX, float camY) {
			ArrayList<Light> tempLights = new ArrayList<GameWorld.Light>();

			//Do not bother with lights that are off screen
			for (Light light : lights.values()) {
				if (light.x - light.size < camX + BloodAndMithrilClient.WIDTH/2 &&
					light.x + light.size > camX - BloodAndMithrilClient.WIDTH/2 &&
					light.y - light.size < camY + BloodAndMithrilClient.HEIGHT/2 &&
					light.y + light.size > camY - BloodAndMithrilClient.HEIGHT/2) {
					tempLights.add(light);
				}
			}

			for (Light light : tempLights) {
				
				if (light.fOcclusion == null) {
					light.fShadowMap = new FrameBuffer(Format.RGBA8888, light.size, 1, true);
					light.mShadowMap = new FrameBuffer(Format.RGBA8888, light.size, 1, true);
					light.fOcclusion = new FrameBuffer(Format.RGBA8888, light.size, light.size, true);
					light.mOcclusion = new FrameBuffer(Format.RGBA8888, light.size, light.size, true);
				}

				//Draw foreground to occlusion map
				light.fOcclusion.begin();
				BloodAndMithrilClient.spriteBatch.begin();
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
				Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
				BloodAndMithrilClient.spriteBatch.draw(
					fBuffer.getColorBufferTexture(),
					0f,
					0f,
					BloodAndMithrilClient.WIDTH,
					BloodAndMithrilClient.HEIGHT,
					(int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,
					(int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2,
					light.size,
					light.size,
					false,
					false
				);
				BloodAndMithrilClient.spriteBatch.end();
				light.fOcclusion.end();

				light.mOcclusion.begin();
				BloodAndMithrilClient.spriteBatch.begin();
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
				Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
				BloodAndMithrilClient.spriteBatch.draw(
					mBuffer.getColorBufferTexture(),
					0f,
					0f,
					BloodAndMithrilClient.WIDTH,
					BloodAndMithrilClient.HEIGHT,
					(int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,
					(int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2,
					light.size,
					light.size,
					false,
					false
				);
				BloodAndMithrilClient.spriteBatch.end();
				light.mOcclusion.end();

				//Calculate 1D shadow map
				light.fShadowMap.begin();
				BloodAndMithrilClient.spriteBatch.begin();
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadowMap);
				Shaders.shadowMap.setUniformf("resolution", light.fOcclusion.getWidth(), light.fOcclusion.getHeight());
				BloodAndMithrilClient.spriteBatch.draw(light.fOcclusion.getColorBufferTexture(), 0f, 0f, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, light.size, light.size, false, false);
				BloodAndMithrilClient.spriteBatch.end();
				light.fShadowMap.end();

				light.mShadowMap.begin();
				BloodAndMithrilClient.spriteBatch.begin();
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadowMap);
				Shaders.shadowMap.setUniformf("resolution", light.mOcclusion.getWidth(), light.mOcclusion.getHeight());
				BloodAndMithrilClient.spriteBatch.draw(light.mOcclusion.getColorBufferTexture(), 0f, 0f, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, light.size, light.size, false, false);
				BloodAndMithrilClient.spriteBatch.end();
				light.mShadowMap.end();
			}

			//Begin rendering----------------------------------//
			Weather.render();
			BloodAndMithrilClient.spriteBatch.begin();

			bBufferLit.begin();
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			for (Light light : tempLights) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
				Shaders.defaultBackGroundTiles.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				Shaders.defaultBackGroundTiles.setUniformf("size", light.size);
				Shaders.defaultBackGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
				Shaders.defaultBackGroundTiles.setUniformf("lightSource", (int)BloodAndMithrilClient.worldToScreenX(light.x), (int)BloodAndMithrilClient.worldToScreenY(light.y));
				BloodAndMithrilClient.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
				BloodAndMithrilClient.spriteBatch.flush();
			}
			bBufferLit.end();
			
			mBufferLit.begin();
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			for (Light light : tempLights) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
				Shaders.defaultBackGroundTiles.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				Shaders.defaultBackGroundTiles.setUniformf("size", light.size);
				Shaders.defaultBackGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
				Shaders.defaultBackGroundTiles.setUniformf("lightSource", (int)BloodAndMithrilClient.worldToScreenX(light.x), (int)BloodAndMithrilClient.worldToScreenY(light.y));
				BloodAndMithrilClient.spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
				BloodAndMithrilClient.spriteBatch.flush();
			}
			mBufferLit.end();

			//Render the background
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.black);
			Shaders.black.setUniformf("color", new Color(0f, 0f, 0f, 1f));
			BloodAndMithrilClient.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			BloodAndMithrilClient.spriteBatch.draw(bBufferLit.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);

			//Render the light rays
			for (Light light: tempLights) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadow);
				Shaders.shadow.setUniformf("resolution", light.fOcclusion.getWidth(), light.fOcclusion.getHeight());
				
				Shaders.shadow.setUniformf("color", light.color.r, light.color.g, light.color.b, 0.4f * light.color.a/20f);
				Shaders.shadow.setUniformf("intensity", light.intensity);
				BloodAndMithrilClient.spriteBatch.draw(light.mShadowMap.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size, 0, 0, light.size, 1, false, true);
				BloodAndMithrilClient.spriteBatch.flush();
				
				Shaders.shadow.setUniformf("color", light.color.r, light.color.g, light.color.b, 0.7f * light.color.a/20f);
				Shaders.shadow.setUniformf("intensity", light.intensity);
				BloodAndMithrilClient.spriteBatch.draw(light.fShadowMap.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size, 0, 0, light.size, 1, false, true);
				BloodAndMithrilClient.spriteBatch.flush();
			}

			//Render middleground without lighting
			if ("true".equals(System.getProperty("seeAll"))) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
				BloodAndMithrilClient.spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			} else {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.black);
				float color = WorldState.currentEpoch.dayLight() * 0.15f;
				Shaders.black.setUniformf("color", new Color(color, color, color, 1f));
				BloodAndMithrilClient.spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
				
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
				BloodAndMithrilClient.spriteBatch.draw(mBufferLit.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			}

			//Render the middleground, affected by lighting
			for (Light light : tempLights) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultForeGroundTiles);
				light.mShadowMap.getColorBufferTexture().bind(1);
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
				Shaders.defaultForeGroundTiles.setUniformi("u_texture2", 1);
				Shaders.defaultForeGroundTiles.setUniformf("penetration", 0.10f);
				Shaders.defaultForeGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a * 0.8f * light.intensity);
				BloodAndMithrilClient.spriteBatch.draw(light.mOcclusion.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size);
			}

			//Render foreground without lighting
			if ("true".equals(System.getProperty("seeAll"))) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
				BloodAndMithrilClient.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			} else {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.black);
				float color = WorldState.currentEpoch.dayLight() * 0.15f;
				Shaders.black.setUniformf("color", new Color(color, color, color, 1f));
				BloodAndMithrilClient.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			}

			//Render the foreground, affected by lighting
			for (Light light : tempLights) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultForeGroundTiles);
				light.fShadowMap.getColorBufferTexture().bind(1);
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
				Shaders.defaultForeGroundTiles.setUniformi("u_texture2", 1);
				Shaders.defaultForeGroundTiles.setUniformf("penetration", 0.07f);
				Shaders.defaultForeGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a * light.intensity);
				BloodAndMithrilClient.spriteBatch.draw(light.fOcclusion.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size);
			}

			BloodAndMithrilClient.spriteBatch.end();
			//End rendering----------------------------------//
		}
	}


	public enum Depth {
		BACKGROUND, FOREGOUND, MIDDLEGROUND
	}
}
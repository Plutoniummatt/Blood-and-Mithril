package bloodandmithril.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Class representing the entire domain governing the game.
 *
 * @author Matt
 */
public class Domain {
	
	/** The current active {@link World} */
	private static World activeWorld;

	/** {@link World}s */
	private static HashMap<Integer, World> 					worlds 					= Maps.newHashMap();
	
	/** {@link Topography}s */
	private static HashMap<Integer, Topography>				topographies			= Maps.newHashMap();

	/** All lights */
	public static ConcurrentHashMap<Integer, Light> 		lights 					= new ConcurrentHashMap<>();

	/** {@link Individual} that are selected for manual control */
	public static Set<Individual> 							selectedIndividuals 	= Sets.newHashSet();

	/** Every {@link Individual} that exists */
	public static ConcurrentHashMap<Integer, Individual> 	individuals 			= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	public static ConcurrentHashMap<Integer, Prop> 			props 					= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	public static ConcurrentHashMap<Integer, Faction> 		factions 				= new ConcurrentHashMap<>();

	/** Textures */
	public static Texture gameWorldTexture;
	public static Texture individualTexture;

	/** The frame buffer used for tiles */
	private static FrameBuffer fBuffer;
	private static FrameBuffer mBuffer;
	private static FrameBuffer mBufferLit;
	private static FrameBuffer bBuffer;
	private static FrameBuffer bBufferProcessedForDaylightShader;
	private static FrameBuffer bBufferLit;


	/**
	 * Constructor
	 */
	public Domain() {
		World world = new World(1200f);
		getWorlds().put(world.getWorldId(), world);
		activeWorld = world;
	}
	
	
	public static Topography getTopography(int id) {
		return topographies.get(id);
	}
	
	
	public static Topography addTopography(int id, Topography topography) {
		return topographies.put(id, topography);
	}


	public static void setup() {
		gameWorldTexture = new Texture(Gdx.files.internal("data/image/gameWorld.png"));
		individualTexture = new Texture(Gdx.files.internal("data/image/character/individual.png"));
		
		gameWorldTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		individualTexture.setFilter(TextureFilter.Linear, TextureFilter.Nearest);
		
		fBuffer = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		mBuffer = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		mBufferLit = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		bBuffer = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		bBufferProcessedForDaylightShader = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
		bBufferLit = new FrameBuffer(Format.RGBA8888, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, true);
	}


	/**
	 * Renders the game world
	 */
	public void render(int camX, int camY) {
		bBuffer.begin();
		getActiveWorld().getTopography().renderBackGround(camX, camY);
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
		
		BackgroundBlur();
		
		mBuffer.begin();
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
		getActiveWorld().getTopography().renderForeGround(camX, camY);
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

	
	private void BackgroundBlur() {
		bBufferProcessedForDaylightShader.begin();
		BloodAndMithrilClient.spriteBatch.begin();
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.daylightOcclusion);
		
		double r;
		double g;
		double b;
		float time = WorldState.currentEpoch.getTime();
		
		if (time < 10.0) {
			r = 0.1 + 1.2 * Math.exp(-0.100*Math.pow((time - 10.0), 2.0));
			g = 0.1 + 1.1 * Math.exp(-0.150*Math.pow((time - 10.0), 2.0));
			b = 0.1 + 1.0 * Math.exp(-0.200*Math.pow((time - 10.0), 2.0));
		} else if (time >= 10 && time < 14) {
			r = 1.3;
			g = 1.2;
			b = 1.1;
		} else {
			r = 0.1 + 1.2 * Math.exp(-0.100*Math.pow((time - 14.0), 2.0));
			g = 0.1 + 1.1 * Math.exp(-0.150*Math.pow((time - 14.0), 2.0));
			b = 0.1 + 1.0 * Math.exp(-0.200*Math.pow((time - 14.0), 2.0));
		}
		
		Shaders.daylightOcclusion.setUniformf("dl", (float)r, (float)g, (float)b, WorldState.currentEpoch.dayLight());
		Shaders.daylightOcclusion.setUniformf("res", BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT);
		BloodAndMithrilClient.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
		BloodAndMithrilClient.spriteBatch.end();
		bBufferProcessedForDaylightShader.end();
	}


	/**
	 * Updates the game world
	 */
	public void update(int camX, int camY) {
		getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToCam(camX, camY);
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

		Topography.executeBackLog();
	}


	public static World getActiveWorld() {
		return activeWorld;
	}


	public static void setActiveWorld(World activeWorldToSet) {
		activeWorld = activeWorldToSet;
	}
	
	
	public static World getWorld(int id) {
		return getWorlds().get(id);
	}
	
	
	public static HashMap<Integer, World> getWorlds() {
		return worlds;
	}


	public static void setWorlds(HashMap<Integer, World> worlds) {
		Domain.worlds = worlds;
	}


	/**
	 * A light, holding data of the occlusion map and 1d shadow map
	 *
	 * @author Matt
	 */
	public static class Light {

		/** World coords and size of this {@link Light} */
		public float x, y, spanBegin, spanEnd;
		public int size;
		public Color color;
		public float intensity;
		public boolean renderSwitch;

		/** Various {@link FrameBuffer}s */
		public FrameBuffer fOcclusion, mOcclusion, fShadowMap, mShadowMap;

		/**
		 * Constructor
		 * 
		 * SpanBegin - Counter-Clockwise, begining from the -ve x-axis, the begining angle of light span
		 * spanEnd - Counter-Clockwise, from spanEnd, the span, 1f meaning 360 degrees.
		 */
		public Light(int size, float x, float y, Color color, float intensity, float spanBegin, float spanEnd) {
			this.size = size;
			this.x = x;
			this.y = y;
			this.color = color;
			this.intensity = intensity;
			this.spanBegin = spanBegin;
			this.spanEnd = spanEnd;
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
				if (getActiveWorld().getTopography().getTile(input.getState().position.x, input.getState().position.y - Topography.TILE_SIZE/2, true).isPlatformTile ||
						getActiveWorld().getTopography().getTile(input.getState().position.x, input.getState().position.y - 3 * Topography.TILE_SIZE/2, true).isPlatformTile) {
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
				if (getActiveWorld().getTopography().getTile(input.getState().position.x, input.getState().position.y - Topography.TILE_SIZE/2, true).isPlatformTile ||
					getActiveWorld().getTopography().getTile(input.getState().position.x, input.getState().position.y - 3 * Topography.TILE_SIZE/2, true).isPlatformTile) {
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
	public static class DynamicLightingPostRenderer {
		public static boolean SEE_ALL = false;

		private static void render(float camX, float camY) {
			ArrayList<Light> tempLights = new ArrayList<Domain.Light>();

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
				
				if (light.renderSwitch) {
					light.renderSwitch = !light.renderSwitch;
					continue;
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
				light.mOcclusion.end();

				//Calculate 1D shadow map
				light.fShadowMap.begin();
				BloodAndMithrilClient.spriteBatch.begin();
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadowMap);
				Shaders.shadowMap.setUniformf("resolution", light.fOcclusion.getWidth(), light.fOcclusion.getHeight());
				Shaders.shadowMap.setUniformf("span", light.spanBegin, light.spanEnd);
				BloodAndMithrilClient.spriteBatch.draw(light.fOcclusion.getColorBufferTexture(), 0f, 0f, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, light.size, light.size, false, false);
				BloodAndMithrilClient.spriteBatch.end();
				light.fShadowMap.end();

				light.mShadowMap.begin();
				BloodAndMithrilClient.spriteBatch.begin();
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadowMap);
				Shaders.shadowMap.setUniformf("resolution", light.mOcclusion.getWidth(), light.mOcclusion.getHeight());
				Shaders.shadowMap.setUniformf("span", light.spanBegin, light.spanEnd);
				BloodAndMithrilClient.spriteBatch.draw(light.mOcclusion.getColorBufferTexture(), 0f, 0f, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, light.size, light.size, false, false);
				BloodAndMithrilClient.spriteBatch.end();
				light.mShadowMap.end();
				
				light.renderSwitch = !light.renderSwitch;
			}

			//Begin rendering----------------------------------//
			Weather.render();
			BloodAndMithrilClient.spriteBatch.begin();

			bBufferLit.begin();
			
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			// Still render through the shader if no lights are present
			if (tempLights.isEmpty()) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
				bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
				Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
				BloodAndMithrilClient.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
				BloodAndMithrilClient.spriteBatch.flush();
			}
			
			for (Light light : tempLights) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
				bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
				Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
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
				bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
				Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
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
			if (SEE_ALL) {
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
			if (SEE_ALL) {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
				BloodAndMithrilClient.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			} else {
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.daylightShader);
				bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
				Shaders.daylightShader.setUniformi("u_texture2", 1);
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
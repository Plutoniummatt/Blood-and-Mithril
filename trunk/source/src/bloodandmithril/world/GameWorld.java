package bloodandmithril.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Logger.LogLevel;
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
	private final Topography topography;

	/** Textures */
	public static final Texture gameWorldTexture = new Texture(Gdx.files.internal("data/image/gameWorld.png"));
	public static final Texture individualTexture = new Texture(Gdx.files.internal("data/image/character/individual.png"));

	/** Lights */
	public static List<Light> lights = new ArrayList<Light>();

	/** The frame buffer used for tiles */
	private static FrameBuffer fBuffer = new FrameBuffer(Format.RGBA8888, Fortress.WIDTH, Fortress.HEIGHT, true);
	private static FrameBuffer bBuffer = new FrameBuffer(Format.RGBA8888, Fortress.WIDTH, Fortress.HEIGHT, true);
	private static FrameBuffer bBufferLit = new FrameBuffer(Format.RGBA8888, Fortress.WIDTH, Fortress.HEIGHT, true);

	/** {@link Individual} that are selected for manual control */
	public static Set<Individual> selectedIndividuals = new HashSet<Individual>();

	/** Every {@link Individual} that exists */
	public static ConcurrentHashMap<Integer, Individual> individuals = new ConcurrentHashMap<>();

	public static ArrayList<Prop> props = new ArrayList<>();


	/**
	 * Constructor
	 */
	public GameWorld() {
		topography = new Topography();
		gameWorldTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		individualTexture.setFilter(TextureFilter.Linear, TextureFilter.Nearest);
	}


	/**
	 * Renders the game world
	 */
	public void render(int camX, int camY) {
		topography.loadOrGenerateNullChunks(camX, camY);

		bBuffer.begin();
		topography.renderBackGround(camX, camY);
		Fortress.spriteBatch.begin();
		Fortress.spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		for (Prop prop : props) {
			prop.render();
		}
		Fortress.spriteBatch.end();
		bBuffer.end();

		fBuffer.begin();
		topography.renderForeGround(camX, camY);
		IndividualRenderer.renderIndividuals();
		fBuffer.end();

		DynamicLightingPostRenderer.render(camX, camY);
	}


	/**
	 * Updates the game world
	 */
	public void update(float delta) {
		WorldState.currentEpoch.incrementTime(delta / 60f);

		for (Individual indi : individuals.values()) {
			indi.update(delta);
		}

		Topography.saveAndFlushUnneededChunks((int) Fortress.cam.position.x, (int) Fortress.cam.position.y);
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
		public FrameBuffer occlusion;
		public FrameBuffer shadowMap;

		/**
		 * Constructor
		 */
		public Light(int size, float x, float y, Color color, float intensity) {
			this.size = size;
			this.x = x;
			this.y = y;
			this.color = color;
			this.intensity = intensity;
			shadowMap = new FrameBuffer(Format.RGBA8888, size, 1, true);
			occlusion = new FrameBuffer(Format.RGBA8888, size, size, true);
		}
	}
	
	
	/**
	 * Class to encapsulate the rendering of {@link Individual}s
	 *
	 * @author Matt
	 */
	private static class IndividualRenderer {
		
		/** {@link Predicate} for filtering out those that are NOT on platorms */
		private static Predicate<Individual> onPlatform = new Predicate<Individual>() {
			public boolean apply(Individual input) {
				if (Topography.getTile(input.state.position.x, input.state.position.y - Topography.TILE_SIZE/2, true).isPlatformTile || 
					Topography.getTile(input.state.position.x, input.state.position.y - 3 * Topography.TILE_SIZE/2, true).isPlatformTile) {
					return true;
				} else {
					return false;
				}
			};
		};
		
		/** {@link Predicate} for filtering out those that ARE on platorms */
		private static Predicate<Individual> offPlatform = new Predicate<Individual>() {
			public boolean apply(Individual input) {
				if (Topography.getTile(input.state.position.x, input.state.position.y - Topography.TILE_SIZE/2, true).isPlatformTile || 
						Topography.getTile(input.state.position.x, input.state.position.y - 3 * Topography.TILE_SIZE/2, true).isPlatformTile) {
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
				Logger.generalDebug("Nullpointer whilst rendering individual", LogLevel.WARN);
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
			for (Light light : lights) {
				if (light.x - light.size < camX + Fortress.WIDTH/2 &&
					light.x + light.size > camX - Fortress.WIDTH/2 &&
					light.y - light.size < camY + Fortress.HEIGHT/2 &&
					light.y + light.size > camY - Fortress.HEIGHT/2) {
					tempLights.add(light);
				}
			}

			for (Light light : tempLights) {
				//Draw foreground to occlusion map
				light.occlusion.begin();
				Fortress.spriteBatch.begin();
				Fortress.spriteBatch.setShader(Shaders.pass);
				Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
				Fortress.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0f, 0f, Fortress.WIDTH, Fortress.HEIGHT, (int)Fortress.worldToScreenX(light.x) - light.size/2, (int)Fortress.worldToScreenY(light.y) - light.size/2, light.size, light.size, false, false);
				Fortress.spriteBatch.end();
				light.occlusion.end();

				//Calculate 1D shadow map
				light.shadowMap.begin();
				Fortress.spriteBatch.begin();
				Fortress.spriteBatch.setShader(Shaders.shadowMap);
				Shaders.shadowMap.setUniformf("resolution", light.occlusion.getWidth(), light.occlusion.getHeight());
				Fortress.spriteBatch.draw(light.occlusion.getColorBufferTexture(), 0f, 0f, Fortress.WIDTH, Fortress.HEIGHT, 0, 0, light.size, light.size, false, false);
				Fortress.spriteBatch.end();
				light.shadowMap.end();
			}

			//Begin rendering----------------------------------//
			Weather.render();
			Fortress.spriteBatch.begin();

			bBufferLit.begin();
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			for (Light light : tempLights) {
				Fortress.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
				Shaders.defaultBackGroundTiles.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				Shaders.defaultBackGroundTiles.setUniformf("size", light.size);
				Shaders.defaultBackGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
				Shaders.defaultBackGroundTiles.setUniformf("lightSource", (int)Fortress.worldToScreenX(light.x), (int)Fortress.worldToScreenY(light.y));
				Fortress.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, Fortress.WIDTH, Fortress.HEIGHT, 0, 0, Fortress.WIDTH, Fortress.HEIGHT, false, true);
				Fortress.spriteBatch.flush();
			}
			bBufferLit.end();

			//Render the backhground tiles
			Fortress.spriteBatch.setShader(Shaders.black);
			Shaders.black.setUniformf("color", new Color(0f, 0f, 0f, 1f));
			Fortress.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, Fortress.WIDTH, Fortress.HEIGHT, 0, 0, Fortress.WIDTH, Fortress.HEIGHT, false, true);
			Fortress.spriteBatch.setShader(Shaders.pass);
			Fortress.spriteBatch.draw(bBufferLit.getColorBufferTexture(), 0, 0, Fortress.WIDTH, Fortress.HEIGHT, 0, 0, Fortress.WIDTH, Fortress.HEIGHT, false, true);

			//Render the light rays
			for (Light light: tempLights) {
				Fortress.spriteBatch.setShader(Shaders.shadow);
				Shaders.shadow.setUniformf("resolution", light.occlusion.getWidth(), light.occlusion.getHeight());
				Shaders.shadow.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a/20f);
				Shaders.shadow.setUniformf("intensity", light.intensity);
				Fortress.spriteBatch.draw(light.shadowMap.getColorBufferTexture(),  (int)Fortress.worldToScreenX(light.x) - light.size/2,  (int)Fortress.worldToScreenY(light.y) - light.size/2, light.size, light.size, 0, 0, light.size, 1, false, true);
			}

			//Render foreground tiles
			if (System.getProperty("seeAll").equals("true")) {
				Fortress.spriteBatch.setShader(Shaders.pass);
				Fortress.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, Fortress.WIDTH, Fortress.HEIGHT, 0, 0, Fortress.WIDTH, Fortress.HEIGHT, false, true);
			} else {
				Fortress.spriteBatch.setShader(Shaders.black);
				float color = WorldState.currentEpoch.dayLight() * 0.15f;
				Shaders.black.setUniformf("color", new Color(color, color, color, 1f));
				Fortress.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, Fortress.WIDTH, Fortress.HEIGHT, 0, 0, Fortress.WIDTH, Fortress.HEIGHT, false, true);
			}

			//Render the foreground, affected by lighting
			for (Light light : tempLights) {
				Fortress.spriteBatch.setShader(Shaders.defaultForeGroundTiles);
				light.shadowMap.getColorBufferTexture().bind(1);
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
				Shaders.defaultForeGroundTiles.setUniformi("u_texture2", 1);
				Shaders.defaultForeGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
				Fortress.spriteBatch.draw(light.occlusion.getColorBufferTexture(),  (int)Fortress.worldToScreenX(light.x) - light.size/2,  (int)Fortress.worldToScreenY(light.y) - light.size/2, light.size, light.size);
			}

			Fortress.spriteBatch.end();
			//End rendering----------------------------------//
		}
	}
}

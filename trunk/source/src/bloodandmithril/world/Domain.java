package bloodandmithril.world;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.graphics.DynamicLightingPostRenderer;
import bloodandmithril.graphics.Light;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.Gdx;
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
	private static HashMap<Integer, World> 						worlds 					= Maps.newHashMap();
	
	/** {@link Topography}s */
	private static HashMap<Integer, Topography>					topographies			= Maps.newHashMap();

	/** All lights */
	private static ConcurrentHashMap<Integer, Light> 			lights 					= new ConcurrentHashMap<>();

	/** {@link Individual} that are selected for manual control */
	private static Set<Individual> 								selectedIndividuals 	= Sets.newHashSet();

	/** Every {@link Individual} that exists */
	private static ConcurrentHashMap<Integer, Individual> 		individuals 			= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	private static ConcurrentHashMap<Integer, Prop> 			props 					= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	private static ConcurrentHashMap<Integer, Faction> 			factions 				= new ConcurrentHashMap<>();
	
	/** Textures */
	public static Texture gameWorldTexture;
	public static Texture individualTexture;

	/** The frame buffer used for tiles */
	public static FrameBuffer fBuffer;
	public static FrameBuffer mBuffer;
	public static FrameBuffer mBufferLit;
	public static FrameBuffer bBuffer;
	public static FrameBuffer bBufferProcessedForDaylightShader;
	public static FrameBuffer bBufferLit;


	/**
	 * Constructor
	 */
	public Domain() {
		if (worlds.isEmpty()) {
			World world = new World(1200f);
			getWorlds().put(world.getWorldId(), world);
			activeWorld = world;
		} else {
			activeWorld = worlds.get(1);
		}
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
		for (Prop prop : getProps().values()) {
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
		for (Prop prop : getProps().values()) {
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
		for (Prop prop : getProps().values()) {
			if (prop.depth == Depth.FOREGOUND) {
				prop.render();
			}
		}
		BloodAndMithrilClient.spriteBatch.end();
		IndividualPlatformFilteringRenderer.renderIndividuals();
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
		if (getActiveWorld() != null) {
			getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToCam(camX, camY);
			
			float d = 1f/60f;
			
			WorldState.currentEpoch.incrementTime(d);

			for (Individual indi : getIndividuals().values()) {
				indi.update(d);
			}
			
			for (Prop prop : getProps().values()) {
				if (ClientServerInterface.isServer()) {
					prop.update(d);
				}
			}
	
			Topography.executeBackLog();
		}
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


	public static ConcurrentHashMap<Integer, Light> getLights() {
		return lights;
	}


	public static void setLights(ConcurrentHashMap<Integer, Light> lights) {
		Domain.lights = lights;
	}


	public static Set<Individual> getSelectedIndividuals() {
		return selectedIndividuals;
	}


	public static void setSelectedIndividuals(Set<Individual> selectedIndividuals) {
		Domain.selectedIndividuals = selectedIndividuals;
	}


	public static ConcurrentHashMap<Integer, Individual> getIndividuals() {
		return individuals;
	}


	public static void setIndividuals(ConcurrentHashMap<Integer, Individual> individuals) {
		Domain.individuals = individuals;
	}


	public static ConcurrentHashMap<Integer, Prop> getProps() {
		return props;
	}


	public static void setProps(ConcurrentHashMap<Integer, Prop> props) {
		Domain.props = props;
	}


	public static ConcurrentHashMap<Integer, Faction> getFactions() {
		return factions;
	}


	public static void setFactions(ConcurrentHashMap<Integer, Faction> factions) {
		Domain.factions = factions;
	}


	/**
	 * Class to encapsulate the rendering of {@link Individual}s
	 *
	 * @author Matt
	 */
	private static class IndividualPlatformFilteringRenderer {

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
				for (Individual indi : Collections2.filter(Lists.newArrayList(getIndividuals().values()), onPlatform)) {
					indi.render();
				}

				for (Individual indi : Collections2.filter(Lists.newArrayList(getIndividuals().values()), offPlatform)) {
					indi.render();
				}
			} catch (NullPointerException e) {
				Logger.generalDebug("Nullpointer whilst rendering individual", LogLevel.WARN, e);
			}
		}
	}


	public enum Depth {
		BACKGROUND, FOREGOUND, MIDDLEGROUND
	}
}
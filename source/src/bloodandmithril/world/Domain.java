package bloodandmithril.world;

import static bloodandmithril.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.BloodAndMithrilClient.cam;
import static bloodandmithril.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Logger.generalDebug;
import static bloodandmithril.world.Domain.Depth.BACKGROUND;
import static bloodandmithril.world.Domain.Depth.FOREGOUND;
import static bloodandmithril.world.Domain.Depth.MIDDLEGROUND;
import static bloodandmithril.world.WorldState.currentEpoch;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.character.Individual;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.graphics.DynamicLightingPostRenderer;
import bloodandmithril.graphics.Light;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.base.Predicate;

/**
 * Class representing the entire domain governing the game.
 *
 * @author Matt
 */
public class Domain {
	
	/** The current active {@link World} */
	private static World activeWorld;

	/** {@link World}s */
	private static HashMap<Integer, World> 						worlds 					= newHashMap();
	
	/** {@link Topography}s */
	private static HashMap<Integer, Topography>					topographies			= newHashMap();

	/** All lights */
	private static ConcurrentHashMap<Integer, Light> 			lights 					= new ConcurrentHashMap<>();

	/** {@link Individual} that are selected for manual control */
	private static Set<Individual> 								selectedIndividuals 	= newHashSet();

	/** Every {@link Individual} that exists */
	private static ConcurrentHashMap<Integer, Individual> 		individuals 			= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	private static ConcurrentHashMap<Integer, Prop> 			props 					= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	private static ConcurrentHashMap<Integer, Faction> 			factions 				= new ConcurrentHashMap<>();
	
	/** Every {@link Item} that exists that is not stored in a {@link Container} */
	private static ConcurrentHashMap<Integer, Item> 			items	 				= new ConcurrentHashMap<>();
	
	/** Domain-specific {@link ShapeRenderer} */
	public static ShapeRenderer shapeRenderer;
	
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
	
	private long topographyUpdateTimer;
	
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
		gameWorldTexture 					= new Texture(files.internal("data/image/gameWorld.png"));
		individualTexture 					= new Texture(files.internal("data/image/character/individual.png"));
		
		gameWorldTexture.setFilter(Linear, Linear);
		individualTexture.setFilter(Linear, Nearest);
		
		fBuffer 							= new FrameBuffer(RGBA8888, WIDTH, HEIGHT, true);
		mBuffer 							= new FrameBuffer(RGBA8888, WIDTH, HEIGHT, true);
		mBufferLit 							= new FrameBuffer(RGBA8888, WIDTH, HEIGHT, true);
		bBuffer 							= new FrameBuffer(RGBA8888, WIDTH, HEIGHT, true);
		bBufferProcessedForDaylightShader 	= new FrameBuffer(RGBA8888, WIDTH, HEIGHT, true);
		bBufferLit 							= new FrameBuffer(RGBA8888, WIDTH, HEIGHT, true);
	}


	/**
	 * Renders the game world
	 */
	public void render(int camX, int camY) {
		bBuffer.begin();
		getActiveWorld().getTopography().renderBackGround(camX, camY);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : getProps().values()) {
			if (prop.depth == BACKGROUND) {
				prop.render();
			}
		}
		spriteBatch.end();
		bBuffer.end();
		
		BackgroundBlur();
		
		mBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : getProps().values()) {
			if (prop.depth == MIDDLEGROUND) {
				prop.render();
			}
		}
		spriteBatch.end();
		mBuffer.end();

		fBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		getActiveWorld().getTopography().renderForeGround(camX, camY);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : getProps().values()) {
			if (prop.depth == FOREGOUND) {
				prop.render();
			}
		}
		spriteBatch.end();
		IndividualPlatformFilteringRenderer.renderIndividuals();
		
		gl20.glEnable(GL20.GL_BLEND);
		shapeRenderer.begin(ShapeType.FilledRectangle);
		shapeRenderer.setProjectionMatrix(cam.combined);
		
		getActiveWorld().getTopography().getFluids().getAllFluids().stream().forEach(entry -> {
			entry.value.render(entry.x, entry.y);
		});
		
		shapeRenderer.end();
		gl20.glDisable(GL20.GL_BLEND);
		
		fBuffer.end();

		DynamicLightingPostRenderer.render(camX, camY);
	}

	
	private void BackgroundBlur() {
		bBufferProcessedForDaylightShader.begin();
		spriteBatch.begin();
		gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.daylightOcclusion);
		
		double r;
		double g;
		double b;
		float time = currentEpoch.getTime();
		
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
		
		Shaders.daylightOcclusion.setUniformf("dl", (float)r, (float)g, (float)b, currentEpoch.dayLight());
		Shaders.daylightOcclusion.setUniformf("res", WIDTH, HEIGHT);
		spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
		spriteBatch.end();
		bBufferProcessedForDaylightShader.end();
	}


	/**
	 * Updates the game world
	 */
	public void update(int camX, int camY) {
		if (getActiveWorld() != null) {
			getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToCam(camX, camY);
			
			if (System.currentTimeMillis() - topographyUpdateTimer > 30) {
				topographyUpdateTimer = System.currentTimeMillis();
				getActiveWorld().getTopography().update();
			}
			
			float d = 1f/60f;
			
			currentEpoch.incrementTime(d);

			for (Individual indi : individuals.values()) {
				indi.update(d);
			}
			
			for (Prop prop : props.values()) {
				prop.update(d);
			}
			
			for (Item item : items.values()) {
				item.update(d);
			}
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


	public static ConcurrentHashMap<Integer, Item> getItems() {
		return items;
	}


	public static void setItems(ConcurrentHashMap<Integer, Item> items) {
		Domain.items = items;
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
			public boolean apply(Individual individual) {
				if (getActiveWorld().getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
						getActiveWorld().getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
					return true;
				} else {
					return false;
				}
			};
		};

		/** {@link Predicate} for filtering out those that ARE on platforms */
		private static Predicate<Individual> offPlatform = new Predicate<Individual>() {
			@Override
			public boolean apply(Individual individual) {
				if (getActiveWorld().getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
					getActiveWorld().getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
					return false;
				} else {
					return true;
				}
			};
		};

		/** Renders all individuals, ones that are on platforms are rendered first */
		private static void renderIndividuals() {
			try {
				for (Individual indi : filter(newArrayList(getIndividuals().values()), onPlatform)) {
					indi.render();
				}

				for (Individual indi : filter(newArrayList(getIndividuals().values()), offPlatform)) {
					indi.render();
				}
			} catch (NullPointerException e) {
				generalDebug("Nullpointer whilst rendering individual", LogLevel.WARN, e);
			}
		}
	}


	public enum Depth {
		BACKGROUND, FOREGOUND, MIDDLEGROUND
	}
}
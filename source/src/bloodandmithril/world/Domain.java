package bloodandmithril.world;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginX;
import static bloodandmithril.core.BloodAndMithrilClient.camMarginY;
import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Logger.generalDebug;
import static bloodandmithril.world.Domain.Depth.BACKGROUND;
import static bloodandmithril.world.Domain.Depth.FOREGOUND;
import static bloodandmithril.world.Domain.Depth.MIDDLEGROUND;
import static bloodandmithril.world.WorldState.getCurrentEpoch;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.round;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.UnitsWindow;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.base.Predicate;

/**
 * Class representing the entire domain governing the game.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Domain {

	/** The current active {@link World} */
	private static World activeWorld;

	/** {@link World}s */
	private static HashMap<Integer, World> 						worlds 					= newHashMap();

	/** {@link Individual} that are selected for manual control */
	private static Set<Individual> 								selectedIndividuals 	= newHashSet();

	/** Every {@link Individual} that exists */
	private static ConcurrentHashMap<Integer, Individual> 		individuals 			= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	private static ConcurrentHashMap<Integer, Faction> 			factions 				= new ConcurrentHashMap<>();

	/** Domain-specific {@link ShapeRenderer} */
	public static ShapeRenderer shapeRenderer;

	/** Textures */
	public static Texture gameWorldTexture;
	public static Texture individualTexture;

	/** The frame buffer used for tiles */
	public static FrameBuffer fBuffer;
	public static FrameBuffer mBuffer;
	public static FrameBuffer bBuffer;
	public static FrameBuffer workingQuantized;
	public static FrameBuffer bBufferQuantized;
	public static FrameBuffer fBufferQuantized;
	public static FrameBuffer combinedBufferQuantized;

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

		try {
			Thread.sleep(50);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void setup() {
		gameWorldTexture 					= new Texture(files.internal("data/image/gameWorld.png"));
		individualTexture 					= new Texture(files.internal("data/image/character/individual.png"));

		gameWorldTexture.setFilter(Linear, Linear);
		individualTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		fBuffer 							= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		mBuffer 							= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		bBuffer 							= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		workingQuantized 					= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		bBufferQuantized 					= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		fBufferQuantized 					= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);
		combinedBufferQuantized 			= new FrameBuffer(RGBA8888, WIDTH + camMarginX, HEIGHT + camMarginY, false);

		bBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}


	/**
	 * Renders the game world
	 */
	public void render(int camX, int camY) {
		bBuffer.begin();
		Shaders.invertAlphaSolidColor.begin();
		getActiveWorld().getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {});
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.pass.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : activeWorld.props().getProps()) {
			if (prop.depth == BACKGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.render();
				spriteBatch.flush();
			}
		}
		renderParticles(Depth.BACKGROUND);
		spriteBatch.end();
		bBuffer.end();

		int xOffset = round(cam.position.x) % TILE_SIZE;
		int yOffset = round(cam.position.y) % TILE_SIZE;

		workingQuantized.begin();
		cam.position.x = cam.position.x - xOffset;
		cam.position.y = cam.position.y - yOffset;
		cam.update();
		getActiveWorld().getTopography().renderBackGround(camX, camY, Shaders.pass, shader -> {});
		cam.position.x = cam.position.x + xOffset;
		cam.position.y = cam.position.y + yOffset;
		cam.update();
		workingQuantized.end();

		bBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 1.0f, 0.0f, 0.0f, 1.0f);
		spriteBatch.draw(workingQuantized.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
		spriteBatch.end();
		bBufferQuantized.end();

		workingQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		cam.position.x = cam.position.x - xOffset;
		cam.position.y = cam.position.y - yOffset;
		cam.update();
		getActiveWorld().getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {});
		cam.position.x = cam.position.x + xOffset;
		cam.position.y = cam.position.y + yOffset;
		cam.update();
		workingQuantized.end();

		fBufferQuantized.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.invertAlphaSolidColor);
		Shaders.invertAlphaSolidColor.setUniformf("c", 0.0f, 1.0f, 0.0f, 1.0f);
		spriteBatch.draw(workingQuantized.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
		spriteBatch.end();
		fBufferQuantized.end();

		combinedBufferQuantized.begin();
		spriteBatch.begin();
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.setShader(Shaders.invertAlphaSolidColorBlend);
		bBufferQuantized.getColorBufferTexture().bind(1);
		Shaders.invertAlphaSolidColorBlend.setUniformi("u_texture_2", 1);
		gl.glActiveTexture(GL_TEXTURE0);
		spriteBatch.draw(
			fBufferQuantized.getColorBufferTexture(),
			0, 0, WIDTH, HEIGHT
		);
		spriteBatch.end();
		combinedBufferQuantized.end();

		mBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : activeWorld.props().getProps()) {
			if (prop.depth == MIDDLEGROUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				spriteBatch.flush();
			}
		}
		renderParticles(Depth.MIDDLEGROUND);
		spriteBatch.end();
		mBuffer.end();

		fBuffer.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		Domain.individualTexture.setFilter(Linear, Linear);
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
		for (Prop prop : activeWorld.props().getProps()) {
			if (prop.depth == FOREGOUND) {
				Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
				prop.preRender();
				prop.render();
				spriteBatch.flush();
			}
		}
		for (Item item : getActiveWorld().items().getItems()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			item.render();
			spriteBatch.flush();
		}
		spriteBatch.end();
		Domain.individualTexture.setFilter(Nearest, Nearest);
		IndividualPlatformFilteringRenderer.renderIndividuals();
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
		for (Projectile projectile : getActiveWorld().projectiles().getProjectiles()) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 1f);
			projectile.render();
			spriteBatch.flush();
		}
		renderParticles(Depth.FOREGOUND);
		spriteBatch.end();
		gl20.glEnable(GL20.GL_BLEND);
		gl20.glBlendFuncSeparate(GL20.GL_ONE, GL20.GL_SRC_COLOR, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		activeWorld.renderFluids();
		gl20.glDisable(GL20.GL_BLEND);
		getActiveWorld().getTopography().renderForeGround(camX, camY, Shaders.pass, shader -> {});
		fBuffer.end();
		GaussianLightingRenderer.render(camX, camY);
	}


	private void renderParticles(Depth depth) {
		gl20.glEnable(GL20.GL_BLEND);
		gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		Domain.shapeRenderer.begin(Line);
		shapeRenderer.setProjectionMatrix(cam.combined);
		Domain.shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
		if (Domain.getActiveWorld().getClientParticles() != null) {
			Domain.getActiveWorld().getClientParticles().stream().filter(p -> p.depth == depth).forEach(p -> {
				p.renderLine(Gdx.graphics.getDeltaTime());
			});
		}
		if (Domain.getActiveWorld().getServerParticles() != null) {
			Domain.getActiveWorld().getServerParticles().values().stream().filter(p -> p.depth == depth).forEach(p -> {
				p.renderLine(Gdx.graphics.getDeltaTime());
			});
		}
		Domain.shapeRenderer.end();
		Domain.shapeRenderer.begin(ShapeType.Filled);
		Domain.shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
		if (Domain.getActiveWorld().getClientParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			Domain.getActiveWorld().getClientParticles().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime());
				counter.t++;
			});
		}
		if (Domain.getActiveWorld().getServerParticles() != null) {
			final Wrapper<Integer> counter = new Wrapper<Integer>(0);
			Domain.getActiveWorld().getServerParticles().values().stream().filter(p -> p.depth == depth && isOnScreen(p.position, 50f)).forEach(p -> {
				p.render(Gdx.graphics.getDeltaTime());
				counter.t++;
			});
		}
		Domain.shapeRenderer.end();
		gl20.glDisable(GL20.GL_BLEND);
	}


	/**
	 * Updates the game world
	 */
	public void update(int camX, int camY) {
		World world = getActiveWorld();
		if (world != null) {
			float d = 1f/60f;

			getCurrentEpoch().incrementTime(d);

			for (Individual indi : individuals.values()) {
				indi.update(d);
			}

			for (Prop prop : world.props().getProps()) {
				prop.update(d);
			}

			for (Projectile projectile : world.projectiles().getProjectiles()) {
				projectile.update(d);
			}

			for (Item item : world.items().getItems()) {
				try {
					item.update(d);
				} catch (NoTileFoundException e) {}
			}
		}
	}


	public static void updateFluids() {
		for (World world : worlds.values()) {
			world.updateFluids();
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


	public static Set<Individual> getSelectedIndividuals() {
		return selectedIndividuals;
	}


	public static ConcurrentHashMap<Integer, Individual> getIndividuals() {
		return individuals;
	}


	/**
	 * @return an {@link Individual} with the specified key
	 */
	public static Individual getIndividual(int key) {
		return individuals.get(key);
	}


	public static void addIndividual(Individual indi, int worldId) {
		indi.setWorldId(worldId);
		individuals.put(indi.getId().getId(), indi);
		Domain.getWorld(worldId).getIndividuals().add(indi.getId().getId());
		if (ClientServerInterface.isClient()) {
			for (Component component : UserInterface.layeredComponents) {
				if (component instanceof UnitsWindow) {
					((UnitsWindow) component).refresh();
				}
			}
		} else {
			ClientServerInterface.SendNotification.notifyRefreshWindows();
		}
	}


	public static void setIndividuals(ConcurrentHashMap<Integer, Individual> individuals) {
		Domain.individuals = individuals;
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
			public boolean apply(Individual individual) {
				try {
					if (getActiveWorld().getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
							getActiveWorld().getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
						return true;
					} else {
						return false;
					}
				} catch (NoTileFoundException e) {
					return false;
				}

			};
		};

		/** {@link Predicate} for filtering out those that ARE on platforms */
		private static Predicate<Individual> offPlatform = new Predicate<Individual>() {
			@Override
			public boolean apply(Individual individual) {
				try {
					if (Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
							Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
							return false;
						} else {
							return true;
						}
				} catch (NoTileFoundException e) {
					return false;
				}
			};
		};

		/** Renders all individuals, ones that are on platforms are rendered first */
		private static void renderIndividuals() {
			try {
				for (Individual indi : filter(newArrayList(getIndividuals().values()), offPlatform)) {
					indi.render();
				}

				for (Individual indi : filter(newArrayList(getIndividuals().values()), onPlatform)) {
					indi.render();
				}
			} catch (NullPointerException e) {
				generalDebug("Nullpointer whilst rendering individual", LogLevel.INFO, e);
			}
		}
	}


	public enum Depth implements Serializable {
		BACKGROUND, FOREGOUND, MIDDLEGROUND
	}
}
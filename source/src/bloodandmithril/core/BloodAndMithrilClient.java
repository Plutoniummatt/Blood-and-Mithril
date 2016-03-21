package bloodandmithril.core;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.setInputProcessor;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.world.topography.Topography.convertToChunkCoord;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.Controls;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.generation.biome.MainMenuBiomeDecider;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.graphics.background.Layer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.objectives.Mission;
import bloodandmithril.performance.PositionalIndexingService;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.MainMenuWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.weather.WeatherRenderer;

/**
 * Main game class, containing the loops
 *
 * To-do before ALPHA release
 * IN PROGRESS
 *
 * <b><p> At least 5 types of NPC                                                                        </b></p>
 * <b><p> Ranged Combat 																                 </b></p>
 * <b><p> Combat (New animations, equipment framework + equipment animation framework)                   </b></p>
 * <b><p> Terraforming - Mining & Placing blocks                                                         </b></p>
 * <b><p> Generation                                                                                     </b></p>
 * <b><p> Fluids	                                                                                     </b></p>
 *
 * DONE
 *
 * <b><p> Resource gathering (At least the following: farming, hunting, foraging)			 	 		 </b></p>
 * <b><p> Customizable AI                                                                                </b></p>
 * <b><p> Window resizing                                          										 </b></p>
 * <b><p> Stockpiling                                              										 </b></p>
 * <b><p> Props (Trees, rocks, etc, and wiring these into generation)                                    </b></p>
 * <b><p> Prop and Construction (Prop construction and de-construction framework)                  		 </b></p>
 * <b><p> Main menu screen                                                                               </b></p>
 * <b><p> Dynamic tile-driven lighting                                      							 </b></p>
 * <b><p> Crafting and cooking                                     										 </b></p>
 * <b><p> Networking                                                                                     </b></p>
 * <b><p> Text input (Renaming elves, setting save path etc)                                             </b></p>
 * <b><p> Trading	                                              										 </b></p>
 * <b><p> Positional Indexing                                      										 </b></p>
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BloodAndMithrilClient implements ApplicationListener {
	public static boolean devMode = false;

	/** The game world */
	private static boolean inGame;

	/** True if game is paused */
	public static boolean paused;

	/** True if the world is currently being rendered */
	public static AtomicBoolean rendering = new AtomicBoolean(false);

	/** True if game is loading */
	public static boolean loading;

	@Inject
	private Timers timers;

	@Inject
	private Graphics graphics;

	@Inject
	private BloodAndMithrilClientInputProcessor processor;

	public static final HashSet<Integer> controlledFactions = Sets.newHashSet();
	private static final Collection<Mission> missions = new ConcurrentLinkedDeque<Mission>();
	private static final Map<Integer, Vector2> worldCamCoordinates = Maps.newHashMap();

	public static float updateRateMultiplier = 1f;

	@Override
	public void create() {
		// Load client-side resources
		ClientServerInterface.setClient(true);
		Wiring.setupInjector();

		loadResources();


		SoundService.changeMusic(2f, SoundService.mainMenu);

		ClientServerInterface.setServer(true);
		Domain.getWorlds().put(
			1,
			new World(1200, new Epoch(15.5f, 5, 22, 25), new ChunkGenerator(new MainMenuBiomeDecider())).setUpdateTick(1f/60f)
		);
		Domain.setActiveWorld(1);
		getGraphics().getCam().position.y = Layer.getCameraYForHorizonCoord(getGraphics().getHeight()/3);
		ClientServerInterface.setServer(false);

		Wiring.injector().injectMembers(this);
		setInputProcessor(processor);
	}


	/**
	 * Loads global resources, client side
	 */
	private void loadResources() {
		Domain.setup();
		WorldRenderer.setup();
		WorldRenderer.shapeRenderer = new ShapeRenderer();
		Fonts.setup();
		Individual.setup();
		Elf.setup();
		PrefabricatedComponent.setup();
		Topography.setup();
		Shaders.setup();
		Component.setup();
		WeatherRenderer.setup();
		Controls.setup();
		Equipable.setup();
		Prop.setup();
		GaussianLightingRenderer.setup();
		Item.setup();
		UserInterface.setup();

		UserInterface.UICamera = new OrthographicCamera(getGraphics().getWidth(), getGraphics().getHeight());
		UserInterface.UICamera.setToOrtho(false, getGraphics().getWidth(), getGraphics().getHeight());
		UserInterface.UICameraTrackingCam = new OrthographicCamera(getGraphics().getWidth(), getGraphics().getHeight());
		UserInterface.UICameraTrackingCam.setToOrtho(false, getGraphics().getWidth(), getGraphics().getHeight());

		UserInterface.addLayeredComponent(
			new MainMenuWindow(false)
		);
	}


	public static int getUpdateRate() {
		return Math.round(updateRateMultiplier);
	}


	@Override
	public void render() {
		try {
			processor.cameraControl();
			if (!GameSaver.isSaving()) {
				SoundService.update(Gdx.graphics.getDeltaTime());
			}

			float x = getGraphics().getCam().position.x;
			float y = getGraphics().getCam().position.y;
			getGraphics().getCam().position.x = Math.round(getGraphics().getCam().position.x);
			getGraphics().getCam().position.y = Math.round(getGraphics().getCam().position.y);

			// Topography backlog, must be done in main threadh because chunks rely on graphics ---------- /
			if (System.currentTimeMillis() - timers.topographyBacklogExecutionTimer > 100) {
				Topography.executeBackLog();
				timers.topographyBacklogExecutionTimer = System.currentTimeMillis();
			}

			// Camera --------------------- /
			getGraphics().getCam().update();
			UserInterface.update();

			// Blending --------------------- /
			Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			// Rendering --------------------- /
			if (Domain.getActiveWorld() != null && !loading) {
				WorldRenderer.render(Domain.getActiveWorld(), (int) getGraphics().getCam().position.x, (int) getGraphics().getCam().position.y);
			}

			// Fading --------------------- /
			fading();

			UserInterface.render();

			getGraphics().getCam().position.x = x;
			getGraphics().getCam().position.y = y;
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}


	private void fading() {
		if (graphics.isFading()) {
			if (graphics.getFadeAlpha() < 1f) {
				graphics.setFadeAlpha(graphics.getFadeAlpha() + 0.03f);
			} else {
				graphics.setFadeAlpha(1f);
			}
		} else {
			if (graphics.getFadeAlpha() > 0f) {
				graphics.setFadeAlpha(graphics.getFadeAlpha() - 0.03f);
			} else {
				graphics.setFadeAlpha(0f);
			}
		}

		Gdx.gl20.glEnable(GL20.GL_BLEND);
		UserInterface.shapeRenderer.begin(ShapeType.Filled);
		UserInterface.shapeRenderer.setColor(0, 0, 0, graphics.getFadeAlpha());
		UserInterface.shapeRenderer.rect(0, 0, getGraphics().getWidth(), getGraphics().getHeight());
		UserInterface.shapeRenderer.end();
		Gdx.gl20.glDisable(GL20.GL_BLEND);
	}


	@Override
	public void resize(int width, int height) {
		getGraphics().resize(width, height);

		ConfigPersistenceService.getConfig().setResX(width);
		ConfigPersistenceService.getConfig().setResY(height);
		ConfigPersistenceService.saveConfig();
	}


	@Override
	public void resume() {
		// Not called on PC
	}


	@Override
	public void dispose() {
	}


	@Override
	public void pause() {
		// Not called on PC
	}


	/**
	 * True is specified world coordinates are on screen within specified tolerance
	 */
	public static boolean isOnScreen(Vector2 position, float tolerance) {
		float screenX = worldToScreenX(position.x);
		float screenY = worldToScreenY(position.y);

		return screenX > -tolerance && screenX < getGraphics().getWidth() + tolerance && screenY > -tolerance && screenY < getGraphics().getHeight() + tolerance;
	}


	/**
	 * Get mouse world coord y
	 */
	public static Vector2 getMouseWorldCoords() {
		return new Vector2(getMouseWorldX(), getMouseWorldY());
	}


	/**
	 * Initial setup
	 */
	public static void setup() {
		SoundService.changeMusic(2f, SoundService.desertAmbient);
		UserInterface.contextMenus.clear();
		PositionalIndexingService.reindex();
	}


	/**
	 * @return whether the chunks on screen are generated/loaded
	 */
	public static boolean areChunksOnScreenGenerated() {
		int camX = (int) getGraphics().getCam().position.x;
		int camY = (int) getGraphics().getCam().position.y;

		int bottomLeftX = convertToChunkCoord((float)(camX - getGraphics().getWidth() / 2));
		int bottomLeftY = convertToChunkCoord((float)(camY - getGraphics().getHeight() / 2));
		int topRightX = bottomLeftX + convertToChunkCoord((float)getGraphics().getWidth());
		int topRightY = bottomLeftY + convertToChunkCoord((float)getGraphics().getHeight());

		World activeWorld = Domain.getActiveWorld();

		if (activeWorld == null) {
			return true;
		}

		Topography topography = activeWorld.getTopography();

		if (topography == null) {
			return true;
		}

		for (int chunkX = bottomLeftX - 2; chunkX <= topRightX + 2; chunkX++) {
			for (int chunkY = bottomLeftY - 2; chunkY <= topRightY + 2; chunkY++) {
				if (topography.getChunkMap().get(chunkX) == null || topography.getChunkMap().get(chunkX).get(chunkY) == null) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Sets the boolean value to indicate whether or not the loading screen should be rendered
	 */
	public static void setLoading(boolean loading) {
		BloodAndMithrilClient.loading = loading;
	}


	/**
	 * Instructs calling thread to sleep for specified number of milliseconds
	 */
	public static void threadWait(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Whether or not the client is currently in a game
	 */
	public static boolean isInGame() {
		return inGame;
	}


	/**
	 * Sets the inGame flag
	 */
	public static void setInGame(boolean inGame) {
		BloodAndMithrilClient.inGame = inGame;
	}


	public static void addMission(Mission m) {
		missions.add(m);

		SoundService.play(SoundService.newMission);
		Wiring.injector().getInstance(Threading.class).clientProcessingThreadPool.submit(() -> {
			for (int i = 0; i < 5; i++) {
				UserInterface.addUIFloatingText(
					"New mission!",
					Color.ORANGE,
					new Vector2(220, 60)
				);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {}
			}
		});
	}


	public static Collection<Mission> getMissions() {
		return missions;
	}


	public static Map<Integer, Vector2> getWorldcamcoordinates() {
		return worldCamCoordinates;
	}


	public static Graphics getGraphics() {
		return Wiring.injector().getInstance(Graphics.class);
	}
}
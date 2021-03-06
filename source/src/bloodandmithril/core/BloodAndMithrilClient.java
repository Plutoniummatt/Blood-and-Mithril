package bloodandmithril.core;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.setInputProcessor;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.audio.SoundService;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.Controls;
import bloodandmithril.generation.biome.MainMenuBiomeDecider;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.ResizeWindowService;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.tree.Tree;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.MainMenuWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.TopographyTaskExecutor;
import bloodandmithril.world.weather.WeatherRenderer;

/**
 * Main game class, containing the loops
 *
 * To-do before ALPHA release
 * IN PROGRESS
 *
 * <b><p> At least 5 types of NPC                                                                    </b></p>
 * <b><p> Ranged Combat 																             </b></p>
 * <b><p> Combat (New animations, equipment framework + equipment animation framework)               </b></p>
 * <b><p> Terraforming - Mining & Placing blocks                                                     </b></p>
 * <b><p> Generation                                                                                 </b></p>
 * <b><p> Fluids	                                                                                 </b></p>
 *
 * DONE
 *
 * <b><p> Resource gathering (At least the following: farming, hunting, foraging)			 	 	 </b></p>
 * <b><p> Customizable AI                                                                            </b></p>
 * <b><p> Window resizing                                          									 </b></p>
 * <b><p> Procedurally generated trees                             									 </b></p>
 * <b><p> Stockpiling                                              									 </b></p>
 * <b><p> Props (Trees, rocks, etc, and wiring these into generation)                                </b></p>
 * <b><p> Prop and Construction (Prop construction and de-construction framework)                  	 </b></p>
 * <b><p> Main menu screen                                                                           </b></p>
 * <b><p> Dynamic tile-driven lighting                                      						 </b></p>
 * <b><p> Crafting and cooking                                     									 </b></p>
 * <b><p> Networking                                                                                 </b></p>
 * <b><p> Text input (Renaming elves, setting save path etc)                                         </b></p>
 * <b><p> Trading	                                              									 </b></p>
 * <b><p> Positional Indexing                                      									 </b></p>
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BloodAndMithrilClient implements ApplicationListener {
	public static boolean devMode = false;

	@Inject	private Timers timers;
	@Inject	private Graphics graphics;
	@Inject	private ResizeWindowService resizeWindowService;
	@Inject	private BloodAndMithrilClientInputProcessor inputProcessor;
	@Inject	private GameSaver gameSaver;
	@Inject	private GameClientStateTracker gameClientStateTracker;
	@Inject	private WorldRenderer worldRenderer;
	@Inject private TopographyTaskExecutor topographyTaskExecutor;
	@Inject private UserInterface userInterface;
	@Inject private GaussianLightingRenderer gaussianLightingRenderer;
	@Inject private WeatherRenderer weatherRenderer;
	@Inject private ConfigPersistenceService configPersistenceService;

	@Override
	public void create() {
		Wiring.injector().injectMembers(this);
		
		loadResources();
		startMusic();
		createMainMenuWorld();
		setInputProcessor(inputProcessor);
	}


	private void startMusic() {
		SoundService.changeMusic(2f, SoundService.mainMenu);
	}


	private void createMainMenuWorld() {
		ClientServerInterface.setServer(true);
		Domain.addWorld(new World(1200, new Epoch(15.5f, 5, 22, 25), MainMenuBiomeDecider.class));
		gameClientStateTracker.setActiveWorldId(1);
		ClientServerInterface.setServer(false);
	}


	/**
	 * Loads global resources, client side
	 */
	private void loadResources() {
		worldRenderer.setup();
		Fonts.setup();
		PrefabricatedComponent.setup();
		Topography.setup();
		Shaders.setup();
		weatherRenderer.setup();
		Controls.setup();
		Equipable.setup();
		Prop.setup();
		gaussianLightingRenderer.setup();
		userInterface.setup();
		Item.setup();
		Component.setup();
		Tree.setup();

		userInterface.addLayeredComponent(
			new MainMenuWindow(false)
		);
	}


	@Override
	public void render() {
		try {
			inputProcessor.cameraControl();
			final float deltaTime = Gdx.graphics.getDeltaTime();
			if (!gameSaver.isSaving()) {
				SoundService.update(deltaTime);
			}

			final float x = graphics.getCam().position.x;
			final float y = graphics.getCam().position.y;
			graphics.getCam().position.x = Math.round(graphics.getCam().position.x);
			graphics.getCam().position.y = Math.round(graphics.getCam().position.y);

			// Topography backlog, must be done in main threadh because chunks rely on graphics ---------- /
			if (System.currentTimeMillis() - timers.topographyBacklogExecutionTimer > 100) {
				topographyTaskExecutor.executeBackLog();
				timers.topographyBacklogExecutionTimer = System.currentTimeMillis();
			}

			// Camera --------------------- /
			graphics.getCam().update();
			userInterface.update();

			// Blending --------------------- /
			Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			// Rendering --------------------- /
			if (gameClientStateTracker.getActiveWorld() != null && !gameClientStateTracker.isLoading()) {
				worldRenderer.render(gameClientStateTracker.getActiveWorld(), (int) graphics.getCam().position.x, (int) graphics.getCam().position.y);
			}

			// Fading --------------------- /
			fading();

			// UI Rendering --------------------- /
			userInterface.render();

			// Timer --------------------- /
			timers.renderUtilityTime += deltaTime;

			graphics.getCam().position.x = x;
			graphics.getCam().position.y = y;
		} catch (final Exception e) {
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
		userInterface.getShapeRenderer().begin(ShapeType.Filled);
		userInterface.getShapeRenderer().setColor(0, 0, 0, graphics.getFadeAlpha());
		userInterface.getShapeRenderer().rect(0, 0, graphics.getWidth(), graphics.getHeight());
		userInterface.getShapeRenderer().end();
		Gdx.gl20.glDisable(GL20.GL_BLEND);
	}


	@Override
	public void resize(final int width, final int height) {
		resizeWindowService.resize(width, height);

		configPersistenceService.getConfig().setResX(width);
		configPersistenceService.getConfig().setResY(height);
		configPersistenceService.saveConfig();
	}


	@Override
	public void resume() {
	}


	@Override
	public void dispose() {
	}


	@Override
	public void pause() {
	}


	/**
	 * Get mouse world coord y
	 */
	public static Vector2 getMouseWorldCoords() {
		return new Vector2(getMouseWorldX(), getMouseWorldY());
	}
}
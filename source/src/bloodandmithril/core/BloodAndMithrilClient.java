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
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.Controls;
import bloodandmithril.generation.biome.MainMenuBiomeDecider;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
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

	@Inject	private Timers timers;
	@Inject	private Graphics graphics;
	@Inject	private BloodAndMithrilClientInputProcessor inputProcessor;
	@Inject	private GameSaver gameSaver;
	@Inject	private GameClientStateTracker gameClientStateTracker;

	@Override
	public void create() {
		// Load client-side resources
		Wiring.setupInjector(new CommonModule());

		loadResources();

		SoundService.changeMusic(2f, SoundService.mainMenu);

		ClientServerInterface.setServer(true);
		Domain.addWorld(new World(1200, new Epoch(15.5f, 5, 22, 25), MainMenuBiomeDecider.class));
		Wiring.injector().injectMembers(this);
		gameClientStateTracker.setActiveWorldId(1);
		ClientServerInterface.setServer(false);


		setInputProcessor(inputProcessor);
	}


	/**
	 * Loads global resources, client side
	 */
	private void loadResources() {
		ClientServerInterface.setClient(true);

		WorldRenderer.setup();
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

		UserInterface.addLayeredComponent(
			new MainMenuWindow(false)
		);
	}


	@Override
	public void render() {
		try {
			inputProcessor.cameraControl();
			if (!gameSaver.isSaving()) {
				SoundService.update(Gdx.graphics.getDeltaTime());
			}

			final float x = graphics.getCam().position.x;
			final float y = graphics.getCam().position.y;
			graphics.getCam().position.x = Math.round(graphics.getCam().position.x);
			graphics.getCam().position.y = Math.round(graphics.getCam().position.y);

			// Topography backlog, must be done in main threadh because chunks rely on graphics ---------- /
			if (System.currentTimeMillis() - timers.topographyBacklogExecutionTimer > 100) {
				Topography.executeBackLog();
				timers.topographyBacklogExecutionTimer = System.currentTimeMillis();
			}

			// Camera --------------------- /
			graphics.getCam().update();
			graphics.getUi().update();

			// Blending --------------------- /
			Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			// Rendering --------------------- /
			if (gameClientStateTracker.getActiveWorld() != null && !gameClientStateTracker.isLoading()) {
				WorldRenderer.render(gameClientStateTracker.getActiveWorld(), (int) graphics.getCam().position.x, (int) graphics.getCam().position.y);
			}

			// Fading --------------------- /
			fading();

			// UI Rendering --------------------- /
			graphics.getUi().render();

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
		UserInterface.shapeRenderer.begin(ShapeType.Filled);
		UserInterface.shapeRenderer.setColor(0, 0, 0, graphics.getFadeAlpha());
		UserInterface.shapeRenderer.rect(0, 0, graphics.getWidth(), graphics.getHeight());
		UserInterface.shapeRenderer.end();
		Gdx.gl20.glDisable(GL20.GL_BLEND);
	}


	@Override
	public void resize(final int width, final int height) {
		graphics.resize(width, height);

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
	 * Get mouse world coord y
	 */
	public static Vector2 getMouseWorldCoords() {
		return new Vector2(getMouseWorldX(), getMouseWorldY());
	}
}
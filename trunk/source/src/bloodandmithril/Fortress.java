package bloodandmithril;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.character.individuals.Names;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.GameWorld.Light;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Main game class, containing the loops
 *
 * To-do before ALPHA release
 *
 * <b><p> Generation                                                                                     </b></p>
 * <b><p> Resource gathering and stockpiling (At least the following: farming, hunting, mining, foraging)</b></p>
 * <b><p> Props (Trees, rocks, etc, and wiring these into generation)                                    </b></p>
 * <b><p> Building and Construction (Building construction and decontruction framework)                  </b></p>
 * <b><p> Combat (New animations, equipment framework + equipment animation framework)                   </b></p>
 * <b><p> Terraforming - Mining & Placing blocks                                                         </b></p>
 * <b><p> Text input (Renaming elves, setting save path etc)                                             </b></p>
 * <b><p> Multiple saved games (Huge refactors of current statics various everywhere)                    </b></p>
 * <b><p> At least 5 types of NPC                                                                        </b></p>
 * <b><p> Main menu screen                                                                               </b></p>
 *
 * @author Matt
 */
public class Fortress implements ApplicationListener, InputProcessor {

	/** The maximum spread of individuals when going to location */
	private static final float INDIVIDUAL_SPREAD = 600f;

	/** See {@link #update(float)}, if delta is greater than this value, skip the update frame */
	private static final float LAG_SPIKE_TOLERANCE = Float.parseFloat(System.getProperty("lagSpikeTolerance"));

	/** The tolerance for double clicking */
	private static final float DOUBLE_CLICK_TIME = 0.2f;

	/** Resolution x */
	public static int WIDTH = Integer.parseInt(System.getProperty("resX"));

	/** Resolution y */
	public static int HEIGHT = Integer.parseInt(System.getProperty("resY"));

	/** 'THE' SpriteBatch */
	public static SpriteBatch spriteBatch;

	/** Camera used for the main game world */
	public static OrthographicCamera cam;

	/** The game world */
	public static GameWorld gameWorld;

	/** For camera dragging */
	private int camDragX, camDragY, oldCamX, oldCamY;

	/** True if game is paused */
	public static boolean paused = true;

	/** The current timer for double clicking */
	private float leftDoubleClickTimer = 0f;
	private float rightDoubleClickTimer = 0f;

	@Override
	public void create() {
		loadResources();

		spriteBatch = new SpriteBatch();

		cam = new OrthographicCamera(WIDTH, HEIGHT);
		cam.setToOrtho(false, WIDTH, HEIGHT);

		gameWorld = new GameWorld();

		Gdx.input.setInputProcessor(this);

		SoundService.changeMusic(2f, SoundService.music1);

		GameLoader.load();
	}


	/**
	 * Loads global resources
	 */
	private void loadResources() {
		Fonts.loadFonts();
		Individual.setup();
		Shaders.setup();
		Component.load();
		UserInterface.setup(WIDTH, HEIGHT);
		Weather.setup();
	}


	@Override
	public void render() {

		// Update
		update(Gdx.graphics.getDeltaTime());

		// Camera --------------------- /
		cam.update();
		UserInterface.update();

		// Blending --------------------- /
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		// Rendering --------------------- /
		gameWorld.render((int) cam.position.x, (int) cam.position.y);
		UserInterface.render();
	}


	@Override
	public void resize(int width, int height) {
		// No resize suport atm
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (GameSaver.isSaving()) {
			return false;
		}

		if (button == KeyMappings.leftClick) {
			leftClick(screenX, screenY);
		}

		if (button == KeyMappings.rightClick) {
			rightClick();
		}

		return false;
	}


	/**
	 * Called upon right clicking
	 */
	private void rightClick() {
		boolean doubleClick = rightDoubleClickTimer < DOUBLE_CLICK_TIME;
		rightDoubleClickTimer = 0f;

		if (!Gdx.input.isKeyPressed(KeyMappings.contextMenuBypass)) {
			UserInterface.rightClick();
		}

		if (UserInterface.contextMenus.isEmpty()) {
			for (Individual indi : GameWorld.selectedIndividuals) {
				indi.walking = !doubleClick;
				if (Gdx.input.isKeyPressed(Input.Keys.A)) {
					indi.ai.setCurrentTask(new MineTile(indi, new Vector2(getMouseWorldX(), getMouseWorldY())));
				} else {
					float spread = Math.min(indi.width * (Util.getRandom().nextFloat() - 0.5f) * 0.5f * (GameWorld.selectedIndividuals.size() - 1), INDIVIDUAL_SPREAD);
					AIProcessor.sendPathfindingRequest(
						indi,
						new WayPoint(
							new Vector2(
								getMouseWorldX() + spread,
								getMouseWorldY()
							)
						),
						false,
						150f,
						Gdx.input.isKeyPressed(KeyMappings.forceMove) ? false : true
					);
				}
			}
		}
	}


	/**
	 * Called upon left clicking
	 */
	private void leftClick(int screenX, int screenY) {
		boolean doubleClick = leftDoubleClickTimer < DOUBLE_CLICK_TIME;
		leftDoubleClickTimer = 0f;

		boolean uiClicked = UserInterface.leftClick();
		if (Gdx.input.isKeyPressed(KeyMappings.cameraDrag)) {
			saveCamDragCoordinates(screenX, screenY);
		}

		Individual individualClicked = null;
		for (Individual indi : GameWorld.individuals.values()) {
			if (indi.isMouseOver()) {
				individualClicked = indi;
			}
		}

		if (!uiClicked) {
			if (individualClicked == null) {
				if (doubleClick) {
					for (Individual indi : GameWorld.individuals.values()) {
						if (indi.controllable) {
							indi.deselect(false);
							GameWorld.selectedIndividuals.remove(indi);
						}
					}
					GameWorld.selectedIndividuals.clear();
				}

			} else {
				for (Individual indi : GameWorld.individuals.values()) {
					if (indi.controllable) {
						indi.deselect(false);
						GameWorld.selectedIndividuals.remove(indi);
					}
				}

				if (individualClicked.controllable) {
					GameWorld.selectedIndividuals.add(individualClicked);
					individualClicked.select();
				}

			}
		}

		if (Gdx.input.isKeyPressed(Input.Keys.E)) {
			GameWorld.lights.add(
				new Light(
					750,
					getMouseWorldX(), getMouseWorldY(),
					Util.randomOneOf(Color.WHITE, Color.CYAN, Color.GREEN, Color.ORANGE, Color.PINK, Color.MAGENTA, Color.YELLOW),
					1f
				)
			);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.V)) {
			Topography.changeTile(getMouseWorldX(), getMouseWorldY(), true, YellowBrickPlatform.class);
		}
	}


	@Override
	public boolean keyDown(int keycode) {
		if (GameSaver.isSaving()) {
			return false;
		}

		UserInterface.keyPressed(keycode);

		if (keycode == Input.Keys.R) {
			IndividualState state = new IndividualState(10f, 10f);
			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
			id.nickName = "Elfie";

			Elf elf = new Elf(
				id, state, true, true,
				new Color(0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 1),
				new Color(0.2f + 0.4f*Util.getRandom().nextFloat(), 0.2f + 0.3f*Util.getRandom().nextFloat(), 0.5f + 0.3f*Util.getRandom().nextFloat(), 1),
				Util.getRandom().nextInt(4),
				20f
			);

			elf.giveItem(new Carrot(), Util.getRandom().nextInt(50));
			elf.giveItem(new ChickenLeg(), Util.getRandom().nextInt(50));
			elf.giveItem(new ButterflySword(100), 1);
			elf.giveItem(new Broadsword(100), 1);

			GameWorld.individuals.put(elf.id.id, elf);
		}

		if (keycode == Input.Keys.U) {
			IndividualState state = new IndividualState(10f, 10f);
			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = new IndividualIdentifier("Unknown", "", new Epoch(10f, 12, 12, 2012));
			id.nickName = "Unknown";

			Boar boar = new Boar(id, state);

			GameWorld.individuals.put(boar.id.id, boar);
		}

		if (keycode == Input.Keys.T) {
			Individual individual = GameWorld.individuals.get(1);
			if (individual != null) {
				GameWorld.props.add(new Furnace(individual.state.position.x, individual.state.position.y));
			}
		}

		if (keycode == Input.Keys.I) {
			UserInterface.renderAvailableInterfaces = !UserInterface.renderAvailableInterfaces;
		}

		if (keycode == Input.Keys.B) {
			UserInterface.renderComponentBoundaries = !UserInterface.renderComponentBoundaries;
		}

		if (keycode == Input.Keys.F1) {
			UserInterface.addLayeredComponent(new TextInputWindow(WIDTH / 2, HEIGHT/2, 250, 100, "Test", 250, 100, new Task() {
				@Override
				public void execute() {
				}
			}));
		}

		return false;
	}


	@Override
	public boolean keyUp(int keycode) {
		return false;
	}


	@Override
	public boolean keyTyped(char character) {
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button == KeyMappings.leftClick) {
			UserInterface.leftClickRelease(screenX, Gdx.graphics.getHeight() - screenY);
		}
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (GameSaver.isSaving()) {
			return false;
		}

		if (Gdx.input.isButtonPressed(KeyMappings.leftClick)) {
			if (Gdx.input.isKeyPressed(KeyMappings.cameraDrag))  {
				cam.position.x = oldCamX + camDragX - screenX;
				cam.position.y = oldCamY + screenY - camDragY;
			}
		}
		return false;
	}


	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		return false;
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
	 * Converts screen coordinates to world coordinates
	 */
	private static float screenToWorldX(float screenX) {
		return cam.position.x - Gdx.graphics.getWidth()/2 + screenX;
	}


	/**
	 * Converts screen coordinates to world coordinates
	 */
	private static float screenToWorldY(float screenY) {
		return cam.position.y - Gdx.graphics.getHeight()/2 + screenY;
	}


	/**
	 * Converts world coordinates to screen coordinates
	 */
	public static float worldToScreenX(float worldX) {
		return Gdx.graphics.getWidth()/2 + (worldX - cam.position.x);
	}


	/**
	 * Converts world coordinates to screen coordinates
	 */
	public static float worldToScreenY(float worldY) {
		return Gdx.graphics.getHeight()/2 + (worldY - cam.position.y);
	}


	/**
	 * The game update logic, use delta to control the update speed to keep it
	 * constant.
	 */
	private void update(float delta) {
		SoundService.update(delta);
		GameSaver.update();

		// Do not update if game is paused
		// Do not update if FPS is lower than tolerance threshold, otherwise bad things can happen, like teleporting
		if (!paused && delta < LAG_SPIKE_TOLERANCE && !GameSaver.isSaving()) {
			Shaders.updateShaderUniforms();
			gameWorld.update(delta);
		}

		leftDoubleClickTimer += delta;
		rightDoubleClickTimer += delta;
	}


	/**
	 * Camera dragging processing
	 */
	private void saveCamDragCoordinates(int screenX, int screenY) {
		oldCamX = (int)cam.position.x;
		oldCamY = (int)cam.position.y;

		camDragX = screenX;
		camDragY = screenY;
	}


	/**
	 * Get mouse screen coord X
	 */
	public static int getMouseScreenX() {
		return Gdx.input.getX();
	}


	/**
	 * Get mouse screen coord y
	 */
	public static int getMouseScreenY() {
		return Gdx.graphics.getHeight() - Gdx.input.getY();
	}


	/**
	 * Get mouse world coord X
	 */
	public static float getMouseWorldX() {
		return screenToWorldX(Gdx.input.getX());
	}


	/**
	 * Get mouse world coord y
	 */
	public static float getMouseWorldY() {
		return screenToWorldY(Gdx.graphics.getHeight() - Gdx.input.getY());
	}
}
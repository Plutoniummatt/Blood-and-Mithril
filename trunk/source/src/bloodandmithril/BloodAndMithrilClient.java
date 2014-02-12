package bloodandmithril;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.character.individuals.Names;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.container.GlassBottle;
import bloodandmithril.item.material.liquid.Liquid.Water;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.item.material.plant.DeathCap;
import bloodandmithril.item.misc.Currency;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.prop.building.PineChest;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.MainMenuWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.GameWorld.Light;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

/**
 * Main game class, containing the loops
 *
 * To-do before ALPHA release
 *
 * <b><p> Building and Construction (Building construction and decontruction framework)                  </b></p>
 * <b><p> Multiple saved games????????? (Huge refactors of current statics various everywhere)           </b></p>
 * <b><p> At least 5 types of NPC                                                                        </b></p>
 * <b><p> Main menu screen                                                                               </b></p>
 *
 * IN PROGRESS
 *
 * <b><p> Resource gathering (At least the following: farming, hunting, foraging)			 	 		 </b></p>
 * <b><p> Combat (New animations, equipment framework + equipment animation framework)                   </b></p>
 * <b><p> Terraforming - Mining & Placing blocks                                                         </b></p>
 * <b><p> Props (Trees, rocks, etc, and wiring these into generation)                                    </b></p>
 * <b><p> Generation                                                                                     </b></p>
 * <b><p> Stockpiling                                              										 </b></p>
 *
 * DONE
 *
 * <b><p> Networking                                                                                     </b></p>
 * <b><p> Text input (Renaming elves, setting save path etc)                                             </b></p>
 * <b><p> Trading	                                              										 </b></p>
 *
 * @author Matt
 */
public class BloodAndMithrilClient implements ApplicationListener, InputProcessor {

	/** The maximum spread of individuals when going to location */
	private static final float INDIVIDUAL_SPREAD = 600f;

	/** See {@link #update(float)}, if delta is greater than this value, skip the update frame */
	private static final float LAG_SPIKE_TOLERANCE = 0.1f;

	/** The tolerance for double clicking */
	private static final float DOUBLE_CLICK_TIME = 0.2f;

	/** Resolution x */
	public static final int WIDTH = Integer.parseInt(System.getProperty("resX"));

	/** Resolution y */
	public static final int HEIGHT = Integer.parseInt(System.getProperty("resY"));

	/** 'THE' SpriteBatch */
	public static SpriteBatch spriteBatch;

	/** Camera used for the main game world */
	public static OrthographicCamera cam;

	/** The game world */
	public static GameWorld gameWorld;

	/** For camera dragging */
	private int camDragX, camDragY, oldCamX, oldCamY;

	/** True if game is paused */
	public static boolean paused = false;

	/** The current timer for double clicking */
	private float leftDoubleClickTimer = 0f;
	private float rightDoubleClickTimer = 0f;

	/** Client-side threadpool */
	public static ExecutorService newCachedThreadPool;

	public static final Set<Integer> controlledFactions = Sets.newHashSet();

	public static long ping = 0;

	@Override
	public void create() {
		// Load client-side resources
		loadResources();
		ClientServerInterface.setClient(true);

		spriteBatch = new SpriteBatch();

		cam = new OrthographicCamera(WIDTH, HEIGHT);
		cam.setToOrtho(false, WIDTH, HEIGHT);

		Gdx.input.setInputProcessor(this);

		SoundService.changeMusic(2f, SoundService.music1);

		newCachedThreadPool = Executors.newCachedThreadPool();
	}


	/**
	 * Loads global resources, client side
	 */
	private void loadResources() {
		GameWorld.setup();
		Fonts.setup();
		Individual.setup();
		Shaders.setup();
		Component.setup();
		Weather.setup();
		KeyMappings.setup();
		Equipable.setup();
		Prop.setup();

		UserInterface.UICamera = new OrthographicCamera(WIDTH, HEIGHT);
		UserInterface.UICamera.setToOrtho(false, WIDTH, HEIGHT);

		UserInterface.addLayeredComponent(
			new MainMenuWindow(
				BloodAndMithrilClient.WIDTH/2 - 100,
				BloodAndMithrilClient.HEIGHT/2 + 55,
				200,
				110,
				"Main menu",
				true,
				200,
				110,
				false
			)
		);
	}


	@Override
	public void render() {
		// Update
		if (gameWorld != null) {
			update(Gdx.graphics.getDeltaTime());
		}

		// Camera --------------------- /
		cam.update();
		UserInterface.update();

		// Blending --------------------- /
		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		// Rendering --------------------- /
		if (gameWorld == null) {
			renderMainMenuBackDrop();
		} else {
			gameWorld.render((int) cam.position.x, (int) cam.position.y);
		}
		UserInterface.render();
	}


	/**
	 * Renders the main menu
	 */
	private void renderMainMenuBackDrop() {
		UserInterface.shapeRenderer.begin(ShapeType.FilledRectangle);
		UserInterface.shapeRenderer.filledRect(0, 0, WIDTH, HEIGHT, Color.BLACK, Color.BLACK, Color.BLACK,Color.BLACK);
		UserInterface.shapeRenderer.end();
	}


	@Override
	public void resize(int width, int height) {
		// No resize support ATM
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
			for (Individual indi : Sets.newHashSet(GameWorld.selectedIndividuals)) {
				if (ClientServerInterface.isServer()) {
					indi.setWalking(!doubleClick);
				} else {
					ClientServerInterface.SendRequest.sendRunWalkRequest(indi.getId().getId(), !doubleClick);
				}
				if (Gdx.input.isKeyPressed(Input.Keys.A)) {
					if (ClientServerInterface.isServer()) {
						indi.getAI().setCurrentTask(new MineTile(indi, new Vector2(getMouseWorldX(), getMouseWorldY())));
					} else {
						ClientServerInterface.SendRequest.sendMineTileRequest(indi.getId().getId(), new Vector2(getMouseWorldX(), getMouseWorldY()));
					}
				} else {
					float spread = Math.min(indi.width * (Util.getRandom().nextFloat() - 0.5f) * 0.5f * (GameWorld.selectedIndividuals.size() - 1), INDIVIDUAL_SPREAD);
					if (ClientServerInterface.isServer()) {
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
							!Gdx.input.isKeyPressed(KeyMappings.forceMove)
						);
					} else {
						ClientServerInterface.SendRequest.sendMoveIndividualRequest(
							indi.getId().getId(), new Vector2(
								getMouseWorldX() + spread,
								getMouseWorldY()
							),
							!Gdx.input.isKeyPressed(KeyMappings.forceMove)
						);
					}
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
						if (indi.isControllable()) {
							if (ClientServerInterface.isServer()) {
								indi.deselect(false);
								GameWorld.selectedIndividuals.remove(indi);
							} else {
								ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
							}
						}
					}
					if (ClientServerInterface.isServer()) {
						GameWorld.selectedIndividuals.clear();
					}
				}

			} else {
				for (Individual indi : GameWorld.individuals.values()) {
					if (indi.isControllable() && indi.getId().getId() != individualClicked.getId().getId()) {
						if (ClientServerInterface.isServer()) {
							indi.deselect(false);
							GameWorld.selectedIndividuals.remove(indi);
						} else {
							ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
						}
					}
				}

				if (individualClicked.isControllable()) {
					if (ClientServerInterface.isServer()) {
						GameWorld.selectedIndividuals.add(individualClicked);
						individualClicked.select();
					} else {
						ClientServerInterface.SendRequest.sendIndividualSelectionRequest(individualClicked.getId().getId(), true);
					}
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
	}


	@Override
	public boolean keyDown(int keycode) {
		if (GameSaver.isSaving()) {
			return false;
		}

		if (UserInterface.keyPressed(keycode)) {
		  return false;
		}

		if (keycode == Input.Keys.R) {
			IndividualState state = new IndividualState(10f, 10f, 0.01f, 1f, 1f, 1f, 1f);
			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
			id.setNickName("Elfie");

			Elf elf = new Elf(
				id, state, Gdx.input.isKeyPressed(Input.Keys.Q) ? Faction.NPC : 1, true,
				new Color(0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 1),
				new Color(0.2f + 0.4f*Util.getRandom().nextFloat(), 0.2f + 0.3f*Util.getRandom().nextFloat(), 0.5f + 0.3f*Util.getRandom().nextFloat(), 1),
				Util.getRandom().nextInt(4),
				20f
			);

			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new Carrot());
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new DeathCap(false));
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new ChickenLeg());
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new GlassBottle(Water.class, 1f));
			}
			for (int i = Util.getRandom().nextInt(1000); i > 0; i--) {
				elf.giveItem(new Currency());
			}
			elf.giveItem(new ButterflySword(100));
			elf.giveItem(new Broadsword(100));

			GameWorld.individuals.put(elf.getId().getId(), elf);
		}

		if (keycode == Input.Keys.U) {
			IndividualState state = new IndividualState(10f, 10f, 0.01f, 1f, 1f, 1f, 1f);
			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = new IndividualIdentifier("Unknown", "", new Epoch(10f, 12, 12, 2012));
			id.setNickName("Unknown");

			Boar boar = new Boar(id, state);

			GameWorld.individuals.put(boar.getId().getId(), boar);
		}

		if (keycode == Input.Keys.T) {
			Individual individual = GameWorld.individuals.get(1);
			if (individual != null) {
				PineChest pineChest = new PineChest(individual.getState().position.x, individual.getState().position.y, true, 100f);
				GameWorld.props.put(pineChest.id, pineChest);
			}
		}
		
		if (keycode == Input.Keys.M) {
			Individual individual = GameWorld.individuals.get(1);
			if (individual != null) {
				Furnace furnace = new Furnace(individual.getState().position.x, individual.getState().position.y);
				GameWorld.props.put(furnace.id, furnace);
			}
		}

		if (keycode == Input.Keys.N) {
			Individual individual = GameWorld.individuals.get(1);
			if (individual != null) {
				bloodandmithril.prop.plant.Carrot carrot = new bloodandmithril.prop.plant.Carrot(individual.getState().position.x, individual.getState().position.y);
				GameWorld.props.put(carrot.id, carrot);
			}
		}

		if (keycode == Input.Keys.I) {
			UserInterface.renderAvailableInterfaces = !UserInterface.renderAvailableInterfaces;
		}

		if (keycode == Input.Keys.B) {
			UserInterface.renderComponentBoundaries = !UserInterface.renderComponentBoundaries;
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
			gameWorld.update((int) cam.position.x, (int) cam.position.y);
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
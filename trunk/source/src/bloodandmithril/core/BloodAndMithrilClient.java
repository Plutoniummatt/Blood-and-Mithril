package bloodandmithril.core;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.persistence.ConfigPersistenceService.getConfig;
import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static com.badlogic.gdx.Gdx.input;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.control.Controls;
import bloodandmithril.event.events.IndividualMoved;
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
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.cursorboundtask.ThrowItemCursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;
import bloodandmithril.world.weather.Weather;

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
public class BloodAndMithrilClient implements ApplicationListener, InputProcessor {
	public static boolean devMode = false;

	/** The game world */
	private static boolean inGame;

	/** True if game is paused */
	public static boolean paused;

	/** True if game is loading */
	public static boolean loading;

	@Inject
	private Timers timers;

	@Inject
	private Graphics graphics;

	private static Controls controls = getConfig().getKeyMappings();

	public static final HashSet<Integer> controlledFactions = Sets.newHashSet();
	private static final Collection<Mission> missions = new ConcurrentLinkedDeque<Mission>();

	private static CursorBoundTask cursorBoundTask = null;

	private static float updateRateMultiplier = 1f;

	@Override
	public void create() {
		// Load client-side resources
		Gdx.input.setInputProcessor(this);
		ClientServerInterface.setClient(true);
		Wiring.setup(new Module() {
			@Override
			public void configure(Binder binder) {
			}
		});

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
		Weather.setup();
		Controls.setup();
		Equipable.setup();
		Prop.setup();
		GaussianLightingRenderer.setup();
		Item.setup();

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


	public static Controls getKeyMappings() {
		return controls;
	}


	@Override
	public void render() {
		try {
			cameraControl();
			if (!GameSaver.isSaving()) {
				SoundService.update(Gdx.graphics.getDeltaTime());
			}

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
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		try {
			if (GameSaver.isSaving() || loading) {
				return false;
			}

			if (button == getKeyMappings().leftClick.keyCode) {
				leftClick(screenX, screenY);
			}

			if (button == getKeyMappings().rightClick.keyCode) {
				rightClick();
			}

			if (button == getKeyMappings().middleClick.keyCode) {
				middleClick(screenX, screenY);
			}
		} catch (NoTileFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	private void middleClick(int screenX, int screenY) {
		saveCamDragCoordinates(screenX, screenY);
	}


	/**
	 * Called upon right clicking
	 */
	@SuppressWarnings("unused")
	private void rightClick() throws NoTileFoundException {
		long currentTime = System.currentTimeMillis();
		boolean doubleClick = Controls.rightDoubleClickTimer + Controls.DOUBLE_CLICK_TIME > currentTime;
		boolean uiClicked = false;
		Controls.rightDoubleClickTimer = currentTime;

		if (cursorBoundTask != null && cursorBoundTask.canCancel()) {
			cursorBoundTask = null;
			return;
		}

		UserInterface.initialRightMouseDragCoordinates = new Vector2(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		if (Gdx.input.isKeyPressed(getKeyMappings().attack.keyCode) && !Gdx.input.isKeyPressed(getKeyMappings().rangedAttack.keyCode)) {
			meleeAttack();
		} else if (Gdx.input.isKeyPressed(getKeyMappings().rangedAttack.keyCode)) {
			rangedAttack();
		} else if (!Gdx.input.isKeyPressed(Keys.ANY_KEY)) {
			uiClicked = UserInterface.rightClick();
		}

		if (UserInterface.contextMenus.isEmpty() && !uiClicked && !Gdx.input.isKeyPressed(getKeyMappings().rightClickDragBox.keyCode) && !Gdx.input.isKeyPressed(getKeyMappings().attack.keyCode) && !Gdx.input.isKeyPressed(getKeyMappings().rangedAttack.keyCode)) {
			Vector2 mouseCoordinate = new Vector2(getMouseWorldX(), getMouseWorldY());
			for (Individual indi : Sets.newHashSet(Domain.getSelectedIndividuals())) {
				if (Gdx.input.isKeyPressed(getKeyMappings().mineTile.keyCode) && !Domain.getWorld(indi.getWorldId()).getTopography().getTile(mouseCoordinate, true).getClass().equals(EmptyTile.class)) {
					mineTile(mouseCoordinate, indi);
				} else if (Gdx.input.isKeyPressed(getKeyMappings().jump.keyCode)) {
					jump(indi);
				} else {
					moveIndividual(indi);
				}
			}
		}
	}


	private void moveIndividual(Individual indi) throws NoTileFoundException {
		float spread = Math.min(indi.getWidth() * (Util.getRandom().nextFloat() - 0.5f) * 0.5f * (Domain.getSelectedIndividuals().size() - 1), Controls.INDIVIDUAL_SPREAD);
		if (ClientServerInterface.isServer()) {
			AIProcessor.sendPathfindingRequest(
				indi,
				new WayPoint(
					Topography.convertToWorldCoord(
						getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
							new Vector2(
								getMouseWorldX() + (Gdx.input.isKeyPressed(getKeyMappings().forceMove.keyCode) ? 0f : spread),
								getMouseWorldY()
							),
							10,
							Domain.getWorld(indi.getWorldId())
						),
						true
					)
				),
				false,
				150f,
				!Gdx.input.isKeyPressed(getKeyMappings().forceMove.keyCode),
				Gdx.input.isKeyPressed(getKeyMappings().addWayPoint.keyCode)
			);

			Domain.getWorld(indi.getWorldId()).addEvent(new IndividualMoved(indi));
		} else {
			ClientServerInterface.SendRequest.sendMoveIndividualRequest(
				indi.getId().getId(),
				Topography.convertToWorldCoord(
					getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
						new Vector2(
							getMouseWorldX() + (Gdx.input.isKeyPressed(getKeyMappings().forceMove.keyCode) ? 0f : spread),
							getMouseWorldY()
						),
						10,
						Domain.getWorld(indi.getWorldId())
					),
					true
				),
				!Gdx.input.isKeyPressed(getKeyMappings().forceMove.keyCode),
				Gdx.input.isKeyPressed(getKeyMappings().addWayPoint.keyCode),
				false, null, null
			);
		}
	}


	private void jump(Individual indi) {
		if (ClientServerInterface.isServer()) {
			AIProcessor.sendJumpResolutionRequest(
				indi,
				indi.getState().position.cpy(),
				new Vector2(getMouseWorldX(), getMouseWorldY()),
				Gdx.input.isKeyPressed(getKeyMappings().addWayPoint.keyCode)
			);
		} else {
			ClientServerInterface.SendRequest.sendMoveIndividualRequest(
				indi.getId().getId(),
				null,
				!Gdx.input.isKeyPressed(getKeyMappings().forceMove.keyCode),
				Gdx.input.isKeyPressed(getKeyMappings().addWayPoint.keyCode),
				true,
				indi.getState().position.cpy(),
				new Vector2(getMouseWorldX(), getMouseWorldY())
			);
		}
	}


	private void mineTile(Vector2 mouseCoordinate, Individual indi) {
		if (ClientServerInterface.isServer()) {
			indi.getAI().setCurrentTask(new MineTile(indi, mouseCoordinate));
		} else {
			ClientServerInterface.SendRequest.sendMineTileRequest(indi.getId().getId(), new Vector2(getMouseWorldX(), getMouseWorldY()));
		}
	}


	private void rangedAttack() {
		for (Individual selected : Domain.getSelectedIndividuals()) {
			if (selected.canAttackRanged()) {
				if (ClientServerInterface.isServer()) {
					selected.attackRanged(new Vector2(getMouseWorldX(), getMouseWorldY()));
				} else {
					ClientServerInterface.SendRequest.sendAttackRangedRequest(selected, new Vector2(getMouseWorldX(), getMouseWorldY()));
				}
			}
		}
	}


	private void meleeAttack() {
		if (!Domain.getSelectedIndividuals().isEmpty()) {
			for (final int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				Individual indi = Domain.getIndividual(indiKey);
				if (indi.isMouseOver() && indi.isAlive()) {
					for (Individual selected : Domain.getSelectedIndividuals()) {
						if (indi == selected) {
							continue;
						}

						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(new Attack(selected, indi));
						} else {
							ClientServerInterface.SendRequest.sendRequestAttack(selected, indi);
						}
					}
					break;
				}
			}
		}
	}


	/**
	 * Called upon left clicking
	 */
	private void leftClick(int screenX, int screenY) {

		long currentTimeMillis = System.currentTimeMillis();

		boolean doubleClick = Controls.leftDoubleClickTimer + Controls.DOUBLE_CLICK_TIME > currentTimeMillis;
		Controls.leftDoubleClickTimer = currentTimeMillis;

		boolean uiClicked = UserInterface.leftClick();

		Individual individualClicked = null;
		if (Domain.getActiveWorld() != null) {
			for (int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				Individual indi = Domain.getIndividual(indiKey);
				if (indi.isMouseOver()) {
					individualClicked = indi;
				}
			}
		}

		if (!uiClicked) {
			if (getCursorBoundTask() != null) {
				if (getCursorBoundTask().executionConditionMet()) {
					if (getCursorBoundTask().isWorldCoordinate()) {
						setCursorBoundTask(getCursorBoundTask().execute(
							(int) getMouseWorldX(),
							(int) getMouseWorldY()
						));
					} else {
						setCursorBoundTask(getCursorBoundTask().execute(
							getMouseScreenX(),
							getMouseScreenY()
						));
					}
				}
				return;
			}

			if (individualClicked == null) {
				if (doubleClick && (cursorBoundTask == null || !(cursorBoundTask instanceof ThrowItemCursorBoundTask))) {
					for (Individual indi : Domain.getIndividuals().values()) {
						if (indi.isControllable()) {
							if (ClientServerInterface.isServer()) {
								indi.deselect(false, 0);
								Domain.removeSelectedIndividual(indi);
							} else {
								ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
							}
						}
					}
					if (ClientServerInterface.isServer()) {
						Domain.clearSelectedIndividuals();
					}
				}
			} else {
				for (Individual indi : Domain.getIndividuals().values()) {
					if (indi.isControllable() && indi.getId().getId() != individualClicked.getId().getId() && !input.isKeyPressed(getKeyMappings().selectIndividual.keyCode)) {
						if (ClientServerInterface.isServer()) {
							indi.deselect(false, 0);
							Domain.removeSelectedIndividual(indi);
						} else {
							ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
						}
					}
				}

				if (individualClicked.isControllable() && individualClicked.isAlive()) {
					if (ClientServerInterface.isServer()) {
						Domain.addSelectedIndividual(individualClicked);;
						individualClicked.select(0);
					} else {
						ClientServerInterface.SendRequest.sendIndividualSelectionRequest(individualClicked.getId().getId(), true);
					}
				}

			}
		}
	}


	@Override
	public boolean keyDown(int keycode) {
		try {
			if (GameSaver.isSaving() || loading) {
				return false;
			}

			if (UserInterface.keyPressed(keycode)) {
				return false;
			} else if (Keys.ESCAPE == keycode) {
				UserInterface.addLayeredComponentUnique(
					new MainMenuWindow(true)
				);
			} else {
				if (keycode == getKeyMappings().speedUp.keyCode) {
					if (updateRateMultiplier < 16f) {
						updateRateMultiplier = Math.round(updateRateMultiplier) + 1;
					}
				}
				if (keycode == getKeyMappings().slowDown.keyCode) {
					if (updateRateMultiplier > 1f) {
						updateRateMultiplier = Math.round(updateRateMultiplier) - 1;
					}
				}
				if (cursorBoundTask != null) {
					cursorBoundTask.keyPressed(keycode);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
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
		try {
			if (button == getKeyMappings().leftClick.keyCode) {
				UserInterface.leftClickRelease(screenX, getGraphics().getHeight() - screenY);
			}

			if (button == getKeyMappings().rightClick.keyCode) {
				UserInterface.rightClickRelease(screenX, getGraphics().getHeight() - screenY);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (GameSaver.isSaving()) {
			return false;
		}

		if (Gdx.input.isButtonPressed(getKeyMappings().middleClick.keyCode) && isInGame()) {
			getGraphics().getCam().position.x = Controls.oldCamX + Controls.camDragX - screenX;
			getGraphics().getCam().position.y = Controls.oldCamY + screenY - Controls.camDragY;
		}
		return false;
	}


	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		try {
			UserInterface.scrolled(amount);
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
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
	 * Converts screen coordinates to world coordinates
	 */
	public static float screenToWorldX(float screenX) {
		return getGraphics().getCam().position.x - getGraphics().getWidth()/2 + screenX;
	}


	/**
	 * Converts screen coordinates to world coordinates
	 */
	public static float screenToWorldY(float screenY) {
		return getGraphics().getCam().position.y - getGraphics().getHeight()/2 + screenY;
	}


	/**
	 * Converts world coordinates to screen coordinates
	 */
	public static float worldToScreenX(float worldX) {
		return getGraphics().getWidth()/2 + (worldX - getGraphics().getCam().position.x);
	}


	/**
	 * Converts world coordinates to screen coordinates
	 */
	public static Vector2 worldToScreen(Vector2 world) {
		return new Vector2(worldToScreenX(world.x), worldToScreenY(world.y));
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
	 * Converts world coordinates to screen coordinates
	 */
	public static float worldToScreenY(float worldY) {
		return getGraphics().getHeight()/2 + (worldY - getGraphics().getCam().position.y);
	}


	/**
	 * Camera movement controls
	 */
	private void cameraControl() {
		if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && isInGame()) {
			if (Gdx.input.isKeyPressed(getKeyMappings().moveCamUp.keyCode)){
				getGraphics().getCam().position.y += 10f;
			}
			if (Gdx.input.isKeyPressed(getKeyMappings().moveCamDown.keyCode)){
				getGraphics().getCam().position.y -= 10f;
			}
			if (Gdx.input.isKeyPressed(getKeyMappings().moveCamLeft.keyCode)){
				getGraphics().getCam().position.x -= 10f;
			}
			if (Gdx.input.isKeyPressed(getKeyMappings().moveCamRight.keyCode)){
				getGraphics().getCam().position.x += 10f;
			}
		}
	}


	/**
	 * Camera dragging processing
	 */
	private void saveCamDragCoordinates(int screenX, int screenY) {
		Controls.oldCamX = (int)getGraphics().getCam().position.x;
		Controls.oldCamY = (int)getGraphics().getCam().position.y;

		Controls.camDragX = screenX;
		Controls.camDragY = screenY;
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
		return getGraphics().getHeight() - Gdx.input.getY();
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
		return screenToWorldY(getGraphics().getHeight() - Gdx.input.getY());
	}


	/**
	 * Get mouse world coord y
	 */
	public static Vector2 getMouseWorldCoords() {
		return new Vector2(getMouseWorldX(), getMouseWorldY());
	}


	/**
	 * @return the active {@link CursorBoundTask}
	 */
	public static CursorBoundTask getCursorBoundTask() {
		return cursorBoundTask;
	}


	/**
	 * @param cursorBoundTask to set
	 */
	public static void setCursorBoundTask(CursorBoundTask cursorBoundTask) {
		BloodAndMithrilClient.cursorBoundTask = cursorBoundTask;
	}


	/**
	 * Initial setup
	 */
	public static void setup() {
		UserInterface.setup();

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





	public static Collection<Mission> getMissions() {
		return missions;
	}


	public static Graphics getGraphics() {
		return Wiring.injector().getInstance(Graphics.class);
	}
}
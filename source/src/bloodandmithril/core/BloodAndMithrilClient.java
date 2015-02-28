package bloodandmithril.core;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.world.topography.Topography.convertToChunkCoord;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.performance.PositionalReindexingService;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.MainMenuWindow;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

/**
 * Main game class, containing the loops
 *
 * To-do before ALPHA release
 * IN PROGRESS
 *
 * <b><p> At least 5 types of NPC                                                                        </b></p>
 * <b><p> Ranged Combat 																                 </b></p>
 * <b><p> Main menu screen                                                                               </b></p>
 * <b><p> Prop and Construction (Prop construction and de-construction framework)                  		 </b></p>
 * <b><p> Resource gathering (At least the following: farming, hunting, foraging)			 	 		 </b></p>
 * <b><p> Combat (New animations, equipment framework + equipment animation framework)                   </b></p>
 * <b><p> Terraforming - Mining & Placing blocks                                                         </b></p>
 * <b><p> Props (Trees, rocks, etc, and wiring these into generation)                                    </b></p>
 * <b><p> Generation                                                                                     </b></p>
 * <b><p> Fluids	                                                                                     </b></p>
 *
 * DONE
 *
 * <b><p> Stockpiling                                              										 </b></p>
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

	/** The maximum spread of individuals when going to location */
	private static final float INDIVIDUAL_SPREAD = 600f;

	/** The tolerance for double clicking */
	private static final float DOUBLE_CLICK_TIME = 0.25f;

	/** Resolution x */
	public static final int WIDTH = ConfigPersistenceService.getConfig().getResX();

	/** Resolution y */
	public static final int HEIGHT = ConfigPersistenceService.getConfig().getResY();

	/** 'THE' SpriteBatch */
	public static SpriteBatch spriteBatch;

	/** Camera used for the main game world */
	public static OrthographicCamera cam;

	/** The game world */
	public static Domain domain;

	/** For camera dragging */
	private int camDragX, camDragY, oldCamX, oldCamY;

	/** True if game is paused */
	public static boolean paused = false, loading = false;

	/** The current timer for double clicking */
	private float leftDoubleClickTimer = 0f;
	private float rightDoubleClickTimer = 0f;

	/** Client-side threadpool */
	public static ExecutorService clientCSIThread;

	public static final HashSet<Integer> controlledFactions = Sets.newHashSet();

	public static long ping = 0;

	public static Thread updateThread;
	public static Thread topographyQueryThread;
	public static Thread particleUpdateThread;

	private long topographyBacklogExecutionTimer;

	private static CursorBoundTask cursorBoundTask = null;

	public static int camMarginX, camMarginY;

	static {
		camMarginX = 640 + 32 - WIDTH % 32;
		camMarginY = 640 + 32 - HEIGHT % 32;
	}

	@Override
	public void create() {
		// Load client-side resources
		ClientServerInterface.setClient(true);
		loadResources();

		spriteBatch = new SpriteBatch();

		cam = new OrthographicCamera(WIDTH + camMarginX, HEIGHT + camMarginY);
		cam.setToOrtho(false, WIDTH + camMarginX, HEIGHT + camMarginY);

		Gdx.input.setInputProcessor(this);

		SoundService.changeMusic(2f, SoundService.mainMenu);

		clientCSIThread = Executors.newCachedThreadPool();

		updateThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > 16) {
					prevFrame = System.currentTimeMillis();
					update(Gdx.graphics.getDeltaTime());
				}
			}
		});

		topographyQueryThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > 16) {
					if (Domain.getActiveWorld() != null) {
						Domain.getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToCam((int) cam.position.x, (int) cam.position.y);
					}
				}
			}
		});

		particleUpdateThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > 16) {
					prevFrame = System.currentTimeMillis();
					World world = Domain.getActiveWorld();
					if (world != null) {
						Collection<Particle> particles = world.getClientParticles();
						for (Particle p : particles) {
							if (p.getRemovalCondition().call()) {
								Domain.getActiveWorld().getClientParticles().remove(p);
							}
							try {
								p.update(0.012f);
							} catch (NoTileFoundException e) {}
						}
					}
				}
			}
		});


		updateThread.setPriority(Thread.MAX_PRIORITY);
		updateThread.setName("Update thread");
		updateThread.start();

		particleUpdateThread.setName("Particle thread");
		particleUpdateThread.start();

		topographyQueryThread.setName("Topography query thread");
		topographyQueryThread.start();
	}


	/**
	 * Loads global resources, client side
	 */
	private void loadResources() {
		Domain.setup();
		Domain.shapeRenderer = new ShapeRenderer();
		Fonts.setup();
		Individual.setup();
		PrefabricatedComponent.setup();
		Topography.setup();
		Shaders.setup();
		Component.setup();
		Weather.setup();
		KeyMappings.setup();
		Equipable.setup();
		Prop.setup();
		GaussianLightingRenderer.setup();
		Item.setup();

		UserInterface.UICamera = new OrthographicCamera(WIDTH, HEIGHT);
		UserInterface.UICamera.setToOrtho(false, WIDTH, HEIGHT);
		UserInterface.UICameraTrackingCam = new OrthographicCamera(WIDTH, HEIGHT);
		UserInterface.UICameraTrackingCam.setToOrtho(false, WIDTH, HEIGHT);

		UserInterface.addLayeredComponent(
			new MainMenuWindow(
				BloodAndMithrilClient.WIDTH/2 - 100,
				BloodAndMithrilClient.HEIGHT/2 + 55,
				200,
				130,
				"",
				true,
				200,
				130,
				false,
				false
			)
		);
	}


	@Override
	public void render() {
		try {
			cameraMovement();
			if (!GameSaver.isSaving()) {
				SoundService.update(Gdx.graphics.getDeltaTime());
				Shaders.updateShaderUniforms();
			}

			// Topography backlog ---------- /
			if (System.currentTimeMillis() - topographyBacklogExecutionTimer > 100) {
				Topography.executeBackLog();
				topographyBacklogExecutionTimer = System.currentTimeMillis();
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
			if (domain == null) {
				renderMainMenuBackDrop();
			} else {
				domain.render((int) cam.position.x, (int) cam.position.y);
			}
			UserInterface.render();
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
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
		try {
			if (GameSaver.isSaving()) {
				return false;
			}

			if (button == KeyMappings.leftClick) {
				leftClick(screenX, screenY);
			}

			if (button == KeyMappings.rightClick) {
				rightClick();
			}

			if (button == KeyMappings.middleClick) {
				middleClick(screenX, screenY);
			}
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
		boolean doubleClick = rightDoubleClickTimer < DOUBLE_CLICK_TIME;
		boolean uiClicked = false;
		rightDoubleClickTimer = 0f;

		if (cursorBoundTask != null) {
			cursorBoundTask = null;
			return;
		}

		UserInterface.initialRightMouseDragCoordinates = new Vector2(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		if (Gdx.input.isKeyPressed(KeyMappings.attack) && !Gdx.input.isKeyPressed(KeyMappings.rangedAttack)) {
			if (!Domain.getSelectedIndividuals().isEmpty()) {
				boolean attacked = false;
				for (final int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY())) {
					Individual indi = Domain.getIndividual(indiKey);
					if (indi.isMouseOver()) {
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
						attacked = true;
						break;
					}
				}
			}
		} else if (Gdx.input.isKeyPressed(KeyMappings.rangedAttack)) {
			for (Individual selected : Domain.getSelectedIndividuals()) {
				if (selected.canAttackRanged()) {
					if (ClientServerInterface.isServer()) {
						selected.attackRanged(new Vector2(getMouseWorldX(), getMouseWorldY()));
					} else {
						ClientServerInterface.SendRequest.sendAttackRangedRequest(selected, new Vector2(getMouseWorldX(), getMouseWorldY()));
					}
				}
			}
		} else if (!Gdx.input.isKeyPressed(KeyMappings.contextMenuBypass)) {
			uiClicked = UserInterface.rightClick();
		}

		if (UserInterface.contextMenus.isEmpty() && !uiClicked && !Gdx.input.isKeyPressed(KeyMappings.rightClickDragBox) && !Gdx.input.isKeyPressed(KeyMappings.attack) && !Gdx.input.isKeyPressed(KeyMappings.rangedAttack)) {
			for (Individual indi : Sets.newHashSet(Domain.getSelectedIndividuals())) {
				if (Gdx.input.isKeyPressed(KeyMappings.mineTile)) {
					if (ClientServerInterface.isServer()) {
						indi.getAI().setCurrentTask(new MineTile(indi, new Vector2(getMouseWorldX(), getMouseWorldY())));
					} else {
						ClientServerInterface.SendRequest.sendMineTileRequest(indi.getId().getId(), new Vector2(getMouseWorldX(), getMouseWorldY()));
					}
				} else if (Gdx.input.isKeyPressed(KeyMappings.jump)) {
					if (ClientServerInterface.isServer()) {
						AIProcessor.sendJumpResolutionRequest(
							indi,
							indi.getState().position.cpy(),
							new Vector2(getMouseWorldX(), getMouseWorldY()),
							Gdx.input.isKeyPressed(KeyMappings.addWayPoint)
						);
					} else {
						ClientServerInterface.SendRequest.sendMoveIndividualRequest(
							indi.getId().getId(),
							null,
							!Gdx.input.isKeyPressed(KeyMappings.forceMove),
							Gdx.input.isKeyPressed(KeyMappings.addWayPoint),
							true,
							indi.getState().position.cpy(),
							new Vector2(getMouseWorldX(), getMouseWorldY())
						);
					}
				} else {
					float spread = Math.min(indi.getWidth() * (Util.getRandom().nextFloat() - 0.5f) * 0.5f * (Domain.getSelectedIndividuals().size() - 1), INDIVIDUAL_SPREAD);
					if (ClientServerInterface.isServer()) {
						AIProcessor.sendPathfindingRequest(
							indi,
							new WayPoint(
								Topography.convertToWorldCoord(
									getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
										new Vector2(
											getMouseWorldX() + (Gdx.input.isKeyPressed(KeyMappings.forceMove) ? 0f : spread),
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
							!Gdx.input.isKeyPressed(KeyMappings.forceMove),
							Gdx.input.isKeyPressed(KeyMappings.addWayPoint)
						);
					} else {
						ClientServerInterface.SendRequest.sendMoveIndividualRequest(
							indi.getId().getId(),
							Topography.convertToWorldCoord(
								getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
									new Vector2(
										getMouseWorldX() + (Gdx.input.isKeyPressed(KeyMappings.forceMove) ? 0f : spread),
										getMouseWorldY()
									),
									10,
									Domain.getWorld(indi.getWorldId())
								),
								true
							),
							!Gdx.input.isKeyPressed(KeyMappings.forceMove),
							Gdx.input.isKeyPressed(KeyMappings.addWayPoint),
							false, null, null
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

		Individual individualClicked = null;
		if (Domain.getActiveWorld() != null) {
			for (int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY())) {
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
						getCursorBoundTask().execute(
							(int) getMouseWorldX(),
							(int) getMouseWorldY()
						);
					} else {
						getCursorBoundTask().execute(
							getMouseScreenX(),
							getMouseScreenY()
						);
					}
					setCursorBoundTask(null);
				}
				return;
			}

			if (individualClicked == null) {
				if (doubleClick) {
					for (Individual indi : Domain.getIndividuals().values()) {
						if (indi.isControllable()) {
							if (ClientServerInterface.isServer()) {
								indi.deselect(false, 0);
								Domain.getSelectedIndividuals().remove(indi);
							} else {
								ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
							}
						}
					}
					if (ClientServerInterface.isServer()) {
						Domain.getSelectedIndividuals().clear();
					}
				}

			} else {
				for (Individual indi : Domain.getIndividuals().values()) {
					if (indi.isControllable() && indi.getId().getId() != individualClicked.getId().getId()) {
						if (ClientServerInterface.isServer()) {
							indi.deselect(false, 0);
							Domain.getSelectedIndividuals().remove(indi);
						} else {
							ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
						}
					}
				}

				if (individualClicked.isControllable()) {
					if (ClientServerInterface.isServer()) {
						Domain.getSelectedIndividuals().add(individualClicked);
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
			if (GameSaver.isSaving()) {
				return false;
			}

			if (UserInterface.keyPressed(keycode)) {
				return false;
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
			if (button == KeyMappings.leftClick) {
				UserInterface.leftClickRelease(screenX, Gdx.graphics.getHeight() - screenY);
			}

			if (button == KeyMappings.rightClick) {
				UserInterface.rightClickRelease(screenX, Gdx.graphics.getHeight() - screenY);
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

		if (Gdx.input.isButtonPressed(KeyMappings.middleClick)) {
			cam.position.x = oldCamX + camDragX - screenX;
			cam.position.y = oldCamY + screenY - camDragY;
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
	public static float screenToWorldX(float screenX) {
		return cam.position.x - Gdx.graphics.getWidth()/2 + screenX;
	}


	/**
	 * Converts screen coordinates to world coordinates
	 */
	public static float screenToWorldY(float screenY) {
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
	public static Vector2 worldToScreen(Vector2 world) {
		return new Vector2(worldToScreenX(world.x), worldToScreenY(world.y));
	}


	/**
	 * True is specified world coordinates are on screen within specified tolerance
	 */
	public static boolean isOnScreen(Vector2 position, float tolerance) {
		float screenX = worldToScreenX(position.x);
		float screenY = worldToScreenY(position.y);

		return screenX > -tolerance && screenX < WIDTH + tolerance && screenY > -tolerance && screenY < HEIGHT + tolerance;
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
		try {
			GameSaver.update();

			// Do not update if game is paused
			// Do not update if FPS is lower than tolerance threshold, otherwise bad things can happen, like teleporting
			if (!paused && !GameSaver.isSaving() && domain != null) {
				domain.update((int) cam.position.x, (int) cam.position.y);
			}

			leftDoubleClickTimer += delta;
			rightDoubleClickTimer += delta;
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}


	private void cameraMovement() {
		if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
			if (Gdx.input.isKeyPressed(Keys.UP)){
				cam.position.y += 10f;
			}
			if (Gdx.input.isKeyPressed(Keys.DOWN)){
				cam.position.y -= 10f;
			}
			if (Gdx.input.isKeyPressed(Keys.LEFT)){
				cam.position.x -= 10f;
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)){
				cam.position.x += 10f;
			}
		}
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


	public static CursorBoundTask getCursorBoundTask() {
		return cursorBoundTask;
	}


	public static void setCursorBoundTask(CursorBoundTask cursorBoundTask) {
		BloodAndMithrilClient.cursorBoundTask = cursorBoundTask;
	}


	public static void setup() {
		UserInterface.setup();

		SoundService.changeMusic(2f, SoundService.desertNight);
		UserInterface.contextMenus.clear();
		PositionalReindexingService.reindex();
	}
	
	
	public static boolean areChunksOnScreenGenerated() {
		int camX = (int) cam.position.x;
		int camY = (int) cam.position.y;
		
		int bottomLeftX = convertToChunkCoord((float)(camX - WIDTH / 2));
		int bottomLeftY = convertToChunkCoord((float)(camY - HEIGHT / 2));
		int topRightX = bottomLeftX + convertToChunkCoord((float)WIDTH);
		int topRightY = bottomLeftY + convertToChunkCoord((float)HEIGHT);
		
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
	
	
	public static void setLoading(boolean loading) {
		BloodAndMithrilClient.loading = loading;
	}
	
	
	public static void threadWait(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
package bloodandmithril.ui;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isButtonPressed;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.control.InputUtilities.screenToWorldX;
import static bloodandmithril.control.InputUtilities.screenToWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static bloodandmithril.item.items.equipment.weapon.RangedWeapon.rangeControl;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.networking.ClientServerInterface.ping;
import static bloodandmithril.ui.FloatingText.floatingText;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Threading;
import bloodandmithril.core.Wiring;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.interfaces.Interface;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.EquipperImpl.AlwaysTrueFunction;
import bloodandmithril.item.items.equipment.EquipperImpl.FalseFunction;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.performance.PositionalIndexMap;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.InfoPopup;
import bloodandmithril.ui.components.TextBubble;
import bloodandmithril.ui.components.TextBubble.TextBubbleSerializableBean;
import bloodandmithril.ui.components.bar.BottomBar;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Class representing UI
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class UserInterface {

	private static final Color DARK_SCREEN_COLOR = new Color(0f, 0f, 0f, 0.8f);
	private static final Color EXISTING_INTERFACE_COLOR = new Color(1f, 0.2f, 0f, 0.5f);
	private static final Color AVAILABLE_INTERFACE_COLOR = new Color(0.2f, 1f, 0f, 0.5f);
	private static final Color COMPONENT_BOUNDARY_COLOR = new Color(1f, 1f, 1f, 0.5f);
	private static final Color COMPONENT_FILL_COLOR = new Color(0f, 1f, 0f, 0.15f);

	/** UI camera */
	private OrthographicCamera UICamera;
	private OrthographicCamera UICameraTrackingCam;

	/** List of {@link Button}s */
	public HashMap<String, Button> buttons = newHashMap();

	/** Unpause button */
	public static Button unpauseButton, savingButton;

	/** {@link ContextMenu}s */
	public static List<ContextMenu> contextMenus = new ArrayList<ContextMenu>();

	/** {@link Window}s */
	private static ArrayDeque<Component> layeredComponents = new ArrayDeque<Component>();

	/** {@link TextBubble}s */
	public static ArrayDeque<TextBubble> textBubbles = new ArrayDeque<TextBubble>();

	/** Shape renderer */
	public static ShapeRenderer shapeRenderer;

	/** The texture atlas for other UI elements */
	public static Texture uiTexture;
	public static Texture iconTexture;

	/** Initial coordinates for the drag box, see {@link #renderDragBox()} */
	public static Vector2 initialLeftMouseDragCoordinates = null;
	public static Vector2 initialRightMouseDragCoordinates = null;

	/** A flag to indicate whether we should render the available interfaces or existing interfaces */
	public static boolean renderAvailableInterfaces = true, renderComponentBoundaries = true;

	/** Whether to render debug UI */
	public static boolean DEBUG = false;

	/** Whether to render debug UI */
	public static boolean RENDER_TOPOGRAPHY = false;

	/** FPS should be updated twice per second */
	private static int fpsTimer, fps, fpsDisplayed;

	/** The mouse-over info popup */
	private static InfoPopup infoPopup;

	private static float averageBarAlpha = 0f, maxHealth = 0f, totalHealth = 0, maxStamina = 0f, totalStamina = 0f, maxMana = 0f, totalMana = 0;

	/** Texture regions */
	public static TextureRegion finalWaypointTexture;
	public static TextureRegion jumpWaypointTexture;
	public static TextureRegion currentArrow;
	public static TextureRegion followArrow;

	private static Map<Integer, List<FloatingText>> worldFloatingTexts;

	private static Deque<Task> uiTasks;

	private static BloodAndMithrilClientInputProcessor inputProcessor;
	private static Graphics graphics;
	private static GameSaver gameSaver;
	private static ChunkLoader chunkLoader;
	private static FactionControlService factionControlService;
	private static GameClientStateTracker gameClientStateTracker;
	private static Threading threading;
	private static TopographyDebugRenderer topographyDebugRenderer;

	static {
		if (ClientServerInterface.isClient()) {
			uiTexture = new Texture(files.internal("data/image/ui.png"));
			iconTexture = new Texture(files.internal("data/image/icons.png"));

			finalWaypointTexture = new TextureRegion(UserInterface.uiTexture, 0, 42, 16, 16);
			jumpWaypointTexture = new TextureRegion(UserInterface.uiTexture, 0, 59, 39, 29);
			currentArrow = new TextureRegion(UserInterface.uiTexture, 500, 1, 11, 8);
			followArrow = new TextureRegion(UserInterface.uiTexture, 500, 10, 11, 8);
			uiTasks = new ConcurrentLinkedDeque<>();
			worldFloatingTexts = Maps.newHashMap();
			shapeRenderer = new ShapeRenderer();
		}
	}


	public UserInterface() {
		UICamera = new OrthographicCamera(getGdxWidth(), getGdxHeight());
		UICamera.setToOrtho(false, getGdxWidth(), getGdxHeight());
		UICameraTrackingCam = new OrthographicCamera(getGdxWidth(), getGdxHeight());
		UICameraTrackingCam.setToOrtho(false, getGdxWidth(), getGdxHeight());
	}


	/**
	 * @return a Camera that is used to display UI elements, but the coordiantes move with the main game camera
	 */
	public OrthographicCamera getUITrackingCamera() {
		return UICameraTrackingCam;
	}


	/**
	 * @return a Camera that is fixed to the game window
	 */
	public OrthographicCamera getUICamera() {
		return UICamera;
	}


	/**
	 * Setup for UI, makes everything it needs.
	 *
	 * @param graphics.getWidth() - initial window width
	 * @param graphics.getHeight() - initial window height
	 */
	public static synchronized void setup() {
		inputProcessor = Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class);
		graphics = Wiring.injector().getInstance(Graphics.class);
		gameSaver = Wiring.injector().getInstance(GameSaver.class);
		chunkLoader = Wiring.injector().getInstance(ChunkLoader.class);
		factionControlService = Wiring.injector().getInstance(FactionControlService.class);
		gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);
		topographyDebugRenderer = Wiring.injector().getInstance(TopographyDebugRenderer.class);
		threading = Wiring.injector().getInstance(Threading.class);
	}


	/**
	 * Resets window positions when the screen is resized
	 */
	public static synchronized void resetWindowPositions(final int oldWidth, final int oldHeight) {
		final float oldW = oldWidth;
		final float oldH = oldHeight;

		for (final Component c : layeredComponents) {
			if (c instanceof Window) {
				((Window) c).x = Math.round(graphics.getWidth() * (float)((Window) c).x / oldW);
				((Window) c).y = Math.round(graphics.getHeight() * (float)((Window) c).y / oldH);
			}
		}
	}


	/**
	 * @return all components
	 */
	public synchronized static Deque<Component> getLayeredComponents() {
		return new ArrayDeque<Component>(layeredComponents);
	}


	/**
	 * Load the task bar and the status bar
	 */
	public static void loadBars() {
		layeredComponents.add(new BottomBar());
	}


	/**
	 * Adds a {@link Task} to the {@link #uiTasks} Deque to be executed in the main thread
	 */
	public static void addUITask(final Task task) {
		uiTasks.add(task);
	}


	public static void refreshRefreshableWindows() {
		layeredComponents.stream().filter((component) -> {
			return component instanceof Refreshable;
		}).forEach((component) -> {
			((Refreshable) component).refresh();
		});
	}


	public static void refreshRefreshableWindows(final Class<? extends Window> classToRefresh) {
		layeredComponents.stream().filter((component) -> {
			return component instanceof Refreshable && component.getClass().equals(classToRefresh);
		}).forEach((component) -> {
			((Refreshable) component).refresh();
		});
	}


	/**
	 * Load the buttons
	 */
	public void loadButtons() {
		final Button pauseButton = new Button(
			"Pause",
			defaultFont,
			-32,
			4,
			55,
			16,
			() -> {
				gameClientStateTracker.setPaused(true);
			},
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.TR
		);

		unpauseButton = new Button(
			"Unpause",
			defaultFont,
			0,
			0,
			75,
			16,
			() -> {
				gameClientStateTracker.setPaused(false);
			},
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.M
		);

		savingButton = new Button(
			"Saving...",
			defaultFont,
			0,
			0,
			75,
			16,
			() -> {},
			Color.WHITE,
			Color.WHITE,
			Color.WHITE,
			UIRef.M
		);

		buttons.put("pauseButton", pauseButton);
	}


	/**
	 * To be called when the window is resized.
	 *
	 * @param width - new screen width
	 * @param height - new screen height
	 */
	public void resize(final int width, final int height) {
		UICamera.setToOrtho(false, width, height);
	}


	/**
	 * Renders the UI
	 */
	public void render() {

		while (!uiTasks.isEmpty()) {
			uiTasks.poll().execute();
		}

		//Individual sprites (Selected arrow, movement arrow etc)
		renderIndividualUISprites(graphics);

		graphics.getSpriteBatch().setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);

		if (DEBUG && isServer()) {
			renderComponentInterfaces();
			renderPositionalIndexes();
			if (renderComponentBoundaries) {
				renderComponentBoundaries();
			}
			renderMouseOverTileHighlightBox(false);
		}


		renderFloatingText(gameClientStateTracker.getActiveWorldId());
		renderTextBubbles();
		renderHint();
		renderCursorBoundTaskText();
		renderDragBox();
		renderAverageBars();
		renderLayeredComponents();
		renderContextMenus();
		if (getInfoPopup() != null) {
			if (getInfoPopup().expiryFunction.call() || !contextMenus.isEmpty()) {
				setInfoPopup(null);
			} else {
				getInfoPopup().setClosing(false);
				getInfoPopup().render(graphics);
			}
		}

		graphics.getSpriteBatch().setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);
		graphics.getSpriteBatch().begin();
		if (DEBUG) {
			renderDebugText();
		}

		if (gameClientStateTracker.isInGame()) {
			renderUIText();
		}
		renderButtons();

		graphics.getSpriteBatch().end();

		renderPauseScreen();
		renderSavingScreen();
		renderLoadingScreen();

		if (RENDER_TOPOGRAPHY) {
			topographyDebugRenderer.render(graphics);
		}
	}


	private static void renderAverageBars() {
		if (averageBarAlpha != 0f || !gameClientStateTracker.getSelectedIndividuals().isEmpty()) {
			if (gameClientStateTracker.getSelectedIndividuals().isEmpty()) {
				averageBarAlpha = averageBarAlpha - 0.04f < 0f ? 0f : averageBarAlpha - 0.04f;
			} else {
				averageBarAlpha = averageBarAlpha + 0.04f > 0.6f ? 0.6f : averageBarAlpha + 0.04f;

				maxHealth = 0f;
				totalHealth = 0f;
				maxStamina = 0f;
				totalStamina = 0f;
				maxMana = 0f;
				totalMana  = 0f;

				for (final Individual indi : gameClientStateTracker.getSelectedIndividuals()) {
					maxHealth += indi.getState().maxHealth;
					totalHealth += indi.getState().health;

					maxStamina += 1f;
					totalStamina += indi.getState().stamina;

					maxMana += indi.getState().maxMana;
					totalMana += indi.getState().mana;
				}
			}

			Gdx.gl.glEnable(GL_BLEND);
			Gdx.gl.glLineWidth(1f);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(1f, 1f, 1f, averageBarAlpha);
			shapeRenderer.rect(graphics.getWidth() / 2 - 200, 65, 400 * totalHealth / maxHealth, 5);
			shapeRenderer.rect(graphics.getWidth() / 2 - 200, 55, 400 * totalStamina / maxStamina, 5);
			shapeRenderer.rect(graphics.getWidth() / 2 - 200, 45, 400 * totalMana / maxMana, 5);
			shapeRenderer.end();
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1f, 1f, 1f, averageBarAlpha);
			shapeRenderer.rect(graphics.getWidth() / 2 - 200, 65, 400, 5);
			shapeRenderer.rect(graphics.getWidth() / 2 - 200, 55, 400, 5);
			shapeRenderer.rect(graphics.getWidth() / 2 - 200, 45, 400, 5);
			shapeRenderer.end();
			Gdx.gl.glDisable(GL_BLEND);
		}
	}


	private static synchronized void renderTextBubbles() {
		graphics.getSpriteBatch().begin();

		final ArrayDeque<TextBubble> newBubbles = new ArrayDeque<>();
		for (final TextBubble bubble : textBubbles) {
			bubble.render(graphics);
			if (bubble.getBean().removalFunction.call()) {
				bubble.setClosing(true);
			}

			if (!(bubble.getAlpha() <= 0f && bubble.isClosing())) {
				newBubbles.add(bubble);
			}
		}

		textBubbles = newBubbles;
		graphics.getSpriteBatch().end();
	}


	public static synchronized void addTextBubble(final String text, final SerializableFunction<Vector2> position, final long duration, final int xOffset, final int yOffset) {
		if (ClientServerInterface.isClient()) {
			textBubbles.add(
				new TextBubble(
					new TextBubbleSerializableBean(text, new Countdown(duration)),
					position,
					xOffset,
					yOffset
				)
			);
		} else {
			ClientServerInterface.SendNotification.notifyTextBubble(text, position, duration, xOffset, yOffset, -1);
		}
	}


	private void renderHint() {
		if (inputProcessor.getCursorBoundTask() == null && contextMenus.isEmpty() && gameClientStateTracker.getActiveWorld() != null && !isKeyPressed(Keys.ANY_KEY)) {
			boolean renderHint = false;
			final PositionalIndexMap positionalIndexMap = gameClientStateTracker.getActiveWorld().getPositionalIndexMap();
			for (final int id : positionalIndexMap.getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				if (Domain.getIndividual(id).isMouseOver()) {
					renderHint = true;
					break;
				}
			}
			for (final int id : positionalIndexMap.getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY())) {
				if (renderHint) {
					break;
				}

				final Prop prop = gameClientStateTracker.getActiveWorld().props().getProp(id);
				if (prop != null && prop.isMouseOver()) {
					renderHint = true;
					break;
				}
			}
			for (final int id : positionalIndexMap.getNearbyEntityIds(Item.class, getMouseWorldX(), getMouseWorldY())) {
				if (renderHint) {
					break;
				}

				final Item item = gameClientStateTracker.getActiveWorld().items().getItem(id);
				if (item != null && item.isMouseOver()) {
					renderHint = true;
					break;
				}
			}

			if (renderHint) {
				graphics.getSpriteBatch().begin();
				graphics.getSpriteBatch().setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", UICamera.combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), "?", getMouseScreenX() + 14, getMouseScreenY() - 5);
				graphics.getSpriteBatch().flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), "?", getMouseScreenX() + 15, getMouseScreenY() - 5);
				graphics.getSpriteBatch().end();
			}
		}
	}


	private static void renderPositionalIndexes() {
		defaultFont.setColor(Color.YELLOW);
		final Collection<Object> nearbyEntities = Lists.newLinkedList();

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return Domain.getIndividual(id);
					}
				)
			)
		);

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return gameClientStateTracker.getActiveWorld().props().getProp(id);
					}
				)
			)
		);

		int position = graphics.getHeight() - 270;
		graphics.getSpriteBatch().begin();
		Fonts.defaultFont.draw(graphics.getSpriteBatch(), "Entities near cursor:", 5, position + 40);
		for (final Object nearbyEntity : nearbyEntities) {
			if (nearbyEntity instanceof Individual) {
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), ((Individual) nearbyEntity).getId().getSimpleName() + " (" + nearbyEntity.getClass().getSimpleName() + ")", 5, position);
			}

			if (nearbyEntity instanceof Prop) {
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), ((Prop) nearbyEntity).getClass().getSimpleName() + " " + ((Prop) nearbyEntity).id, 5, position);
			}
			position = position - 20;
		}
		graphics.getSpriteBatch().end();
	}


	private void renderCursorBoundTaskText() {
		if (inputProcessor.getCursorBoundTask() != null) {
			inputProcessor.getCursorBoundTask().renderUIGuide(graphics);
			graphics.getSpriteBatch().begin();
			graphics.getSpriteBatch().setShader(Shaders.filter);
			Shaders.filter.setUniformMatrix("u_projTrans", UICamera.combined);
			Shaders.filter.setUniformf("color", Color.BLACK);
			Fonts.defaultFont.draw(
				graphics.getSpriteBatch(),
				inputProcessor.getCursorBoundTask().getShortDescription(),
				getMouseScreenX() + 20,
				getMouseScreenY() - 20
			);
			graphics.getSpriteBatch().flush();
			Shaders.filter.setUniformf("color", Color.WHITE);
			Fonts.defaultFont.draw(
				graphics.getSpriteBatch(),
				inputProcessor.getCursorBoundTask().getShortDescription(),
				getMouseScreenX() + 21,
				getMouseScreenY() - 21
			);
			graphics.getSpriteBatch().end();
		}
	}


	/**
	 * Renders the {@link Boundaries} of all {@link bloodandmithril.generation.component.Component}s
	 */
	private static void renderComponentBoundaries() {
		gl.glEnable(GL_BLEND);
		Gdx.gl20.glLineWidth(2f);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (final Structure struct : Structures.getStructures().values()) {
			for (final bloodandmithril.generation.component.Component component : newArrayList(struct.getComponents())) {
				shapeRenderer.begin(Filled);
				shapeRenderer.setColor(COMPONENT_FILL_COLOR);
				shapeRenderer.rect(
					worldToScreenX(component.getBoundaries().left * TILE_SIZE),
					worldToScreenY(component.getBoundaries().bottom * TILE_SIZE),
					(component.getBoundaries().right - component.getBoundaries().left + 1) * TILE_SIZE,
					(component.getBoundaries().top - component.getBoundaries().bottom + 1) * TILE_SIZE
				);
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(COMPONENT_BOUNDARY_COLOR);
				shapeRenderer.rect(
					worldToScreenX(component.getBoundaries().left * TILE_SIZE),
					worldToScreenY(component.getBoundaries().bottom * TILE_SIZE),
					(component.getBoundaries().right - component.getBoundaries().left + 1) * TILE_SIZE,
					(component.getBoundaries().top - component.getBoundaries().bottom + 1) * TILE_SIZE
				);
				shapeRenderer.end();
			}
		}
		gl.glDisable(GL_BLEND);
	}


	/**
	 * Renders a small rectangle to indicate the current tile the mouse is over
	 */
	private static boolean renderMouseOverTileHighlightBox(final boolean nonEmptyTilesOnly) {
		try {
			if (nonEmptyTilesOnly && gameClientStateTracker.getActiveWorld().getTopography().getTile(getMouseWorldX(), getMouseWorldY(), true) instanceof EmptyTile) {
				return false;
			}
		} catch (final NoTileFoundException e) {
			return false;
		}

		final float x = worldToScreenX(TILE_SIZE * convertToWorldTileCoord(getMouseWorldX()));
		final float y = worldToScreenY(TILE_SIZE * convertToWorldTileCoord(getMouseWorldY()));

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glLineWidth(2f);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(x, y, TILE_SIZE, TILE_SIZE);
		shapeRenderer.end();
		gl.glDisable(GL_BLEND);

		return true;
	}


	private static void renderComponentInterfaces() {
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glLineWidth(2f);
		for (final Structure struct : Structures.getStructures().values()) {
			for (final bloodandmithril.generation.component.Component comp : newArrayList(struct.getComponents())) {
				if (renderAvailableInterfaces) {
					for (final Interface in : newArrayList(comp.getAvailableInterfaces())) {
						if (in != null) {
							in.render(AVAILABLE_INTERFACE_COLOR);
						}
					}
				} else {
					for (final Interface in : newArrayList(comp.getExistingInterfaces())) {
						if (in != null) {

							in.render(EXISTING_INTERFACE_COLOR);
						}
					}
				}
			}
		}
		gl.glDisable(GL_BLEND);
	}


	/** Darkens the screen by 80% and draws "Saving..." on the screen if the game is being saved */
	private static void renderSavingScreen() {
		if (gameSaver.isSaving()) {
			graphics.getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(DARK_SCREEN_COLOR);
			shapeRenderer.rect(0, 0, graphics.getWidth(), graphics.getHeight());
			shapeRenderer.end();
			savingButton.render(true, 1f, graphics);
			gl.glDisable(GL_BLEND);
			graphics.getSpriteBatch().end();
		}
	}


	/** Darkens the screen by 50% and draws an "unpause" button on the screen if the game is paused */
	private static void renderPauseScreen() {
		if (gameClientStateTracker.isPaused()) {
			graphics.getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(DARK_SCREEN_COLOR);
			shapeRenderer.rect(0, 0, graphics.getWidth(), graphics.getHeight());
			shapeRenderer.end();

			if (unpauseButton != null) {
				unpauseButton.render(true, 1f, graphics);
			}

			gl.glDisable(GL_BLEND);
			graphics.getSpriteBatch().end();
		}
	}


	/** Draws the loading screen */
	private static void renderLoadingScreen() {
		if (gameClientStateTracker.isLoading()) {
			graphics.getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.rect(0, 0, graphics.getWidth(), graphics.getHeight());
			shapeRenderer.end();

			graphics.getSpriteBatch().setShader(Shaders.text);
			defaultFont.setColor(Color.YELLOW);
			defaultFont.draw(graphics.getSpriteBatch(), "Loading - " + chunkLoader.loaderTasks.size(), graphics.getWidth()/2 - 60, graphics.getHeight()/2);

			gl.glDisable(GL_BLEND);
			graphics.getSpriteBatch().end();
		}
	}


	/**
	 * Called when the left click is released
	 */
	public static void leftClickRelease(final int screenX, final int screenY) {

		if (initialLeftMouseDragCoordinates != null && gameClientStateTracker.isInGame()) {
			final Vector2 diagCorner1 = initialLeftMouseDragCoordinates.cpy();
			final Vector2 diagCorner2 = new Vector2(screenX, screenY);

			final float left = min(diagCorner1.x, diagCorner2.x);
			final float right = max(diagCorner1.x, diagCorner2.x);
			final float top = max(diagCorner1.y, diagCorner2.y);
			final float bottom = min(diagCorner1.y, diagCorner2.y);

			if (right - left < 3 || top - bottom < 3) {
				return;
			}

			for (final Individual indi : Domain.getIndividuals().values()) {
				if (factionControlService.isControllable(indi) && indi.isAlive()) {

					final Vector2 centre = new Vector2(indi.getState().position.x, indi.getState().position.y + indi.getHeight() / 2);

					centre.x = worldToScreenX(centre.x);
					centre.y = worldToScreenY(centre.y);

					final IndividualSelectionService individualSelectionService = Wiring.injector().getInstance(IndividualSelectionService.class);
					if (centre.x > left && centre.x < right && centre.y > bottom && centre.y < top) {
						individualSelectionService.select(indi, ClientServerInterface.getClientID());
					} else if (gameClientStateTracker.isIndividualSelected(indi)) {
						individualSelectionService.deselect(indi);
					}
				}
			}

			if (gameClientStateTracker.getSelectedIndividuals().size() > 1) {
				inputProcessor.setCamFollowFunction(null);
			}
		}

		if (!layeredComponents.isEmpty()) {
			final Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				iter.next().leftClickReleased();
			}
		}
	}


	public static void rightClickRelease(final int screenX, final int screenY) {
		if (initialRightMouseDragCoordinates != null && isKeyPressed(inputProcessor.getKeyMappings().rightClickDragBox.keyCode)) {
			final Vector2 diagCorner1 = initialRightMouseDragCoordinates.cpy();
			final Vector2 diagCorner2 = new Vector2(screenX, screenY);

			final float left = min(diagCorner1.x, diagCorner2.x);
			final float right = max(diagCorner1.x, diagCorner2.x);
			final float top = max(diagCorner1.y, diagCorner2.y);
			final float bottom = min(diagCorner1.y, diagCorner2.y);

			if (right - left < 3 || top - bottom < 3) {
				return;
			}

			final List<Item> items = Lists.newLinkedList();

			Lists.newLinkedList(Iterables.transform(
				gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getEntitiesWithinBounds(
					Item.class,
					screenToWorldX(left),
					screenToWorldX(right),
					screenToWorldY(top),
					screenToWorldY(bottom)
				),
				id -> {
					return gameClientStateTracker.getActiveWorld().items().getItem(id);
				}
			)).stream().filter(toKeep -> {
				return toKeep.getWorldId() == gameClientStateTracker.getActiveWorld().getWorldId();
			}).filter(item -> {
				return
					worldToScreenX(item.getPosition().x) > left &&
					worldToScreenX(item.getPosition().x) < right &&
					worldToScreenY(item.getPosition().y) > bottom &&
					worldToScreenY(item.getPosition().y) < top;
			}).forEach(toAdd -> {
				items.add(toAdd);
			});

			if (!items.isEmpty()) {
				if (gameClientStateTracker.getSelectedIndividuals().size() > 0) {
					contextMenus.clear();
					final boolean singleIndividualSelected = gameClientStateTracker.getSelectedIndividuals().size() == 1;
					contextMenus.add(new ContextMenu(
						screenX,
						screenY,
						true,
						new MenuItem(
							"Take items",
							() -> {
								if (singleIndividualSelected) {
									final Individual next = gameClientStateTracker.getSelectedIndividuals().iterator().next();
									if (ClientServerInterface.isServer()) {
										try {
											next.getAI().setCurrentTask(new TakeItem(next, items));
										} catch (final NoTileFoundException e) {}
									} else {
										ClientServerInterface.SendRequest.sendRequestTakeItems(next, items);
									}
								}
							},
							singleIndividualSelected ? Color.WHITE : Colors.UI_DARK_GRAY,
							singleIndividualSelected ? Color.GREEN : Colors.UI_DARK_GRAY,
							singleIndividualSelected ? Color.WHITE : Colors.UI_DARK_GRAY,
							() -> {
								return new ContextMenu(screenX, screenY, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
							},
							() -> {
								return !singleIndividualSelected;
							}
						)
					));
				}
			}
		}
	}


	/**
	 * Renders the drag-box
	 */
	private static void renderDragBox() {
		if (isButtonPressed(inputProcessor.getKeyMappings().leftClick.keyCode) && initialLeftMouseDragCoordinates != null) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.GREEN);
			final float width = getMouseScreenX() - initialLeftMouseDragCoordinates.x;
			final float height = getMouseScreenY() - initialLeftMouseDragCoordinates.y;
			shapeRenderer.rect(initialLeftMouseDragCoordinates.x, initialLeftMouseDragCoordinates.y, width, height);
			shapeRenderer.end();
		}

		if (isButtonPressed(inputProcessor.getKeyMappings().rightClick.keyCode) && initialRightMouseDragCoordinates != null && isKeyPressed(inputProcessor.getKeyMappings().rightClickDragBox.keyCode)) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.RED);
			final float width = getMouseScreenX() - initialRightMouseDragCoordinates.x;
			final float height = getMouseScreenY() - initialRightMouseDragCoordinates.y;
			shapeRenderer.rect(initialRightMouseDragCoordinates.x, initialRightMouseDragCoordinates.y, width, height);
			shapeRenderer.end();
		}
	}


	private static void renderIndividualUISprites(final Graphics graphics) {
		graphics.getSpriteBatch().begin();
		for (final Individual indi : Domain.getIndividuals().values()) {
			if (gameClientStateTracker.isIndividualSelected(indi)) {
				final AITask currentTask = indi.getAI().getCurrentTask();
				if (currentTask instanceof GoToLocation) {
					shapeRenderer.setColor(Color.WHITE);
					 ((GoToLocation)currentTask).renderPath();
					((GoToLocation)currentTask).renderFinalWayPoint(graphics);
				} else if (currentTask instanceof Travel) {
					((Travel) currentTask).renderWaypoints(graphics);

					if (isKeyPressed(inputProcessor.getKeyMappings().jump.keyCode)) {
						final Vector2 destination = ((Travel) currentTask).getFinalGoToLocationWaypoint();
						Vector2 start;
						if (destination != null) {
							if (isKeyPressed(inputProcessor.getKeyMappings().addWayPoint.keyCode)) {
								start = destination;
							} else {
								start = indi.getState().position.cpy();
							}

							renderJumpArrow(
								start,
								new Vector2(getMouseWorldX(), getMouseWorldY())
							);
						}
					}
				} else if (currentTask instanceof CompositeAITask) {
					// AITask subTask = ((CompositeAITask) currentTask).getCurrentTask();
					// if (subTask instanceof GoToLocation) {
					// 	 ((GoToLocation)subTask).renderPath();
					// 	 ((GoToLocation)subTask).renderFinalWayPoint();
					// } else if (subTask instanceof GoToMovingLocation) {
					// 	 ((GoToMovingLocation)subTask).getCurrentGoToLocation().renderPath();
					// 	 ((GoToMovingLocation)subTask).getCurrentGoToLocation().renderFinalWayPoint();
					// } else if (subTask instanceof JitGoToLocation) {
					// 	 GoToLocation goToLocation = (GoToLocation)((JitGoToLocation)subTask).getTask();
					// 	 if (goToLocation != null) {
					// 	 	goToLocation.renderFinalWayPoint();
					// 	 }
					// }
				}

				if (!(currentTask instanceof Travel)) {
					if (isKeyPressed(inputProcessor.getKeyMappings().jump.keyCode)) {
						renderJumpArrow(
							indi.getState().position.cpy(),
							new Vector2(getMouseWorldX(), getMouseWorldY())
						);
					}
				}
			}
			indi.renderUIDecorations(graphics);
		}
		graphics.getSpriteBatch().end();
	}


	public static void renderJumpArrow(final Vector2 start, final Vector2 finish) {
		if (!isKeyPressed(inputProcessor.getKeyMappings().attack.keyCode) && !isKeyPressed(inputProcessor.getKeyMappings().rangedAttack.keyCode)) {
			renderArrow(start, finish, new Color(0f, 1f, 0f, 0.65f), 3f, 0f, 75f);
		}
	}


	/**
	 * Renders the jump arrow, coordinates are world coordinates
	 */
	public static void renderArrow(final Vector2 start, final Vector2 finish, final Color color, final float lineWidth, final float arrowSize, final float maxLength) {
		final Vector2 difference = finish.cpy().sub(start);
		final Vector2 arrowHead = start.cpy().add(
			difference.cpy().nor().scl(Math.min(difference.len(), maxLength))
		);

		final Vector2 fin = arrowHead.cpy().sub(
			difference.cpy().nor().scl(14f)
		);

		graphics.getSpriteBatch().flush();
		shapeRenderer.begin(ShapeType.Line);
		Gdx.gl20.glLineWidth(lineWidth);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		shapeRenderer.setColor(color);
		shapeRenderer.line(
			worldToScreenX(start.x),
			worldToScreenY(start.y),
			worldToScreenX(fin.x),
			worldToScreenY(fin.y)
		);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(color);

		final Vector2 point = arrowHead.cpy().add(difference.cpy().nor().scl(5f + arrowSize));
		final Vector2 corner1 = arrowHead.cpy().sub(difference.cpy().nor().rotate(25f).scl(15f + arrowSize / 2f));
		final Vector2 corner2 = arrowHead.cpy().sub(difference.cpy().nor().rotate(-25f).scl(15f + arrowSize / 2f));

		shapeRenderer.triangle(
			worldToScreenX(point.x),
			worldToScreenY(point.y),
			worldToScreenX(corner1.x),
			worldToScreenY(corner1.y),
			worldToScreenX(corner2.x),
			worldToScreenY(corner2.y)
		);
		shapeRenderer.end();
		Gdx.gl20.glDisable(GL20.GL_BLEND);
		Gdx.gl20.glLineWidth(1f);
	}


	/** Any text that is rendered on UI */
	private void renderUIText() {
		defaultFont.setColor(Color.WHITE);
		defaultFont.draw(graphics.getSpriteBatch(), "Time: " + gameClientStateTracker.getActiveWorld().getEpoch().getTimeString(), 5, graphics.getHeight() - 5);
		defaultFont.draw(graphics.getSpriteBatch(), "Date: " + gameClientStateTracker.getActiveWorld().getEpoch().getDateString(), 5, graphics.getHeight() - 25);
		defaultFont.draw(graphics.getSpriteBatch(), "Ping: " + ping, 5, graphics.getHeight() - 45);

		fps = (fps + Math.round(1f/Gdx.graphics.getDeltaTime())) / 2;
		fpsTimer++;
		if (fpsTimer >= 30) {
			fpsDisplayed = fps;
			fpsTimer = 0;
		}

		defaultFont.draw(graphics.getSpriteBatch(), "Framerate: " + fpsDisplayed, 5, graphics.getHeight() - 65);
		defaultFont.draw(graphics.getSpriteBatch(), "Game speed: " + threading.getUpdateRate() + "x", graphics.getWidth() - 165, 20);
		renderMouseText();
	}


	private void renderMouseText() {
		if (inputProcessor.getCursorBoundTask() != null) {
			return;
		}

		final boolean jumpPressed = isKeyPressed(inputProcessor.getKeyMappings().jump.keyCode);
		final boolean addWayPointPressed = isKeyPressed(inputProcessor.getKeyMappings().addWayPoint.keyCode);
		final boolean forceMovePressed = isKeyPressed(inputProcessor.getKeyMappings().forceMove.keyCode);
		final boolean attackPressed = isKeyPressed(inputProcessor.getKeyMappings().attack.keyCode);
		final boolean rangedAttackPressed = isKeyPressed(inputProcessor.getKeyMappings().rangedAttack.keyCode);
		final boolean mineTIlePressed = isKeyPressed(inputProcessor.getKeyMappings().mineTile.keyCode);

		if (!gameClientStateTracker.getSelectedIndividuals().isEmpty()) {

			if ((jumpPressed || addWayPointPressed || forceMovePressed) && !attackPressed && !rangedAttackPressed && !mineTIlePressed) {
				String text = "";
				if (jumpPressed) {
					if (!addWayPointPressed) {
						text = "Jump";
					} else {
						text = "Add jump waypoint";
					}
				} else if (addWayPointPressed) {
					if (forceMovePressed) {
						text = "Add force move waypoint";
					} else {
						text = "Add waypoint";
					}
				} else if (forceMovePressed) {
					text = "Force move";
				}

				graphics.getSpriteBatch().setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", UICamera.combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), text, getMouseScreenX() + 14, getMouseScreenY() - 26);
				graphics.getSpriteBatch().flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), text, getMouseScreenX() + 15, getMouseScreenY() - 25);
			} else if (rangedAttackPressed) {
				boolean canAttackRanged = false;
				for (final Individual indi : gameClientStateTracker.getSelectedIndividuals()) {
					if (indi.canAttackRanged()) {
						renderArrow(indi.getEmissionPosition(), new Vector2(getMouseWorldX(), getMouseWorldY()), new Color(1f, 0f, 0f, 0.65f), 2f, 0f, rangeControl);
						canAttackRanged = true;
					}
				}

				if (canAttackRanged) {
					graphics.getSpriteBatch().setShader(Shaders.filter);
					Shaders.filter.setUniformMatrix("u_projTrans", UICamera.combined);
					Shaders.filter.setUniformf("color", Color.BLACK);
					Fonts.defaultFont.draw(graphics.getSpriteBatch(), "Attack Ranged", getMouseScreenX() + 14, getMouseScreenY() - 26);
					graphics.getSpriteBatch().flush();
					Shaders.filter.setUniformf("color", Color.ORANGE);
					Fonts.defaultFont.draw(graphics.getSpriteBatch(), "Attack Ranged", getMouseScreenX() + 15, getMouseScreenY() - 25);
				}
			} else if (mineTIlePressed) {
				graphics.getSpriteBatch().flush();
				if (renderMouseOverTileHighlightBox(true)) {
					graphics.getSpriteBatch().setShader(Shaders.filter);
					Shaders.filter.setUniformMatrix("u_projTrans", UICamera.combined);
					Shaders.filter.setUniformf("color", Color.BLACK);
					Fonts.defaultFont.draw(graphics.getSpriteBatch(), "Mine", getMouseScreenX() + 14, getMouseScreenY() - 26);
					graphics.getSpriteBatch().flush();
					Shaders.filter.setUniformf("color", Color.ORANGE);
					Fonts.defaultFont.draw(graphics.getSpriteBatch(), "Mine", getMouseScreenX() + 15, getMouseScreenY() - 25);
				}
			}
		}
	}


	private static void renderFloatingText(final int worldId) {
		graphics.getSpriteBatch().begin();

		final List<FloatingText> elements = worldFloatingTexts.get(worldId);
		if (elements != null) {
			Lists.newArrayList(elements).stream().forEach(text -> {
				defaultFont.setColor(Colors.modulateAlpha(Color.BLACK, text.life / text.maxLife));

				Vector2 renderPos = new Vector2();
				if (text.ui) {
					renderPos = text.worldPosition;
				} else {
					renderPos.x = text.worldPosition.x - graphics.getCam().position.x + graphics.getWidth()/2 - text.text.length() * 5;
					renderPos.y = text.worldPosition.y - graphics.getCam().position.y + graphics.getHeight()/2;
				}

				defaultFont.draw(
					graphics.getSpriteBatch(),
					text.text,
					renderPos.x - 1,
					renderPos.y - 1
				);
				defaultFont.setColor(Colors.modulateAlpha(text.color, text.life / text.maxLife));
				defaultFont.draw(
					graphics.getSpriteBatch(),
					text.text,
					renderPos.x,
					renderPos.y
				);
				text.worldPosition.y += 0.5f;
				text.life -= Gdx.graphics.getDeltaTime();

				if (text.life <= 0f) {
					synchronized(worldFloatingTexts) {
						elements.remove(text);
					}
				}
			});
		}

		graphics.getSpriteBatch().end();
	}

	/** Debug text */
	private static void renderDebugText() {
		defaultFont.setColor(Color.YELLOW);
		defaultFont.draw(graphics.getSpriteBatch(), Integer.toString(convertToWorldTileCoord(getMouseWorldX())) + ", " + Float.toString(convertToWorldTileCoord(getMouseWorldY())), getMouseScreenX() - 35, getMouseScreenY() - 35);
		defaultFont.draw(graphics.getSpriteBatch(), "Mouse World Coords: " + getMouseWorldX() + ", " + getMouseWorldY(), 5, 72);
		defaultFont.draw(graphics.getSpriteBatch(), "Centre of screen Coords: " + Float.toString(graphics.getCam().position.x) + ", " + Float.toString(graphics.getCam().position.y) + ", " + Float.toString(graphics.getCam().zoom), 5, 52);

		int chunksInMemory = 0;
		if (ClientServerInterface.isServer()) {
			for (final Entry<Integer, HashMap<Integer, Chunk>> entry : gameClientStateTracker.getActiveWorld().getTopography().getChunkMap().chunkMap.entrySet()) {
				chunksInMemory = chunksInMemory + entry.getValue().size();
			}
		}

		defaultFont.setColor(Color.GREEN);
		defaultFont.draw(graphics.getSpriteBatch(), "Number of chunks in memory of active world: " + Integer.toString(chunksInMemory), 5, graphics.getHeight() - 105);
		defaultFont.draw(graphics.getSpriteBatch(), "Number of tasks queued in AI/Pathfinding thread: " + Integer.toString(AIProcessor.getNumberOfOutstandingTasks()), 5, graphics.getHeight() - 125);
		defaultFont.draw(graphics.getSpriteBatch(), "Number of tasks queued in Loader thread: " + Integer.toString(chunkLoader.loaderTasks.size()), 5, graphics.getHeight() - 145);
		defaultFont.draw(graphics.getSpriteBatch(), "Number of tasks queued in Saver thread: " + Integer.toString(gameSaver.saverTasks.size()), 5, graphics.getHeight() - 165);

		defaultFont.setColor(Color.CYAN);
	}


	/** Renders layered components, e.g. {@link Window}s */
	private static synchronized void renderLayeredComponents() {
		final ArrayDeque<Component> copy = new ArrayDeque<>(layeredComponents);
		for (final Component component : new ArrayDeque<>(layeredComponents)) {
			if (component instanceof Window) {
				((Window) component).renderWorldUIGuide();
			}
		}

		for (final Component component : new ArrayDeque<>(layeredComponents)) {
			if (component instanceof Window) {
				if (((Window) component).y < 0) {
					((Window) component).y = 20;
				}

				if (((Window) component).y > graphics.getHeight()) {
					((Window) component).y = graphics.getHeight();
				}

				if (!((Window)component).minimized || component.getAlpha() > 0f) {
					component.render(graphics);
				}
				((Window) component).close(copy);
			} else {
				component.render(graphics);
			}
		}

		layeredComponents.clear();
		layeredComponents.addAll(copy);
	}


	/** Renders all the context menus */
	private static void renderContextMenus() {
		final Iterator<ContextMenu> iterator = contextMenus.iterator();
		while (iterator.hasNext()) {
			final ContextMenu next = iterator.next();
			if (iterator.hasNext()) {
				next.setActive(false);
				next.render(graphics);
			} else {
				next.setActive(true);
				next.render(graphics);
			}
		}
	}


	/**
	 * Renders all buttons
	 */
	private void renderButtons() {
		for (final Entry<String, Button> buttonEntry : buttons.entrySet()) {
			buttonEntry.getValue().render(!gameClientStateTracker.isPaused() && !gameSaver.isSaving(), 1f, graphics);
		}
	}


	/**
	 * Updates the camera
	 */
	public void update() {
		UICamera.update();
		UICameraTrackingCam.position.x = graphics.getCam().position.x;
		UICameraTrackingCam.position.y = graphics.getCam().position.y;
		UICameraTrackingCam.update();
	}


	public static void addFloatingText(final String text, final Color color, final Vector2 position, final boolean ui, final int worldId) {
		addFloatingText(floatingText(text, color, position, ui), worldId, false);
	}


	public static void addFloatingText(final FloatingText floatingText, final int worldId, final boolean csi) {
		if (isServer()) {
			if (isClient()) {
				synchronized(worldFloatingTexts) {
					if (!worldFloatingTexts.containsKey(worldId)) {
						worldFloatingTexts.put(worldId, Lists.newLinkedList());
					}
					worldFloatingTexts.get(worldId).add(floatingText);
				}
			} else {
				ClientServerInterface.SendNotification.notifyAddFloatingText(floatingText, worldId);
			}

			return;
		}

		if (csi) {
			synchronized(worldFloatingTexts) {
				if (!worldFloatingTexts.containsKey(worldId)) {
					worldFloatingTexts.put(worldId, Lists.newLinkedList());
				}
				worldFloatingTexts.get(worldId).add(floatingText);
			}
		}
	}


	public static void addUIFloatingText(final String text, final Color color, final Vector2 position) {
		addFloatingText(floatingText(text, color, position, true), gameClientStateTracker.getActiveWorldId(), false);
	}


	/**
	 * Called when the scroll wheel is scrolled.
	 */
	public static void scrolled(final int amount) {
		for (final Component component : newArrayList(layeredComponents)) {
			if (component.isActive()) {
				component.scrolled(amount);
			}
		}
	}


	/**
	 * Removes a layered component
	 */
	public static void removeLayeredComponent(final Component toRemove) {
		layeredComponents.remove(toRemove);
	}


	/**
	 * Removes a layered component
	 */
	public static void removeLayeredComponent(final String title) {
		for (final Component component : layeredComponents) {
			if (component instanceof Window && ((Window) component).title.equals(title)) {
				component.setClosing(true);
			}
		}
	}


	/** Adds a {@link Component} to {@link #layeredComponents} */
	public synchronized static void addLayeredComponent(final Component toAdd) {
		for (final Component component : layeredComponents) {
			if (component instanceof Window) {
				((Window)component).setActive(false);
			}
		}
		layeredComponents.addLast(toAdd);
	}


	/** Adds a {@link Component} to {@link #layeredComponents}, checking if an existing one with the same title exists */
	public synchronized static void addLayeredComponentUnique(final Window toAdd) {
		Window existing = null;

		for (final Component window : layeredComponents) {
			if (window instanceof Window) {
				((Window) window).setActive(false);

				final Object existingUniqueIdentifier = ((Window)window).getUniqueIdentifier();
				final Object newUniqueIdentifier = toAdd.getUniqueIdentifier();

				if (existingUniqueIdentifier.equals(newUniqueIdentifier)) {
					existing = (Window)window;
				}
			}
		}

		if (existing == null) {
			layeredComponents.addLast(toAdd);
		} else {
			layeredComponents.remove(existing);
			layeredComponents.addLast(existing);
			existing.setActive(true);
			existing.minimized = false;
		}
	}


	public static void addClientMessage(final String title, final String message) {
		addGlobalMessage(title, message, -1, new FalseFunction());
	}


	public static void addClientMessage(final String title, final SerializableFunction<String> message) {
		addGlobalMessage(title, message, -1, new FalseFunction());
	}


	public static void addGlobalMessage(final String title, final String message) {
		addGlobalMessage(title, message, -1, new AlwaysTrueFunction());
	}


	public static void addGlobalMessage(final String title, final String message, final SerializableFunction<Boolean> function) {
		addGlobalMessage(title, message, -1, function);
	}


	public static void addGlobalMessage(final String title, final SerializableFunction<String> message, final int client, final SerializableFunction<Boolean> function) {
		if (ClientServerInterface.isClient()) {
			addLayeredComponent(
				new MessageWindow(
					message,
					Color.ORANGE,
					graphics.getWidth() / 2 - 150,
					graphics.getHeight() / 2 + 75,
					300,
					150,
					title,
					true,
					300,
					150
				)
			);
		}
	}


	public static void addGlobalMessage(final String title, final String message, final int client, final SerializableFunction<Boolean> function) {
		if (ClientServerInterface.isClient()) {
			addLayeredComponent(
				new MessageWindow(
					message,
					Color.ORANGE,
					300,
					150,
					title,
					true,
					300,
					150
				)
			);
		} else {
			ClientServerInterface.SendNotification.notifyMessage(client, title, message, function);
		}
	}


	/**
	 * Where an UI element should be rendered
	 *
	 * @author Matt
	 */
	public enum UIRef {
		TL, TM, TR, M, BL, BM, BR
	}


	public static void closeAllWindows() {
		for (final Component component : layeredComponents) {
			component.setClosing(true);
		}
	}


	public static InfoPopup getInfoPopup() {
		return infoPopup;
	}


	public static void setInfoPopup(final InfoPopup infoPopup) {
		UserInterface.infoPopup = infoPopup;
	}


	public void addButton(final String name, final Button button) {
		buttons.put(name, button);
	}


	public void removeButton(final String button) {
		buttons.remove(button);
	}


	public void clearLayeredComponents() {
		layeredComponents.clear();
	}


	public void addLayeredComponents(final Collection<Component> toAdd) {
		layeredComponents.addAll(toAdd);
	}
}
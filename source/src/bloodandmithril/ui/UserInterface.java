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
import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.loading;
import static bloodandmithril.core.BloodAndMithrilClient.paused;
import static bloodandmithril.item.items.equipment.weapon.RangedWeapon.rangeControl;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.networking.ClientServerInterface.ping;
import static bloodandmithril.persistence.GameSaver.isSaving;
import static bloodandmithril.ui.UserInterface.FloatingText.floatingText;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.Domain.getActiveWorldId;
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

import java.io.Serializable;
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
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualContextMenuService;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.interfaces.Interface;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.EquipperImpl.AlwaysTrueFunction;
import bloodandmithril.item.items.equipment.EquipperImpl.FalseFunction;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.performance.PositionalIndexMap;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.InfoPopup;
import bloodandmithril.ui.components.TextBubble;
import bloodandmithril.ui.components.TextBubble.TextBubbleSerializableBean;
import bloodandmithril.ui.components.bar.BottomBar;
import bloodandmithril.ui.components.window.AIRoutinesWindow;
import bloodandmithril.ui.components.window.BuildWindow;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableColor;
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
@Copyright("Matthew Peck 2014")
public class UserInterface {

	private static final Color DARK_SCREEN_COLOR = new Color(0f, 0f, 0f, 0.8f);
	private static final Color EXISTING_INTERFACE_COLOR = new Color(1f, 0.2f, 0f, 0.5f);
	private static final Color AVAILABLE_INTERFACE_COLOR = new Color(0.2f, 1f, 0f, 0.5f);
	private static final Color COMPONENT_BOUNDARY_COLOR = new Color(1f, 1f, 1f, 0.5f);
	private static final Color COMPONENT_FILL_COLOR = new Color(0f, 1f, 0f, 0.15f);

	/** UI camera */
	public static OrthographicCamera UICamera;
	public static OrthographicCamera UICameraTrackingCam;

	/** List of {@link Button}s */
	public static HashMap<String, Button> buttons = newHashMap();

	/** Unpause button */
	private static Button unpauseButton, savingButton;

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
	private static Vector2 initialLeftMouseDragCoordinates = null;
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

	private static IndividualContextMenuService individualContextMenuService;
	private static BloodAndMithrilClientInputProcessor inputProcessor;

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

	/**
	 * Setup for UI, makes everything it needs.
	 *
	 * @param getGraphics().getWidth() - initial window width
	 * @param getGraphics().getHeight() - initial window height
	 */
	public static synchronized void setup() {
		loadBars();
		loadButtons();

		individualContextMenuService = Wiring.injector().getInstance(IndividualContextMenuService.class);
		inputProcessor = Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class);
	}


	/**
	 * Resets window positions when the screen is resized
	 */
	public static synchronized void resetWindowPositions(int oldWidth, int oldHeight) {
		float oldW = oldWidth;
		float oldH = oldHeight;

		for (Component c : layeredComponents) {
			if (c instanceof Window) {
				((Window) c).x = Math.round(getGraphics().getWidth() * (float)((Window) c).x / oldW);
				((Window) c).y = Math.round(getGraphics().getHeight() * (float)((Window) c).y / oldH);
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
	private static void loadBars() {
		layeredComponents.add(new BottomBar());
	}


	/**
	 * Adds a {@link Task} to the {@link #uiTasks} Deque to be executed in the main thread
	 */
	public static void addUITask(Task task) {
		uiTasks.add(task);
	}


	public static void refreshRefreshableWindows() {
		layeredComponents.stream().filter((component) -> {
			return component instanceof Refreshable;
		}).forEach((component) -> {
			((Refreshable) component).refresh();
		});
	}


	public static void refreshRefreshableWindows(Class<? extends Window> classToRefresh) {
		layeredComponents.stream().filter((component) -> {
			return component instanceof Refreshable && component.getClass().equals(classToRefresh);
		}).forEach((component) -> {
			((Refreshable) component).refresh();
		});
	}


	/**
	 * Load the buttons
	 */
	private static void loadButtons() {
		Button pauseButton = new Button(
			"Pause",
			defaultFont,
			-32,
			4,
			55,
			16,
			() -> {
				BloodAndMithrilClient.paused = true;
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
				BloodAndMithrilClient.paused = false;
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
	public void resize(int width, int height) {
		UICamera.setToOrtho(false, width, height);
	}


	/**
	 * Renders the UI
	 */
	public static void render() {

		while (!uiTasks.isEmpty()) {
			uiTasks.poll().execute();
		}

		//Individual sprites (Selected arrow, movement arrow etc)
		renderIndividualUISprites();

		getGraphics().getSpriteBatch().setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);

		if (DEBUG && isServer()) {
			renderComponentInterfaces();
			renderPositionalIndexes();
			if (renderComponentBoundaries) {
				renderComponentBoundaries();
			}
			renderMouseOverTileHighlightBox(false);
		}


		renderFloatingText(getActiveWorldId());
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
				getInfoPopup().render();
			}
		}

		getGraphics().getSpriteBatch().setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);
		getGraphics().getSpriteBatch().begin();
		if (DEBUG) {
			renderDebugText();
		}

		if (BloodAndMithrilClient.isInGame()) {
			renderUIText();
		}
		renderButtons();

		getGraphics().getSpriteBatch().end();

		renderPauseScreen();
		renderSavingScreen();
		renderLoadingScreen();

		if (RENDER_TOPOGRAPHY) {
			TopographyDebugRenderer.render();
		}
	}


	private static void renderAverageBars() {
		if (averageBarAlpha != 0f || !Domain.getSelectedIndividuals().isEmpty()) {
			if (Domain.getSelectedIndividuals().isEmpty()) {
				averageBarAlpha = averageBarAlpha - 0.04f < 0f ? 0f : averageBarAlpha - 0.04f;
			} else {
				averageBarAlpha = averageBarAlpha + 0.04f > 0.6f ? 0.6f : averageBarAlpha + 0.04f;

				maxHealth = 0f;
				totalHealth = 0f;
				maxStamina = 0f;
				totalStamina = 0f;
				maxMana = 0f;
				totalMana  = 0f;

				for (Individual indi : Domain.getSelectedIndividuals()) {
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
			shapeRenderer.rect(getGraphics().getWidth() / 2 - 200, 65, 400 * totalHealth / maxHealth, 5);
			shapeRenderer.rect(getGraphics().getWidth() / 2 - 200, 55, 400 * totalStamina / maxStamina, 5);
			shapeRenderer.rect(getGraphics().getWidth() / 2 - 200, 45, 400 * totalMana / maxMana, 5);
			shapeRenderer.end();
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1f, 1f, 1f, averageBarAlpha);
			shapeRenderer.rect(getGraphics().getWidth() / 2 - 200, 65, 400, 5);
			shapeRenderer.rect(getGraphics().getWidth() / 2 - 200, 55, 400, 5);
			shapeRenderer.rect(getGraphics().getWidth() / 2 - 200, 45, 400, 5);
			shapeRenderer.end();
			Gdx.gl.glDisable(GL_BLEND);
		}
	}


	private static synchronized void renderTextBubbles() {
		getGraphics().getSpriteBatch().begin();

		ArrayDeque<TextBubble> newBubbles = new ArrayDeque<>();
		for (TextBubble bubble : textBubbles) {
			bubble.render();
			if (bubble.getBean().removalFunction.call()) {
				bubble.setClosing(true);
			}

			if (!(bubble.getAlpha() <= 0f && bubble.isClosing())) {
				newBubbles.add(bubble);
			}
		}

		textBubbles = newBubbles;
		getGraphics().getSpriteBatch().end();
	}


	public static synchronized void addTextBubble(String text, SerializableFunction<Vector2> position, long duration, int xOffset, int yOffset) {
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


	private static void renderHint() {
		if (inputProcessor.getCursorBoundTask() == null && contextMenus.isEmpty() && Domain.getActiveWorld() != null && !isKeyPressed(Keys.ANY_KEY)) {
			boolean renderHint = false;
			PositionalIndexMap positionalIndexMap = Domain.getActiveWorld().getPositionalIndexMap();
			for (int id : positionalIndexMap.getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				if (Domain.getIndividual(id).isMouseOver()) {
					renderHint = true;
					break;
				}
			}
			for (int id : positionalIndexMap.getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY())) {
				if (renderHint) {
					break;
				}

				Prop prop = Domain.getActiveWorld().props().getProp(id);
				if (prop != null && prop.isMouseOver()) {
					renderHint = true;
					break;
				}
			}
			for (int id : positionalIndexMap.getNearbyEntityIds(Item.class, getMouseWorldX(), getMouseWorldY())) {
				if (renderHint) {
					break;
				}

				Item item = Domain.getActiveWorld().items().getItem(id);
				if (item != null && item.isMouseOver()) {
					renderHint = true;
					break;
				}
			}

			if (renderHint) {
				getGraphics().getSpriteBatch().begin();
				getGraphics().getSpriteBatch().setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICamera.combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "?", getMouseScreenX() + 14, getMouseScreenY() - 5);
				getGraphics().getSpriteBatch().flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "?", getMouseScreenX() + 15, getMouseScreenY() - 5);
				getGraphics().getSpriteBatch().end();
			}
		}
	}


	private static void renderPositionalIndexes() {
		defaultFont.setColor(Color.YELLOW);
		Collection<Object> nearbyEntities = Lists.newLinkedList();

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return Domain.getIndividual(id);
					}
				)
			)
		);

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return Domain.getActiveWorld().props().getProp(id);
					}
				)
			)
		);

		int position = BloodAndMithrilClient.getGraphics().getHeight() - 270;
		getGraphics().getSpriteBatch().begin();
		Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "Entities near cursor:", 5, position + 40);
		for (Object nearbyEntity : nearbyEntities) {
			if (nearbyEntity instanceof Individual) {
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), ((Individual) nearbyEntity).getId().getSimpleName() + " (" + nearbyEntity.getClass().getSimpleName() + ")", 5, position);
			}

			if (nearbyEntity instanceof Prop) {
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), ((Prop) nearbyEntity).getClass().getSimpleName() + " " + ((Prop) nearbyEntity).id, 5, position);
			}
			position = position - 20;
		}
		getGraphics().getSpriteBatch().end();
	}


	private static void renderCursorBoundTaskText() {
		if (inputProcessor.getCursorBoundTask() != null) {
			inputProcessor.getCursorBoundTask().renderUIGuide();
			getGraphics().getSpriteBatch().begin();
			getGraphics().getSpriteBatch().setShader(Shaders.filter);
			Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICamera.combined);
			Shaders.filter.setUniformf("color", Color.BLACK);
			Fonts.defaultFont.draw(
				getGraphics().getSpriteBatch(),
				inputProcessor.getCursorBoundTask().getShortDescription(),
				getMouseScreenX() + 20,
				getMouseScreenY() - 20
			);
			getGraphics().getSpriteBatch().flush();
			Shaders.filter.setUniformf("color", Color.WHITE);
			Fonts.defaultFont.draw(
				getGraphics().getSpriteBatch(),
				inputProcessor.getCursorBoundTask().getShortDescription(),
				getMouseScreenX() + 21,
				getMouseScreenY() - 21
			);
			getGraphics().getSpriteBatch().end();
		}
	}


	/**
	 * Renders the {@link Boundaries} of all {@link bloodandmithril.generation.component.Component}s
	 */
	private static void renderComponentBoundaries() {
		gl.glEnable(GL_BLEND);
		Gdx.gl20.glLineWidth(2f);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (Structure struct : Structures.getStructures().values()) {
			for (bloodandmithril.generation.component.Component component : newArrayList(struct.getComponents())) {
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
	private static boolean renderMouseOverTileHighlightBox(boolean nonEmptyTilesOnly) {
		try {
			if (nonEmptyTilesOnly && Domain.getActiveWorld().getTopography().getTile(getMouseWorldX(), getMouseWorldY(), true) instanceof EmptyTile) {
				return false;
			}
		} catch (NoTileFoundException e) {
			return false;
		}

		float x = worldToScreenX(TILE_SIZE * convertToWorldTileCoord(getMouseWorldX()));
		float y = worldToScreenY(TILE_SIZE * convertToWorldTileCoord(getMouseWorldY()));

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
		for (Structure struct : Structures.getStructures().values()) {
			for (bloodandmithril.generation.component.Component comp : newArrayList(struct.getComponents())) {
				if (renderAvailableInterfaces) {
					for (Interface in : newArrayList(comp.getAvailableInterfaces())) {
						if (in != null) {
							in.render(AVAILABLE_INTERFACE_COLOR);
						}
					}
				} else {
					for (Interface in : newArrayList(comp.getExistingInterfaces())) {
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
		if (isSaving()) {
			getGraphics().getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(DARK_SCREEN_COLOR);
			shapeRenderer.rect(0, 0, getGraphics().getWidth(), getGraphics().getHeight());
			shapeRenderer.end();
			savingButton.render(true, 1f);
			gl.glDisable(GL_BLEND);
			getGraphics().getSpriteBatch().end();
		}
	}


	/** Darkens the screen by 50% and draws an "unpause" button on the screen if the game is paused */
	private static void renderPauseScreen() {
		if (paused) {
			getGraphics().getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(DARK_SCREEN_COLOR);
			shapeRenderer.rect(0, 0, getGraphics().getWidth(), getGraphics().getHeight());
			shapeRenderer.end();

			if (unpauseButton != null) {
				unpauseButton.render(true, 1f);
			}

			gl.glDisable(GL_BLEND);
			getGraphics().getSpriteBatch().end();
		}
	}


	/** Draws the loading screen */
	private static void renderLoadingScreen() {
		if (loading) {
			getGraphics().getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.rect(0, 0, getGraphics().getWidth(), getGraphics().getHeight());
			shapeRenderer.end();

			getGraphics().getSpriteBatch().setShader(Shaders.text);
			defaultFont.setColor(Color.YELLOW);
			defaultFont.draw(getGraphics().getSpriteBatch(), "Loading - " + ChunkLoader.loaderTasks.size(), getGraphics().getWidth()/2 - 60, getGraphics().getHeight()/2);

			gl.glDisable(GL_BLEND);
			getGraphics().getSpriteBatch().end();
		}
	}


	/**
	 * Called when the left click is released
	 */
	public static void leftClickRelease(int screenX, int screenY) {

		if (initialLeftMouseDragCoordinates != null && BloodAndMithrilClient.isInGame()) {
			Vector2 diagCorner1 = initialLeftMouseDragCoordinates.cpy();
			Vector2 diagCorner2 = new Vector2(screenX, screenY);

			float left = min(diagCorner1.x, diagCorner2.x);
			float right = max(diagCorner1.x, diagCorner2.x);
			float top = max(diagCorner1.y, diagCorner2.y);
			float bottom = min(diagCorner1.y, diagCorner2.y);

			if (right - left < 3 || top - bottom < 3) {
				return;
			}

			for (Individual indi : Domain.getIndividuals().values()) {
				if (indi.isControllable() && indi.isAlive()) {

					Vector2 centre = new Vector2(indi.getState().position.x, indi.getState().position.y + indi.getHeight() / 2);

					centre.x = worldToScreenX(centre.x);
					centre.y = worldToScreenY(centre.y);

					IndividualSelectionService individualSelectionService = Wiring.injector().getInstance(IndividualSelectionService.class);
					if (centre.x > left && centre.x < right && centre.y > bottom && centre.y < top) {
						individualSelectionService.select(indi);
					} else if (Domain.isIndividualSelected(indi)) {
						individualSelectionService.deselect(indi);
					}
				}
			}

			if (Domain.getSelectedIndividuals().size() > 1) {
				inputProcessor.setCamFollowFunction(null);
			}
		}

		if (!layeredComponents.isEmpty()) {
			Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				iter.next().leftClickReleased();
			}
		}
	}


	public static void rightClickRelease(int screenX, int screenY) {
		if (initialRightMouseDragCoordinates != null && isKeyPressed(inputProcessor.getKeyMappings().rightClickDragBox.keyCode)) {
			Vector2 diagCorner1 = initialRightMouseDragCoordinates.cpy();
			Vector2 diagCorner2 = new Vector2(screenX, screenY);

			float left = min(diagCorner1.x, diagCorner2.x);
			float right = max(diagCorner1.x, diagCorner2.x);
			float top = max(diagCorner1.y, diagCorner2.y);
			float bottom = min(diagCorner1.y, diagCorner2.y);

			if (right - left < 3 || top - bottom < 3) {
				return;
			}

			List<Item> items = Lists.newLinkedList();

			Lists.newLinkedList(Iterables.transform(
				Domain.getActiveWorld().getPositionalIndexMap().getEntitiesWithinBounds(
					Item.class,
					screenToWorldX(left),
					screenToWorldX(right),
					screenToWorldY(top),
					screenToWorldY(bottom)
				),
				id -> {
					return Domain.getActiveWorld().items().getItem(id);
				}
			)).stream().filter(toKeep -> {
				return toKeep.getWorldId() == Domain.getActiveWorld().getWorldId();
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
				if (Domain.getSelectedIndividuals().size() > 0) {
					contextMenus.clear();
					boolean singleIndividualSelected = Domain.getSelectedIndividuals().size() == 1;
					contextMenus.add(new ContextMenu(
						screenX,
						screenY,
						true,
						new MenuItem(
							"Take items",
							() -> {
								if (singleIndividualSelected) {
									Individual next = Domain.getSelectedIndividuals().iterator().next();
									if (ClientServerInterface.isServer()) {
										try {
											next.getAI().setCurrentTask(new TakeItem(next, items));
										} catch (NoTileFoundException e) {}
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
			float width = getMouseScreenX() - initialLeftMouseDragCoordinates.x;
			float height = getMouseScreenY() - initialLeftMouseDragCoordinates.y;
			shapeRenderer.rect(initialLeftMouseDragCoordinates.x, initialLeftMouseDragCoordinates.y, width, height);
			shapeRenderer.end();
		}

		if (isButtonPressed(inputProcessor.getKeyMappings().rightClick.keyCode) && initialRightMouseDragCoordinates != null && isKeyPressed(inputProcessor.getKeyMappings().rightClickDragBox.keyCode)) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.RED);
			float width = getMouseScreenX() - initialRightMouseDragCoordinates.x;
			float height = getMouseScreenY() - initialRightMouseDragCoordinates.y;
			shapeRenderer.rect(initialRightMouseDragCoordinates.x, initialRightMouseDragCoordinates.y, width, height);
			shapeRenderer.end();
		}
	}


	private static void renderIndividualUISprites() {
		getGraphics().getSpriteBatch().begin();
		for (Individual indi : Domain.getIndividuals().values()) {
			if (indi.isSelected()) {
				AITask currentTask = indi.getAI().getCurrentTask();
				if (currentTask instanceof GoToLocation) {
					shapeRenderer.setColor(Color.WHITE);
					 ((GoToLocation)currentTask).renderPath();
					((GoToLocation)currentTask).renderFinalWayPoint();
				} else if (currentTask instanceof Travel) {
					((Travel) currentTask).renderWaypoints();

					if (isKeyPressed(inputProcessor.getKeyMappings().jump.keyCode)) {
						Vector2 destination = ((Travel) currentTask).getFinalGoToLocationWaypoint();
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
			indi.renderUIDecorations();
		}
		getGraphics().getSpriteBatch().end();
	}


	public static void renderJumpArrow(Vector2 start, Vector2 finish) {
		if (!isKeyPressed(inputProcessor.getKeyMappings().attack.keyCode) && !isKeyPressed(inputProcessor.getKeyMappings().rangedAttack.keyCode)) {
			renderArrow(start, finish, new Color(0f, 1f, 0f, 0.65f), 3f, 0f, 75f);
		}
	}


	/**
	 * Renders the jump arrow, coordinates are world coordinates
	 */
	public static void renderArrow(Vector2 start, Vector2 finish, Color color, float lineWidth, float arrowSize, float maxLength) {
		Vector2 difference = finish.cpy().sub(start);
		Vector2 arrowHead = start.cpy().add(
			difference.cpy().nor().scl(Math.min(difference.len(), maxLength))
		);

		Vector2 fin = arrowHead.cpy().sub(
			difference.cpy().nor().scl(14f)
		);

		getGraphics().getSpriteBatch().flush();
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

		Vector2 point = arrowHead.cpy().add(difference.cpy().nor().scl(5f + arrowSize));
		Vector2 corner1 = arrowHead.cpy().sub(difference.cpy().nor().rotate(25f).scl(15f + arrowSize / 2f));
		Vector2 corner2 = arrowHead.cpy().sub(difference.cpy().nor().rotate(-25f).scl(15f + arrowSize / 2f));

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
	private static void renderUIText() {
		defaultFont.setColor(Color.WHITE);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Time: " + Domain.getActiveWorld().getEpoch().getTimeString(), 5, getGraphics().getHeight() - 5);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Date: " + Domain.getActiveWorld().getEpoch().getDateString(), 5, getGraphics().getHeight() - 25);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Ping: " + ping, 5, getGraphics().getHeight() - 45);

		fps = (fps + Math.round(1f/Gdx.graphics.getDeltaTime())) / 2;
		fpsTimer++;
		if (fpsTimer >= 30) {
			fpsDisplayed = fps;
			fpsTimer = 0;
		}

		defaultFont.draw(getGraphics().getSpriteBatch(), "Framerate: " + fpsDisplayed, 5, getGraphics().getHeight() - 65);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Game speed: " + BloodAndMithrilClient.getUpdateRate() + "x", getGraphics().getWidth() - 165, 20);
		renderMouseText();
	}


	private static void renderMouseText() {
		if (inputProcessor.getCursorBoundTask() != null) {
			return;
		}

		boolean jumpPressed = isKeyPressed(inputProcessor.getKeyMappings().jump.keyCode);
		boolean addWayPointPressed = isKeyPressed(inputProcessor.getKeyMappings().addWayPoint.keyCode);
		boolean forceMovePressed = isKeyPressed(inputProcessor.getKeyMappings().forceMove.keyCode);
		boolean attackPressed = isKeyPressed(inputProcessor.getKeyMappings().attack.keyCode);
		boolean rangedAttackPressed = isKeyPressed(inputProcessor.getKeyMappings().rangedAttack.keyCode);
		boolean mineTIlePressed = isKeyPressed(inputProcessor.getKeyMappings().mineTile.keyCode);

		if (!Domain.getSelectedIndividuals().isEmpty()) {

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

				getGraphics().getSpriteBatch().setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICamera.combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), text, getMouseScreenX() + 14, getMouseScreenY() - 26);
				getGraphics().getSpriteBatch().flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), text, getMouseScreenX() + 15, getMouseScreenY() - 25);
			} else if (rangedAttackPressed) {
				boolean canAttackRanged = false;
				for (Individual indi : Domain.getSelectedIndividuals()) {
					if (indi.canAttackRanged()) {
						renderArrow(indi.getEmissionPosition(), new Vector2(getMouseWorldX(), getMouseWorldY()), new Color(1f, 0f, 0f, 0.65f), 2f, 0f, rangeControl);
						canAttackRanged = true;
					}
				}

				if (canAttackRanged) {
					getGraphics().getSpriteBatch().setShader(Shaders.filter);
					Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICamera.combined);
					Shaders.filter.setUniformf("color", Color.BLACK);
					Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "Attack Ranged", getMouseScreenX() + 14, getMouseScreenY() - 26);
					getGraphics().getSpriteBatch().flush();
					Shaders.filter.setUniformf("color", Color.ORANGE);
					Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "Attack Ranged", getMouseScreenX() + 15, getMouseScreenY() - 25);
				}
			} else if (mineTIlePressed) {
				getGraphics().getSpriteBatch().flush();
				if (renderMouseOverTileHighlightBox(true)) {
					getGraphics().getSpriteBatch().setShader(Shaders.filter);
					Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICamera.combined);
					Shaders.filter.setUniformf("color", Color.BLACK);
					Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "Mine", getMouseScreenX() + 14, getMouseScreenY() - 26);
					getGraphics().getSpriteBatch().flush();
					Shaders.filter.setUniformf("color", Color.ORANGE);
					Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "Mine", getMouseScreenX() + 15, getMouseScreenY() - 25);
				}
			}
		}
	}


	private static void renderFloatingText(int worldId) {
		getGraphics().getSpriteBatch().begin();

		List<FloatingText> elements = worldFloatingTexts.get(worldId);
		if (elements != null) {
			Lists.newArrayList(elements).stream().forEach(text -> {
				defaultFont.setColor(Colors.modulateAlpha(Color.BLACK, text.life / text.maxLife));

				Vector2 renderPos = new Vector2();
				if (text.ui) {
					renderPos = text.worldPosition;
				} else {
					renderPos.x = text.worldPosition.x - getGraphics().getCam().position.x + getGraphics().getWidth()/2 - text.text.length() * 5;
					renderPos.y = text.worldPosition.y - getGraphics().getCam().position.y + getGraphics().getHeight()/2;
				}

				defaultFont.draw(
					getGraphics().getSpriteBatch(),
					text.text,
					renderPos.x - 1,
					renderPos.y - 1
				);
				defaultFont.setColor(Colors.modulateAlpha(text.color, text.life / text.maxLife));
				defaultFont.draw(
					getGraphics().getSpriteBatch(),
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

		getGraphics().getSpriteBatch().end();
	}

	/** Debug text */
	private static void renderDebugText() {
		defaultFont.setColor(Color.YELLOW);
		defaultFont.draw(getGraphics().getSpriteBatch(), Integer.toString(convertToWorldTileCoord(getMouseWorldX())) + ", " + Float.toString(convertToWorldTileCoord(getMouseWorldY())), getMouseScreenX() - 35, getMouseScreenY() - 35);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Mouse World Coords: " + getMouseWorldX() + ", " + getMouseWorldY(), 5, 72);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Centre of screen Coords: " + Float.toString(getGraphics().getCam().position.x) + ", " + Float.toString(getGraphics().getCam().position.y) + ", " + Float.toString(getGraphics().getCam().zoom), 5, 52);

		int chunksInMemory = 0;
		if (ClientServerInterface.isServer()) {
			for (Entry<Integer, HashMap<Integer, Chunk>> entry : Domain.getActiveWorld().getTopography().getChunkMap().chunkMap.entrySet()) {
				chunksInMemory = chunksInMemory + entry.getValue().size();
			}
		}

		defaultFont.setColor(Color.GREEN);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Number of chunks in memory of active world: " + Integer.toString(chunksInMemory), 5, getGraphics().getHeight() - 105);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Number of tasks queued in AI/Pathfinding thread: " + Integer.toString(AIProcessor.getNumberOfOutstandingTasks()), 5, getGraphics().getHeight() - 125);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Number of tasks queued in Loader thread: " + Integer.toString(ChunkLoader.loaderTasks.size()), 5, getGraphics().getHeight() - 145);
		defaultFont.draw(getGraphics().getSpriteBatch(), "Number of tasks queued in Saver thread: " + Integer.toString(GameSaver.saverTasks.size()), 5, getGraphics().getHeight() - 165);

		defaultFont.setColor(Color.CYAN);
	}


	/** Renders layered components, e.g. {@link Window}s */
	private static synchronized void renderLayeredComponents() {
		ArrayDeque<Component> copy = new ArrayDeque<>(layeredComponents);
		for (Component component : new ArrayDeque<>(layeredComponents)) {
			if (component instanceof Window) {
				((Window) component).renderWorldUIGuide();
			}
		}

		for (Component component : new ArrayDeque<>(layeredComponents)) {
			if (component instanceof Window) {
				if (((Window) component).y < 0) {
					((Window) component).y = 20;
				}

				if (((Window) component).y > getGraphics().getHeight()) {
					((Window) component).y = getGraphics().getHeight();
				}

				if (!((Window)component).minimized || component.getAlpha() > 0f) {
					component.render();
				}
				((Window) component).close(copy);
			} else {
				component.render();
			}
		}

		layeredComponents = copy;
	}


	/** Renders all the context menus */
	private static void renderContextMenus() {
		Iterator<ContextMenu> iterator = contextMenus.iterator();
		while (iterator.hasNext()) {
			ContextMenu next = iterator.next();
			if (iterator.hasNext()) {
				next.setActive(false);
				next.render();
			} else {
				next.setActive(true);
				next.render();
			}
		}
	}


	/**
	 * Renders all buttons
	 */
	private static void renderButtons() {
		for (Entry<String, Button> buttonEntry : buttons.entrySet()) {
			buttonEntry.getValue().render(!BloodAndMithrilClient.paused && !GameSaver.isSaving(), 1f);
		}
	}


	/**
	 * Updates the camera
	 */
	public static void update() {
		UICamera.update();
		UICameraTrackingCam.position.x = getGraphics().getCam().position.x;
		UICameraTrackingCam.position.y = getGraphics().getCam().position.y;
		UICameraTrackingCam.update();
	}


	/**
	 * Called when left mouse button is clicked
	 */
	public static boolean leftClick() {
		boolean clicked = false;

		if (BloodAndMithrilClient.paused) {
			if (unpauseButton != null) {
				clicked = unpauseButton.click();
			}
			return false;
		}

		if (GameSaver.isSaving()) {
			return false;
		}

		for (Entry<String, Button> buttonEntry : buttons.entrySet()) {
			clicked = buttonEntry.getValue().click() || clicked;
		}

		List<ContextMenu> contextMenuCopy = new ArrayList<ContextMenu>(contextMenus);

		if (!layeredComponents.isEmpty() && contextMenus.isEmpty()) {
			ArrayDeque<Component> windowsCopy = new ArrayDeque<Component>(layeredComponents);
			Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				Component next = iter.next();
				if (next.leftClick(contextMenuCopy, windowsCopy)) {
					clicked = true;
					break;
				}
			}
			if (windowsCopy.size() >= layeredComponents.size()) {
				layeredComponents = windowsCopy;
			}
		}

		Iterator<ContextMenu> iterator = contextMenus.iterator();
		while (iterator.hasNext()) {
			ContextMenu menu = iterator.next();
			if (!iterator.hasNext()) {
				clicked = menu.leftClick(contextMenuCopy, null) || clicked;
			}
			if (!menu.isInside(getMouseScreenX(), getMouseScreenY())) {
				if (menu.getTop() == null) {
					contextMenuCopy.remove(menu);
				} else {
					if (!menu.getTop().isInside(getMouseScreenX(), getMouseScreenY())) {
						contextMenuCopy.remove(menu);
					}
				}
			}
		}

		contextMenus.clear();
		contextMenus.addAll(contextMenuCopy);

		if (!clicked) {
			initialLeftMouseDragCoordinates = new Vector2(getMouseScreenX(), getMouseScreenY());
		} else {
			initialLeftMouseDragCoordinates = null;
		}

		return clicked;
	}


	public static void addFloatingText(String text, Color color, Vector2 position, boolean ui, int worldId) {
		addFloatingText(floatingText(text, color, position, ui), worldId, false);
	}


	public static void addFloatingText(FloatingText floatingText, int worldId, boolean csi) {
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


	public static void addUIFloatingText(String text, Color color, Vector2 position) {
		addFloatingText(floatingText(text, color, position, true), getActiveWorldId(), false);
	}


	/**
	 * Called when the scroll wheel is scrolled.
	 */
	public static void scrolled(int amount) {
		for (Component component : newArrayList(layeredComponents)) {
			if (component.isActive()) {
				component.scrolled(amount);
			}
		}
	}


	public static boolean keyPressed(int keyCode) {
		if (BloodAndMithrilClient.paused) {
			return false;
		}

		if (GameSaver.isSaving()) {
			return false;
		}

		for (Component component : layeredComponents) {
			boolean pressed = false;
			if (component.isActive() && !(component instanceof BottomBar)) {
				pressed = component.keyPressed(keyCode) || pressed;
			}
			if (pressed) {
				return true;
			}
		}

		if (keyCode == inputProcessor.getKeyMappings().openInventory.keyCode) {
			if (Domain.getSelectedIndividuals().size() == 1) {
				Individual individual = Domain.getSelectedIndividuals().iterator().next();
				String simpleName = individual.getId().getSimpleName();

				UserInterface.addLayeredComponentUnique(
					new InventoryWindow(
						individual,
						simpleName + " - Inventory",
						true
					)
				);
			}
		}

		if (keyCode == inputProcessor.getKeyMappings().openAIRoutines.keyCode) {
			if (Domain.getSelectedIndividuals().size() == 1) {
				Individual individual = Domain.getSelectedIndividuals().iterator().next();
				UserInterface.addLayeredComponentUnique(
					new AIRoutinesWindow(
						individual
					)
				);
			}
		}

		if (keyCode == inputProcessor.getKeyMappings().openBuildWindow.keyCode) {
			if (Domain.getSelectedIndividuals().size() == 1) {
				Individual individual = Domain.getSelectedIndividuals().iterator().next();

				UserInterface.addLayeredComponentUnique(
					new BuildWindow(
						individual,
						new Function<Construction, String>() {
							@Override
							public String apply(Construction input) {
								return input.getTitle();
							}
						},
						(c1, c2) -> {
							return c1.getTitle().compareTo(c2.getTitle());
						}
					)
				);
			}
		}

		return false;
	}


	/**
	 * Removes a layered component
	 */
	public static void removeLayeredComponent(Component toRemove) {
		layeredComponents.remove(toRemove);
	}


	/**
	 * Removes a layered component
	 */
	public static void removeLayeredComponent(String title) {
		for (Component component : layeredComponents) {
			if (component instanceof Window && ((Window) component).title.equals(title)) {
				component.setClosing(true);
			}
		}
	}


	/**
	 * Called when right mouse button is clicked
	 */
	public static boolean rightClick() {
		boolean clicked = false;

		if (BloodAndMithrilClient.paused || GameSaver.isSaving()) {
			return false;
		}

		contextMenus.clear();
		ContextMenu newMenu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		if (!layeredComponents.isEmpty()) {
			ArrayDeque<Component> windowsCopy = new ArrayDeque<Component>(layeredComponents);
			Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				Component next = iter.next();
				if (next instanceof Window && ((Window)next).rightClick(windowsCopy)) {
					clicked = true;
					break;
				}
			}
			layeredComponents = windowsCopy;

			if (clicked) {
				return true;
			}
		}

		for (final int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
			Individual indi = Domain.getIndividual(indiKey);
			if (indi.isMouseOver()) {
				final ContextMenu secondaryMenu = individualContextMenuService.getContextMenu(indi);
				newMenu.getMenuItems().add(
					new MenuItem(
						indi.getId().getSimpleName() + " (" + indi.getClass().getSimpleName() + ")",
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						Color.WHITE,
						indi.getToolTipTextColor(),
						indi.getToolTipTextColor(),
						() -> { return secondaryMenu; }
					)
				);
			}
		}

		for (final int propKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY())) {
			Prop prop = Domain.getActiveWorld().props().getProp(propKey);
			if (prop.isMouseOver()) {
				final ContextMenu secondaryMenu = prop.getContextMenu();
				newMenu.getMenuItems().add(
					new MenuItem(
						prop.getContextMenuItemLabel(),
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						prop.getContextMenuColor(),
						Color.GREEN,
						Color.GRAY,
						() -> { return secondaryMenu; }
					)
				);
			}
		}

		for (final Integer itemId : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Item.class, getMouseWorldX(), getMouseWorldY())) {
			final Item item = Domain.getActiveWorld().items().getItem(itemId);
			if (item.isMouseOver()) {
				final ContextMenu secondaryMenu = item.getContextMenu();
				newMenu.getMenuItems().add(
					new MenuItem(
						item.getSingular(true),
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						() -> { return secondaryMenu; }
					)
				);
			}
		}

		if (!newMenu.getMenuItems().isEmpty()) {
			contextMenus.add(newMenu);
		}

		return clicked;
	}


	/** Adds a {@link Component} to {@link #layeredComponents} */
	public synchronized static void addLayeredComponent(Component toAdd) {
		for (Component component : layeredComponents) {
			if (component instanceof Window) {
				((Window)component).setActive(false);
			}
		}
		layeredComponents.addLast(toAdd);
	}


	/** Adds a {@link Component} to {@link #layeredComponents}, checking if an existing one with the same title exists */
	public synchronized static void addLayeredComponentUnique(Window toAdd) {
		Window existing = null;

		for (Component window : layeredComponents) {
			if (window instanceof Window) {
				((Window) window).setActive(false);

				Object existingUniqueIdentifier = ((Window)window).getUniqueIdentifier();
				Object newUniqueIdentifier = toAdd.getUniqueIdentifier();

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


	public static void addClientMessage(String title, String message) {
		addGlobalMessage(title, message, -1, new FalseFunction());
	}


	public static void addClientMessage(String title, SerializableFunction<String> message) {
		addGlobalMessage(title, message, -1, new FalseFunction());
	}


	public static void addGlobalMessage(String title, String message) {
		addGlobalMessage(title, message, -1, new AlwaysTrueFunction());
	}


	public static void addGlobalMessage(String title, String message, SerializableFunction<Boolean> function) {
		addGlobalMessage(title, message, -1, function);
	}


	public static void addGlobalMessage(String title, SerializableFunction<String> message, int client, SerializableFunction<Boolean> function) {
		if (ClientServerInterface.isClient()) {
			addLayeredComponent(
				new MessageWindow(
					message,
					Color.ORANGE,
					getGraphics().getWidth() / 2 - 150,
					getGraphics().getHeight() / 2 + 75,
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


	public static void addGlobalMessage(String title, String message, int client, SerializableFunction<Boolean> function) {
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
	 * Floating text that is rendered at UI layer
	 *
	 * @author Matt
	 */
	public static class FloatingText implements Serializable {
		private static final long serialVersionUID = -2300891549168870979L;

		public final String text;
		public final SerializableColor color;
		public final Vector2 worldPosition;
		public float maxLife = 1f, life = 1f;
		public boolean ui;

		private FloatingText(String text, SerializableColor color, Vector2 worldPosition, boolean ui) {
			this.text = text;
			this.color = color;
			this.worldPosition = worldPosition;
			this.ui = ui;
		}

		public static FloatingText floatingText(String text, Color color, Vector2 worldPosition, boolean ui) {
			return new FloatingText(text, new SerializableColor(color), worldPosition, ui);
		}


		public static FloatingText floatingText(String text, Color color, Vector2 worldPosition, float life, boolean ui) {
			FloatingText floatingText = new FloatingText(text, new SerializableColor(color), worldPosition, ui);
			floatingText.maxLife = life;
			floatingText.life = life;
			return floatingText;
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
		for (Component component : layeredComponents) {
			component.setClosing(true);
		}
	}


	public static InfoPopup getInfoPopup() {
		return infoPopup;
	}


	public static void setInfoPopup(InfoPopup infoPopup) {
		UserInterface.infoPopup = infoPopup;
	}
}
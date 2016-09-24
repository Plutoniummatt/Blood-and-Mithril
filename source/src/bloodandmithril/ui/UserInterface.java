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
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldCoords;
import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static bloodandmithril.item.items.equipment.weapon.RangedWeapon.rangeControl;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.networking.ClientServerInterface.ping;
import static bloodandmithril.ui.FloatingText.floatingText;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
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

import org.lwjgl.opengl.Display;

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
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.ai.task.takeitem.TakeItem;
import bloodandmithril.character.ai.task.travel.Travel;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.ThreadedTasks;
import bloodandmithril.core.Wiring;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.components.stemming.interfaces.HorizontalInterface;
import bloodandmithril.generation.component.components.stemming.interfaces.Interface;
import bloodandmithril.generation.component.components.stemming.interfaces.VerticalInterface;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.EquipperImpl.AlwaysTrueFunction;
import bloodandmithril.item.items.equipment.EquipperImpl.FalseFunction;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.performance.PositionalIndexChunkMap;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.world.ChunkProvider;
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
import bloodandmithril.world.fluids.FluidStrip;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;
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

	/** The texture atlas for other UI elements */
	public static Texture uiTexture;
	public static Texture iconTexture;

	/** Texture regions */
	public static TextureRegion finalWaypointTexture;
	public static TextureRegion jumpWaypointTexture;
	public static TextureRegion currentArrow;
	public static TextureRegion followArrow;

	/** UI colors */
	private static final Color DARK_SCREEN_COLOR = new Color(0f, 0f, 0f, 0.8f);
	private static final Color COMPONENT_BOUNDARY_COLOR = new Color(1f, 1f, 1f, 0.5f);
	private static final Color COMPONENT_FILL_COLOR = new Color(0f, 1f, 0f, 0.15f);

	/** UI camera */
	private OrthographicCamera UICamera;
	private OrthographicCamera UICameraTrackingCam;

	//---DEBUG----------------------------------------------------------------------
	/** A flag to indicate whether we should render the available interfaces or existing interfaces */
	public boolean renderComponentBoundaries = true;

	/** Whether to render debug UI */
	public boolean DEBUG = false;

	/** Whether to render debug UI */
	public boolean RENDER_TOPOGRAPHY = false;
	//---DEBUG----------------------------------------------------------------------

	/** List of {@link Button}s */
	public HashMap<String, Button> buttons = newHashMap();

	/** Pause/Unpause buttons */
	public Button unpauseButton;
	public Button savingButton;

	/** {@link ContextMenu}s */
	private List<ContextMenu> contextMenus = new ArrayList<>();

	/** {@link Window}s */
	private ArrayDeque<Component> layeredComponents = new ArrayDeque<>();

	/** {@link TextBubble}s */
	private ArrayDeque<TextBubble> textBubbles = new ArrayDeque<>();

	/** Floating texts per world */
	private Map<Integer, List<FloatingText>> worldFloatingTexts = Maps.newHashMap();

	/** UI-related tasks that must be executed here */
	private Deque<Task> uiTasks = new ConcurrentLinkedDeque<>();

	/** Shape renderer */
	private ShapeRenderer shapeRenderer = new ShapeRenderer();

	/** Initial coordinates for the drag box, see {@link #renderDragBox()} */
	public Vector2 initialLeftMouseDragCoordinates = null;
	public Vector2 initialRightMouseDragCoordinates = null;

	/** FPS should be updated twice per second */
	private int fpsTimer, fps, fpsDisplayed;

	/** The mouse-over info popup */
	private InfoPopup infoPopup;

	private float averageBarAlpha = 0f;
	private float maxHealth = 0f;
	private float totalHealth = 0;
	private float maxStamina = 0f;
	private float totalStamina = 0f;
	private float maxMana = 0f;
	private float totalMana = 0;

	@Inject private BloodAndMithrilClientInputProcessor inputProcessor;
	@Inject private Controls controls;
	@Inject private Graphics graphics;
	@Inject private ThreadedTasks threadedTasks;
	@Inject private GameSaver gameSaver;
	@Inject private ChunkProvider chunkLoader;
	@Inject private FactionControlService factionControlService;
	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private TopographyDebugRenderer topographyDebugRenderer;
	@Inject private IndividualUIDecorationsRenderer individualUIDecorationsRenderer;

	/**
	 * @return a Camera that is used to display UI elements, but the coordiantes move with the main game camera
	 */
	public OrthographicCamera getUITrackingCamera() {
		if (UICameraTrackingCam == null) {
			UICameraTrackingCam = new OrthographicCamera(getGdxWidth(), getGdxHeight());
			UICameraTrackingCam.setToOrtho(false, getGdxWidth(), getGdxHeight());
		}

		return UICameraTrackingCam;
	}


	/**
	 * @return a Camera that is fixed to the game window
	 */
	public OrthographicCamera getUICamera() {
		if (UICamera == null) {
			UICamera = new OrthographicCamera(getGdxWidth(), getGdxHeight());
			UICamera.setToOrtho(false, getGdxWidth(), getGdxHeight());
		}

		return UICamera;
	}


	/**
	 * Setup for UI, makes everything it needs.
	 */
	public synchronized void setup() {
		uiTexture = new Texture(files.internal("data/image/ui.png"));
		iconTexture = new Texture(files.internal("data/image/icons.png"));

		finalWaypointTexture = new TextureRegion(UserInterface.uiTexture, 0, 42, 16, 16);
		jumpWaypointTexture = new TextureRegion(UserInterface.uiTexture, 0, 59, 39, 29);
		currentArrow = new TextureRegion(UserInterface.uiTexture, 500, 1, 11, 8);
		followArrow = new TextureRegion(UserInterface.uiTexture, 500, 10, 11, 8);
	}


	/**
	 * Resets window positions when the screen is resized
	 */
	public synchronized void resetWindowPositions(final int oldWidth, final int oldHeight) {
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
	public synchronized Deque<Component> getLayeredComponents() {
		return new ArrayDeque<>(layeredComponents);
	}


	/**
	 * Load the task bar and the status bar
	 */
	public void loadBars() {
		layeredComponents.add(new BottomBar());
	}


	/**
	 * Adds a {@link Task} to the {@link #uiTasks} Deque to be executed in the main thread
	 */
	public void addUITask(final Task task) {
		uiTasks.add(task);
	}


	public synchronized void refreshRefreshableWindows() {
		layeredComponents.stream().filter((component) -> {
			return component instanceof Refreshable;
		}).forEach((component) -> {
			((Refreshable) component).refresh();
		});
	}


	public synchronized void refreshRefreshableWindows(final Class<? extends Window> classToRefresh) {
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
			renderPositionalIndexes();
			renderChunkBoundaries();
			renderComponentInterfaces();
			renderEdgeTileBoxes(gameClientStateTracker.getActiveWorld().getTopography(), (int)graphics.getCam().position.x, (int)graphics.getCam().position.y);
			if (renderComponentBoundaries) {
				renderComponentBoundaries();
			}
			renderFluidStripHighightBoxes();
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


	private void renderComponentInterfaces() {
		getShapeRenderer().begin(ShapeType.Filled);
		gl.glEnable(GL_BLEND);
		Gdx.gl20.glLineWidth(2f);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		getShapeRenderer().setColor(new Color(0.2f, 0.6f, 1f, 0.75f));

		Structures.getStructures().values().stream().forEach(structure -> {
			structure.getComponents().forEach(component -> {
				component.getInterfaces().stream().forEach(iface -> {
					render(iface);
				});
			});
		});

		getShapeRenderer().end();		
	}


	private void render(Interface iface) {
		if (iface instanceof VerticalInterface) {
			shapeRenderer.rect(
				worldToScreenX(convertToWorldCoord(((VerticalInterface) iface).tileX, true)),
				worldToScreenY(convertToWorldCoord(((VerticalInterface) iface).tileY, true)),
				TILE_SIZE,
				((VerticalInterface) iface).height * TILE_SIZE
			);
		}
		
		if (iface instanceof HorizontalInterface) {
			shapeRenderer.rect(
				worldToScreenX(convertToWorldCoord(((HorizontalInterface) iface).tileX, true)),
				worldToScreenY(convertToWorldCoord(((HorizontalInterface) iface).tileY, true)),
				((HorizontalInterface) iface).width * TILE_SIZE,
				TILE_SIZE
			);
		}
	}


	private void renderEdgeTileBoxes(final Topography topography, final int camX, final int camY) {
		final int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		final int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		final int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		final int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		getShapeRenderer().begin(ShapeType.Line);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glLineWidth(2f);
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (topography.getChunkMap().get(x) != null && topography.getChunkMap().get(x).get(y) != null) {
					final Chunk chunk = topography.getChunkMap().get(x).get(y);

					for (int tileX = 0; tileX < CHUNK_SIZE; tileX++) {
						for (int tileY = 0; tileY < CHUNK_SIZE; tileY++) {
							getShapeRenderer().setColor(1f, 0.3f, 0.2f, 0.4f);
							if (chunk.getTile(tileX, tileY, false).edge) {
								getShapeRenderer().rect(
									worldToScreenX(CHUNK_SIZE * TILE_SIZE * x + TILE_SIZE * tileX),
									worldToScreenY(CHUNK_SIZE * TILE_SIZE * y + TILE_SIZE * tileY),
									TILE_SIZE,
									TILE_SIZE
								);
							}
							getShapeRenderer().setColor(0.3f, 0.9f, 0.7f, 0.9f);
							if (chunk.getTile(tileX, tileY, true).edge) {
								getShapeRenderer().rect(
									worldToScreenX(CHUNK_SIZE * TILE_SIZE * x + TILE_SIZE * tileX),
									worldToScreenY(CHUNK_SIZE * TILE_SIZE * y + TILE_SIZE * tileY),
									TILE_SIZE,
									TILE_SIZE
								);
							}
						}
					}
				}
			}
		}

		getShapeRenderer().end();
		gl.glDisable(GL_BLEND);
	}


	private void renderChunkBoundaries() {
		getShapeRenderer().begin(ShapeType.Line);
		gl.glEnable(GL_BLEND);
		Gdx.gl20.glLineWidth(2f);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		getShapeRenderer().setColor(new Color(0.7f, 0.5f, 1f, 0.25f));

		final float baseLineHorizontal = (Graphics.getGdxHeight()/2 - graphics.getCam().position.y) % 320;
		final float lineLengthHorizontal = Graphics.getGdxWidth();

		final float baseLineVertical = (Graphics.getGdxWidth()/2 - graphics.getCam().position.x) % 320;
		final float lineLengthVeritical = Graphics.getGdxHeight();

		for (int i = 0; i < 8; i++) {
			getShapeRenderer().line(
				0,
				baseLineHorizontal + i * 320,
				lineLengthHorizontal,
				baseLineHorizontal + i * 320
			);
		}
		for (int i = 0; i < 16; i++) {
			getShapeRenderer().line(
				baseLineVertical + i * 320,
				0,
				baseLineVertical + i * 320,
				lineLengthVeritical
			);
		}

		getShapeRenderer().end();
	}


	private void renderAverageBars() {
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
			getShapeRenderer().begin(ShapeType.Filled);
			getShapeRenderer().setColor(1f, 1f, 1f, averageBarAlpha);
			getShapeRenderer().rect(graphics.getWidth() / 2 - 200, 65, 400 * totalHealth / maxHealth, 5);
			getShapeRenderer().rect(graphics.getWidth() / 2 - 200, 55, 400 * totalStamina / maxStamina, 5);
			getShapeRenderer().rect(graphics.getWidth() / 2 - 200, 45, 400 * totalMana / maxMana, 5);
			getShapeRenderer().end();
			getShapeRenderer().begin(ShapeType.Line);
			getShapeRenderer().setColor(1f, 1f, 1f, averageBarAlpha);
			getShapeRenderer().rect(graphics.getWidth() / 2 - 200, 65, 400, 5);
			getShapeRenderer().rect(graphics.getWidth() / 2 - 200, 55, 400, 5);
			getShapeRenderer().rect(graphics.getWidth() / 2 - 200, 45, 400, 5);
			getShapeRenderer().end();
			Gdx.gl.glDisable(GL_BLEND);
		}
	}


	private synchronized void renderTextBubbles() {
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


	public synchronized void addTextBubble(final String text, final SerializableFunction<Vector2> position, final long duration, final int xOffset, final int yOffset) {
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
		if (!gameClientStateTracker.isLoading() && inputProcessor.getCursorBoundTask() == null && contextMenus.isEmpty() && gameClientStateTracker.getActiveWorld() != null && !isKeyPressed(Keys.ANY_KEY)) {
			boolean renderHint = false;
			final PositionalIndexChunkMap positionalIndexMap = gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap();
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
	
	
	private void renderFluidStripHighightBoxes() {
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glLineWidth(2f);
		for(FluidStrip strip : gameClientStateTracker.getActiveWorld().fluids().getAllFluidStrips()) {
			getShapeRenderer().begin(ShapeType.Line);
			getShapeRenderer().setColor(Color.PURPLE);
			getShapeRenderer().rect(worldToScreenX(strip.worldTileX * TILE_SIZE), worldToScreenY(strip.worldTileY * TILE_SIZE), strip.width * TILE_SIZE, TILE_SIZE);
			getShapeRenderer().end();
		}
		gl.glDisable(GL_BLEND);
	}


	private void renderPositionalIndexes() {
		defaultFont.setColor(Color.YELLOW);
		final Collection<Object> nearbyEntities = Lists.newLinkedList();

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return Domain.getIndividual(id);
					}
				)
			)
		);

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return gameClientStateTracker.getActiveWorld().props().getProp(id);
					}
				)
			)
		);
		
		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getNearbyEntityIds(FluidStrip.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						if(gameClientStateTracker.getActiveWorld().fluids().getFluidStrip(id).isPresent()){
							return gameClientStateTracker.getActiveWorld().fluids().getFluidStrip(id).get();
						} else {
							return null;
						}
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
			
			if (nearbyEntity instanceof FluidStrip) {
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), ((FluidStrip) nearbyEntity).getClass().getSimpleName() + " " + ((FluidStrip) nearbyEntity).id, 5, position);
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
	private void renderComponentBoundaries() {
		gl.glEnable(GL_BLEND);
		Gdx.gl20.glLineWidth(2f);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (final Structure struct : Structures.getStructures().values()) {
			for (final bloodandmithril.generation.component.Component component : newArrayList(struct.getComponents())) {
				getShapeRenderer().begin(Filled);
				getShapeRenderer().setColor(COMPONENT_FILL_COLOR);
				getShapeRenderer().rect(
					worldToScreenX(component.getBoundaries().left * TILE_SIZE),
					worldToScreenY(component.getBoundaries().bottom * TILE_SIZE),
					(component.getBoundaries().right - component.getBoundaries().left + 1) * TILE_SIZE,
					(component.getBoundaries().top - component.getBoundaries().bottom + 1) * TILE_SIZE
				);
				getShapeRenderer().end();

				getShapeRenderer().begin(ShapeType.Line);
				getShapeRenderer().setColor(COMPONENT_BOUNDARY_COLOR);
				getShapeRenderer().rect(
					worldToScreenX(component.getBoundaries().left * TILE_SIZE),
					worldToScreenY(component.getBoundaries().bottom * TILE_SIZE),
					(component.getBoundaries().right - component.getBoundaries().left + 1) * TILE_SIZE,
					(component.getBoundaries().top - component.getBoundaries().bottom + 1) * TILE_SIZE
				);
				getShapeRenderer().end();
			}
		}
		gl.glDisable(GL_BLEND);
	}


	/**
	 * Renders a small rectangle to indicate the current tile the mouse is over
	 */
	private boolean renderMouseOverTileHighlightBox(final boolean nonEmptyTilesOnly) {
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
		getShapeRenderer().begin(ShapeType.Line);
		if(gameClientStateTracker.getActiveWorld().fluids().getFluidStrip(convertToWorldTileCoord(getMouseWorldX()), convertToWorldTileCoord(getMouseWorldY())).isPresent()) {
			getShapeRenderer().setColor(Color.PINK);
		} else {
			getShapeRenderer().setColor(Color.GREEN);
		}
		getShapeRenderer().rect(x, y, TILE_SIZE, TILE_SIZE);
		getShapeRenderer().end();

		gl.glDisable(GL_BLEND);

		return true;
	}


	/** Darkens the screen by 80% and draws "Saving..." on the screen if the game is being saved */
	private void renderSavingScreen() {
		if (gameSaver.isSaving()) {
			graphics.getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			getShapeRenderer().begin(ShapeType.Filled);
			getShapeRenderer().setColor(DARK_SCREEN_COLOR);
			getShapeRenderer().rect(0, 0, graphics.getWidth(), graphics.getHeight());
			getShapeRenderer().end();
			savingButton.render(true, 1f, graphics);
			gl.glDisable(GL_BLEND);
			graphics.getSpriteBatch().end();
		}
	}


	/** Darkens the screen by 50% and draws an "unpause" button on the screen if the game is paused */
	private void renderPauseScreen() {
		if (gameClientStateTracker.isPaused()) {
			graphics.getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			getShapeRenderer().begin(ShapeType.Filled);
			getShapeRenderer().setColor(DARK_SCREEN_COLOR);
			getShapeRenderer().rect(0, 0, graphics.getWidth(), graphics.getHeight());
			getShapeRenderer().end();

			if (unpauseButton != null) {
				unpauseButton.render(true, 1f, graphics);
			}

			gl.glDisable(GL_BLEND);
			graphics.getSpriteBatch().end();
		}
	}


	/** Draws the loading screen */
	private void renderLoadingScreen() {
		if (gameClientStateTracker.isLoading()) {
			graphics.getSpriteBatch().begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			getShapeRenderer().begin(ShapeType.Filled);
			getShapeRenderer().setColor(Color.BLACK);
			getShapeRenderer().rect(0, 0, graphics.getWidth(), graphics.getHeight());
			getShapeRenderer().end();

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
	public void leftClickRelease(final int screenX, final int screenY) {

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

			for (final Individual indi : Domain.getIndividuals()) {
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


	public void rightClickRelease(final int screenX, final int screenY) {
		if (initialRightMouseDragCoordinates != null && isKeyPressed(controls.rightClickDragBox.keyCode)) {
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
				gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getEntitiesWithinBounds(
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
	private void renderDragBox() {
		if (isButtonPressed(controls.leftClick.keyCode) && initialLeftMouseDragCoordinates != null) {
			getShapeRenderer().begin(ShapeType.Line);
			getShapeRenderer().setColor(Color.GREEN);
			final float width = getMouseScreenX() - initialLeftMouseDragCoordinates.x;
			final float height = getMouseScreenY() - initialLeftMouseDragCoordinates.y;
			getShapeRenderer().rect(initialLeftMouseDragCoordinates.x, initialLeftMouseDragCoordinates.y, width, height);
			getShapeRenderer().end();
		}

		if (isButtonPressed(controls.rightClick.keyCode) && initialRightMouseDragCoordinates != null && isKeyPressed(controls.rightClickDragBox.keyCode)) {
			getShapeRenderer().begin(ShapeType.Line);
			getShapeRenderer().setColor(Color.RED);
			final float width = getMouseScreenX() - initialRightMouseDragCoordinates.x;
			final float height = getMouseScreenY() - initialRightMouseDragCoordinates.y;
			getShapeRenderer().rect(initialRightMouseDragCoordinates.x, initialRightMouseDragCoordinates.y, width, height);
			getShapeRenderer().end();
		}
	}


	private void renderIndividualUISprites(final Graphics graphics) {
		graphics.getSpriteBatch().begin();
		for (final Individual indi : Domain.getIndividuals()) {
			if (gameClientStateTracker.isIndividualSelected(indi)) {
				final AITask currentTask = indi.getAI().getCurrentTask();
				if (currentTask instanceof GoToLocation) {
					getShapeRenderer().setColor(Color.WHITE);
					 ((GoToLocation)currentTask).renderPath();
					((GoToLocation)currentTask).renderFinalWayPoint(graphics);
				} else if (currentTask instanceof Travel) {
					((Travel) currentTask).renderWaypoints(graphics);

					if (isKeyPressed(controls.jump.keyCode)) {
						final Vector2 destination = ((Travel) currentTask).getFinalGoToLocationWaypoint();
						Vector2 start;
						if (destination != null) {
							if (isKeyPressed(controls.addWayPoint.keyCode)) {
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
				}

				if (!(currentTask instanceof Travel)) {
					if (isKeyPressed(controls.jump.keyCode)) {
						renderJumpArrow(
							indi.getState().position.cpy(),
							new Vector2(getMouseWorldX(), getMouseWorldY())
						);
					}
				}
			}

			individualUIDecorationsRenderer.render(indi, shapeRenderer, DEBUG, UICameraTrackingCam, UICamera);
		}
		graphics.getSpriteBatch().end();
	}


	public void renderJumpArrow(final Vector2 start, final Vector2 finish) {
		if (!isKeyPressed(controls.attack.keyCode) && !isKeyPressed(controls.rangedAttack.keyCode)) {
			renderArrow(start, finish, new Color(0f, 1f, 0f, 0.65f), 3f, 0f, 75f);
		}
	}


	/**
	 * Renders the jump arrow, coordinates are world coordinates
	 */
	public void renderArrow(final Vector2 start, final Vector2 finish, final Color color, final float lineWidth, final float arrowSize, final float maxLength) {
		final Vector2 difference = finish.cpy().sub(start);
		final Vector2 arrowHead = start.cpy().add(
			difference.cpy().nor().scl(Math.min(difference.len(), maxLength))
		);

		final Vector2 fin = arrowHead.cpy().sub(
			difference.cpy().nor().scl(14f)
		);

		graphics.getSpriteBatch().flush();
		getShapeRenderer().begin(ShapeType.Line);
		Gdx.gl20.glLineWidth(lineWidth);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		getShapeRenderer().setColor(color);
		getShapeRenderer().line(
			worldToScreenX(start.x),
			worldToScreenY(start.y),
			worldToScreenX(fin.x),
			worldToScreenY(fin.y)
		);
		getShapeRenderer().end();

		getShapeRenderer().begin(ShapeType.Filled);
		getShapeRenderer().setColor(color);

		final Vector2 point = arrowHead.cpy().add(difference.cpy().nor().scl(5f + arrowSize));
		final Vector2 corner1 = arrowHead.cpy().sub(difference.cpy().nor().rotate(25f).scl(15f + arrowSize / 2f));
		final Vector2 corner2 = arrowHead.cpy().sub(difference.cpy().nor().rotate(-25f).scl(15f + arrowSize / 2f));

		getShapeRenderer().triangle(
			worldToScreenX(point.x),
			worldToScreenY(point.y),
			worldToScreenX(corner1.x),
			worldToScreenY(corner1.y),
			worldToScreenX(corner2.x),
			worldToScreenY(corner2.y)
		);
		getShapeRenderer().end();
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
		renderMouseText();
	}


	private void renderMouseText() {
		if (inputProcessor.getCursorBoundTask() != null) {
			return;
		}

		final boolean jumpPressed = isKeyPressed(controls.jump.keyCode);
		final boolean addWayPointPressed = isKeyPressed(controls.addWayPoint.keyCode);
		final boolean forceMovePressed = isKeyPressed(controls.forceMove.keyCode);
		final boolean attackPressed = isKeyPressed(controls.attack.keyCode);
		final boolean rangedAttackPressed = isKeyPressed(controls.rangedAttack.keyCode);
		final boolean mineTIlePressed = isKeyPressed(controls.mineTile.keyCode);

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


	private void renderFloatingText(final int worldId) {
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
	private void renderDebugText() {
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
		defaultFont.draw(graphics.getSpriteBatch(), "Number of tasks queued in Saver thread: " + Integer.toString(threadedTasks.saverTasks.size()), 5, graphics.getHeight() - 165);

		try {
			defaultFont.draw(
				graphics.getSpriteBatch(),
				"Superstructure on mouse: " + gameClientStateTracker.getActiveWorld().getTopography().getStructures().getStructure(convertToChunkCoord(getMouseWorldCoords().x), convertToChunkCoord(getMouseWorldCoords().y), true).toString(),
				5,
				graphics.getHeight() - 185
			);
		} catch (final NullPointerException e) {}

		defaultFont.setColor(Color.CYAN);
	}


	/** Renders layered components, e.g. {@link Window}s */
	private synchronized void renderLayeredComponents() {
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
	private void renderContextMenus() {
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


	public List<ContextMenu> getContextMenus() {
		return contextMenus;
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


	public void addFloatingText(final String text, final Color color, final Vector2 position, final boolean ui, final int worldId) {
		addFloatingText(floatingText(text, color, position, ui), worldId, false);
	}


	public void addFloatingText(final FloatingText floatingText, final int worldId, final boolean csi) {
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


	public void addUIFloatingText(final String text, final Color color, final Vector2 position) {
		addFloatingText(floatingText(text, color, position, true), gameClientStateTracker.getActiveWorldId(), false);
	}


	/**
	 * Called when the scroll wheel is scrolled.
	 */
	public void scrolled(final int amount) {
		for (final Component component : newArrayList(layeredComponents)) {
			if (component.isActive()) {
				component.scrolled(amount);
			}
		}
	}


	/**
	 * Removes a layered component
	 */
	public synchronized void removeLayeredComponent(final Component toRemove) {
		layeredComponents.remove(toRemove);
	}


	/**
	 * Removes a layered component
	 */
	public synchronized void removeLayeredComponent(final String title) {
		for (final Component component : layeredComponents) {
			if (component instanceof Window && ((Window) component).title.equals(title)) {
				component.setClosing(true);
			}
		}
	}


	/** Adds a {@link Component} to {@link #layeredComponents} */
	public synchronized void addLayeredComponent(final Component toAdd) {
		for (final Component component : layeredComponents) {
			if (component instanceof Window) {
				((Window)component).setActive(false);
			}
		}
		layeredComponents.addLast(toAdd);
	}


	/** Adds a {@link Component} to {@link #layeredComponents}, checking if an existing one with the same title exists */
	public synchronized void addLayeredComponentUnique(final Window toAdd) {
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


	public void addClientMessage(final String title, final String message) {
		addGlobalMessage(title, message, -1, new FalseFunction());
	}


	public void addClientMessage(final String title, final SerializableFunction<String> message) {
		addGlobalMessage(title, message, -1, new FalseFunction());
	}


	public void addGlobalMessage(final String title, final String message) {
		addGlobalMessage(title, message, -1, new AlwaysTrueFunction());
	}


	public void addGlobalMessage(final String title, final String message, final SerializableFunction<Boolean> function) {
		addGlobalMessage(title, message, -1, function);
	}


	public void addGlobalMessage(final String title, final SerializableFunction<String> message, final int client, final SerializableFunction<Boolean> function) {
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


	public void addGlobalMessage(final String title, final String message, final int client, final SerializableFunction<Boolean> function) {
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


	public synchronized void closeAllWindows() {
		for (final Component component : layeredComponents) {
			component.setClosing(true);
		}
	}


	public InfoPopup getInfoPopup() {
		return infoPopup;
	}


	public void setInfoPopup(final InfoPopup infoPopup) {
		this.infoPopup = infoPopup;
	}


	public void addButton(final String name, final Button button) {
		buttons.put(name, button);
	}


	public void removeButton(final String button) {
		buttons.remove(button);
	}


	public synchronized void clearLayeredComponents() {
		layeredComponents.clear();
	}


	public synchronized void addLayeredComponents(final Collection<Component> toAdd) {
		layeredComponents.addAll(toAdd);
	}


	public ShapeRenderer getShapeRenderer() {
		return shapeRenderer;
	}
}
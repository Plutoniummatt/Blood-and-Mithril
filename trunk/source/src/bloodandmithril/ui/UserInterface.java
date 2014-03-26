package bloodandmithril.ui;

import static bloodandmithril.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.BloodAndMithrilClient.cam;
import static bloodandmithril.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.BloodAndMithrilClient.paused;
import static bloodandmithril.BloodAndMithrilClient.ping;
import static bloodandmithril.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.BloodAndMithrilClient.worldToScreenY;
import static bloodandmithril.csi.ClientServerInterface.isServer;
import static bloodandmithril.persistence.GameSaver.isSaving;
import static bloodandmithril.ui.KeyMappings.cameraDrag;
import static bloodandmithril.ui.KeyMappings.leftClick;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.WorldState.currentEpoch;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.GL10.GL_BLEND;
import static com.badlogic.gdx.graphics.GL10.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL10.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.FilledRectangle;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Rectangle;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.Interface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.world.ChunkLoaderImpl;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.bar.BottomBar;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Chunk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

/**
 * Class representing UI
 *
 * @author Matt
 */
public class UserInterface {

	private static final Color DARK_SCREEN_COLOR 			= new Color(0f, 0f, 0f, 0.8f);
	private static final Color EXISTING_INTERFACE_COLOR 	= new Color(1f, 0.2f, 0f, 0.5f);
	private static final Color AVAILABLE_INTERFACE_COLOR 	= new Color(0.2f, 1f, 0f, 0.5f);
	private static final Color TILE_OVERLAY_COLOR 			= new Color(0f, 1f, 1f, 0.3f);
	private static final Color COMPONENT_BOUNDARY_COLOR 	= new Color(1f, 1f, 1f, 0.5f);
	private static final Color COMPONENT_FILL_COLOR 		= new Color(0f, 1f, 0f, 0.15f);

	/** UI camera */
	public static OrthographicCamera UICamera;

	/** List of {@link Button}s */
	public static HashMap<String, Button> buttons = newHashMap();

	/** Unpause button */
	private static Button unpauseButton, savingButton;

	/** {@link ContextMenu}s */
	public static List<ContextMenu> contextMenus = new ArrayList<ContextMenu>();

	/** {@link Window}s */
	public static ArrayDeque<Component> layeredComponents = new ArrayDeque<Component>();

	/** Shape renderer */
	public static ShapeRenderer shapeRenderer = new ShapeRenderer();

	/** The texture atlas for other UI elements */
	public static final Texture uiTexture = new Texture(files.internal("data/image/ui.png"));

	/** Initial coordinates for the drag box, see {@link #renderDragBox()} */
	private static Vector2 initialDragCoordinates = null;

	/** A flag to indicate whether we should render the available interfaces or existing interfaces */
	public static boolean renderAvailableInterfaces = true, renderComponentBoundaries = true;
	
	/** Whether to render debug UI */
	public static boolean DEBUG = false;
	
	/** Whether to render debug UI */
	public static boolean RENDER_TOPOGRAPHY = false;

	/** Texture regions */
	public static TextureRegion finalWaypointTexture 	= new TextureRegion(UserInterface.uiTexture, 0, 42, 16, 16);
	public static TextureRegion currentArrow 			= new TextureRegion(UserInterface.uiTexture, 0, 0, 11, 8);
	public static TextureRegion followArrow 			= new TextureRegion(UserInterface.uiTexture, 0, 34, 11, 8);

	/**
	 * Steup for UI, makes everything it needs.
	 *
	 * @param WIDTH - initial window width
	 * @param HEIGHT - initial window height
	 */
	public static void setup() {
		loadBars();
		loadButtons();
	}


	/**
	 * Load the task bar and the status bar
	 */
	private static void loadBars() {
		layeredComponents.add(new BottomBar());
	}


	public static void refreshInventoryWindows() {
		for (Component component : UserInterface.layeredComponents) {
			if (component instanceof TradeWindow) {
				((TradeWindow) component).refresh();
			} else if (component instanceof InventoryWindow) {
				((InventoryWindow) component).refresh();
			}
		}
	}


	/**
	 * Load the buttons
	 */
	private static void loadButtons() {
		Button pauseButton = new Button("Pause", defaultFont, -32, 4, 55, 16, new Task() {
			@Override
			public void execute() {
				BloodAndMithrilClient.paused = true;
			}
		}, Color.WHITE, Color.GREEN, Color.WHITE, UIRef.TR);

		unpauseButton = new Button("Unpause", defaultFont, 0, 0, 75, 16, new Task() {
			@Override
			public void execute() {
				BloodAndMithrilClient.paused = false;
			}
		}, Color.WHITE, Color.GREEN, Color.WHITE, UIRef.M);

		savingButton = new Button("Saving...", defaultFont, 0, 0, 75, 16, new Task() {
			@Override
			public void execute() {
			}
		}, Color.WHITE, Color.WHITE, Color.WHITE, UIRef.M);

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
	 * Renders the coordinates of the {@link BloodAndMithrilClient#cam} used in
	 * {@link BloodAndMithrilClient} at the bottom left of the screen.
	 */
	public static void render() {

		//Individual sprites (Selected arrow, movement arrow etc)
		renderIndividualUISprites();

		spriteBatch.setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);

		if (DEBUG && isServer()) {
			renderComponentInterfaces();
			if (renderComponentBoundaries) {
				renderComponentBoundaries();
			}
			renderMouseOverTileHighlightBox();
		}

		renderDragBox();
		renderLayeredComponents();
		renderContextMenus();

		spriteBatch.setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);
		spriteBatch.begin();
		if (DEBUG) {
			renderDebugText();
		}

		if (BloodAndMithrilClient.domain != null) {
			renderUIText();
		}
		renderButtons();

		spriteBatch.end();

		renderPauseScreen();
		renderSavingScreen();
		
		if (RENDER_TOPOGRAPHY) {
			TopographyDebugRenderer.render();
		}
	}


	/**
	 * Renders the {@link Boundaries} of all {@link bloodandmithril.generation.component.Component}s
	 */
	private static void renderComponentBoundaries() {
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (Structure struct : Structures.getStructures().values()) {
			for (bloodandmithril.generation.component.Component comp : newArrayList(struct.getComponents())) {
				shapeRenderer.begin(FilledRectangle);
				shapeRenderer.setColor(COMPONENT_FILL_COLOR);
				shapeRenderer.filledRect(
					worldToScreenX(comp.getBoundaries().left * TILE_SIZE),
					worldToScreenY(comp.getBoundaries().bottom * TILE_SIZE),
					(comp.getBoundaries().right - comp.getBoundaries().left + 1) * TILE_SIZE,
					(comp.getBoundaries().top - comp.getBoundaries().bottom + 1) * TILE_SIZE
				);
				shapeRenderer.end();

				shapeRenderer.begin(Rectangle);
				shapeRenderer.setColor(COMPONENT_BOUNDARY_COLOR);
				shapeRenderer.rect(
					worldToScreenX(comp.getBoundaries().left * TILE_SIZE),
					worldToScreenY(comp.getBoundaries().bottom * TILE_SIZE),
					(comp.getBoundaries().right - comp.getBoundaries().left + 1) * TILE_SIZE,
					(comp.getBoundaries().top - comp.getBoundaries().bottom + 1) * TILE_SIZE
				);
				shapeRenderer.end();
			}
		}
		gl.glDisable(GL_BLEND);
	}


	/**
	 * Renders a small rectangle to indicate the current tile the mouse is over
	 */
	private static void renderMouseOverTileHighlightBox() {
		float x = worldToScreenX(TILE_SIZE * convertToWorldTileCoord(getMouseWorldX()));
		float y = worldToScreenY(TILE_SIZE * convertToWorldTileCoord(getMouseWorldY()));

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(FilledRectangle);
		shapeRenderer.setColor(TILE_OVERLAY_COLOR);
		shapeRenderer.filledRect(x, y, TILE_SIZE, TILE_SIZE);
		shapeRenderer.end();
		gl.glDisable(GL_BLEND);
	}


	private static void renderComponentInterfaces() {
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
			spriteBatch.begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.FilledRectangle);
			shapeRenderer.setColor(DARK_SCREEN_COLOR);
			shapeRenderer.filledRect(0, 0, WIDTH, HEIGHT);
			shapeRenderer.end();
			savingButton.render(true, 1f);
			gl.glDisable(GL_BLEND);
			spriteBatch.end();
		}
	}


	/** Darkens the screen by 50% and draws an "unpause" button on the screen if the game is paused */
	private static void renderPauseScreen() {
		if (paused) {
			spriteBatch.begin();
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.FilledRectangle);
			shapeRenderer.setColor(DARK_SCREEN_COLOR);
			shapeRenderer.filledRect(0, 0, WIDTH, HEIGHT);
			shapeRenderer.end();

			if (unpauseButton != null) {
				unpauseButton.render(true, 1f);
			}

			gl.glDisable(GL_BLEND);
			spriteBatch.end();
		}
	}


	/**
	 * Called when the left click is released
	 */
	public static void leftClickRelease(int screenX, int screenY) {

		if (initialDragCoordinates != null) {
			Vector2 diagCorner1 = initialDragCoordinates.cpy();
			Vector2 diagCorner2 = new Vector2(screenX, screenY);

			float left = min(diagCorner1.x, diagCorner2.x);
			float right = max(diagCorner1.x, diagCorner2.x);
			float top = max(diagCorner1.y, diagCorner2.y);
			float bottom = min(diagCorner1.y, diagCorner2.y);

			if (right - left < 3 || top - bottom < 3) {
				return;
			}

			for (Individual indi : Domain.getIndividuals().values()) {
				if (indi.isControllable()) {

					Vector2 centre = new Vector2(indi.getState().position.x, indi.getState().position.y + indi.getHeight() / 2);

					centre.x = worldToScreenX(centre.x);
					centre.y = worldToScreenY(centre.y);

					if (centre.x > left && centre.x < right && centre.y > bottom && centre.y < top) {
						if (isServer()) {
							indi.select(0);
							Domain.getSelectedIndividuals().add(indi);
						} else {
							ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), true);
						}
					} else if (Domain.getSelectedIndividuals().contains(indi)) {
						if (isServer()) {
							indi.deselect(false, 0);
							Domain.getSelectedIndividuals().remove(indi);
						} else {
							ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
						}
					}
				}
			}
		}

		if (!layeredComponents.isEmpty()) {
			Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				iter.next().leftClickReleased();
			}
		}
	}


	/**
	 * Renders the drag-box
	 */
	private static void renderDragBox() {
		if (input.isButtonPressed(leftClick) && initialDragCoordinates != null && !input.isKeyPressed(cameraDrag)) {
			shapeRenderer.begin(Rectangle);
			shapeRenderer.setColor(Color.GREEN);
			float width = getMouseScreenX() - initialDragCoordinates.x;
			float height = getMouseScreenY() - initialDragCoordinates.y;
			shapeRenderer.rect(initialDragCoordinates.x, initialDragCoordinates.y, width, height);
			shapeRenderer.end();
		}
	}


	private static void renderIndividualUISprites() {
		spriteBatch.begin();
		for (Individual indi : Domain.getIndividuals().values()) {
			if (indi.isSelected()) {
				AITask currentTask = indi.getAI().getCurrentTask();
				if (currentTask instanceof GoToLocation) {
					shapeRenderer.setColor(Color.WHITE);
					// ((GoToLocation)currentTask).renderPath();
					((GoToLocation)currentTask).renderFinalWayPoint();
				}
			}
			indi.renderArrows();
			spriteBatch.flush();
		}
		spriteBatch.end();
	}


	/** Any text that is rendered on UI */
	private static void renderUIText() {
		defaultFont.setColor(Color.WHITE);
		defaultFont.draw(spriteBatch, "Time: " + currentEpoch.getTimeString(), 5, HEIGHT - 5);
		defaultFont.draw(spriteBatch, "Date: " + currentEpoch.getDateString(), 5, HEIGHT - 25);
		defaultFont.draw(spriteBatch, "Ping: " + ping, 5, HEIGHT - 45);
	}


	/** Debug text */
	private static void renderDebugText() {
		defaultFont.setColor(Color.YELLOW);
		defaultFont.draw(spriteBatch, Integer.toString(convertToWorldTileCoord(getMouseWorldX())) + ", " + Float.toString(convertToWorldTileCoord(getMouseWorldY())), getMouseScreenX() - 35, getMouseScreenY() - 35);
		defaultFont.draw(spriteBatch, "Mouse World Coords: " + getMouseWorldX() + ", " + getMouseWorldY(), 5, 72);
		defaultFont.draw(spriteBatch, "Centre of screen Coords: " + Float.toString(cam.position.x) + ", " + Float.toString(cam.position.y) + ", " + Float.toString(cam.zoom), 5, 52);

		int chunksInMemory = 0;
		if (ClientServerInterface.isServer()) {
			for (Entry<Integer, ConcurrentHashMap<Integer, Chunk>> entry : Domain.getActiveWorld().getTopography().getChunkMap().chunkMap.entrySet()) {
				chunksInMemory = chunksInMemory + entry.getValue().size();
			}
		}

		defaultFont.setColor(Color.GREEN);
		defaultFont.draw(spriteBatch, "Number of chunks in memory of active world: " + Integer.toString(chunksInMemory), 5, Gdx.graphics.getHeight() - 55);
		defaultFont.draw(spriteBatch, "Number of tasks queued in AI thread: " + Integer.toString(AIProcessor.aiThreadTasks.size()), 5, Gdx.graphics.getHeight() - 125);
		defaultFont.draw(spriteBatch, "Number of tasks queued in Loader thread: " + Integer.toString(ChunkLoaderImpl.loaderTasks.size()), 5, Gdx.graphics.getHeight() - 145);
		defaultFont.draw(spriteBatch, "Number of tasks queued in Saver thread: " + Integer.toString(GameSaver.saverTasks.size()), 5, Gdx.graphics.getHeight() - 165);
		defaultFont.draw(spriteBatch, "Number of tasks queued in Pathfinding thread: " + Integer.toString(AIProcessor.pathFinderTasks.size()), 5, Gdx.graphics.getHeight() - 185);

		defaultFont.setColor(Color.CYAN);
	}


	/** Renders layered components, e.g. {@link Window}s */
	private static void renderLayeredComponents() {
		ArrayDeque<Component> copy = new ArrayDeque<>(layeredComponents);
		for (Component component : layeredComponents) {
			if (component instanceof Window) {
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

		if (!layeredComponents.isEmpty()) {
			ArrayDeque<Component> windowsCopy = new ArrayDeque<Component>(layeredComponents);
			Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				if (iter.next().leftClick(contextMenuCopy, windowsCopy)) {
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
			if (!menu.isInside(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY())) {
				contextMenuCopy.remove(menu);
			}
		}

		contextMenus = contextMenuCopy;

		if (!clicked && !Gdx.input.isKeyPressed(KeyMappings.cameraDrag)) {
			initialDragCoordinates = new Vector2(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());
		} else {
			initialDragCoordinates = null;
		}

		return clicked;
	}


	public static boolean keyPressed(int keyCode) {
		if (BloodAndMithrilClient.paused) {
			return false;
		}

		if (GameSaver.isSaving()) {
			return false;
		}

		for (Component component : layeredComponents) {
			if (component.isActive() && !(component instanceof BottomBar)) {
				return component.keyPressed(keyCode);
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
	public static void rightClick() {
		if (BloodAndMithrilClient.paused || GameSaver.isSaving()) {
			return;
		}

		contextMenus.clear();
		ContextMenu newMenu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		if (!layeredComponents.isEmpty()) {
			ArrayDeque<Component> windowsCopy = new ArrayDeque<Component>(layeredComponents);
			Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				Component next = iter.next();
				if (next instanceof Window && ((Window)next).rightClick(windowsCopy)) {
					break;
				}
			}
			layeredComponents = windowsCopy;
		}

		for (final Individual indi : Domain.getIndividuals().values()) {
			if (indi.isMouseOver()) {
				final ContextMenu secondaryMenu = indi.getContextMenu();
				newMenu.getMenuItems().add(
					new ContextMenuItem(
						indi.getId().getSimpleName() + " (" + indi.getClass().getSimpleName() + ")",
						new Task() {
							@Override
							public void execute() {
								secondaryMenu.x = BloodAndMithrilClient.getMouseScreenX();
								secondaryMenu.y = BloodAndMithrilClient.getMouseScreenY();
							}
						},
						Color.WHITE,
						indi.getToolTipTextColor(),
						indi.getToolTipTextColor(),
						secondaryMenu
					)
				);
			}
		}

		for (final Prop prop : Domain.getProps().values()) {
			if (prop.isMouseOver()) {
				final ContextMenu secondaryMenu = prop.getContextMenu();
				newMenu.getMenuItems().add(
					new ContextMenuItem(
						prop.getContextMenuLabel(),
						new Task() {
							@Override
							public void execute() {
								secondaryMenu.x = getMouseScreenX();
								secondaryMenu.y = getMouseScreenY();
							}
						},
						Color.WHITE,
						Color.GREEN,
						Color.GRAY,
						secondaryMenu
					)
				);
			}
		}

		if (!newMenu.getMenuItems().isEmpty()) {
			contextMenus.add(newMenu);
		}
	}


	/** Adds a {@link Component} to {@link #layeredComponents} */
	public static void addLayeredComponent(Component toAdd) {
		for (Component component : layeredComponents) {
			if (component instanceof Window) {
				((Window)component).setActive(false);
			}
		}
		layeredComponents.addLast(toAdd);
	}


	/** Adds a {@link Component} to {@link #layeredComponents}, checking if an existing one with the same title exists */
	public static void addLayeredComponentUnique(Component toAdd, String title) {
		Window existing = null;

		for (Component window : layeredComponents) {
			if (window instanceof Window && ((Window)window).title.equals(title)) {
				existing = (Window)window;
				break;
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


	/**
	 * Where an UI element should be rendered
	 *
	 * @author Matt
	 */
	public enum UIRef {
		TL, TM, TR, M, BL, BM, BR
	}
}
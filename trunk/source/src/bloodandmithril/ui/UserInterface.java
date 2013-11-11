package bloodandmithril.ui;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.StructureMap;
import bloodandmithril.generation.component.Interface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.world.ChunkLoaderImpl;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.bar.BottomBar;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.WorldState;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * Class representing UI
 *
 * @author Matt
 */
public class UserInterface {

	/** UI camera */
	public static OrthographicCamera UICamera;

	/** List of {@link Button}s */
	public static HashMap<String, Button> buttons = new HashMap<String, Button>();

	/** Unpause button */
	private static Button unpauseButton, savingButton;

	/** {@link ContextMenu}s */
	public static List<ContextMenu> contextMenus = new ArrayList<ContextMenu>();

	/** {@link Window}s */
	public static ArrayDeque<Component> layeredComponents = new ArrayDeque<Component>();

	/** Shape renderer */
	public static ShapeRenderer shapeRenderer = new ShapeRenderer();

	/** The texture atlas for other UI elements */
	public static final Texture uiTexture = new Texture(Gdx.files.internal("data/image/ui.png"));

	/** Initial coordinates for the drag box, see {@link #renderDragBox()} */
	private static Vector2 initialDragCoordinates = null;

	/** A flag to indicate whether we should render the available interfaces or existing interfaces */
	public static boolean renderAvailableInterfaces = true, renderComponentBoundaries = true;

	/** Texture regions */
	public static TextureRegion finalWaypointTexture = new TextureRegion(UserInterface.uiTexture, 0, 42, 16, 16);
	public static TextureRegion currentArrow = new TextureRegion(UserInterface.uiTexture, 0, 0, 11, 8);
	public static TextureRegion followArrow = new TextureRegion(UserInterface.uiTexture, 0, 34, 11, 8);

	/**
	 * Steup for UI, makes everything it needs.
	 *
	 * @param WIDTH - initial window width
	 * @param HEIGHT - initial window height
	 */
	public static void setup(int WIDTH, int HEIGHT) {
		UICamera = new OrthographicCamera(WIDTH, HEIGHT);
		UICamera.setToOrtho(false, WIDTH, HEIGHT);
		loadBars();
		loadButtons();
	}


	/**
	 * Load the task bar and the status bar
	 */
	private static void loadBars() {
		layeredComponents.add(new BottomBar());
	}


	/**
	 * Load the buttons
	 */
	private static void loadButtons() {
		Button pauseButton = new Button("Pause", defaultFont, -32, 4, 55, 16, new Task() {
			@Override
			public void execute() {
				Fortress.paused = true;
			}
		}, Color.WHITE, Color.GREEN, Color.WHITE, UIRef.TR);

		unpauseButton = new Button("Unpause", defaultFont, 0, 0, 75, 16, new Task() {
			@Override
			public void execute() {
				Fortress.paused = false;
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
	 * Renders the coordinates of the {@link Fortress#cam} used in
	 * {@link Fortress} at the bottom left of the screen.
	 */
	public static void render() {

		//Individual sprites (Selected arrow, movement arrow etc)
		renderIndividualUISprites();

		Fortress.spriteBatch.setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);

		if ("true".equals(System.getProperty("debug"))) {
			renderComponentInterfaces();
			if (renderComponentBoundaries) {
				renderComponentBoundaries();
			}
			renderMouseOverTileHighlightBox();
		}

		renderDragBox();
		renderLayeredComponents();
		renderContextMenus();

		Fortress.spriteBatch.setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);
		Fortress.spriteBatch.begin();
		if (System.getProperty("debug").equals("true")) {
			renderDebugText();
		}

		renderUIText();
		renderButtons();

		Fortress.spriteBatch.end();

		renderPauseScreen();
		renderSavingScreen();
	}


	/**
	 * Renders the {@link Boundaries} of all {@link bloodandmithril.generation.component.Component}s
	 */
	private static void renderComponentBoundaries() {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		for (Structure struct : StructureMap.structures.values()) {
			for (bloodandmithril.generation.component.Component comp : Lists.newArrayList(struct.components)) {
				shapeRenderer.begin(ShapeType.FilledRectangle);
				shapeRenderer.setColor(new Color(0f, 1f, 0f, 0.15f));
				shapeRenderer.filledRect(
					Fortress.worldToScreenX(comp.boundaries.left * Topography.TILE_SIZE),
					Fortress.worldToScreenY(comp.boundaries.bottom * Topography.TILE_SIZE),
					(comp.boundaries.right - comp.boundaries.left + 1) * Topography.TILE_SIZE,
					(comp.boundaries.top - comp.boundaries.bottom + 1) * Topography.TILE_SIZE
				);
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Rectangle);
				shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.5f));
				shapeRenderer.rect(
					Fortress.worldToScreenX(comp.boundaries.left * Topography.TILE_SIZE),
					Fortress.worldToScreenY(comp.boundaries.bottom * Topography.TILE_SIZE),
					(comp.boundaries.right - comp.boundaries.left + 1) * Topography.TILE_SIZE,
					(comp.boundaries.top - comp.boundaries.bottom + 1) * Topography.TILE_SIZE
				);
				shapeRenderer.end();
			}
		}
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}


	/**
	 * Renders a small rectangle to indicate the current tile the mouse is over
	 */
	private static void renderMouseOverTileHighlightBox() {
		float x = Fortress.worldToScreenX(Topography.TILE_SIZE * Topography.convertToWorldTileCoord(Fortress.getMouseWorldX()));
		float y = Fortress.worldToScreenY(Topography.TILE_SIZE * Topography.convertToWorldTileCoord(Fortress.getMouseWorldY()));

		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeType.FilledRectangle);
		shapeRenderer.setColor(new Color(0f, 1f, 1f, 0.3f));
		shapeRenderer.filledRect(x, y, Topography.TILE_SIZE, Topography.TILE_SIZE);
		shapeRenderer.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}


	private static void renderComponentInterfaces() {
		Color availableColor = new Color(0.2f, 1f, 0f, 0.5f);
		Color existingColor = new Color(1f, 0.2f, 0f, 0.5f);

		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		for (Structure struct : StructureMap.structures.values()) {
			for (bloodandmithril.generation.component.Component comp : Lists.newArrayList(struct.components)) {
				if (renderAvailableInterfaces) {
					for (Interface in : Lists.newArrayList(comp.availableInterfaces)) {
						in.render(availableColor);
					}
				} else {
					for (Interface in : Lists.newArrayList(comp.existingInterfaces)) {
						in.render(existingColor);
					}
				}
			}
		}
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}


	/** Darkens the screen by 80% and draws "Saving..." on the screen if the game is being saved */
	private static void renderSavingScreen() {
		if (GameSaver.isSaving()) {
			Fortress.spriteBatch.begin();
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.FilledRectangle);
			shapeRenderer.setColor(new Color(0f, 0f, 0f, 0.8f));
			shapeRenderer.filledRect(0, 0, Fortress.WIDTH, Fortress.HEIGHT);
			shapeRenderer.end();
			savingButton.render(true, 1f);
			Gdx.gl.glDisable(GL10.GL_BLEND);
			Fortress.spriteBatch.end();
		}
	}


	/** Darkens the screen by 50% and draws an "unpause" button on the screen if the game is paused */
	private static void renderPauseScreen() {
		if (Fortress.paused) {
			Fortress.spriteBatch.begin();
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.FilledRectangle);
			shapeRenderer.setColor(new Color(0f, 0f, 0f, 0.5f));
			shapeRenderer.filledRect(0, 0, Fortress.WIDTH, Fortress.HEIGHT);
			shapeRenderer.end();
			unpauseButton.render(true, 1f);
			Gdx.gl.glDisable(GL10.GL_BLEND);
			Fortress.spriteBatch.end();
		}
	}


	/**
	 * Called when the left click is released
	 */
	public static void leftClickRelease(int screenX, int screenY) {

		if (initialDragCoordinates != null) {
			Vector2 diagCorner1 = initialDragCoordinates.cpy();
			Vector2 diagCorner2 = new Vector2(screenX, screenY);

			float left = Math.min(diagCorner1.x, diagCorner2.x);
			float right = Math.max(diagCorner1.x, diagCorner2.x);
			float top = Math.max(diagCorner1.y, diagCorner2.y);
			float bottom = Math.min(diagCorner1.y, diagCorner2.y);

			for (Individual indi : GameWorld.individuals.values()) {
				if (indi.controllable) {
					Vector2 centre = new Vector2(indi.state.position.x, indi.state.position.y + indi.height / 2);

					centre.x = Fortress.worldToScreenX(centre.x);
					centre.y = Fortress.worldToScreenY(centre.y);

					if (centre.x > left && centre.x < right && centre.y > bottom && centre.y < top) {
						indi.select();
						GameWorld.selectedIndividuals.add(indi);
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
		if (Gdx.input.isButtonPressed(KeyMappings.leftClick) && initialDragCoordinates != null && !Gdx.input.isKeyPressed(KeyMappings.cameraDrag)) {
			shapeRenderer.begin(ShapeType.Rectangle);
			shapeRenderer.setColor(Color.GREEN);
			float width = Fortress.getMouseScreenX() - initialDragCoordinates.x;
			float height = Fortress.getMouseScreenY() - initialDragCoordinates.y;
			shapeRenderer.rect(initialDragCoordinates.x, initialDragCoordinates.y, width, height);
			shapeRenderer.end();
		}
	}


	private static void renderIndividualUISprites() {
		Fortress.spriteBatch.begin();
		for (Individual indi : GameWorld.individuals.values()) {
			if (indi.isSelected()) {
				AITask currentTask = indi.ai.getCurrentTask();
				if (currentTask instanceof GoToLocation) {
					shapeRenderer.setColor(Color.WHITE);
					((GoToLocation)currentTask).renderFinalWayPoint();
				}
			}
			indi.renderArrows();
			Fortress.spriteBatch.flush();
		}
		Fortress.spriteBatch.end();
	}


	/** Any text that is rendered on UI */
	private static void renderUIText() {
		defaultFont.setColor(Color.WHITE);
		defaultFont.draw(Fortress.spriteBatch, "Time: " + WorldState.currentEpoch.getTimeString(), 5, Gdx.graphics.getHeight() - 5);
		defaultFont.draw(Fortress.spriteBatch, "Date: " + WorldState.currentEpoch.getDateString(), 5, Gdx.graphics.getHeight() - 25);
	}


	/** Debug text */
	private static void renderDebugText() {
		defaultFont.setColor(Color.YELLOW);
		defaultFont.draw(Fortress.spriteBatch, Float.toString(Topography.convertToWorldTileCoord(Fortress.getMouseWorldX())) + ", " + Float.toString(Topography.convertToWorldTileCoord(Fortress.getMouseWorldY())), Fortress.getMouseScreenX() - 35, Fortress.getMouseScreenY() - 35);
		defaultFont.draw(Fortress.spriteBatch, "Mouse World Coords: " + Fortress.getMouseWorldX() + ", " + Fortress.getMouseWorldY(), 5, 72);
		defaultFont.draw(Fortress.spriteBatch, "Centre of screen Coords: " + Float.toString(Fortress.cam.position.x) + ", " + Float.toString(Fortress.cam.position.y) + ", " + Float.toString(Fortress.cam.zoom), 5, 52);

		int chunksInMemory = 0;
		for (Entry<Integer, ConcurrentHashMap<Integer, Chunk>> entry : Topography.chunkMap.chunkMap.entrySet()) {
			chunksInMemory = chunksInMemory + entry.getValue().size();
		}

		defaultFont.setColor(Color.GREEN);
		defaultFont.draw(Fortress.spriteBatch, "Number of chunks in memory: " + Integer.toString(chunksInMemory), 5, Gdx.graphics.getHeight() - 55);

		defaultFont.draw(Fortress.spriteBatch, "Number of tasks queued in AI thread: " + Integer.toString(AIProcessor.aiThreadTasks.size()), 5, Gdx.graphics.getHeight() - 125);
		defaultFont.draw(Fortress.spriteBatch, "Number of tasks queued in Loader thread: " + Integer.toString(ChunkLoaderImpl.loaderTasks.size()), 5, Gdx.graphics.getHeight() - 145);
		defaultFont.draw(Fortress.spriteBatch, "Number of tasks queued in Saver thread: " + Integer.toString(GameSaver.saverTasks.size()), 5, Gdx.graphics.getHeight() - 165);
		defaultFont.draw(Fortress.spriteBatch, "Number of tasks queued in Pathfinding thread: " + Integer.toString(AIProcessor.pathFinderTasks.size()), 5, Gdx.graphics.getHeight() - 185);

		defaultFont.setColor(Color.CYAN);
	}


	/** Renders layered components, e.g. {@link Window}s */
	private static void renderLayeredComponents() {
		ArrayDeque<Component> copy = new ArrayDeque<>(layeredComponents);
		for (Component component : layeredComponents) {
			if (component instanceof Window) {
				if (!((Window)component).minimized || component.alpha > 0f) {
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
				next.active = false;
				next.render();
			} else {
				next.active = true;
				next.render();
			}
		}
	}


	/**
	 * Renders all buttons
	 */
	private static void renderButtons() {
		for (Entry<String, Button> buttonEntry : buttons.entrySet()) {
			buttonEntry.getValue().render(!Fortress.paused && !GameSaver.isSaving(), 1f);
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

		if (Fortress.paused) {
			clicked = unpauseButton.click();
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
			layeredComponents = windowsCopy;
		}

		Iterator<ContextMenu> iterator = contextMenus.iterator();
		while (iterator.hasNext()) {
			ContextMenu menu = iterator.next();
			if (!iterator.hasNext()) {
				clicked = menu.leftClick(contextMenuCopy, null) || clicked;
			}
			if (!menu.isInside(Fortress.getMouseScreenX(), Fortress.getMouseScreenY())) {
				contextMenuCopy.remove(menu);
			}
		}

		contextMenus = contextMenuCopy;

		if (!clicked && !Gdx.input.isKeyPressed(KeyMappings.cameraDrag)) {
			initialDragCoordinates = new Vector2(Fortress.getMouseScreenX(), Fortress.getMouseScreenY());
		} else {
			initialDragCoordinates = null;
		}

		return clicked;
	}


	/**
	 * Removes a layered component
	 */
	public static void removeLayeredComponent(Component toRemove) {
		layeredComponents.remove(toRemove);
	}


	/**
	 * Called when right mouse button is clicked
	 */
	public static void rightClick() {
		if (Fortress.paused || GameSaver.isSaving()) {
			return;
		}

		contextMenus.clear();
		ContextMenu newMenu = new ContextMenu(Fortress.getMouseScreenX(), Fortress.getMouseScreenY());

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

		for (final Individual indi : GameWorld.individuals.values()) {
			if (indi.isMouseOver()) {
				final ContextMenu secondaryMenu = indi.getContextMenu();
				newMenu.getMenuItems().add(
					new ContextMenuItem(
						indi.id.getSimpleName() + " (" + indi.getClass().getSimpleName() + ")",
						new Task() {
							@Override
							public void execute() {
								secondaryMenu.x = Fortress.getMouseScreenX();
								secondaryMenu.y = Fortress.getMouseScreenY();
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

		for (final Prop prop : GameWorld.props) {
			if (prop.isMouseOver()) {
				final ContextMenu secondaryMenu = prop.getContextMenu();
				newMenu.getMenuItems().add(
					new ContextMenuItem(
						prop.getClass().getSimpleName(),
						new Task() {
							@Override
							public void execute() {
								secondaryMenu.x = Fortress.getMouseScreenX();
								secondaryMenu.y = Fortress.getMouseScreenY();
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
				((Window)component).active = false;
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
			existing.active = true;
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

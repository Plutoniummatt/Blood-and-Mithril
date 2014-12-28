package bloodandmithril.ui;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.getCursorBoundTask;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.paused;
import static bloodandmithril.core.BloodAndMithrilClient.ping;
import static bloodandmithril.core.BloodAndMithrilClient.screenToWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.screenToWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.persistence.GameSaver.isSaving;
import static bloodandmithril.ui.KeyMappings.leftClick;
import static bloodandmithril.ui.KeyMappings.rightClick;
import static bloodandmithril.ui.UserInterface.FloatingText.floatingText;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.WorldState.getCurrentEpoch;
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

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.interfaces.Interface;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.EquipperImpl.AlwaysTrueFunction;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.bar.BottomBar;
import bloodandmithril.ui.components.window.BuildWindow;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Chunk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
	private static final Color MOUSE_OVER_TILE_BOX_COLOR = new Color(0f, 1f, 1f, 0.5f);
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
	public static ArrayDeque<Component> layeredComponents = new ArrayDeque<Component>();

	/** Shape renderer */
	public static ShapeRenderer shapeRenderer = new ShapeRenderer();

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

	/** Texture regions */
	public static TextureRegion finalWaypointTexture;
	public static TextureRegion jumpWaypointTexture;
	public static TextureRegion currentArrow;
	public static TextureRegion followArrow;

	private static final List<FloatingText> floatingTexts = Lists.newLinkedList();

	private static final Deque<Task> uiTasks = new ConcurrentLinkedDeque<>();

	static {
		if (ClientServerInterface.isClient()) {
			uiTexture = new Texture(files.internal("data/image/ui.png"));
			iconTexture = new Texture(files.internal("data/image/icons.png"));
			finalWaypointTexture = new TextureRegion(UserInterface.uiTexture, 0, 42, 16, 16);
			jumpWaypointTexture = new TextureRegion(UserInterface.uiTexture, 0, 59, 39, 29);
			currentArrow = new TextureRegion(UserInterface.uiTexture, 0, 0, 11, 8);
			followArrow = new TextureRegion(UserInterface.uiTexture, 0, 34, 11, 8);
		}
	}

	/**
	 * Setup for UI, makes everything it needs.
	 *
	 * @param WIDTH - initial window width
	 * @param HEIGHT - initial window height
	 */
	public static synchronized void setup() {
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

		spriteBatch.setShader(Shaders.text);
		Shaders.text.setUniformMatrix("u_projTrans", UICamera.combined);

		if (DEBUG && isServer()) {
			renderComponentInterfaces();
			renderPositionalIndexes();
			if (renderComponentBoundaries) {
				renderComponentBoundaries();
			}
			renderMouseOverTileHighlightBox();
		}

		renderCursorBoundTask();
		renderFloatingText();
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


	private static void renderPositionalIndexes() {
		defaultFont.setColor(Color.YELLOW);
		Collection<Object> nearbyEntities = Lists.newLinkedList();

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return Domain.getIndividual(id);
					}
				)
			)
		);

		nearbyEntities.addAll(
			Lists.newArrayList(
				Iterables.transform(
					Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, getMouseWorldX(), getMouseWorldY()),
					id -> {
						return Domain.getProp(id);
					}
				)
			)
		);

		int position = BloodAndMithrilClient.HEIGHT - 270;
		spriteBatch.begin();
		Fonts.defaultFont.draw(spriteBatch, "Entities near cursor:", 5, position + 40);
		for (Object nearbyEntity : nearbyEntities) {
			if (nearbyEntity instanceof Individual) {
				Fonts.defaultFont.draw(spriteBatch, ((Individual) nearbyEntity).getId().getSimpleName() + " (" + nearbyEntity.getClass().getSimpleName() + ")", 5, position);
			}

			if (nearbyEntity instanceof Prop) {
				Fonts.defaultFont.draw(spriteBatch, ((Prop) nearbyEntity).getClass().getSimpleName() + " " + nearbyEntity.hashCode(), 5, position);
			}
			position = position - 20;
		}
		spriteBatch.end();
	}


	private static void renderCursorBoundTask() {
		if (getCursorBoundTask() != null) {
			getCursorBoundTask().renderUIGuide();
			spriteBatch.begin();
			Fonts.defaultFont.setColor(Color.BLACK);
			Fonts.defaultFont.draw(
				spriteBatch,
				getCursorBoundTask().getShortDescription(),
				BloodAndMithrilClient.getMouseScreenX() + 20,
				BloodAndMithrilClient.getMouseScreenY() - 20
			);
			Fonts.defaultFont.setColor(Color.WHITE);
			Fonts.defaultFont.draw(
				spriteBatch,
				getCursorBoundTask().getShortDescription(),
				BloodAndMithrilClient.getMouseScreenX() + 21,
				BloodAndMithrilClient.getMouseScreenY() - 21
			);
			spriteBatch.end();
		}
	}


	/**
	 * Renders the {@link Boundaries} of all {@link bloodandmithril.generation.component.Component}s
	 */
	private static void renderComponentBoundaries() {
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (Structure struct : Structures.getStructures().values()) {
			for (bloodandmithril.generation.component.Component component : newArrayList(struct.getComponents())) {
				shapeRenderer.begin(FilledRectangle);
				shapeRenderer.setColor(COMPONENT_FILL_COLOR);
				shapeRenderer.filledRect(
					worldToScreenX(component.getBoundaries().left * TILE_SIZE),
					worldToScreenY(component.getBoundaries().bottom * TILE_SIZE),
					(component.getBoundaries().right - component.getBoundaries().left + 1) * TILE_SIZE,
					(component.getBoundaries().top - component.getBoundaries().bottom + 1) * TILE_SIZE
				);
				shapeRenderer.end();

				shapeRenderer.begin(Rectangle);
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
	private static void renderMouseOverTileHighlightBox() {
		float x = worldToScreenX(TILE_SIZE * convertToWorldTileCoord(getMouseWorldX()));
		float y = worldToScreenY(TILE_SIZE * convertToWorldTileCoord(getMouseWorldY()));

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(Rectangle);
		shapeRenderer.setColor(MOUSE_OVER_TILE_BOX_COLOR);
		shapeRenderer.rect(x, y, TILE_SIZE, TILE_SIZE);
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

		if (initialLeftMouseDragCoordinates != null) {
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


	public static void rightClickRelease(int screenX, int screenY) {
		if (initialRightMouseDragCoordinates != null && Gdx.input.isKeyPressed(KeyMappings.rightClickDragBox)) {
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
					return Domain.getItem(id);
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
										next.getAI().setCurrentTask(new TakeItem(next, items));
									} else {
										ClientServerInterface.SendRequest.sendRequestTakeItems(next, items);
									}
								}
							},
							singleIndividualSelected ? Color.WHITE : Colors.UI_DARK_GRAY,
							singleIndividualSelected ? Color.GREEN : Colors.UI_DARK_GRAY,
							singleIndividualSelected ? Color.WHITE : Colors.UI_DARK_GRAY,
							new ContextMenu(screenX, screenY, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
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
		if (input.isButtonPressed(leftClick) && initialLeftMouseDragCoordinates != null) {
			shapeRenderer.begin(Rectangle);
			shapeRenderer.setColor(Color.GREEN);
			float width = getMouseScreenX() - initialLeftMouseDragCoordinates.x;
			float height = getMouseScreenY() - initialLeftMouseDragCoordinates.y;
			shapeRenderer.rect(initialLeftMouseDragCoordinates.x, initialLeftMouseDragCoordinates.y, width, height);
			shapeRenderer.end();
		}

		if (input.isButtonPressed(rightClick) && initialRightMouseDragCoordinates != null && Gdx.input.isKeyPressed(KeyMappings.rightClickDragBox)) {
			shapeRenderer.begin(Rectangle);
			shapeRenderer.setColor(Color.RED);
			float width = getMouseScreenX() - initialRightMouseDragCoordinates.x;
			float height = getMouseScreenY() - initialRightMouseDragCoordinates.y;
			shapeRenderer.rect(initialRightMouseDragCoordinates.x, initialRightMouseDragCoordinates.y, width, height);
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
				} else if (currentTask instanceof Travel) {
					((Travel) currentTask).renderWaypoints();
				} else if (currentTask instanceof CompositeAITask) {
					AITask subTask = ((CompositeAITask) currentTask).getCurrentTask();
					if (subTask instanceof GoToLocation) {
						// ((GoToLocation)subTask).renderPath();
						// ((GoToLocation)subTask).renderFinalWayPoint();
					} else if (subTask instanceof GoToMovingLocation) {
						// ((GoToMovingLocation)subTask).getCurrentGoToLocation().renderPath();
						// ((GoToMovingLocation)subTask).getCurrentGoToLocation().renderFinalWayPoint();
					} else if (subTask instanceof JitGoToLocation) {
						// GoToLocation goToLocation = (GoToLocation)((JitGoToLocation)subTask).getTask();
						// if (goToLocation != null) {
						// 	goToLocation.renderFinalWayPoint();
						// }
					}
				}
			}
			indi.renderUIDecorations();
		}
		spriteBatch.end();
	}


	/** Any text that is rendered on UI */
	private static void renderUIText() {
		defaultFont.setColor(Color.WHITE);
		defaultFont.draw(spriteBatch, "Time: " + getCurrentEpoch().getTimeString(), 5, HEIGHT - 5);
		defaultFont.draw(spriteBatch, "Date: " + getCurrentEpoch().getDateString(), 5, HEIGHT - 25);
		defaultFont.draw(spriteBatch, "Ping: " + ping, 5, HEIGHT - 45);

		fps = (fps + Math.round(1f/Gdx.graphics.getDeltaTime())) / 2;
		fpsTimer++;
		if (fpsTimer >= 30) {
			fpsDisplayed = fps;
			fpsTimer = 0;
		}

		defaultFont.draw(spriteBatch, "Framerate: " + fpsDisplayed, 5, HEIGHT - 65);
	}


	private static void renderFloatingText() {
		spriteBatch.begin();
		Lists.newArrayList(floatingTexts).stream().forEach(text -> {
			defaultFont.setColor(Colors.modulateAlpha(Color.BLACK, text.life / text.maxLife));

			Vector2 renderPos = new Vector2();
			if (text.ui) {
				renderPos = text.worldPosition;
			} else {
				renderPos.x = text.worldPosition.x - cam.position.x + WIDTH/2 - text.text.length() * 5;
				renderPos.y = text.worldPosition.y - cam.position.y + HEIGHT/2;
			}

			defaultFont.draw(
				spriteBatch,
				text.text,
				renderPos.x - 1,
				renderPos.y - 1
			);
			defaultFont.setColor(Colors.modulateAlpha(text.color, text.life / text.maxLife));
			defaultFont.draw(
				spriteBatch,
				text.text,
				renderPos.x,
				renderPos.y
			);
			text.worldPosition.y += 0.5f;
			text.life -= Gdx.graphics.getDeltaTime();

			if (text.life <= 0f) {
				synchronized(floatingTexts) {
					floatingTexts.remove(text);
				}
			}
		});
		spriteBatch.end();
	}

	/** Debug text */
	private static void renderDebugText() {
		defaultFont.setColor(Color.YELLOW);
		defaultFont.draw(spriteBatch, Integer.toString(convertToWorldTileCoord(getMouseWorldX())) + ", " + Float.toString(convertToWorldTileCoord(getMouseWorldY())), getMouseScreenX() - 35, getMouseScreenY() - 35);
		defaultFont.draw(spriteBatch, "Mouse World Coords: " + getMouseWorldX() + ", " + getMouseWorldY(), 5, 72);
		defaultFont.draw(spriteBatch, "Centre of screen Coords: " + Float.toString(cam.position.x) + ", " + Float.toString(cam.position.y) + ", " + Float.toString(cam.zoom), 5, 52);

		int chunksInMemory = 0;
		if (ClientServerInterface.isServer()) {
			for (Entry<Integer, HashMap<Integer, Chunk>> entry : Domain.getActiveWorld().getTopography().getChunkMap().chunkMap.entrySet()) {
				chunksInMemory = chunksInMemory + entry.getValue().size();
			}
		}

		defaultFont.setColor(Color.GREEN);
		defaultFont.draw(spriteBatch, "Number of chunks in memory of active world: " + Integer.toString(chunksInMemory), 5, Gdx.graphics.getHeight() - 105);
		defaultFont.draw(spriteBatch, "Number of tasks queued in AI thread: " + Integer.toString(AIProcessor.aiThreadTasks.size()), 5, Gdx.graphics.getHeight() - 125);
		defaultFont.draw(spriteBatch, "Number of tasks queued in Loader thread: " + Integer.toString(ChunkLoader.loaderTasks.size()), 5, Gdx.graphics.getHeight() - 145);
		defaultFont.draw(spriteBatch, "Number of tasks queued in Saver thread: " + Integer.toString(GameSaver.saverTasks.size()), 5, Gdx.graphics.getHeight() - 165);
		defaultFont.draw(spriteBatch, "Number of tasks queued in Pathfinding thread: " + Integer.toString(AIProcessor.pathFinderTasks.size()), 5, Gdx.graphics.getHeight() - 185);

		defaultFont.setColor(Color.CYAN);
	}


	/** Renders layered components, e.g. {@link Window}s */
	private static synchronized void renderLayeredComponents() {
		ArrayDeque<Component> copy = new ArrayDeque<>(layeredComponents);
		for (Component component : new ArrayDeque<>(layeredComponents)) {
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
		UICameraTrackingCam.position.x = cam.position.x;
		UICameraTrackingCam.position.y = cam.position.y;
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
			if (!menu.isInside(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY())) {
				contextMenuCopy.remove(menu);
			}
		}

		contextMenus.clear();
		contextMenus.addAll(contextMenuCopy);

		if (!clicked) {
			initialLeftMouseDragCoordinates = new Vector2(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());
		} else {
			initialLeftMouseDragCoordinates = null;
		}

		return clicked;
	}


	public static void addFloatingText(String text, Color color, Vector2 position, boolean ui) {
		addFloatingText(floatingText(text, color, position, ui), false);
	}


	public static void addFloatingText(FloatingText floatingText, boolean csi) {
		if (isServer()) {
			if (isClient()) {
				synchronized(floatingTexts) {
					floatingTexts.add(floatingText);
				}
			} else {
				ClientServerInterface.SendNotification.notifyAddFloatingText(floatingText);
			}

			return;
		}

		if (csi) {
			synchronized(floatingTexts) {
				floatingTexts.add(floatingText);
			}
		}
	}


	public static void addUIFloatingText(String text, Color color, Vector2 position) {
		addFloatingText(floatingText(text, color, position, true), false);
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

		if (keyCode == Keys.I) {
			if (Domain.getSelectedIndividuals().size() == 1) {
				Individual individual = Domain.getSelectedIndividuals().iterator().next();
				String simpleName = individual.getId().getSimpleName();

				UserInterface.addLayeredComponentUnique(
					new InventoryWindow(
						individual,
						WIDTH/2 - ((simpleName + " - Inventory").length() * 10 + 50)/2,
						HEIGHT/2 + 150,
						(simpleName + " - Inventory").length() * 10 + 50,
						300,
						simpleName + " - Inventory",
						true,
						150, 300
					)
				);
			}
		}

		if (keyCode == Keys.B) {
			if (Domain.getSelectedIndividuals().size() == 1) {
				Individual individual = Domain.getSelectedIndividuals().iterator().next();

				UserInterface.addLayeredComponentUnique(
					new BuildWindow(
						WIDTH / 2 - 150,
						HEIGHT/2 + 100,
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
		ContextMenu newMenu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(), true);

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

		for (final int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY())) {
			Individual indi = Domain.getIndividual(indiKey);
			if (indi.isMouseOver()) {
				final ContextMenu secondaryMenu = indi.getContextMenu();
				newMenu.getMenuItems().add(
					new MenuItem(
						indi.getId().getSimpleName() + " (" + indi.getClass().getSimpleName() + ")",
						() -> {
							secondaryMenu.x = BloodAndMithrilClient.getMouseScreenX();
							secondaryMenu.y = BloodAndMithrilClient.getMouseScreenY();
						},
						Color.WHITE,
						indi.getToolTipTextColor(),
						indi.getToolTipTextColor(),
						secondaryMenu
					)
				);
			}
		}

		for (final int propKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, BloodAndMithrilClient.getMouseWorldX(), BloodAndMithrilClient.getMouseWorldY())) {
			Prop prop = Domain.getProp(propKey);
			if (prop.isMouseOver()) {
				final ContextMenu secondaryMenu = prop.getContextMenu();
				newMenu.getMenuItems().add(
					new MenuItem(
						prop.getContextMenuItemLabel(),
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						Color.CYAN,
						Color.GREEN,
						Color.GRAY,
						secondaryMenu
					)
				);
			}
		}

		for (final Integer itemId : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Item.class, BloodAndMithrilClient.getMouseWorldX(), BloodAndMithrilClient.getMouseWorldY())) {
			final Item item = Domain.getItem(itemId);
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
						secondaryMenu
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
	public static void addLayeredComponent(Component toAdd) {
		for (Component component : layeredComponents) {
			if (component instanceof Window) {
				((Window)component).setActive(false);
			}
		}
		layeredComponents.addLast(toAdd);
	}


	/** Adds a {@link Component} to {@link #layeredComponents}, checking if an existing one with the same title exists */
	public static void addLayeredComponentUnique(Window toAdd) {
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


	public static void addMessage(String title, String message) {
		addMessage(title, message, -1, new AlwaysTrueFunction());
	}


	public static void addMessage(String title, String message, SerializableFunction<Boolean> function) {
		addMessage(title, message, -1, function);
	}


	public static void addMessage(String title, String message, int client, SerializableFunction<Boolean> function) {
		if (ClientServerInterface.isClient()) {
			addLayeredComponent(
				new MessageWindow(
					message,
					Color.ORANGE,
					WIDTH / 2 - 150,
					HEIGHT / 2 + 75,
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
}
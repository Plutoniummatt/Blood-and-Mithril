package spritestar;

import spritestar.audio.SoundService;
import spritestar.character.Individual;
import spritestar.character.Individual.IndividualIdentifier;
import spritestar.character.Individual.IndividualState;
import spritestar.character.ai.AIProcessor;
import spritestar.character.ai.pathfinding.Path.WayPoint;
import spritestar.character.ai.task.MineTile;
import spritestar.character.individuals.Elf;
import spritestar.character.individuals.Names;
import spritestar.item.consumable.Carrot;
import spritestar.item.consumable.ChickenLeg;
import spritestar.persistence.GameLoader;
import spritestar.persistence.GameSaver;
import spritestar.prop.building.Furnace;
import spritestar.ui.KeyMappings;
import spritestar.ui.UserInterface;
import spritestar.ui.components.Component;
import spritestar.util.Fonts;
import spritestar.util.Shaders;
import spritestar.util.Util;
import spritestar.world.GameWorld;
import spritestar.world.GameWorld.Light;
import spritestar.world.generation.StandardGenerator;
import spritestar.world.weather.Weather;

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
 * @author Matt
 */
public class Fortress implements ApplicationListener, InputProcessor {

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

	/** See {@link #update(float)}, if delta is greater than this value, skip the update frame */
	private static final float lagSpikeTolerance = 0.1f;

	/** The tolerance for double clicking */
	private static final float doubleClickTime = 0.2f;

	/** The current timer for double clicking */
	private float leftDoubleClickTimer = 0f;
	private float rightDoubleClickTimer = 0f;

	@Override
	public void create() {
		loadResources();

		spriteBatch = new SpriteBatch();

		cam = new OrthographicCamera(WIDTH, HEIGHT);
		cam.setToOrtho(false, WIDTH, HEIGHT);

		gameWorld = new GameWorld(StandardGenerator.class);

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
		boolean doubleClick = rightDoubleClickTimer < doubleClickTime;
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
					AIProcessor.sendPathfindingRequest(indi, new WayPoint(new Vector2(getMouseWorldX(), getMouseWorldY())), false, 150f);
				}
			}
		}
	}


	/**
	 * Called upon left clicking
	 */
	private void leftClick(int screenX, int screenY) {
		boolean doubleClick = leftDoubleClickTimer < doubleClickTime;
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
						}
					}
					GameWorld.selectedIndividuals.clear();
				}

			} else {
				for (Individual indi : GameWorld.individuals.values()) {
					if (indi.controllable) {
						indi.deselect(false);
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
					700,
					getMouseWorldX(), getMouseWorldY(),
					Util.randomOneOf(Color.WHITE),
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

		if (keycode == Input.Keys.R) {
			IndividualState state = new IndividualState(10f, 10f);
			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
			id.nickName = "LOL";

			Elf elf = new Elf(
				id, state, true, true,
				new Color(0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 1),
				new Color(0.2f + 0.4f*Util.getRandom().nextFloat(), 0.2f + 0.3f*Util.getRandom().nextFloat(), 0.5f + 0.3f*Util.getRandom().nextFloat(), 1),
				Util.getRandom().nextInt(4),
				20f
			);

			elf.giveItem(new Carrot(), Util.getRandom().nextInt(50));
			elf.giveItem(new ChickenLeg(), Util.getRandom().nextInt(50));

			GameWorld.individuals.put(elf.id.id, elf);
		}

		if (keycode == Input.Keys.T) {
			Individual individual = GameWorld.individuals.get(1);
			if (individual != null) {
				GameWorld.props.add(new Furnace(individual.state.position.x, individual.state.position.y));
			}
		}

		if (keycode == Input.Keys.Y) {
			SoundService.changeMusic(5f, SoundService.music2);
		}

		if (keycode == Input.Keys.U) {
			SoundService.changeMusic(5f, SoundService.music1);
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
		if (!paused && delta < lagSpikeTolerance && !GameSaver.isSaving()) {
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
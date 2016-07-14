package bloodandmithril.graphics;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.weather.WeatherRenderer;

/**
 * Singleton graphics instance
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class Graphics {

	/** 'THE' SpriteBatch */
	private SpriteBatch spriteBatch;

	/** Camera used for the main game world */
	private OrthographicCamera cam;

	/** Resolution */
	private int width, height;

	/** The cam margin is the extra 'space' required for frame buffers so that dynamic tile lighting works */
	private int camMarginX, camMarginY;

	/** Current 'amount' of fade */
	private float fadeAlpha;

	/** Whether the screen is currently fading */
	private boolean fading;

	@Inject private UserInterface ui;
	@Inject private WorldRenderer worldRenderer;

	@Inject
	public Graphics() {
		this.width = ConfigPersistenceService.getConfig().getResX();
		this.height = ConfigPersistenceService.getConfig().getResY();

		camMarginX = 640 + 32 - width % 32;
		camMarginY = 640 + 32 - height % 32;

		cam = new OrthographicCamera(width + camMarginX, height + camMarginY);
		cam.setToOrtho(false, width + camMarginX, height + camMarginY);

		spriteBatch = new SpriteBatch();
	}


	public OrthographicCamera getCam() {
		return cam;
	}


	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}


	public int getCamMarginX() {
		return camMarginX;
	}


	public int getCamMarginY() {
		return camMarginY;
	}


	public int getHeight() {
		return height;
	}


	public int getWidth() {
		return width;
	}


	public static int getGdxHeight() {
		return Gdx.graphics.getHeight();
	}


	public static int getGdxWidth() {
		return Gdx.graphics.getWidth();
	}


	public void setWidth(final int width) {
		this.width = width;
	}


	public void setHeight(final int height) {
		this.height = height;
	}


	public float getFadeAlpha() {
		return fadeAlpha;
	}


	public void setFadeAlpha(final float fadeAlpha) {
		this.fadeAlpha = fadeAlpha;
	}


	public boolean isFading() {
		return fading;
	}


	public void setFading(final boolean fading) {
		this.fading = fading;
	}


	/**
	 * Processes graphics side of resizing the window
	 */
	public void resize(final int newWidth, final int newHeight) {
		final int oldWidth = width;
		final int oldHeight = height;

		width = newWidth;
		height = newHeight;

		camMarginX = 640 + 32 - width % 32;
		camMarginY = 640 + 32 - height % 32;

		final float oldCamX = cam.position.x;
		final float oldCamY = cam.position.y;

		cam.setToOrtho(false, width + camMarginX, height + camMarginY);
		cam.position.x = oldCamX;
		cam.position.y = oldCamY;

		ui.getUICamera().setToOrtho(false, width, height);
		ui.getUITrackingCamera().setToOrtho(false, width, height);

		UserInterface.shapeRenderer.setProjectionMatrix(ui.getUICamera().projection);
		UserInterface.shapeRenderer.setTransformMatrix(ui.getUICamera().view);

		worldRenderer.getShapeRenderer().setProjectionMatrix(cam.projection);
		worldRenderer.getShapeRenderer().setTransformMatrix(cam.view);

		UserInterface.resetWindowPositions(oldWidth, oldHeight);

		worldRenderer.dispose();
		GaussianLightingRenderer.dispose();
		WeatherRenderer.dispose();

		worldRenderer.setup();
		GaussianLightingRenderer.setup();
		WeatherRenderer.setup();

		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}


	/**
	 * @return the {@link UserInterface}
	 */
	public UserInterface getUi() {
		return ui;
	}



	/**
	 * True is specified world coordinates are on screen within specified tolerance
	 */
	public static boolean isOnScreen(final Vector2 position, final float tolerance) {
		final float screenX = worldToScreenX(position.x);
		final float screenY = worldToScreenY(position.y);

		return screenX > -tolerance && screenX < getGdxWidth() + tolerance && screenY > -tolerance && screenY < getGdxHeight() + tolerance;
	}
}
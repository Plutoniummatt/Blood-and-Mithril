package bloodandmithril.graphics;

import bloodandmithril.core.Copyright;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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


	public void setWidth(int width) {
		this.width = width;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	/**
	 * Processes graphics side of resizing the window
	 */
	public void resize(int newWidth, int newHeight) {
		int oldWidth = width;
		int oldHeight = height;

		width = newWidth;
		height = newHeight;

		camMarginX = 640 + 32 - width % 32;
		camMarginY = 640 + 32 - height % 32;

		float oldCamX = cam.position.x;
		float oldCamY = cam.position.y;

		cam.setToOrtho(false, width + camMarginX, height + camMarginY);
		cam.position.x = oldCamX;
		cam.position.y = oldCamY;

		UserInterface.UICamera.setToOrtho(false, width, height);
		UserInterface.UICameraTrackingCam.setToOrtho(false, width, height);

		UserInterface.shapeRenderer.setProjectionMatrix(UserInterface.UICamera.projection);
		UserInterface.shapeRenderer.setTransformMatrix(UserInterface.UICamera.view);

		WorldRenderer.shapeRenderer.setProjectionMatrix(cam.projection);
		WorldRenderer.shapeRenderer.setTransformMatrix(cam.view);

		UserInterface.resetWindowPositions(oldWidth, oldHeight);

		WorldRenderer.dispose();
		GaussianLightingRenderer.dispose();
		Weather.dispose();

		WorldRenderer.setup();
		GaussianLightingRenderer.setup();
		Weather.setup();

		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}
}
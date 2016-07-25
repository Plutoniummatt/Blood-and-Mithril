package bloodandmithril.graphics;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.weather.WeatherRenderer;

/**
 * Handles resizing of the game window
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ResizeWindowService {

	@Inject private Graphics graphics;
	@Inject private UserInterface userInterface;
	@Inject private WorldRenderer worldRenderer;
	@Inject private WeatherRenderer weatherRenderer;
	@Inject private GaussianLightingRenderer gaussianLightingRenderer;

	/**
	 * Processes graphics side of resizing the window
	 */
	public void resize(final int newWidth, final int newHeight) {
		final int oldWidth = graphics.getWidth();
		final int oldHeight = graphics.getHeight();

		graphics.width = newWidth;
		graphics.height = newHeight;

		graphics.camMarginX = 640 + 32 - graphics.width % 32;
		graphics.camMarginY = 640 + 32 - graphics.height % 32;

		final float oldCamX = graphics.cam.position.x;
		final float oldCamY = graphics.cam.position.y;

		graphics.cam.setToOrtho(false, graphics.width + graphics.camMarginX, graphics.height + graphics.camMarginY);
		graphics.cam.position.x = oldCamX;
		graphics.cam.position.y = oldCamY;

		worldRenderer.getShapeRenderer().setProjectionMatrix(graphics.cam.projection);
		worldRenderer.getShapeRenderer().setTransformMatrix(graphics.cam.view);

		worldRenderer.dispose();
		GaussianLightingRenderer.dispose();
		weatherRenderer.dispose();

		worldRenderer.setup();
		gaussianLightingRenderer.setup();
		weatherRenderer.setup();

		graphics.getSpriteBatch().getProjectionMatrix().setToOrtho2D(0, 0, graphics.width, graphics.height);

		userInterface.resetWindowPositions(oldWidth, oldHeight);

		userInterface.getUICamera().setToOrtho(false, graphics.getWidth(), graphics.getHeight());
		userInterface.getUITrackingCamera().setToOrtho(false, graphics.getWidth(), graphics.getHeight());

		userInterface.getShapeRenderer().setProjectionMatrix(userInterface.getUICamera().projection);
		userInterface.getShapeRenderer().setTransformMatrix(userInterface.getUICamera().view);
	}
}
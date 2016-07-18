package bloodandmithril.graphics;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;

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

	/**
	 * Processes graphics side of resizing the window
	 */
	public void resize(final int newWidth, final int newHeight) {
		final int oldWidth = graphics.getWidth();
		final int oldHeight = graphics.getHeight();

		graphics.resize(newWidth, newHeight);

		userInterface.resetWindowPositions(oldWidth, oldHeight);

		userInterface.getUICamera().setToOrtho(false, graphics.getWidth(), graphics.getHeight());
		userInterface.getUITrackingCamera().setToOrtho(false, graphics.getWidth(), graphics.getHeight());

		UserInterface.shapeRenderer.setProjectionMatrix(userInterface.getUICamera().projection);
		UserInterface.shapeRenderer.setTransformMatrix(userInterface.getUICamera().view);
	}
}
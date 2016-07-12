package bloodandmithril.control.keydown;

import static bloodandmithril.control.InputUtilities.isKeyPressed;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.KeyPressedHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.DevWindow;

/**
 * {@link KeyPressedHandler} to spawn a {@link DevWindow}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class DevWindowKeyPressedHandler implements KeyPressedHandler {

	@Inject
	private Graphics graphics;

	@Override
	public boolean handle(final int keycode) {
		if (isKeyPressed(Keys.CONTROL_LEFT) && keycode == Input.Keys.D) {
			UserInterface.addLayeredComponentUnique(
				new DevWindow(
					graphics.getWidth(),
					graphics.getHeight()/2 + 150,
					500,
					300,
					true
				)
			);
		}

		return false;
	}
}
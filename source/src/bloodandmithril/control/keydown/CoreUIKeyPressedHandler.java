package bloodandmithril.control.keydown;

import com.badlogic.gdx.Input.Keys;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.KeyPressedHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.bar.BottomBar;
import bloodandmithril.ui.components.window.MainMenuWindow;

/**
 * Handler for core UI functionality
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CoreUIKeyPressedHandler implements KeyPressedHandler {

	@Inject private UserInterface userInterface;

	@Override
	public boolean handle(final int keycode) {
		for (final Component component : userInterface.getLayeredComponents()) {
			boolean pressed = false;
			if (component.isActive() && !(component instanceof BottomBar)) {
				pressed = component.keyPressed(keycode) || pressed;
			}
			if (pressed) {
				return true;
			}
		}

		if (Keys.ESCAPE == keycode) {
			userInterface.addLayeredComponentUnique(
				new MainMenuWindow(true)
			);
		}

		return false;
	}
}

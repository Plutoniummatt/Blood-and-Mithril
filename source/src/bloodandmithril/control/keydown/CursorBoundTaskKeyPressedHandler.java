package bloodandmithril.control.keydown;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.KeyPressedHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.CursorBoundTask;

/**
 * Handler for {@link CursorBoundTask}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CursorBoundTaskKeyPressedHandler implements KeyPressedHandler {

	@Inject
	private BloodAndMithrilClientInputProcessor inputProcessor;

	@Override
	public boolean handle(final int keycode) {
		if (inputProcessor.getCursorBoundTask() != null) {
			inputProcessor.getCursorBoundTask().keyPressed(keycode);
		}

		return false;
	}
}
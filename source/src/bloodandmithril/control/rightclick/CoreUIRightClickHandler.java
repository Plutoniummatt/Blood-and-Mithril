package bloodandmithril.control.rightclick;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.control.RightClickHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.Window;

/**
 * Handles core UI right clicks
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class CoreUIRightClickHandler implements RightClickHandler {

	@Inject	private GameClientStateTracker gameClientStateTracker;
	@Inject	private GameSaver gameSaver;
	@Inject	private UserInterface ui;

	@Override
	public boolean rightClick(final boolean doubleClick) {
		if (!isKeyPressed(Keys.ANY_KEY)) {
			UserInterface.initialRightMouseDragCoordinates = new Vector2(getMouseScreenX(), getMouseScreenY());
			return rightClick();
		}

		return false;
	}


	private boolean rightClick() {
		boolean clicked = false;

		if (gameClientStateTracker.isPaused() || gameSaver.isSaving()) {
			return false;
		}

		ui.contextMenus.clear();

		final Deque<Component> layeredComponents = ui.getLayeredComponents();
		if (!layeredComponents.isEmpty()) {
			final ArrayDeque<Component> windowsCopy = new ArrayDeque<Component>(layeredComponents);
			final Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				final Component next = iter.next();
				if (next instanceof Window && ((Window)next).rightClick(windowsCopy)) {
					clicked = true;
					break;
				}
			}

			ui.clearLayeredComponents();
			ui.addLayeredComponents(windowsCopy);
		}

		return clicked;
	}
}
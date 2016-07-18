package bloodandmithril.control.leftclick;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.LeftClickHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;

/**
 * Core UI {@link LeftClickHandler}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CoreUILeftClickHandler implements LeftClickHandler {

	@Inject	private GameClientStateTracker gameClientStateTracker;
	@Inject	private UserInterface userInterface;


	@Override
	public boolean leftClick(final boolean doubleClick) {
		final HashMap<String, Button> buttons = userInterface.buttons;
		final List<ContextMenu> contextMenus = userInterface.contextMenus;
		final Deque<Component> layeredComponents = userInterface.getLayeredComponents();

		boolean clicked = false;
		if (gameClientStateTracker.isPaused()) {
			if (userInterface.unpauseButton != null) {
				clicked = userInterface.unpauseButton.click();
			}
			return false;
		}

		for (final Entry<String, Button> buttonEntry : buttons.entrySet()) {
			clicked = buttonEntry.getValue().click() || clicked;
		}

		final List<ContextMenu> contextMenuCopy = new ArrayList<ContextMenu>(contextMenus);

		if (!layeredComponents.isEmpty() && contextMenus.isEmpty()) {
			final ArrayDeque<Component> windowsCopy = new ArrayDeque<Component>(layeredComponents);
			final Iterator<Component> iter = layeredComponents.descendingIterator();
			while (iter.hasNext()) {
				final Component next = iter.next();
				if (next.leftClick(contextMenuCopy, windowsCopy)) {
					clicked = true;
					break;
				}
			}
			if (windowsCopy.size() > layeredComponents.size()) {
				userInterface.clearLayeredComponents();
				userInterface.addLayeredComponents(windowsCopy);
			}
		}

		final Iterator<ContextMenu> iterator = contextMenus.iterator();
		while (iterator.hasNext()) {
			final ContextMenu menu = iterator.next();
			if (!iterator.hasNext()) {
				clicked = menu.leftClick(contextMenuCopy, null) || clicked;
			}
			if (!menu.isInside(getMouseScreenX(), getMouseScreenY())) {
				if (menu.getTop() == null) {
					contextMenuCopy.remove(menu);
				} else {
					if (!menu.getTop().isInside(getMouseScreenX(), getMouseScreenY())) {
						contextMenuCopy.remove(menu);
					}
				}
			}
		}

		contextMenus.clear();
		contextMenus.addAll(contextMenuCopy);

		if (!clicked) {
			UserInterface.initialLeftMouseDragCoordinates = new Vector2(getMouseScreenX(), getMouseScreenY());
		} else {
			UserInterface.initialLeftMouseDragCoordinates = null;
		}

		return clicked;
	}
}
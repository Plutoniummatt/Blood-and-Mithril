package bloodandmithril.control;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.keydown.CoreUIKeyPressedHandler;
import bloodandmithril.control.keydown.CursorBoundTaskKeyPressedHandler;
import bloodandmithril.control.keydown.DevWindowKeyPressedHandler;
import bloodandmithril.control.leftclick.CoreUILeftClickHandler;
import bloodandmithril.control.rightclick.CoreUIRightClickHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;

/**
 * Maintains a list of {@link KeyPressedHandler}s that are currently in use
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class InputHandlers {

	/** All key pressed handlers */
	private final List<KeyPressedHandler> keyPressedHandlers = Lists.newLinkedList();

	/** All left click handlers */
	private final List<LeftClickHandler> leftClickHandlers = Lists.newLinkedList();

	/** All right click handlers */
	private final List<RightClickHandler> rightClickHandlers = Lists.newLinkedList();

	@Inject
	InputHandlers(
		final DevWindowKeyPressedHandler devWindowKeyPressedHandler,
		final CoreUIKeyPressedHandler coreUIKeyPressedHandler,
		final CursorBoundTaskKeyPressedHandler cursorBoundTaskKeyPressedHandler,

		final CoreUILeftClickHandler coreUILeftClickHandler,

		final CoreUIRightClickHandler coreUIRightClickHandler
	) {
		this.keyPressedHandlers.add(devWindowKeyPressedHandler);
		this.keyPressedHandlers.add(coreUIKeyPressedHandler);
		this.keyPressedHandlers.add(cursorBoundTaskKeyPressedHandler);

		this.leftClickHandlers.add(coreUILeftClickHandler);

		this.rightClickHandlers.add(coreUIRightClickHandler);
	}


	public void keyDown(final int keycode) {
		for (final KeyPressedHandler handler : keyPressedHandlers) {
			if (handler.handle(keycode)) {
				break;
			}
		}
	}


	public void leftClick(final boolean doubleClick) {
		for (final LeftClickHandler handler : leftClickHandlers) {
			if (handler.leftClick(doubleClick)) {
				break;
			}
		}
	}


	public void rightClick(final boolean doubleClick) {
		for (final RightClickHandler handler : rightClickHandlers) {
			if (handler.rightClick(doubleClick)) {
				break;
			}
		}
	}


	public void addKeyPressedHandler(final Class<? extends KeyPressedHandler> handler) {
		this.keyPressedHandlers.add(Wiring.injector().getInstance(handler));
	}


	public void addLeftClickHandler(final Class<? extends LeftClickHandler> handler) {
		this.leftClickHandlers.add(Wiring.injector().getInstance(handler));
	}


	public void addRightClickHandler(final Class<? extends RightClickHandler> handler) {
		this.rightClickHandlers.add(Wiring.injector().getInstance(handler));
	}
}
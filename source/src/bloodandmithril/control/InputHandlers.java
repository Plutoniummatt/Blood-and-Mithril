package bloodandmithril.control;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.keydown.CoreUIKeyPressedHandler;
import bloodandmithril.control.keydown.CursorBoundTaskKeyPressedHandler;
import bloodandmithril.control.keydown.DevWindowKeyPressedHandler;
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
	private List<KeyPressedHandler> keyPressedHandlers = Lists.newLinkedList();

	/** All left click handlers */
	private List<LeftClickHandler> leftClickHandlers = Lists.newLinkedList();

	@Inject
	InputHandlers(
		final DevWindowKeyPressedHandler devWindowKeyPressedHandler,
		final CoreUIKeyPressedHandler coreUIKeyPressedHandler,
		final CursorBoundTaskKeyPressedHandler cursorBoundTaskKeyPressedHandler
	) {
		this.keyPressedHandlers.add(devWindowKeyPressedHandler);
		this.keyPressedHandlers.add(coreUIKeyPressedHandler);
		this.keyPressedHandlers.add(cursorBoundTaskKeyPressedHandler);
	}


	public void addKeyPressedHandler(final Class<? extends KeyPressedHandler> handler) {
		this.keyPressedHandlers.add(Wiring.injector().getInstance(handler));
	}


	public void iterateKeyDown(final int keycode) {
		for (final KeyPressedHandler handler : keyPressedHandlers) {
			if (handler.handle(keycode)) {
				break;
			}
		}
	}


	public void addLeftClickHandler(final Class<? extends LeftClickHandler> handler) {
		this.leftClickHandlers.add(Wiring.injector().getInstance(handler));
	}


	public void iterateLeftClick() {
		for (final LeftClickHandler handler : leftClickHandlers) {
			if (handler.leftClick()) {
				break;
			}
		}
	}
}
package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.control.Controls;
import bloodandmithril.control.Controls.MappedKey;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * Window used for changing key binding.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ChangeKeyWindow extends Window {

	@Inject	private Controls controls;
	@Inject	private UserInterface userInterface;

	private MappedKey mappedKey;

	/**
	 * Constructor
	 */
	public ChangeKeyWindow(final MappedKey mappedKey) {
		super(200, 100, "Change key", true, false, false, true);
		this.mappedKey = mappedKey;
	}

	@Override
	public boolean keyPressed(final int keyCode) {
		if (Controls.disallowedKeys.contains(keyCode)) {
			userInterface.addGlobalMessage("Disallowed", "Can not remap this key.");
			setClosing(true);
		} else {
			if (controls.getFunctionalKeyMappings().containsKey(keyCode)) {
				userInterface.addGlobalMessage("Conflict", "Key already mapped to " + controls.getFunctionalKeyMappings().get(keyCode).description);
			} else {
				mappedKey.keyCode = keyCode;
			}
			setClosing(true);
		}

		return super.keyPressed(keyCode);
	}

	@Override
	protected void internalWindowRender(final Graphics graphics) {
		defaultFont.setColor(isActive() ? Colors.modulateAlpha(Color.ORANGE, getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * getAlpha()));
		final String messageToDisplay = Util.fitToWindow("Press Key", width, (height - 75) / 25);
		defaultFont.drawMultiLine(graphics.getSpriteBatch(), messageToDisplay, x + 6, y - 25);

		userInterface.refreshRefreshableWindows();
	}

	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
	}

	@Override
	protected void uponClose() {
	}

	@Override
	public Object getUniqueIdentifier() {
		return "ChangeKey" + mappedKey.keyCode;
	}

	@Override
	public void leftClickReleased() {
	}
}
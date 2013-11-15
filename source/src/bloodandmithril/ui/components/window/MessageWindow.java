package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.Fortress;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Util;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Window} to display the a generic message.
 *
 * @author Matt
 */
public class MessageWindow extends Window {

	/** Message displayed by this window */
	private final String message;
	private final Color messageColor;

	/**
	 * Constructor
	 */
	@Deprecated
	public MessageWindow(String message, Color messageColor, int x, int y, int length, int height, Color borderColor, Color backGroundColor, String title, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, borderColor, backGroundColor, title, active, minLength, minHeight, false);
		this.message = message;
		this.messageColor = messageColor;
	}


	/**
	 * Overloaded constructor - uses default colors
	 */
	public MessageWindow(String message, Color messageColor, int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, title, active, minLength, minHeight, false);
		this.message = message;
		this.messageColor = messageColor;
	}


	@Override
	protected void internalWindowRender() {
		defaultFont.setColor(active ? new Color(messageColor.r, messageColor.g, messageColor.b, alpha) : new Color(messageColor.r, messageColor.g, messageColor.b, 0.6f * alpha));

		String messageToDisplay = Util.fitToWindow(message, length, (height - 75) / 25);

		defaultFont.drawMultiLine(Fortress.spriteBatch, messageToDisplay, x + 6, y - 25);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		//Do nothing
	}


	@Override
	public void leftClickReleased() {
		//Do nothing
	}


	@Override
	protected void uponClose() {
	}
}

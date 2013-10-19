package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.*;

import java.util.Deque;
import java.util.List;


import bloodandmithril.Fortress;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;

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

		String messageToDisplay = "";
		int lineLength = length / 12;
		int lines = message.length() * 12 / length + 1;
		for (int i = 0; i != lines; i++) {
			messageToDisplay = messageToDisplay + message.substring(i * lineLength, (i + 1) * lineLength > message.length() ? message.length() : (i + 1) * lineLength);
			if ((i + 1) * 20 + 110 > height) {
				messageToDisplay = (messageToDisplay + "...").length() * 10 > lineLength ? messageToDisplay + "\n" + "..." : messageToDisplay + "...";
				break;
			} else {
				messageToDisplay = messageToDisplay + "\n";
			}
		}

		defaultFont.drawMultiLine(Fortress.spriteBatch, messageToDisplay, x + 6, y - 25);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		//Do nothing
	}
}

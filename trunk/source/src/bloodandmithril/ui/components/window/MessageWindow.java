package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

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

	/** A clickable button for this window */
	private Button button;

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


	/**
	 * Overloaded constructor - uses default colors, has a button
	 */
	public MessageWindow(String message, Color messageColor, int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Task buttonAction) {
		super(x, y, length, height, title, active, minLength, minHeight, false);
		this.message = message;
		this.messageColor = messageColor;

		this.button = new Button(
			"Confirm",
			Fonts.defaultFont,
			0,
			0,
			70,
			16,
			buttonAction,
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);
	}


	@Override
	protected void internalWindowRender() {
		defaultFont.setColor(active ? Colors.modulateAlpha(messageColor, alpha) : Colors.modulateAlpha(messageColor, 0.6f * alpha));

		String messageToDisplay = Util.fitToWindow(message, width, (height - 75) / 25);

		defaultFont.drawMultiLine(BloodAndMithrilClient.spriteBatch, messageToDisplay, x + 6, y - 25);

		if (button != null) {
			button.render(x + width/2, y - height + 30, active, alpha);
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		if (button != null) {
			button.click();
		}
	}


	@Override
	public void leftClickReleased() {
		//Do nothing
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}

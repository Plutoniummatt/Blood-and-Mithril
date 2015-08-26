package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

/**
 * {@link Window} to display the a generic message.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class MessageWindow extends Window {

	/** Message displayed by this window */
	private final SerializableFunction<String> message;
	private final Color messageColor;

	/** A clickable button for this window */
	private Button button;

	/**
	 * Constructor
	 */
	@Deprecated
	public MessageWindow(String message, Color messageColor, int x, int y, int length, int height, Color borderColor, Color backGroundColor, String title, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, borderColor, backGroundColor, title, active, minLength, minHeight, false, true, true);
		this.message = () -> {
			return message;
		};
		this.messageColor = messageColor;
	}


	/**
	 * Overloaded constructor - uses default colors
	 */
	public MessageWindow(String message, Color messageColor, int length, int height, String title, boolean active, int minLength, int minHeight) {
		super(length, height, title, active, minLength, minHeight, false, true, true);
		this.message = () -> {
			return message;
		};
		this.messageColor = messageColor;
	}
	
	
	/**
	 * Overloaded constructor - uses default colors
	 */
	public MessageWindow(SerializableFunction<String> message, Color messageColor, int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight) {
		super(length, height, title, active, minLength, minHeight, false, true, true);
		this.message = message;
		this.messageColor = messageColor;
	}


	/**
	 * Overloaded constructor - uses default colors and position
	 */
	public MessageWindow(String message, Color messageColor, int length, int height, String title, boolean active) {
		super(length, height, title, active, length, height, false, true, true);
		this.message = () -> {
			return message;
		};
		this.messageColor = messageColor;
	}



	/**
	 * Overloaded constructor - uses default colors, has a button
	 */
	public MessageWindow(String message, Color messageColor, int length, int height, String title, boolean active, int minLength, int minHeight, Task buttonAction) {
		super(length, height, title, active, minLength, minHeight, false, true, true);
		this.message = () -> {
			return message;
		};
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
		defaultFont.setColor(isActive() ? Colors.modulateAlpha(messageColor, getAlpha()) : Colors.modulateAlpha(messageColor, 0.6f * getAlpha()));

		TextBounds bounds = new TextBounds();
		bounds.width = width;
		TextBounds wrappedBounds = defaultFont.getWrappedBounds(message.call(), width - 5);
		int height = (int) wrappedBounds.height + 10;

		if (wrappedBounds.width < 300) {
			width = (int) wrappedBounds.width + 10;
		}

		this.height = height + 32;
		this.minHeight = height + 32;

		defaultFont.drawWrapped(
			spriteBatch,
			message.call(),
			x + 10,
			y - 27,
			width - 5
		);

		if (button != null) {
			button.render(x + width/2, y - height + 30, isActive(), getAlpha());
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
	public Object getUniqueIdentifier() {
		return message;
	}
}

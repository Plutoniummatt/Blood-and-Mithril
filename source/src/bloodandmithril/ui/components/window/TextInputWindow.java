package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.TextInputFieldPanel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.JITTask;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Window} which handles text input
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TextInputWindow extends Window {

	/** The text input panel */
	private final TextInputFieldPanel panel;

	private final Button confirmButton;

	private final boolean closeUponButtonClick;

	private final JITTask task;

	/**
	 * Constructor
	 */
	public TextInputWindow(int length, int height, String title, int minLength, int minHeight, JITTask task, String buttonText, boolean closeUponButtonClick, String defaultText) {
		super(length, height, title, true, minLength, minHeight, false, false, true);
		this.task = task;
		this.closeUponButtonClick = closeUponButtonClick;

		this.confirmButton = new Button(
			buttonText,
			Fonts.defaultFont,
			0,
			0,
			buttonText.length() * 10,
			16,
			task,
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		this.panel = new TextInputFieldPanel(this, defaultText);
	}


	@Override
	protected void internalWindowRender() {
		panel.x = x + 15;
		panel.y = y - 30;
		panel.height = height - 70;
		panel.width = width - 30;

		panel.render();

		confirmButton.render(x + width/2, y - height + 25, isActive(), getAlpha());
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		if (confirmButton.click(panel.getInputText())) {
		  setClosing(closeUponButtonClick || isClosing());
		}
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		if (super.keyPressed(keyCode)) {
			return true;
		}

		if (keyCode == Input.Keys.ENTER) {
			task.execute(panel.getInputText());
			if (closeUponButtonClick) {
				setClosing(true);
			}
		}

		return panel.keyPressed(keyCode);
	}


	@Override
	public Object getUniqueIdentifier() {
		return hashCode();
	}
}
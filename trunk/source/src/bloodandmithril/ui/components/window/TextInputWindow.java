package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;

import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.TextInputFieldPanel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Window} which handles text input
 *
 * @author Matt
 */
public class TextInputWindow extends Window {

	/** The text input panel */
	private final TextInputFieldPanel panel;

	private final Button confirmButton;

	/**
	 * Constructor
	 */
	public TextInputWindow(int x, int y, int length, int height, String title, int minLength, int minHeight, Task task) {
		super(x, y, length, height, title, true, minLength, minHeight, false);

		this.confirmButton = new Button(
			"Confirm",
			Fonts.defaultFont,
			0,
			0,
			70,
			16,
			task,
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		this.panel = new TextInputFieldPanel(this);
	}


	@Override
	protected void internalWindowRender() {
		panel.x = x + 15;
		panel.y = y - 30;
		panel.height = height - 70;
		panel.width = width - 30;

		panel.render();

		confirmButton.render(x + width/2, y - height + 25, active, alpha);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		confirmButton.click();
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return panel.keyPressed(keyCode);
	}
}
package bloodandmithril.ui.components.window;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Deque;
import java.util.List;

import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;

/**
 * {@link Window} with controls relevant to the selected entity
 *
 * @author Matt
 */
public class SelectedEntityControlWindow extends Window {

	List<Button> buttons = newArrayList();

	/**
	 * Constructor
	 */
	public SelectedEntityControlWindow(int x, int y, int length, int height, String title, boolean active, boolean multipleSelected) {
		super(x, y, length, height, title, active, length, height, true, false);
		setupButtons(multipleSelected);
	}


	/**
	 * Sets up the buttons for this {@link SelectedEntityControlWindow}
	 */
	private void setupButtons(boolean multipleSelected) {
		buttons.add(new Button(
			text,
			font,
			offsetX,
			offsetY,
			width,
			height,
			task,
			idle,
			over,
			down,
			ref
		));
	}


	@Override
	protected void internalWindowRender() {
		// TODO Auto-generated method stub
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		// TODO Auto-generated method stub
	}


	@Override
	protected void uponClose() {
		// TODO Auto-generated method stub
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}


	@Override
	public void leftClickReleased() {
	}
}
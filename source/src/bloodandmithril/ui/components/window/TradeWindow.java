package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;

import bloodandmithril.item.Container;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;

/**
 * Trade window, used when transferring items between {@link Container}s
 *
 * @author Matt
 */
public class TradeWindow extends Window {

	/**
	 * Constructor
	 */
	public TradeWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable) {
		super(x, y, length, height, title, active, minLength, minHeight, minimizable);
	}


	@Override
	protected void internalWindowRender() {
		// 2 Panes, either top/bottom split, or left/right split, corresponding to the container being transferred from/to
		// Arrows indicating moving items between containers
		// 2 Further panes indicating which items are currently selected for trading
		// Currency will be treated as an Item, not separately.
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {

	}


	@Override
	public void leftClickReleased() {

	}
}
package bloodandmithril.ui.components;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;


import bloodandmithril.Fortress;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;

import com.badlogic.gdx.graphics.Color;

/**
 * A context menu.
 *
 * @author Matt
 */
public class ContextMenu extends Component {

	private final List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

	private final Color borderColor, backGroundColor;

	public int x, y;

	/** Constructor */
	@Deprecated
	public ContextMenu(int x, int y, Color borderColor, Color backGroundColor, ContextMenuItem... items) {
		this.x = x;
		this.y = y;
		this.borderColor = borderColor;
		this.backGroundColor = backGroundColor;

		for (int i = 0; i < items.length; i++) {
			menuItems.add(items[i]);
		}
	}


	/** Constructor with default colors */
	public ContextMenu(int x, int y, ContextMenuItem... items) {
		this(x, y, Color.GRAY, Color.BLACK, items);
	}


	/** Gets the menu items list */
	public List<ContextMenuItem> getMenuItems() {
		return menuItems;
	}


	/** Adds a {@link ContextMenuItem} to the {@link ContextMenu} */
	public void addMenuItem(ContextMenuItem item) {
		menuItems.add(item);
	}


	/**
	 * Renders this context menu
	 */
	@Override
	protected void internalComponentRender() {

		Fortress.spriteBatch.begin();
		int maxHeight = 20 * (menuItems.size() + 1);
		int maxLength = 0;
		for (ContextMenuItem item : menuItems) {
			maxLength = item.button.getText().length() + 2 > maxLength ? item.button.getText().length() + 2 : maxLength;
		}
		maxLength = maxLength * 10;

		renderRectangle(x + bottomLeft.getRegionWidth(), y + bottomLeft.getRegionHeight(), maxLength, maxHeight, active, backGroundColor);
		renderBox(x, y, maxLength, maxHeight, active, borderColor);

		int i = 0;
		Iterator<ContextMenuItem> iterator = menuItems.iterator();
		while (iterator.hasNext()) {
			ContextMenuItem next = iterator.next();
			next.button.render(x + next.button.width/2 + 5, y - i * 20, active);
			i++;
		}

		Fortress.spriteBatch.end();
	}


	/**
	 * Left click to register.
	 */
	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		for (ContextMenuItem item : menuItems) {
			if (item.button.click() && item.menu == null) {
				copy.clear();
			}
			if (item.menu != null && item.button.isMouseOver()) {
				copy.add(item.menu);
			}
		}
		return isInside(Fortress.getMouseScreenX(), Fortress.getMouseScreenY());
	}


	/**
	 * @return whether or not the location is within the context menu
	 */
	public boolean isInside(int locX, int locY) {
		int height = menuItems.size() + 1;
		int width = 0;
		for (ContextMenuItem item : menuItems) {
			width = item.button.getText().length() > width ? item.button.getText().length() + 2 : width;
		}
		return locX > x && locX < x + width * top.getRegionWidth() && locY < y && locY > y - height * left.getRegionHeight();
	}


	/**
	 * Item that belongs in a {@link ContextMenu}
	 *
	 * @author Matt
	 */
	public static class ContextMenuItem {

		public ContextMenu menu;
		public Button button;


		/** Constructor */
		public ContextMenuItem(Button button, ContextMenu menu) {
			this.button = button;
			this.menu = menu;
		}


		/** Overloaded Constructor */
		public ContextMenuItem(String text, Task task, Color idle, Color over, Color down, ContextMenu menu) {
			this.menu = menu;
			this.button = new Button(
				text,
				Fonts.defaultFont,
				0,
				0,
				text.length() * 10,
				16,
				task,
				idle,
				over,
				down,
				UIRef.BL
			);
		}
	}
}

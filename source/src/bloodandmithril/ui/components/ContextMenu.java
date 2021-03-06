package bloodandmithril.ui.components;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.graphics.Graphics.getGdxWidth;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Function;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;

/**
 * A context menu.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ContextMenu extends Component {

	private final List<MenuItem> menuItems = new ArrayList<MenuItem>();

	private final Color borderColor, backGroundColor;

	public int x, y;

	private final boolean closeUponButtonClick;

	private ContextMenu secondary;

	/** Constructor */
	@Deprecated
	public ContextMenu(int x, int y, boolean closeUponButtonClick, Color borderColor, Color backGroundColor, MenuItem... items) {
		this.x = x;
		this.y = y;
		this.closeUponButtonClick = closeUponButtonClick;
		this.borderColor = borderColor;
		this.backGroundColor = backGroundColor;

		for (int i = 0; i < items.length; i++) {
			menuItems.add(items[i]);
		}
	}


	/** Constructor with default colors */
	public ContextMenu(int x, int y, boolean closeUponButtonClick, MenuItem... items) {
		this(x, y, closeUponButtonClick, Color.GRAY, Color.BLACK, items);
	}


	/** Gets the menu items list */
	public List<MenuItem> getMenuItems() {
		return menuItems;
	}


	/** Adds a {@link MenuItem} to the {@link ContextMenu} */
	public void addMenuItem(MenuItem item) {
		menuItems.add(item);
	}


	/**
	 * Renders this context menu
	 */
	@Override
	protected void internalComponentRender(Graphics graphics) {

		graphics.getSpriteBatch().begin();
		int maxHeight = 20 * (menuItems.size() + 1);
		int maxLength = 0;
		for (MenuItem item : menuItems) {
			maxLength = item.button.getText().length() + 2 > maxLength ? item.button.getText().length() + 2 : maxLength;
		}
		maxLength = maxLength * 10;

		x = x + maxLength >= getGdxWidth() ? getGdxWidth() - maxLength : x;
		y = y - maxHeight <= 0 ? maxHeight : y;

		renderRectangle(
			x + BOTTOM_LEFT.getRegionWidth(),
			y + BOTTOM_LEFT.getRegionHeight(),
			maxLength,
			maxHeight,
			isActive(),
			backGroundColor,
			graphics
		);

		renderBox(
			x,
			y,
			maxLength,
			maxHeight,
			isActive(),
			borderColor,
			graphics
		);

		int i = 0;
		Iterator<MenuItem> iterator = menuItems.iterator();
		while (iterator.hasNext()) {
			MenuItem next = iterator.next();
			next.button.render(x + next.button.width/2 + 5, y - i * 20, isActive(), graphics);
			i++;
		}

		graphics.getSpriteBatch().end();
	}


	/**
	 * Left click to register.
	 */
	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		for (MenuItem item : menuItems) {
			if (item.button.click() && item.menu == null && closeUponButtonClick) {
				copy.clear();
			}
			if (item.menu != null && item.button.isMouseOver()) {
				if (item.secondaryContextMenuCondition == null) {
					ContextMenu newMenu = item.menu.call();
					newMenu.x = getMouseScreenX();
					newMenu.y = getMouseScreenY();
					copy.add(newMenu);
					secondary = newMenu;
				} else {
					if (item.secondaryContextMenuCondition.call()) {
						ContextMenu newMenu = item.menu.call();
						newMenu.x = getMouseScreenX();
						newMenu.y = getMouseScreenY();
						copy.add(newMenu);
						secondary = newMenu;
					} else {
						copy.clear();
					}
				}
			}
		}

		return isInside(getMouseScreenX(), getMouseScreenY());
	}


	/**
	 * @return whether or not the location is within the context menu
	 */
	public boolean isInside(int locX, int locY) {
		int maxHeight = 20 * (menuItems.size() + 1);
		int maxLength = 0;
		for (MenuItem item : menuItems) {
			maxLength = item.button.getText().length() + 2 > maxLength ? item.button.getText().length() + 2 : maxLength;
		}
		maxLength = maxLength * 10;

		return locX > x && locX < x + maxLength && locY < y && locY > y - maxHeight;
	}


	public ContextMenu addFirst(MenuItem item) {
		this.getMenuItems().add(0, item);
		return this;
	}


	/**
	 * Item that belongs in a {@link ContextMenu}
	 *
	 * @author Matt
	 */
	public static class MenuItem {

		public Function<ContextMenu> menu;
		public Button button;
		public Function<Boolean> secondaryContextMenuCondition;


		/** Constructor */
		public MenuItem(Button button, Function<ContextMenu> menu) {
			this.button = button;
			this.menu = menu;
		}


		/** Overloaded Constructor */
		public MenuItem(String text, Task task, Color idle, Color over, Color down, Function<ContextMenu> menu) {
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


		/** Overloaded Constructor */
		public MenuItem(String text, JITTask task, Color idle, Color over, Color down, Function<ContextMenu> menu) {
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


		/** Overloaded Constructor */
		public MenuItem(String text, Task task, Color idle, Color over, Color down, Function<ContextMenu> menu, Function<Boolean> secondaryContextMenuCondition) {
			this.menu = menu;
			this.secondaryContextMenuCondition = secondaryContextMenuCondition;
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


	@Override
	public void leftClickReleased() {
	}


	public ContextMenu getTop() {
		if (secondary == null) {
			return this;
		} else {
			return secondary.getTop();
		}
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}

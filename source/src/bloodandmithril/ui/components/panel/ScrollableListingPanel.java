package bloodandmithril.ui.components.panel;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;

import bloodandmithril.Fortress;
import bloodandmithril.item.Item;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;

/**
 * A panel that displays a listing of clickable menu items
 *
 * @author Matt
 */
public abstract class ScrollableListingPanel extends Panel {

	/** Datastructure that backs this listing panel */
	private final List<HashMap<ListingMenuItem, Integer>> listings = Lists.newArrayList();
	
	/** The current starting index for which the inventory listing is rendered */
	private int startingIndex = 0;
	
	/** The position of the scroll bar button, 0f is top of list, 1f is bottom of list */
	private float scrollBarButtonLocation = 0f;

	/** Used for scroll processing */
	private Float scrollBarButtonLocationOld = null;
	private float mouseLocYFrozen;
	
	/**
	 * Constructor
	 */
	public ScrollableListingPanel(Component parent) {
		super(parent);
		onSetup(listings);
	}
	
	
	/**
	 * Returns a string that will be rendered at an offset from the right edge of the panel, see {@link #getExtraStringOffset()}
	 */
	protected abstract String getExtraString(Entry<ListingMenuItem, Integer> item);
	
	
	/**
	 * Returns the offset value at which {@link #getExtraString(Entry, int)} will be rendered from the right edge of the {@link Panel}
	 */
	protected abstract int getExtraStringOffset();
	
	
	/**
	 * Populates the datastructure {@link #listings}
	 */
	protected abstract void onSetup(List<HashMap<ListingMenuItem, Integer>> listings);
	
	
	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		int size = 0;
		for (Map<ListingMenuItem, Integer> listing : listings) {
			size += listing.size();
		}
		
		for (HashMap<ListingMenuItem, Integer> listing : listings) {
			for(Entry<ListingMenuItem, Integer> item : Lists.newArrayList(listing.entrySet())) {
				if (item.getKey().button.click() && item.getKey().menu == null) {
					copy.clear();
				}
				if (item.getKey().menu != null && item.getKey().button.isMouseOver()) {
					copy.add(item.getKey().menu);
				}
			}
		}

		float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;
		if (isMouseOverScrollButton(scrollBarButtonPos)) {

			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * size);
			scrollBarButtonLocationOld = scrollBarButtonLocation;
			mouseLocYFrozen = Fortress.getMouseScreenY();
		}
		
		return false;
	}

	
	@Override
	public void leftClickReleased() {
		scrollBarButtonLocationOld = null;
	}

	
	@Override
	public void render() {
		// Render the scroll bar
		renderScrollBar();
		
		// Render the scroll bar button
		renderScrollBarButton();
		
		// Render the listings
		renderInventoryItems();		
	}
	
	
	/**
	 * Renders the scroll bar
	 */
	private void renderScrollBar() {
		Window p = ((Window) parent);
		Color scrollBarColor = p.active ? new Color(p.borderColor.r, p.borderColor.g, p.borderColor.b, p.alpha * 0.5f) : new Color(p.borderColor.r, p.borderColor.g, p.borderColor.b, p.borderColor.a * 0.2f * p.alpha);
		Component.shapeRenderer.begin(ShapeType.FilledRectangle);
		Component.shapeRenderer.setColor(scrollBarColor);
		Component.shapeRenderer.filledRect(x + width - 6, y - 50, 3, 30, scrollBarColor, scrollBarColor, Color.CLEAR, Color.CLEAR);
		Component.shapeRenderer.filledRect(x + width - 6, y + 52 - height, 3, height - 102);
		Component.shapeRenderer.filledRect(x + width - 6, y + 22 - height, 3, 30, Color.CLEAR, Color.CLEAR, scrollBarColor, scrollBarColor);
	}
	
	
	/**
	 * Renders the inventory listing
	 */
	private void renderInventoryItems() {
		// Render the equipped items first
		int i = 0;
		for (Map<ListingMenuItem, Integer> listing : listings) {
			
			List<Entry<ListingMenuItem, Integer>> entrySet = Lists.newArrayList(listing.entrySet());
			
			Collections.sort(entrySet, new Comparator<Entry<ListingMenuItem, Integer>>() {
				@Override
				public int compare(Entry<ListingMenuItem, Integer> o1, Entry<ListingMenuItem, Integer> o2) {
					return o1.getKey().item.getClass().getName().compareTo(o2.getKey().item.getClass().getName());
				}
			});
			
			for(Entry<ListingMenuItem, Integer> item : entrySet) {
				if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
					i++;
					continue;
				}
				if (y - (i - (startingIndex == 0 ? 1 : startingIndex)) * 20 - 110 < y - height) {
					defaultFont.draw(Fortress.spriteBatch, "...", x + 6, y - (i - (startingIndex == 0 ? 1 : startingIndex) + 1) * 20 - 33);
					break;
				}
				item.getKey().button.render(x + item.getKey().button.width/2 + 6, y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 25, parent.active && UserInterface.contextMenus.isEmpty(), parent.alpha);
				defaultFont.draw(Fortress.spriteBatch, getExtraString(item), x + width - getExtraStringOffset(), y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 33);
				i++;
			}
		}
		Fortress.spriteBatch.flush();
	}

	
	/**
	 * Renders the scroll bar button
	 */
	private void renderScrollBarButton() {
		int size = 0;
		for (Map<ListingMenuItem, Integer> listing : listings) {
			size += listing.size();
		}
		
		float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;

		if (Gdx.input.isButtonPressed(KeyMappings.leftClick) && scrollBarButtonLocationOld != null) {
			scrollBarButtonLocation = Math.min(1, Math.max(0, scrollBarButtonLocationOld + (mouseLocYFrozen - Fortress.getMouseScreenY())/(height - 102)));
			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * size);
		}

		if (parent.active) {
			if (isMouseOverScrollButton(scrollBarButtonPos) ||
					scrollBarButtonLocationOld != null) {
				Component.shapeRenderer.setColor(0f, 1f, 0f, parent.alpha);
			} else {
				Component.shapeRenderer.setColor(1f, 1f, 1f, parent.alpha);
			}
		} else {
			Component.shapeRenderer.setColor(0.5f, 0.5f, 0.5f, parent.alpha);
		}

		Component.shapeRenderer.filledRect(x + width - 8, scrollBarButtonPos - 7.5f, 7, 15);
		Component.shapeRenderer.end();
	}
	
	
	/**
	 * True if the mouse is over the scroll button
	 */
	private boolean isMouseOverScrollButton(float scrollBarButtonPos) {
		return	Fortress.getMouseScreenX() > x + width - 13 &&
				Fortress.getMouseScreenX() < x + width + 4 &&
				Fortress.getMouseScreenY() > scrollBarButtonPos - 10 &&
				Fortress.getMouseScreenY() < scrollBarButtonPos + 10;
	}
	

	/**
	 * An item to be displayed within this {@link InventoryWindow}
	 *
	 * @author Matt
	 */
	public static class ListingMenuItem extends ContextMenuItem implements Comparable<ListingMenuItem> {

		public Item item;

		/**
		 * Constructor which takes an {@link Item}
		 */
		public ListingMenuItem(Item item, Button button, ContextMenu menu) {
			super(button, menu);
			this.item = item;
		}

		@Override
		public int compareTo(ListingMenuItem o) {
			if (item.value == o.item.value) {
				return item.getClass().getSimpleName().compareTo(o.item.getClass().getSimpleName());
			} else {
				return item.value > o.item.value ? 1 : -1;
			}
		}
	}
}
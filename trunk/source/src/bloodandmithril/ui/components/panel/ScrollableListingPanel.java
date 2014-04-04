package bloodandmithril.ui.components.panel;

import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;

/**
 * A panel that displays a listing of clickable menu items
 *
 * @author Matt
 */
public abstract class ScrollableListingPanel<T extends Comparable<T>, A extends Object> extends Panel {

	/** Datastructure that backs this listing panel */
	private List<HashMap<ListingMenuItem<T>, A>> listings = Lists.newArrayList();

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
	 * Refreshes this {@link Panel}
	 */
	public void refresh(List<HashMap<ListingMenuItem<T>, A>> listings) {
		this.listings = listings;
	}


	/**
	 * Returns a string that will be rendered at an offset from the right edge of the panel, see {@link #getExtraStringOffset()}
	 */
	protected abstract String getExtraString(Entry<ListingMenuItem<T>, A> item);


	/**
	 * Returns the offset value at which {@link #getExtraString(Entry, int)} will be rendered from the right edge of the {@link Panel}
	 */
	protected abstract int getExtraStringOffset();


	/**
	 * Populates the datastructure {@link #listings}
	 */
	protected abstract void onSetup(List<HashMap<ListingMenuItem<T>, A>> listings);


	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		int size = 0;
		for (Map<ListingMenuItem<T>, A> listing : listings) {
			size += listing.size();
		}

		int i = 0;
		ArrayList<HashMap<ListingMenuItem<T>, A>> newArrayList = Lists.newArrayList(listings);
		for (Map<ListingMenuItem<T>, A> listing : newArrayList) {

			List<Entry<ListingMenuItem<T>, A>> entrySet = Lists.newArrayList(listing.entrySet());

			Collections.sort(entrySet, new Comparator<Entry<ListingMenuItem<T>, A>>() {
				@Override
				public int compare(Entry<ListingMenuItem<T>, A> o1, Entry<ListingMenuItem<T>, A> o2) {
					return o1.getKey().t.compareTo(o2.getKey().t);
				}
			});

			for(Entry<ListingMenuItem<T>, A> item : entrySet) {
				if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
					i++;
					continue;
				}
				if (item.getKey().button.click() && item.getKey().menu == null) {
					copy.clear();
				}
				if (item.getKey().menu != null && item.getKey().button.isMouseOver()) {
					copy.add(item.getKey().menu);
				}
				i++;
			}
		}

		float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;
		if (isMouseOverScrollButton(scrollBarButtonPos)) {
			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * size);
			scrollBarButtonLocationOld = scrollBarButtonLocation;
			mouseLocYFrozen = BloodAndMithrilClient.getMouseScreenY();
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
		renderListing();
	}


	/**
	 * Renders the scroll bar
	 */
	private void renderScrollBar() {
		Window p = (Window) parent;
		Color scrollBarColor = p.isActive() ? Colors.modulateAlpha(p.borderColor, 0.5f * p.getAlpha()) : Colors.modulateAlpha(p.borderColor, 0.2f * p.getAlpha());
		Component.shapeRenderer.begin(ShapeType.FilledRectangle);
		Component.shapeRenderer.setColor(scrollBarColor);
		Component.shapeRenderer.filledRect(x + width - 6, y - 50, 3, 30, scrollBarColor, scrollBarColor, Color.CLEAR, Color.CLEAR);
		Component.shapeRenderer.filledRect(x + width - 6, y + 52 - height, 3, height - 102);
		Component.shapeRenderer.filledRect(x + width - 6, y + 22 - height, 3, 30, Color.CLEAR, Color.CLEAR, scrollBarColor, scrollBarColor);
	}


	/**
	 * Renders the listing
	 */
	private void renderListing() {
		// Render the equipped items first
		int i = 0;
		ArrayList<HashMap<ListingMenuItem<T>, A>> newArrayList = Lists.newArrayList(listings);

		for (Map<ListingMenuItem<T>, A> listing : newArrayList) {

			List<Entry<ListingMenuItem<T>, A>> entrySet = newArrayList(listing.entrySet());

			Collections.sort(entrySet, new Comparator<Entry<ListingMenuItem<T>, A>>() {
				@Override
				public int compare(Entry<ListingMenuItem<T>, A> o1, Entry<ListingMenuItem<T>, A> o2) {
					return o1.getKey().t.compareTo(o2.getKey().t);
				}
			});

			for(Entry<ListingMenuItem<T>, A> item : entrySet) {
				if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
					i++;
					continue;
				}
				if (y - (i - (startingIndex == 0 ? 1 : startingIndex)) * 20 - 110 < y - height) {
					defaultFont.draw(BloodAndMithrilClient.spriteBatch, "...", x + 6, y - (i - (startingIndex == 0 ? 1 : startingIndex) + 1) * 20 - 33);
					break;
				}
				item.getKey().button.render(x + item.getKey().button.width/2 + 6, y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 25, parent.isActive() && UserInterface.contextMenus.isEmpty(), parent.getAlpha());
				defaultFont.draw(BloodAndMithrilClient.spriteBatch, getExtraString(item), x + width - getExtraStringOffset(), y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 33);
				i++;
			}
		}
		BloodAndMithrilClient.spriteBatch.flush();
	}


	/**
	 * Renders the scroll bar button
	 */
	private void renderScrollBarButton() {
		int size = 0;
		for (Map<ListingMenuItem<T>, A> listing : listings) {
			size += listing.size();
		}

		float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;

		if (Gdx.input.isButtonPressed(KeyMappings.leftClick) && scrollBarButtonLocationOld != null) {
			scrollBarButtonLocation = Math.min(1, Math.max(0, scrollBarButtonLocationOld + (mouseLocYFrozen - BloodAndMithrilClient.getMouseScreenY())/(height - 102)));
			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * size);
		}

		if (parent.isActive()) {
			if (isMouseOverScrollButton(scrollBarButtonPos) ||
					scrollBarButtonLocationOld != null) {
				Component.shapeRenderer.setColor(0f, 1f, 0f, parent.getAlpha());
			} else {
				Component.shapeRenderer.setColor(1f, 1f, 1f, parent.getAlpha());
			}
		} else {
			Component.shapeRenderer.setColor(0.5f, 0.5f, 0.5f, parent.getAlpha());
		}

		Component.shapeRenderer.filledRect(x + width - 8, scrollBarButtonPos - 7.5f, 7, 15);
		Component.shapeRenderer.end();
	}


	/**
	 * True if the mouse is over the scroll button
	 */
	private boolean isMouseOverScrollButton(float scrollBarButtonPos) {
		return	BloodAndMithrilClient.getMouseScreenX() > x + width - 13 &&
				BloodAndMithrilClient.getMouseScreenX() < x + width + 4 &&
				BloodAndMithrilClient.getMouseScreenY() > scrollBarButtonPos - 10 &&
				BloodAndMithrilClient.getMouseScreenY() < scrollBarButtonPos + 10;
	}


	/**
	 * An item to be displayed within this {@link InventoryWindow}
	 *
	 * @author Matt
	 */
	public static class ListingMenuItem<T extends Comparable<T>> extends ContextMenuItem implements Comparable<ListingMenuItem<T>> {

		public T t;

		/**
		 * Constructor
		 */
		public ListingMenuItem(T t, Button button, ContextMenu menu) {
			super(button, menu);
			this.t = t;
		}

		@Override
		public int compareTo(ListingMenuItem<T> o) {
			return t.compareTo(o.t);
		}
	}


	public List<HashMap<ListingMenuItem<T>, A>> getListing() {
		return listings;
	}
}
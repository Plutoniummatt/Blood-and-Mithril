package bloodandmithril.ui.components.panel;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A panel that displays a listing of clickable menu items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class ScrollableListingPanel<T, A> extends Panel {

	/** Datastructure that backs this listing panel */
	private List<HashMap<ListingMenuItem<T>, A>> listings = Lists.newArrayList();

	/** The current starting index for which the inventory listing is rendered */
	private int startingIndex = 0;

	/** The position of the scroll bar button, 0f is top of list, 1f is bottom of list */
	private float scrollBarButtonLocation = 0f;

	/** Used for scroll processing */
	private Float scrollBarButtonLocationOld = null;
	private float mouseLocYFrozen;
	private boolean scrollWheelActive;

	/** Used to sort this listing panel */
	private Comparator<T> sortingComparator;

	/** Filters used to filter this listing panel */
	private final Collection<Predicate<T>> filters = Lists.newLinkedList();

	/**
	 * Constructor
	 */
	public ScrollableListingPanel(Component parent, Comparator<T> sortingComparator) {
		super(parent);
		this.sortingComparator = sortingComparator;
		onSetup(listings);
	}


	/**
	 * Refreshes this {@link Panel}
	 */
	public void refresh(List<HashMap<ListingMenuItem<T>, A>> listings) {
		this.listings = listings;

		if (filters.isEmpty()) {
			return;
		}
		
		for (HashMap<ListingMenuItem<T>, A> item : Lists.newArrayList(this.listings)) {
			for (ListingMenuItem<T> t : Sets.newHashSet(item.keySet())) {
				boolean keep = false;
				for (Predicate<T> predicate : filters) {
					if (predicate.apply(t.t)) {
						keep = true;
						break;
					}
				}

				if (!keep) {
					item.remove(t);
				}
			}
		}
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
					return sortingComparator.compare(o1.getKey().t, o2.getKey().t);
				}
			});

			boolean clicked = false;
			for(Entry<ListingMenuItem<T>, A> item : entrySet) {
				if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
					i++;
					continue;
				}
				if (item.getKey().button.click() && item.getKey().menu == null) {
					copy.clear();
					clicked = true;
				}
				if (item.getKey().menu != null && item.getKey().button.isMouseOver()) {
					item.getKey().menu.x = getMouseScreenX();
					item.getKey().menu.y = getMouseScreenY();
					copy.add(item.getKey().menu);
				}
				if (clicked) {
					break;
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
	public boolean scrolled(int amount) {
		scrollWheelActive = isMouseWithin();
		if (scrollWheelActive) {
			startingIndex += amount;

			int size = 0;
			for (Map<ListingMenuItem<T>, A> listing : listings) {
				size += listing.size();
			}

			if (size == 0) {
				return false;
			}

			if (startingIndex < 0) {
				startingIndex = 0;
			}

			if (startingIndex > size) {
				startingIndex = size;
			}

			scrollBarButtonLocation = (float)startingIndex / (float)size;

			return true;
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
		if (listings.isEmpty()) {
			return;
		}

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
		if (listings.isEmpty()) {
			return;
		}

		// Render the equipped items first
		int i = 0;
		ArrayList<HashMap<ListingMenuItem<T>, A>> newArrayList = Lists.newArrayList(listings);

		for (Map<ListingMenuItem<T>, A> listing : newArrayList) {

			List<Entry<ListingMenuItem<T>, A>> entrySet = newArrayList(listing.entrySet());

			Collections.sort(entrySet, new Comparator<Entry<ListingMenuItem<T>, A>>() {
				@Override
				public int compare(Entry<ListingMenuItem<T>, A> o1, Entry<ListingMenuItem<T>, A> o2) {
					return sortingComparator.compare(o1.getKey().t, o2.getKey().t);
				}
			});

			for(Entry<ListingMenuItem<T>, A> item : entrySet) {
				if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
					i++;
					continue;
				}
				if (y - (i - (startingIndex == 0 ? 1 : startingIndex)) * 20 - 110 < y - height) {
					defaultFont.draw(spriteBatch, "...", x + 6, y - (i - (startingIndex == 0 ? 1 : startingIndex) + 1) * 20 - 33);
					break;
				}

				item.getKey().button.render(
					x + item.getKey().button.width/2 + 6,
					y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 25,
					parent.isActive() && UserInterface.contextMenus.isEmpty(), parent.getAlpha(),
					width - 35
				);

				defaultFont.draw(spriteBatch, getExtraString(item), x + width - getExtraStringOffset(), y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 33);
				i++;
			}
		}

		spriteBatch.flush();
	}


	/**
	 * Renders the scroll bar button
	 */
	private void renderScrollBarButton() {
		if (listings.isEmpty()) {
			return;
		}

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
	public static class ListingMenuItem<T> extends MenuItem {

		public T t;

		/**
		 * Constructor
		 */
		public ListingMenuItem(T t, Button button, ContextMenu menu) {
			super(button, menu);
			this.t = t;
		}
	}


	public List<HashMap<ListingMenuItem<T>, A>> getListing() {
		return listings;
	}


	public void setScrollWheelActive(boolean active) {
		this.scrollWheelActive = active;
	}


	public Collection<Predicate<T>> getFilters() {
		return filters;
	}
}
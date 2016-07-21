package bloodandmithril.ui.components.panel;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isButtonPressed;
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Function;
import bloodandmithril.util.Util.Colors;

/**
 * A panel that displays a listing of clickable menu items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class ScrollableListingPanel<T, A> extends Panel {

	@Inject	private Controls controls;
	@Inject	private UserInterface userInterface;

	/** Datastructure that backs this listing panel */
	private List<HashMap<ListingMenuItem<T>, A>> listings = Lists.newArrayList();

	/** The current starting index for which the inventory listing is rendered */
	private int startingIndex = 0;

	/** The position of the scroll bar button, 0f is top of list, 1f is bottom of list */
	private float scrollBarButtonLocation = 0f;

	/** Used for scroll processing */
	private Float scrollBarButtonLocationOld = null;
	private float mouseLocYFrozen;
	private boolean scrollWheelActive, canScroll = true;

	/** Used to sort this listing panel */
	private Comparator<T> sortingComparator;

	/** Filters used to filter this listing panel */
	private final Collection<Predicate<T>> filters = Lists.newLinkedList();
	private final Predicate<T> textSearch;
	private final boolean filtered;
	private final int extraColumnWidth;

	/**
	 * Constructor
	 */
	public ScrollableListingPanel(final Component parent, final Comparator<T> sortingComparator, final boolean filtered, final int extraColumnWidth, final Predicate<T> textSearch) {
		super(parent);
		this.sortingComparator = sortingComparator;
		this.filtered = filtered;
		this.extraColumnWidth = extraColumnWidth;
		this.textSearch = textSearch;
		populateListings(listings);
	}


	public ScrollableListingPanel<T, A> canScroll(final boolean canScroll) {
		this.canScroll = canScroll;
		return this;
	}


	/**
	 * Refreshes this {@link Panel}
	 */
	public void refresh(final List<HashMap<ListingMenuItem<T>, A>> listings) {
		this.listings = listings;

		if (!filtered) {
			return;
		}

		for (final HashMap<ListingMenuItem<T>, A> item : Lists.newArrayList(this.listings)) {
			for (final ListingMenuItem<T> t : Sets.newHashSet(item.keySet())) {
				boolean keep = false;
				for (final Predicate<T> predicate : filters) {
					if (predicate.apply(t.t)) {
						if (textSearch != null) {
							keep = textSearch.apply(t.t);
						} else {
							keep = true;
						}
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
	protected abstract void populateListings(List<HashMap<ListingMenuItem<T>, A>> listings);


	@Override
	public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		int size = 0;
		for (final Map<ListingMenuItem<T>, A> listing : listings) {
			size += listing.size();
		}

		int i = 0;
		final ArrayList<HashMap<ListingMenuItem<T>, A>> newArrayList = Lists.newArrayList(listings);
		for (final Map<ListingMenuItem<T>, A> listing : newArrayList) {

			final List<Entry<ListingMenuItem<T>, A>> entrySet = Lists.newArrayList(listing.entrySet());

			Collections.sort(entrySet, new Comparator<Entry<ListingMenuItem<T>, A>>() {
				@Override
				public int compare(final Entry<ListingMenuItem<T>, A> o1, final Entry<ListingMenuItem<T>, A> o2) {
					return sortingComparator.compare(o1.getKey().t, o2.getKey().t);
				}
			});

			boolean clicked = false;
			for(final Entry<ListingMenuItem<T>, A> item : entrySet) {
				if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
					i++;
					continue;
				}
				if (item.getKey().button.click() && item.getKey().menu == null) {
					copy.clear();
					clicked = true;
				}
				if (item.getKey().menu != null && item.getKey().button.isMouseOver()) {
					final ContextMenu newMenu = item.getKey().menu.call();
					newMenu.x = getMouseScreenX();
					newMenu.y = getMouseScreenY();
					copy.add(newMenu);
				}
				if (clicked) {
					break;
				}
				i++;
			}
		}

		final float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;
		if (isMouseOverScrollButton(scrollBarButtonPos)) {
			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * size);
			scrollBarButtonLocationOld = scrollBarButtonLocation;
			mouseLocYFrozen = getMouseScreenY();
		}

		return false;
	}


	@Override
	public boolean scrolled(final int amount) {
		scrollWheelActive = isMouseWithin();
		if (scrollWheelActive && canScroll) {
			final float max = Math.round((height - 100f) / 20f);
			int size = 0;
			for (final Map<ListingMenuItem<T>, A> listing : listings) {
				size += listing.size();
			}

			if (size == 0 || size < max) {
				return false;
			}

			if (startingIndex >= size - max) {
				startingIndex = Math.round(size - max);
				if (amount > 0) {
					return false;
				}
			}

			startingIndex += amount;

			if (startingIndex < 0) {
				startingIndex = 0;
			}

			if (startingIndex > size) {
				startingIndex = size;
			}

			scrollBarButtonLocation = Math.max(0.0f, Math.min(1.0f, startingIndex / (size - max)));

			return true;
		}
		return false;
	}


	@Override
	public void leftClickReleased() {
		scrollBarButtonLocationOld = null;
	}


	@Override
	public void render(final Graphics graphics) {
		if (canScroll) {
			// Render the scroll bar
			renderScrollBar();

			// Render the scroll bar button
			renderScrollBarButton();
		}

		// Render the listings
		renderListing(graphics);
	}


	/**
	 * Renders the scroll bar
	 */
	private void renderScrollBar() {
		if (listings.isEmpty()) {
			return;
		}

		final Window p = (Window) parent;
		final Color scrollBarColor = p.isActive() ? Colors.modulateAlpha(p.borderColor, 0.5f * p.getAlpha()) : Colors.modulateAlpha(p.borderColor, 0.2f * p.getAlpha());
		Component.shapeRenderer.begin(ShapeType.Filled);
		Component.shapeRenderer.setColor(scrollBarColor);
		Component.shapeRenderer.rect(x + width - 6, y - 50, 3, 30, scrollBarColor, scrollBarColor, Color.CLEAR, Color.CLEAR);
		Component.shapeRenderer.rect(x + width - 6, y + 52 - height, 3, height - 102);
		Component.shapeRenderer.rect(x + width - 6, y + 22 - height, 3, 30, Color.CLEAR, Color.CLEAR, scrollBarColor, scrollBarColor);
	}


	/**
	 * Renders the listing
	 */
	private void renderListing(final Graphics graphics) {
		if (listings.isEmpty()) {
			return;
		}

		int size = 0;
		final float max = Math.round((height - 100f) / 20f);
		for (final Map<ListingMenuItem<T>, A> listing : listings) {
			size += listing.size();
		}

		if (size < max) {
			startingIndex = 0;
		} else if (startingIndex >= size - max + 1) {
			startingIndex = Math.round(size - max);
		}


		// Render the equipped items first
		int i = 0;
		final ArrayList<HashMap<ListingMenuItem<T>, A>> newArrayList = Lists.newArrayList(listings);

		for (final Map<ListingMenuItem<T>, A> listing : newArrayList) {

			final List<Entry<ListingMenuItem<T>, A>> entrySet = newArrayList(listing.entrySet());

			Collections.sort(entrySet, new Comparator<Entry<ListingMenuItem<T>, A>>() {
				@Override
				public int compare(final Entry<ListingMenuItem<T>, A> o1, final Entry<ListingMenuItem<T>, A> o2) {
					return sortingComparator.compare(o1.getKey().t, o2.getKey().t);
				}
			});

			for(final Entry<ListingMenuItem<T>, A> item : entrySet) {
				if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
					i++;
					continue;
				}
				if (y - (i - (startingIndex == 0 ? 1 : startingIndex)) * 20 - 110 < y - height) {
					defaultFont.draw(graphics.getSpriteBatch(), "...", x + 6, y - (i - (startingIndex == 0 ? 1 : startingIndex) + 1) * 20 - 33);
					break;
				}

				item.getKey().button.render(
					x + item.getKey().button.width/2 + 6,
					y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 25,
					parent.isActive() && userInterface.getContextMenus().isEmpty(), parent.getAlpha(),
					width - extraColumnWidth,
					graphics
				);

				defaultFont.draw(graphics.getSpriteBatch(), getExtraString(item), x + width - getExtraStringOffset(), y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 33);
				i++;
			}
		}

		graphics.getSpriteBatch().flush();
	}


	/**
	 * Renders the scroll bar button
	 */
	private void renderScrollBarButton() {
		if (listings.isEmpty()) {
			return;
		}

		int size = 0;
		for (final Map<ListingMenuItem<T>, A> listing : listings) {
			size += listing.size();
		}

		final float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;

		final float max = (height - 100f) / 20f;
		if (isButtonPressed(controls.leftClick.keyCode) && scrollBarButtonLocationOld != null && size > max) {
			scrollBarButtonLocation = Math.min(1, Math.max(0, scrollBarButtonLocationOld + (mouseLocYFrozen - getMouseScreenY())/(height - 102)));
			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * (size - max));
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

		Component.shapeRenderer.rect(x + width - 8, scrollBarButtonPos - 7.5f, 7, 15);
		Component.shapeRenderer.end();
	}


	/**
	 * True if the mouse is over the scroll button
	 */
	private boolean isMouseOverScrollButton(final float scrollBarButtonPos) {
		return	getMouseScreenX() > x + width - 13 &&
				getMouseScreenX() < x + width + 4 &&
				getMouseScreenY() > scrollBarButtonPos - 10 &&
				getMouseScreenY() < scrollBarButtonPos + 10;
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
		public ListingMenuItem(final T t, final Button button, final Function<ContextMenu> menu) {
			super(button, menu);
			this.t = t;
		}
	}


	public List<HashMap<ListingMenuItem<T>, A>> getListing() {
		return listings;
	}


	public void setScrollWheelActive(final boolean active) {
		this.scrollWheelActive = active;
	}


	public Collection<Predicate<T>> getFilters() {
		return filters;
	}
}
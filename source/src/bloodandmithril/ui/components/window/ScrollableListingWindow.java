package bloodandmithril.ui.components.window;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;

/**
 * A window that simply displays a {@link ScrollableListingPanel}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class ScrollableListingWindow<T, A> extends Window implements Refreshable {

	/**
	 * The {@link ScrollableListingPanel} of this {@link ScrollableListingWindow}.
	 */
	private ScrollableListingPanel<T, A> listing;
	private Function<T, String> displayFunction;

	/**
	 * Constructor
	 */
	public ScrollableListingWindow(int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable, boolean resizeable, Map<T, A> map, Function<T, String> displayFunction, Comparator<T> sortingOrder) {
		super(length, height, title, active, minLength, minHeight, minimizable, resizeable, true);
		this.displayFunction = displayFunction;
		buildListing(map, sortingOrder);

		listing.setScrollWheelActive(true);
	}


	/**
	 * Builds the {@link ScrollableListingPanel} object
	 */
	protected void buildListing(final Map<T, A> mapToBuildFrom, final Comparator<T> sortingOrder) {
		this.listing = new ScrollableListingPanel<T, A>(this, sortingOrder, false, 35, null) {

			@Override
			protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<T>, A> item) {
				return item.getValue().toString();
			}


			@Override
			protected int getExtraStringOffset() {
				return 80;
			}


			@Override
			protected void populateListings(List<HashMap<ScrollableListingPanel.ListingMenuItem<T>, A>> listings) {
				populateListing(mapToBuildFrom, listings);
			}


			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalWindowRender(Graphics graphics) {
		listing.x = x;
		listing.y = y;
		listing.width = width;
		listing.height = height;

		listing.render(graphics);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		listing.leftClick(copy, windowsCopy);
	}


	@Override
	public boolean scrolled(int amount) {
		return listing.scrolled(amount);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public void leftClickReleased() {
		listing.leftClickReleased();
	}


	private void populateListing(final Map<T, A> mapToBuildFrom, List<HashMap<ScrollableListingPanel.ListingMenuItem<T>, A>> listings) {
		HashMap<ScrollableListingPanel.ListingMenuItem<T>, A> map = newHashMap();

		for (Entry<T, A> tEntry : mapToBuildFrom.entrySet()) {
			map.put(
				new ListingMenuItem<T>(
					tEntry.getKey(),
					new Button(
						displayFunction.apply(tEntry.getKey()),
						Fonts.defaultFont,
						0,
						0,
						tEntry.getKey().toString().length() * 10,
						16,
						buttonTask(tEntry),
						Color.ORANGE,
						Color.GREEN,
						Color.WHITE,
						UIRef.BL
					),
					() -> { return buttonContextMenu(tEntry);}
				),
				tEntry.getValue()
			);
		}

		listings.add(map);
	}


	protected ContextMenu buttonContextMenu(Entry<T, A> tEntry) {
		return null;
	}


	protected Task buttonTask(Entry<T, A> listingItem) {
		return () -> {};
	}


	protected ScrollableListingPanel<T, A> getListing() {
		return listing;
	}
}
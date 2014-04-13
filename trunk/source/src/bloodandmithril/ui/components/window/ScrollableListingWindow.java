package bloodandmithril.ui.components.window;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;

/**
 * A window that simply displays a {@link ScrollableListingPanel}
 *
 * @author Matt
 */
public abstract class ScrollableListingWindow<T extends Comparable<T>, A extends Object> extends Window implements Refreshable {

	/**
	 * The {@link ScrollableListingPanel} of this {@link ScrollableListingWindow}.
	 */
	private ScrollableListingPanel<T, A> listing;
	private Function<T, String> displayFunction;

	/**
	 * Constructor
	 */
	public ScrollableListingWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable, boolean resizeable, Map<T, A> map, Function<T, String> displayFunction) {
		super(x, y, length, height, title, active, minLength, minHeight, minimizable, resizeable);
		this.displayFunction = displayFunction;
		buildListing(map);
	}


	/**
	 * Builds the {@link ScrollableListingPanel} object
	 */
	protected void buildListing(final Map<T, A> mapToBuildFrom) {
		this.listing = new ScrollableListingPanel<T, A>(this) {

			@Override
			protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<T>, A> item) {
				return item.getValue().toString();
			}


			@Override
			protected int getExtraStringOffset() {
				return 80;
			}


			@Override
			protected void onSetup(List<HashMap<ScrollableListingPanel.ListingMenuItem<T>, A>> listings) {
				populateListing(mapToBuildFrom, listings);
			}


			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalWindowRender() {
		listing.x = x;
		listing.y = y;
		listing.width = width;
		listing.height = height;

		listing.render();
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
	public boolean keyPressed(int keyCode) {
		return false;
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
					null
				),
				tEntry.getValue()
			);
		}

		listings.add(map);
	}


	protected Task buttonTask(Entry<T, A> listingItem) {
		return () -> {};
	}
}
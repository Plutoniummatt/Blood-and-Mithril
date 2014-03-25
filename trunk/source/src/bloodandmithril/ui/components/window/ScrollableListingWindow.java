package bloodandmithril.ui.components.window;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util.Colors;

/**
 * A window that simply displays a {@link ScrollableListingPanel}
 *
 * @author Matt
 */
public class ScrollableListingWindow<T extends Comparable<T>> extends Window {

	/**
	 * The {@link ScrollableListingPanel} of this {@link ScrollableListingWindow}.
	 */
	private ScrollableListingPanel<T> listing;
	
	/**
	 * Constructor
	 */
	public ScrollableListingWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable, boolean resizeable, Map<T, Integer> map) {
		super(x, y, length, height, title, active, minLength, minHeight, minimizable, resizeable);
		buildListing(map);
	}


	/**
	 * Builds the {@link ScrollableListingPanel} object
	 */
	private void buildListing(final Map<T, Integer> mapToBuildFrom) {
		this.listing = new ScrollableListingPanel<T>(this) {

			@Override
			protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<T>, Integer> item) {
				return item.getValue().toString();
			}

			
			@Override
			protected int getExtraStringOffset() {
				return 80;
			}
			

			@Override
			protected void onSetup(List<HashMap<ScrollableListingPanel.ListingMenuItem<T>, Integer>> listings) {
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
	
	
	private void populateListing(final Map<T, Integer> mapToBuildFrom, List<HashMap<ScrollableListingPanel.ListingMenuItem<T>, Integer>> listings) {
		HashMap<ScrollableListingPanel.ListingMenuItem<T>, Integer> map = newHashMap();
		
		for (Entry<T, Integer> tEntry : mapToBuildFrom.entrySet()) {
			map.put(
				new ListingMenuItem<T>(
					tEntry.getKey(), 
					new Button(
						"", 
						Fonts.defaultFont, 
						0, 
						0, 
						tEntry.getKey().toString().length() * 10, 
						16, 
						new Task() {
							@Override
							public void execute() {
							}
						}, 
						Colors.DARK_RED,
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
}
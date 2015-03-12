package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.control.Controls.MappedKey;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Maps;

/**
 * Window to remap controls
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class KeyMappingsWindow extends Window {

	ScrollableListingPanel<MappedKey, String> keyMappings;

	private static Comparator<MappedKey> stringComparator = new Comparator<MappedKey>() {
		@Override
		public int compare(MappedKey o1, MappedKey o2) {
			return o1.description.compareTo(o2.description);
		}
	};

	/**
	 * Constructor
	 */
	public KeyMappingsWindow() {
		super(500, 400, "Controls", true, false, true, true);

		keyMappings = new ScrollableListingPanel<MappedKey, String>(this, stringComparator, false, 100) {

			@Override
			protected String getExtraString(Entry<ListingMenuItem<MappedKey>, String> item) {
				return item.getValue();
			}

			@Override
			protected int getExtraStringOffset() {
				return 90;
			}

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<MappedKey>, String>> listings) {
				populateKeyMappingsPairs(listings);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalWindowRender() {
		keyMappings.x = x;
		keyMappings.y = y;
		keyMappings.width = width;
		keyMappings.height = height;

		keyMappings.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		keyMappings.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return KeyMappingsWindow.class;
	}


	@Override
	public void leftClickReleased() {
		keyMappings.leftClickReleased();
	}


	@Override
	public boolean scrolled(int amount) {
		return keyMappings.scrolled(amount);
	}


	private void populateKeyMappingsPairs(List<HashMap<ListingMenuItem<MappedKey>, String>> listings) {
		HashMap<ListingMenuItem<MappedKey>, String> map = Maps.newHashMap();

		for (MappedKey entry : BloodAndMithrilClient.getKeyMappings().getFunctionalKeyMappings()) {
			map.put(
				new ListingMenuItem<MappedKey>(
					entry,
					new Button(
						entry.description,
						defaultFont,
						0,
						0,
						entry.description.length() * 10,
						16,
						() -> {
						},
						Color.ORANGE,
						Color.GREEN,
						Color.WHITE,
						UIRef.BL
					),
					null
				),
				Integer.toString(entry.keyCode)
			);
		}

		listings.add(map);
	}
}
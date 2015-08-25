package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Window for examining the contents of a container, zero interactions
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ContainerInspectionWindow extends Window implements Refreshable {

	private final Container container;
	private ScrollableListingPanel<Item, Integer> items;

	/**
	 * Constructor
	 */
	public ContainerInspectionWindow(
		Container container,
		String title
	) {
		super(
			600,
			400,
			title,
			true,
			600,
			400,
			true,
			true,
			true
		);
		this.container = container;

		items = new ScrollableListingPanel<Item, Integer>(this, InventoryWindow.inventorySortingOrder, true, 35) {
			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				HashMap<ListingMenuItem<Item>, Integer> newHashMap = buildMap(container);

				listings.add(newHashMap);
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}

			@Override
			protected String getExtraString(Entry<ListingMenuItem<Item>, Integer> item) {
				return Integer.toString(item.getValue());
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	private HashMap<ListingMenuItem<Item>, Integer> buildMap(Container container) {
		HashMap<ListingMenuItem<Item>, Integer> newHashMap = newHashMap();

		for (Entry<Item, Integer> entry : container.getInventory().entrySet()) {
			MenuItem showInfo = new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponentUnique(entry.getKey().getInfoWindow());
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);

			newHashMap.put(
				new ListingMenuItem<Item>(
					entry.getKey(),
					new Button(
						entry.getKey().getSingular(true),
						defaultFont,
						0,
						0,
						entry.getKey().getSingular(true).length() * 10,
						16,
						() -> {
						},
						Color.ORANGE,
						Color.WHITE,
						Color.GREEN,
						UIRef.BL
					),
					new ContextMenu(
						getMouseScreenX(),
						getMouseScreenY(),
						true,
						showInfo
					)
				),
				entry.getValue()
			);
		}
		return newHashMap;
	}


	@Override
	protected void internalWindowRender() {
		items.x = x;
		items.y = y;
		items.width = width;
		items.height = height;

		items.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		items.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass().getSimpleName() + container.hashCode();
	}


	@Override
	public void leftClickReleased() {
		items.leftClickReleased();
	}


	@Override
	public void refresh() {
		items.getListing().clear();
		items.getListing().add(buildMap(container));
	}
}
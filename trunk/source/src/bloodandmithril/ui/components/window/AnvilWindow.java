package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.equipment.Smithable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.furniture.Anvil;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Anvil} related UI
 *
 * @author Matt
 */
public class AnvilWindow extends Window {

	/** The anvil related to this {@link AnvilWindow} */
	private Anvil anvil;

	private HashMap<ListingMenuItem<String>, String> availableToCraft;

	private ScrollableListingPanel<String, String> listing;

	private Individual smith;

	private static Set<Smithable> ordinaryItems = newHashSet();

	static {
		ordinaryItems.add(new ButterflySword(0));
		ordinaryItems.add(new Broadsword(0));
	}

	/**
	 * Constructor
	 */
	public AnvilWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Individual smith, Anvil anvil) {
		super(x, y, length, height, title, active, minLength, minHeight, true, true);
		this.smith = smith;

		this.anvil = anvil;

		this.availableToCraft = constructSmithingMenu();

		this.listing = new ScrollableListingPanel<String, String>(this) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<String>, String> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<String>, String>> listings) {
				listings.add(availableToCraft);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	/**
	 * Constructs a list of items able to be smith'd.
	 */
	private HashMap<ListingMenuItem<String>, String> constructSmithingMenu() {
		HashMap<ListingMenuItem<String>, String> items = newHashMap();

		for (Smithable item : ordinaryItems) {
			final ContextMenu secondaryMenu = new ContextMenu(0, 0);

			if (item.canBeSmithedBy(smith)) {
				secondaryMenu.addMenuItem(
					new MenuItem(
						"Required materials",
						() -> {},
						Color.WHITE,
						Color.GREEN,
						Color.WHITE,
						null
					)
				);
				secondaryMenu.addMenuItem(
					new MenuItem(
						"Smith",
						() -> {},
						Color.WHITE,
						Color.GREEN,
						Color.WHITE,
						null
					)
				);
			} else {
				secondaryMenu.addMenuItem(
					new MenuItem(
						"Requires smithing level " + item.getRequiredSmithingLevel(),
						() -> {},
						Colors.UI_DARK_GRAY,
						Colors.UI_DARK_GRAY,
						Colors.UI_DARK_GRAY,
						null
					)
				);
			}

			items.put(
				new ListingMenuItem<String>(
					((Item)item).getSingular(true),
					new Button(
						((Item)item).getSingular(true),
						Fonts.defaultFont,
						0,
						0,
						((Item)item).getSingular(true).length() * 10,
						16,
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						item.canBeSmithedBy(smith) ? Color.WHITE : Colors.UI_DARK_GRAY,
						item.canBeSmithedBy(smith) ? Color.GREEN : Colors.UI_DARK_GRAY,
						item.canBeSmithedBy(smith) ? Color.WHITE : Colors.UI_DARK_GRAY,
						UIRef.BL
					),
					secondaryMenu
				),
				""
			);
		}

		return items;
	}


	@Override
	protected void internalWindowRender() {
		if (((Prop) anvil).position.cpy().sub((smith).getState().position.cpy()).len() > 64) {
			setClosing(true);
		}

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
}
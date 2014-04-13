package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Craftable;
import bloodandmithril.prop.crafting.CraftingStation;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.RequiredMaterialsPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Anvl} related UI
 *
 * @author Matt
 */
public class CraftingStationWindow extends Window implements Refreshable {

	private final Individual individual;
	private final CraftingStation craftingStation;
	private Item currentlySelectedToCraft;

	private ScrollableListingPanel<Item, String> craftablesListing;
	private RequiredMaterialsPanel requiredMaterialsListing;
	private Button showInfoButton, craftButton;

	/**
	 * Constructor
	 */
	public CraftingStationWindow(int x, int y, String title, Individual individual, CraftingStation craftingStation) {
		super(x, y, 750, 300, title, true, 750, 300, true, true);
		this.individual = individual;
		this.craftingStation = craftingStation;
		this.currentlySelectedToCraft = craftingStation.getCraftables().get(0);

		this.craftablesListing = new ScrollableListingPanel<Item, String>(this) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<Item>, String> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, String>> listings) {
				listings.add(constructCraftablesListing());
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		this.requiredMaterialsListing = new RequiredMaterialsPanel(this, individual, ((Craftable)currentlySelectedToCraft).getRequiredMaterials());

		this.showInfoButton = new Button(
			"Show info",
			Fonts.defaultFont,
			0,
			0,
			90,
			16,
			() -> {UserInterface.addLayeredComponent(currentlySelectedToCraft.getInfoWindow());},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		this.craftButton = new Button(
			craftingStation.getAction(),
			Fonts.defaultFont,
			0,
			0,
			90,
			16,
			() -> {},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);
	}


	/**
	 * Constructs the listing
	 */
	private HashMap<ListingMenuItem<Item>, String> constructCraftablesListing() {
		HashMap<ListingMenuItem<Item>, String> listing = newHashMap();

		for (Item item : craftingStation.getCraftables()) {
			String itemName = item.getSingular(true);
			listing.put(
				new ListingMenuItem<Item>(
					item,
					new Button(
						itemName,
						Fonts.defaultFont,
						0,
						0,
						itemName.length() * 10,
						16,
						() -> {
							currentlySelectedToCraft = item;
							craftablesListing.getListing().clear();
							craftablesListing.getListing().add(constructCraftablesListing());
							requiredMaterialsListing.getRequiredMaterials().clear();
							requiredMaterialsListing.getRequiredMaterials().putAll(((Craftable)item).getRequiredMaterials());
							refresh();
						},
						currentlySelectedToCraft.sameAs(item) ? Color.GREEN : Color.WHITE,
						currentlySelectedToCraft.sameAs(item) ? Color.ORANGE : Color.GREEN,
						Color.WHITE,
						UIRef.BL
					),
					null
				),
				""
			);
		}

		return listing;
	}


	@Override
	protected void internalWindowRender() {
		if (individual.getState().position.cpy().sub(craftingStation.position).len() > 64f) {
			setClosing(true);
		}

		craftablesListing.x = x;
		craftablesListing.y = y;
		craftablesListing.width = width / 2 - 50;
		craftablesListing.height = height;

		requiredMaterialsListing.x = x + width / 2 - 40;
		requiredMaterialsListing.y = y - 100;
		requiredMaterialsListing.width = width / 2 + 40;
		requiredMaterialsListing.height = height - 100;

		craftablesListing.render();
		requiredMaterialsListing.render();

		defaultFont.setColor(isActive() ? Colors.modulateAlpha(Color.GREEN, getAlpha()) : Colors.modulateAlpha(Color.GREEN, 0.5f * getAlpha()));
		defaultFont.draw(spriteBatch, "Selected: " + currentlySelectedToCraft.getSingular(true), x + width / 2 - 33, y - 33);
		defaultFont.draw(spriteBatch, "Required materials:", x + width / 2 - 33, y - 113);

		showInfoButton.render(
			x + width / 2 + 11,
			y - 45,
			isActive(),
			getAlpha()
		);

		craftButton.render(
			x + width / 2 + 11,
			y - 65,
			isActive(),
			getAlpha()
		);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		craftablesListing.leftClick(copy, windowsCopy);
		requiredMaterialsListing.leftClick(copy, windowsCopy);
		showInfoButton.click();
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return "CraftingStationWindow" + (individual.hashCode() + craftingStation.hashCode());
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}


	@Override
	public void leftClickReleased() {
		craftablesListing.leftClickReleased();
		requiredMaterialsListing.leftClickReleased();
	}


	@Override
	public void refresh() {
		requiredMaterialsListing.refresh();
	}
}
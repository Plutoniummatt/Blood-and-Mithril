package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Craft;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Craftable;
import bloodandmithril.prop.crafting.CraftingStation;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
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
	private boolean enoughMaterials;

	private ScrollableListingPanel<Item, String> craftablesListing;
	private RequiredMaterialsPanel requiredMaterialsListing;
	private Button showInfoButton, craftButton, takeFinishedItemButton;

	private static Comparator<Item> sortingComparator = new Comparator<Item>() {
		@Override
		public int compare(Item o1, Item o2) {
			return o1.getSingular(false).compareTo(o2.getSingular(false));
		}
	};

	/**
	 * Constructor
	 */
	public CraftingStationWindow(int x, int y, String title, Individual individual, CraftingStation craftingStation) {
		super(x, y, 750, 300, title, true, 750, 300, true, true);
		this.individual = individual;
		this.craftingStation = craftingStation;
		this.currentlySelectedToCraft = craftingStation.getCurrentlyBeingCrafted() == null ? craftingStation.getCraftables().get(0) : craftingStation.getCurrentlyBeingCrafted();

		this.craftablesListing = new ScrollableListingPanel<Item, String>(this, sortingComparator) {
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
			() -> {
				craft();
			},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		this.takeFinishedItemButton = new Button(
			"Take item",
			Fonts.defaultFont,
			0,
			0,
			90,
			16,
			() -> {
				takeItem();
			},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		craftablesListing.setScrollWheelActive(true);

		refresh();
	}


	/**
	 * Takes the item from the {@link CraftingStation} and gives it to the {@link #individual}
	 */
	private void takeItem() {
		if (craftingStation.isFinished()) {
			if (ClientServerInterface.isServer()) {
				craftingStation.takeItem(individual);
				UserInterface.refreshRefreshableWindows();
			} else {
				ClientServerInterface.SendRequest.sendTakeItemFromCraftingStationRequest(craftingStation, individual);
			}
		} else {
			UserInterface.addMessage(craftingStation.getTitle(), "Item already taken");
		}
	}


	/**
	 * Called when the action button is pressed, i.e 'Smith'
	 */
	private void craft() {
		if (!customCanCraft() ||
			craftingStation.getCurrentlyBeingCrafted() != null && craftingStation.isOccupied() ||
			!enoughMaterials && craftingStation.getCurrentlyBeingCrafted() == null) {
			return;
		}

		if (ClientServerInterface.isServer()) {
			individual.getAI().setCurrentTask(new Craft(individual, craftingStation, currentlySelectedToCraft));
		} else {
			ClientServerInterface.SendRequest.sendStartCraftingRequest(individual, craftingStation, currentlySelectedToCraft);
		}
	}


	@Override
	public boolean scrolled(int amount) {
		return craftablesListing.scrolled(amount) || requiredMaterialsListing.scrolled(amount);
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
						currentlySelectedToCraft.sameAs(item) ? Color.ORANGE : Color.ORANGE,
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
		requiredMaterialsListing.y = y - 120;
		requiredMaterialsListing.width = width / 2 + 40;
		requiredMaterialsListing.height = height - 120;

		craftablesListing.render();
		requiredMaterialsListing.render();

		defaultFont.setColor(isActive() ? Colors.modulateAlpha(Color.GREEN, getAlpha()) : Colors.modulateAlpha(Color.GREEN, 0.5f * getAlpha()));
		String selected = craftingStation.getCurrentlyBeingCrafted() == null ? "Selected: " : craftingStation.getAction() + "ing: ";
		String progress = craftingStation.getCurrentlyBeingCrafted() == null ? "" : " (" + String.format("%.1f", 100f * craftingStation.getCraftingProgress()) + "%)";
		defaultFont.draw(spriteBatch, selected + currentlySelectedToCraft.getSingular(true) + progress, x + width / 2 - 33, y - 33);
		defaultFont.draw(spriteBatch, "Required materials:", x + width / 2 - 33, y - 133);

		showInfoButton.render(
			x + width / 2 + 11,
			y - 45,
			isActive(),
			getAlpha()
		);

		craftButton.render(
			x + width / 2 + 11,
			y - 65,
			isActive() && (craftingStation.getCurrentlyBeingCrafted() == null || !craftingStation.isOccupied()) && !craftingStation.isFinished() && (enoughMaterials || craftingStation.getCurrentlyBeingCrafted() != null) && customCanCraft(),
			getAlpha()
		);

		takeFinishedItemButton.render(
			x + width / 2 + 11,
			y - 85,
			isActive() && craftingStation.isFinished(),
			getAlpha()
		);
	}


	/**
	 * @return See {@link CraftingStation#customCanCraft()}
	 */
	protected boolean customCanCraft() {
		return craftingStation.customCanCraft();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		craftablesListing.leftClick(copy, windowsCopy);
		requiredMaterialsListing.leftClick(copy, windowsCopy);
		showInfoButton.click();

		if (craftingStation.isFinished()) {
			takeFinishedItemButton.click();
		}

		if (!customCanCraft() && craftButton.click()) {
			copy.add(
				new ContextMenu(
					getMouseScreenX(),
					getMouseScreenY(),
					new MenuItem(
						craftingStation.getCustomMessage(),
						() -> {},
						Colors.UI_DARK_GRAY,
						Colors.UI_DARK_GRAY,
						Colors.UI_DARK_GRAY,
						null
					)
				)
			);
			return;
		}

		if (craftingStation.getCurrentlyBeingCrafted() != null && craftingStation.isOccupied() && !craftingStation.isFinished()) {
			if (craftButton.click()) {
				copy.add(
					new ContextMenu(
						getMouseScreenX(),
						getMouseScreenY(),
						new MenuItem(
							"Something is currently in progress",
							() -> {},
							Colors.UI_DARK_GRAY,
							Colors.UI_DARK_GRAY,
							Colors.UI_DARK_GRAY,
							null
						)
					)
				);
			}
		} else {
			craftablesListing.leftClick(copy, windowsCopy);
			if (craftButton.click() && !(enoughMaterials || craftingStation.getCurrentlyBeingCrafted() != null)) {
				copy.add(
					new ContextMenu(
						getMouseScreenX(),
						getMouseScreenY(),
						new MenuItem(
							"Not enough materials",
							() -> {},
							Colors.UI_DARK_GRAY,
							Colors.UI_DARK_GRAY,
							Colors.UI_DARK_GRAY,
							null
						)
					)
				);
			}
		}
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
	@SuppressWarnings("unchecked")
	public void refresh() {
		if (craftingStation.isOccupied() && !currentlySelectedToCraft.sameAs(craftingStation.getCurrentlyBeingCrafted())) {
			currentlySelectedToCraft = craftingStation.getCurrentlyBeingCrafted();
			craftablesListing.getListing().clear();
			craftablesListing.getListing().add(constructCraftablesListing());
			requiredMaterialsListing.getRequiredMaterials().clear();
			requiredMaterialsListing.getRequiredMaterials().putAll(((Craftable)currentlySelectedToCraft).getRequiredMaterials());
			refresh();
		}

		enoughMaterials = CraftingStation.enoughMaterialsToCraft(individual, ((Craftable) currentlySelectedToCraft).getRequiredMaterials());
		requiredMaterialsListing.refresh();

		if (craftingStation.getCurrentlyBeingCrafted() != null) {
			craftablesListing.getListing().stream().forEach(map -> {
				for (Entry<ListingMenuItem<Item>, String> entry : map.entrySet()) {
					if (!entry.getKey().t.sameAs(craftingStation.getCurrentlyBeingCrafted())) {
						entry.getKey().button.setIdleColor(Colors.UI_DARK_GRAY);
						entry.getKey().button.setOverColor(Colors.UI_DARK_GRAY);
						entry.getKey().button.setDownColor(Colors.UI_DARK_GRAY);
						entry.getKey().button.setTask(() -> {});
					}
				}
			});
		} else {
			craftablesListing.refresh(newArrayList(constructCraftablesListing()));
		}
	}
}
package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.modulateAlpha;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.Craft;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
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
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.SerializableDoubleWrapper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * {@link Anvl} related UI
 *
 * @author Matt
 */
public class CraftingStationWindow extends Window implements Refreshable {

	private final Individual individual;
	private SerializableDoubleWrapper<Item, Integer> currentlySelectedToCraft;
	protected final CraftingStation craftingStation;
	protected boolean enoughMaterials;

	private ScrollableListingPanel<Item, String> craftablesListing;
	private RequiredMaterialsPanel requiredMaterialsListing;
	protected Button showInfoButton, craftButton, takeFinishedItemButton;

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

		Entry<Item, Integer> next = craftingStation.getCraftables().entrySet().iterator().next();
		this.currentlySelectedToCraft = craftingStation.getCurrentlyBeingCrafted() == null ? new SerializableDoubleWrapper<Item, Integer>(next.getKey(), next.getValue()) : craftingStation.getCurrentlyBeingCrafted();

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

		this.requiredMaterialsListing = new RequiredMaterialsPanel(this, individual, ((Craftable)currentlySelectedToCraft.t).getRequiredMaterials());

		this.showInfoButton = new Button(
			"Show info",
			Fonts.defaultFont,
			0,
			0,
			90,
			16,
			() -> {UserInterface.addLayeredComponent(currentlySelectedToCraft.t.getInfoWindow());},
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
				if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							WIDTH / 2 - 125,
							HEIGHT/2 + 50,
							250,
							100,
							"Quantity",
							250,
							100,
							args -> {
								try {
									int quantity = Integer.parseInt(args[0].toString());
									craft(quantity);
								} catch (Exception e) {
									UserInterface.addMessage("Error", "Cannot recognise " + args[0].toString() + " as a quantity.");
								}
							},
							"Confirm",
							true,
							""
						)
					);
				} else {
					craft(1);
				}
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
	private void craft(int quantity) {
		if (!customCanCraft() ||
			craftingStation.getCurrentlyBeingCrafted() != null && craftingStation.isOccupied() ||
			!enoughMaterials && craftingStation.getCurrentlyBeingCrafted() == null) {
			return;
		}

		if (ClientServerInterface.isServer()) {
			individual.getAI().setCurrentTask(new Craft(individual, craftingStation, currentlySelectedToCraft, quantity));
		} else {
			ClientServerInterface.SendRequest.sendStartCraftingRequest(individual, craftingStation, currentlySelectedToCraft, quantity);
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

		for (Entry<Item, Integer> item : craftingStation.getCraftables().entrySet()) {
			String itemName = item.getKey().getSingular(true) + " (" + item.getValue() + ")";
			listing.put(
				new ListingMenuItem<Item>(
					item.getKey(),
					new Button(
						itemName,
						Fonts.defaultFont,
						0,
						0,
						itemName.length() * 10,
						16,
						() -> {
							currentlySelectedToCraft = new SerializableDoubleWrapper<>(item.getKey(), item.getValue());
							craftablesListing.getListing().clear();
							craftablesListing.getListing().add(constructCraftablesListing());
							requiredMaterialsListing.getRequiredMaterials().clear();
							requiredMaterialsListing.getRequiredMaterials().putAll(((Craftable)item.getKey()).getRequiredMaterials());
							refresh();
						},
						currentlySelectedToCraft.t.sameAs(item.getKey()) ? Color.GREEN : Color.WHITE,
						currentlySelectedToCraft.t.sameAs(item.getKey()) ? Color.ORANGE : Color.ORANGE,
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
		requiredMaterialsListing.y = y - 130;
		requiredMaterialsListing.width = width / 2 + 40;
		requiredMaterialsListing.height = height - 130;

		craftablesListing.render();
		requiredMaterialsListing.render();

		defaultFont.setColor(isActive() ? Colors.modulateAlpha(Color.GREEN, getAlpha()) : Colors.modulateAlpha(Color.GREEN, 0.5f * getAlpha()));
		String selected = craftingStation.getCurrentlyBeingCrafted() == null ? "Selected: " : craftingStation.getAction() + "ing: ";
		String bulkMessage = "";

		AITask currentTask = individual.getAI().getCurrentTask();
		if (currentTask instanceof Craft) {
			int quantity = ((Craft) currentTask).getQuantity();
			bulkMessage = quantity > 1 ? "(" + quantity + " left)" : "";
		}

		String progress = craftingStation.getCurrentlyBeingCrafted() == null ? "" : " (" + String.format("%.1f", 100f * craftingStation.getCraftingProgress()) + "%) " + bulkMessage;
		defaultFont.draw(spriteBatch, selected + currentlySelectedToCraft.t.getSingular(true) + progress, x + width / 2 - 33, y - 33);
		defaultFont.draw(spriteBatch, "Required materials:", x + width / 2 - 33, y - 133);

		renderButtons();
		renderItemIcon();
	}


	private void renderItemIcon() {
		renderRectangle(x + width - 74, y - 30, 64, 64, isActive(), modulateAlpha(Color.BLACK, 0.5f));
		renderBox(x + width - 76, y - 32, 64, 64, isActive(), borderColor);

		TextureRegion icon = currentlySelectedToCraft.t.getIconTextureRegion();
		if (icon != null) {
			spriteBatch.setShader(Shaders.filter);
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, getAlpha() * (isActive() ? 1f : 0.6f));
			spriteBatch.draw(icon, x + width - 74, y - 96);
		}
	}


	/**
	 * Renders the buttons on this {@link CraftingStationWindow}
	 */
	protected void renderButtons() {
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
	public void leftClickReleased() {
		craftablesListing.leftClickReleased();
		requiredMaterialsListing.leftClickReleased();
	}


	@Override
	@SuppressWarnings("unchecked")
	public void refresh() {
		if (craftingStation.isOccupied() && craftingStation.getCurrentlyBeingCrafted() != null && !currentlySelectedToCraft.t.sameAs(craftingStation.getCurrentlyBeingCrafted().t)) {
			currentlySelectedToCraft = craftingStation.getCurrentlyBeingCrafted();
			craftablesListing.getListing().clear();
			craftablesListing.getListing().add(constructCraftablesListing());
			requiredMaterialsListing.getRequiredMaterials().clear();
			requiredMaterialsListing.getRequiredMaterials().putAll(((Craftable)currentlySelectedToCraft).getRequiredMaterials());
		}

		enoughMaterials = CraftingStation.enoughMaterialsToCraft(individual, ((Craftable) currentlySelectedToCraft.t).getRequiredMaterials());
		requiredMaterialsListing.refresh();

		if (craftingStation.getCurrentlyBeingCrafted() != null) {
			craftablesListing.getListing().stream().forEach(map -> {
				for (Entry<ListingMenuItem<Item>, String> entry : map.entrySet()) {
					if (!entry.getKey().t.sameAs(craftingStation.getCurrentlyBeingCrafted().t)) {
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
package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.modulateAlpha;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.craft.Craft;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
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

/**
 * {@link CraftingStation} related UI
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class CraftingStationWindow extends Window implements Refreshable {

	@Inject	private Controls controls;
	@Inject	private UserInterface userInterface;

	private final Individual individual;
	private SerializableDoubleWrapper<Item, Integer> currentlySelectedToCraft;
	protected final CraftingStation craftingStation;
	protected boolean enoughMaterials;

	private ScrollableListingPanel<Item, String> craftablesListing;
	private RequiredMaterialsPanel requiredMaterialsListing;
	protected Button showInfoButton, craftButton, takeFinishedItemButton;

	private static Comparator<Item> sortingComparator = new Comparator<Item>() {
		@Override
		public int compare(final Item o1, final Item o2) {
			return o1.getSingular(false).compareTo(o2.getSingular(false));
		}
	};

	/**
	 * Constructor
	 */
	public CraftingStationWindow(final String title, final Individual individual, final CraftingStation craftingStation) {
		super(800, 300, title, true, 800, 300, true, true, true);
		this.individual = individual;
		this.craftingStation = craftingStation;

		final Entry<Item, Integer> next = craftingStation.getCraftables().entrySet().iterator().next();
		this.currentlySelectedToCraft = craftingStation.getCurrentlyBeingCrafted() == null ? new SerializableDoubleWrapper<Item, Integer>(next.getKey(), next.getValue()) : craftingStation.getCurrentlyBeingCrafted();

		this.craftablesListing = new ScrollableListingPanel<Item, String>(this, sortingComparator, false, 35, null) {
			@Override
			protected String getExtraString(final Entry<ListingMenuItem<Item>, String> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void populateListings(final List<HashMap<ListingMenuItem<Item>, String>> listings) {
				listings.add(constructCraftablesListing());
			}

			@Override
			public boolean keyPressed(final int keyCode) {
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
			() -> {userInterface.addLayeredComponentUnique(currentlySelectedToCraft.t.getInfoWindow());},
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
				if (isKeyPressed(controls.bulkCraft.keyCode)) {
					userInterface.addLayeredComponent(
						new TextInputWindow(
							250,
							100,
							"Quantity",
							250,
							100,
							args -> {
								try {
									final int quantity = Integer.parseInt(args[0].toString());
									craft(quantity);
								} catch (final Exception e) {
									userInterface.addGlobalMessage("Error", "Cannot recognise " + args[0].toString() + " as a quantity.");
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
			userInterface.addGlobalMessage(craftingStation.getTitle(), "Item already taken");
		}
	}


	/**
	 * Called when the action button is pressed, i.e 'Smith'
	 */
	private void craft(final int quantity) {
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
	public boolean scrolled(final int amount) {
		return craftablesListing.scrolled(amount) || requiredMaterialsListing.scrolled(amount);
	}


	/**
	 * Constructs the listing
	 */
	private HashMap<ListingMenuItem<Item>, String> constructCraftablesListing() {
		final HashMap<ListingMenuItem<Item>, String> listing = newHashMap();

		for (final Entry<Item, Integer> item : craftingStation.getCraftables().entrySet()) {
			final String itemName = item.getKey().getSingular(true) + " (" + item.getValue() + ")";
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
						CraftingStation.enoughMaterialsToCraft(individual, ((Craftable) item.getKey()).getRequiredMaterials()) ? currentlySelectedToCraft.t.sameAs(item.getKey()) ? Color.GREEN : Color.WHITE : Color.GRAY,
						currentlySelectedToCraft.t.sameAs(item.getKey()) ? Color.ORANGE : Color.ORANGE,
						CraftingStation.enoughMaterialsToCraft(individual, ((Craftable) item.getKey()).getRequiredMaterials()) ? Color.WHITE : Color.GRAY,
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
	protected void internalWindowRender(final Graphics graphics) {
		if (individual.getState().position.cpy().sub(craftingStation.position).len() > 96f) {
			setClosing(true);
		}

		if (!individual.isAlive()) {
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

		craftablesListing.render(graphics);
		requiredMaterialsListing.render(graphics);

		defaultFont.setColor(isActive() ? Colors.modulateAlpha(Color.GREEN, getAlpha()) : Colors.modulateAlpha(Color.GREEN, 0.5f * getAlpha()));
		final String selected = craftingStation.getCurrentlyBeingCrafted() == null ? "Selected: " : craftingStation.getAction() + "ing: ";
		String bulkMessage = "";

		final AITask currentTask = individual.getAI().getCurrentTask();
		if (currentTask instanceof Craft) {
			final int quantity = ((Craft) currentTask).getQuantity();
			bulkMessage = quantity > 1 ? "(" + quantity + " left)" : "";
		}

		final String progress = craftingStation.getCurrentlyBeingCrafted() == null ? "" : " (" + String.format("%.1f", 100f * craftingStation.getCraftingProgress()) + "%) " + bulkMessage;
		defaultFont.draw(graphics.getSpriteBatch(), selected + currentlySelectedToCraft.t.getSingular(true) + progress, x + width / 2 - 33, y - 33);
		defaultFont.draw(graphics.getSpriteBatch(), "Required materials:", x + width / 2 - 33, y - 133);

		renderButtons(graphics);
		renderItemIcon(graphics);
	}


	private void renderItemIcon(final Graphics graphics) {
		renderRectangle(x + width - 74, y - 30, 64, 64, isActive(), modulateAlpha(Color.BLACK, 0.5f), graphics);
		renderBox(x + width - 76, y - 32, 64, 64, isActive(), borderColor, graphics);

		final TextureRegion icon = currentlySelectedToCraft.t.getIconTextureRegion();
		if (icon != null) {
			graphics.getSpriteBatch().setShader(Shaders.filter);
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, getAlpha() * (isActive() ? 1f : 0.6f));
			graphics.getSpriteBatch().draw(icon, x + width - 74, y - 96);
		}
	}


	/**
	 * Renders the buttons on this {@link CraftingStationWindow}
	 */
	protected void renderButtons(final Graphics graphics) {
		showInfoButton.render(
			x + width / 2 + 11,
			y - 45,
			isActive(),
			getAlpha(),
			graphics
		);

		craftButton.render(
			x + width / 2 + 11,
			y - 65,
			isActive() && (craftingStation.getCurrentlyBeingCrafted() == null || !craftingStation.isOccupied()) && !craftingStation.isFinished() && (enoughMaterials || craftingStation.getCurrentlyBeingCrafted() != null) && customCanCraft(),
			getAlpha(),
			graphics
		);

		takeFinishedItemButton.render(
			x + width / 2 + 11,
			y - 85,
			isActive() && craftingStation.isFinished(),
			getAlpha(),
			graphics
		);

		graphics.getSpriteBatch().flush();
	}


	/**
	 * @return See {@link CraftingStation#customCanCraft()}
	 */
	protected boolean customCanCraft() {
		return craftingStation.customCanCraft();
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
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
					true,
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
						true,
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
						true,
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
			requiredMaterialsListing.getRequiredMaterials().putAll(((Craftable)currentlySelectedToCraft.t).getRequiredMaterials());
		}

		enoughMaterials = CraftingStation.enoughMaterialsToCraft(individual, ((Craftable) currentlySelectedToCraft.t).getRequiredMaterials());
		requiredMaterialsListing.refresh();

		if (craftingStation.getCurrentlyBeingCrafted() != null) {
			craftablesListing.getListing().stream().forEach(map -> {
				for (final Entry<ListingMenuItem<Item>, String> entry : map.entrySet()) {
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
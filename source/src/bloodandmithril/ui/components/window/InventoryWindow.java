package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Container;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.container.Bottle;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Maps;

/**
 * {@link Window} to display the inventory of an {@link Individual} or Container
 *
 * @author Matt
 */
public class InventoryWindow extends Window {

	/** Inventory listing maps */
	HashMap<ListingMenuItem<Item>, Integer> equippedItemsToDisplay = Maps.newHashMap();
	HashMap<ListingMenuItem<Item>, Integer> nonEquippedItemsToDisplay = Maps.newHashMap();

	/** The {@link Container} that is the host of this {@link InventoryWindow} */
	public Equipper host;

	/** The inventory listing panel, see {@link ScrollableListingPanel} */
	private Panel inventoryListingPanel;

	/**
	 * Constructor
	 */
	@Deprecated
	public InventoryWindow(
			Equipper host,
			int x,
			int y,
			int length,
			int height,
			Color borderColor,
			Color backGroundColor,
			String title,
			boolean active,
			int minLength,
			int minHeight) {
		super(x, y, length, height, borderColor, backGroundColor, title, active, minLength, minHeight, true);
		this.host = host;
		buildItems(host.getEquipped(), host.getInventory());
	}


	/**
	 * Overloaded constructor - with default colors
	 */
	public InventoryWindow(
			Equipper host,
			int x,
			int y,
			int length,
			int height,
			String title,
			boolean active,
			int minLength,
			int minHeight) {
		super(x, y, length, height, title, active, minLength, minHeight, true);
		this.host = host;
		buildItems(host.getEquipped(), host.getInventory());
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		inventoryListingPanel.leftClick(copy, windowsCopy);
	}


	@Override
	public void leftClickReleased() {
		inventoryListingPanel.leftClickReleased();
	}


	@Override
	protected void internalWindowRender() {

		// Set the position and dimensions of the panel
		inventoryListingPanel.height = height;
		inventoryListingPanel.width = width;
		inventoryListingPanel.x = x;
		inventoryListingPanel.y = y;

		// Render the separator
		renderSeparator();

		// Render the listing panel
		inventoryListingPanel.render();

		// Render the weight indication text
		renderWeightIndicationText();
	}


	/**
	 * Renders the weight display
	 */
	private void renderWeightIndicationText() {
		Color activeColor = host.getCurrentLoad() < host.getMaxCapacity() ?
				new Color(0.7f * host.getCurrentLoad()/host.getMaxCapacity(), 1f - 0.7f * host.getCurrentLoad()/host.getMaxCapacity(), 0f, getAlpha()) :
				Colors.modulateAlpha(Color.RED, getAlpha());

		Color inactiveColor = host.getCurrentLoad() < host.getMaxCapacity() ?
				new Color(0.7f*host.getCurrentLoad()/host.getMaxCapacity(), 1f - 0.7f * host.getCurrentLoad()/host.getMaxCapacity(), 0f, 0.6f * getAlpha()) :
					Colors.modulateAlpha(Color.RED, 0.6f * getAlpha());

		defaultFont.setColor(isActive() ? activeColor : inactiveColor);
		defaultFont.draw(BloodAndMithrilClient.spriteBatch, truncate("Weight: " + String.format("%.2f", host.getCurrentLoad()) + "/" + String.format("%.2f", host.getMaxCapacity())), x + 6, y - height + 20);
	}


	/**
	 * Renders the separator that separates the item listing from the quantity listing
	 */
	private void renderSeparator() {
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.filter);
		shapeRenderer.begin(ShapeType.FilledRectangle);
		Color color = isActive() ? Colors.modulateAlpha(borderColor, getAlpha()) : Colors.modulateAlpha(borderColor, 0.4f * getAlpha());
		shapeRenderer.filledRect(x + width - 88, y + 24 - height, 2, height - 45, Color.CLEAR, Color.CLEAR, color, color);
		shapeRenderer.end();
	}


	/**
	 * Builds the list of items to display
	 */
	private void buildItems(HashMap<Item, Integer> equippedItems, HashMap<Item, Integer> nonEquippedItems) {
		populateList(equippedItems, true);
		populateList(nonEquippedItems, false);

		inventoryListingPanel = new ScrollableListingPanel<Item>(this) {
			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				listings.add(equippedItemsToDisplay);
				listings.add(nonEquippedItemsToDisplay);
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


	/**
	 * @param listToPopulate
	 * @param eq - true if equipped
	 */
	private void populateList(HashMap<Item, Integer> listToPopulate, boolean eq) {
		for(final Entry<Item, Integer> item : listToPopulate.entrySet()) {

			final ContextMenu menuToAddUnequipped = determineMenu(item.getKey(), false);
			Button inventoryButton = new Button(
				item.getKey().getSingular(true),
				defaultFont,
				0,
				0,
				item.getKey().getSingular(true).length() * 10,
				16,
				new Task() {
					@Override
					public void execute() {
						menuToAddUnequipped.x = BloodAndMithrilClient.getMouseScreenX();
						menuToAddUnequipped.y = BloodAndMithrilClient.getMouseScreenY();
					}
				},
				eq ? Colors.UI_GRAY : Color.WHITE,
				Color.GREEN,
				Color.WHITE,
				UIRef.BL
			);

			final ContextMenu menuToAddEquipped = determineMenu(item.getKey(), true);
			Button equippedButton = new Button(
				item.getKey().getSingular(true),
				defaultFont,
				0,
				0,
				item.getKey().getSingular(true).length() * 10,
				16,
				new Task() {
					@Override
					public void execute() {
						menuToAddEquipped.x = BloodAndMithrilClient.getMouseScreenX();
						menuToAddEquipped.y = BloodAndMithrilClient.getMouseScreenY();
					}
				},
				eq ? Colors.UI_GRAY : Color.WHITE,
				Color.GREEN,
				Color.WHITE,
				UIRef.BL
			);

			if (eq) {
				this.equippedItemsToDisplay.put(
					new ListingMenuItem<Item>(item.getKey(), equippedButton, menuToAddEquipped),
					item.getValue()
				);
			} else {
				this.nonEquippedItemsToDisplay.put(
					new ListingMenuItem<Item>(item.getKey(), inventoryButton, menuToAddUnequipped),
					item.getValue()
				);
			}

			minLength = minLength < inventoryButton.width + 100 ? inventoryButton.width + 100 : minLength;
			this.width = width < minLength ? minLength : width;
		}
	}


	/** Determines which context menu to use */
	private ContextMenu determineMenu(final Item item, boolean equipped) {
		if (item instanceof Consumable) {
			ContextMenuItem consume = new ContextMenuItem(
				"Consume",
				new Task() {
					@Override
					public void execute() {
						if (ClientServerInterface.isServer()) {
							if (item instanceof Consumable && host instanceof Individual && ((Consumable)item).consume((Individual)host)) {
								host.takeItem(item);
							}
						} else {
							ClientServerInterface.SendRequest.sendConsumeItemRequest((Consumable)item, ((Individual)host).getId().getId());
						}
						refresh();
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.WHITE,
				null
			);

			ContextMenu contextMenu = new ContextMenu(x, y,
				InventoryItemContextMenuConstructor.showInfo(item)
			);

			if (host instanceof Individual) {
				contextMenu.addMenuItem(consume);
			}
			return contextMenu;
		}

		if (item instanceof Bottle) {
			ContextMenuItem drink = new ContextMenuItem(
				"Drink",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(
							new TextInputWindow(
								BloodAndMithrilClient.WIDTH / 2 - 125,
								BloodAndMithrilClient.HEIGHT/2 + 50,
								250,
								100,
								"Amount",
								250,
								100,
								new JITTask() {
									@Override
									public void execute(Object... args) {
										if (ClientServerInterface.isServer()) {
											host.takeItem(item);
											Bottle newBottle = ((Bottle) item).clone();
											newBottle.drink(Float.parseFloat((String)args[0]), (Individual)host);
											host.giveItem(newBottle);
											refresh();
										} else {
											ClientServerInterface.SendRequest.sendDrinkLiquidRequest(((Individual)host).getId().getId(), (Bottle)item, Float.parseFloat((String)args[0]));
										}
									}
								},
								"Drink",
								true,
								""
							)
						);
					}
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);

			ContextMenu contextMenu = new ContextMenu(x, y,
				InventoryItemContextMenuConstructor.showInfo(item)
			);

			if (host instanceof Individual) {
				contextMenu.addMenuItem(drink);
			}

			return contextMenu;
		}

		if (item instanceof Equipable) {
			ContextMenuItem equipUnequip = equipped ? new ContextMenuItem(
				"Unequip",
				new Task() {
					@Override
					public void execute() {
						if (ClientServerInterface.isServer()) {
							host.unequip((Equipable)item);
						} else {
							ClientServerInterface.SendRequest.sendEquipOrUnequipItemRequest(false, (Equipable)item, ((Individual)host).getId().getId());
						}
						refresh();
					}
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			) :

			new ContextMenuItem(
				"Equip",
				new Task() {
					@Override
					public void execute() {
						if (ClientServerInterface.isServer()) {
							host.equip((Equipable)item);
						} else {
							ClientServerInterface.SendRequest.sendEquipOrUnequipItemRequest(true, (Equipable)item, ((Individual)host).getId().getId());
						}
						refresh();
					}
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);

			return new ContextMenu(x, y,
				InventoryItemContextMenuConstructor.showInfo(item),
				equipUnequip
			);
		}

		return new ContextMenu(x, y,
			InventoryItemContextMenuConstructor.showInfo(item)
		);
	}


	/** Refreshes this {@link InventoryWindow} */
	public synchronized void refresh() {
		equippedItemsToDisplay.clear();
		nonEquippedItemsToDisplay.clear();

		buildItems(host.getEquipped(), host.getInventory());
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}


	private static class InventoryItemContextMenuConstructor {
		private static ContextMenuItem showInfo(final Item item) {
			return new ContextMenuItem(
				"Show info",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(item.getInfoWindow());
					}
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);
		}
	}
}
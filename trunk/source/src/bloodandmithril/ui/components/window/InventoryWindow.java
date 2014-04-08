package bloodandmithril.ui.components.window;

import static bloodandmithril.csi.ClientServerInterface.isServer;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.CursorBoundTask;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.container.LiquidContainer;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.JITTask;
import bloodandmithril.character.ai.task.DiscardLiquid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Maps;

/**
 * {@link Window} to display the inventory of an {@link Individual} or Container
 *
 * @author Matt
 */
public class InventoryWindow extends Window implements Refreshable {

	/** Inventory listing maps */
	private HashMap<ListingMenuItem<Item>, Integer> equippedItemsToDisplay = Maps.newHashMap();
	private HashMap<ListingMenuItem<Item>, Integer> nonEquippedItemsToDisplay = Maps.newHashMap();

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
		super(x, y, length, height, borderColor, backGroundColor, title, active, minLength, minHeight, true, true);
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
		super(x, y, length, height, title, active, minLength, minHeight, true, true);
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
	private void buildItems(Map<Item, Integer> equippedItems, Map<Item, Integer> nonEquippedItems) {
		populateList(equippedItems, true);
		populateList(nonEquippedItems, false);

		inventoryListingPanel = new ScrollableListingPanel<Item, Integer>(this) {
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
	private void populateList(Map<Item, Integer> listToPopulate, boolean eq) {
		for(final Entry<Item, Integer> item : listToPopulate.entrySet()) {

			final ContextMenu menuToAddUnequipped = determineMenu(item.getKey(), false);
			Button inventoryButton = new Button(
				item.getKey().getSingular(true),
				defaultFont,
				0,
				0,
				item.getKey().getSingular(true).length() * 10,
				16,
				() -> {
					menuToAddUnequipped.x = BloodAndMithrilClient.getMouseScreenX();
					menuToAddUnequipped.y = BloodAndMithrilClient.getMouseScreenY();
				},
				eq ? Color.GREEN : Color.WHITE,
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
				() -> {
					menuToAddEquipped.x = BloodAndMithrilClient.getMouseScreenX();
					menuToAddEquipped.y = BloodAndMithrilClient.getMouseScreenY();
				},
				eq ? Color.GREEN : Color.WHITE,
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
			MenuItem consume = new MenuItem(
				"Consume",
				() -> {
					if (ClientServerInterface.isServer()) {
						if (item instanceof Consumable && host instanceof Individual && ((Consumable)item).consume((Individual)host)) {
							host.takeItem(item);
						}
					} else {
						ClientServerInterface.SendRequest.sendConsumeItemRequest((Consumable)item, ((Individual)host).getId().getId());
					}
					refresh();
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

		if (item instanceof LiquidContainer) {
			MenuItem drink = new MenuItem(
				"Drink from",
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							BloodAndMithrilClient.WIDTH / 2 - 125,
							BloodAndMithrilClient.HEIGHT/2 + 50,
							250,
							100,
							"Amount",
							250,
							100,
							args -> {
								try {
									float amount = Float.parseFloat(String.format("%.2f", Float.parseFloat(args[0].toString())));
									
									if (amount < 0.01f) {
										UserInterface.addMessage("Too little to drink", "It would be a waste of time to drink this little, enter a larger amount");
										return;
									}
									
									if (ClientServerInterface.isServer()) {
										host.takeItem(item);
										LiquidContainer newBottle = ((LiquidContainer) item).clone();
										newBottle.drinkFrom(amount, (Individual)host);
										host.giveItem(newBottle);
										refresh();
									} else {
										ClientServerInterface.SendRequest.sendDrinkLiquidRequest(((Individual)host).getId().getId(), (LiquidContainer)item, Float.parseFloat((String)args[0]));
									}
								} catch (NumberFormatException e) {
									UserInterface.addMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
								}
							},
							"Drink",
							true,
							""
						)
					);
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);
			
			MenuItem emptyContainerContents = new MenuItem(
				"Discard", 
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							BloodAndMithrilClient.WIDTH / 2 - 125,
							BloodAndMithrilClient.HEIGHT/2 + 50,
							250,
							100,
							"Amount",
							250,
							100,
							args -> {
								try {
									float amount = Util.round2dp(Float.parseFloat(args[0].toString()));
									
									if (amount < 0.01f) {
										UserInterface.addMessage("Too little to discard", "Its too little to discard, enter a larger amount");
										return;
									}
									
									if (ClientServerInterface.isServer()) {
										BloodAndMithrilClient.setCursorBoundTask(new CursorBoundTask(
											new JITTask() {
												@Override
												public void execute(Object... args) {
													((Individual)host).getAI().setCurrentTask(
														new DiscardLiquid((Individual)host, (int) args[0], (int) args[1], (LiquidContainer) item, amount)
													);
												}
											},
											true
										));
									} else {
										// TODO
									}
								} catch (NumberFormatException e) {
									UserInterface.addMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
								}
							},
							"Discard",
							true,
							""
						)
					);
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);
			
			MenuItem transferContainerContents = new MenuItem(
				"Transfer", 
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							BloodAndMithrilClient.WIDTH / 2 - 125,
							BloodAndMithrilClient.HEIGHT/2 + 50,
							250,
							100,
							"Amount",
							250,
							100,
							args -> {
								try {
									float amount = Util.round2dp(Float.parseFloat(args[0].toString()));
									if (amount < 0.01f) {
										UserInterface.addMessage("Too little to transfer", "Its too little to transfer, enter a larger amount");
										return;
									}
									
									for (ListingMenuItem<Item> listItem : nonEquippedItemsToDisplay.keySet()) {
										if (listItem.t instanceof LiquidContainer) {
											
											if (listItem.t == item) {
												listItem.button.setIdleColor(Colors.UI_DARK_GREEN);
												listItem.button.setDownColor(Colors.UI_DARK_GREEN);
												listItem.button.setOverColor(Colors.UI_DARK_GREEN);
												listItem.menu = null;
											} else {
												listItem.button.setIdleColor(Color.ORANGE);
												LiquidContainer toTransferTo = ((LiquidContainer)listItem.t).clone();
												listItem.menu = null;
												listItem.button.setTask(() -> {
													if (isServer()) {
														Individual individual = (Individual) host;
														LiquidContainer container = (LiquidContainer) item;
														individual.takeItem(container);
														individual.takeItem(listItem.t);
														LiquidContainer newBottle = ((LiquidContainer) container).clone();
														Map<Class<? extends Liquid>, Float> subtracted = newBottle.subtract(amount);
														Map<Class<? extends Liquid>, Float> remainder = toTransferTo.add(subtracted);
														if (!remainder.isEmpty()) {
															newBottle.add(remainder);
														}
														individual.giveItem(newBottle);
														individual.giveItem(toTransferTo);
														refresh();
													} else {
														// TODO
													}
												});
											}
										} else {
											listItem.button.setIdleColor(Colors.UI_DARK_GRAY);
											listItem.button.setDownColor(Colors.UI_DARK_GRAY);
											listItem.button.setOverColor(Colors.UI_DARK_GRAY);
											listItem.menu = null;
										}
									}
									
								} catch (NumberFormatException e) {
									UserInterface.addMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
								}
							},
							"Transfer",
							true,
							""
						)
					);
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
				if (!((LiquidContainer)item).isEmpty()) {
					contextMenu.addMenuItem(transferContainerContents);
					contextMenu.addMenuItem(emptyContainerContents);
				}
			}

			return contextMenu;
		}

		if (item instanceof Equipable) {
			MenuItem equipUnequip = equipped ? new MenuItem(
				"Unequip",
				() -> {
					if (ClientServerInterface.isServer()) {
						host.unequip((Equipable)item);
					} else {
						ClientServerInterface.SendRequest.sendEquipOrUnequipItemRequest(false, (Equipable)item, ((Individual)host).getId().getId());
					}
					refresh();
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			) :

			new MenuItem(
				"Equip",
				() -> {
					if (ClientServerInterface.isServer()) {
						host.equip((Equipable)item);
					} else {
						ClientServerInterface.SendRequest.sendEquipOrUnequipItemRequest(true, (Equipable)item, ((Individual)host).getId().getId());
					}
					refresh();
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
	@Override
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
		private static MenuItem showInfo(final Item item) {
			return new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(item.getInfoWindow());
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);
		}
	}
}
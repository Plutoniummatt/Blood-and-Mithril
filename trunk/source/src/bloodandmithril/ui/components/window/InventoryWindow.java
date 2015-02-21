package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.util.Fonts.defaultFont;
import static java.lang.Math.min;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.Item.Category;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.container.LiquidContainer;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.equipment.armor.Armor;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.item.items.food.plant.Seed;
import bloodandmithril.item.items.furniture.Furniture;
import bloodandmithril.item.items.material.Material;
import bloodandmithril.item.items.mineral.Mineral;
import bloodandmithril.item.items.misc.Misc;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.cursorboundtask.PlaceCursorBoundTask;
import bloodandmithril.util.cursorboundtask.PlantSeedCursorBoundTask;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * {@link Window} to display the inventory of an {@link Individual} or Container
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class InventoryWindow extends Window implements Refreshable {

	/** Inventory listing maps */
	private HashMap<ListingMenuItem<Item>, Integer> equippedItemsToDisplay = Maps.newHashMap();
	private HashMap<ListingMenuItem<Item>, Integer> nonEquippedItemsToDisplay = Maps.newHashMap();

	/** The {@link Container} that is the host of this {@link InventoryWindow} */
	public Equipper host;

	/** The inventory listing panel, see {@link ScrollableListingPanel} */
	private ScrollableListingPanel<Item, Integer> equippedListingPanel;
	private ScrollableListingPanel<Item, Integer> inventoryListingPanel;

	/** Filters used to filter this inventory window */
	private final Map<String, WrapperForTwo<Predicate<Item>, Boolean>> filters = Maps.newHashMap();

	/** Button that controls filtering */
	private Button filterButton = new Button(
		"Filters",
		Fonts.defaultFont,
		0,
		0,
		70,
		16,
		() -> {},
		Color.YELLOW,
		Color.GREEN,
		Color.YELLOW,
		UIRef.BR
	);

	public static Comparator<Item> inventorySortingOrder = new Comparator<Item>() {
		@Override
		public int compare(Item o1, Item o2) {
			return o1.getSingular(false).compareTo(o2.getSingular(false));
		}
	};

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
		super(
			x,
			y,
			length,
			height,
			title,
			active,
			minLength,
			minHeight,
			true,
			true,
			true
		);
		this.host = host;
		buildItems(host.getEquipped(), host.getInventory(), true);
		inventoryListingPanel.setScrollWheelActive(true);
		addFilterItems();
	}


	private void addFilterItems() {
		filters.put("Weapons", 		WrapperForTwo.wrap(item -> {return item instanceof Weapon;}, true));
		filters.put("Armor", 		WrapperForTwo.wrap(item -> {return item instanceof Armor;}, true));
		filters.put("Accesories", 	WrapperForTwo.wrap(item -> {return item instanceof Equipable && !(item instanceof Armor) && !(item instanceof Weapon);}, true));
		filters.put("Materials", 	WrapperForTwo.wrap(item -> {return item instanceof Material;}, true));
		filters.put("Minerals", 	WrapperForTwo.wrap(item -> {return item instanceof Mineral;}, true));
		filters.put("Consumable", 	WrapperForTwo.wrap(item -> {return item instanceof Consumable;}, true));
		filters.put("Containers", 	WrapperForTwo.wrap(item -> {return item instanceof LiquidContainer;}, true));
		filters.put("Furniture", 	WrapperForTwo.wrap(item -> {return item instanceof Furniture;}, true));
		filters.put("Misc", 		WrapperForTwo.wrap(item -> {return item instanceof Misc;}, true));
		filters.put("Seed", 		WrapperForTwo.wrap(item -> {return item instanceof Seed;}, true));
		filters.put("Ammo", 		WrapperForTwo.wrap(item -> {return item.getType() == Category.AMMO;}, true));
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		inventoryListingPanel.leftClick(copy, windowsCopy);
		equippedListingPanel.leftClick(copy, windowsCopy);

		if (filterButton.click()) {
			copy.add(
				new ContextMenu(getMouseScreenX(), getMouseScreenY(), false, filterListItems())
			);
		}
	}


	private MenuItem[] filterListItems() {
		final Collection<MenuItem> menuItems = Lists.newArrayList(Collections2.transform(
			this.filters.entrySet(),
			entry -> {
				final MenuItem menuItem = new MenuItem(
					entry.getKey(),
					() -> {},
					entry.getValue().b ? Color.GREEN : Color.RED,
					entry.getValue().b ? Color.WHITE : Color.WHITE,
					entry.getValue().b ? Color.GREEN : Color.RED,
					null
				);

				menuItem.button.setTask(() -> {
					entry.getValue().b = !entry.getValue().b;
					menuItem.button.setIdleColor(entry.getValue().b ? Color.GREEN : Color.RED);
					menuItem.button.setOverColor(entry.getValue().b ? Color.WHITE : Color.WHITE);
					menuItem.button.setDownColor(entry.getValue().b ? Color.GREEN : Color.RED);
					refresh();
				});

				return menuItem;
			}
		));


		final MenuItem selectAll = new MenuItem(
			"Select all",
			() -> {},
			Color.ORANGE,
			Color.WHITE,
			Color.ORANGE,
			null
		);


		final MenuItem deselectAll = new MenuItem(
			"Deselect all",
			() -> {},
			Color.ORANGE,
			Color.WHITE,
			Color.ORANGE,
			null
		);

		selectAll.button.setTask(() -> {
			filters.values().stream().forEach(value -> {
				value.b = true;
			});

			menuItems.stream().forEach(item -> {
				if (item != deselectAll && item != selectAll) {
					item.button.setIdleColor(Color.GREEN);
					item.button.setOverColor(Color.WHITE);
					item.button.setDownColor(Color.GREEN);
				}
			});
			refresh();
		});

		deselectAll.button.setTask(() -> {
			filters.values().stream().forEach(value -> {
				value.b = false;
			});

			menuItems.stream().forEach(item -> {
				if (item != deselectAll && item != selectAll) {
					item.button.setIdleColor(Color.RED);
					item.button.setOverColor(Color.WHITE);
					item.button.setDownColor(Color.RED);
				}
			});
			refresh();
		});

		menuItems.add(selectAll);
		menuItems.add(deselectAll);
		return Lists.newArrayList(menuItems).toArray(new MenuItem[menuItems.size()]);
	}


	@Override
	public boolean scrolled(int amount) {
		return equippedListingPanel.scrolled(amount) || inventoryListingPanel.scrolled(amount);
	}


	@Override
	public void leftClickReleased() {
		inventoryListingPanel.leftClickReleased();
		equippedListingPanel.leftClickReleased();
	}


	@Override
	protected void internalWindowRender() {
		int lineWidth = 23;

		// Set the position and dimensions of the panel
		inventoryListingPanel.height = height - (equippedItemsToDisplay.isEmpty() ? 0 : (1 + min(5,equippedItemsToDisplay.size())) * lineWidth) - lineWidth * 3;
		inventoryListingPanel.width = width;
		inventoryListingPanel.x = x;
		inventoryListingPanel.y = y - (equippedItemsToDisplay.isEmpty() ? 0 : (1 + min(5,equippedItemsToDisplay.size())) * lineWidth);

		equippedListingPanel.height = 50 + (equippedItemsToDisplay.isEmpty() ? 0 : (1 + min(5, equippedItemsToDisplay.size())) * lineWidth);
		equippedListingPanel.width = width;
		equippedListingPanel.x = x;
		equippedListingPanel.y = y;

		// Render the separator
		renderSeparator();

		// Render the listing panel
		inventoryListingPanel.render();

		if (!equippedItemsToDisplay.isEmpty()) {
			equippedListingPanel.render();
		}

		// Render the weight indication text
		renderCapacityIndicationText(host, this, 6, -height);

		// Render filter button
		filterButton.render(x + 41, y - height + 73, isActive() && UserInterface.contextMenus.isEmpty(), getAlpha());
	}


	/**
	 * Renders the weight display
	 */
	public static void renderCapacityIndicationText(Container container, Window parentComponent, int xOffset, int yOffset) {
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.text);
		Color activeColor;
				
		if (container.getWeightLimited()) {
			activeColor = container.getCurrentLoad() < container.getMaxCapacity() ?
				new Color(0.7f * container.getCurrentLoad()/container.getMaxCapacity(), 1f - 0.7f * container.getCurrentLoad()/container.getMaxCapacity(), 0f, parentComponent.getAlpha()) :
				Colors.modulateAlpha(Color.RED, parentComponent.getAlpha());
		} else {
			activeColor = Color.GREEN;
		}

		Color inactiveColor = container.getCurrentLoad() < container.getMaxCapacity() ?
				new Color(0.7f*container.getCurrentLoad()/container.getMaxCapacity(), 1f - 0.7f * container.getCurrentLoad()/container.getMaxCapacity(), 0f, 0.6f * parentComponent.getAlpha()) :
					Colors.modulateAlpha(Color.RED, 0.6f * parentComponent.getAlpha());

		defaultFont.setColor(parentComponent.isActive() ? activeColor : inactiveColor);
		defaultFont.draw(BloodAndMithrilClient.spriteBatch, parentComponent.truncate("Weight: " + String.format("%.2f", container.getCurrentLoad()) + (container.getWeightLimited() ? "/" + String.format("%.2f", container.getMaxCapacity()) : "")), parentComponent.x + xOffset, parentComponent.y + yOffset + 40);
		defaultFont.draw(BloodAndMithrilClient.spriteBatch, parentComponent.truncate("Volume: " + container.getCurrentVolume() + "/" + container.getMaxVolume()), parentComponent.x + xOffset, parentComponent.y + yOffset + 20);
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
	@SuppressWarnings("unchecked")
	private void buildItems(Map<Item, Integer> equippedItems, Map<Item, Integer> nonEquippedItems, boolean newPanels) {
		populateList(equippedItems, true);
		populateList(nonEquippedItems, false);

		if (newPanels) {
			inventoryListingPanel = new ScrollableListingPanel<Item, Integer>(this, inventorySortingOrder, true, 35) {
				@Override
				protected void populateListings(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
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

			equippedListingPanel = new ScrollableListingPanel<Item, Integer>(this, inventorySortingOrder, true, 35) {
				@Override
				protected void populateListings(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
					listings.add(equippedItemsToDisplay);
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
		} else {
			inventoryListingPanel.getFilters().clear();
			inventoryListingPanel.getFilters().addAll(
				Collections2.transform(
					Collections2.filter(this.filters.entrySet(), toKeep -> {
						return toKeep.getValue().b;
					}),
					value -> {
						return value.getValue().a;
					}
				)
			);

			inventoryListingPanel.refresh(Lists.newArrayList(nonEquippedItemsToDisplay));
			equippedListingPanel.refresh(Lists.newArrayList(equippedItemsToDisplay));
		}
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
		ContextMenu toReturn = null;

		if (item instanceof Consumable) {
			toReturn = consumableMenu(item);
		}

		if (item instanceof LiquidContainer) {
			toReturn = liquidContainerMenu(item);
		}

		if (item instanceof Equipable) {
			toReturn = equippableMenu(item, equipped);
		}

		if (item instanceof Seed) {
			toReturn = seedMenu(item);
		}

		if (item instanceof PropItem) {
			toReturn = placeableMenu((PropItem) item);
		}

		if (toReturn == null) {
			toReturn = new ContextMenu(x, y, true, InventoryItemContextMenuConstructor.showInfo(item));
		}

		if (!equipped) {
			toReturn.addMenuItem(new MenuItem(
				"Discard",
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
										int quantity = 0;
										quantity = Integer.parseInt(args[0].toString());
										ContainerImpl.discard((Individual)host, item, quantity);
										UserInterface.refreshRefreshableWindows();
									} catch (NumberFormatException e) {
										UserInterface.addMessage("Error", "Can not recognise " + args[0].toString() + " as a quantity");
									}
								},
								"Confirm",
								true,
								""
							)
						);
					} else {
						ContainerImpl.discard((Individual)host, item, 1);
						UserInterface.refreshRefreshableWindows();
					}
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			));
		}

		return toReturn;
	}


	private ContextMenu placeableMenu(PropItem item) {
		MenuItem place = new MenuItem(
			"Place",
			() -> {
				Prop prop = item.getProp();
				prop.setWorldId(Domain.getActiveWorld().getWorldId());
				BloodAndMithrilClient.setCursorBoundTask(
					new PlaceCursorBoundTask(prop, (Individual) host, item)
				);
			},
			Colors.UI_GRAY,
			Color.GREEN,
			Color.WHITE,
			null
		);

		return new ContextMenu(x, y,
			true,
			InventoryItemContextMenuConstructor.showInfo(item),
			place
		);
	}


	private ContextMenu seedMenu(Item item) {
		MenuItem plant = new MenuItem(
			"Plant",
			() -> {
				BloodAndMithrilClient.setCursorBoundTask(
					new PlantSeedCursorBoundTask((Seed) item, host)
				);
			},
			Colors.UI_GRAY,
			Color.GREEN,
			Color.WHITE,
			null
		);

		return new ContextMenu(x, y,
			true,
			InventoryItemContextMenuConstructor.showInfo(item),
			plant
		);
	}


	private ContextMenu equippableMenu(final Item item, boolean equipped) {
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
					if (item instanceof Weapon && host instanceof Individual) {
						if (((Individual) host).attacking()) {
							Action action = ((Individual) host).getCurrentAction();
							((Individual) host).setCurrentAction(action.left() ? Action.STAND_LEFT : Action.STAND_RIGHT);
						}
					}
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

		ContextMenu contextMenu = new ContextMenu(x, y,
			true,
			InventoryItemContextMenuConstructor.showInfo(item),
			equipUnequip
		);

		if (item instanceof RangedWeapon && equipped) {
			contextMenu.addMenuItem(
				new MenuItem(
					"Select Ammunition",
					() -> {
					},
					Colors.UI_GRAY,
					Color.GREEN,
					Color.WHITE,
					new ContextMenu(
						getMouseScreenX(),
						getMouseScreenY(),
						true,
						getAmmo((RangedWeapon) item)
					)
				)
			);
		}

		return contextMenu;
	}


	private MenuItem[] getAmmo(RangedWeapon weapon) {
		List<Item> ammo = Lists.newArrayList();
		for (Item item : host.getInventory().keySet()) {
			if  (weapon.canFire(item)) {
				ammo.add(item);
			}
		}

		MenuItem[] menuItems;
		if (ammo.isEmpty()) {
			menuItems = new MenuItem[1];
			menuItems[0] = new MenuItem(
				"No suitable ammunition",
				() -> {},
				Color.GRAY,
				Color.GRAY,
				Color.GRAY,
				null
			);
		} else {
			menuItems = new MenuItem[ammo.size()];
			int i = 0;
			for (Item item : ammo) {
				menuItems[i] = new MenuItem(
					item.getSingular(true),
					() -> {
						if (ClientServerInterface.isServer()) {
							weapon.setAmmo(item);
						} else {
							ClientServerInterface.SendRequest.sendRequestChangeAmmo((Individual) host, weapon, item);
						}
						refresh();
					},
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				);
				i++;
			}
		}

		return menuItems;
	}


	private ContextMenu liquidContainerMenu(final Item item) {
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
									LiquidContainer newContainer = ((LiquidContainer) item).clone();
									newContainer.drinkFrom(amount, (Individual)host);
									host.giveItem(newContainer);
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
			"Discard content",
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
									((LiquidContainer) item).subtract(amount);
								} else {
									ClientServerInterface.SendRequest.sendDiscardLiquidRequest(((Individual)host).getId().getId(), (LiquidContainer) item, amount);
								}
							} catch (NumberFormatException e) {
								UserInterface.addMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
							}
						},
						"Discard content",
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

								for (ListingMenuItem<Item> listItem : equippedItemsToDisplay.keySet()) {
									listItem.button.setIdleColor(Colors.UI_DARK_GRAY);
									listItem.button.setDownColor(Colors.UI_DARK_GRAY);
									listItem.button.setOverColor(Colors.UI_DARK_GRAY);
									listItem.menu = null;
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
											setActive(true);
											LiquidContainer toTransferTo = ((LiquidContainer)listItem.t).clone();
											listItem.menu = null;
											listItem.button.setTask(() -> {
												if (isServer()) {
													LiquidContainer.transfer(
														(Individual) host,
														(LiquidContainer) item,
														toTransferTo,
														amount
													);
													refresh();
												} else {
													ClientServerInterface.SendRequest.sendRequestTransferLiquidBetweenContainers(
														(Individual) host,
														(LiquidContainer) item,
														toTransferTo,
														amount
													);
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

		ContextMenu contextMenu = new ContextMenu(x, y, true,
			InventoryItemContextMenuConstructor.showInfo(item)
		);

		if (host instanceof Individual && !((LiquidContainer)item).isEmpty()) {
			contextMenu.addMenuItem(drink);
			contextMenu.addMenuItem(transferContainerContents);
			contextMenu.addMenuItem(emptyContainerContents);
		}

		return contextMenu;
	}


	private ContextMenu consumableMenu(final Item item) {
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

		ContextMenu contextMenu = new ContextMenu(x, y, true,
			InventoryItemContextMenuConstructor.showInfo(item)
		);

		if (host instanceof Individual) {
			contextMenu.addMenuItem(consume);
		}
		return contextMenu;
	}


	/** Refreshes this {@link InventoryWindow} */
	@Override
	public synchronized void refresh() {
		equippedItemsToDisplay.clear();
		nonEquippedItemsToDisplay.clear();

		buildItems(host.getEquipped(), host.getInventory(), false);
	}


	@Override
	protected void uponClose() {
	}


	private static class InventoryItemContextMenuConstructor {
		private static MenuItem showInfo(final Item item) {
			return new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponentUnique(item.getInfoWindow());
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			);
		}
	}


	@Override
	public Object getUniqueIdentifier() {
		return "inventoryWindow" + ((Individual)host).getId().getId();
	}
}
package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.Item.ItemCategory;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.container.LiquidContainerItem;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.equipment.armor.Armor;
import bloodandmithril.item.items.equipment.offhand.Lantern;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.item.items.furniture.FurnitureItem;
import bloodandmithril.item.items.material.MaterialItem;
import bloodandmithril.item.items.mineral.MineralItem;
import bloodandmithril.item.items.misc.MiscItem;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.liquid.Oil;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.InfoPopup;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.ui.components.panel.TextInputFieldPanel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.cursorboundtask.PlaceCursorBoundTask;
import bloodandmithril.util.cursorboundtask.PlantSeedCursorBoundTask;
import bloodandmithril.util.cursorboundtask.ThrowItemCursorBoundTask;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * {@link Window} to display the inventory of an {@link Individual} or Container
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class InventoryWindow extends Window implements Refreshable {

	@Inject private GameClientStateTracker gameClientStateTracker;

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
	private ScrollableListingPanel<Button, String> filterButtons;

	private boolean refreshSuppressed = false;

	public static Comparator<Item> inventorySortingOrder = new Comparator<Item>() {
		@Override
		public int compare(final Item o1, final Item o2) {
			return ComparisonChain.start()
					.compare(o1.getType().getColor().toIntBits(), o2.getType().getColor().toIntBits())
					.compare(o1.getSingular(false).toUpperCase(), o2.getSingular(false).toUpperCase())
					.result();
		}
	};

	/** The text search predicate */
	private String searchString = "";
	private final Predicate<Item> textSearch = new Predicate<Item>() {
		@Override
		public boolean apply(final Item input) {
			return input.getSingular(true).toUpperCase().contains(searchString.toUpperCase());
		}
	};

	/** Input for text searching */
	private final TextInputFieldPanel textInput = new TextInputFieldPanel(this, "");

	/**
	 * Overloaded constructor - with default colors
	 */
	public InventoryWindow(
			final Equipper host,
			final String title,
			final boolean active) {
		super(
			600,
			500,
			title,
			active,
			600,
			450,
			true,
			true,
			true
		);
		this.host = host;
		buildItems(host.getEquipped(), host.getInventory(), true);
		inventoryListingPanel.setScrollWheelActive(true);
		filterButtons = setupFilters(filters, this).canScroll(false);
	}


	public static ScrollableListingPanel<Button, String> setupFilters(final Map<String, WrapperForTwo<Predicate<Item>, Boolean>> filters, final Component parent) {
		filters.put("Weapons", 		WrapperForTwo.wrap(item -> {return item instanceof Weapon;}, true));
		filters.put("Armor", 		WrapperForTwo.wrap(item -> {return item instanceof Armor;}, true));
		filters.put("Accesories", 	WrapperForTwo.wrap(item -> {return item instanceof Equipable && !(item instanceof Armor) && !(item instanceof Weapon);}, true));
		filters.put("Materials", 	WrapperForTwo.wrap(item -> {return item instanceof MaterialItem;}, true));
		filters.put("Minerals", 	WrapperForTwo.wrap(item -> {return item instanceof MineralItem;}, true));
		filters.put("Consumable", 	WrapperForTwo.wrap(item -> {return item instanceof Consumable;}, true));
		filters.put("Containers", 	WrapperForTwo.wrap(item -> {return item instanceof LiquidContainerItem;}, true));
		filters.put("Furniture", 	WrapperForTwo.wrap(item -> {return item instanceof FurnitureItem;}, true));
		filters.put("Misc", 		WrapperForTwo.wrap(item -> {return item instanceof MiscItem;}, true));
		filters.put("Seed", 		WrapperForTwo.wrap(item -> {return item instanceof SeedItem;}, true));
		filters.put("Ammo", 		WrapperForTwo.wrap(item -> {return item.getType() == ItemCategory.AMMO;}, true));
		filters.put("Off-hand",		WrapperForTwo.wrap(item -> {return item.getType() == ItemCategory.OFFHAND;}, true));

		return new ScrollableListingPanel<Button, String>(parent, (b1, b2) -> {
			return b1.text.call().compareTo(b2.text.call());
		}, false, 0, null) {
			@Override
			protected String getExtraString(final Entry<ListingMenuItem<Button>, String> item) {
				return "";
			}
			@Override
			protected int getExtraStringOffset() {
				return 0;
			}
			@Override
			protected void populateListings(final List<HashMap<ListingMenuItem<Button>, String>> listings) {
				final HashMap<ListingMenuItem<Button>, String> map = Maps.newHashMap();
				final Button[] filterButtonsArray = filterButtons(filters, (Refreshable) parent);
				for (final Button button : filterButtonsArray) {
					map.put(
						new ListingMenuItem<Button>(button, button, null),
						""
					);
				}

				final HashMap<ListingMenuItem<Button>, String> map2 = Maps.newHashMap();
				for (final Button button : filterAllButtons(Lists.newArrayList(filterButtonsArray), filters, (Refreshable) parent)) {
					map2.put(
						new ListingMenuItem<Button>(button, button, null),
						""
					);
				}

				listings.add(map);
				listings.add(map2);
			}
			@Override
			public boolean keyPressed(final int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		inventoryListingPanel.leftClick(copy, windowsCopy);
		equippedListingPanel.leftClick(copy, windowsCopy);
		filterButtons.leftClick(copy, windowsCopy);
	}

	private static Button[] filterAllButtons(final Collection<Button> filterButtons, final Map<String, WrapperForTwo<Predicate<Item>, Boolean>> filters, final Refreshable refreshable) {
		final Collection<Button> buttons = Lists.newArrayList();

		final Button selectAll = new Button(
				"Select all",
				defaultFont,
				0,
				0,
				"Select all".length() * 10,
				16,
				() -> {},
				Color.ORANGE,
				Color.WHITE,
				Color.ORANGE,
				UIRef.BL
			);

			final Button deselectAll = new Button(
				"Deselect all",
				defaultFont,
				0,
				0,
				"Deselect all".length() * 10,
				16,
				() -> {},
				Color.ORANGE,
				Color.WHITE,
				Color.ORANGE,
				UIRef.BL
			);

			selectAll.setTask(() -> {
				filters.values().stream().forEach(value -> {
					value.b = true;
				});

				filterButtons.stream().forEach(item -> {
					if (item != deselectAll && item != selectAll) {
						item.setIdleColor(Color.GREEN);
						item.setOverColor(Color.WHITE);
						item.setDownColor(Color.GREEN);
					}
				});
				refreshable.refresh();
			});

			deselectAll.setTask(() -> {
				filters.values().stream().forEach(value -> {
					value.b = false;
				});

				filterButtons.stream().forEach(item -> {
					if (item != deselectAll && item != selectAll) {
						item.setIdleColor(Color.RED);
						item.setOverColor(Color.WHITE);
						item.setDownColor(Color.RED);
					}
				});
				refreshable.refresh();
			});

			buttons.add(selectAll);
			buttons.add(deselectAll);

			return Lists.newArrayList(buttons).toArray(new Button[buttons.size()]);
	}

	private static Button[] filterButtons(final Map<String, WrapperForTwo<Predicate<Item>, Boolean>> filters, final Refreshable refreshable) {
		final Collection<Button> buttons = Lists.newArrayList(Collections2.transform(
			filters.entrySet(),
			entry -> {
				final Button button = new Button(
					entry.getKey(),
					defaultFont,
					0,
					0,
					entry.getKey().length() * 10,
					16,
					() -> {},
					entry.getValue().b ? Color.GREEN : Color.RED,
					entry.getValue().b ? Color.WHITE : Color.WHITE,
					entry.getValue().b ? Color.GREEN : Color.RED,
					UIRef.BL
				);

				button.setTask(() -> {
					entry.getValue().b = !entry.getValue().b;
					button.setIdleColor(entry.getValue().b ? Color.GREEN : Color.RED);
					button.setOverColor(entry.getValue().b ? Color.WHITE : Color.WHITE);
					button.setDownColor(entry.getValue().b ? Color.GREEN : Color.RED);
					refreshable.refresh();
				});

				return button;
			}
		));

		return Lists.newArrayList(buttons).toArray(new Button[buttons.size()]);
	}


	@Override
	public boolean scrolled(final int amount) {
		return equippedListingPanel.scrolled(amount) || inventoryListingPanel.scrolled(amount) || filterButtons.scrolled(amount);
	}


	@Override
	public void leftClickReleased() {
		inventoryListingPanel.leftClickReleased();
		equippedListingPanel.leftClickReleased();
		filterButtons.leftClickReleased();
	}


	@Override
	protected synchronized void internalWindowRender(final Graphics graphics) {
		if (host instanceof Individual && !((Individual) host).isAlive()) {
			setClosing(true);
		}

		final int lineWidth = 23;

		// Set the position and dimensions of the panel
		inventoryListingPanel.height = height - (equippedItemsToDisplay.isEmpty() ? 0 : (1 + min(5,equippedItemsToDisplay.size())) * lineWidth) - lineWidth * 3;
		inventoryListingPanel.width = width - 180;
		inventoryListingPanel.x = x + 180;
		inventoryListingPanel.y = y - (equippedItemsToDisplay.isEmpty() ? 0 : (1 + min(5,equippedItemsToDisplay.size())) * lineWidth);

		equippedListingPanel.height = 50 + (equippedItemsToDisplay.isEmpty() ? 0 : (1 + min(5, equippedItemsToDisplay.size())) * lineWidth);
		equippedListingPanel.width = width - 180;
		equippedListingPanel.x = x + 180;
		equippedListingPanel.y = y;

		filterButtons.height = height;
		filterButtons.width = 180;
		filterButtons.x = x;
		filterButtons.y = y;

		// Render the separator
		renderSeparator(x + width - 88, graphics);
		renderSeparator(x + 170, graphics);

		// Render the listing panel
		inventoryListingPanel.render(graphics);
		filterButtons.render(graphics);

		if (!equippedItemsToDisplay.isEmpty()) {
			equippedListingPanel.render(graphics);
		}

		// Render the text search
		textInput.x = x + 6;
		textInput.y = y - height + 70;
		textInput.width = 150;
		textInput.height = 20;
		textInput.render(graphics);

		// Render the weight indication text
		renderCapacityIndicationText(host, this, 6, -height, "", "", graphics);
	}


	@Override
	public boolean keyPressed(final int keyCode) {
		final boolean keyPressed = textInput.keyPressed(keyCode);
		this.searchString = textInput.getInputText();
		refresh();

		return super.keyPressed(keyCode) || keyPressed;
	}


	/**
	 * Renders the weight display
	 */
	public static void renderCapacityIndicationText(final Container container, final Window parentComponent, final int xOffset, final int yOffset, final String extra1, final String extra2, final Graphics graphics) {
		graphics.getSpriteBatch().setShader(Shaders.text);
		Color activeColor;

		if (container.getWeightLimited()) {
			activeColor = container.getCurrentLoad() < container.getMaxCapacity() ?
				new Color(0.7f * container.getCurrentLoad()/container.getMaxCapacity(), 1f - 0.7f * container.getCurrentLoad()/container.getMaxCapacity(), 0f, parentComponent.getAlpha()) :
				Colors.modulateAlpha(Color.RED, parentComponent.getAlpha());
		} else {
			activeColor = Colors.modulateAlpha(Color.GREEN, parentComponent.getAlpha());
		}

		final Color inactiveColor = container.getCurrentLoad() < container.getMaxCapacity() ?
				new Color(0.7f*container.getCurrentLoad()/container.getMaxCapacity(), 1f - 0.7f * container.getCurrentLoad()/container.getMaxCapacity(), 0f, 0.6f * parentComponent.getAlpha()) :
					Colors.modulateAlpha(Color.RED, 0.6f * parentComponent.getAlpha());

		defaultFont.setColor(parentComponent.isActive() ? activeColor : inactiveColor);
		defaultFont.draw(graphics.getSpriteBatch(), parentComponent.truncate("Weight: " + String.format("%.2f", container.getCurrentLoad()) + (container.getWeightLimited() ? "/" + String.format("%.2f", container.getMaxCapacity()) : "") + extra1), parentComponent.x + xOffset, parentComponent.y + yOffset + 40);
		defaultFont.draw(graphics.getSpriteBatch(), parentComponent.truncate("Volume: " + container.getCurrentVolume() + "/" + container.getMaxVolume() + extra2), parentComponent.x + xOffset, parentComponent.y + yOffset + 20);
	}


	/**
	 * Renders the separator that separates the item listing from the quantity listing
	 */
	private void renderSeparator(final int xCoord, final Graphics graphics) {
		graphics.getSpriteBatch().setShader(Shaders.filter);
		shapeRenderer.begin(ShapeType.Filled);
		final Color color = isActive() ? Colors.modulateAlpha(borderColor, getAlpha()) : Colors.modulateAlpha(borderColor, 0.4f * getAlpha());
		shapeRenderer.rect(xCoord, y + 24 - height, 2, height - 45, Color.CLEAR, Color.CLEAR, color, color);
		shapeRenderer.end();
	}


	/**
	 * Builds the list of items to display
	 */
	@SuppressWarnings("unchecked")
	private void buildItems(final Map<Item, Integer> equippedItems, final Map<Item, Integer> nonEquippedItems, final boolean newPanels) {
		populateList(equippedItems, true);
		populateList(nonEquippedItems, false);

		if (newPanels) {
			inventoryListingPanel = new ScrollableListingPanel<Item, Integer>(this, inventorySortingOrder, true, 35, textSearch) {
				@Override
				protected void populateListings(final List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
					listings.add(nonEquippedItemsToDisplay);
				}

				@Override
				protected int getExtraStringOffset() {
					return 80;
				}

				@Override
				protected String getExtraString(final Entry<ListingMenuItem<Item>, Integer> item) {
					return Integer.toString(item.getValue());
				}

				@Override
				public boolean keyPressed(final int keyCode) {
					return false;
				}
			};

			equippedListingPanel = new ScrollableListingPanel<Item, Integer>(this, inventorySortingOrder, false, 35, null) {
				@Override
				protected void populateListings(final List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
					listings.add(equippedItemsToDisplay);
				}

				@Override
				protected int getExtraStringOffset() {
					return 80;
				}

				@Override
				protected String getExtraString(final Entry<ListingMenuItem<Item>, Integer> item) {
					return Integer.toString(item.getValue());
				}

				@Override
				public boolean keyPressed(final int keyCode) {
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
	private void populateList(final Map<Item, Integer> listToPopulate, final boolean eq) {
		for(final Entry<Item, Integer> item : listToPopulate.entrySet()) {

			final ContextMenu menuToAddUnequipped = determineMenu(item.getKey(), false);

			final Button inventoryButton = new Button(
				() -> {return item.getKey().getSingular(true);},
				defaultFont,
				0,
				0,
				item.getKey().getSingular(true).length() * 10,
				16,
				() -> {
					menuToAddUnequipped.x = getMouseScreenX();
					menuToAddUnequipped.y = getMouseScreenY();
				},
				eq ? Color.GREEN : item.getKey().getType().getColor(),
				Color.GREEN,
				Color.WHITE,
				UIRef.BL
			);

			inventoryButton.mouseOverPopup(
				() -> {
					return new InfoPopup(
						item.getKey().getInfoPanel(),
						() -> {
							return !inventoryButton.isMouseOver() || !isActive();
						}
					);
				}
			);

			final ContextMenu menuToAddEquipped = determineMenu(item.getKey(), true);
			final Button equippedButton = new Button(
				() -> {return item.getKey().getSingular(true);},
				defaultFont,
				0,
				0,
				item.getKey().getSingular(true).length() * 10,
				16,
				() -> {
					menuToAddEquipped.x = getMouseScreenX();
					menuToAddEquipped.y = getMouseScreenY();
				},
				eq ? Color.GREEN : Color.ORANGE,
				Color.GREEN,
				Color.WHITE,
				UIRef.BL
			);

			equippedButton.mouseOverPopup(
				() -> {
					return new InfoPopup(
						item.getKey().getInfoPanel(),
						() -> {
							return !equippedButton.isMouseOver() || !isActive();
						}
					);
				}
			);


			if (eq) {
				this.equippedItemsToDisplay.put(
					new ListingMenuItem<Item>(item.getKey(), equippedButton, () -> { return menuToAddEquipped;}),
					item.getValue()
				);
			} else {
				this.nonEquippedItemsToDisplay.put(
					new ListingMenuItem<Item>(item.getKey(), inventoryButton, () -> { return menuToAddUnequipped;}),
					item.getValue()
				);
			}

			minLength = minLength < inventoryButton.width + 100 ? inventoryButton.width + 100 : minLength;
			this.width = width < minLength ? minLength : width;
		}
	}


	/** Determines which context menu to use */
	private ContextMenu determineMenu(final Item item, final boolean equipped) {
		ContextMenu toReturn = null;

		if (item instanceof Consumable) {
			toReturn = consumableMenu(item);
		}

		if (item instanceof LiquidContainerItem) {
			toReturn = liquidContainerMenu(item);
		}

		if (item instanceof Equipable) {
			toReturn = equippableMenu(item, equipped);
		}

		if (item instanceof SeedItem) {
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
					if (isKeyPressed(Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).getKeyMappings().bulkDiscard.keyCode)) {
						UserInterface.addLayeredComponent(
							new TextInputWindow(
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
									} catch (final NumberFormatException e) {
										UserInterface.addGlobalMessage("Error", "Can not recognise " + args[0].toString() + " as a quantity");
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

		if (host instanceof Individual && item.throwable()) {
			toReturn.addMenuItem(new MenuItem(
				"Throw",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(new ThrowItemCursorBoundTask(item, (Individual) host));
				},
				Colors.UI_GRAY,
				Color.GREEN,
				Color.WHITE,
				null
			));
		}

		return toReturn;
	}


	private ContextMenu placeableMenu(final PropItem item) {
		final MenuItem place = new MenuItem(
			"Place",
			() -> {
				final Prop prop = item.getProp();
				prop.setWorldId(gameClientStateTracker.getActiveWorld().getWorldId());
				Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
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


	private ContextMenu seedMenu(final Item item) {
		final MenuItem plant = new MenuItem(
			"Plant",
			() -> {
				Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
					new PlantSeedCursorBoundTask((SeedItem) item, (Individual) host, null)
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


	private ContextMenu equippableMenu(final Item item, final boolean equipped) {
		final MenuItem equipUnequip = equipped ? new MenuItem(
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
							final Action action = ((Individual) host).getCurrentAction();
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

		final ContextMenu contextMenu = new ContextMenu(x, y,
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
					() -> { return new ContextMenu(
						getMouseScreenX(),
						getMouseScreenY(),
						true,
						getAmmo((RangedWeapon) item)
					);}
				)
			);
		}

		if (item instanceof Lantern) {
			contextMenu.addMenuItem(
				new MenuItem(
					"Refuel",
					() -> {
						UserInterface.addLayeredComponent(
							new TextInputWindow(
								250,
								100,
								"Amount",
								250,
								100,
								args -> {
									try {
										final float amount = Util.round2dp(Float.parseFloat(args[0].toString()));
										if (amount < 0.01f) {
											UserInterface.addGlobalMessage("Too little to refuel", "Its too little to refuel, enter a larger amount");
											return;
										}

										refreshSuppressed = true;

										for (final ListingMenuItem<Item> listItem : equippedItemsToDisplay.keySet()) {
											listItem.button.setIdleColor(Colors.UI_DARK_GRAY);
											listItem.button.setDownColor(Colors.UI_DARK_GRAY);
											listItem.button.setOverColor(Colors.UI_DARK_GRAY);
											listItem.menu = null;
										}

										for (final ListingMenuItem<Item> listItem : nonEquippedItemsToDisplay.keySet()) {
											if (listItem.t instanceof LiquidContainerItem) {

												listItem.button.setIdleColor(Color.ORANGE);
												setActive(true);
												listItem.menu = null;
												listItem.button.setTask(() -> {
													if (isServer()) {
														host.takeItem(listItem.t);

														final Item copy = listItem.t.copy();
														final Map<Class<? extends Liquid>, Float> subtracted = ((LiquidContainerItem) copy).subtract(amount);
														if (subtracted.containsKey(Oil.class)) {
															final float oil = subtracted.get(Oil.class);
															((Lantern) item).addFuel(oil);
														}

														host.giveItem(copy);
													} else {
														// CSI
													}

													refreshSuppressed = false;
													refresh();
												});
											} else {
												listItem.button.setIdleColor(Colors.UI_DARK_GRAY);
												listItem.button.setDownColor(Colors.UI_DARK_GRAY);
												listItem.button.setOverColor(Colors.UI_DARK_GRAY);
												listItem.menu = null;
											}
										}

									} catch (final NumberFormatException e) {
										UserInterface.addGlobalMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
									}
								},
								"Refuel",
								true,
								""
							)
						);
					},
					Colors.UI_GRAY,
					Color.GREEN,
					Color.WHITE,
					null
				)
			);
		}

		return contextMenu;
	}


	private MenuItem[] getAmmo(final RangedWeapon weapon) {
		final List<Item> ammo = Lists.newArrayList();
		for (final Item item : host.getInventory().keySet()) {
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
			for (final Item item : ammo) {
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
		final MenuItem drink = new MenuItem(
			"Drink from",
			() -> {
				UserInterface.addLayeredComponent(
					new TextInputWindow(
						250,
						100,
						"Amount",
						250,
						100,
						args -> {
							try {
								final float amount = Float.parseFloat(String.format("%.2f", Float.parseFloat(args[0].toString())));

								if (amount < 0.01f) {
									UserInterface.addGlobalMessage("Too little to drink", "It would be a waste of time to drink this little, enter a larger amount");
									return;
								}

								if (ClientServerInterface.isServer()) {
									host.takeItem(item);
									final LiquidContainerItem newContainer = ((LiquidContainerItem) item).clone();
									newContainer.drinkFrom(amount, (Individual)host);
									host.giveItem(newContainer);
									refresh();
								} else {
									ClientServerInterface.SendRequest.sendDrinkLiquidRequest(((Individual)host).getId().getId(), (LiquidContainerItem)item, Float.parseFloat((String)args[0]));
								}
							} catch (final NumberFormatException e) {
								UserInterface.addGlobalMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
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

		final MenuItem emptyContainerContents = new MenuItem(
			"Discard content",
			() -> {
				UserInterface.addLayeredComponent(
					new TextInputWindow(
						250,
						100,
						"Amount",
						250,
						100,
						args -> {
							try {
								final float amount = Util.round2dp(Float.parseFloat(args[0].toString()));

								if (amount < 0.01f) {
									UserInterface.addGlobalMessage("Too little to discard", "Its too little to discard, enter a larger amount");
									return;
								}

								if (ClientServerInterface.isServer()) {
									((LiquidContainerItem) item).subtract(amount);
								} else {
									ClientServerInterface.SendRequest.sendDiscardLiquidRequest(((Individual)host).getId().getId(), (LiquidContainerItem) item, amount);
								}
							} catch (final NumberFormatException e) {
								UserInterface.addGlobalMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
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

		final MenuItem transferContainerContents = new MenuItem(
			"Transfer",
			() -> {
				UserInterface.addLayeredComponent(
					new TextInputWindow(
						250,
						100,
						"Amount",
						250,
						100,
						args -> {
							try {
								final float amount = Util.round2dp(Float.parseFloat(args[0].toString()));
								if (amount < 0.01f) {
									UserInterface.addGlobalMessage("Too little to transfer", "Its too little to transfer, enter a larger amount");
									return;
								}

								refreshSuppressed = true;

								for (final ListingMenuItem<Item> listItem : equippedItemsToDisplay.keySet()) {
									listItem.button.setIdleColor(Colors.UI_DARK_GRAY);
									listItem.button.setDownColor(Colors.UI_DARK_GRAY);
									listItem.button.setOverColor(Colors.UI_DARK_GRAY);
									listItem.menu = null;
								}
								for (final ListingMenuItem<Item> listItem : nonEquippedItemsToDisplay.keySet()) {
									if (listItem.t instanceof LiquidContainerItem) {

										if (listItem.t == item) {
											listItem.button.setIdleColor(Colors.UI_DARK_GREEN);
											listItem.button.setDownColor(Colors.UI_DARK_GREEN);
											listItem.button.setOverColor(Colors.UI_DARK_GREEN);
											listItem.menu = null;
										} else {
											listItem.button.setIdleColor(Color.ORANGE);
											setActive(true);
											final LiquidContainerItem toTransferTo = ((LiquidContainerItem)listItem.t).clone();
											listItem.menu = null;
											listItem.button.setTask(() -> {
												if (isServer()) {
													LiquidContainerItem.transfer(
														(Individual) host,
														(LiquidContainerItem) item,
														toTransferTo,
														amount
													);
													refresh();
												} else {
													ClientServerInterface.SendRequest.sendRequestTransferLiquidBetweenContainers(
														(Individual) host,
														(LiquidContainerItem) item,
														toTransferTo,
														amount
													);
												}

												refreshSuppressed = false;
											});
										}
									} else {
										listItem.button.setIdleColor(Colors.UI_DARK_GRAY);
										listItem.button.setDownColor(Colors.UI_DARK_GRAY);
										listItem.button.setOverColor(Colors.UI_DARK_GRAY);
										listItem.menu = null;
									}
								}

							} catch (final NumberFormatException e) {
								UserInterface.addGlobalMessage("Error", "Cannot recognise " + args[0].toString() + " as an amount.");
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

		final ContextMenu contextMenu = new ContextMenu(x, y, true,
			InventoryItemContextMenuConstructor.showInfo(item)
		);

		if (host instanceof Individual && !((LiquidContainerItem)item).isEmpty()) {
			contextMenu.addMenuItem(drink);
			contextMenu.addMenuItem(transferContainerContents);
			contextMenu.addMenuItem(emptyContainerContents);
		}

		return contextMenu;
	}


	private ContextMenu consumableMenu(final Item item) {
		final MenuItem consume = new MenuItem(
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

		final ContextMenu contextMenu = new ContextMenu(x, y, true,
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
		if (refreshSuppressed) {
			return;
		}

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
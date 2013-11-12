package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Container;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper;
import bloodandmithril.item.Item;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * {@link Window} to display the inventory of an {@link Individual} or Container
 *
 * @author Matt
 */
public class InventoryWindow extends Window {

	/** The list of items this inventory window displays, equipped items first. */
	public Map<InventoryWindowItem, Integer> equippedItemsToDisplay = new TreeMap<InventoryWindowItem, Integer>();
	public Map<InventoryWindowItem, Integer> nonEquippedItemsToDisplay = new TreeMap<InventoryWindowItem, Integer>();

	/** The {@link Container} that is the host of this {@link InventoryWindow} */
	public Equipper host;

	/** The current starting index for which the inventory listing is rendered */
	private int startingIndex = 0;

	/** The position of the scroll bar button, 0f is top of list, 1f is bottom of list */
	private float scrollBarButtonLocation = 0f;

	/** Used for scroll processing */
	private Float scrollBarButtonLocationOld = null;
	private float mouseLocYFrozen;

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
		for(Entry<InventoryWindowItem, Integer> item : equippedItemsToDisplay.entrySet()) {
			if (item.getKey().button.click() && item.getKey().menu == null) {
				copy.clear();
			}
			if (item.getKey().menu != null && item.getKey().button.isMouseOver()) {
				copy.add(item.getKey().menu);
			}
		}
		for(Entry<InventoryWindowItem, Integer> item : nonEquippedItemsToDisplay.entrySet()) {
			if (item.getKey().button.click() && item.getKey().menu == null) {
				copy.clear();
			}
			if (item.getKey().menu != null && item.getKey().button.isMouseOver()) {
				copy.add(item.getKey().menu);
			}
		}


		float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;
		if (Fortress.getMouseScreenX() > x + length - 13 &&
			Fortress.getMouseScreenX() < x + length + 4 &&
			Fortress.getMouseScreenY() > scrollBarButtonPos - 5 &&
			Fortress.getMouseScreenY() < scrollBarButtonPos + 12) {

			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * (equippedItemsToDisplay.size() + nonEquippedItemsToDisplay.size()));
			scrollBarButtonLocationOld = scrollBarButtonLocation;
			mouseLocYFrozen = Fortress.getMouseScreenY();
		}
	}


	@Override
	public void leftClickReleased() {
		scrollBarButtonLocationOld = null;
	}


	@Override
	protected void internalWindowRender() {
		// Render the separator
		renderSeparator();

		// Render the scroll bar
		renderScrollBar();

		// Render the scroll button
		renderScrollBarButton();

		// Renders the inventory listing
		renderInventoryItems();

		// Render the weight indication text
		renderWeightIndicationText();
	}


	/**
	 * Renders the scroll bar button
	 */
	private void renderScrollBarButton() {
		float scrollBarButtonPos = y - 50 - (height - 102) * scrollBarButtonLocation;

		if (Gdx.input.isButtonPressed(KeyMappings.leftClick) && scrollBarButtonLocationOld != null) {
			scrollBarButtonLocation = Math.min(1, Math.max(0, scrollBarButtonLocationOld + (mouseLocYFrozen - Fortress.getMouseScreenY())/(height - 102)));
			startingIndex = Math.round((y - 50 - scrollBarButtonPos)/(height - 102) * (equippedItemsToDisplay.size() + nonEquippedItemsToDisplay.size()));
		}

		if (active) {
			if (Fortress.getMouseScreenX() > x + length - 13 &&
					Fortress.getMouseScreenX() < x + length + 4 &&
					Fortress.getMouseScreenY() > scrollBarButtonPos - 5 &&
					Fortress.getMouseScreenY() < scrollBarButtonPos + 12 ||
					scrollBarButtonLocationOld != null) {
				shapeRenderer.setColor(0f, 1f, 0f, alpha);
			} else {
				shapeRenderer.setColor(1f, 1f, 1f, alpha);
			}
		} else {
			shapeRenderer.setColor(0.5f, 0.5f, 0.5f, alpha);
		}

		shapeRenderer.filledRect(x + length - 8, scrollBarButtonPos, 7, 7);
		shapeRenderer.end();
	}


	/**
	 * Renders the inventory listing
	 */
	private void renderInventoryItems() {
		// Render the equipped items first
		int i = 0;
		for(Entry<InventoryWindowItem, Integer> item : equippedItemsToDisplay.entrySet()) {
			if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
				i++;
				continue;
			}
			if (y - (i - (startingIndex == 0 ? 1 : startingIndex)) * 20 - 110 < y - height) {
				defaultFont.draw(Fortress.spriteBatch, "...", x + 6, y - (i - (startingIndex == 0 ? 1 : startingIndex) + 1) * 20 - 33);
				break;
			}
			item.getKey().button.render(x + item.getKey().button.width/2 + 6, y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 25, active && UserInterface.contextMenus.isEmpty(), alpha);
			defaultFont.draw(Fortress.spriteBatch, Integer.toString(item.getValue()), x + length - 80, y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 33);
			i++;
		}

		// Render the non-equipped items
		for(Entry<InventoryWindowItem, Integer> item : nonEquippedItemsToDisplay.entrySet()) {
			if (i + 1 < (startingIndex == 0 ? 1 : startingIndex)) {
				i++;
				continue;
			}
			if (y - (i - (startingIndex == 0 ? 1 : startingIndex)) * 20 - 110 < y - height) {
				defaultFont.draw(Fortress.spriteBatch, "...", x + 6, y - (i - (startingIndex == 0 ? 1 : startingIndex) + 1) * 20 - 33);
				break;
			}
			item.getKey().button.render(x + item.getKey().button.width/2 + 6, y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 25, active && UserInterface.contextMenus.isEmpty(), alpha);
			defaultFont.draw(Fortress.spriteBatch, Integer.toString(item.getValue()), x + length - 80, y - (i - startingIndex + (startingIndex == 0 ? 0 : 1)) * 20 - 33);
			i++;
		}
	}


	/**
	 * Renders the weight display
	 */
	private void renderWeightIndicationText() {
		Color activeColor = host.getCurrentLoad() < host.getMaxCapacity() ?
				new Color(0.7f*host.getCurrentLoad()/host.getMaxCapacity(), 1f - 0.7f * host.getCurrentLoad()/host.getMaxCapacity(), 0f, alpha) :
				new Color(1f, 0f, 0f, alpha);

		Color inactiveColor = host.getCurrentLoad() < host.getMaxCapacity() ?
				new Color(0.7f*host.getCurrentLoad()/host.getMaxCapacity(), 1f - 0.7f * host.getCurrentLoad()/host.getMaxCapacity(), 0f, 0.6f * alpha) :
				new Color(1f, 0f, 0f, 0.6f * alpha);

		defaultFont.setColor(active ? activeColor : inactiveColor);
		defaultFont.draw(Fortress.spriteBatch, truncate("Weight: " + String.format("%.2f", host.getCurrentLoad()) + "/" + String.format("%.2f", host.getMaxCapacity())), x + 6, y - height + 20);
	}


	/**
	 * Renders the separator that separates the item listing from the quantity listing
	 */
	private void renderSeparator() {
		Fortress.spriteBatch.setShader(Shaders.filter);
		shapeRenderer.begin(ShapeType.FilledRectangle);
		Color color = active ? new Color(borderColor.r, borderColor.g, borderColor.b, alpha) : new Color(borderColor.r, borderColor.g, borderColor.b, borderColor.a * 0.4f * alpha);
		shapeRenderer.filledRect(x + length - 88, y + 24 - height, 2, height - 45, Color.CLEAR, Color.CLEAR, color, color);
	}


	/**
	 * Renders the scroll bar
	 */
	private void renderScrollBar() {
		Color scrollBarColor = active ? new Color(borderColor.r, borderColor.g, borderColor.b, alpha * 0.5f) : new Color(borderColor.r, borderColor.g, borderColor.b, borderColor.a * 0.2f * alpha);
		shapeRenderer.setColor(scrollBarColor);
		shapeRenderer.filledRect(x + length - 6, y - 50, 3, 30, scrollBarColor, scrollBarColor, Color.CLEAR, Color.CLEAR);
		shapeRenderer.filledRect(x + length - 6, y + 52 - height, 3, height - 102);
		shapeRenderer.filledRect(x + length - 6, y + 22 - height, 3, 30, Color.CLEAR, Color.CLEAR, scrollBarColor, scrollBarColor);
	}


	/**
	 * Builds the list of items to display
	 */
	private void buildItems(HashMap<Item, Integer> equippedItemsToDisplay, HashMap<Item, Integer> nonEquippedItemsToDisplay) {
		populateList(equippedItemsToDisplay, true);
		populateList(nonEquippedItemsToDisplay, false);
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
						menuToAddUnequipped.x = Fortress.getMouseScreenX();
						menuToAddUnequipped.y = Fortress.getMouseScreenY();
					}
				},
				eq ? new Color(0f, 0.6f, 0f, 1f) : new Color(0.8f, 0.8f, 0.8f, 1f),
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
						menuToAddEquipped.x = Fortress.getMouseScreenX();
						menuToAddEquipped.y = Fortress.getMouseScreenY();
					}
				},
				eq ? new Color(0f, 0.6f, 0f, 1f) : new Color(0.8f, 0.8f, 0.8f, 1f),
				Color.GREEN,
				Color.WHITE,
				UIRef.BL
			);

			if (eq) {
				this.equippedItemsToDisplay.put(
					new InventoryWindowItem(item.getKey(), equippedButton, menuToAddEquipped),
					item.getValue()
				);
			} else {
				this.nonEquippedItemsToDisplay.put(
					new InventoryWindowItem(item.getKey(), inventoryButton, menuToAddUnequipped),
					item.getValue()
				);
			}

			minLength = minLength < inventoryButton.width + 100 ? inventoryButton.width + 100 : minLength;
		}
	}


	/** Determines which context menu to use */
	private ContextMenu determineMenu(final Item item, boolean equipped) {
		if (item instanceof Consumable) {
			return new ContextMenu(x, y,
				new ContextMenuItem(
					"Show info",
					new Task() {
						@Override
						public void execute() {
							UserInterface.addLayeredComponent(item.getInfoWindow());
						}
					},
					new Color(0.8f, 0.8f, 0.8f, 1f),
					Color.GREEN,
					Color.WHITE,
					null
				),

				new ContextMenuItem(
					"Consume",
					new Task() {
						@Override
						public void execute() {
							if (item instanceof Consumable && host instanceof Individual && ((Consumable)item).consume((Individual)host)) {
								host.takeItem(item, 1);
							}
							refresh();
						}
					},
					new Color(0.8f, 0.8f, 0.8f, 1f),
					Color.GREEN,
					Color.WHITE,
					null
				)
			);
		}

		if (item instanceof Equipable) {

			return new ContextMenu(x, y,
				new ContextMenuItem(
					"Show info",
					new Task() {
						@Override
						public void execute() {
							UserInterface.addLayeredComponent(item.getInfoWindow());
						}
					},
					new Color(0.8f, 0.8f, 0.8f, 1f),
					Color.GREEN,
					Color.WHITE,
					null
				),

				equipped ? new ContextMenuItem(
					"Unequip",
					new Task() {
						@Override
						public void execute() {
							host.unequip((Equipable)item);
							refresh();
						}
					},
					new Color(0.8f, 0.8f, 0.8f, 1f),
					Color.GREEN,
					Color.WHITE,
					null
				) :

				new ContextMenuItem(
					"Equip",
					new Task() {
						@Override
						public void execute() {
							host.equip((Equipable)item);
							refresh();
						}
					},
					new Color(0.8f, 0.8f, 0.8f, 1f),
					Color.GREEN,
					Color.WHITE,
					null
				)
			);
		}

		return null;
	}


	/** Refreshes this {@link InventoryWindow} */
	public void refresh() {
		equippedItemsToDisplay.clear();
		nonEquippedItemsToDisplay.clear();
		buildItems(host.getEquipped(), host.getInventory());
	}


	/**
	 * An item to be displayed within this {@link InventoryWindow}
	 *
	 * @author Matt
	 */
	public static class InventoryWindowItem extends ContextMenuItem implements Comparable<InventoryWindowItem> {

		public Item item;

		public InventoryWindowItem(Item item, Button button, ContextMenu menu) {
			super(button, menu);
			this.item = item;
		}

		@Override
		public int compareTo(InventoryWindowItem o) {
			if (item.value == o.item.value) {
				return item.getClass().getSimpleName().compareTo(o.item.getClass().getSimpleName());
			} else {
				return item.value > o.item.value ? 1 : -1;
			}
		}
	}
}
package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.*;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.item.consumable.Consumable;
import bloodandmithril.item.equipment.Equipable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;

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
	public Container host;

	/**
	 * Constructor
	 */
	@Deprecated
	public InventoryWindow(
			Container host,
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
			Container host,
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
	public void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
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
	}


	@Override
	protected void internalWindowRender() {
		int i = 0;
		Shaders.filter.setUniformf("color", borderColor.r, borderColor.g, borderColor.b, active ? borderColor.a : 0.7f * alpha);
		Fortress.spriteBatch.setShader(Shaders.filter);
		shapeRenderer.begin(ShapeType.FilledRectangle);
		Color color = new Color(borderColor.r, borderColor.g, borderColor.b, alpha);
		shapeRenderer.filledRect(x + length - 88, y + 25 - height, 2, height - 45, Color.CLEAR, Color.CLEAR, color, color);
		shapeRenderer.end();

		for(Entry<InventoryWindowItem, Integer> item : equippedItemsToDisplay.entrySet()) {
			if (y - i * 20 - 110 < y - height) {
				defaultFont.draw(Fortress.spriteBatch, "...", x + 6, y - i * 20 - 33);
				break;
			}
			item.getKey().button.render(x + item.getKey().button.width/2 + 6, y - i * 20 - 25, active && UserInterface.contextMenus.isEmpty(), alpha);
			defaultFont.draw(Fortress.spriteBatch, Integer.toString(item.getValue()), x + length - 80, y - i * 20 - 33);
			i++;
		}
		for(Entry<InventoryWindowItem, Integer> item : nonEquippedItemsToDisplay.entrySet()) {
			if (y - i * 20 - 110 < y - height) {
				defaultFont.draw(Fortress.spriteBatch, "...", x + 6, y - i * 20 - 33);
				break;
			}
			item.getKey().button.render(x + item.getKey().button.width/2 + 6, y - i * 20 - 25, active && UserInterface.contextMenus.isEmpty(), alpha);
			defaultFont.draw(Fortress.spriteBatch, Integer.toString(item.getValue()), x + length - 80, y - i * 20 - 33);
			i++;
		}

		Color activeColor = host.getCurrentLoad() < host.getMaxCapacity() ?
				new Color(0.7f*host.getCurrentLoad()/host.getMaxCapacity(), 1f - 0.7f * host.getCurrentLoad()/host.getMaxCapacity(), 0f, alpha) :
				Color.RED;

		Color inactiveColor = host.getCurrentLoad() < host.getMaxCapacity() ?
				new Color(0.7f*host.getCurrentLoad()/host.getMaxCapacity(), 1f - 0.7f * host.getCurrentLoad()/host.getMaxCapacity(), 0f, 0.6f * alpha) :
				new Color(1f, 0f, 0f, 0.6f);

		defaultFont.setColor(active ? activeColor : inactiveColor);
		defaultFont.draw(Fortress.spriteBatch, truncate("Weight: " + String.format("%.1f", host.getCurrentLoad()) + "/" + Float.toString(host.getMaxCapacity())), x + 6, y - height + 20);
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
			
			final ContextMenu menuToAdd = determineMenu(item.getKey());
			
			Button button = new Button(
				item.getKey().getSingular(true),
				defaultFont,
				0,
				0,
				item.getKey().getSingular(true).length() * 10,
				16,
				new Task() {
					@Override
					public void execute() {
						menuToAdd.x = Fortress.getMouseScreenX();
						menuToAdd.y = Fortress.getMouseScreenY();
					}
				},
				eq ? new Color(0f, 0.6f, 0f, 1f) : new Color(0.8f, 0.8f, 0.8f, 1f),
				Color.GREEN,
				Color.WHITE,
				UIRef.BL
			);

			if (eq) {
				this.equippedItemsToDisplay.put(
					new InventoryWindowItem(item.getKey(), button, menuToAdd),
					item.getValue()
				);
			} else {
				this.nonEquippedItemsToDisplay.put(
					new InventoryWindowItem(item.getKey(), button, menuToAdd),
					item.getValue()
				);
			}

			minLength = minLength < button.width + 100 ? button.width + 100 : minLength;
		}
	}


	/** Determines which context menu to use */
	private ContextMenu determineMenu(final Item item) {
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
			
			boolean equipped = host.getEquipped().containsKey(item);
			
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
							host.unequip(item);
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
							host.takeItem(item, 1);
							host.equip(item);
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
	private void refresh() {
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
			return item.value == o.item.value ? 0 : item.value > o.item.value ? 1 : -1;
		}
	}
}
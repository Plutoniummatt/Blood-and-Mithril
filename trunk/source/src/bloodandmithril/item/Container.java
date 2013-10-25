package bloodandmithril.item;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;


/**
 * A container that contains {@link Item}s
 *
 * @author Matt
 */
public class Container implements Serializable {
	private static final long serialVersionUID = -8874868600941684035L;

	/** How much this {@link Container} can carry in mass */
	protected float inventoryMassCapacity, currentLoad;

	/** What this {@link Container} has in its inventory, maps an Item to the quantity of said item */
	protected HashMap<Item, Integer> inventory = new HashMap<Item, Integer>();

	/** The current equipped {@link Item}s of this {@link Container} */
	protected HashMap<Item, Integer> equippedItems = new HashMap<Item, Integer>();

	/**
	 * Protected constructor
	 */
	protected Container(float inventoryMassCapacity) {
		this.inventoryMassCapacity = inventoryMassCapacity;
	}


	/**
	 * @param item to put
	 * @param quantity of item to put
	 */
	public void giveItem(Item item, int quantity) {
		if (quantity <= 0) {
			Logger.generalDebug("Can not give " + quantity + " items", LogLevel.WARN);
			return;
		}

		HashMap<Item, Integer> copy = new HashMap<Item, Integer>(inventory);

		if (inventory.isEmpty()) {
			copy.put(item, quantity);
		} else  {
			for (Entry<Item, Integer> entry : inventory.entrySet()) {
				if (item.sameAs(entry.getKey())) {
					copy.put(entry.getKey(), entry.getValue() + quantity);
					break;
				}
			}
			copy.put(item, quantity);
		}
		
		inventory = copy;
		refreshCurrentLoad();
	}
	
	
	/**
	 * Equip an {@link Item}
	 */
	public void equip(Item item) {
		for (Item equipped : equippedItems.keySet()) {
			if (equipped.sameAs(item)) {
				return;
			}
		}
		
		takeItem(item, 1);
		equippedItems.put(item, 1);
		refreshCurrentLoad();
	}
	
	
	/**
	 * Equip an {@link Item}
	 */
	public void unequip(Item item) {
		if (equippedItems.containsKey(item)) {
			equippedItems.remove(item);
			inventory.put(item, inventory.get(item) + 1);
			refreshCurrentLoad();
		}
	}
	
	
	/**
	 * Takes a number of items
	 * @return the number of items taken.
	 */
	public int takeItem(Item item, int quantity) {
		if (quantity <= 0) {
			Logger.generalDebug("Can not take " + quantity + " items", LogLevel.WARN);
			return 0;
		}

		int taken = 0;
		HashMap<Item, Integer> copy = new HashMap<Item, Integer>(inventory);
		for (Entry<Item, Integer> entry : inventory.entrySet()) {
			if (item.sameAs(entry.getKey())) {
				if (entry.getValue() - quantity <= 0) {
					taken = entry.getValue();
					copy.remove(entry.getKey());
					break;
				}
				copy.put(entry.getKey(), entry.getValue() - quantity);
				taken = quantity;
				break;
			}
		}
		
		inventory = copy;
		refreshCurrentLoad();
		return taken;
	}


	/**
	 * @return the equipped items
	 */
	public HashMap<Item, Integer> getEquipped() {
		return equippedItems;
	}


	/**
	 * @return the inventory
	 */
	public HashMap<Item, Integer> getInventory() {
		return inventory;
	}


	/**
	 * @return the maximum weight that can be stored in this {@link Container}
	 */
	public float getMaxCapacity() {
		return inventoryMassCapacity;
	}


	/**
	 * @return the current weight that is stored in the {@link Container}
	 */
	public float getCurrentLoad() {
		return currentLoad;
	}


	/** Refreshes the {@link #currentLoad} */
	private void refreshCurrentLoad() {
		float weight = 0f;
		for (Entry<Item, Integer> entry : inventory.entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().mass;
		}
		for (Entry<Item, Integer> entry : equippedItems.entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().mass;
		}
		currentLoad = weight;
	}
}

package bloodandmithril.item;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;


/**
 * A container that contains {@link Item}s
 *
 * @author Matt
 */
public class Container implements Serializable {
	private static final long serialVersionUID = -8874868600941684035L;

	/** How much this {@link Container} can carry in mass */
	protected float inventoryMassCapacity, currentLoad;

	/** Whether this container can hold more than its capacity */
	protected boolean canExceedCapacity;

	/** What this {@link Container} has in its inventory, maps an Item to the quantity of said item */
	protected HashMap<Item, Integer> inventory = new HashMap<Item, Integer>();

	/**
	 * Protected constructor
	 */
	protected Container(float inventoryMassCapacity, boolean canExceedCapacity) {
		this.inventoryMassCapacity = inventoryMassCapacity;
		this.canExceedCapacity = canExceedCapacity;
	}


	/**
	 * Synchronizes this container with another
	 */
	public void synchronize(Container other) {
		this.inventory.clear();
		this.inventory.putAll(other.inventory);

		this.inventoryMassCapacity = other.inventoryMassCapacity;
		this.currentLoad = other.currentLoad;
		this.canExceedCapacity = other.canExceedCapacity;

		refreshCurrentLoad();
	}


	/**
	 * @param item to put
	 * @param quantity of item to put
	 */
	public synchronized void giveItem(Item item) {
		HashMap<Item, Integer> copy = new HashMap<Item, Integer>(inventory);

		if (inventory.isEmpty()) {
			copy.put(item, 1);
		} else {
			boolean stacked = false;
			for (Entry<Item, Integer> entry : inventory.entrySet()) {
				if (item.sameAs(entry.getKey())) {
					copy.put(entry.getKey(), entry.getValue() + 1);
					stacked = true;
					break;
				}
			}

			if (!stacked) {
				copy.put(item, 1);
			}
		}

		inventory = copy;
		refreshCurrentLoad();
	}


	/**
	 * Takes a number of items
	 * @return the number of items taken.
	 */
	public synchronized int takeItem(Item item) {
		int taken = 0;
		HashMap<Item, Integer> copy = new HashMap<Item, Integer>(inventory);
		for (Entry<Item, Integer> entry : inventory.entrySet()) {
			if (item.sameAs(entry.getKey())) {
				if (entry.getValue() - 1 <= 0) {
					taken = entry.getValue();
					copy.remove(entry.getKey());
					break;
				}
				copy.put(entry.getKey(), entry.getValue() - 1);
				taken = 1;
				break;
			}
		}

		inventory = copy;
		refreshCurrentLoad();
		return taken;
	}


	/**
	 * @return the inventory
	 */
	public synchronized HashMap<Item, Integer> getInventory() {
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
		currentLoad = weight;
	}
}

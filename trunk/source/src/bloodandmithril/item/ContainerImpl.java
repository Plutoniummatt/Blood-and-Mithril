package bloodandmithril.item;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Default implementation of container.
 *
 * @author Matt
 */
public class ContainerImpl implements Container, Serializable {
	private static final long serialVersionUID = -8874868600941684035L;

	/** How much this {@link ContainerImpl} can carry in mass */
	private float inventoryMassCapacity;

	protected float currentLoad;

	/** Whether this container can hold more than its capacity */
	private boolean canExceedCapacity;

	/** What this {@link ContainerImpl} has in its inventory, maps an Item to the quantity of said item */
	protected Map<Item, Integer> inventory = new ConcurrentHashMap<>();

	/** Whether or not this {@link ContainerImpl} is locked */
	private boolean locked;
	
	/** The function that determines if an {@link Item} can unlock this {@link ContainerImpl} */
	private transient Function<Item, Boolean> unlockingFunction;

	/**
	 * Constructor for non-lockable container.
	 */
	public ContainerImpl(float inventoryMassCapacity, boolean canExceedCapacity) {
		this.inventoryMassCapacity = inventoryMassCapacity;
		this.canExceedCapacity = canExceedCapacity;
		this.locked = false;
	}

	
	/**
	 * Constructor for a lockable container.
	 */
	public ContainerImpl(float inventoryMassCapacity, boolean canExceedCapacity, boolean locked, Function<Item, Boolean> unlockingFunction) {
		this.inventoryMassCapacity = inventoryMassCapacity;
		this.canExceedCapacity = canExceedCapacity;
		this.locked = locked;
		this.setUnlockingFunction(unlockingFunction);
	}


	@Override
	public void synchronizeContainer(Container other) {
		this.inventory.clear();
		this.inventory.putAll(other.getInventory());

		this.inventoryMassCapacity = (other.getMaxCapacity());
		this.currentLoad = other.getCurrentLoad();
		this.canExceedCapacity = (other.canExceedCapacity());
		this.locked = other.isLocked();

		refreshCurrentLoad();
	}


	@Override
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


	@Override
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


	@Override
	public synchronized Map<Item, Integer> getInventory() {
		return inventory;
	}


	@Override
	public float getMaxCapacity() {
		return inventoryMassCapacity;
	}


	@Override
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


	@Override
	public boolean canExceedCapacity() {
		return canExceedCapacity;
	}


	@Override
	public boolean isLocked() {
		return locked;
	}


	@Override
	public boolean unlock(Item with) {
		if (getUnlockingFunction().apply(with)) {
			locked = false;
			return true;
		}
		
		return false;
	}


	@Override
	public boolean lock(Item with) {
		if (getUnlockingFunction().apply(with)) {
			locked = true;
			return true;
		}
		
		return false;
	}


	public Function<Item, Boolean> getUnlockingFunction() {
		return unlockingFunction;
	}


	public void setUnlockingFunction(Function<Item, Boolean> unlockingFunction) {
		this.unlockingFunction = unlockingFunction;
	}
}
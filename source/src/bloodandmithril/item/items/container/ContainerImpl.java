package bloodandmithril.item.items.container;

import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * Default implementation of container.
 *
 * @author Matt
 */
public class ContainerImpl implements Container, Serializable {
	private static final long serialVersionUID = -8874868600941684035L;

	/** How much this {@link ContainerImpl} can carry in mass and volume */
	private float inventoryMassCapacity;
	private int inventoryVolumeCapacity;

	protected float currentLoad;
	protected int currentVolume;

	/** What this {@link ContainerImpl} has in its inventory, maps an Item to the quantity of said item */
	protected Map<Item, Integer> inventory = new ConcurrentHashMap<>();

	/** Whether or not this {@link ContainerImpl} is locked */
	private boolean locked, lockable;

	/** The function that determines if an {@link Item} can unlock this {@link ContainerImpl} */
	private transient Function<Item, Boolean> unlockingFunction;

	/**
	 * Constructor for non-lockable container.
	 */
	public ContainerImpl(float inventoryMassCapacity, int inventoryVolumeCapacity) {
		this.inventoryMassCapacity = inventoryMassCapacity;
		this.inventoryVolumeCapacity = inventoryVolumeCapacity;
		this.locked = false;
		this.lockable = false;
	}


	/**
	 * Constructor for a lockable container.
	 */
	public ContainerImpl(float inventoryMassCapacity, int inventoryVolumeCapacity, boolean locked, Function<Item, Boolean> unlockingFunction) {
		this.inventoryMassCapacity = inventoryMassCapacity;
		this.inventoryVolumeCapacity = inventoryVolumeCapacity;
		this.locked = locked;
		this.setUnlockingFunction(unlockingFunction);
		this.lockable = true;
	}


	@Override
	public void synchronizeContainer(Container other) {
		this.inventory.clear();
		this.inventory.putAll(other.getInventory());

		this.inventoryMassCapacity = other.getMaxCapacity();
		this.inventoryVolumeCapacity = other.getMaxVolume();
		this.currentLoad = other.getCurrentLoad();
		this.currentVolume = other.getCurrentVolume();
		this.locked = other.isLocked();
		this.lockable = other.isLockable();

		refreshCurrentLoadAndVolume();
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
		refreshCurrentLoadAndVolume();
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
		refreshCurrentLoadAndVolume();
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
	private void refreshCurrentLoadAndVolume() {
		float weight = 0f;
		int volume = 0;
		for (Entry<Item, Integer> entry : inventory.entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().getMass();
			volume = volume + entry.getValue() * entry.getKey().getVolume();
		}
		currentLoad = weight;
		currentVolume = volume;
	}


	@Override
	public int getMaxVolume() {
		return inventoryVolumeCapacity;
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


	@Override
	public boolean isLockable() {
		return lockable;
	}


	public void setLockable(boolean lockable) {
		this.lockable = lockable;
	}


	@Override
	public int has(Item item) {
		for (Entry<Item, Integer> entry : inventory.entrySet()) {
			if (entry.getKey().sameAs(item)) {
				return entry.getValue();
			}
		}
		return 0;
	}


	public static void discard(Individual individual, final Item item, int quantity, bloodandmithril.util.Function<Vector2> v) {
		if (isServer()) {
			for (int i = quantity; i !=0; i--) {
				if (individual.takeItem(item) == 1) {
					Domain.addItem(
						item.copy(),
						individual.getEmissionPosition(),
						v.call(),
						individual.getWorldId()
					);
				} else {
					break;
				}
			}
		} else {
			ClientServerInterface.SendRequest.sendDiscardItemRequest(individual, item, quantity);
		}
	}


	public static void discard(Individual individual, final Item item, int quantity) {
		discard(individual, item, quantity, () -> {
			return new Vector2(100f, 0).rotate(Util.getRandom().nextFloat() * 180f);
		});
	}


	@Override
	public Container getContainerImpl() {
		return this;
	}


	@Override
	public int getCurrentVolume() {
		return currentVolume;
	}


	@Override
	public void setCurrentVolume(int volume) {
		this.currentVolume = volume;
	}


	@Override
	public void setCurrentLoad(float currentLoad) {
		this.currentLoad = currentLoad;
	}


	@Override
	public boolean canReceive(Item item) {
		if (currentLoad + item.getMass() > inventoryMassCapacity) {
			return false;
		}

		if (currentVolume + item.getVolume() > inventoryVolumeCapacity) {
			return false;
		}

		return true;
	}
	
	
	@Override
	public boolean canReceive(Collection<Item> items) {
		float totalMass = 0f;
		float totalVolume = 0;
		
		for (Item item : items) {
			totalMass += item.getMass();
			totalVolume += item.getVolume();
		}
		
		if (currentLoad + totalMass > inventoryMassCapacity) {
			return false;
		}

		if (currentVolume + totalVolume > inventoryVolumeCapacity) {
			return false;
		}

		return true;
	}


	@Override
	public boolean isEmpty() {
		return inventory.isEmpty();
	}
}
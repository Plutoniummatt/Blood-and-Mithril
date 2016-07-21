package bloodandmithril.item.items.container;

import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

/**
 * Default implementation of container.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public final class ContainerImpl implements Container {
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
	public ContainerImpl(final float inventoryMassCapacity, final int inventoryVolumeCapacity) {
		this.inventoryMassCapacity = inventoryMassCapacity;
		this.inventoryVolumeCapacity = inventoryVolumeCapacity;
		this.locked = false;
		this.lockable = false;
	}


	/**
	 * Constructor for a lockable container.
	 */
	public ContainerImpl(final float inventoryMassCapacity, final int inventoryVolumeCapacity, final boolean locked, final Function<Item, Boolean> unlockingFunction) {
		this.inventoryMassCapacity = inventoryMassCapacity;
		this.inventoryVolumeCapacity = inventoryVolumeCapacity;
		this.locked = locked;
		this.setUnlockingFunction(unlockingFunction);
		this.lockable = true;
	}


	@Override
	public void synchronizeContainer(final Container other) {
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
	public synchronized void giveItem(final Item item) {
		if (item instanceof RangedWeapon) {
			((RangedWeapon) item).setAmmo(null);
		}

		final HashMap<Item, Integer> copy = new HashMap<Item, Integer>(inventory);

		if (inventory.isEmpty()) {
			copy.put(item.copy(), 1);
		} else {
			boolean stacked = false;
			for (final Entry<Item, Integer> entry : inventory.entrySet()) {
				if (item.sameAs(entry.getKey())) {
					copy.put(entry.getKey(), entry.getValue() + 1);
					stacked = true;
					break;
				}
			}

			if (!stacked) {
				copy.put(item.copy(), 1);
			}
		}

		inventory = copy;
		refreshCurrentLoadAndVolume();
	}


	@Override
	public synchronized int takeItem(final Item item) {
		if (item instanceof RangedWeapon) {
			((RangedWeapon) item).setAmmo(null);
		}

		int taken = 0;
		final HashMap<Item, Integer> copy = new HashMap<Item, Integer>(inventory);
		for (final Entry<Item, Integer> entry : inventory.entrySet()) {
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
		for (final Entry<Item, Integer> entry : inventory.entrySet()) {
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
	public boolean unlock(final Item with) {
		if (getUnlockingFunction().apply(with)) {
			locked = false;
			return true;
		}

		return false;
	}


	@Override
	public boolean lock(final Item with) {
		if (getUnlockingFunction().apply(with)) {
			locked = true;
			return true;
		}

		return false;
	}


	public Function<Item, Boolean> getUnlockingFunction() {
		return unlockingFunction;
	}


	public void setUnlockingFunction(final Function<Item, Boolean> unlockingFunction) {
		this.unlockingFunction = unlockingFunction;
	}


	@Override
	public boolean isLockable() {
		return lockable;
	}


	public void setLockable(final boolean lockable) {
		this.lockable = lockable;
	}


	@Override
	public int has(final Item item) {
		for (final Entry<Item, Integer> entry : inventory.entrySet()) {
			if (entry.getKey().sameAs(item)) {
				return entry.getValue();
			}
		}
		return 0;
	}


	public static void discard(final Individual individual, final Item item, final int quantity, final bloodandmithril.util.Function<Vector2> v) {
		if (isServer()) {
			for (int i = quantity; i !=0; i--) {
				if (individual.takeItem(item) == 1) {
					Domain.getWorld(individual.getWorldId()).items().addItem(
						item.copy(),
						individual.getEmissionPosition(),
						v.call()
					);
				} else {
					break;
				}
			}
		} else {
			ClientServerInterface.SendRequest.sendDiscardItemRequest(individual, item, quantity);
		}
	}


	public static void discard(final Individual individual, final Item item, final int quantity) {
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
	public void setCurrentVolume(final int volume) {
		this.currentVolume = volume;
	}


	@Override
	public void setCurrentLoad(final float currentLoad) {
		this.currentLoad = currentLoad;
	}


	@Override
	public boolean canReceive(final Item item) {
		if (currentLoad + item.getMass() > getMaxCapacity()) {
			return false;
		}

		if (currentVolume + item.getVolume() > getMaxVolume()) {
			return false;
		}

		return true;
	}


	@Override
	public boolean canReceive(final Collection<Item> items) {
		float totalMass = 0f;
		float totalVolume = 0;

		for (final Item item : items) {
			totalMass += item.getMass();
			totalVolume += item.getVolume();
		}

		if (currentLoad + totalMass > getMaxCapacity()) {
			return false;
		}

		if (currentVolume + totalVolume > getMaxVolume()) {
			return false;
		}

		return true;
	}


	@Override
	public boolean isEmpty() {
		return inventory.isEmpty();
	}
}
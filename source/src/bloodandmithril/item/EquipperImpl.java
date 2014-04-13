package bloodandmithril.item;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Default implementation of {@link Equipper}
 *
 * @author Matt
 */
public class EquipperImpl implements Equipper, Serializable {
	private static final long serialVersionUID = -4489215845226338399L;

	/** The current equipped {@link Item}s of this {@link Container} */
	protected HashMap<Item, Integer> equippedItems = new HashMap<Item, Integer>();

	/** The current available {@link EquipmentSlot}s, maps to true if empty/available */
	protected Map<EquipmentSlot, Boolean> availableEquipmentSlots = new HashMap<>();

	private ContainerImpl container;

	/**
	 * @param inventoryMassCapacity
	 */
	public EquipperImpl(float inventoryMassCapacity) {
		this.container = new ContainerImpl(inventoryMassCapacity, true);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			availableEquipmentSlots.put(slot, true);
		}
	}


	@Override
	public Map<EquipmentSlot, Boolean> getAvailableEquipmentSlots() {
		return availableEquipmentSlots;
	}


	@Override
	public void giveItem(Item item) {
		container.giveItem(item);
		refreshCurrentLoad();
	}


	@Override
	public int takeItem(Item item) {
		int taken = container.takeItem(item);
		refreshCurrentLoad();
		return taken;
	}


	@Override
	public HashMap<Item, Integer> getEquipped() {
		return equippedItems;
	}


	@Override
	public void equip(Equipable item) {
		for (Item equipped : equippedItems.keySet()) {
			if (equipped.sameAs(item)) {
				return;
			}
		}

		if (availableEquipmentSlots.get(item.slot)) {
			takeItem(item);
			equippedItems.put(item, 1);
			availableEquipmentSlots.put(item.slot, false);
			refreshCurrentLoad();
		} else {
			for (Item eq : equippedItems.keySet()) {
				if (((Equipable)eq).slot.equals(item.slot)) {
					unequip((Equipable)eq);
					break;
				}
			}
			availableEquipmentSlots.put(item.slot, true);
			equip(item);
		}
	}


	@Override
	public void unequip(Equipable item) {
		Equipable toUnequip = null;
		for (Item equipped : equippedItems.keySet()) {
			if (equipped.sameAs(item)) {
				toUnequip = (Equipable)equipped;
			}
		}

		if (toUnequip == null) {
			return;
		}

		equippedItems.remove(toUnequip);
		container.getInventory().put(toUnequip, (container.getInventory().get(toUnequip) == null ? 0 : container.getInventory().get(toUnequip)) + 1);
		availableEquipmentSlots.put(toUnequip.slot, true);
		refreshCurrentLoad();
	}


	/** Refreshes the {@link #currentLoad} */
	private void refreshCurrentLoad() {
		float weight = 0f;
		for (Entry<Item, Integer> entry : container.getInventory().entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().mass;
		}
		for (Entry<Item, Integer> entry : equippedItems.entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().mass;
		}
		container.currentLoad = weight;
	}


	@Override
	public void synchronizeContainer(Container other) {
		container.synchronizeContainer(other);
	}


	@Override
	public Map<Item, Integer> getInventory() {
		return container.getInventory();
	}


	@Override
	public float getMaxCapacity() {
		return container.getMaxCapacity();
	}


	@Override
	public float getCurrentLoad() {
		return container.getCurrentLoad();
	}


	@Override
	public void synchronizeEquipper(Equipper other) {
		this.equippedItems = other.getEquipped();
		this.availableEquipmentSlots = other.getAvailableEquipmentSlots();
	}


	@Override
	public boolean canExceedCapacity() {
		return container.canExceedCapacity();
	}


	@Override
	public boolean isLocked() {
		return false;
	}


	@Override
	public boolean unlock(Item with) {
		return false;
	}


	@Override
	public boolean lock(Item with) {
		return false;
	}


	@Override
	public boolean isLockable() {
		return false;
	}


	@Override
	public int has(Item item) {
		return container.has(item);
	}
}
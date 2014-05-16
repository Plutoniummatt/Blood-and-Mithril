package bloodandmithril.item;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.item.equipment.Ring;
import bloodandmithril.util.Function;

/**
 * Default implementation of {@link Equipper}
 *
 * @author Matt
 */
public class EquipperImpl implements Equipper, Serializable {
	private static final long serialVersionUID = -4489215845226338399L;

	/** The current equipped {@link Item}s of this {@link Container} */
	protected HashMap<Item, Integer> equippedItems = newHashMap();

	/** The current available {@link EquipmentSlot}s, maps to true if empty/available */
	protected Map<EquipmentSlot, Function<Boolean>> availableEquipmentSlots = newHashMap();

	private List<Ring> equippedRings = newArrayList();

	private ContainerImpl containerImpl;

	private int maxRings;

	/**
	 * @param inventoryMassCapacity
	 */
	public EquipperImpl(float inventoryMassCapacity, int maxRings) {
		this.maxRings = maxRings;
		this.containerImpl = new ContainerImpl(inventoryMassCapacity, true);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot != EquipmentSlot.RING) {
				availableEquipmentSlots.put(
					slot,
					() -> {
						return true;
					}
				);
			} else {
				availableEquipmentSlots.put(
					slot,
					() -> {
						return equippedRings.size() < maxRings;
					}
				);
			}
		}
	}


	@Override
	public Equipper getEquipperImpl() {
		return this;
	}


	@Override
	public List<Ring> getEquippedRings() {
		return equippedRings;
	}


	@Override
	public Map<EquipmentSlot, Function<Boolean>> getAvailableEquipmentSlots() {
		return availableEquipmentSlots;
	}


	@Override
	public void giveItem(Item item) {
		containerImpl.giveItem(item);
		refreshCurrentLoad();
	}


	@Override
	public int takeItem(Item item) {
		int taken = containerImpl.takeItem(item);
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
			if (equipped.sameAs(item) && item.slot != EquipmentSlot.RING) {
				return;
			}
		}

		if (availableEquipmentSlots.get(item.slot).call()) {
			takeItem(item);
			equippedItems.put(item, 1);
			if (item.slot == EquipmentSlot.RING) {
				equippedRings.add((Ring)item);
			}
			availableEquipmentSlots.put(
				item.slot,
				item.slot == EquipmentSlot.RING ?
				() -> {
					return equippedRings.size() < maxRings;
				} :
				() -> {
					return false;
				}
			);
			refreshCurrentLoad();
		} else {
			for (Item eq : equippedItems.keySet()) {
				if (((Equipable)eq).slot.equals(item.slot)) {
					unequip((Equipable)eq);
					break;
				}
			}
			availableEquipmentSlots.put(
				item.slot,
				item.slot == EquipmentSlot.RING ?
				() -> {
					return equippedRings.size() < maxRings;
				} :
				() -> {
					return true;
				}
			);
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
		if (toUnequip.slot == EquipmentSlot.RING) {
			equippedRings.remove(toUnequip);
		}
		containerImpl.getInventory().put(toUnequip, (containerImpl.getInventory().get(toUnequip) == null ? 0 : containerImpl.getInventory().get(toUnequip)) + 1);
		availableEquipmentSlots.put(
			toUnequip.slot,
			item.slot == EquipmentSlot.RING ?
			() -> {
				return equippedRings.size() < maxRings;
			} :
			() -> {
				return true;
			}
		);
		refreshCurrentLoad();
	}


	/** Refreshes the {@link #currentLoad} */
	private void refreshCurrentLoad() {
		float weight = 0f;
		for (Entry<Item, Integer> entry : containerImpl.getInventory().entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().getMass();
		}
		for (Entry<Item, Integer> entry : equippedItems.entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().getMass();
		}
		containerImpl.currentLoad = weight;
	}


	@Override
	public int getMaxRings() {
		return maxRings;
	}


	@Override
	public void synchronizeEquipper(Equipper other) {
		this.equippedItems = other.getEquipped();
		this.equippedRings = other.getEquippedRings();
		this.maxRings = other.getMaxRings();
		this.availableEquipmentSlots = other.getAvailableEquipmentSlots();
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
	public Container getContainerImpl() {
		return containerImpl;
	}
}
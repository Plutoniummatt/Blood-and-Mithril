package bloodandmithril.item.items.equipment;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.equipment.misc.Ring;
import bloodandmithril.util.SerializableFunction;

/**
 * Default implementation of {@link Equipper}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class EquipperImpl implements Equipper, Serializable {
	private static final long serialVersionUID = -4489215845226338399L;

	/** The current equipped {@link Item}s of this {@link Container} */
	protected HashMap<Item, Integer> equippedItems = newHashMap();

	/** The current available {@link EquipmentSlot}s, maps to true if empty/available */
	protected Map<EquipmentSlot, SerializableFunction<Boolean>> availableEquipmentSlots = newHashMap();

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
					new AlwaysTrueFunction()
				);
			} else {
				availableEquipmentSlots.put(
					slot,
					new RingFunction()
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
	public Map<EquipmentSlot, SerializableFunction<Boolean>> getAvailableEquipmentSlots() {
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
			equippedItems.put(item, (equippedItems.get(item) == null ? 0 : equippedItems.get(item)) + 1);
			if (item.slot == EquipmentSlot.RING) {
				equippedRings.add((Ring)item);
			}
			availableEquipmentSlots.put(
				item.slot,
				item.slot == EquipmentSlot.RING ? new RingFunction() : new FalseFunction()
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
				item.slot == EquipmentSlot.RING ? new RingFunction() : new AlwaysTrueFunction()
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

		if (equippedItems.get(toUnequip) > 1) {
			equippedItems.put(toUnequip, equippedItems.get(toUnequip) - 1);
		} else {
			equippedItems.remove(toUnequip);
		}

		if (toUnequip.slot == EquipmentSlot.RING) {
			equippedRings.remove(toUnequip);
		}
		containerImpl.getInventory().put(toUnequip, (containerImpl.getInventory().get(toUnequip) == null ? 0 : containerImpl.getInventory().get(toUnequip)) + 1);
		availableEquipmentSlots.put(
			toUnequip.slot,
			item.slot == EquipmentSlot.RING ? new RingFunction() : new AlwaysTrueFunction()
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
		containerImpl.setCurrentLoad(weight);
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

	/**
	 * {@link SerializableFunction} to always return true
	 *
	 * @author Matt
	 */
	public static class AlwaysTrueFunction implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -6919306076788382244L;

		@Override
		public Boolean call() {
			return true;
		}
	}

	/**
	 * {@link SerializableFunction} to always return false
	 *
	 * @author Matt
	 */
	public class FalseFunction implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -460652673581918065L;

		@Override
		public Boolean call() {
			return false;
		}
	}

	/**
	 * {@link SerializableFunction} to determine whether another ring can be equipped
	 *
	 * @author Matt
	 */
	public class RingFunction implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -4418867523435245643L;

		@Override
		public Boolean call() {
			return equippedRings.size() < maxRings;
		}
	}
}